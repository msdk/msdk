package fr.profi.mzdb.db.model.params.thermo;

public class ThermoFragmentationTarget {

	/** The msLevel. */
	protected final int msLevel;

	/** The mz. */
	protected final double mz;

	/** The peak encoding. */
	protected String activationType;

	/** The collisionEnergy. */
	protected final float collisionEnergy;

	public ThermoFragmentationTarget(int msLevel, double mz, String activationType, float collisionEnergy) {
		super();
		this.msLevel = msLevel;
		this.mz = mz;
		this.activationType = activationType;
		this.collisionEnergy = collisionEnergy;
	}

	public int getMsLevel() {
		return msLevel;
	}

	public double getMz() {
		return mz;
	}

	public String getActivationType() {
		return activationType;
	}

	public float getCollisionEnergy() {
		return collisionEnergy;
	}


}
