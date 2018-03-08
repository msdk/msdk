package fr.profi.mzdb.io.writer.mgf;

/** */
public enum PrecursorMzComputationEnum {
	MAIN_PRECURSOR_MZ("main precursor mz"),
	SELECTED_ION_MZ("selected ion mz"),
	REFINED("mzdb-access refined precursor mz"),
	REFINED_THERMO("Thermo refined precursor mz");

	private final String paramName;

	PrecursorMzComputationEnum(String f) {
		this.paramName = f;
	}

	public String getUserParamName() {
		return this.paramName;
	}

}