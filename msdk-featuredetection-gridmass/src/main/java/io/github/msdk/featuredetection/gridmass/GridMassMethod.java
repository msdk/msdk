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

package io.github.msdk.featuredetection.gridmass;

import java.util.ArrayList;
import java.util.Arrays;
import java.util.HashMap;
import java.util.List;

import javax.annotation.Nonnull;
import javax.annotation.Nullable;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import io.github.msdk.MSDKException;
import io.github.msdk.MSDKMethod;
import io.github.msdk.datamodel.FeatureTable;
import io.github.msdk.datamodel.MsScan;
import io.github.msdk.datamodel.RawDataFile;
import io.github.msdk.datamodel.SimpleChromatogram;
import io.github.msdk.datamodel.SimpleFeature;
import io.github.msdk.datamodel.SimpleFeatureTable;
import io.github.msdk.datamodel.SimpleFeatureTableRow;
import io.github.msdk.datamodel.SimpleSample;
import io.github.msdk.util.ChromatogramUtil;
import io.github.msdk.util.tolerances.MzTolerance;

/**
 * <p>
 * GridMassMethod class.
 * </p>
 */
public class GridMassMethod implements MSDKMethod<FeatureTable> {

  final Logger logger = LoggerFactory.getLogger(this.getClass());

  private final @Nonnull RawDataFile rawDataFile;

  private boolean canceled = false;
  private int processedScans = 0, totalScans = 0;

  private HashMap<MsScan, DataPoint[]> dpCache = null;

  // scan counter
  private final @Nonnull List<MsScan> scans;
  Datum[] roi[];
  double retentiontime[];

  // User parameters
  private double mzTol;
  private double intensitySimilarity;
  private double minimumTimeSpan, maximumTimeSpan;
  private double smoothTimeSpan, smoothTimeMZ, smoothMZ;
  private double additionTimeMaxPeaksPerScan;
  private double minimumHeight;
  private double rtPerScan;
  private int tolScans;
  private int maxTolScans;
  private int debug = 0;

  private double minMasa = 0;
  private double maxMasa = 0;

  private SimpleFeatureTable newPeakList;
  private SimpleSample newSample;

  private String ignoreTimes = "";


  /**
   * <p>
   * Constructor for GridMassMethod.
   * </p>
   *
   * @param rawDataFile a {@link io.github.msdk.datamodel.RawDataFile} object.
   * @param scans a {@link java.util.List} object.
   * @param mzTol a {@link io.github.msdk.util.tolerances.MzTolerance} object.
   * @param intensitySimilarity a {@link java.lang.Double} object.
   * @param minimumTimeSpan a {@link java.lang.Double} object.
   * @param maximumTimeSpan a {@link java.lang.Double} object.
   * @param smoothTimeSpan a {@link java.lang.Double} object.
   * @param smoothTimeMZ a {@link java.lang.Double} object.
   * @param smoothMZ a {@link java.lang.Double} object.
   * @param additionTimeMaxPeaksPerScan a {@link java.lang.Double} object.
   * @param minimumHeight a {@link java.lang.Double} object.
   * @param rtPerScan a {@link java.lang.Double} object.
   */
  public GridMassMethod(@Nonnull RawDataFile rawDataFile, @Nonnull List<MsScan> scans,
      @Nonnull MzTolerance mzTol, @Nonnull Double intensitySimilarity,
      @Nonnull Double minimumTimeSpan, @Nonnull Double maximumTimeSpan,
      @Nonnull Double smoothTimeSpan, @Nonnull Double smoothTimeMZ, @Nonnull Double smoothMZ,
      @Nonnull Double additionTimeMaxPeaksPerScan, @Nonnull Double minimumHeight,
      @Nonnull Double rtPerScan) {
    this.rawDataFile = rawDataFile;
    this.scans = scans;
  }

