package com.hp.curiosity.fixtures.general;

import com.google.common.base.Optional;
import com.hp.propertiesservice.ZkCentralPropertiesService;

import java.util.Map;

import static com.google.common.collect.Maps.newHashMap;

import org.apache.kafka.clients.producer.ProducerConfig;
import org.apache.kafka.common.serialization.StringSerializer;

public class ZooKeeperPropertiesUtility {

    private static final String ES_CLUSTER_NAME = "elasticsearch.cluster.name";
    private static final String ES_HOSTNAME = "elasticsearch.hostname";
    private static final String ES_ENABLE_SSL = "elasticsearch.enable.ssl";
    private static final String CLIENT_TRANSPORT_SNIFF = "elasticsearch.client.transport.sniff";
    private static final String ES_PORT = "elasticsearch.port";
    private static final String TRUST_STORE_LOCATION = "ssl.truststore.location";
    private static final String TRUST_STORE_PASSWD = "ssl.truststore.password";
    private static final String SECURITY_PROTOCOL = "security.protocol";


    private ZkCentralPropertiesService zk;



    public ZooKeeperPropertiesUtility(String ps_environment, String application_namespace, String ps_zk_quorum) {
        zk = new ZkCentralPropertiesService(ps_environment, application_namespace, ps_zk_quorum);
    }

    public Map<String, String> getElasticSearchProperties() {
        Map<String, String> properties = newHashMap();
        properties.put("es.mapping.id", "prediction_id");
        properties.put(ES_PORT, getZookeeperValue(ES_PORT));
        properties.put(ES_CLUSTER_NAME, getZookeeperValue("elasticsearch.cluster"));
        properties.put(CLIENT_TRANSPORT_SNIFF, getZookeeperValue("elasticsearch.sniff"));
        properties.put(ES_HOSTNAME, getZookeeperValue("elasticsearch.hosts"));
        properties.put(ES_ENABLE_SSL, getZookeeperValue("elasticsearch.enableSsl"));
        properties.put("es.net.http.auth.pass", getZookeeperValue("es.net.http.auth.pass"));
        properties.put("es.net.http.auth.user", getZookeeperValue("es.net.http.auth.user"));
        properties.put("es.net.ssl", getZookeeperValue("es.net.ssl"));
        properties.put("es.nodes.discovery", getZookeeperValue("es.nodes.discovery"));
        properties.put("es.nodes.wan.only", getZookeeperValue("es.nodes.wan.only"));
        properties.put("es.nodes", getZookeeperValue("es.nodes"));
        properties.put("es.index.auto.create", getZookeeperValue("es.index.auto.create"));
        properties.put("es.mapping.date.rich", getZookeeperValue("es.mapping.date.rich"));
        properties.put("pushdown", getZookeeperValue("pushdown"));
        properties.put("es.input.json", "yes");

        return properties;
    }

    public Map<String, Object> getKafkaProperties(String kafkaTruststorePath, String kafkaTruststorePasswd) {
        Map<String, Object> properties = newHashMap();
        properties.put(ProducerConfig.BOOTSTRAP_SERVERS_CONFIG, getZookeeperValue("metadata.broker.list"));
        properties.put(ProducerConfig.KEY_SERIALIZER_CLASS_CONFIG, StringSerializer.class.getName());
        properties.put(ProducerConfig.VALUE_SERIALIZER_CLASS_CONFIG, StringSerializer.class.getName());
        properties.put(SECURITY_PROTOCOL, getZookeeperValue("kafka.security.protocol"));
        properties.put(TRUST_STORE_LOCATION, kafkaTruststorePath);
        properties.put(TRUST_STORE_PASSWD, kafkaTruststorePasswd);

        return properties;
    }

    private String getZookeeperValue(String propertyName) {
        Optional<String> property = zk.getProperty(propertyName);
        return property.isPresent() ? property.get() : "";
    }

}