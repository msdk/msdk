package fr.profi.mzdb.util.jaxb;

import java.io.StringReader;

import javax.xml.parsers.ParserConfigurationException;
import javax.xml.parsers.SAXParser;
import javax.xml.parsers.SAXParserFactory;
import javax.xml.transform.sax.SAXSource;

import org.xml.sax.InputSource;
import org.xml.sax.SAXException;
import org.xml.sax.SAXNotRecognizedException;
import org.xml.sax.XMLReader;

//Source: https://jaxb.java.net/nonav/2.2.4/docs/api/javax/xml/bind/Unmarshaller.html
public class XercesSAXParser {

	private static final String JAXP_SCHEMA_LANGUAGE = "http://java.sun.com/xml/jaxp/properties/schemaLanguage";
	//private static final String JAXP_SCHEMA_LOCATION = "http://java.sun.com/xml/jaxp/properties/schemaSource";
	private static final String W3C_XML_SCHEMA = "http://www.w3.org/2001/XMLSchema";
	
	private static SAXParser saxParser;
	private static XMLReader xmlReader;
	
	private static SAXParser newSaxParser() throws ParserConfigurationException, SAXException {
		System.setProperty("javax.xml.parsers.SAXParserFactory", "org.apache.xerces.jaxp.SAXParserFactoryImpl");

		SAXParserFactory spf = SAXParserFactory.newInstance();
		spf.setNamespaceAware(true);
		spf.setValidating(true);
		
		SAXParser newSaxParser = spf.newSAXParser();
		
		try {
			newSaxParser.setProperty(JAXP_SCHEMA_LANGUAGE, W3C_XML_SCHEMA);
			//newSaxParser.setProperty(JAXP_SCHEMA_LOCATION, "http://....");
		} catch (SAXNotRecognizedException x) {
			// exception handling omitted
		}
		
		return newSaxParser;
	};

	public static XMLReader getXMLReaderInstance() throws ParserConfigurationException, SAXException {
		if( saxParser == null ) saxParser = newSaxParser();
		if( xmlReader == null ) xmlReader = saxParser.getXMLReader();

		return xmlReader;
	}
	
	public static SAXSource getSAXSource( String xmlString ) throws ParserConfigurationException, SAXException {
		return new SAXSource(xmlReader, new InputSource(new StringReader(xmlString)) );
	}

}
