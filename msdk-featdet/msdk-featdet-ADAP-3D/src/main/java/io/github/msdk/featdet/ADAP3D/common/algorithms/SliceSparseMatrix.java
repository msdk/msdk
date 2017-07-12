/*
 * (C) Copyright 2015-2017 by MSDK Development Team
 *
 * This software is dual-licensed under either
 *
 * (a) the terms of the GNU Lesser General Public License version 2.1 as published by the Free
 * Software Foundation
 *
 * or (per the licensee's choosing)
 *
 * (b) the terms of the Eclipse Public License v1.0 as published by the Eclipse Foundation.
 */
package io.github.msdk.featdet.ADAP3D.common.algorithms;

import java.util.ArrayList;
import java.util.Collections;
import java.util.Comparator;
import java.util.HashSet;
import java.util.List;
import java.util.Set;
import java.lang.Math;

import io.github.msdk.datamodel.rawdata.MsScan;
import io.github.msdk.datamodel.rawdata.RawDataFile;

import org.apache.commons.collections4.MapIterator;
import org.apache.commons.collections4.keyvalue.MultiKey;
import org.apache.commons.collections4.map.MultiKeyMap;


/**
 * <p>
 * SliceSparseMatrix class is used for slicing the sparse matrix of raw data as per given mz value.
 * slice contains intensities for one mz value for different scans. In this class sparse matrix has
 * been implemented in the form of multikey map. Mz and scan number are keys and object containing
 * Intensities along information like retention time and whether the value is still in matrix or not
 * is the value. Consider Scan numbers as column index and mz values as row index. cell values as
 * object containing Intensities along with other information like retention time and whether the
 * value is still in matrix or not.
 * </p>
 */

public class SliceSparseMatrix {

  /**
   * <p>
   * tripletMap is used for creating MultiKeyMap type of hashmap from raw data file.
   * </p>
   */
  private final MultiKeyMap<Integer, Triplet> tripletMap;

  /**
   * <p>
   * filterListOfTriplet is used for adding intensities for same mz values under same scan numbers.
   * </p>
   */
  private final List<Triplet> filterListOfTriplet;

  /**
   * <p>
   * maxIntensityIndex is used for keeping track of next maximum intensity in the loop.
   * </p>
   */
  private int maxIntensityIndex = 0;

  /**
   * <p>
   * roundMz is used for rounding mz value.
   * </p>
   */
  private final int roundMzFactor = 10000;

  /**
   * <p>
   * listOfScans is used for getting scan objects from raw data file.
   * </p>
   */
  private final List<MsScan> listOfScans;

  /**
   * <p>
   * mzValues is used to store all the mz values from raw file.
   * </p>
   */
  public final List<Integer> mzValues;

  /**
   * <p>
   * Triplet is used for representing elements of sparse matrix.
   * </p>
   */
  public static class Triplet {
    public int mz;
    public int scanNumber;
    public float intensity;
    public float rt;
    public boolean removed;
  }

  /**
   * <p>
   * This is the data model for getting vertical slice from sparse matrix.
   * </p>
   */
  public static class VerticalSliceDataPoint {
    float mz;
    float intensity;
  }

  /**
   * <p>
   * This constructor takes raw data file and create the triplet map which contains information such
   * as mz,intensity,rt,scan number
   * </p>
   * 
   * @param rawFile a {@link io.github.msdk.datamodel.rawdata.RawDataFile} object. This is raw data
   *        file object by which we can pass raw file.
   */
  public SliceSparseMatrix(RawDataFile rawFile) {
    listOfScans = rawFile.getScans();
    List<Triplet> listOfTriplet = new ArrayList<Triplet>();

    for (int i = 0; i < listOfScans.size(); i++) {
      MsScan scan = listOfScans.get(i);

      if (scan == null)
        continue;

      double mzBuffer[];
      float intensityBuffer[];
      Float rt;
      mzBuffer = scan.getMzValues();
      intensityBuffer = scan.getIntensityValues();
      rt = scan.getRetentionTime();

      if (rt == null)
        continue;

      for (int j = 0; j < mzBuffer.length; j++) {
        Triplet triplet = new Triplet();
        triplet.intensity = intensityBuffer[j];
        triplet.mz = roundMZ(mzBuffer[j]);
        triplet.scanNumber = i;
        triplet.rt = rt;
        triplet.removed = false;
        listOfTriplet.add(triplet);
      }
    }



    Comparator<Triplet> compare = new Comparator<Triplet>() {

      @Override
      public int compare(Triplet o1, Triplet o2) {

        Integer scan1 = o1.scanNumber;
        Integer scan2 = o2.scanNumber;
        int scanCompare = scan1.compareTo(scan2);

        if (scanCompare != 0) {
          return scanCompare;
        } else {
          Integer mz1 = o1.mz;
          Integer mz2 = o2.mz;
          return mz1.compareTo(mz2);
        }
      }
    };


    Collections.sort(listOfTriplet, compare);

    filterListOfTriplet = new ArrayList<Triplet>();
    Triplet currTriplet = new Triplet();
    Triplet lastFilterTriplet = new Triplet();
    tripletMap = new MultiKeyMap<Integer, Triplet>();
    int index = 0;
    Set<Integer> mzSet = new HashSet<Integer>();

    filterListOfTriplet.add(listOfTriplet.get(0));
    for (int i = 1; i < listOfTriplet.size(); i++) {
      currTriplet = listOfTriplet.get(i);
      mzSet.add(listOfTriplet.get(i).mz);
      lastFilterTriplet = filterListOfTriplet.get(index);
      if (currTriplet.mz == lastFilterTriplet.mz
          && currTriplet.scanNumber == lastFilterTriplet.scanNumber) {
        lastFilterTriplet.intensity += currTriplet.intensity;
      } else {
        filterListOfTriplet.add(currTriplet);
        tripletMap.put(currTriplet.scanNumber, currTriplet.mz, currTriplet);
        index++;
      }

    }
    mzValues = new ArrayList<Integer>(mzSet);


    Collections.sort(mzValues);
  }

