package fr.profi.mzdb.util.iterator;

import java.util.Iterator;
import java.util.LinkedList;
import java.util.NoSuchElementException;

public class BufferedIterator<E> implements Iterator<E> {

	private Iterator<E> source;
	private int max;
	private LinkedList<Object> queue;
	private E nextReturn;
	private Object done = new Object();

	public BufferedIterator(Iterator<E> src, int m) {

		max = m;
		source = src;
		queue = new LinkedList<Object>();

		(new Thread("BufferedIterator Filler") {

			public void run() {

				while (source.hasNext()) {

					E next = source.next();

					synchronized (queue) {

						while (queue.size() >= max) {

							try {

								queue.wait();
							} catch (InterruptedException doh) {

								doh.printStackTrace();

								return; // something went wrong

							}

						}

						queue.add(next);

						queue.notify();

					}

				}

				synchronized (queue) {

					queue.add(done);

					queue.notify();

				}

			}

		}).start();

	}

	@SuppressWarnings("unchecked")
	public synchronized boolean hasNext() {

		while (nextReturn == null) {

			synchronized (queue) {

				while (queue.isEmpty()) {

					try {

						queue.wait();

					} catch (InterruptedException doh) {

						doh.printStackTrace();

						return false; // something went wrong

					}

				}

				nextReturn = (E) queue.removeFirst();

				queue.notify();

				if (nextReturn == done) {

					return false;

				}

			}

		}

		return true;

	}

	public synchronized E next() {

		if (!hasNext()) {

			throw new NoSuchElementException();

		}

		E retVal = nextReturn;

		nextReturn = null;

		return retVal;

	}

	public void remove() {

		throw new UnsupportedOperationException("Unsupported operation.");

	}

}