package fr.profi.mzdb.db.model.params.param;

public enum CVEntry {

	// TODO: import full enumeration from PWIZ
	//PSI_MS_32_BIT_FLOAT("1000521"), // FIXME: it should be MS:1000521
	//PSI_MS_64_BIT_FLOAT("1000523"), // FIXME: it should be MS:1000523	
	ACQUISITION_PARAMETER("MS:1001954"), // FIXME: it should be MS:1001954
	FILTER_STRING("MS:1000512"),
	ISOLATION_WINDOW_TARGET_MZ("MS:1000827"),
	ISOLATION_WINDOW_LOWER_OFFSET("MS:1000828"),
	ISOLATION_WINDOW_UPPER_OFFSET("MS:1000829"),
	SELECTED_ION_MZ("MS:1000744");

	/** The value. */
	private final String m_accession;

	/**
	 * Instantiates a new controlled vocabulary entry.
	 * 
	 * @param accession
	 *            the accession
	 */
	private CVEntry(String accession) {
		m_accession = accession;
	}

	/**
	 * Gets the accession.
	 * 
	 * @return the accession
	 */
	public String getAccession() {
		return m_accession;
	}

}
