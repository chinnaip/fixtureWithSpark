package com.hp.sastreaming.fixtures.kafka;

import java.util.ArrayList;
import java.util.List;
import java.util.Properties;

import org.apache.kafka.clients.consumer.ConsumerRecord;
import org.apache.kafka.clients.consumer.ConsumerRecords;
import org.apache.kafka.clients.consumer.KafkaConsumer;

import com.hp.ts.sa.analysis.messages.deprecated.TextfileContentDatapackage;

/**
 * TODO: Why doesn't this work<br>
 * TODO: Describe this fixture and tidy up all public method comments incl. TODO: Add sample usage (ref. fitnesse.org)<br>
 * 
 * Note: This is a Kafka 0.9.x based implementation but it currently doesn't work!!!
 * 
 * @link http://www.confluent.io/blog/tutorial-getting-started-with-the-new-apache-kafka-0.9-consumer-client
 */
public class EventConsumer1 {

    // Note: These must be the same as in EventMessageCreator
    public static final String EVENT_ANALYZED_TOPIC = "event_analyzed";
    public static final String EVENT_INDICATION_TOPIC = "event_indication";
    private static final long POLL_TIMEOUT = 30000;

    private final String kafkaServer;
    private final String schemaRegistryUrl;
    private final String groupId;
    private final List<String> topics;

    public EventConsumer1(String kafaServer, String schemaRegistryUrl, String groupId, String topic) {
        this.kafkaServer = kafaServer;
        this.schemaRegistryUrl = schemaRegistryUrl;
        this.groupId = groupId;
        topics = new ArrayList<String>();
        topics.add(topic);
    }

    private KafkaConsumer<String, TextfileContentDatapackage> createKafkaConsumer() {
        KafkaConsumer<String, TextfileContentDatapackage> consumer;
        Properties props = new Properties();

        props.put(org.apache.kafka.clients.consumer.ConsumerConfig.BOOTSTRAP_SERVERS_CONFIG, kafkaServer);
        props.put(org.apache.kafka.clients.consumer.ConsumerConfig.GROUP_ID_CONFIG, groupId);
        props.put(org.apache.kafka.clients.consumer.ConsumerConfig.KEY_DESERIALIZER_CLASS_CONFIG,
                io.confluent.kafka.serializers.KafkaAvroDeserializer.class.getName());
        props.put(org.apache.kafka.clients.consumer.ConsumerConfig.VALUE_DESERIALIZER_CLASS_CONFIG,
                io.confluent.kafka.serializers.KafkaAvroDeserializer.class.getName());
        props.put("schema.registry.url", schemaRegistryUrl);

        consumer = new KafkaConsumer<String, TextfileContentDatapackage>(props);

        return consumer;
    }
    /**
     * Consume next Kafka message
     * 
     * @return consumed message as a String or "" if there are no messages available
     */
    public String consume() throws Exception {

        String result = "";

        KafkaConsumer<String, TextfileContentDatapackage> consumer = null;

        try {
            consumer = createKafkaConsumer();
            consumer.subscribe(topics);

            ConsumerRecords<String, TextfileContentDatapackage> records = consumer.poll(POLL_TIMEOUT);

            if (!records.isEmpty()) {
                ConsumerRecord<String, TextfileContentDatapackage> record = records.iterator().next();

                switch (topics.get(0)) {
                    case EVENT_ANALYZED_TOPIC:
                    case EVENT_INDICATION_TOPIC:
                        TextfileContentDatapackage message = record.value();

                        result = message.toString();
                        break;
                }
            }
        } finally {
            if (consumer != null) {
                consumer.close();
            }
        }

        return result;
    }

}

