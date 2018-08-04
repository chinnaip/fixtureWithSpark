package com.hp.curiosity.fixtures.general;

import com.google.common.base.Optional;
import com.hp.it.sadb.framework.systemtest.telemetry.TelemetryFileReader;
import org.junit.Assert;
import org.junit.Before;
import org.junit.Test;

import java.io.File;
import java.net.URL;
import java.util.Map;

import static org.hamcrest.core.Is.is;
import static org.junit.Assert.assertThat;


public class FileRecyclerTest {

    private FileRecycler recycler;
    private TelemetryFileReader telemetryFileReader;
    private static final String SERIAL_NO = "54909e60-b4b4-4c6d-afef-8e088c8bee6e";
    private static final String ZIP_FILE_TO_RECYCLE = "3par_cpg.zip";

    @Before
    public void setup() {
        recycler = new FileRecycler();
        telemetryFileReader = new TelemetryFileReader();
    }

    @Test
    public void testSerialNoRecycle() throws Exception {
        URL resourceUrl = Thread.currentThread().getContextClassLoader().getResource(ZIP_FILE_TO_RECYCLE);

        File recycledFile = recycler.recycleSerialNo(resourceUrl.getPath(), SERIAL_NO);

        Optional<String> newSerialNo = telemetryFileReader.readValue(recycledFile,
                FileRecycler.SERIAL_NO_XPATH);

        assertThat(newSerialNo.get(), is(SERIAL_NO));

    }


    @Test
    public void testReadFileAndSubstituteSerialAndPredictionIdValues() throws Exception {
        URL resourceUrl = Thread.currentThread().getContextClassLoader().getResource("collections/threepar/outage_prediction_1202078_for_e2e");
        Map<String, String> predictions = recycler.readFileAndSubstituteSerialAndPredictionIdValues(
                resourceUrl.getPath(), SERIAL_NO);
        assertThat(predictions.size(), is(11));
    }

    @Test
    public void testReplacementInJSON() {
        String json = "{\"model\": \"VCL400\",\"quiet_period\": \"true\",\"email_sent\":\"EMAIL_SENT_VALUE\",\"latest_symptom_timestamp\": \"LATEST_SYMPTOM_TS_VALUE\",\"product_number\": \"testing_Again_Product\",\"customer\": \"Nova\",\"min_time_to_outage_mins\": \"-1\",\"confidence\": \"0\",\"max_time_to_outage_mins\": \"-1\",\"prediction_timestamp\": \"1\",\"prediction_id\": \"PREDICTION_ID_VALUE\",\"serial_number\": \"SERIAL_NUMBER_VALUE\",\"avg_time_to_outage_mins\": \"0\",\"predicted_outage_msg_description\": \"CPG Growth Failure\",\"last_six_events\": \"[15.9522433648408884088,15.95224336484088,15.95224336484088]\",\"latest_symptom_description\": \"VCL4101\",\"mock\": \"false\",\"acknowledged\": \"false\"}";
        String serialNo = "9a5a6b1e-1d7d-4080-8c16-47bd22554669";
        String time = "2017-09-13T08:05:47.380";
        String predictionId = "1b13c67a-1a43-489e-827f-a44468a0ac86";

        String fixedJson = recycler.replace(json, serialNo, time, predictionId);

        Assert.assertNotNull(fixedJson);
        Assert.assertFalse(fixedJson.isEmpty());
        Assert.assertTrue(fixedJson.endsWith("}"));
        Assert.assertTrue(fixedJson.contains(serialNo));
        Assert.assertTrue(fixedJson.contains(time));
        Assert.assertTrue(fixedJson.contains(predictionId));

        System.out.println(fixedJson);
    }
}