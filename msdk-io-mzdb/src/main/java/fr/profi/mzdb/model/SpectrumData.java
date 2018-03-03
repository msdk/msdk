package fr.profi.mzdb.model;

import java.util.Arrays;

import org.apache.commons.lang3.ArrayUtils;

import fr.profi.mzdb.util.ms.MsUtils;

// TODO: Auto-generated Javadoc
/**
 * The Class SpectrumData.
 * 
 * @author David Bouyssie
 */
public class SpectrumData {

	/** The mz list. */
	protected double[] mzList;

	/** The intensity list. */
	protected float[] intensityList;

	/** The left hwhm list. */
	protected float[] leftHwhmList;

	/** The right hwhm list. */
	protected float[] rightHwhmList;

	protected int peaksCount;

	/**
	 * Instantiates a new spectrum data.
	 * 
	 * @param mzList
	 *            the mz list
	 * @param intensityList
	 *            the intensity list
	 * @param lHwhmList
	 *            the l hwhm list
	 * @param rHwhmList
	 *            the r hwhm list
	 */
	public SpectrumData(double[] mzList, float[] intensityList, float[] lHwhmList, float[] rHwhmList) {
		super();
		this.peaksCount = mzList.length;
		this.mzList = mzList;
		this.intensityList = intensityList;
		this.leftHwhmList = lHwhmList;
		this.rightHwhmList = rHwhmList;
	}

	/**
	 * Instantiates a new spectrum data.
	 * 
	 * @param mzList
	 *            the mz list
	 * @param intensityList
	 *            the intensity list
	 */
	public SpectrumData(double[] mzList, float[] intensityList) {
		this(mzList, intensityList, null, null);
	}
	
	/**
	 * Gets the peaks count.
	 * 
	 * @return the peaks list
	 */
	public int getPeaksCount() {
		return peaksCount;
	}

	/**
	 * Gets the mz list.
	 * 
	 * @return the mz list
	 */
	public double[] getMzList() {
		return mzList;
	}

	/**
	 * Gets the intensity list.
	 * 
	 * @return the intensity list
	 */
	public float[] getIntensityList() {
		return intensityList;
	}

	/**
	 * Gets the left hwhm list.
	 * 
	 * @return the left hwhm list
	 */
	public float[] getLeftHwhmList() {
		return leftHwhmList;
	}

	/**
	 * Gets the right hwhm list.
	 * 
	 * @return the right hwhm list
	 */
	public float[] getRightHwhmList() {
		return rightHwhmList;
	}

	/**
	 * To peaks. A new peaks tab is instantiated at each call
	 * 
	 * @return the peak[]
	 */
	public Peak[] toPeaks(ILcContext lcContext) {
		Peak[] peaks = new Peak[peaksCount];

		for (int i = 0; i < peaksCount; i++) {

			float leftHwhm = 0, rightHwhm = 0;
			if (leftHwhmList != null && rightHwhmList != null) {
				leftHwhm = leftHwhmList[i];
				rightHwhm = rightHwhmList[i];
			}

			peaks[i] = new Peak(mzList[i], intensityList[i], leftHwhm, rightHwhm, lcContext);
		}
		return peaks;
	}

	/**
	 * Adds the spectrum data.
	 * 
	 * @param spectrumData
	 *            the spectrum data
	 */
	// TODO: create a SpectrumDataBuilder instead anddon't use apache ArrayUtils
	public void addSpectrumData(SpectrumData spectrumData) {
		if (spectrumData != null) {
			this.mzList = ArrayUtils.addAll(this.mzList, spectrumData.mzList);
			this.intensityList = ArrayUtils.addAll(this.intensityList, spectrumData.intensityList);
			if (spectrumData.leftHwhmList != null && spectrumData.rightHwhmList != null) {
				this.leftHwhmList = ArrayUtils.addAll(this.leftHwhmList, spectrumData.leftHwhmList);
				this.rightHwhmList = ArrayUtils.addAll(this.rightHwhmList, spectrumData.rightHwhmList);
			}
			this.peaksCount = this.mzList.length;
		}
	}

	/**
	 * Resize data arrays.
	 * 
	 * @param newLength
	 *            the new length
	 */
	public void resizeDataArrays(int newLength) {
		this.mzList = Arrays.copyOf(this.mzList, newLength);
		this.intensityList = Arrays.copyOf(this.intensityList, newLength);

		if (this.leftHwhmList != null && this.rightHwhmList != null) {
			this.leftHwhmList = Arrays.copyOf(this.leftHwhmList, newLength);
			this.rightHwhmList = Arrays.copyOf(this.rightHwhmList, newLength);
		}
		
		this.peaksCount = newLength;
	}
	
	/**
	 * Gets the min mz.
	 * 
	 * @return the min mz
	 */
	public double getMinMz() {
		// supposed and i hope it will always be true that mzList is sorted
		// do not do any verification
		if (peaksCount == 0) {
			return 0;
		}
		return mzList[0];
	}

	/**
	 * Gets the max mz.
	 * 
	 * @return the max mz
	 */
	public double getMaxMz() {
		// supposed and i hope it will always be true that mzList is sorted
		// do not do any verification
		if (peaksCount == 0) {
			return 0;
		}
		return mzList[peaksCount - 1];
	}

