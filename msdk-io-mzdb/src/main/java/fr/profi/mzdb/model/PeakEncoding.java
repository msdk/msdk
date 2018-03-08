package fr.profi.mzdb.model;

public enum PeakEncoding {
	NO_LOSS_PEAK(16), HIGH_RES_PEAK(12), LOW_RES_PEAK(8);

	private final int value;

	private PeakEncoding(int value) {
		this.value = value;
	}

	public int getValue() {
		return value;
	}
}
