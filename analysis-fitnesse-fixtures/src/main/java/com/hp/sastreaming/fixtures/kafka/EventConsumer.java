package com.hp.sastreaming.fixtures.kafka;

import java.util.Collections;
import java.util.Properties;

import org.apache.avro.generic.GenericRecord;
import org.apache.kafka.clients.consumer.ConsumerConfig;
import org.apache.kafka.clients.consumer.ConsumerRecord;
import org.apache.kafka.clients.consumer.ConsumerRecords;
import org.apache.kafka.clients.consumer.KafkaConsumer;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import com.hp.ts.sa.analysis.messages.deprecated.TextfileContentDatapackage;
import com.hp.ts.sa.common.avro.mapper.AutoMapper;


/**
 * This is a convenience class for constructing a Kafka consumer specifically for consuming Event messages.<br>
 * This class can be used programmatically or as a Fixture in a Fitnesse test.<br>
 * <br>
 * Example fixture usage:
 * <p>
 * <pre>
 * !2 Retrieve processed Event
 * !|script                     |Event Consumer|${KAFKA_SERVER}|${SCHEMA_REGISTRY_URLS}|fitnesse|${CONSUMER_TOPIC_NAME}|
 * |setExpectedThingformationId;|$tf_uuid                                                                                  |
 * |$event=                     |consume                                                                                   |
 * </pre>
 * <p>
 * Note: This is a Kafka 0.10.x implementation!!!<br>
 */
public class EventConsumer {
    private static final Logger logger = LoggerFactory.getLogger(EventConsumer.class);

    private final String bootstrapServers;
    private final String schemaRegistryUrl;
    private final String groupId;
    private final String topic;

    /**
     * ID to correlate with Event submitted to the System Under Test (SUT)
     **/
    private String expectedThingformationId;

    /**
     * An Event message consumer is instantiated by providing a valid zookeeper address, schema registry URL and topic
     * name.<br>
     *
     * @param bootstrapServers  Kafka bootstrap servers. Format: Address:Port(,Address:Port)
     * @param schemaRegistryUrl Web address of the registry (e.g. http://hdp-sandbox:8081)
     * @param groupId           A string that uniquely identifies the group of consumer processes to which this consumer belongs
     *                          (e.g. fitnesse)
     * @param topic             The name of the Kafka topic to consume Event messages from (e.g. event_analyzed)
     */
    public EventConsumer(String bootstrapServers, String schemaRegistryUrl, String groupId, String topic) {
        this.bootstrapServers = bootstrapServers;
        this.schemaRegistryUrl = schemaRegistryUrl;
        this.groupId = groupId;
        this.topic = topic;
    }

    /**
     * Consume the next Event message found with matching Thingformation ID on the Kafa topic and wrap it in an
     * EventPackage object.<br>
     * Note 1: This creates a Kafka consumer each time it is called and just consumes the first message found.<br>
     * Note 2: If the expected Thingformation ID isn't defined then just return the next message found on the topic.<br>
     *
     * @return The next Event message in an EventPackage object or null none exists
     */
    public EventPackage consume() throws Exception {
        EventPackage result = null;
        Properties consumerProps = createConsumerConfig();
        io.confluent.kafka.serializers.KafkaAvroDeserializer decoder = new io.confluent.kafka.serializers.KafkaAvroDeserializer();

        try (org.apache.kafka.clients.consumer.Consumer<byte[], byte[]> consumer = new KafkaConsumer<>(consumerProps)) {
            consumer.subscribe(Collections.singletonList(this.topic));
            for (int retries = 0; retries < 30; retries++) {
                ConsumerRecords<byte[], byte[]> consumerRecords = consumer.poll(2000L);
                for (ConsumerRecord<byte[], byte[]> consumerRecord : consumerRecords) {
                    TextfileContentDatapackage message = AutoMapper.decode(
                            (GenericRecord) decoder.deserialize(null, consumerRecord.value()),
                            TextfileContentDatapackage.SCHEMA$);
                    if (expectedThingformationId == null
                            || expectedThingformationId.equals(message.getThingformationId())) {
                        logger.debug("Matched Event with Thingformation ID: {} \n {}", message.getThingformationId(),
                                message);
                        result = new EventPackage(message);
                        return result;
                    } else {
                        logger.debug("Ignored Event with Thingformation ID: {} \n {}", message.getThingformationId(),
                                message);
                    }
                }
            }
        }
        return result;
    }

    /**
     * Set the Thingformation ID expected in the matching message to be consumed.<br>
     * This ID is used to correlate with Event submitted to the System Under Test (SUT).<br>
     *
     * @param expectedThingformationId ID of Thingformation of Event submitted to the SUT
     */
    public void setExpectedThingformationId(String expectedThingformationId) {
        this.expectedThingformationId = expectedThingformationId;
    }

    /**
     * @see https://kafka.apache.org/08/configuration.html
     */  
    private Properties createConsumerConfig() {
    	Properties props = new Properties();
    	props.put(ConsumerConfig.BOOTSTRAP_SERVERS_CONFIG, bootstrapServers);
    	props.put(ConsumerConfig.GROUP_ID_CONFIG, groupId);
    	props.put(ConsumerConfig.AUTO_COMMIT_INTERVAL_MS_CONFIG, "100");
    	props.put(ConsumerConfig.REQUEST_TIMEOUT_MS_CONFIG, "2000");
    	props.put("schema.registry.url", schemaRegistryUrl);
    	return props;
    }
}

