package io.github.msdk.featdet.ADAP3D.common.algorithms;

import java.io.File;

import org.junit.Assert;
import org.junit.BeforeClass;
import org.junit.Test;

import io.github.msdk.MSDKException;
import io.github.msdk.datamodel.rawdata.RawDataFile;
import io.github.msdk.io.netcdf.NetCDFFileImportMethod;
import io.github.msdk.datamodel.datastore.DataPointStore;
import io.github.msdk.datamodel.datastore.DataPointStoreFactory;

public class EstimateFwhmMsTest {
	
	private static final String TEST_DATA_PATH = "src/test/resources/";
	private static RawDataFile rawFile;
	private static CurveTool objCurveTool;
	
	@BeforeClass
	public static void loadData() throws MSDKException {
		
	    // Import the file
	    File inputFile = new File(TEST_DATA_PATH + "test_output.cdf");
	    Assert.assertTrue("Cannot read test data", inputFile.canRead());
	    DataPointStore dataStore = DataPointStoreFactory.getTmpFileDataStore();
	    NetCDFFileImportMethod importer = new NetCDFFileImportMethod(inputFile,dataStore);
	    rawFile = importer.execute();
	    SliceSparseMatrix objSliceSparseMatrix = new SliceSparseMatrix(rawFile);
	    objCurveTool = new CurveTool(objSliceSparseMatrix);
	    Assert.assertNotNull(rawFile);
	    Assert.assertEquals(1.0, importer.getFinishedPercentage(), 0.0001);
	  }

	@Test
	public void testestimateFwhmMs() throws MSDKException{
		double fwhm = objCurveTool.estimateFwhmMs(20);
		Assert.assertEquals(1, fwhm,1);
	}
}
