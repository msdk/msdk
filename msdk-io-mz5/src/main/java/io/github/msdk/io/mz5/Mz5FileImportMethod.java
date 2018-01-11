package io.github.msdk.io.mz5;

import java.io.File;

import io.github.msdk.MSDKException;
import io.github.msdk.MSDKMethod;
import io.github.msdk.datamodel.RawDataFile;
import ncsa.hdf.object.h5.H5File;

public class Mz5FileImportMethod implements MSDKMethod<RawDataFile> {

	private final File mz5File;
	private RawDataFile rawDataFile;
	private volatile boolean canceled;
	private Float progress;
	
	private H5File h5File;

	public Mz5FileImportMethod(File mz5File) {
		this.mz5File = mz5File;
		canceled = false;
		progress = 0f;
	}

	@Override
	public RawDataFile execute() throws MSDKException {
		
		return rawDataFile;
	}

	@Override
	public Float getFinishedPercentage() {
		return progress;
	}

	@Override
	public RawDataFile getResult() {
		return rawDataFile;
	}

	@Override
	public void cancel() {
		this.canceled = true;
	}

}
