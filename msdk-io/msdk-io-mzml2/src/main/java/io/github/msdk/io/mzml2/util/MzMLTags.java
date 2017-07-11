/*
 * (C) Copyright 2015-2016 by MSDK Development Team
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

package io.github.msdk.io.mzml2.util;

public abstract class MzMLTags {

  public static final String TAG_INDEXED_MZML = "indexedmzML";
  public static final String TAG_MZML = "mzML";
  public static final String TAG_CV_LIST = "cvList";
  public static final String TAG_DATA_PROCESSING_LIST = "dataProcessingList";
  public static final String TAG_DATA_PROCESSING = "dataProcessing";
  public static final String TAG_PROCESSING_METHOD = "processingMethod";
  public static final String TAG_RUN = "run";
  public static final String TAG_SPECTRUM_LIST = "spectrumList";
  public static final String TAG_SPECTRUM = "spectrum";
  public static final String TAG_CV_PARAM = "cvParam";
  public static final String TAG_SCAN_LIST = "scanList";
  public static final String TAG_SCAN = "scan";
  public static final String TAG_SCAN_WINDOW_LIST = "scanWindowList";
  public static final String TAG_SCAN_WINDOW = "scanWindow";
  public static final String TAG_BINARY_DATA_ARRAY_LIST = "binaryDataArrayList";
  public static final String TAG_BINARY_DATA_ARRAY = "binaryDataArray";
  public static final String TAG_BINARY = "binary";
  public static final String TAG_CHROMATOGRAM_LIST = "chromatogramList";
  public static final String TAG_CHROMATOGRAM = "chromatogram";
  public static final String TAG_PRECURSOR = "precursor";
  public static final String TAG_ISOLATION_WINDOW = "isolationWindow";
  public static final String TAG_ACTIVATION = "activation";
  public static final String TAG_PRODUCT = "product";
  public final static String TAG_REF_PARAM_GROUP = "referenceableParamGroup";
  public final static String TAG_REF_PARAM_GROUP_REF = "referenceableParamGroupRef";
  public final static String TAG_REF_PARAM_GROUP_LIST = "referenceableParamGroupList";
  public final static String TAG_PRECURSOR_LIST = "precursorList";
  public final static String TAG_SELECTED_ION_LIST = "selectedIonList";
  public final static String TAG_SELECTED_ION = "selectedIon";
  public final static String TAG_INDEX_LIST = "indexList";
  public final static String TAG_INDEX = "index";
  public final static String TAG_OFFSET = "offset";
  public final static String TAG_INDEX_LIST_OFFSET = "indexListOffset";
  public final static String TAG_FILE_CHECKSUM = "fileChecksum";

  public static final String ATTR_XSI = "xsi";
  public static final String ATTR_SCHEME_LOCATION = "schemeLocation";
  public static final String ATTR_ID = "id";
  public static final String ATTR_VERSION = "version";
  public static final String ATTR_COUNT = "count";
  public static final String ATTR_SOFTWARE_REF = "softwareRef";
  public static final String ATTR_ORDER = "order";
  public static final String ATTR_DEFAULT_ARRAY_LENGTH = "defaultArrayLength";
  public static final String ATTR_INDEX = "index";
  public static final String ATTR_CV_REF = "cvRef";
  public static final String ATTR_NAME = "name";
  public static final String ATTR_ACCESSION = "accession";
  public static final String ATTR_VALUE = "value";
  public static final String ATTR_UNIT_ACCESSION = "unitAccession";
  public static final String ATTR_ENCODED_LENGTH = "encodedLength";
  public static final String ATTR_ID_REF = "idRef";

}
