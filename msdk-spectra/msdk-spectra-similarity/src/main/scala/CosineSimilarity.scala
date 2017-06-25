package edu.ucdavis.fiehnlab.math.similarity

import edu.ucdavis.fiehnlab.{Ion, Spectrum}

/**
  * Created by singh on 1/28/2016.
  */
class CosineSimilarity extends Similarity {

  /**
    * computes the absolute value distance between two mass spectra, as defined here
    *
    * http://ac.els-cdn.com/1044030594870098/1-s2.0-1044030594870098-main.pdf?_tid=143d0622-c54d-11e5-af32-00000aab0f26&acdnat=1453937234_932c2334be5baa8bddfb4d7fe64e5580
    *
    * @param unknown
    * @param library
    * @return
    */
  def compute(unknown: Spectrum, library: Spectrum): Double = {
    val sharedIons: Set[Ion] = library.fragments.keySet intersect unknown.fragments.keySet

    compute(unknown, library, sharedIons.toList)
  }

  /**
    *
    * @param unknown
    * @param library
    * @param sharedIons needs to be an ordered list
    * @return
    */
  def compute(unknown: Spectrum, library: Spectrum, sharedIons: List[Ion]): Double = {

    val unknownNorm: Double = unknown.fragments.values.map(x => x.value * x.value).sum
    val libraryNorm: Double = library.fragments.values.map(x => x.value * x.value).sum
    val product: Double = math.pow(sharedIons.map(k => library.fragments(k).value * unknown.fragments(k).value).sum, 2)

    //added a rounding step
    (math round  product / libraryNorm / unknownNorm * 1000) /1000

    //sharedIons.map(k => (library.normalizedFragments(k).value / libraryNorm) * (unknown.normalizedFragments(k).value / unknownNorm)).sum
  }
}
