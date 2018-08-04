package com.hp.sastreaming.fixtures.kafka;

import static org.junit.Assert.assertEquals;
import static org.junit.Assert.assertNotNull;
import static org.junit.Assert.assertTrue;

import org.junit.Test;

import com.hp.curiosity.fixtures.general.Utils;

public class EventAttachmentTest {
    private static final String TEST_FILE_NAME_DC = "event/json/event_analyzed-DC.json";

    @Test
    public void testToString() throws Exception {

        EventPackage eventPkg = new EventPackage(Utils.getTestFile(TEST_FILE_NAME_DC));
        EventAttachment attachment = eventPkg.attachment("index.xml");

        String expected = EventAttachment.class.getSimpleName() + "[name=index.xml, type=text]";

        assertEquals(expected, attachment.toString());
    }

    @Test
    public void testContent() throws Exception {

        EventPackage eventPkg = new EventPackage(Utils.getTestFile(TEST_FILE_NAME_DC));
        EventAttachment attachment = eventPkg.attachment("index.xml");
        String content = attachment.content();

        assertNotNull(content);
        assertTrue(content.startsWith(""));
    }

}
