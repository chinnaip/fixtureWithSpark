package com.hp.sastreaming.fixtures.kafka;

import com.hp.ts.sa.analysis.messages.deprecated.TextfileContent;

public class EventAttachment {
    private final TextfileContent attachment;

    public EventAttachment(TextfileContent attachment) {
        this.attachment = attachment;
    }
 
    public String content() {
        return attachment.getSourceFileContent();
    }

    /**
     * @return Fitnesse friendly String representation of this Object
     */
    @Override
    public String toString() {
        StringBuffer sb = new StringBuffer();

        sb.append(getClass().getSimpleName()).append("[name=").append(attachment.getSourceFileName()).append(", type=")
                .append(attachment.getSourceFileMimeType()).append("]");

        return sb.toString();
    }
}
