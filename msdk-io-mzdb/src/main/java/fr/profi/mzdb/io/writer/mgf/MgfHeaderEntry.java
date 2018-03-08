package fr.profi.mzdb.io.writer.mgf;

/** Class representing a row in the MGF header */
public class MgfHeaderEntry {

	final MgfField field;
	final Object value;
	final String trailer;

	public MgfHeaderEntry(MgfField field, Object value, String trailer) {
		super();
		this.field = field;
		this.value = value;
		this.trailer = trailer;
	}

	public MgfHeaderEntry(MgfField field, Object value) {
		super();
		this.field = field;
		this.value = value;
		this.trailer = null;
	}

	public StringBuilder appendToStringBuilder(StringBuilder sb) {
		sb.append(field).append("=").append(value);

		if (this.trailer != null) {
			sb.append(trailer);
		}

		return sb;
	}

	@Override
	public String toString() {
		StringBuilder sb = new StringBuilder();
		return this.appendToStringBuilder(sb).toString();
	}
}