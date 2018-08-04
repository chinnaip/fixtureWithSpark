package com.hp.curiosity.fixtures.general;

import com.fasterxml.jackson.databind.ObjectMapper;
import com.jayway.jsonpath.JsonPath;
import com.jayway.jsonpath.Predicate;
import net.minidev.json.JSONArray;

import java.io.File;
public class JsonUtil {

    public boolean checkJsonPathExists(String json, String jsonPath) throws Exception {

        if (jsonPath == null) {
            throw new Exception("jsonPath must not be null");
        } else if (json == null) {
            throw new Exception("json document mut not be null");
        } else {
                JSONArray answer = JsonPath.read(json, jsonPath, new Predicate[0]);
                return answer.size() > 0;

        }
    }

    public static <T> T deserializeJson(Class<T> objClass, File file) throws Exception {
        ObjectMapper mapper = new ObjectMapper();
        T obj = mapper.readValue(file, objClass);

        return obj;
    }

}
