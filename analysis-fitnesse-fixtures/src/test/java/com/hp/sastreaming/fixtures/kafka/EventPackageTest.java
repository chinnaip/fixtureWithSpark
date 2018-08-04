package com.hp.sastreaming.fixtures.kafka;

import static org.junit.Assert.assertEquals;
import static org.junit.Assert.assertNotNull;
import static org.junit.Assert.assertNull;

import org.junit.Test;

import com.hp.curiosity.fixtures.general.Utils;

public class EventPackageTest {
    private static final String TEST_FILE_NAME_DC = "event/json/event_analyzed-DC.json";

    @Test
    public void testToString() throws Exception {

        EventPackage eventPkg = new EventPackage(Utils.getTestFile(TEST_FILE_NAME_DC));

        String expected = "EventPackage[id=6ece01e0-d4c5-e511-8e59-b4b52f677526.zip, files=[rst-datacenter.properties,index.xml,inlineEventOriginal.xml,inlineEvent.xml,eventAnalyzed.xml]]";

        assertEquals(expected, eventPkg.toString());
    }

    @Test
    public void testAttachment() throws Exception {

        EventPackage eventPkg = new EventPackage(Utils.getTestFile(TEST_FILE_NAME_DC));
        EventAttachment attachment = eventPkg.attachment("index.xml");

        assertNotNull(attachment);
        attachment = eventPkg.attachment("no such file");
        assertNull(attachment);
        attachment = eventPkg.attachment(null);
        assertNull(attachment);
    }

}
