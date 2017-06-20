package io.github.msdk.featdet.ADAP3D.common.algorithms;

import java.util.List;
import io.github.msdk.featdet.ADAP3D.common.algorithms.SliceSparseMatrix.SparseMatrixTriplet;


public class CurveTool {
	
	
	public int estimateFwhmMs(List<SparseMatrixTriplet> scanMatrix,int numberOfScansForFWHMCalc){
		
		for(int i=0;i<=numberOfScansForFWHMCalc;i++){
			SparseMatrixTriplet triplet = scanMatrix.get(i);
		}
		return 0;
	}
}