  /** {@inheritDoc} */
  @Override
  public FeatureTable execute() throws MSDKException {

    logger.info("Started GRIDMASS v1.0 [Apr-09-2014]");

    totalScans = scans.size();

    // Check if the scans are properly ordered by RT
    double prevRT = Double.NEGATIVE_INFINITY;
    for (MsScan s : scans) {
      if (s.getRetentionTime() < prevRT) {
        final String msg = "Retention time of scan #" + s.getScanNumber()
            + " is smaller then the retention time of the previous scan."
            + " Please make sure you only use scans with increasing retention times."
            + " You can restrict the scan numbers in the parameters, or you can use the Crop filter module";
        throw new MSDKException(msg);
      }
      prevRT = s.getRetentionTime();
    }

    // Create new peak list
    newPeakList = new SimpleFeatureTable();
    newSample = new SimpleSample(rawDataFile.getName(), rawDataFile);

    // If no scans, return
    if (scans.size() == 0) {
      return newPeakList;
    }

    int j;
    // minimumTimeSpan
    MsScan scan = scans.get(0);
    double minRT = scan.getRetentionTime();
    double maxRT = scan.getRetentionTime();
    retentiontime = new double[totalScans];
    int i;
    for (i = 0; i < totalScans; i++) {
      scan = scans.get(i);
      double irt = scan.getRetentionTime();
      if (irt < minRT)
        minRT = irt;
      if (irt > maxRT)
        maxRT = irt;
      retentiontime[i] = irt;
    }
    rtPerScan = (maxRT - minRT) / i;
    // "tolerable" units in scans
    tolScans = Math.max(2, (int) ((minimumTimeSpan / rtPerScan)));
    maxTolScans = Math.max(2, (int) ((maximumTimeSpan / rtPerScan)));

    // Algorithm to find masses:
    // (1) copy masses:intensity > threshold
    // (2) sort intensities descend
    // (3) Find "spot" for each intensity
    // (3.1) if they have not spot ID assigned
    // (3.1.1) Extend mass in mass and time while > 70% pixels > threshold
    // (3.1.2) If extension > mintime ==> mark all pixels with the spot ID
    // (3.1.3) if extension < mintime ==> mark all pixels with spot ID = -1
    // (4) Group spots within a time-tolerance and mass-tolerance

    roi = new Datum[totalScans][];
    ArrayList<Datum> roiAL = new ArrayList<Datum>();
    long passed = 0, nopassed = 0;
    minMasa = Double.MAX_VALUE;
    maxMasa = 0;
    int maxJ = 0;
    boolean[] scanOk = new boolean[totalScans];
    Arrays.fill(scanOk, true);

    logger.info(
        "Smoothing data points (Time min=" + smoothTimeSpan + "; Time m/z=" + smoothTimeMZ + ")");
    IndexedDataPoint[][] data = smoothDataPoints(rawDataFile, smoothTimeSpan, smoothTimeMZ, 0,
        smoothMZ, 0, minimumHeight, scans);

    logger.info("Determining intensities (mass sum) per scan on " + rawDataFile);
    for (i = 0; i < totalScans; i++) {
      if (canceled)
        return null;
      scan = scans.get(i);
      IndexedDataPoint mzv[] = data[i]; // scan.getDataPoints();
      double prev = (mzv.length > 0 ? mzv[0].datapoint.getMZ() : 0);
      double massSum = 0;
      for (j = 0; j < mzv.length; j++) {
        if (mzv[j].datapoint.getIntensity() >= minimumHeight)
          massSum += mzv[j].datapoint.getMZ() - prev;
        prev = mzv[j].datapoint.getMZ();
        if (mzv[j].datapoint.getMZ() < minMasa)
          minMasa = mzv[j].datapoint.getMZ();
        if (mzv[j].datapoint.getMZ() > maxMasa)
          maxMasa = mzv[j].datapoint.getMZ();
      }
      double dm = 100.0 / (maxMasa - minMasa);
      if (i % 30 == 0 && debug > 0) {
        logger.debug("");
        System.out.print("t=" + Math.round(retentiontime[i] * 100) / 100.0 + ": (in %) ");
      }
      if (scanOk[i]) {
        if (!scanOk[i]) {
          // Disable neighbouring scans, how many ?
          for (j = i; j > 0
              && retentiontime[j] + additionTimeMaxPeaksPerScan > retentiontime[i]; j--) {
            scanOk[j] = false;
          }
          for (j = i; j < totalScans
              && retentiontime[j] - additionTimeMaxPeaksPerScan < retentiontime[i]; j++) {
            scanOk[j] = false;
          }
        }
        if (debug > 0)
          System.out.print(((int) (massSum * dm)) + (scanOk[i] ? " " : "*** "));
      } else {
        if (debug > 0)
          System.out.print(((int) (massSum * dm)) + (scanOk[i] ? " " : "* "));
      }
    }

    if (debug > 0)
      logger.debug("");

    String[] it = ignoreTimes.trim().split(", ?");
    for (j = 0; j < it.length; j++) {
      String itj[] = it[j].split("-");
      if (itj.length == 2) {
        Double a = Double.parseDouble(itj[0].trim());
        Double b = Double.parseDouble(itj[1].trim());
        for (i = Math.abs(Arrays.binarySearch(retentiontime, a)); i < totalScans
            && retentiontime[i] <= b; i++) {
          if (retentiontime[i] >= a) {
            scanOk[i] = false;
          }
        }
      }
    }

    passed = 0;
    nopassed = 0;
    for (i = 0; i < totalScans; i++) {
      if (canceled)
        return null;
      if (scanOk[i]) {
        scan = scans.get(i);
        IndexedDataPoint mzv[] = data[i];
        DataPoint mzvOriginal[] = getCachedDataPoints(scan);
        ArrayList<Datum> dal = new ArrayList<Datum>();
        for (j = 0; j < mzv.length; j++) {
          if (mzv[j].datapoint.getIntensity() >= minimumHeight) {
            dal.add(new Datum(mzv[j].datapoint, i, mzvOriginal[mzv[j].index]));
            passed++;
          } else {
            nopassed++;
          }
        }
        if (j > maxJ)
          maxJ = j;
        roi[i] = dal.toArray(new Datum[0]);
        roiAL.addAll(dal);
      }
    }
    logger.info(passed + " intensities >= " + minimumHeight + " of " + (passed + nopassed) + " ("
        + Math.round(passed * 10000.0 / (double) (passed + nopassed)) / 100.0 + "%) on "
        + rawDataFile);

    // New "probing" algorithm
    // (1) Generate probes all over chromatograms
    // (2) Move each probe to their closest maximum until it cannot find a
    // new maximum
    // (3) assign spot id to each "center" using all points within region

    // (1) Generate probes all over
    double byMZ = Math.max(mzTol * 2, 1e-6);
    int byScan = Math.max(1, tolScans / 4);
    logger.info("Creating Grid of probes every " + byMZ + " m/z and " + byScan + " scans");
    double m;
    int ndata = (int) Math
        .round((((double) totalScans / (double) byScan) + 1) * ((maxMasa - minMasa + byMZ) / byMZ));
    Probe probes[] = new Probe[ndata];
    int idata = 0;
    for (i = 0; i < totalScans; i += byScan) {
      if (canceled)
        return null;
      for (m = minMasa - (i % 2) * byMZ / 2; m <= maxMasa; m += byMZ) {
        probes[idata++] = new Probe(m, i);
      }
    }

    // (2) Move each probe to their closest center
    double mzR = byMZ / 2;
    int scanR = Math.max(byScan - 1, 2);
    logger.info("Finding local maxima for each probe radius: scans=" + scanR + ", m/z=" + mzR);
    int okProbes = 0;
    for (i = 0; i < idata; i++) {
      if (canceled)
        return null;
      moveProbeToCenter(probes[i], scanR, mzR);
      if (probes[i].intensityCenter < minimumHeight) {
        probes[i] = null;
      } else {
        okProbes++;
      }
    }
    if (okProbes > 0) {
      Probe[] pArr = new Probe[okProbes];
      for (okProbes = i = 0; i < idata; i++) {
        if (probes[i] != null) {
          pArr[okProbes++] = probes[i];
        }
      }
      probes = pArr;
      pArr = null;
    }
    // (3) Assign spot id to each "center"
    logger.info("Sorting probes");
    Arrays.sort(probes);
    logger.info("Assigning spot id to local maxima");
    SpotByProbes sbp = new SpotByProbes();
    ArrayList<SpotByProbes> spots = new ArrayList<SpotByProbes>();
    double mzA = -1;
    int scanA = -1;
    for (i = 0; i < probes.length; i++) {
      if (probes[i] != null && probes[i].intensityCenter >= minimumHeight) {
        if (probes[i].mzCenter != mzA || probes[i].scanCenter != scanA) {
          if (canceled)
            return null;
          if (sbp.size() > 0) {
            spots.add(sbp);
            sbp.assignSpotId();
          }
          sbp = new SpotByProbes();
          mzA = probes[i].mzCenter;
          scanA = probes[i].scanCenter;
        }
        sbp.addProbe(probes[i]);
      }
    }
    if (sbp.size() > 0) {
      spots.add(sbp);
      sbp.assignSpotId();
    }
    logger.info("Spots:" + spots.size());

    // Assign specific datums to spots to avoid using datums to several
    // spots
    logger.info("Assigning intensities to local maxima on");
    i = 0;
    for (SpotByProbes sx : spots) {
      if (sx.size() > 0) {
        if (canceled)
          return null;
        assignSpotIdToDatumsFromScans(sx, scanR, mzR);
      }
    }

    // (4) Join Tolerable Centers
    logger.info("Joining tolerable maxima");
    int criticScans = Math.max(1, tolScans / 2);
    int joins = 0;
    for (i = 0; i < spots.size() - 1; i++) {
      SpotByProbes s1 = spots.get(i);
      if (s1.center != null && s1.size() > 0) {
        if (canceled)
          return null;
        for (j = i; j > 0 && j < spots.size() && spots.get(j - 1).center != null
            && spots.get(j - 1).center.mzCenter + mzTol > s1.center.mzCenter; j--);
        for (; j < spots.size(); j++) {
          SpotByProbes s2 = spots.get(j);
          if (i != j && s2.center != null) {
            if (s2.center.mzCenter - s1.center.mzCenter > mzTol)
              break;
            int l = Math.min(Math.abs(s1.minScan - s2.minScan), Math.abs(s1.minScan - s2.maxScan));
            int r = Math.min(Math.abs(s1.maxScan - s2.minScan), Math.abs(s1.maxScan - s2.maxScan));
            int d = Math.min(l, r);
            boolean overlap = !(s2.maxScan < s1.minScan || s2.minScan > s1.maxScan);
            if ((d <= criticScans || overlap) && (intensityRatio(s1.center.intensityCenter,
                s2.center.intensityCenter) > intensitySimilarity)) {
              if (debug > 2)
                logger.debug("Joining s1 id " + s1.spotId + "=" + s1.center.mzCenter + " mz ["
                    + s1.minMZ + " ~ " + s1.maxMZ + "] time=" + retentiontime[s1.center.scanCenter]
                    + " int=" + s1.center.intensityCenter + " with s2 id " + s2.spotId + "="
                    + s2.center.mzCenter + " mz [" + s2.minMZ + " ~ " + s2.maxMZ + "] time="
                    + retentiontime[s2.center.scanCenter] + " int=" + s2.center.intensityCenter);
              assignSpotIdToDatumsFromSpotId(s1, s2, scanR, mzR);
              s1.addProbesFromSpot(s2, true);
              j = i; // restart
              joins++;
            }
            // }
          }
        }
      }
    }
    logger.info("Joins:" + joins);

    // (5) Remove "Large" spanned masses
    logger.info("Removing long and comparable 'masses'");
    for (i = 0; i < spots.size() - 1; i++) {
      SpotByProbes s1 = spots.get(i);
      if (s1.center != null && s1.size() > 0) {
        if (canceled)
          return null;
        int totalScans = s1.maxScan - s1.minScan + 1;
        int lScan = s1.minScan;
        int rScan = s1.maxScan;
        ArrayList<Integer> toRemove = new ArrayList<Integer>();
        toRemove.add(i);
        for (j = i; j > 0 && j < spots.size() && spots.get(j - 1).center != null
            && spots.get(j - 1).center.mzCenter + mzTol > s1.center.mzCenter; j--);
        for (; j < spots.size(); j++) {
          SpotByProbes s2 = spots.get(j);
          if (i != j && s2.center != null) {
            if (s2.center.mzCenter - s1.center.mzCenter > mzTol)
              break;
            if (intensityRatio(s1.center.intensityCenter,
                s2.center.intensityCenter) > intensitySimilarity) {
              int dl = Math.min(Math.abs(lScan - s2.minScan), Math.abs(lScan - s2.maxScan));
              int dr = Math.min(Math.abs(rScan - s2.minScan), Math.abs(rScan - s2.maxScan));
              int md = Math.min(dl, dr);
              if (md <= maxTolScans || !(s2.maxScan < lScan || s2.minScan > rScan)) {
                // distancia tolerable o intersectan
                totalScans += s2.maxScan - s2.minScan + 1;
                toRemove.add(j);
                lScan = Math.min(lScan, s2.minScan);
                rScan = Math.max(rScan, s2.maxScan);
              }
            }
          }
        }
        if (totalScans * rtPerScan > maximumTimeSpan) {
          if (debug > 2)
            logger.debug("Removing " + toRemove.size() + " masses around " + s1.center.mzCenter
                + " m/z (" + s1.spotId + "), time " + retentiontime[s1.center.scanCenter]
                + ", intensity " + s1.center.intensityCenter + ", Total Scans=" + totalScans + " ("
                + Math.round(totalScans * rtPerScan * 1000.0) / 1000.0 + " min).");
          for (Integer J : toRemove) {
            // logger.debug("Removing: "+spots.get(J).spotId);
            spots.get(J).clear();
          }
        }
      }
    }

    // Build peaks from assigned datums
    logger.info("Building peak rows (tolereance scans=" + tolScans + ")");
    i = 0;
    for (SpotByProbes sx : spots) {
      if (sx.size() > 0 && sx.maxScan - sx.minScan + 1 >= tolScans) {
        if (canceled)
          return null;
        sx.buildMaxDatumFromScans(roi, minimumHeight);
        if (sx.getMaxDatumScans() >= tolScans && (sx.getContigousMaxDatumScans() >= tolScans
            || sx.getContigousToMaxDatumScansRatio() > 0.5)) {
          SimpleChromatogram peak = new SimpleChromatogram();
          peak.setRawDataFile(rawDataFile);
          if (addMaxDatumFromScans(sx, peak) > 0) {
            float area = ChromatogramUtil.getArea(peak.getRetentionTimes(),
                peak.getIntensityValues(), peak.getNumberOfDataPoints());
            if (area > 1e-6) {
              SimpleFeatureTableRow newRow = new SimpleFeatureTableRow(newPeakList);

              SimpleFeature newFeature = new SimpleFeature();
              newFeature.setArea(area);
              newFeature.setChromatogram(peak);
              float height = ChromatogramUtil.getMaxHeight(peak.getIntensityValues(),
                  peak.getNumberOfDataPoints());
              newFeature.setHeight(height);

              newRow.setFeature(newSample, newFeature);
              newPeakList.addRow(newRow);
              if (debug > 0)
                logger.debug("Peak added id=" + sx.spotId + " " + sx.center.mzCenter + " mz, time="
                    + retentiontime[sx.center.scanCenter] + ", intensity="
                    + sx.center.intensityCenter + ", probes=" + sx.size() + ", data scans="
                    + sx.getMaxDatumScans() + ", cont scans=" + sx.getContigousMaxDatumScans()
                    + ", cont ratio=" + sx.getContigousToMaxDatumScansRatio());
              if (debug > 1) {
                // Peak info:
                logger.debug(sx.toString());
                sx.printDebugInfo();
              }
            } else {
              if (debug > 0)
                logger.debug("Ignored by area ~ 0 id=" + sx.spotId + " " + sx.center.mzCenter
                    + " mz, time=" + retentiontime[sx.center.scanCenter] + ", intensity="
                    + sx.center.intensityCenter + ", probes=" + sx.size() + ", data scans="
                    + sx.getMaxDatumScans() + ", cont scans=" + sx.getContigousMaxDatumScans()
                    + ", cont ratio=" + sx.getContigousToMaxDatumScansRatio());
            }
          }
        } else {
          if (debug > 0)
            logger.debug("Ignored by continous criteria: id=" + sx.spotId + " " + sx.center.mzCenter
                + " mz, time=" + retentiontime[sx.center.scanCenter] + ", intensity="
                + sx.center.intensityCenter + ", probes=" + sx.size() + ", data scans="
                + sx.getMaxDatumScans() + ", cont scans=" + sx.getContigousMaxDatumScans()
                + ", cont ratio=" + sx.getContigousToMaxDatumScansRatio());
        }
      } else {
        if (sx.size() > 0) {
          if (debug > 0)
            logger
                .debug("Ignored by time range criteria: id=" + sx.spotId + " " + sx.center.mzCenter
                    + " mz, time=" + retentiontime[sx.center.scanCenter] + ", intensity="
                    + sx.center.intensityCenter + ", probes=" + sx.size() + ", data scans="
                    + sx.getMaxDatumScans() + ", cont scans=" + sx.getContigousMaxDatumScans()
                    + ", cont ratio=" + sx.getContigousToMaxDatumScansRatio());
        }
      }
    }
    logger.info("Detected " + newPeakList.getRows().size() + " peaks");

    return newPeakList;
  }

