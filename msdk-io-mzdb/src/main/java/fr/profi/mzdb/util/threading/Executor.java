/**
 * Class launching several threads with runnable or callables
 * size of runnable array can be greater than the number of cores
 */
package fr.profi.mzdb.util.threading;

import java.util.ArrayList;
import java.util.List;
import java.util.concurrent.Callable;
import java.util.concurrent.ExecutionException;
import java.util.concurrent.ExecutorService;
import java.util.concurrent.Executors;
import java.util.concurrent.Future;
import java.util.concurrent.TimeUnit;

/**
 * 
 * @author marco
 * 
 */
public class Executor {

	private ExecutorService pool;
	private Callable<Object>[] callables = null;
	private Runnable[] runnables = null;
	private List<Future<Object>> futures = new ArrayList<Future<Object>>();

	public Executor(Callable<Object>[] r, int core) {
		pool = Executors.newFixedThreadPool(core);
		callables = r;
	}

	public Executor(Runnable[] r, int core) {
		pool = Executors.newFixedThreadPool(core);
		runnables = r;
	}

	public List<Object> getResults() throws InterruptedException, ExecutionException {
		for (Callable<Object> t : callables) {
			Future<Object> f = pool.submit(t);
			futures.add(f);
		}
		pool.shutdown();
		List<Object> r = new ArrayList<Object>(futures.size());
		for (Future<Object> future : futures)
			r.add(future.get());
		return r;

	}

	public void start() throws InterruptedException {
		for (Runnable t : runnables)
			pool.execute(t);
		pool.shutdown();
		while (!pool.awaitTermination(1, TimeUnit.SECONDS)) {
			System.out.println("Waiting for tasks to shutdown");
		}
	}

}
