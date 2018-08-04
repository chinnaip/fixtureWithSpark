package com.hp.sastreaming.fixtures.kafka;

import java.io.File;

import org.apache.commons.io.FileUtils;
import org.junit.Before;
import org.junit.Test;

import com.hp.curiosity.fixtures.general.Utils;
import com.hp.curiosity.fixtures.rest.DataUtility;

import static org.junit.Assert.*;

public class DataUtilityTest {
    private static final String TEST_FILE_NAME_DC = "event/json/event_analyzed-DC.json";

    private DataUtility dataUtility;

    @Before
    public void setUp() {
        dataUtility = new DataUtility();
    }

    @Test
    public void checkJsonpath_Event() throws Exception {

        File testFile = Utils.getTestFile(TEST_FILE_NAME_DC);
        String event = FileUtils.readFileToString(testFile);

        assertFalse(event.isEmpty());

        assertEquals("6ece01e0-d4c5-e511-8e59-b4b52f677526.zip",
                dataUtility.checkJsonpath(event, "$.thingformation_id"));

    }



}
