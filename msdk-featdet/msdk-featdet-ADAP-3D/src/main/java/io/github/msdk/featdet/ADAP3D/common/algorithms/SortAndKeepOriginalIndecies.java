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
package io.github.msdk.featdet.ADAP3D.common.algorithms;

import java.util.Comparator;

/**
 *
 * @author owenmyers
 * Modified by Dharak Shah to include in MSDK
 */
public class SortAndKeepOriginalIndecies implements Comparator<Integer>{
    private final double[] dataArr;
    public SortAndKeepOriginalIndecies(double[] dataInArr)
    {
        this.dataArr =dataInArr;
    }
    
    public Integer[] makeArrOfIndecies(){
        Integer[] indecies = new Integer[dataArr.length];
        for (int i = 0; i < dataArr.length;i++){
            indecies[i] = i;
        }
        return indecies;
    }
    
    @Override
    public int compare(Integer index1, Integer index2)
    {
        if (dataArr[index2]>dataArr[index1]){
            return -1;
        }
        else if (dataArr[index2]<dataArr[index1]){
            return 1;
        }
        else{
            return 0;
        }
    }
    
    
}
