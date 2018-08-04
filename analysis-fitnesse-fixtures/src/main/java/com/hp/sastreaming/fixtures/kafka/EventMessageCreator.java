package com.hp.sastreaming.fixtures.kafka;

import com.google.common.base.Throwables;
import com.google.inject.Inject;
import com.hp.curiosity.store.AttachmentReader;
import com.hp.curiosity.store.CuriosityStorage;
import com.hp.curiosity.store.CuriosityStorageNativeFS;
import com.hp.it.sadb.framework.common.utils.ISadbFileExchangePathAdapter;
import com.hp.it.sadb.framework.common.utils.ISadbWorkingDirectory;
import com.hp.it.sadb.framework.common.utils.SadbInMemoryZipWorkingDirectory;
import com.hp.ts.sa.analysis.messages.deprecated.TextfileContent;
import com.hp.ts.sa.analysis.messages.deprecated.TextfileContentDatapackage;
import com.hp.ts.sa.analysis.router.processor.SAMessageCreator;
import com.hp.ts.sa.platform.messages.Attachment;
import com.hp.ts.sa.platform.messages.Thingformation;
import com.hp.ts.sa.streaming.datapackage.AnalysisAttachmentReader;
import com.hp.ts.sa.streaming.kafka.ErrorLoggingCallback;
import org.apache.commons.io.IOUtils;
import org.apache.kafka.clients.producer.Producer;
import org.apache.kafka.clients.producer.ProducerRecord;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.io.File;
import java.io.IOException;
import java.io.InputStream;
import java.io.StringWriter;
import java.net.URISyntaxException;
import java.util.ArrayList;
import java.util.Collection;
import java.util.List;
import java.util.concurrent.ExecutionException;

/**
 * Post SA Collection Messages to Kafka
 * <p/>
 * SA consumers interested in the arrival of collections can subscribe to these.
 * <p/>
 * Copyright (c) 2014 Hewlett-Packard Development Company, L.P.
 * 
 * @author markdav Date: 07/05/2015
 * 
 * @deprecated Refactor code to use latest version of class:
 *             com.hp.ts.sa.analysis.router.processor.event.EventMessageCreator;
 */
@Deprecated
public class EventMessageCreator implements SAMessageCreator {

    private static final Logger logger = LoggerFactory.getLogger(EventMessageCreator.class);
    private static final String EVENT_ANALYZED_TOPIC = "event_analyzed";
    private static final String EVENT_INDICATION_TOPIC = "event_indication";
    private static final String TEXT_ENCODING = "UTF-8";
    private static final String INLINE_EVENT_XML = "inlineevent.xml";
    private static final String INLINE_INCIDENT_XML = "inlineincident.xml";

    // todo make configurable
    private static final long MAX_FILE_SIZE_BYTES = 2097152L;

    private final AnalysisAttachmentReader analysisAttachmentReader;

    Producer<String, TextfileContentDatapackage> eventMessageProducer;

    @Inject
    public EventMessageCreator(Producer<String, TextfileContentDatapackage> eventMessageProducer,
            AttachmentReader attachmentReader) {
        this.eventMessageProducer = eventMessageProducer;
        this.analysisAttachmentReader = new AnalysisAttachmentReader(attachmentReader);
    }

    @Override
    public void postMessage(Thingformation thingformationMessage) {

        // Contains attachment? If not, exit early
        Attachment attachment = thingformationMessage.getAttachment();
        if (null == attachment) {
            logger.warn("No attachment in thingformation");
            return;
        }

        try {
            TextfileContentDatapackage eventMessage = new TextfileContentDatapackage();

            // add the curiosity attibs
            if (null != thingformationMessage.getAudit()) {
                eventMessage.setLogTrackingId(thingformationMessage.getAudit().getLogTrackingId());
            }

            // todo parse the collected time from the message if possible
            eventMessage.setCollectedDateTimeMillis(thingformationMessage.getTimeStamp());

            if (null != thingformationMessage.getAudit()
                    && null != thingformationMessage.getAudit().getCuriosityCommunicationChannelId()) {
                eventMessage.setCommunicationChannelId(thingformationMessage.getAudit()
                        .getCuriosityCommunicationChannelId());
            }

            eventMessage.setReceivedDateTimeMillis(thingformationMessage.getTimeStamp());
            if (null != thingformationMessage.getAttachment()
                    && null != thingformationMessage.getAttachment().getFileId()) {
                eventMessage.setFileId(thingformationMessage.getAttachment().getFileId());
            }

            if (null != thingformationMessage.getAudit()
                    && null != thingformationMessage.getAudit().getCuriosityGroupAccountId()) {
                eventMessage.setGroupAccountId(thingformationMessage.getAudit().getCuriosityGroupAccountId());
            }
            eventMessage.setSubmittedDateTimeMillis(thingformationMessage.getTimeStamp());
            if (null != thingformationMessage.getThingformationId()) {
                eventMessage.setThingformationId(thingformationMessage.getThingformationId());
            }
            if (null != thingformationMessage.getThingId()) {
                eventMessage.setThingId(thingformationMessage.getThingId().toString());
            }

            // add files
            eventMessage.setDatapackageFiles(this.getFileContent(attachment));

            if (isDirectConnect(eventMessage)) {
                postMessageToKafka(eventMessage, EVENT_INDICATION_TOPIC);
            } else {
                postMessageToKafka(eventMessage, EVENT_ANALYZED_TOPIC);
            }
        } catch (Throwable t) {
            Throwables.propagate(t);
        }
    }

