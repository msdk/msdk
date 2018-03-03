/**
 *
 */
package fr.profi.mzdb;

/**
 * @author JeT
 *
 */
/**
 * class holding bounding box dimensions that retrieved from the param_tree of the mzdb table. We distinguish
 * two sizes, one for ms1, the other one for all msn
 */
public class BBSizes {
    public double BB_MZ_HEIGHT_MS1;
    public double BB_MZ_HEIGHT_MSn;
    public float BB_RT_WIDTH_MS1;
    public float BB_RT_WIDTH_MSn;

    /**
     * Default Constructor
     */
    public BBSizes() {
	this(0, 0, 0, 0);
    }

    /**
     * Quick constructor
     * 
     * @param bB_MZ_HEIGHT_MS1
     * @param bB_MZ_HEIGHT_MSn
     * @param bB_RT_WIDTH_MS1
     * @param bB_RT_WIDTH_MSn
     */
    public BBSizes(double bB_MZ_HEIGHT_MS1, double bB_MZ_HEIGHT_MSn, float bB_RT_WIDTH_MS1,
	    float bB_RT_WIDTH_MSn) {
	super();
	this.BB_MZ_HEIGHT_MS1 = bB_MZ_HEIGHT_MS1;
	this.BB_MZ_HEIGHT_MSn = bB_MZ_HEIGHT_MSn;
	this.BB_RT_WIDTH_MS1 = bB_RT_WIDTH_MS1;
	this.BB_RT_WIDTH_MSn = bB_RT_WIDTH_MSn;
    }

    @Override
    public int hashCode() {
	final int prime = 31;
	int result = 1;
	long temp;
	temp = Double.doubleToLongBits(this.BB_MZ_HEIGHT_MS1);
	result = (prime * result) + (int) (temp ^ (temp >>> 32));
	temp = Double.doubleToLongBits(this.BB_MZ_HEIGHT_MSn);
	result = (prime * result) + (int) (temp ^ (temp >>> 32));
	temp = Double.doubleToLongBits(this.BB_RT_WIDTH_MS1);
	result = (prime * result) + (int) (temp ^ (temp >>> 32));
	temp = Double.doubleToLongBits(this.BB_RT_WIDTH_MSn);
	result = (prime * result) + (int) (temp ^ (temp >>> 32));
	return result;
    }

    @Override
    public boolean equals(Object obj) {
	if (this == obj) {
	    return true;
	}
	if (obj == null) {
	    return false;
	}
	if (this.getClass() != obj.getClass()) {
	    return false;
	}
	BBSizes other = (BBSizes) obj;
	if (Double.doubleToLongBits(this.BB_MZ_HEIGHT_MS1) != Double
		.doubleToLongBits(other.BB_MZ_HEIGHT_MS1)) {
	    return false;
	}
	if (Double.doubleToLongBits(this.BB_MZ_HEIGHT_MSn) != Double
		.doubleToLongBits(other.BB_MZ_HEIGHT_MSn)) {
	    return false;
	}
	if (Double.doubleToLongBits(this.BB_RT_WIDTH_MS1) != Double.doubleToLongBits(other.BB_RT_WIDTH_MS1)) {
	    return false;
	}
	if (Double.doubleToLongBits(this.BB_RT_WIDTH_MSn) != Double.doubleToLongBits(other.BB_RT_WIDTH_MSn)) {
	    return false;
	}
	return true;
    }

    @Override
    public String toString() {
	return "BBSizes [BB_MZ_HEIGHT_MS1=" + this.BB_MZ_HEIGHT_MS1 + ", BB_MZ_HEIGHT_MSn="
		+ this.BB_MZ_HEIGHT_MSn + ", BB_RT_WIDTH_MS1=" + this.BB_RT_WIDTH_MS1 + ", BB_RT_WIDTH_MSn="
		+ this.BB_RT_WIDTH_MSn + "]";
    }

};
