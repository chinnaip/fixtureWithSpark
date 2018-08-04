package com.hp.sastreaming.fixtures.kafka;

import com.hp.ts.sa.analysis.messages.SACollection;
import com.hp.ts.sa.streaming.avro.KafkaAvroDecoder;
import com.jayway.jsonpath.JsonPath;
import org.apache.kafka.clients.consumer.ConsumerConfig;
import org.apache.kafka.clients.consumer.ConsumerRecord;
import org.apache.kafka.clients.consumer.ConsumerRecords;
import org.apache.kafka.clients.consumer.KafkaConsumer;

import java.util.Collections;
import java.util.Properties;

public class Consumer {

    private final String bootstrapServers;
    private final String groupId;
    private final String topic;
    private final KafkaAvroDecoder<?> decoder;
    private String consumerResponse;

    /**
     * Consumer constructor
     *
     * @param bootstrapServers Kafka bootstrap servers. Format: Address:Port(,Address:Port)*
     * @param groupId Consumer GroupID
     * @param topic Kafka Topic Name
     */
    public Consumer(String bootstrapServers, String groupId, String topic) {
        this.bootstrapServers = bootstrapServers;
        this.groupId = groupId;
        this.topic = topic;
        this.decoder = getAvroDecoder(topic);
    }

    /**
     * Returns Kafka Consumer Properties
     */
    private Properties createConsumerConfig() {
        Properties props = new Properties();
        props.put(ConsumerConfig.BOOTSTRAP_SERVERS_CONFIG, this.bootstrapServers);
        props.put(ConsumerConfig.GROUP_ID_CONFIG, this.groupId);
        props.put(ConsumerConfig.AUTO_COMMIT_INTERVAL_MS_CONFIG, "100");
        props.put(ConsumerConfig.REQUEST_TIMEOUT_MS_CONFIG, "2000");
        return props;
    }

    /**
     * Consumes all Kafka messages
     *
     * @return last consumed message without bytes
     */
    public String consume() {
        this.consumerResponse = "";
        try (org.apache.kafka.clients.consumer.Consumer<byte[], byte[]> consumer = new KafkaConsumer<>(this.createConsumerConfig())) {
            consumer.subscribe(Collections.singletonList(this.topic));
            for (int retries = 0; retries < 30; retries++) {
                ConsumerRecords<byte[], byte[]> consumerRecords = consumer.poll(2000L);
                for (ConsumerRecord<byte[], byte[]> consumerRecord : consumerRecords) {
                    Object obj = decoder.fromBytes(consumerRecord.value());
                    this.consumerResponse = obj.toString();
                    return this.consumerResponse;
                }
            }
        }
        throw new RuntimeException("No message for topic " + this.topic);
    }

    private static KafkaAvroDecoder<?> getAvroDecoder(String topic) {
        if (topic.equals("sa_collection")) {
            return new KafkaAvroDecoder<>(SACollection.getClassSchema());
        } else {
            throw new IllegalArgumentException("Invalid topic " + topic);
        }
    }

    /**
     * Checks for jsonPath values in the response body
     *
     * @param jsonPath A valid GPath expression
     * @return value of the verified GPath expression
     */
    public String checkJsonpath(String jsonPath) throws Exception {

        Object res = this.extractJsonpath(jsonPath);
        if (res == null) {
            return "";
        }
        String result = res.toString();
        //result = result.replace("[", "");
        //result = result.replace("]", "");
        //result = result.replace("\"", "");
        return result;
    }

    /**
     * Checks for jsonPath values in the response body
     *
     * @param jsonPath A valid GPath expression
     * @return value of the verified GPath expression
     */
    private Object extractJsonpath(String jsonPath) throws Exception {
        if (jsonPath == null) {
            throw new Exception("!");
        }

        String json = this.consumerResponse;
        if (json == null) {
            throw new Exception("!!");
        }

        return JsonPath.read(json, jsonPath);
    }


}

