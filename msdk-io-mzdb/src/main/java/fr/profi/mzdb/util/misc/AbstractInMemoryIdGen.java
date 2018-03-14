package fr.profi.mzdb.util.misc;

import java.util.concurrent.atomic.AtomicInteger;

public abstract class AbstractInMemoryIdGen {

	private static AtomicInteger _idSequence = new AtomicInteger(0);

	public static int generateNewId() {
		return _idSequence.incrementAndGet();
	}

}
