package com.hp.sastreaming.fixtures.kafka;

import java.io.File;
import java.io.UnsupportedEncodingException;
import java.net.URISyntaxException;
import java.nio.ByteBuffer;
import java.nio.charset.StandardCharsets;
import java.util.List;
import java.util.Properties;
import java.util.UUID;

import com.hp.curiosity.store.CuriosityStorage;
import com.hp.curiosity.store.CuriosityStorageNativeFS;
import org.apache.kafka.clients.producer.Callback;
import org.apache.kafka.clients.producer.KafkaProducer;
import org.apache.kafka.clients.producer.ProducerConfig;
import org.apache.kafka.clients.producer.ProducerRecord;
import org.apache.kafka.clients.producer.RecordMetadata;
import org.apache.kafka.common.PartitionInfo;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import com.google.common.io.Files;
import com.hp.curiosity.store.AttachmentReader;
import com.hp.ts.sa.analysis.messages.deprecated.TextfileContentDatapackage;
import com.hp.ts.sa.platform.messages.Attachment;
import com.hp.ts.sa.platform.messages.Audit;
import com.hp.ts.sa.platform.messages.Thingformation;
import com.hp.ts.sa.platform.messages.ThingformationType;

/**
 * This is a convenience class for constructing a Kafka producer specifically for submitting Event messages.<br>
 * This class can be used programmatically or as a Fixture in a Fitnesse test.<br>
 * <br>
 * The produce method expects a Data Package file and then this is converted into a Thingformation message and the
 * EventMessageCreator is used to convert this into an Event messages which is then submitted to the appropriate Kafka
 * topic.<br>
 * <br>
 * Example fixture usage:
 * 
 * <pre>
 * !2 Submit Event
 * !| script|Event Producer  |${KAFKA_SERVER}|${SCHEMA_REGISTRY_URLS}  |
 * |setThingformationTypeId; |$tft_uuid                                |
 * |setThingId;              |$thing_uuid                              |
 * |produce                  |${EVENT_FILE_PATH}                       |
 * |$tf_uuid=                |getThingformationId;                     |
 * </pre>
 * 
 * Note 1: This implementation uses a hard coded ThingformationTypeId and ThingId by default.<br>
 * Note 2: This is a Kafka 0.9.x based implementation<br>
 * 
 */
public class EventProducer {
    private static final Logger logger = LoggerFactory.getLogger(EventProducer.class);

    // Note: These are just default values which can be overridden by the corresponding setters!
    private static final String THINGFORMATION_TYPE_NAME = "Event"; // SA name for 'Event' Thingformations
    private static final String THINGFORMATION_TYPE_UUID = "a4507f8d-b28c-40a6-9d2a-ff9b70e9f1c0";
    private static final String NULL_UUID = (new UUID(0, 0)).toString();
    private static final String THING_ID = NULL_UUID;
    private static final String FITNESSE = "Fitnesse";

    private final String kafkaServer;
    private final String schemaRegistryUrl;
    private String thingformationTypeId;
    private String thingId;
    private Thingformation thingformation;
    
    private boolean produceCompleted;
    private Exception produceException;

    /**
     * An Event message producer is instantiated by providing a valid Kafka address and schema registry URL name.<br>
     * 
     * @param kafkaServer Kafka hostname:port (e.g. hdp-sandbox:9092)
     * @param schemaRegistryUrl Web address of the registry (e.g. http://hdp-sandbox:8081)
     */
    public EventProducer(String kafkaServer, String schemaRegistryUrl) {
        this.kafkaServer = kafkaServer;
        this.schemaRegistryUrl = schemaRegistryUrl;
        thingformationTypeId = THINGFORMATION_TYPE_UUID;
        thingId = THING_ID;
    }

    /**
     * @return ID of produced Thingformation
     */
    public String getThingformationId() {
        return thingformation.getThingformationId();
    }

    /**
     * Set ThingformationType ID with one fetched via REST API with "name=Event".<br>
     * 
     * @param tft_uuid A valid SA Curisoity ThingformationType ID
     */
    public void setThingformationTypeId(String tft_uuid) {
        this.thingformationTypeId = tft_uuid;
    }

    /**
     * Set Thing ID with one fetched via REST API for an existing Thing.<br>
     * 
     * @param thing_uuid A valid SA Curisoity Thing ID
     */
    public void setThingId(String thing_uuid) {
        this.thingId = thing_uuid;
    }

    private KafkaProducer<String, TextfileContentDatapackage> createKafkaProducer() {
        KafkaProducer<String, TextfileContentDatapackage> producer;
        Properties props = new Properties();

        props.put(ProducerConfig.BOOTSTRAP_SERVERS_CONFIG, kafkaServer);
        props.put(ProducerConfig.KEY_SERIALIZER_CLASS_CONFIG, io.confluent.kafka.serializers.KafkaAvroSerializer.class);
        props.put(ProducerConfig.VALUE_SERIALIZER_CLASS_CONFIG,
                io.confluent.kafka.serializers.KafkaAvroSerializer.class);
        props.put("schema.registry.url", schemaRegistryUrl);

        producer = new KafkaProducer<String, TextfileContentDatapackage>(props);

        return producer;
    }

