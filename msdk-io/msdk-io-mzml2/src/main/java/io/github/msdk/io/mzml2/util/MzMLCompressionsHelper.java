package io.github.msdk.io.mzml2.util;

import java.util.EnumSet;

import io.github.msdk.io.mzml2.data.MzMLBinaryDataInfo;
import io.github.msdk.io.mzml2.data.MzMLBinaryDataInfo.MzMLCompressionType;

public abstract class MzMLCompressionsHelper {

  public static EnumSet<MzMLBinaryDataInfo.MzMLCompressionType> getCompressions(
      MzMLBinaryDataInfo.MzMLCompressionType compression) {
    EnumSet<MzMLBinaryDataInfo.MzMLCompressionType> compressions =
        EnumSet.noneOf(MzMLBinaryDataInfo.MzMLCompressionType.class);

    if (compression == MzMLCompressionType.NUMPRESS_LINPRED_ZLIB) {
      compressions.add(MzMLCompressionType.NUMPRESS_LINPRED);
      compressions.add(MzMLCompressionType.ZLIB);
    } else if (compression == MzMLCompressionType.NUMPRESS_POSINT_ZLIB) {
      compressions.add(MzMLCompressionType.NUMPRESS_POSINT);
      compressions.add(MzMLCompressionType.ZLIB);
    } else if (compression == MzMLCompressionType.NUMPRESS_SHLOGF_ZLIB) {
      compressions.add(MzMLCompressionType.NUMPRESS_SHLOGF);
      compressions.add(MzMLCompressionType.ZLIB);
    } else if (compression == null) {
      compressions.add(MzMLCompressionType.NO_COMPRESSION);
    } else {
      compressions.add(compression);
    }

    return compressions;
  }

}
