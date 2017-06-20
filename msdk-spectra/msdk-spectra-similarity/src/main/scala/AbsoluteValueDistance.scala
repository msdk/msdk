package edu.ucdavis.fiehnlab.math.similarity

import edu.ucdavis.fiehnlab.{Intensity, Spectrum}

/**
  * Created by singh on 1/28/2016.
  */
class AbsoluteValueDistance extends Similarity {

  /**
    * computes the absolute value distance between two mass spectra, as defined here
    *
    * http://ac.els-cdn.com/1044030594870098/1-s2.0-1044030594870098-main.pdf?_tid=143d0622-c54d-11e5-af32-00000aab0f26&acdnat=1453937234_932c2334be5baa8bddfb4d7fe64e5580
    *
    * @param unknown
    * @param library
    * @return
    */
  def compute(unknown:Spectrum, library:Spectrum) : Double = {
    val libraryIons = library.fragments
    val unknownIons = unknown.fragments

    val zeroIntensity = new Intensity(0.0)

    val norm = unknown.fragments.values.map(_.value).sum
    val diff = libraryIons.keySet.map(k => math.abs(libraryIons(k).value - unknownIons.getOrElse(k, zeroIntensity).value)).sum

    math.pow(1 + diff / norm, -1)
  }
}
