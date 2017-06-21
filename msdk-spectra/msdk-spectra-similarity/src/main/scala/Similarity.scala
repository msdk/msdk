package edu.ucdavis.fiehnlab.math.similarity

import edu.ucdavis.fiehnlab.Spectrum

/**
  * computes a similarity between 2 different spectra
  * different implementation can provide the exact way todo so
  */
trait Similarity {

  /**
    * computes a similarity between 2 different spectra and return a value between 0-1
    * @param unknown
    * @param reference
    * @return
    */
  def compute(unknown:Spectrum,reference:Spectrum) : Double
}

