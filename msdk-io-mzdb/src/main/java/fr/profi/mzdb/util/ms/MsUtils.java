package fr.profi.mzdb.util.ms;

public class MsUtils {

    /** The proton mass. */
    public static double protonMass = 1.007276466812;

    /**
     * 
     * @param mz
     * @param errorInPPM
     * @return
     */
    public static double ppmToDa(double mz, double errorInPPM) {
	return mz * errorInPPM / 1e6;
    }

    /**
     * 
     * @param mz
     * @param d
     * @return
     */
    public static double DaToPPM(double mz, double d) {
	return d * 1e6 / mz;
    }

}
