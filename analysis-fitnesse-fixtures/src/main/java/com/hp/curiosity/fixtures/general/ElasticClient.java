package com.hp.curiosity.fixtures.general;

import com.hp.it.techasmts.elasticsearch.ElasticSearchClient;
import com.hp.it.techasmts.elasticsearch.QueryCriteria;
import com.hpe.ts.sa.client.DataResponse;
import org.elasticsearch.action.search.SearchResponse;
import org.elasticsearch.client.transport.TransportClient;
import org.elasticsearch.common.settings.Settings;
import org.elasticsearch.common.transport.InetSocketTransportAddress;
import org.elasticsearch.index.query.MatchQueryBuilder;
import org.elasticsearch.index.query.QueryBuilder;
import org.elasticsearch.index.query.QueryBuilders;

import java.net.InetAddress;
import java.net.UnknownHostException;
import java.sql.Date;
import java.time.LocalDateTime;
import java.time.ZoneOffset;
import java.util.HashMap;
import java.util.Map;
import java.util.Properties;
import java.util.Random;

public class ElasticClient {
    private ElasticSearchClient esClient;
    Map<String, String> propertiesMap;

    public ElasticClient(Map<String, String> mapProperties) {
        propertiesMap = mapProperties;
        Properties properties = new Properties();
        properties.putAll(mapProperties);

        esClient = new ElasticSearchClient(properties);
    }

    public SearchResponse searchForPredictions(String indexName, String serialNo) {
        QueryCriteria query = new QueryCriteria();
        query.setIndexName(indexName);

        query.setQueryBuilder(QueryBuilders.matchQuery("serial_number", serialNo));

        return esClient.searchItems(query);
    }

    public DataResponse writeToEs(String indexName, String indexType, String indexId, String json) {
        return esClient.addItemToIndex(indexName, indexType, indexId, json);
    }


    public DataResponse writeToEs(String indexName, String indexType, Map<String, String> items) {

        return esClient.addListItemsToIndex(indexName, indexType, items);
    }
}