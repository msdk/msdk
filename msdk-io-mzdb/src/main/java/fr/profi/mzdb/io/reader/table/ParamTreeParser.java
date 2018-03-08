package fr.profi.mzdb.io.reader.table;

import javax.xml.bind.JAXBContext;
import javax.xml.bind.Unmarshaller;
import javax.xml.transform.sax.SAXSource;

import fr.profi.mzdb.db.model.params.ComponentList;
import fr.profi.mzdb.db.model.params.ParamTree;
import fr.profi.mzdb.db.model.params.Precursor;
import fr.profi.mzdb.db.model.params.ScanList;
import fr.profi.mzdb.util.jaxb.XercesSAXParser;

// TODO: Auto-generated Javadoc
/**
 * The Class ParamTreeParser.
 * 
 * @author David Bouyssie
 */
public class ParamTreeParser {
	
	/** The xml mappers. */
	public static Unmarshaller paramTreeUnmarshaller = null;
	public static Unmarshaller componentListUnmarshaller = null;
	public static Unmarshaller scanListUnmarshaller = null;
	public static Unmarshaller precursorUnmarshaller = null;

	/**
	 * Parses the param tree.
	 * 
	 * @param paramTreeAsStr The param tree as a String
	 * @return the param tree
	 */
	synchronized public static ParamTree parseParamTree(String paramTreeAsStr) {
		
		ParamTree paramTree = null;
		
		try {
			if( paramTreeUnmarshaller == null ) {
				paramTreeUnmarshaller = JAXBContext.newInstance(ParamTree.class).createUnmarshaller();
			}
			
			SAXSource source = XercesSAXParser.getSAXSource( paramTreeAsStr );
			paramTree = (ParamTree) paramTreeUnmarshaller.unmarshal(source);
			
		} catch (Exception e) {
			e.printStackTrace();
		}
		
		return paramTree;
	}

	synchronized public static ScanList parseScanList(String scanListAsStr) {

		ScanList scanList = null;
		
		try {
			if( scanListUnmarshaller == null ) {
				scanListUnmarshaller = JAXBContext.newInstance(ScanList.class).createUnmarshaller();
			}
			
			SAXSource source = XercesSAXParser.getSAXSource( scanListAsStr );
			scanList = (ScanList) scanListUnmarshaller.unmarshal(source);
			
		}  catch (Exception e) {
			e.printStackTrace();
		}
		
		return scanList;
	}

	synchronized public static Precursor parsePrecursor(String precursorAsStr) {
		Precursor prec = null;
		
		try {
			if( precursorUnmarshaller == null ) {
				precursorUnmarshaller = JAXBContext.newInstance(Precursor.class).createUnmarshaller();
			}
			
			SAXSource source = XercesSAXParser.getSAXSource( precursorAsStr );
			prec = (Precursor) precursorUnmarshaller.unmarshal(source);
			
		}  catch (Exception e) {
			e.printStackTrace();
		}

		return prec;
	}
	
	synchronized public static ComponentList parseComponentList(String paramTreeAsStr) {

		ComponentList paramTree = null;
		
		try {
			if( componentListUnmarshaller == null ) {
				componentListUnmarshaller = JAXBContext.newInstance(ComponentList.class).createUnmarshaller();
			}
			
			SAXSource source = XercesSAXParser.getSAXSource( paramTreeAsStr );
			paramTree = (ComponentList) componentListUnmarshaller.unmarshal(source);
			
		}  catch (Exception e) {
			e.printStackTrace();
		}
		
		return paramTree;
	}

}
