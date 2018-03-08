/**
 * This file is part of the mzDB project
 */
package fr.profi.mzdb.util.primitives;

/**
 * @author marco
 * 
 */
public class MutableInt implements Comparable<MutableInt> {
	public int value = 1;

	public void inc() {
		++value;
	}

	// @Override
	public int compareTo(MutableInt i) {
		return value - i.value;
	}
}
