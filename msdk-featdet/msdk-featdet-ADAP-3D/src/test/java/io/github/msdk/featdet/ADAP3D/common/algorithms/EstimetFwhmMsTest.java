package io.github.msdk.featdet.ADAP3D.common.algorithms;

import java.io.File;

import javax.annotation.Nonnull;

import org.junit.Assert;
import org.junit.BeforeClass;
import org.junit.Test;

import io.github.msdk.MSDKException;
import io.github.msdk.datamodel.rawdata.RawDataFile;
import io.github.msdk.io.netcdf.NetCDFFileImportMethod;
import io.github.msdk.datamodel.datastore.DataPointStore;
import io.github.msdk.datamodel.datastore.DataPointStoreFactory;

public class EstimetFwhmMsTest {
	
	private static final String TEST_DATA_PATH = "src/test/resources/";
	private static RawDataFile rawFile;
	
	@Test
	public void loadData() throws MSDKException {
		
	    // Import the file
	    File inputFile = new File(TEST_DATA_PATH + "test_out.cdf");
	    Assert.assertTrue("Cannot read test data", inputFile.canRead());
	    DataPointStore dataStore = DataPointStoreFactory.getTmpFileDataStore();
	    NetCDFFileImportMethod importer = new NetCDFFileImportMethod(inputFile,dataStore);
	    rawFile = importer.execute();
	  }

}
