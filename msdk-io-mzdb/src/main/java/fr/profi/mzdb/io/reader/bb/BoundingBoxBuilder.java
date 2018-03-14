package fr.profi.mzdb.io.reader.bb;

import java.io.InputStream;
import java.io.StreamCorruptedException;
import java.util.Map;

import com.almworks.sqlite4java.SQLiteBlob;

import fr.profi.mzdb.model.BoundingBox;
import fr.profi.mzdb.model.DataEncoding;
import fr.profi.mzdb.model.SpectrumHeader;

/**
 * The Class BoundingBoxBuilder.
 * <p>
 * Contains static methods to build BoundingBox objects Use a different reader depending of provided data in
 * the constructor
 * </p>
 * 
 * @author David Bouyssie
 */
public class BoundingBoxBuilder {

	public static BoundingBox buildBB(
		int bbId,
		byte[] bytes,
		long firstSpectrumId,
		long lastSpectrumId,
		Map<Long, SpectrumHeader> spectrumHeaderById,
		Map<Long, DataEncoding> dataEncodingBySpectrumId
	) throws StreamCorruptedException {
		
		BoundingBox bb = new BoundingBox(bbId, new BytesReader(bytes, firstSpectrumId, lastSpectrumId, spectrumHeaderById, dataEncodingBySpectrumId));
		bb.setFirstSpectrumId(firstSpectrumId);
		bb.setLastSpectrumId(lastSpectrumId);
		
		return bb;
	}

	public static BoundingBox buildBB(
		int bbId,
		SQLiteBlob blob,
		long firstSpectrumId,
		long lastSpectrumId,
		Map<Long, SpectrumHeader> spectrumHeaderById,
		Map<Long, DataEncoding> dataEncodingBySpectrumId
	) throws StreamCorruptedException {
		
		BoundingBox bb =  new BoundingBox(bbId, new SQLiteBlobReader(blob, firstSpectrumId, lastSpectrumId, spectrumHeaderById, dataEncodingBySpectrumId) );
		bb.setFirstSpectrumId(firstSpectrumId);
		bb.setLastSpectrumId(lastSpectrumId);
		
		return bb;
	}

	public static BoundingBox buildBB(
		int bbId,
		InputStream stream,
		long firstSpectrumId,
		long lastSpectrumId,
		Map<Long, SpectrumHeader> spectrumHeaderById,
		Map<Long, DataEncoding> dataEncodingBySpectrumId
	) {
		
		BoundingBox bb = new BoundingBox(bbId, new StreamReader(stream, firstSpectrumId, lastSpectrumId, spectrumHeaderById, dataEncodingBySpectrumId) );
		bb.setFirstSpectrumId(firstSpectrumId);
		bb.setLastSpectrumId(lastSpectrumId);
		
		return bb;
	}

}
