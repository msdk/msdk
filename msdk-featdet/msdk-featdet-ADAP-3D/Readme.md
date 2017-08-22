# Intelligent LC/MS Feature Detection ( ADAP3D Algorithm)

## Introduction

*I developed this algorithm as a part of **Google Summer Of Code 2017**.*

## Description

*The data from LC/MS and GC/MS experiments is 3 dimensional: m/z (mass to charge ratio), intensity, and retention time. To process this data efficietly I've developed this algorithm. This algorithm can process different file formats with the help of readers available in MSDK library. This algorithm primarily uses two tests to determine intensity peaks in from the data file. Peak3D test and BiGaussian Test. This algorithm is parameter free. First it determines 20 higehst peaks with default parameters and estimates the parameters again from those peaks. After that it determines rest of the peaks with newly estimated parameters. This algorithm also creates the features like chromatogram for each peak.*

## Useful Link

1. [Link to Commits](https://github.com/msdk/msdk/commits?author=dharak029)
2. [Link to Pull Requests](https://github.com/msdk/msdk/pulls?q=is%3Apr+is%3Aclosed+no%3Aassignee+author%3Adharak029)

## Future Work

1. Resamplling of Data
2. Determination of Isotope  
2. Make algorithm more memory efficient

## Conclusion

*Working with Open Chemistry was my first experience with open source software development and I really enjoyed it. I learned a new framework of java, new coding standard, how to code efficiently in terms of time and memory both, applied many concepts studied in college. I got to work with many distinguish people of the field. It was a very enriching experience, which I intend to continue participating. Thanks to all the mentors who helped me to achieve the desired results.*
