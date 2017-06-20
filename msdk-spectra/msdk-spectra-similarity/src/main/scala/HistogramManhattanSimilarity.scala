package edu.ucdavis.fiehnlab.math.similarity

import edu.ucdavis.fiehnlab.Spectrum
import edu.ucdavis.fiehnlab.util.MathUtilities

/**
  * Created by sajjan on 1/29/16.
  */
class HistogramManhattanSimilarity extends Similarity {

  /**
    * Compute the manhattan similarity between two histograms
    *
    * The manhattan distance is defined as the sum of the pairwise absolute
    * difference between histogram bins, and the manhattan similarity scales
    * this value by the sums of the bins of both histograms
 *
    * @param unknown
    * @param library
    * @return
    */
  def compute(unknown: Spectrum, library: Spectrum): Double = {
    MathUtilities.calculateHistogramSimilarity(unknown.histogram,library.histogram,36)
  }
}
