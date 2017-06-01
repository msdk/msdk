package io.github.msdk.io.mzml2;

public class MzMLBinaryDataInfo {

	private long position;
	private MzMLCV.MzMLCompressionType compressionType;
	private MzMLCV.MzMLBitLength bitLength;

	public MzMLBinaryDataInfo(long position, MzMLCV.MzMLCompressionType compressionType,
			MzMLCV.MzMLBitLength bitLength) {
		this.position = position;
		this.compressionType = compressionType;
		this.bitLength = bitLength;
	}

	public MzMLBinaryDataInfo() {

	}

	public MzMLCV.MzMLBitLength getBitLength() {
		return bitLength;
	}

	public void setBitLength(String bitLengthAccession) {
		for (MzMLCV.MzMLBitLength bitLength : MzMLCV.MzMLBitLength.values()) {
			if (bitLength.getValue().equals(bitLengthAccession))
				this.bitLength = bitLength;
		}
	}

	public boolean isBitLengthAccession(String bitLengthAccession) {
		for (MzMLCV.MzMLBitLength bitLength : MzMLCV.MzMLBitLength.values()) {
			if (bitLength.getValue().equals(bitLengthAccession))
				return true;
		}
		return false;
	}

	public MzMLCV.MzMLCompressionType getCompressionType() {
		return compressionType;
	}

	public void setCompressionType(String compressionTypeAccession) {
		for (MzMLCV.MzMLCompressionType compressionType : MzMLCV.MzMLCompressionType.values()) {
			if (compressionType.getValue().equals(compressionTypeAccession))
				this.compressionType = compressionType;
		}
	}

	public boolean isCompressionTypeAccession(String compressionTypeAccession) {
		for (MzMLCV.MzMLCompressionType compressionType : MzMLCV.MzMLCompressionType.values()) {
			if (compressionType.getValue().equals(compressionTypeAccession))
				return true;
		}
		return false;
	}

	public long getPosition() {
		return position;
	}

	public void setPosition(long position) {
		this.position = position;
	}
}
