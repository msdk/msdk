package fr.profi.mzdb.util.concurrent;

import java.util.concurrent.Callable;

/**
 * @author JeT
 *
 */
public class CallableCallback<V> implements Callable<V> {

	private final Callable<V> callable;
	private final Callback<V> callback;

	/**
	 * @param callback
	 */
	public CallableCallback(Callable<V> callable, Callback<V> callback) {
		super();
		this.callable = callable;
		this.callback = callback;
	}

	/*
	 * (non-Javadoc)
	 *
	 * @see java.util.concurrent.Callable#call()
	 */
	@Override
	public V call() throws Exception {
		V result = this.callable.call();
		if (this.callback != null) {
			this.callback.onCompletion(result);
		}
		return result;
	}

}
