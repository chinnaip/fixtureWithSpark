package com.hp.curiosity.fixtures.general;

import static org.junit.Assert.assertEquals;

import java.io.File;

import org.junit.Test;

public class ZipXmlCheckerTest {
    private static final String TEST_DATA_PACKAGE = "event/zip/6ece01e0-d4c5-e511-8e59-b4b52f677526.zip";

    @Test
    public void test() throws Exception {
        File testFile = Utils.getTestFile(TEST_DATA_PACKAGE);
        ZipXmlChecker checker = new ZipXmlChecker("index.xml", testFile.getAbsolutePath());

        assertEquals("iLO4_1_TestEvent", checker.checkXpath("//Caption"));
        assertEquals("JPT2398E4K:661189-B21:iLO4_1_TestEvent::", checker.checkXpath("//Description"));
    }

}
