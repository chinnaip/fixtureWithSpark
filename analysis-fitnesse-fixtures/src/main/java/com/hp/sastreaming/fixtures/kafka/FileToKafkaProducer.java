package com.hp.sastreaming.fixtures.kafka;


import org.apache.commons.io.FileUtils;
import org.apache.kafka.clients.producer.KafkaProducer;
import org.apache.kafka.clients.producer.Producer;
import org.apache.kafka.clients.producer.ProducerRecord;

import java.io.File;
import java.util.List;
import java.util.Map;
import java.util.Properties;

public class FileToKafkaProducer {

    private static final String RANDOM_SERIAL_NUMBER = "RANDOM_SERIAL_NUMBER";

    public void write(String filePath, String topicName, Map<String, Object> configProperties, String serialNo) throws Exception {
        Properties properties = new Properties();
        properties.putAll(configProperties);
        //Configure the Producer
        Producer producer = new KafkaProducer<String, String>(properties);

        List<String> fileLines = FileUtils.readLines(new File(filePath), "UTF-8");
        for (String fileLine : fileLines) {
            String replacedString = fileLine.replace(RANDOM_SERIAL_NUMBER, serialNo);

            ProducerRecord<String, String> rec = new ProducerRecord<>(topicName, replacedString);
            producer.send(rec);
        }




        producer.close();
    }
}