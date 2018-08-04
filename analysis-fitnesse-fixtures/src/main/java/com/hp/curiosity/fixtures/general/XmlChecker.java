package com.hp.curiosity.fixtures.general;

import java.io.ByteArrayInputStream;
import java.io.File;

import javax.xml.parsers.DocumentBuilder;
import javax.xml.parsers.DocumentBuilderFactory;
import javax.xml.xpath.XPath;
import javax.xml.xpath.XPathConstants;
import javax.xml.xpath.XPathExpression;
import javax.xml.xpath.XPathFactory;

import org.w3c.dom.Document;

/**
 * This is a convenience class for validating XML data.<br>
 * <br>
 * Example fixture usage:
 * 
 * <pre>
 * !define XML {<parent><child>Some text</child></parent>}
 * |script|Xml Checker|${XML}                   |
 * |check |check Xpath|//parent//child|Some text|
 * </pre>
 */
public class XmlChecker {
	
    private File xmlFile;
    private String xmlString;

    private Document doc;

    /**
     * Constructor for checking an XML string.<br>
     * 
     * @param xmlString An actual XML string
     */
    public XmlChecker(String xmlString) {
        this.xmlString = xmlString;
    }

    public XmlChecker(File xmlFile) {
        this.xmlFile = xmlFile;
    }

    /**
     * Check for an XPath value in the XML
     * 
     * @param xpath A valid XPath expression
     * @return Value of the verified XPath expression or a blank String if it doesn't exist
     * 
     */
    public String checkXpath(String xpath) throws Exception {
        return getNodeValue(getDocument(), xpath);
	}
	
    private String getNodeValue(Document doc, String path) throws Exception {
		XPath xp = XPathFactory.newInstance().newXPath();
        XPathExpression xpr = xp.compile(path);
			
        return (String) xpr.evaluate(doc, XPathConstants.STRING);
	}

    private Document getDocument() throws Exception {
        if (xmlString != null) {
            return getDocument(xmlString);
        }
        if (xmlFile != null) {
            return getDocument(xmlFile);
        }

        throw new IllegalStateException("No XML file or string defined!");
    }

    private Document getDocument(File xmlFile) {
        throw new IllegalArgumentException("Not implemented yet!");
    }

    private Document getDocument(String xmlString) throws Exception {

        if (doc == null) {
            DocumentBuilderFactory df = DocumentBuilderFactory.newInstance();
            DocumentBuilder builder;

            df.setNamespaceAware(true);
            df.setIgnoringElementContentWhitespace(true);
            df.setValidating(true);
            df.setIgnoringComments(true);
            builder = df.newDocumentBuilder();
            doc = builder.parse(new ByteArrayInputStream(xmlString.getBytes()));
        }

        return doc;
	}
	
}
