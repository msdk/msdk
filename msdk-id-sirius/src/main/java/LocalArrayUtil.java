import javax.annotation.Nonnull;

/**
 * Created by evger on 15-May-18.
 */
public class LocalArrayUtil {

  public static @Nonnull double[] convertToDoubles(@Nonnull float[] array) {
    double doubles[] = new double[array.length];
    for (int i = 0; i < array.length; i++)
      doubles[i] = array[i];

    return doubles;
  }

}
