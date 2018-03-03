package fr.profi.mzdb.io.writer.mgf;

/** Class representing a MGF header */
public class MgfHeader {
	MgfHeaderEntry[] entries;

	public MgfHeader(MgfHeaderEntry[] entries) {
		super();
		this.entries = entries;
	}

	/**
	 * 
	 * @param title
	 * @param pepMass
	 * @param charge
	 * @return a new MgfHeader
	 */
	public MgfHeader(String title, double precMz, int charge) {
		this(new MgfHeaderEntry[] {
				new MgfHeaderEntry(MgfField.TITLE, title),
				new MgfHeaderEntry(MgfField.PEPMASS, String.format("%.4f", precMz)),
				// TODO: use the trailer corresponding to the acquisition polarity (see mzDB meta-data)
				new MgfHeaderEntry(MgfField.CHARGE, charge, "+")
			}
		);
	}
	
	/**
	 * 
	 * @param title
	 * @param pepMass
	 * @param rt
	 * @return a new MgfHeader
	 */
	public MgfHeader(String title, double precMz, float rt) {

		this(
			new MgfHeaderEntry[] {
				new MgfHeaderEntry(MgfField.TITLE, title),
				new MgfHeaderEntry(MgfField.PEPMASS, String.format("%.4f", precMz)),
				new MgfHeaderEntry(MgfField.RTINSECONDS, String.format("%.2f", rt))
			}
		);
	}

	/**
	 * 
	 * @param title
	 * @param pepMass
	 * @param charge
	 * @param rt
	 * @return a new MgfHeader
	 */
	public MgfHeader(String title, double precMz, int charge, float rt) {

		this(
			new MgfHeaderEntry[] {
				new MgfHeaderEntry(MgfField.TITLE, title),
				new MgfHeaderEntry(MgfField.PEPMASS, String.format("%.4f", precMz)),
				// TODO: use the trailer corresponding to the acquisition polarity (see mzDB meta-data)
				new MgfHeaderEntry(MgfField.CHARGE, charge, "+"),
				new MgfHeaderEntry(MgfField.RTINSECONDS, String.format("%.2f", rt))
			}
		);
	}

	public StringBuilder appendToStringBuilder(StringBuilder sb) {

		sb.append(MgfField.BEGIN_IONS).append(MgfWriter.LINE_SPERATOR);

		for (MgfHeaderEntry entry : entries) {
			entry.appendToStringBuilder(sb).append(MgfWriter.LINE_SPERATOR);
		}

		return sb;
	}

	@Override
	public String toString() {
		StringBuilder sb = new StringBuilder();
		return this.appendToStringBuilder(sb).toString();
	}

}