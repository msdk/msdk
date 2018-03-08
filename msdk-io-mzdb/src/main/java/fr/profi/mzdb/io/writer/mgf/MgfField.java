package fr.profi.mzdb.io.writer.mgf;

/** */
public enum MgfField {
	BEGIN_IONS("BEGIN IONS"), END_IONS("END IONS"), TITLE("TITLE"), PEPMASS("PEPMASS"), CHARGE("CHARGE"), RTINSECONDS("RTINSECONDS");

	//
	// NEWLINE("\n"), EQUAL("="), PLUS("+");

	private final String fieldString;

	MgfField(String f) {
		this.fieldString = f;
	}

	public String toString() {
		return this.fieldString;
	}
}