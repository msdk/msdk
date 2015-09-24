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

package io.github.msdk.search.mona;

import java.io.IOException;
import java.net.MalformedURLException;
import java.net.URL;

import javax.annotation.Nonnull;
import javax.annotation.Nullable;

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

import org.openscience.cdk.interfaces.IAtomContainer;
import org.openscience.cdk.interfaces.IMolecularFormula;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import com.fasterxml.jackson.databind.ObjectMapper;
import com.google.common.collect.Range;

import io.github.msdk.datamodel.impl.MSDKObjectBuilder;
import io.github.msdk.datamodel.ionannotations.IonAnnotation;
import io.github.msdk.datamodel.ionannotations.IonType;
import io.github.msdk.datamodel.msspectra.MsSpectrum;
import io.github.msdk.datamodel.msspectra.MsSpectrumDataPointList;
import io.github.msdk.datamodel.msspectra.MsSpectrumType;
import io.github.msdk.datamodel.rawdata.ChromatographyInfo;
import io.github.msdk.search.mona.pojo.Spectra;

/**
 * A basic MoNA record, which describes a MassBank of Northern America Spectra. This is a readonly entity and should not be modified by the software in any possible way
 */
public class MonaSpectrum implements MsSpectrum,MonaConfiguration,IonAnnotation {

    /**
     * internal MoNa ID
     */
    private Long id;

    private final Logger logger = LoggerFactory.getLogger(this.getClass());

    /**
     * unless otherwise said, MoNA spectra are always centroided
     */
    private MsSpectrumType spectrumType = MsSpectrumType.CENTROIDED;

    /**
     * storage of dataPoints
     */
    private MsSpectrumDataPointList dataPoints;

    /**
     * chemical structure of this compound
     */
    private IAtomContainer atomContainer;

    /**
     * build a new mona spectrum from the given record
     */
    public MonaSpectrum(Spectra monaRecord) {
        this.build(monaRecord);
    }

    /**
     * build a spectrum directly from an id, while accessing the mona repository
     *
     * @param id
     */
    public MonaSpectrum(long id) throws IOException {

        this.id = id;

        URL url = getAccessionURL();

        ObjectMapper mapper = new ObjectMapper();
        Spectra spectra = mapper.readValue(url.openStream(), Spectra.class);

        build(spectra);
    }

    /**
     * actual builder
     * @param monaRecord
     */
    protected void build(Spectra monaRecord){

        logger.info("received: " + monaRecord.getId());

        //convert to internal model
        for(String s : monaRecord.getSpectrum().split(" ")){
            String v[] = s.split(":");
            addDataPoint(Double.parseDouble(v[0]),Float.parseFloat(v[1]));
        }

        //assign compound information

        String molFile = monaRecord.getBiologicalCompound().getMolFile();

        //done
        logger.debug("spectra build");
    }

    /**
     * adds a datapoint internally
     * @param mass
     * @param intensity
     */
    protected void addDataPoint(Double mass, Float intensity){
        if(this.dataPoints == null){
            this.dataPoints = MSDKObjectBuilder.getMsSpectrumDataPointList();
        }
        this.dataPoints.add(mass, intensity);
    }

    @Nonnull
    @Override
    public MsSpectrumType getSpectrumType() {
        return this.spectrumType;
    }

    @Override
    public void setSpectrumType(@Nonnull MsSpectrumType spectrumType) {
        this.spectrumType = spectrumType;
    }


    @Override
    public void setDataPoints(@Nonnull MsSpectrumDataPointList newDataPoints) {
        this.dataPoints = newDataPoints;
    }


    @Nullable
    @Override
    public IAtomContainer getChemicalStructure() {
        return this.atomContainer;
    }

    @Override
    public void setChemicalStructure(@Nullable IAtomContainer structure) {
        //not supported
    }

    @Nullable
    @Override
    public IMolecularFormula getFormula() {
        return null;
    }

    @Override
    public void setFormula(@Nullable IMolecularFormula formula) {
        //not supported
    }

    @Nullable
    @Override
    public String getDescription() {
        return "Massbank of Northern America record, " + id;
    }

    @Override
    public void setDescription(@Nullable String description) {
        //not supported
    }

    @Nullable
    @Override
    public String getIdentificationMethod() {
        return null;
    }

    @Override
    public void setIdentificationMethod(@Nullable String idMethod) {
        //not supported
    }

    @Nullable
    @Override
    public String getAnnotationId() {
        return this.id.toString();
    }

    @Override
    public void setAnnotationId(@Nullable String dbId) {
        //not supported
    }

    @Nullable
    @Override
    public URL getAccessionURL(){
        try {
            return  new URL(MONA_URL+"/rest/spectra/"+id);
        } catch (MalformedURLException e) {
            throw new RuntimeException("malformed URL, should never actually happen!");
        }
    }

    @Override
    public void setAccessionURL(@Nullable URL dbURL) {
        //not supported
    }

    @Override
    public int compareTo(IonAnnotation o) {
        // TODO Auto-generated method stub
        return 0;
    }

    @Override
    @Nullable
    public IonType getIonType() {
        // TODO Auto-generated method stub
        return null;
    }

    @Override
    public void setIonType(@Nullable IonType ionType) {
        // TODO Auto-generated method stub
        
    }

    @Override
    @Nullable
    public Double getExpectedMz() {
        // TODO Auto-generated method stub
        return null;
    }

    @Override
    public void setExpectedMz(@Nullable Double expectedMz) {
        // TODO Auto-generated method stub
        
    }

    @Override
    @Nullable
    public ChromatographyInfo getChromatographyInfo() {
        // TODO Auto-generated method stub
        return null;
    }

    @Override
    public void setChromatographyInfo(
            @Nullable ChromatographyInfo chromatographyInfo) {
        // TODO Auto-generated method stub
        
    }

    @Override
    public void getDataPoints(@Nonnull MsSpectrumDataPointList dataPointList) {
        // TODO Auto-generated method stub
        
    }

    @Override
    public void getDataPointsByMzAndIntensity(
            @Nonnull MsSpectrumDataPointList dataPointList,
            @Nonnull Range<Double> mzRange,
            @Nonnull Range<Float> intensityRange) {
        // TODO Auto-generated method stub
        
    }

    @Override
    @Nonnull
    public Float getTIC() {
        // TODO Auto-generated method stub
        return 0f;
    }

    @Override
    @Nullable
    public Range<Double> getMzRange() {
        // TODO Auto-generated method stub
        return null;
    }

}
