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
package io.github.msdk.featuredetection.adap3d.algorithms;

import io.github.msdk.MSDKException;
import io.github.msdk.datamodel.RawDataFile;
import io.github.msdk.featuredetection.adap3d.algorithms.SliceSparseMatrix.Triplet;
import io.github.msdk.io.mzxml.MzXMLFileImportMethod;
import org.junit.Assert;
import org.junit.BeforeClass;
import org.junit.Test;

import java.io.File;
import java.net.URISyntaxException;
import java.net.URL;
import java.nio.file.Path;
import java.nio.file.Paths;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.List;

public class BiGaussianTest {

    private static RawDataFile rawFile;
    private static SliceSparseMatrix objSliceSparseMatrix;

    private static Path getResourcePath(String resource) throws MSDKException {
        final URL url = BiGaussianTest.class.getClassLoader().getResource(resource);
        try {
            return Paths.get(url.toURI()).toAbsolutePath();
        } catch (URISyntaxException e) {
            throw new MSDKException(e);
        }
    }

    @BeforeClass
    public static void loadData() throws MSDKException {
        // Import the file
        String file = "tiny.mzXML";
        Path path = getResourcePath(file);
        File inputFile = path.toFile();
        Assert.assertTrue("Cannot read test data", inputFile.canRead());
        MzXMLFileImportMethod importer = new MzXMLFileImportMethod(inputFile);
        rawFile = importer.execute();
        objSliceSparseMatrix = new SliceSparseMatrix(rawFile);
        Assert.assertNotNull(rawFile);
    }


    @Test
    public void testgetBiGaussianValue() throws MSDKException {
        List<Triplet> horizontalSlice = objSliceSparseMatrix.getHorizontalSlice(1810596, 50, 77);
        BiGaussian objBiGaussian = new BiGaussian(horizontalSlice, 1810596, 50, 77);
        double biGaussianValue = objBiGaussian.getValue(55);
        Assert.assertEquals(916711, biGaussianValue, 1);
    }

    @Test
    public void testInterpolationX() {

        final int numTriplets = 5;
        List<Triplet> triplets = new ArrayList<>(numTriplets);
        for (int i = 0; i < numTriplets; ++i) triplets.add(new Triplet());
        triplets.get(0).mz = 40;
        triplets.get(0).scanListIndex = 1;
        triplets.get(0).intensity = 100;
        triplets.get(1).mz = 40;
        triplets.get(1).scanListIndex = 3;
        triplets.get(1).intensity = 900;
        triplets.get(2).mz = 40;
        triplets.get(2).scanListIndex = 4;
        triplets.get(2).intensity = 1000;
        triplets.get(3).mz = 40;
        triplets.get(3).scanListIndex = 5;
        triplets.get(3).intensity = 900;
        triplets.get(4).mz = 40;
        triplets.get(4).scanListIndex = 7;
        triplets.get(4).intensity = 100;

        BiGaussian biGaussian = new BiGaussian(triplets, 40, 1, 7);

        Assert.assertEquals(1.6986436005760381, biGaussian.sigmaLeft, 1e-12);
        Assert.assertEquals(1.6986436005760381, biGaussian.sigmaRight, 1e-12);
    }
}
