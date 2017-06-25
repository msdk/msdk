package edu.ucdavis.fiehnlab.math.similarity

import edu.ucdavis.fiehnlab.Spectrum

/**
  * Created by wohlgemuth on 1/28/16.
  */
class SplashSimilarity extends Similarity{
  /**
    * computes a similarity between 2 different spectra and return a value between 0-1
    * @param unknown
    * @param reference
    * @return
    */
  override def compute(unknown: Spectrum, reference: Spectrum): Double = {
    if(unknown.splash.equals(reference.splash)){
      1
    }
    else{
      0
    }
  }
}