  double intensityRatio(double int1, double int2) {
    return Math.min(int1, int2) / Math.max(int1, int2);
  }

  IndexedDataPoint[][] smoothDataPoints(RawDataFile dataFile, double timeSpan, double timeMZSpan,
      int scanSpan, double mzTol, int mzPoints, double minimumHeight, List<MsScan> scans) {
    int totalScans = scans.size();
    DataPoint mzValues[][] = null; // [relative scan][j value]
    DataPoint mzValuesJ[] = null;
    int mzValuesMZidx[] = null;
    IndexedDataPoint newMZValues[][] = null;
    IndexedDataPoint tmpDP[] = new IndexedDataPoint[0];
    newMZValues = new IndexedDataPoint[totalScans][];
    int i, j, si, sj, ii, k, ssi, ssj, m;
    double timeSmoothingMZtol = Math.max(timeMZSpan, 1e-6);

    int modts = Math.max(1, totalScans / 10);

    for (i = 0; i < totalScans; i++) {

      if (canceled)
        return null;

      // Smoothing in TIME space
      MsScan scan = scans.get(i);
      double rt = retentiontime[i];
      DataPoint[] xDP = null;
      IndexedDataPoint[] iDP = null;
      sj = si = i;
      ssi = ssj = i;
      int t = 0;
      if (timeSpan > 0 || scanSpan > 0) {
        if (scan != null) {
          for (si = i; si > 1; si--) {
            if (retentiontime[si - 1] < rt - timeSpan / 2) {
              break;
            }
          }
          for (sj = i; sj < totalScans - 1; sj++) {
            if (retentiontime[sj + 1] >= rt + timeSpan / 2) {
              break;
            }
          }
          ssi = i - (scanSpan - 1) / 2;
          ssj = i + (scanSpan - 1) / 2;
          if (ssi < 0) {
            ssj += -ssi;
            ssi = 0;
          }
          if (ssj >= totalScans) {
            ssi -= (ssj - totalScans + 1);
            ssj = totalScans - 1;
          }
          if (sj - si + 1 < scanSpan) {
            si = ssi;
            sj = ssj;
          }
        }
        if (scan != null && sj > si) {
          // Allocate
          if (mzValues == null || mzValues.length < sj - si + 1) {
            mzValues = new DataPoint[sj - si + 1][];
            mzValuesMZidx = new int[sj - si + 1];
          }
          // Load Data Points
          for (j = si; j <= sj; j++) {
            int jsi = j - si;
            if (mzValues[jsi] == null) {
              // if (mzValues[jsi] == null || jsi >= mzValuesScan.length - 1 || mzValuesScan[jsi +
              // 1] != scanNumbers[j]) {
              MsScan xscan = scans.get(j);
              mzValues[jsi] = getCachedDataPoints(xscan);
            } else {
              mzValues[jsi] = mzValues[jsi + 1];
              // mzValuesScan[jsi] = mzValuesScan[jsi + 1];
            }
            mzValuesMZidx[jsi] = 0;
          }
          // Estimate Averages
          ii = i - si;
          if (tmpDP.length < mzValues[ii].length)
            tmpDP = new IndexedDataPoint[mzValues[ii].length * 3 / 2];
          for (k = 0; k < mzValues[ii].length; k++) {
            DataPoint dp = mzValues[ii][k];
            double mz = dp.getMZ();
            double intensidad = 0;
            if (dp.getIntensity() > 0) { // only process those > 0
              double a = 0;
              short c = 0;
              int f = 0;
              for (j = 0; j <= sj - si; j++) {
                for (mzValuesJ = mzValues[j]; mzValuesMZidx[j] < mzValuesJ.length - 1
                    && mzValuesJ[mzValuesMZidx[j] + 1].getMZ() < mz
                        - timeSmoothingMZtol; mzValuesMZidx[j]++);

                f = mzValuesMZidx[j];

                for (m = mzValuesMZidx[j] + 1; m < mzValuesJ.length
                    && mzValuesJ[m].getMZ() < mz + timeSmoothingMZtol; m++) {
                  if (Math.abs(mzValuesJ[m].getMZ() - mz) < Math.abs(mzValuesJ[f].getMZ() - mz)) {
                    f = m;
                  } else {
                    // siempre debe ser mas cercano porque
                    // están ordenados por masa, entonces
                    // parar la búsqueda
                    break;
                  }
                }
                if (f > 0 && f < mzValuesJ.length
                    && Math.abs(mzValuesJ[f].getMZ() - mz) <= timeSmoothingMZtol
                    && mzValuesJ[f].getIntensity() > 0) { // >=
                  // minimumHeight
                  // ?
                  // logger.debug("mz="+mz+"; Closer="+mzValuesJ[f].getMZ()+", f="+f+",
                  // Intensity="+mzValuesJ[f].getIntensity());
                  a += mzValuesJ[f].getIntensity();
                  c++;
                }
              }
              intensidad = c > 0 ? a / c : 0;
              if (intensidad >= minimumHeight) {
                tmpDP[t++] = new IndexedDataPoint(k, new DataPoint(mz, intensidad));
              }
            }
          }

        }
      } else if (scan != null) {
        xDP = getCachedDataPoints(scan);
        if (tmpDP.length < xDP.length)
          tmpDP = new IndexedDataPoint[xDP.length];
        for (k = 0; k < xDP.length; k++) {
          if (xDP[k].getIntensity() >= minimumHeight) {
            tmpDP[t++] = new IndexedDataPoint(k, xDP[k]);
          }
        }
      }
      iDP = new IndexedDataPoint[t];
      for (k = 0; k < t; k++) {
        iDP[k] = tmpDP[k];
      }
      newMZValues[i] = iDP;

      if (i % modts == 0) {
        logger.info("Smoothing/Caching " + dataFile + "..." + (i / modts) * 10 + "%");
      }

    }

    return newMZValues;
  }