	/**
	 * Checks if is empty.
	 * 
	 * @return true, if is empty
	 */
	public boolean isEmpty() {
		// supposing intensityList and others have the same size
		return peaksCount == 0; 
	}

	/**
	 * _bin search index to nearest index.
	 * 
	 * @param binSearchIndex
	 *            the bin search index
	 * @param length
	 *            the length
	 * @return the int
	 */
	private int _binSearchIndexToNearestIndex(int binSearchIndex) {
		if (binSearchIndex >= 0)
			return binSearchIndex;
		else {
			int idx = -binSearchIndex - 1;
			if (idx == 0)
				return -1;
			else
				return idx;
		}
	}
	
	/** assuming mzList is sorted */
	public Peak getNearestPeak(double mz, double mzTolPPM, ILcContext lcContext) {

		if (peaksCount == 0)
			return null;
		
		double[] myMzList = this.mzList;

		final double mzDa = MsUtils.ppmToDa(mz, mzTolPPM);
		final int binSearchIndex = Arrays.binarySearch(myMzList, mz);
		/*if (binSearchIndex >= 0) {
			System.out.println("data found");
		}*/
		
		int idx = binSearchIndex >= 0 ? binSearchIndex : -binSearchIndex - 1;
		double prevVal = 0.0, nextVal = 0.0;
		int newIdx = 0;

		if (idx == peaksCount) {
			prevVal = myMzList[peaksCount - 1];
			if (Math.abs(mz - prevVal) > mzDa)
				return null;
			newIdx = idx - 1;
		} else if (idx == 0) {
			// System.out.println("idx == zero");
			nextVal = myMzList[idx];
			if (Math.abs(mz - nextVal) > mzDa)
				return null;
			newIdx = idx;
			// System.out.println(""+ this.mzList[idx] +", "+ mz);

		} else {
			nextVal = myMzList[idx];
			prevVal = myMzList[idx - 1];

			final double diffNextVal = Math.abs(mz - nextVal);
			final double diffPrevVal = Math.abs(mz - prevVal);

			if (diffNextVal < diffPrevVal) {
				if (diffNextVal > mzDa)
					return null;
				newIdx = idx;
			} else {
				if (diffPrevVal > mzDa)
					return null;
				newIdx = idx - 1;
			}
		}
		
		return new Peak(
			myMzList[newIdx],
			this.intensityList[newIdx],
			this.leftHwhmList[newIdx],
			this.rightHwhmList[newIdx],
			lcContext
		);

	}

	public int getNearestPeakIndex(double value) {
		int idx = Arrays.binarySearch(this.mzList, value);
		idx = (idx < 0) ? ~idx : idx;
		double min = Double.MAX_VALUE;
		for (int k = Math.max(0, idx - 1); k <= Math.min(this.peaksCount - 1, idx + 1); k++) {
			if (Math.abs(this.mzList[k] - value) < min) {
				min = Math.abs(this.mzList[k] - value);
				idx = k;
			}
		}
		return idx;
	}

	public int getPeakIndex(double value, double ppmTol) {
		int idx = Arrays.binarySearch(this.mzList, value);
		idx = (idx < 0) ? ~idx : idx;
		double min = Double.MAX_VALUE;
		int resultIdx = -1;
		for (int k = Math.max(0, idx - 1); k <= Math.min(this.peaksCount - 1, idx + 1); k++) {
			if (((1e6 * Math.abs(this.mzList[k] - value) / value) < ppmTol) && (Math.abs(this.mzList[k] - value) < min)) {
				min = Math.abs(this.mzList[k] - value);
				resultIdx = k;
			}
		}
		return resultIdx;
	}

	/**
	 * Mz range filter.
	 * 
	 * @param mzMin
	 *            the mz min
	 * @param mzMax
	 *            the mz max
	 * @return the spectrum data
	 */
	public SpectrumData mzRangeFilter(double mzMin, double mzMax) {
		if (mzMin > mzMax) {
			double tmp = mzMax;
			mzMax = mzMin;
			mzMin = tmp;
		}
		int nbPoints = peaksCount;

		// Retrieve the index of nearest minimum value if it exists
		int minBinSearchIndex = Arrays.binarySearch(this.mzList, mzMin);
		int firstIdx = this._binSearchIndexToNearestIndex(minBinSearchIndex);
		// If out of bounds => return empty spectrum data
		if (firstIdx == nbPoints)
			return null;
		// If first index => set the first value index as the array first index
		if (firstIdx == -1)
			firstIdx = 0;

		// Retrieve the index of nearest maximum value if it exists
		int maxBinSearchIndex = Arrays.binarySearch(this.mzList, firstIdx, nbPoints, mzMax);
		int lastIdx = this._binSearchIndexToNearestIndex(maxBinSearchIndex);
		// If first index => return empty spectrum data
		if (lastIdx == -1)
			return null;

		SpectrumData filteredSpectrumData = new SpectrumData(Arrays.copyOfRange(mzList, firstIdx, lastIdx), Arrays.copyOfRange(intensityList, firstIdx, lastIdx));

		if (this.leftHwhmList != null) {
			filteredSpectrumData.leftHwhmList = Arrays.copyOfRange(this.leftHwhmList, firstIdx, lastIdx);
			filteredSpectrumData.rightHwhmList = Arrays.copyOfRange(this.rightHwhmList, firstIdx, lastIdx);
		}

		return filteredSpectrumData;
	}

}
