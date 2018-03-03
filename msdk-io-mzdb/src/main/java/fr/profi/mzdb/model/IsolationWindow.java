package fr.profi.mzdb.model;

/**
 * 
 * 
 */
public class IsolationWindow {
    
    private final double minMz;
    private final double maxMz;
    
    public IsolationWindow(double minMz, double maxMz) {
	this.minMz = minMz;
	this.maxMz = maxMz;
    }
    
    public double getMinMz() {
	return this.minMz;
    }
    
    public double getMaxMz() {
	return this.maxMz;
    }

	/* (non-Javadoc)
	 * @see java.lang.Object#hashCode()
	 */
	@Override
	public int hashCode() {
		final int prime = 31;
		int result = 1;
		long temp;
		temp = Double.doubleToLongBits(maxMz);
		result = prime * result + (int) (temp ^ (temp >>> 32));
		temp = Double.doubleToLongBits(minMz);
		result = prime * result + (int) (temp ^ (temp >>> 32));
		return result;
	}

	/* (non-Javadoc)
	 * @see java.lang.Object#equals(java.lang.Object)
	 */
	@Override
	public boolean equals(Object obj) {
		if (this == obj)
			return true;
		if (obj == null)
			return false;
		if (getClass() != obj.getClass())
			return false;
		IsolationWindow other = (IsolationWindow) obj;
		if (Double.doubleToLongBits(maxMz) != Double.doubleToLongBits(other.maxMz))
			return false;
		if (Double.doubleToLongBits(minMz) != Double.doubleToLongBits(other.minMz))
			return false;
		return true;
	}

	/* (non-Javadoc)
	 * @see java.lang.Object#toString()
	 */
	@Override
	public String toString() {
		return "IsolationWindow [minMz=" + minMz + ", maxMz=" + maxMz + "]";
	}

}