  double HWHM(double x0, double x1, double y0, double y1) {
    // x0 is the "scan" or m/z estimated at the highest peak
    // y0 is the "highest" peak intensity
    // x1 is the "scan" or m/z estimated at the point closest to (highest
    // intensity / 2)
    // y1 is the intensity closest to (highest intensity / 2)
    // x1_2 = x0 - F * (x0-x1)
    // F = (y0 - y0/2) / (y0 - y1) = y0/2 / (y0 - y1)
    // x1_2 = x0(1-F) + x1*F /// this X1/2 is given a triangle, so using the
    // same slope than between x0 and x1, this x1_2 is farther than the
    // actual 1/2 point
    // x3_4 = (x1_2 - x1) / 2 + x1 // aproximation to real X 1/2 by the half
    // of the difference between the x1 and the estimated x1_2
    double f = (y0 / 2) / (y0 - y1);
    if (Double.isInfinite(f))
      f = 1;
    double x3_4 = (x1 * (1 + f) / 2) - (x0 * (1 + f) / 2);
    return Math.abs(x3_4);
  }

  int addMaxDatumFromScans(SpotByProbes s, SimpleChromatogram peak) {

    int i, j;
    int adds = 0;
    for (i = s.minScan; i <= s.maxScan; i++) {
      Datum[] di = roi[i];
      if (di != null && di.length > 0) {
        Datum max = new Datum(new DataPoint(0, -1), 0, new DataPoint(0, -1));
        int idx = findFirstMass(s.minMZ, di);
        for (j = idx; j < di.length && di[j].mz <= s.maxMZ; j++) {
          Datum d = di[j];
          if (d.spotId == s.spotId) {
            if (d.intensity > max.intensity && d.mz >= s.minMZ && d.intensity > minimumHeight) {
              max = d;
            }
          }
        }
        if (max.intensity > 0) {
          adds++;
          peak.addDataPoint(scans.get(i).getRetentionTime(), max.mzOriginal,
              new Float(max.intensityOriginal));
        }
      }
    }
    return adds;
  }