    /**
     * checks if the event comes from a direct connect end point: iLO/OA, STaTS Bridge, OneView/Fusion
     * 
     * @param eventMessage
     * @return isDirectConnect
     */
    private boolean isDirectConnect(TextfileContentDatapackage eventMessage) {
        boolean isDirectConnect = false;
        for (TextfileContent file : eventMessage.getDatapackageFiles()) {
            String fileName = file.getSourceFileName().toLowerCase();
            if (fileName.contains(INLINE_EVENT_XML) || fileName.equals(INLINE_INCIDENT_XML)) {
                isDirectConnect = true;
                break;
            }
        }
        return isDirectConnect;
    }

    /**
     * Retrieves the content of text files in the datapackage and creates a list that can be added to the event message
     * 
     * Only files of size < MAX_CONTENT_SIZE are stored
     * 
     * @param attachment the attacment object from the data message
     * @return the content of the text file
     * @throws IOException exception
     * @throws StreamNullException exteption
     */
    private List<TextfileContent> getFileContent(Attachment attachment) throws IOException, StreamNullException, URISyntaxException {
        InputStream fileInputStream = null;

        List<TextfileContent> fileContentList = new ArrayList<>();

        File ROOT_DIR = new File(Thread.currentThread().getContextClassLoader().getResource(".").toURI());
        CuriosityStorage curiosityStorage =  new CuriosityStorageNativeFS(ROOT_DIR.toString(), 20480);
        AnalysisAttachmentReader analysisAttachmentReader = new AnalysisAttachmentReader(new AttachmentReader(curiosityStorage));

        try (InputStream inputStream = analysisAttachmentReader.getPayloadStream(attachment)) {
            if (inputStream == null) {
                throw new StreamNullException();
            }

            ISadbWorkingDirectory workingDirectory;
            try {
                workingDirectory = new SadbInMemoryZipWorkingDirectory(inputStream);
                Collection<ISadbFileExchangePathAdapter> files = workingDirectory.listFiles();
                for (ISadbFileExchangePathAdapter file : files) {
                    if (!file.isDirectory()) {
                        TextfileContent fileContent = new TextfileContent();
                        fileContent.setSourceFileName(file.getFileName());
                        fileContent.setSourceFileMimeType("text");
                        StringWriter writer = new StringWriter();
                        fileInputStream = file.newInputStream();
                        IOUtils.copy(fileInputStream, writer, TEXT_ENCODING);
                        String fileString = writer.toString();
                        if (fileString.getBytes(TEXT_ENCODING).length < MAX_FILE_SIZE_BYTES) {
                            fileContent.setSourceFileContent(fileString);
                        } else {
                            fileContent.setSourceFileContent("File Size > " + MAX_FILE_SIZE_BYTES);
                        }
                        fileContentList.add(fileContent);
                        fileInputStream.close();
                    }
                }

            } catch (IOException e) {
                logger.warn("IOException opening zip file as working directory.", e);
            } finally {
                IOUtils.closeQuietly(fileInputStream);
            }

        }
        return (fileContentList);
    }

    protected void postMessageToKafka(TextfileContentDatapackage eventMessage, String topic) throws ExecutionException, InterruptedException {
        ProducerRecord<String, TextfileContentDatapackage> producerRecord = new ProducerRecord<>(topic, eventMessage);
        eventMessageProducer.send(producerRecord, new ErrorLoggingCallback());
    }

    /**
     * Internal to this class for control flow when InputStream is null
     */
    private static class StreamNullException extends Exception {
    }
}
