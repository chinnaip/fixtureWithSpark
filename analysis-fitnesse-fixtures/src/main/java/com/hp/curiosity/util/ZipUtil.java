package com.hp.curiosity.util;

import java.io.IOException;
import java.util.Enumeration;
import java.util.zip.ZipEntry;
import java.util.zip.ZipFile;

import org.apache.commons.io.IOUtils;

public class ZipUtil {

    public static String readZipFileEntryContents(ZipFile zipFile, String entryName) throws IOException {
        try {
            Enumeration<? extends ZipEntry> entries = zipFile.entries();

            while (entries.hasMoreElements()) {
                ZipEntry entry = entries.nextElement();

                if (entry.getName().equals(entryName)) {
                    return IOUtils.toString(zipFile.getInputStream(entry), "UTF-8");
                }
            }
        } finally {
            if (zipFile != null) {
                zipFile.close();
            }
        }

        return null;
    }

}
