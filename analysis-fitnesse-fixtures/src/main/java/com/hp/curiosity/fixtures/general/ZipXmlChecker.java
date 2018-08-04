package com.hp.curiosity.fixtures.general;

import java.io.IOException;
import java.util.zip.ZipFile;

import com.hp.curiosity.util.ZipUtil;

/**
 * This is a convenience class for validating XML files in ZIP files.<br>
 */
public class ZipXmlChecker extends XmlChecker {

    /**
     * Construct checker for a specific XML file entry in a ZIP file.<br>
     * 
     * @param entryName Name of XML file entry to check
     * @param zipFilepath Absolute ZIP file path
     */
    public ZipXmlChecker(String entryName, String zipFilepath) throws IOException {
        super(ZipUtil.readZipFileEntryContents(new ZipFile(zipFilepath), entryName));
    }

}
