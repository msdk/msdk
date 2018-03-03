package fr.profi.mzdb.db.model.params;

import java.util.List;

import javax.xml.bind.annotation.XmlElementWrapper;

import fr.profi.mzdb.db.model.params.param.CVEntry;
import fr.profi.mzdb.db.model.params.param.CVParam;
import fr.profi.mzdb.db.model.params.thermo.ThermoScanMetaData;

public class ScanParamTree extends AbstractParamTree {	

	@XmlElementWrapper
	protected List<ScanWindowList> scanWindowList;

	public List<ScanWindowList> getScanWindowList() {
		return scanWindowList;
	}

	public ScanParamTree() {
	}
	
	public ThermoScanMetaData getThermoMetaData() {
		CVParam filterStringCvParam = this.getCVParam(CVEntry.FILTER_STRING);
		if (filterStringCvParam == null) return null;
		
		return new ThermoScanMetaData(filterStringCvParam.getValue());
	}
	
}
