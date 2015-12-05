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
import io.github.msdk.datamodel.datapointstore.DataPointStore;
import io.github.msdk.datamodel.impl.MSDKObjectBuilder;
import io.github.msdk.datamodel.msspectra.MsSpectrumDataPointList;
import io.github.msdk.datamodel.rawdata.MsScan;
import java.util.ArrayList;
import java.util.List;
import javax.annotation.Nonnull;

public class MeanFilterAlgorithm {

    private final @Nonnull MsScan scan;
    private final @Nonnull double windowLength;
    private final @Nonnull DataPointStore store;

    private MsScan result;

    public MeanFilterAlgorithm(@Nonnull MsScan scan, @Nonnull double windowLength, @Nonnull DataPointStore store) {
        this.scan = scan;
        this.windowLength = windowLength;
        this.store = store;
    }
   
    public MsScan execute() throws MSDKException {
        List<Double> massWindow = new ArrayList();
        List<Float> intensityWindow = new ArrayList();

        double currentMass;
        double lowLimit;
        double hiLimit;
        double mzVal;
        float elSum;
        int addi = 0;

        // Create data point list object and fill it with the scan data points
        MsSpectrumDataPointList dataPoints = MSDKObjectBuilder.getMsSpectrumDataPointList();
        scan.getDataPoints(dataPoints);
        double mzValues[] = dataPoints.getMzBuffer();
        float intensityValues[] = dataPoints.getIntensityBuffer();
         

        int totalDataPoints = dataPoints.getSize();
        // Clear the data point list to fill it with the new points after filtering
        dataPoints.clear();
        
        // For each data point
        for (int i = 0; i < totalDataPoints; i++) {
            currentMass = mzValues[i];
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
            while ((addi < totalDataPoints)
                && (mzValues[addi] <= hiLimit)) {
                massWindow.add(mzValues[addi]);
                intensityWindow.add(intensityValues[addi]);
                addi++;
            }

            elSum = 0;
            for (Float intensity : intensityWindow) {
                elSum += intensity;
            }
           
            dataPoints.add(currentMass, elSum / (float) intensityWindow.size());
        }

        // Return a new scan with the new data points
        result = MSDKObjectBuilder.getMsScan(store, scan.getScanNumber(), scan.getMsFunction());
        result.setDataPoints(dataPoints);
        result.setChromatographyInfo(scan.getChromatographyInfo());
        result.setRawDataFile(scan.getRawDataFile());

        return result;
    }

    
    public MsScan getResult() {
        return result;
    }


}
