/* 
 * (C) Copyright 2015 by MSDK Development Team
 *
 * This software is dual-licensed under either
 *
 * (a) the terms of the GNU Lesser General Public License version 2.1
 * as published by the Free Software Foundation
 *
 * or (per the licensee's choosing)
 *
 * (b) the terms of the Eclipse Public License v1.0 as published by
 * the Eclipse Foundation.
 */
package io.github.msdk.filtering.scanfilters;

import io.github.msdk.MSDKException;
import io.github.msdk.MSDKMethod;
import io.github.msdk.datamodel.datapointstore.DataPointStore;
import io.github.msdk.datamodel.impl.MSDKObjectBuilder;
import io.github.msdk.datamodel.msspectra.MsIon;
import io.github.msdk.datamodel.msspectra.MsSpectrumDataPointList;
import io.github.msdk.datamodel.rawdata.MsScan;
import com.google.common.collect.Lists;
import java.util.ArrayList;
import java.util.Iterator;
import java.util.List;
import java.util.Map.Entry;
import javax.annotation.Nonnull;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

public class MeanFilterMethod implements MSDKMethod<MsScan> {

    private final Logger logger = LoggerFactory.getLogger(this.getClass());

    private final @Nonnull
    MsScan scan;
    private final @Nonnull
    double windowLength;
    private final @Nonnull
    DataPointStore store;

    private int processedDataPoints = 0, totalDataPoints = 0;
    private MsScan result;
    private boolean canceled = false;

    public MeanFilterMethod(@Nonnull MsScan scan, @Nonnull double windowLength, @Nonnull DataPointStore store) {
        this.scan = scan;
        this.windowLength = windowLength;
        this.store = store;
    }

    @Override
    public Float getFinishedPercentage() {
        if (totalDataPoints == 0) {
            return null;
        } else {
            return (float) processedDataPoints / totalDataPoints;
        }
    }

    @Override
    public MsScan execute() throws MSDKException {
        logger.info("Started Mean Filter with Scan number #"
            + scan.getScanNumber());

        List<Double> massWindow = new ArrayList();
        List<Float> intensityWindow = new ArrayList();

        double currentMass;
        double lowLimit;
        double hiLimit;
        double mzVal;
        float elSum;
        int addi = 0;

        // Create dataPoint list object and fill it with the scan data points
        MsSpectrumDataPointList dataPoints = MSDKObjectBuilder.getMsSpectrumDataPointList();
        scan.getDataPoints(dataPoints);

        List<Ion> ions = new ArrayList();

        for (MsIon ion : dataPoints) {
            ions.add(new Ion(ion.getMz(), ion.getIntensity()));
        }

        totalDataPoints = ions.size();
        dataPoints.clear();
        // For each data point
        for (int i = 0; i < ions.size(); i++) {
            currentMass = ions.get(i).mz();
            lowLimit = currentMass - windowLength;
            hiLimit = currentMass + windowLength;

            // Remove all elements from window whose m/z value is less than the
            // low limit
            if (massWindow.size() > 0) {
                mzVal = massWindow.get(0);
                while ((massWindow.size() > 0) && (mzVal < lowLimit)) {
                    massWindow.remove(0);
                    intensityWindow.remove(0);
                    if (massWindow.size() > 0) {
                        mzVal = massWindow.get(0);
                    }
                }
            }

            // Add new elements as long as their m/z values are less than the hi
            // limit
            // Add new elements as long as their m/z values are less than the hi
            // limit
            while ((addi < ions.size())
                && (ions.get(addi).mz() <= hiLimit)) {
                massWindow.add(ions.get(addi).mz());
                intensityWindow.add(ions.get(addi).intensity());
                addi++;
            }

            elSum = 0;
            for (Float intensity : intensityWindow) {
                elSum += (intensity);
            }

            dataPoints.add(currentMass, elSum / (float) intensityWindow.size());

            if (canceled) {
                return null;
            }

            processedDataPoints++;
        }

        result = MSDKObjectBuilder.getMsScan(store, scan.getScanNumber(), scan.getMsFunction());
        result.setDataPoints(dataPoints);
        result.setChromatographyInfo(scan.getChromatographyInfo());
        result.setRawDataFile(scan.getRawDataFile());

        logger.info("Finished Mean Filter with Scan number #"
            + scan.getScanNumber());
        return result;
    }

    @Override
    public MsScan getResult() {
        return result;
    }

    @Override
    public void cancel() {
        this.canceled = true;
    }

}

class Ion {

    private final Double mz;
    private final Float intensity;

    public Ion(Double mz, Float intensity) {
        this.mz = mz;
        this.intensity = intensity;
    }

    public Double mz() {
        return mz;
    }

    public Float intensity() {
        return intensity;
    }
}
