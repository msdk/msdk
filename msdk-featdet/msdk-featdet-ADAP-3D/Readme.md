# Intelligent LC/MS Feature Detection (ADAP3D Algorithm)

## Introduction

*This algorithm is developed by Du-Lab research team ([http://www.du-lab.org](http://du-lab.org)) to process Mass Spectrometry Metabolomic data in python. This algorithm is rewritten by Dharak Shah in java to make it part of MSDK library as **Google Summer of Code 2017** project.*

## Description

*The algorithm is designed to detect 3-D peaks in raw data.The 3 dimensions are M/z(mass to charge ratio), Retention Time and Intensity. Continuous Wavelet transform is used to determine peak boundaries. This algorithm can process different file formats with the help of readers available in MSDK library. This algorithm primarily uses two tests to determine intensity peaks in from the data file. Peak3D test and BiGaussian Test. First it determines N higehst peaks with default parameters and estimates the parameters again from those peaks. After that it determines rest of the peaks with newly estimated parameters. So this algorithm has ability to adapt parameters. This algorithm also creates the features like chromatogram for each peak.*

## Useful Link

1. [Link to Commits](https://github.com/msdk/msdk/commits?author=dharak029)
2. [Link to Pull Requests](https://github.com/msdk/msdk/pulls?q=is%3Apr+is%3Aclosed+no%3Aassignee+author%3Adharak029)
3. [Link to Code](https://github.com/msdk/msdk/tree/master/msdk-featdet/msdk-featdet-ADAP-3D/src/main/java/io/github/msdk/featdet/ADAP3D)
4. [Link to TestCases](https://github.com/msdk/msdk/tree/master/msdk-featdet/msdk-featdet-ADAP-3D/src/test/java/io/github/msdk/featdet/ADAP3D)
5. [Link to Detailed Project Report](https://github.com/du-lab/msdk/blob/master/msdk-featdet/msdk-featdet-ADAP-3D/ADAP3D%20Project%20Report.docx)

## Major Challenges

1. Implementation of Sparse Matrix and it's operations.
2. Implementation of Guassian and BiGaussian.

## Future Work

1. Make algorithm more memory efficient - Currently algorithm is able to process raw data file as big as 310 mb with java heap size of 1 GB. It should be able to process file as big as 3 GB.
2. To be able to read different types of metabolomics data this algorithm should be able to resample the data and it also should be able to determine isoptopes.

## Conclusion

*For implementing this algorithm in java I developed my own class for sparse matrix and developed my own methods for different matrix operations. In addition to that I also developed my own classes for implementing BiGaussian and Gaussian which helped me to improve my coding skills. Working with Open Chemistry was my first experience with open source software development and I really enjoyed it. I learned a new framework of java, new coding standard, how to code efficiently in terms of time and memory both, applied many concepts studied in college. I got to work with many distinguish people of the field. It was a very enriching experience, which I intend to continue participating. Thanks to all the mentors who helped me to achieve the desired results.*
