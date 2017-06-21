package edu.ucdavis.fiehnlab.math.similarity

import edu.ucdavis.fiehnlab.Spectrum

/**
  * Created by wohlgemuth on 1/27/16.
  */
class CompositeSimilarity extends Similarity {
  /**
    * computes a similarity between 2 different spectra and return a value between 0-1
    *
    * based on the following publication
    *
    * http://ac.els-cdn.com/1044030594850224/1-s2.0-1044030594850224-main.pdf?_tid=4c05468e-c550-11e5-9321-00000aab0f02&acdnat=1453938616_682e264082fe2b8b1b7ef53d90e46a6b
    *
    * @param unknown
    * @param library
    * @return
    */
  override def compute(unknown: Spectrum, library: Spectrum): Double = {
    val sharedIons = library.fragments.keySet.intersect(unknown.fragments.keySet).toList.sorted

    val cosineSimilarity = new CosineSimilarity().compute(unknown, library, sharedIons)

    if (sharedIons.size > 1) {
      // Takes the ratio of successive list elements, ie A[i] / A[i + 1]
      val unknownRatios: List[Double] = sharedIons.map(k => library.fragments(k)).sliding(2).map { case List(x, y) => x.value / y.value }.toList
      val libraryRatios: List[Double] = sharedIons.map(k => library.fragments(k)).sliding(2).map { case List(x, y) => x.value / y.value }.toList

      // Divide the unknown ratio by the library ratio
      val combinedRations: List[Double] = unknownRatios.zip(libraryRatios).map { case (x, y) => x / y }

      // Ensure each term is less than 1 and then sum
      val intensitySimilarity: Double = 1 + combinedRations.map { x => if (x < 1) x else 1 / x }.sum

      // Compute the composite similarity
      (unknown.fragments.size * cosineSimilarity + intensitySimilarity) / (unknown.fragments.size + sharedIons.size)
    } else {
      cosineSimilarity
    }
  }
}