  void assignSpotIdToDatumsFromScans(SpotByProbes s, int sRadius, double mzRadius) {

    int i, j;
    for (i = s.minScan; i <= s.maxScan; i++) {
      Datum[] di = roi[i];
      if (di != null && di.length > 0) {
        int idx = findFirstMass(s.minMZ - mzRadius, di);
        for (j = idx; j < di.length && di[j].mz <= s.maxMZ + mzRadius; j++) {
          Datum d = di[j];
          if (d.mz >= s.minMZ - mzRadius) {
            if (d.spotId != 0) {
              // Some spot already assigned this to it. Check
              // exactly who is the winner
              Probe p = new Probe(d.mz, d.scan);
              moveProbeToCenter(p, sRadius, mzRadius);
              if (p.mzCenter == s.center.mzCenter && p.scanCenter == s.center.scanCenter) {
                // This datum is actually MINE (s) !!!, this
                // will happen to datums close to spot borders
                // and that compete with other spot
                // logger.debug("Reassigning spot to Id="+s.spotId+" from
                // Spot:"+d.toString());
                s.setSpotIdToDatum(d);
              }
            } else {
              s.setSpotIdToDatum(d);
            }
          }
        }
      }
    }
  }

  void assignSpotIdToDatumsFromSpotId(SpotByProbes s, SpotByProbes s2, int sRadius,
      double mzRadius) {

    int i, j;
    int oldSpotId = s2.spotId;
    int mxScan = Math.max(s.maxScan, s2.maxScan);
    double minMZ = Math.min(s.minMZ, s2.minMZ);
    double maxMZ = Math.max(s.maxMZ, s2.maxMZ);
    for (i = Math.min(s.minScan, s2.minScan); i <= mxScan; i++) {
      Datum[] di = roi[i];
      if (di != null && di.length > 0) {
        int idx = findFirstMass(minMZ - mzRadius, di);
        for (j = idx; j < di.length && di[j].mz <= maxMZ + mzRadius; j++) {
          Datum d = di[j];
          if (d.spotId == oldSpotId) {
            s.setSpotIdToDatum(d);
          }
        }
      }
    }
  }

