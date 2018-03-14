package fr.profi.mzdb;

/**
 * @author bouyssie
 *
 */
public enum XicMethod {
	MAX(0), NEAREST(1), SUM(2);

	private final Integer val;

	private XicMethod(Integer val_) {
		this.val = val_;
	}

	@Override
	public String toString() {
		return this.val.toString();
	}
};
