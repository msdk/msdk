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
package io.github.msdk.featdet.ADAP3D.datamodel;
/**
 * <p>
 * This is the data model for creating triplet representation of sparse matrix. 
 * </p>
 */
public class SparseMatrixTriplet {
	public double mz;
	public int scanNumber;
	public float intensity;
	public float rt;
	public boolean removed;
}
