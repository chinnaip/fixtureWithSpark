package com.hp.curiosity.fixtures.general;

import static org.junit.Assert.assertEquals;
import static org.junit.Assert.fail;

import org.junit.Test;

public class XmlCheckerTest {

    @Test
    public void testCheckXPath() throws Exception {
        XmlChecker checker = new XmlChecker("<parent> <child>Some text</child> </parent>");

        assertEquals("Some text", checker.checkXpath("//parent//child"));

        assertEquals("", checker.checkXpath("//NoSuchPath"));
        try {
            assertEquals("", checker.checkXpath("invalid xpath"));
            fail("Invalid xpath should result in an exception!");
        } catch (Exception e) {
            // Ignore as we expect this for invalid xpaths!
        }
    }

}