  /**
   * <p>
   * This method returns the MultiKeyMap slice of data for given mz,lowerScanBound,upperScanBound
   * </p>
   * 
   * @param mz a {@link java.lang.Double} object. This is original m/z value from raw file.
   * @param lowerScanBound a {@link java.lang.Integer} object. This is lowest scan number in the
   *        horizontal matrix slice.
   * @param upperScanBound a {@link java.lang.Integer} object. This is highest scan number in the
   *        horizontal matrix slice.
   * 
   * @return sliceMap a {@link org.apache.commons.collections4.map.MultiKeyMap} object. This object
   *         contains horizontal slice with single m/z value,different scan numbers and different
   *         intensities along with retention time.
   */
  public MultiKeyMap<Integer, Triplet> getHorizontalSlice(double mz, int lowerScanBound,
      int upperScanBound) {

    int roundedmz = roundMZ(mz);
    MultiKeyMap<Integer, Triplet> sliceMap = new MultiKeyMap<Integer, Triplet>();

    for (int i = lowerScanBound; i <= upperScanBound; i++) {
      if (tripletMap.containsKey(new Integer(i), new Integer(roundedmz))) {
        Triplet triplet = (Triplet) tripletMap.get(new Integer(i), new Integer(roundedmz));
        sliceMap.put(i, roundedmz, triplet);
      } else {
        sliceMap.put(i, roundedmz, null);
      }
    }

    return sliceMap;
  }

  /**
   * <p>
   * This method returns the MultiKeyMap slice of data for rounded mz,lowerScanBound,upperScanBound
   * </p>
   * 
   * @param roundedMZ a {@link java.lang.Double} object. This is rounded m/z value which is already
   *        multiplied by 10000.
   * @param lowerScanBound a {@link java.lang.Integer} object. This is lowest scan number in the
   *        horizontal matrix slice.
   * @param upperScanBound a {@link java.lang.Integer} object. This is highest scan number in the
   *        horizontal matrix slice.
   * 
   * @return sliceMap a {@link org.apache.commons.collections4.map.MultiKeyMap} object. This object
   *         contains horizontal slice with single m/z value,different scan numbers and different
   *         intensities along with retention time.
   */
  public MultiKeyMap<Integer, Triplet> getHorizontalSlice(int roundedMZ, int lowerScanBound,
      int upperScanBound) {
    return getHorizontalSlice((double) roundedMZ / roundMzFactor, lowerScanBound, upperScanBound);
  }

  /**
   * <p>
   * This method returns the List of type VerticalSliceDataPoint for given Scan Number.
   * </p>
   * 
   * @param scanNumber a {@link java.lang.Integer} object. This is scan number for which we get
   *        vertical slice from sparse matrix.
   * @return datapointList a
   *         {@link io.github.msdk.featdet.ADAP3D.common.algorithms.SliceSparseMatrix.VerticalSliceDataPoint}
   *         list. This is list containing m/z and intensities for one scan number.
   */
  public List<VerticalSliceDataPoint> getVerticalSlice(int scanNumber) {


    List<VerticalSliceDataPoint> datapointList = new ArrayList<VerticalSliceDataPoint>();

    for (int roundedMZ : mzValues) {
      VerticalSliceDataPoint datapoint = new VerticalSliceDataPoint();

      if (tripletMap.containsKey(new Integer(scanNumber), new Integer(roundedMZ))) {

        datapoint.intensity =
            ((Triplet) tripletMap.get(new Integer(scanNumber), new Integer(roundedMZ))).intensity;
        datapoint.mz = (float) roundedMZ / roundMzFactor;
        datapointList.add(datapoint);
      } else {
        datapoint.intensity = (float) 0.0;
        datapoint.mz = (float) roundedMZ / roundMzFactor;
        datapointList.add(datapoint);
      }
    }
    return datapointList;
  }



