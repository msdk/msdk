/*
 * Package fr.profi.mzdb.model
 * @author David Bouyssie
 */
package fr.profi.mzdb.model;

import java.util.Comparator;

// TODO: Auto-generated Javadoc
/**
 * The Class Peak.
 * 
 * @author David Bouyssie
 */
public class Peak implements Cloneable, Comparable<Peak> {

    /** The mz. */
    protected final double mz;

    /** The intensity. */
    protected float intensity;

    /** The left hwhm. */
    protected final float leftHwhm;

    /** The right hwhm. */
    protected final float rightHwhm;

    private float _nf = -1;

    /** The peak context. */
    protected ILcContext lcContext;

    // public float elutionTime; // only for LCMS maps
    // public int spectrumId; // only for LCMS maps
    // public int spectrumSliceId; // only for LCMS maps

    /**
     * Instantiates a new peak.
     * 
     * @param mz
     *            the mz
     * @param intensity
     *            the intensity
     * @param leftHwhm
     *            the left hwhm
     * @param rightHwhm
     *            the right hwhm
     * @param lcContext
     *            the LC context
     */
    public Peak(double mz, float intensity, float leftHwhm, float rightHwhm, ILcContext lcContext) {
	super();
	this.mz = mz;
	this.intensity = intensity;
	this.leftHwhm = leftHwhm;
	this.rightHwhm = rightHwhm;
	this.lcContext = lcContext;
    }

    /**
     * Instantiates a new peak.
     * 
     * @param mz
     *            peak's mass
     * @param intensity
     *            peak's intensity
     */
    public Peak(double mz, float intensity) {
	this(mz, intensity, 0, 0, null);
    }

    /**
     * Gets the mz.
     * 
     * @return the mz
     */
    public double getMz() {
	return mz;
    }

    /**
     * Gets the intensity.
     * 
     * @return the intensity
     */
    public float getIntensity() {
	return intensity;
    }

    /**
     * Gets the left hwhm.
     * 
     * @return the left hwhm
     */
    public float getLeftHwhm() {
	return leftHwhm;
    }

    /**
     * Gets the right hwhm.
     * 
     * @return the right hwhm
     */
    public float getRightHwhm() {
	return rightHwhm;
    }

	/**
	 * Gets the lc context.
	 * 
	 * @return the lc context
	 */
	public ILcContext getLcContext() {
		return lcContext;
	}

	/**
	 * Sets the lc context.
	 * 
	 * @param lcContext
	 *            the new lc context
	 */
	public void setLcContext(ILcContext lcContext) {
		this.lcContext = lcContext;
	}
	
	/**
	 * Gets the SpectrumHeader.
	 * 
	 * @return the SpectrumHeader
	 */
	public SpectrumHeader getSpectrumHeader() throws ClassCastException {
		return (SpectrumHeader) lcContext;
	}

    public boolean isNormalized() {
	return this._nf > 0;
    }

    public void normalizeIntensity(float nf) throws Exception {
	if (nf < 0) {
	    throw new IllegalArgumentException("nf can't be negative");
	}
	if (this._nf > 0)
	    throw new Exception("peak intensity has been already normalized");

	this._nf = nf;
	this.intensity *= nf;
    }

    /*
     * (non-Javadoc)
     * 
     * @see java.lang.Object#clone()
     */
    protected Peak clone() {
	return new Peak(mz, intensity, leftHwhm, rightHwhm, lcContext);
    }

    /*
     * (non-Javadoc)
     * 
     * @see java.lang.Object#toString()
     */
    @Override
    public String toString() {
	return String.format("Peak@ %.4f;%.1f", this.mz, this.intensity);
    }

    /**
     * Gets the intensity comp.
     * 
     * @return the intensity comp
     */
    public static Comparator<Peak> getIntensityComp() {
	return intensityComp;
    }

    // TODO: put private (also ins spectrum headers and other comparators)
    /** The intensity comp. */
    public static Comparator<Peak> intensityComp = new Comparator<Peak>() {
	// @Override
	public int compare(Peak peak0, Peak peak1) {
	    if (peak0.intensity < peak1.intensity)
		return -1;
	    else if (Math.abs(peak0.intensity - peak1.intensity) < 1e-6)
		return 0;
	    else
		return 1;
	}
    };

    /*
     * (non-Javadoc)
     * 
     * @see java.lang.Comparable#compareTo(java.lang.Object)
     */
    // @Override
    public int compareTo(Peak aPeak) {
	if (mz < aPeak.mz)
	    return -1;
	else if (Math.abs(mz - aPeak.mz) < Double.MIN_VALUE)
	    return 0;
	else
	    return 1;
    }

}