  void moveProbeToCenter(Probe p, int sRadius, double mzRadius) {

    int i, j, k;
    double maxMZ, minMZ;
    boolean move = true;
    Datum max = new Datum(new DataPoint(0, -1), 0, new DataPoint(0, -1));
    while (move) {
      k = Math.min(totalScans - 1, p.scanCenter + sRadius);
      for (i = Math.max(p.scanCenter - sRadius, 0); i <= k; i++) {
        Datum[] di = roi[i];
        if (di != null && di.length > 0) {
          minMZ = p.mzCenter - mzRadius;
          int idx = findFirstMass(minMZ, di);
          maxMZ = p.mzCenter + mzRadius;
          for (j = idx; j < di.length && di[j].mz <= maxMZ; j++) {
            Datum d = di[j];
            if (d.intensity > max.intensity && d.mz >= minMZ) {
              max = d;
            }
          }
        }
      }
      if (max.intensity >= 0 && (max.mz != p.mzCenter || max.scan != p.scanCenter)) {
        p.mzCenter = max.mz;
        p.scanCenter = max.scan;
        p.intensityCenter = max.intensity;
        // p.moves++;
      } else {
        move = false;
      }
    }
  }

  double intensityForMZorScan(ArrayList<DatumExpand> deA, double mz, int scan) {
    double h = -1;
    int j;
    for (j = 0; j < deA.size(); j++) {
      DatumExpand de = deA.get(j);
      if ((de.dato.scan == scan || de.dato.mz == mz) && de.dato.intensity > h) {
        h = de.dato.intensity;
      }
    }
    return h;
  }

