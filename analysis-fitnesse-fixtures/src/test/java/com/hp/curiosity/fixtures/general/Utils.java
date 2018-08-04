package com.hp.curiosity.fixtures.general;

import java.io.File;
import java.net.URISyntaxException;

public class Utils {

    public static File getTestFile(String filepath) throws URISyntaxException {
        File testFile = null;
        String baseDir = (new File(Thread.currentThread().getContextClassLoader().getResource(".").toURI()))
                .getAbsolutePath();

        testFile = new File(baseDir, filepath);

        return testFile;
    }

}
