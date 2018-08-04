package com.hp.sastreaming.fixtures.kafka;


import com.hp.curiosity.fixtures.general.JsonUtil;
import com.hp.ts.sa.analysis.messages.deprecated.TextfileContent;
import com.hp.ts.sa.analysis.messages.deprecated.TextfileContentDatapackage;

import java.io.File;
import java.io.IOException;
import java.util.Iterator;

/**
 * This is a wrapper class for Kafka Event messages returned by EventConsumers.<br>
 * 
 */
public class EventPackage {
    private final TextfileContentDatapackage pkg;

    /**
     * Constructor to wrap an actual Kafka Event message
     * 
     * @param pkg Kafka Event message Avro object
     */
    public EventPackage(TextfileContentDatapackage pkg) {
        this.pkg = pkg;
    }

    /**
     * Constructor for testing via pre-recorded JSON representations of Event messages.
     * 
     * @param jsonFile Pre-recored Event message JSON file
     */
    protected EventPackage(File jsonFile) throws Exception {
        pkg = JsonUtil.deserializeJson(TextfileContentDatapackage.class, jsonFile);
    }

    /**
     * Check if the specified Event attachment exists.<br>
     * 
     * @param filename Name of an Event attachment file
     * 
     * @return true if the Event attacment or false if a matching file doesn't exist in this package
     */
    public boolean attachmentExists(String filename) {
        for (TextfileContent file : pkg.getDatapackageFiles()) {
            if (file.getSourceFileName().equals(filename)) {
                return true;
            }
        }

        return false;
    }

    /**
     * Get the specified Event attachment.<br>
     * 
     * @param filename Name of an Event attachment file
     * 
     * @return An EventAttacment or null if a matching file doesn't exist in this package
     */
    public EventAttachment attachment(String filename) {
        EventAttachment attachment = null;

        for (TextfileContent file : pkg.getDatapackageFiles()) {
            if (file.getSourceFileName().equals(filename)) {
                attachment = new EventAttachment(file);
                break;
            }
        }

        return attachment;
    }

    /**
     * Note: This value may change so please don't use it in "checks" in fitnesse tests.<br>
     * 
     * @return Fitnesse friendly String representation of this Object
     */
    @Override
    public String toString() {
        StringBuffer sb = new StringBuffer();
        Iterator<TextfileContent> fileIterator = pkg.getDatapackageFiles().iterator();

        sb.append(getClass().getSimpleName()).append("[id=").append(pkg.getFileId()).append(", files=[");
        while (fileIterator.hasNext()) {
            sb.append(fileIterator.next().getSourceFileName());
            if (fileIterator.hasNext()) {
                sb.append(",");
            }
        }
        sb.append("]]");

        return sb.toString();
    }
}
