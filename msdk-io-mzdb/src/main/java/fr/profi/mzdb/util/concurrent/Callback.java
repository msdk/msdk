package fr.profi.mzdb.util.concurrent;

/**
 * @author JeT
 *
 */
public interface Callback<T> {

    public void onCompletion(T t);
}