  double[] massCenter(int l, int r, double min, double max) {
    double x = 0;
    double y = 0;
    double sum = 0;
    double maxValue = 0;
    for (int i = l; i <= r; i++) {
      DataPoint mzs[] = getCachedDataPoints(scans.get(i));
      if (mzs != null) {
        for (int j = findFirstMass(min, mzs); j < mzs.length; j++) {
          double mass = mzs[j].getMZ();
          if (mass >= min) {
            if (mass <= max) {
              double intensity = mzs[j].getIntensity();
              if (intensity >= minimumHeight) {
                x += i * intensity;
                y += mass * intensity;
                sum += intensity;
                if (intensity > maxValue) {
                  maxValue = intensity;
                }
              }
            } else {
              break;
            }
          }
        }
      }
    }
    if (sum > 0.0) {
      x /= sum;
      y /= sum;
    }
    return new double[] {x, y, maxValue};
  }

  static int findFirstMass(double mass, DataPoint mzValues[]) {
    int l = 0;
    int r = mzValues.length - 1;
    int mid = 0;
    while (l < r) {
      mid = (r + l) / 2;
      if (mzValues[mid].getMZ() > mass) {
        r = mid - 1;
      } else if (mzValues[mid].getMZ() < mass) {
        l = mid + 1;
      } else {
        return mid;
      }
    }
    while (l > 0 && mzValues[l].getMZ() > mass)
      l--;
    return l;
  }