  /**
   * <p>
   * This method finds next maximum intensity from filterListOfTriplet
   * </p>
   * 
   * @return tripletObject a {@link Triplet} object. This is element of sparse matrix.
   */
  public Triplet findNextMaxIntensity() {

    Triplet tripletObject = null;
    Comparator<Triplet> compare = new Comparator<Triplet>() {

      @Override
      public int compare(Triplet o1, Triplet o2) {

        Float intensity1 = o1.intensity;
        Float intensity2 = o2.intensity;
        int intensityCompare = intensity2.compareTo(intensity1);
        return intensityCompare;
      }
    };
    Collections.sort(filterListOfTriplet, compare);

    for (int i = maxIntensityIndex; i < filterListOfTriplet.size(); i++) {
      if (filterListOfTriplet.get(i).removed == false) {
        tripletObject = filterListOfTriplet.get(i);
        maxIntensityIndex = i + 1;
        break;
      }

    }
    return tripletObject;
  }

  /**
   * <p>
   * This method returns sorted list of ContinuousWaveletTransform.DataPoint object.Object contain
   * retention time and intensity values.
   * </p>
   * 
   * @param slice a {@link org.apache.commons.collections4.map.MultiKeyMap} object. This is
   *        horizontal slice from sparse matrix.
   * @return listOfDataPoint a {@link Triplet} list. This returns list of retention time and
   *         intensities.
   */
  public List<ContinuousWaveletTransform.DataPoint> getCWTDataPoint(
      MultiKeyMap<Integer, Triplet> slice) {

    MapIterator<MultiKey<? extends Integer>, Triplet> iterator = slice.mapIterator();
    List<ContinuousWaveletTransform.DataPoint> listOfDataPoint =
        new ArrayList<ContinuousWaveletTransform.DataPoint>();

    while (iterator.hasNext()) {
      ContinuousWaveletTransform.DataPoint dataPoint = new ContinuousWaveletTransform.DataPoint();
      iterator.next();
      MultiKey<Integer> sliceKey = (MultiKey<Integer>) iterator.getKey();
      Triplet triplet = (Triplet) slice.get(sliceKey);
      if (triplet != null) {
        dataPoint.rt = triplet.rt / 60;
        dataPoint.intensity = triplet.intensity;
        listOfDataPoint.add(dataPoint);
      } else {
        MsScan scan = listOfScans.get((int) sliceKey.getKey(0));
        dataPoint.rt = scan.getRetentionTime() / 60;
        dataPoint.intensity = 0.0;
        listOfDataPoint.add(dataPoint);
      }
    }
    Comparator<ContinuousWaveletTransform.DataPoint> compare =
        new Comparator<ContinuousWaveletTransform.DataPoint>() {

          @Override
          public int compare(ContinuousWaveletTransform.DataPoint o1,
              ContinuousWaveletTransform.DataPoint o2) {
            Double rt1 = o1.rt;
            Double rt2 = o2.rt;
            return rt1.compareTo(rt2);
          }
        };

    Collections.sort(listOfDataPoint, compare);

    return listOfDataPoint;
  }

  /**
   * <p>
   * This method removes data points from whole data set for given mz,lowerscanbound and
   * upperscanbound
   * </p>
   * 
   * @param mz a {@link java.lang.Double} object.This takes original m/z value from raw file.
   * @param lowerScanBound a {@link java.lang.Integer} object.This is lowest scan number.
   * @param upperScanBound a {@link java.lang.Integer} object.This is highest scan number.
   * @return tripletMap a {@link org.apache.commons.collections4.map.MultiKeyMap} object. This is
   *         whole sparse matrix.
   */
  public MultiKeyMap<Integer, Triplet> removeDataPoints(double mz, int lowerScanBound,
      int upperScanBound) {
    int roundedmz = roundMZ(mz);
    for (int i = lowerScanBound; i <= upperScanBound; i++) {
      if (tripletMap.containsKey(new Integer(i), new Integer(roundedmz))) {
        Triplet triplet = (Triplet) tripletMap.get(new Integer(i), new Integer(roundedmz));
        triplet.removed = true;
      }
    }
    return tripletMap;
  }

  /**
   * <p>
   * This method rounds mz value based on roundMz variable
   * </p>
   * 
   * @param mz a {@link java.lang.Double} object. This takes original m/z value from raw file.
   * @return roundedmz a {@link java.lang.Integer} object. This value is rounded by multiplying
   *         10000.
   */
  public int roundMZ(double mz) {
    int roundedmz = (int) Math.round(mz * roundMzFactor);
    return roundedmz;
  }

  /**
   * <p>
   * This method sets maxIntensityIndex to 0
   * </p>
   */
  public void setMaxIntensityIndexZero() {
    maxIntensityIndex = 0;
  }

  /**
   * <p>
   * This method returns size of raw data file in terms of total scans.
   * 
   * @return size a {@link java.lang.Integer} object. This is total number of scans in raw file.
   *         </p>
   */
  public int getSizeOfRawDataFile() {
    int size = listOfScans.size();
    return size;
  }

}