    /**
     * Post the specified Event data package to a topic.<br>
     * This method waits up to 30s for an acknowledge and re-throws any exceptions thrown by the Kafka framework.<br>
     * Note: If the topic doesn't exist then this isn't detected because Kafka automatically creates it in this case.<br>
     * 
     * @param dataPackagePath Path to an Event Data Package file
     */
    public void produce(String dataPackagePath) throws Exception {
        File dataPackageFile = new File(dataPackagePath);
        KafkaProducer<String, TextfileContentDatapackage> producer = createKafkaProducer();
        AttachmentReader attachmentReader = createAttachmentReader();

        Attachment attachment = buildAttachment(dataPackageFile.getName(), dataPackageFile);
        long startTime = System.currentTimeMillis();
        EventMessageCreator creator;

        final Callback callback = new Callback() {
            
            @Override
            public void onCompletion(RecordMetadata metadata, Exception e) {
                setProduceCompleted(true, e);
            }
        };

        thingformation = buildThingformation(attachment);

        creator = new EventMessageCreator(producer, attachmentReader) {
            @Override
            protected void postMessageToKafka(TextfileContentDatapackage eventMessage, String topic) {
                try {
                    ProducerRecord<String, TextfileContentDatapackage> producerRecord = new ProducerRecord<>(topic,
                            eventMessage);
                    List<PartitionInfo> partitions = eventMessageProducer.partitionsFor(topic);

                    eventMessageProducer.send(producerRecord, callback);

                    logger.debug("Posted Event message with Thingformation ID '{}' to topic '{}' :  \n {}",
                            eventMessage.getThingformationId(), topic, eventMessage);

                } catch (Exception e) {
                    setProduceCompleted(true, e);
                }
            }
        };
        try {
            setProduceCompleted(false, null);
            creator.postMessage(thingformation);

            while (!produceCompleted && (System.currentTimeMillis() - startTime < 30000)) {
                Thread.sleep(250);
            }

            if (produceException != null) {
                throw produceException;

            } else if (!produceCompleted) {
                throw new RuntimeException("Timeout waiting for produce to complete!");
            }

        } finally {
            producer.close();
        }
    }

    protected void setProduceCompleted(boolean produceCompleted, Exception produceException) {
        this.produceCompleted = produceCompleted;
        this.produceException = produceException;
    }

    private AttachmentReader createAttachmentReader() throws URISyntaxException {
        // Note: CuriosityStorageFactory could be used here but
        String baseDir = "";
        try {
            baseDir = (new File(Thread.currentThread().getContextClassLoader().getResource(".").toURI())).getAbsolutePath();
        } catch (URISyntaxException e) {
            throw e;
        }

        CuriosityStorage storage = new CuriosityStorageNativeFS(baseDir, 4096);
        AttachmentReader attachmentReader = new AttachmentReader(storage);
        return attachmentReader;
    }

    private Thingformation buildThingformation(Attachment attachment) throws UnsupportedEncodingException {
        ThingformationType type = new ThingformationType();
        type.setThingformationTypeId(thingformationTypeId);
        type.setTypeName(THINGFORMATION_TYPE_NAME);

        Thingformation thingformation = new Thingformation();
        thingformation.setTimeStamp(System.currentTimeMillis());
        thingformation.setThingformationId(UUID.randomUUID().toString());
        thingformation.setThingformationType(type);
        thingformation.setThingId(thingId);
        thingformation.setAttachment(attachment);
        thingformation.setAudit(createAudit(thingformation));

        return thingformation;
    }

    private Audit createAudit(Thingformation thingformation) throws UnsupportedEncodingException {
        Audit audit = new Audit();

        audit.setCreationTime(System.currentTimeMillis());
        audit.setLogTrackingId(thingformation.getThingformationId()); // Note: Not sure if this is what happens in the
                                                                      // Curiosity API!
        // Note: Since we're by passing curiosity these fields aren't applicable!
//        audit.setCuriosityAuthToken(ByteBuffer.wrap(NULL_UUID.getBytes(StandardCharsets.UTF_8)));
        audit.setCuriosityCommunicationChannelId(NULL_UUID);
        audit.setCuriosityGroupAccountId(NULL_UUID);
        audit.setCuriosityEstablishingUser(FITNESSE);

        return audit;
    }

    private Attachment buildAttachment(String fileID, File file) throws Exception {
        Attachment attachment = new Attachment();
        attachment.setFileId(fileID);

        byte[] payload = Files.toByteArray(file);

//        attachment.setPayload(ByteBuffer.wrap(payload));
        attachment.setSize((long) payload.length);
        attachment.setName(file.getName());
        attachment.setFileId(fileID);

        return attachment;
    }
}
