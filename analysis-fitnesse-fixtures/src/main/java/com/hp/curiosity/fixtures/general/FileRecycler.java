package com.hp.curiosity.fixtures.general;
import com.hp.it.sadb.framework.systemtest.telemetry.Option;
import com.hp.it.sadb.framework.systemtest.telemetry.Recycler;

import java.io.File;
import java.nio.file.Files;
import java.nio.file.Paths;
import java.util.List;
import java.util.Map;
import java.util.UUID;
import java.util.stream.Collectors;

import static com.google.common.collect.Maps.newHashMap;

public class FileRecycler {

    private static final Recycler RECYCLER = new Recycler();


    static final String SERIAL_NO_XPATH = "//Section[@name='SYSTEM_IDENTIFIERS']/Property[@name='ProductSerialNumber']/@value";

    File recycleFile(String filePath, String xPath, String newValue) {
        return RECYCLER
                .recycle(getTelemetryFile(filePath),
                        new Option.XPathReplace(xPath, newValue));
    }

    public File recycleSerialNo(String filePath, String newValue) {
        return recycleFile(filePath, SERIAL_NO_XPATH,
                newValue);

    }

    public String getPath(File myFile) {
        return myFile.getPath();
    }

    private static final File getTelemetryFile(String fileResource) {
        File telemetryFile = new File(fileResource);

        if (!telemetryFile.isFile()) {
            throw new IllegalArgumentException("Invalid telemetry file location: " + telemetryFile);
        }

        return telemetryFile;
    }

    public String randomUUIDString() {
        return UUID.randomUUID().toString();
    }

    public Map<String, String> readFileAndSubstituteSerialAndPredictionIdValues(String filePath, String serialNo) throws Exception {

        File myFile = getTelemetryFile(filePath);

        List<String> predictions = Files.lines(Paths.get(myFile.toURI()))
                .map(r -> r.replace("test_Again_Serial", serialNo))
                .collect(Collectors.toList());

        Map<String, String> predictionsAndKey = newHashMap();
        for (String prediction:predictions) {
            String predictionId = UUID.randomUUID().toString();
            predictionsAndKey.put(predictionId, prediction.replace("prediction_id_value", predictionId));
        }
        return predictionsAndKey;

    }

    public String replace(String myString, String serialNo, String timeStamp, String predictionId) {
        return myString
                .replace("EMAIL_SENT_VALUE", timeStamp)
                .replace("LATEST_SYMPTOM_TS_VALUE", timeStamp)
                .replace("PREDICTION_ID_VALUE", predictionId)
                .replace("SERIAL_NUMBER_VALUE", serialNo);
    }
}