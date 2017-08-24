# ADAP-3D: An Adaptive Algorithm for Peak Detection from Mass Spectrometry-Based Metabolomics Data

## Introduction

ADAP-3D was originally developed by the Du-Lab research team ([http://www.du-lab.org](http://du-lab.org)) for detecting analyte-relevant peaks from raw Mass Spectrometry Metabolomic data. ADAP-3D takes advantage of the 3D nature of raw LC/MS or GC/MS data wherein mass spectra are stored in profile rather than centroid mode. The algorithm was first prototyped by the Du-Lab research team in python. Dharak Shah re-wrote the algorithm in java to speed up the computation and also make it part of the MSDK library as a **Google Summer of Code 2017** project.

## Description

The three dimensions of LC/MS or GC/MS data are m/z(mass to charge ratio), retention time and intensity. To detect peaks, ADAP-3D uses Continuous Wavelet transform and ridgeline detection. In addition, ADAP-3D estimates key preprocessing parameters from the data itself, making the algorithm self adaptive to the data being analyzed. ADAP-3D can accept raw data files in multiple formats including mzXML, CDF, mzML, et el. by using the existing capabilities of MSDK to import raw data.

## Useful Link

1. [Link to Commits](https://github.com/msdk/msdk/commits?author=dharak029)
2. [Link to Pull Requests](https://github.com/msdk/msdk/pulls?q=is%3Apr+is%3Aclosed+no%3Aassignee+author%3Adharak029)
3. [Link to Code](https://github.com/msdk/msdk/tree/master/msdk-featdet/msdk-featdet-ADAP-3D/src/main/java/io/github/msdk/featdet/ADAP3D)
4. [Link to TestCases](https://github.com/msdk/msdk/tree/master/msdk-featdet/msdk-featdet-ADAP-3D/src/test/java/io/github/msdk/featdet/ADAP3D)
5. [Link to Detailed Project Report](https://github.com/du-lab/msdk/blob/master/msdk-featdet/msdk-featdet-ADAP-3D/ADAP3D%20Project%20Report.docx)

## Major Challenges

1. Implementation of Sparse Matrix and it's operations.
2. Implementation of Guassian and BiGaussian fitting.

## Future Work

1. To make ADAP-3D more memory-efficient - Currently it is able to preprocess raw data file as big as 310 MB with Java heap size of 1 GB. It is hoped that more memory-efficient methods can enable ADAP-3D to preprocess files of 3+ GB in size.
2. To implement a method for detecting isotopes.
3. To resample raw profile mass spectra to achieve consistent sampling across all scans.

## Conclusion

*For implementing this algorithm in java I developed my own class for sparse matrix and developed my own methods for different matrix operations. In addition to that I also developed my own classes for implementing BiGaussian and Gaussian which helped me to improve my coding skills. Working with Open Chemistry was my first experience with open source software development and I really enjoyed it. I learned a new framework of java, new coding standard, how to code efficiently in terms of time and memory both, applied many concepts studied in college. I got to work with many distinguish people of the field. It was a very enriching experience, which I intend to continue participating. Thanks to all the mentors who helped me to achieve the desired results.*
