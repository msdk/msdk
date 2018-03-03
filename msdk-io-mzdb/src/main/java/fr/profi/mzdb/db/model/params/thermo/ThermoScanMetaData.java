package fr.profi.mzdb.db.model.params.thermo;

import java.util.regex.Matcher;
import java.util.regex.Pattern;

public class ThermoScanMetaData {

	/** The spectrumType. */
	protected final String acquisitionType;

	/** The analyzerType. */
	protected final String analyzerType;

	/** The msLevel. */
	protected int msLevel;

	/** The mzRange. */
	protected final float[] mzRange;

	/** Thetargets. */
	protected final ThermoFragmentationTarget[] targets;

	public ThermoScanMetaData(String spectrumType, String analyzerType, int msLevel, float[] mzRange, ThermoFragmentationTarget[] targets) {
		super();
		this.acquisitionType = spectrumType;
		this.analyzerType = analyzerType;
		this.msLevel = msLevel;
		this.mzRange = mzRange;
		this.targets = targets;
	}
	
	// MS2 example: ITMS + c NSI d Full ms2 476.20@cid30.00 [120.00-1440.00]
	// MS3 example: FTMS + p NSI sps d Full ms3 707.8472@cid35.00 463.3669@hcd45.00 [115.0000-140.0000]
	String targetPattern = "\\s(\\d+\\.\\d+)@([a-z]+)(\\d+\\.\\d+)";
	String ms2Pattern = "\\d" + targetPattern +"\\s\\[(\\d+\\.\\d+)-(\\d+\\.\\d+)\\]";
	String ms3Pattern = "\\d" + targetPattern + targetPattern +"\\s\\[(\\d+\\.\\d+)-(\\d+\\.\\d+)\\]";
		
	public ThermoScanMetaData(String filterString) {
		
		String[] stringParts = filterString.split("Full ms");
		String rightString = stringParts[1];
		char msLevelChar = rightString.charAt(0);
		int msLevel = Character.getNumericValue(msLevelChar);
		
		String spectrumType = stringParts[0] + "Full ms" + msLevelChar;
		
		String pattern = msLevel == 3 ? ms3Pattern : ms2Pattern;
		
		// Compile the regex
		Pattern r = Pattern.compile(pattern);
		Matcher m = r.matcher(rightString);
		
		ThermoFragmentationTarget[] targets = new ThermoFragmentationTarget[msLevel - 1];
		float[] mzRange = {0f,0f};
		
		if (m.find()) {

			// Parse MS2 target information
			targets[0] = new ThermoFragmentationTarget(
				1, Double.parseDouble(m.group(1)), m.group(2), Float.parseFloat(m.group(3))
			);
			
			
			if (msLevel == 2) {
				mzRange[0] = Float.parseFloat(m.group(4));
				mzRange[1] = Float.parseFloat(m.group(5));
			}
			else {
				
				// Parse MS3 target information
				targets[1] = new ThermoFragmentationTarget(
					2, Double.parseDouble(m.group(4)), m.group(5), Float.parseFloat(m.group(6))
				);
				
				mzRange[0] = Float.parseFloat(m.group(7));
				mzRange[1] = Float.parseFloat(m.group(8));
			}
		}
		
		this.acquisitionType = spectrumType;
		this.analyzerType = stringParts[0].split("\\s")[0];
		this.msLevel = msLevel;
		this.mzRange = mzRange;
		this.targets = targets;
	}
	
	/*"FTMS + p NSI sps d Full ms3 707.8472@cid35.00 463.3669@hcd45.00 [115.0000-140.0000]"
	ITMS + c NSI d Full ms2 476.20@cid30.00 [120.00-1440.00]
		
		spectrum_type: "FTMS + p NSI sps d Full ms3",
	    analyzer_type: "FTMS",
		ms_level : 3,
		mz_range: [115.0000,140.0000],
		targets: [
		  {ms_level: 1, mz: 707.8472, activation_type: 'CID', collision_energy: 35},
		  {ms_level: 2, mz: 463.3669, activation_type: 'HCD', collision_energy: 45}
		  ]*/

	public String getAcquisitionType() {
		return acquisitionType;
	}

	public String getAnalyzerType() {
		return analyzerType;
	}

	public int getMsLevel() {
		return msLevel;
	}

	public float[] getMzRange() {
		return mzRange;
	}

	public ThermoFragmentationTarget[] getTargets() {
		return targets;
	}

}
