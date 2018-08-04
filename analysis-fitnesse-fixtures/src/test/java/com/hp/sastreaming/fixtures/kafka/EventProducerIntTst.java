package com.hp.sastreaming.fixtures.kafka;

import static org.junit.Assert.assertNotNull;

import java.io.File;
import java.net.URISyntaxException;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

/**
 * Integration tests for EventProducer and EventConsumer fixtures.<br>
 * Just run this as a Java application and ensure there are no exceptions in console output.<br>
 * <br>
 * If all tests pass then the following message is logged: *** TEST PASSED ***<br>
 * <br>
 * <br>
 * Prerequisites:<br>
 * - hdp-sandbox must point to a validate Hortonworks sandbox in host file<br>
 * - event-analysis Storm topology must be deployed.<br>
 * - jrules-event-topology Storm topology must be deployed.<br>
 * 
 * Note: A Java unit test isn't currently possible because Kafka Zookeeper and the Schema Registry mocking doesn't
 * appear to be feasible.
 * 
 */
public class EventProducerIntTst {
    private static final String HDP_SANDBOX = "hdp-sandbox";

    private static final String ZOOKEEPER_CONNECT = HDP_SANDBOX + ":5181";
    private static final String SCHEMA_REGISTRY_URL = "http://" + HDP_SANDBOX + ":8081";
    private static final String KAFKA_SERVER = HDP_SANDBOX + ":9092";

    private static final Logger logger = LoggerFactory.getLogger(EventProducerIntTst.class);

    private static final String TEST_FILE_NAME_DC = "event/dc/6ece01e0-d4c5-e511-8e59-b4b52f677526.zip";
    private static final String TEST_FILE_NAME_CC = "event/cc/Server/Proliant/Apollo_XL250aGen9/768535-B21_USE250AM07_ISEE.zip";

    public static void main(String[] args) throws Exception {
        try {
            EventProducerIntTst intTest = new EventProducerIntTst();

            intTest.test_send_dc_event();
            intTest.test_send_cc_event();

            logger.info("\n\n *** TEST PASSED ***\n");

        } catch (Throwable t) {
            logger.info("\n\n *** TEST FAILED ***\n");
            logger.error("Unexpected exception:", t);
            System.exit(-1);
        }
    }


    public void test_send_dc_event() throws Exception {
        EventPackage event = parameterizedEvent(TEST_FILE_NAME_DC, "event_analyzed");

        assertNotNull(event);
        assertNotNull(event.attachment("index.xml"));
    }

    public void test_send_cc_event() throws Exception {
        EventPackage event = parameterizedEvent(TEST_FILE_NAME_CC, "event_actionable");

        assertNotNull(event);
        assertNotNull(event.attachment("current_event.xml"));
    }

    private EventPackage parameterizedEvent(String eventFilePath, String consumerTopic) throws Exception {
        File dataPackageFile = getDataPackageFile(eventFilePath);
        EventProducer producer = new EventProducer(KAFKA_SERVER, SCHEMA_REGISTRY_URL);
        EventConsumer consumer = new EventConsumer(KAFKA_SERVER, SCHEMA_REGISTRY_URL, "fitnesse", consumerTopic);

        producer.produce(dataPackageFile.getAbsolutePath());
        consumer.setExpectedThingformationId(producer.getThingformationId());

        return consumer.consume();
    }


    private File getDataPackageFile(String filepath) throws URISyntaxException {
        File dataPackageFile = null;
        String baseDir = (new File(Thread.currentThread().getContextClassLoader().getResource(".").toURI()))
                .getAbsolutePath();

        baseDir = baseDir.replaceAll("test-classes", "classes");
        dataPackageFile = new File(baseDir, filepath);

        return dataPackageFile;
    }

}
