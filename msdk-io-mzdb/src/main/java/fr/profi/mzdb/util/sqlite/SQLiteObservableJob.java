package fr.profi.mzdb.util.sqlite;

import java.util.Iterator;

import com.almworks.sqlite4java.SQLiteConnection;
import com.almworks.sqlite4java.SQLiteException;
import com.almworks.sqlite4java.SQLiteJob;
import com.almworks.sqlite4java.SQLiteQueue;

import rx.Observable;
import rx.Subscriber;

/**
 * @author bouyssie
 *
 */
public class SQLiteObservableJob<T> extends Observable<T> {
	
    private final ISQLiteConnectionFunction<T> sqliteJobWrapper;
    
    public ISQLiteConnectionFunction<T> getSQLiteJobWrapper() {
        return sqliteJobWrapper;
    }
    
    public SQLiteObservableJob( final SQLiteQueue queue, final SQLiteJobWrapper<T> sqliteJobWrapper ) {
        super(onSubscribe(queue, sqliteJobWrapper));
        this.sqliteJobWrapper = sqliteJobWrapper;
    }

    private static <T> OnSubscribe<T> onSubscribe( final SQLiteQueue queue, final SQLiteJobWrapper<T> sqliteJobWrapper ) {
    	
    	return subscriber -> {
    		
    		SQLiteJob<T> sqliteJob = buildSQLiteJob( subscriber, connection -> {
    			T result = null;
    			
				result = sqliteJobWrapper.job(connection);
				subscriber.onNext(result);
				
				return result;
    		});
    				/*new SQLiteJob<T>() {
    			// this method is called from database thread and passed the connection
    			protected T job(SQLiteConnection connection) throws SQLiteException {
    				
    				T result = null;
					try {
						result = sqliteJobWrapper.job(connection);
						subscriber.onNext(result);
					} catch (Exception e) {
						e.printStackTrace();
						subscriber.onError(e);
					}
					
					return result;
    			}

    			protected void jobFinished(T result) {
					System.out.println("Finishing job from SQLiteObservableJob");
					
					if (subscriber.isUnsubscribed() == false) {
						// Complete the observer
						subscriber.onCompleted();
					}
    			}

    		};*/
    		
    		sqliteJobWrapper.setSQLiteJob(sqliteJob);
    		
    		queue.execute(sqliteJob);
        };
    }
    
    public static <R> SQLiteJob<R> buildSQLiteJob( final Subscriber<?> subscriber, final ISQLiteConnectionFunction<R> function ) {
    	
		return new SQLiteJob<R>() {
			// this method is called from database thread and passed the connection
			protected R job(SQLiteConnection connection) throws SQLiteException {
				
				R result = null;
				try {
					// Warning: the consumer MUST call the onNext method
					result = function.apply(connection);
				} catch (Exception e) {
					System.err.println("Error caught in SQLiteObservableJob");
					e.printStackTrace();
					
					// Send error to the observer
					subscriber.onError(e);
				}
				
				return result;
			}

			protected void jobFinished(R result) {
				if (subscriber.isUnsubscribed() == false) {
					// Complete the observer
					subscriber.onCompleted();
				}
			}
			
			@Override
			protected void jobError(Throwable error) throws Throwable {
				if (subscriber.isUnsubscribed() == false) {
					// Send error to the observer
					subscriber.onError(error);
				}
			}
		};
    		
    }
    
    public static <T> void observeIterator( final Subscriber<? super T> subscriber, final Iterator<T> iterator ) {
    	
		// Iterate over each record
		while (iterator.hasNext()) {
			// Emit new elem
			subscriber.onNext(iterator.next());
		}
    	
    }

}