  static int findFirstMass(double mass, Datum mzValues[]) {
    return findFirstMass(mass, mzValues, 0, mzValues.length - 1);
  }

  static int findFirstMass(double mass, Datum mzValues[], int l, int r) {
    int mid = 0;
    while (l < r) {
      mid = (r + l) / 2;
      if (mzValues[mid].mz > mass) {
        r = mid - 1;
      } else if (mzValues[mid].mz < mass) {
        l = mid + 1;
      } else {
        return mid;
      }
    }
    while (l > 0 && mzValues[l].mz > mass)
      l--;
    return l;
  }

  Spot intensities(int l, int r, double min, double max, SimpleChromatogram chr,
      PearsonCorrelation stats, int spotId) {
    boolean passSpot = false;
    Spot s = new Spot();
    if (r >= scans.size())
      r = scans.size() - 1;
    if (l < 0)
      l = 0;
    for (int i = l; i <= r; i++) {
      Datum mzs[] = roi[i];
      if (mzs != null) {
        Datum mzMax = null;
        for (int j = findFirstMass(min, mzs); j < mzs.length; j++) {
          double mass = mzs[j].mz;
          double mjint = mzs[j].intensity;
          if (mass >= min) {
            if (mass <= max) {
              if (mzs[j].spotId == spotId || passSpot) {
                s.addPoint(i, mass, (mjint >= minimumHeight ? mjint : -mjint));
                if (mjint >= minimumHeight) {
                  if (mzMax == null || mjint > mzMax.intensity) {
                    mzMax = mzs[j];
                  }
                }
              } else {
                if (mjint >= minimumHeight)
                  s.pointsNoSpot++;
              }
            } else {
              break;
            }
          }
        }
        if (chr != null && mzMax != null) {
          // Add ONLY THE MAX INTENSITY PER SCAN
          chr.addDataPoint(scans.get(i).getRetentionTime(), mzMax.mz, new Float(mzMax.intensity));
        }
        if (stats != null && mzMax != null) {
          stats.enter(i, mzMax.mz);
        }
      }
    }
    return s;
  }

  DataPoint[] getCachedDataPoints(MsScan scan) {
    if (dpCache == null)
      dpCache = new HashMap<>();
    DataPoint[] dp = dpCache.get(scan);
    if (dp != null) {
      return dp;
    }
    dp = new DataPoint[scan.getNumberOfDataPoints()];
    double mzValues[] = scan.getMzValues();
    float intensityValues[] = scan.getIntensityValues();
    for (int i = 0; i < scan.getNumberOfDataPoints(); i++) {
      dp[i] = new DataPoint(mzValues[i], intensityValues[i]);
    }
    dpCache.put(scan, dp);
    return dp;
  }

  /** {@inheritDoc} */
  @Override
  @Nullable
  public Float getFinishedPercentage() {
    return totalScans == 0 ? null : (float) processedScans / totalScans;
  }

  /** {@inheritDoc} */
  @Override
  @Nullable
  public FeatureTable getResult() {
    return newPeakList;
  }

  /** {@inheritDoc} */
  @Override
  public void cancel() {
    canceled = true;
  }

}
