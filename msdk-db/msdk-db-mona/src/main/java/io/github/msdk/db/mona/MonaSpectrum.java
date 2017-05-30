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

package io.github.msdk.db.mona;

import java.io.IOException;
import java.net.MalformedURLException;
import java.net.URL;

import javax.annotation.Nonnull;
import javax.annotation.Nullable;

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

import org.openscience.cdk.interfaces.IAtomContainer;
import org.openscience.cdk.interfaces.IMolecularFormula;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import com.fasterxml.jackson.databind.ObjectMapper;
import com.google.common.collect.Range;

import io.github.msdk.datamodel.ionannotations.IonAnnotation;
import io.github.msdk.datamodel.ionannotations.IonType;
import io.github.msdk.datamodel.msspectra.MsSpectrum;
import io.github.msdk.datamodel.msspectra.MsSpectrumType;
import io.github.msdk.db.mona.pojo.Spectra;
import io.github.msdk.util.tolerances.MzTolerance;

/**
 * A basic MoNA record, which describes a MassBank of Northern America Spectra. This is a readonly
 * entity and should not be modified by the software in any possible way
 */
public class MonaSpectrum implements MsSpectrum, MonaConfiguration, IonAnnotation {

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
   * chemical structure of this compound
   */
  private IAtomContainer atomContainer;

  /**
   * build a new mona spectrum from the given record
   *
   * @param monaRecord object.
   */
  public MonaSpectrum(Spectra monaRecord) {
    this.build(monaRecord);
  }

  /**
   * build a spectrum directly from an id, while accessing the mona repository
   *
   * @param id a long.
   * @throws java.io.IOException if any.
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
   *
   * @param monaRecord object.
   */
  protected void build(Spectra monaRecord) {

    logger.info("received: " + monaRecord.getId());

    // convert to internal model
    for (String s : monaRecord.getSpectrum().split(" ")) {
      String v[] = s.split(":");
      addDataPoint(Double.parseDouble(v[0]), Float.parseFloat(v[1]));
    }

    // assign compound information

    @SuppressWarnings("unused")
    String molFile = monaRecord.getBiologicalCompound().getMolFile();

    // done
    logger.debug("spectra build");
  }

  /**
   * adds a datapoint internally
   *
   * @param mass a {@link java.lang.Double} object.
   * @param intensity a {@link java.lang.Float} object.
   */
  protected void addDataPoint(Double mass, Float intensity) {

  }

  /** {@inheritDoc} */

  @Nonnull
  @Override
  public MsSpectrumType getSpectrumType() {
    return this.spectrumType;
  }


  /** {@inheritDoc} */
  @Nullable
  @Override
  public IAtomContainer getChemicalStructure() {
    return this.atomContainer;
  }



  /** {@inheritDoc} */
  @Nullable
  @Override
  public IMolecularFormula getFormula() {
    return null;
  }


  /** {@inheritDoc} */
  @Nullable
  @Override
  public String getDescription() {
    return "Massbank of Northern America record, " + id;
  }



  /** {@inheritDoc} */
  @Nullable
  @Override
  public String getIdentificationMethod() {
    return null;
  }



  /** {@inheritDoc} */
  @Nullable
  @Override
  public String getAnnotationId() {
    return this.id.toString();
  }



  /** {@inheritDoc} */
  @Nullable
  @Override
  public URL getAccessionURL() {
    try {
      return new URL(MONA_URL + "/rest/spectra/" + id);
    } catch (MalformedURLException e) {
      throw new RuntimeException("malformed URL, should never actually happen!");
    }
  }



  /** {@inheritDoc} */
  @Override
  public int compareTo(IonAnnotation o) {
    // TODO Auto-generated method stub
    return 0;
  }

  /** {@inheritDoc} */
  @Override
  @Nullable
  public IonType getIonType() {
    return null;
  }



  /** {@inheritDoc} */
  @Override
  @Nullable
  public Double getExpectedMz() {
    return null;
  }





  /** {@inheritDoc} */
  @Override
  @Nonnull
  public Integer getNumberOfDataPoints() {
    // TODO Auto-generated method stub
    return 0;
  }

  /** {@inheritDoc} */
  @Override
  @Nonnull
  public double[] getMzValues() {
    // TODO Auto-generated method stub
    return new double[0];
  }


  /** {@inheritDoc} */
  @Override
  @Nonnull
  public float[] getIntensityValues() {
    // TODO Auto-generated method stub
    return new float[0];
  }


  /** {@inheritDoc} */
  @Override
  @Nonnull
  public Float getTIC() {
    // TODO Auto-generated method stub
    return 0f;
  }

  /** {@inheritDoc} */
  @Override
  @Nullable
  public Range<Double> getMzRange() {
    // TODO Auto-generated method stub
    return null;
  }

  /** {@inheritDoc} */
  @Override
  public @Nonnull Boolean isNA() {
    return false;
  }

  /** {@inheritDoc} */
  @Override
  @Nullable
  public String getInchiKey() {
    // TODO Auto-generated method stub
    return null;
  }



  /** {@inheritDoc} */
  @Override
  @Nullable
  public Integer getTaxId() {
    // TODO Auto-generated method stub
    return null;
  }



  /** {@inheritDoc} */
  @Override
  @Nullable
  public String getSpecies() {
    // TODO Auto-generated method stub
    return null;
  }



  /** {@inheritDoc} */
  @Override
  @Nullable
  public String getDatabase() {
    // TODO Auto-generated method stub
    return null;
  }



  /** {@inheritDoc} */
  @Override
  @Nullable
  public String getDatabaseVersion() {
    // TODO Auto-generated method stub
    return null;
  }



  /** {@inheritDoc} */
  @Override
  @Nullable
  public String getSpectraRef() {
    // TODO Auto-generated method stub
    return null;
  }



  /** {@inheritDoc} */
  @Override
  @Nullable
  public String getSearchEngine() {
    // TODO Auto-generated method stub
    return null;
  }


  /** {@inheritDoc} */
  @Override
  @Nullable
  public Double getBestSearchEngineScore() {
    // TODO Auto-generated method stub
    return null;
  }


  /** {@inheritDoc} */
  @Override
  @Nullable
  public String getModifications() {
    // TODO Auto-generated method stub
    return null;
  }


  /** {@inheritDoc} */
  @Override
  @Nullable
  public Integer getReliability() {
    // TODO Auto-generated method stub
    return null;
  }


  /** {@inheritDoc} */
  @Override
  @Nullable
  public MzTolerance getMzTolerance() {
    // TODO Auto-generated method stub
    return null;
  }

  @Override
  public Float getExpectedRetentionTime() {
    // TODO Auto-generated method stub
    return null;
  }


}
