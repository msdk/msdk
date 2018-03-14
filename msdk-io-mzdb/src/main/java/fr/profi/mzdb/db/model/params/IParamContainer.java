package fr.profi.mzdb.db.model.params;

import java.util.List;

import javax.xml.bind.annotation.XmlElement;

import fr.profi.mzdb.db.model.params.param.CVParam;
import fr.profi.mzdb.db.model.params.param.UserParam;
import fr.profi.mzdb.db.model.params.param.UserText;

// TODO: Auto-generated Javadoc
/**
 * The Interface IParamContainer.
 * 
 * @author David Bouyssie
 */
public interface IParamContainer {

	/**
	 * Gets the cV params.
	 * 
	 * @return the cV params
	 */
	@XmlElement(name = "cvParam", type = CVParam.class, required = false)
	public List<CVParam> getCVParams();

	/**
	 * Gets the user params.
	 * 
	 * @return the user params
	 */
	@XmlElement(name = "userParam", type = UserParam.class, required = false)
	public List<UserParam> getUserParams();
	
	/**
	 * Gets the user texts.
	 * 
	 * @return the user texts
	 */
	@XmlElement(name = "userText", type = UserParam.class, required = false)
	public List<UserText> getUserTexts();
	
	/**
	 * Gets the user param.
	 * 
	 * @param name
	 *            the name
	 * @return the user param
	 */
	public UserParam getUserParam(String name);

}
