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
import java.util.ArrayList;
import java.util.Iterator;
import java.util.List;
import javax.annotation.Nonnull;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

public class MeanFilterMethod implements MSDKMethod<MsScan> {

    private final Logger logger = LoggerFactory.getLogger(this.getClass());

    private final @Nonnull MsScan scan;
    private final @Nonnull double windowLength;
    private final @Nonnull DataPointStore store;
    
    private MsScan result;
    private boolean canceled = false;
    
    public MeanFilterMethod(@Nonnull MsScan scan, @Nonnull double windowLength, @Nonnull DataPointStore store) {
        this.scan = scan;
        this.windowLength = windowLength;
        this.store = store;
    }

    @Override
    public Float getFinishedPercentage() {
        throw new UnsupportedOperationException("Not supported yet."); //To change body of generated methods, choose Tools | Templates.
    }

    @Override
    public MsScan execute() throws MSDKException {
        List<Double> massWindow = new ArrayList();
        List<Float> intensityWindow = new ArrayList();

        double currentMass;
        double lowLimit;
        double hiLimit;
        double mzVal;
        float elSum;

        MsSpectrumDataPointList dataPoints = MSDKObjectBuilder.getMsSpectrumDataPointList();
        MsSpectrumDataPointList newDataPoints = MSDKObjectBuilder.getMsSpectrumDataPointList();

        scan.getDataPoints(dataPoints);

        int addi = 0;
        Iterator iterator = dataPoints.iterator();
        List<MsIon> ionList = new ArrayList();
        while (iterator.hasNext()) {
            ionList.add((MsIon) iterator.next());
        }
        
        for (int i = 0; i < ionList.size(); i++) {
            currentMass = ionList.get(i).getMz();
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
            while ((addi < dataPoints.getSize())
                    && (ionList.get(addi).getMz() <= hiLimit)) {
                massWindow.add(ionList.get(addi).getMz());
                intensityWindow.add(ionList.get(addi).getIntensity());
                addi++;
            }

            elSum = 0;
            for (Float intensity : intensityWindow) {
                elSum += (intensity);
            }           
            newDataPoints.add(currentMass, elSum /(float) intensityWindow.size());
            
            if (canceled) {
                return null;
            }        
        }
        
        MsScan newScan = MSDKObjectBuilder.getMsScan(store, scan.getScanNumber(), scan.getMsFunction());
        newScan.setDataPoints(newDataPoints);
        result = newScan;
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
