package com.hp.curiosity.fixtures.general;

import org.apache.http.HttpResponse;
import org.apache.http.client.HttpClient;
import org.apache.http.client.methods.HttpGet;
import org.apache.http.impl.client.HttpClientBuilder;

import java.io.BufferedReader;
import java.io.InputStreamReader;

public class PaHttpClient {

    private String cookies;
    private HttpClient client = HttpClientBuilder.create().useSystemProperties().build();
    private final String USER_AGENT = "Mozilla/5.0";

    public String ackPredictionNotification(String Host, String urlTemplate, String parameters) throws Exception {

        String url = urlTemplate.replace("HOST", Host).replace("UUID", parameters);

        HttpGet request = new HttpGet(url);

        request.setHeader("User-Agent", USER_AGENT);
        request.setHeader("Accept", "application/json;q=0.9,*/*;q=0.8");
        request.setHeader("Accept-Language", "en-US,en;q=0.5");

        HttpResponse response = client.execute(request);
        int responseCode = response.getStatusLine().getStatusCode();

        BufferedReader rd = new BufferedReader(new InputStreamReader(response.getEntity().getContent()));

        StringBuffer result = new StringBuffer();
        String line = "";
        while ((line = rd.readLine()) != null) {
            result.append(line);
        }

        return result.toString();

    }

}