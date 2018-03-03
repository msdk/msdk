package fr.profi.mzdb.util.sqlite;

import com.almworks.sqlite4java.SQLiteJob;
import com.almworks.sqlite4java.SQLiteQueue;

import rx.Observable;

/**
 * @author bouyssie
 *
 */
public class SQLiteObservableRecord extends Observable<SQLiteRecord> {
    
    public SQLiteObservableRecord( final SQLiteQueue queue, final String sqlQuery ) {
        super(onSubscribe(queue, sqlQuery));
    }

    private static OnSubscribe<SQLiteRecord> onSubscribe( final SQLiteQueue queue, final String sqlQuery ) {
        	
    	return subscriber -> {
    		
    		SQLiteJob<Void> sqliteJob = SQLiteObservableJob.buildSQLiteJob( subscriber, connection -> {
    			
				// Create the query
				SQLiteQuery sqliteQuery = new SQLiteQuery(connection, sqlQuery, false);
				
				// Iterate over each record
				sqliteQuery.forEachRecord( (record,idx) -> {
					// Emit new SQLiteRecord
					subscriber.onNext(record);
				});

				// Dispose the statement
				sqliteQuery.dispose();
				
				return null;
    		});
    	
    	/*return subscriber -> {
    		
    		SQLiteJob<SQLiteRecord> sqliteJob = new SQLiteJob<SQLiteRecord>() {
    			
    			// this method is called from database thread and passed the connection
    			protected SQLiteRecord job(SQLiteConnection connection) throws SQLiteException {
    				
					try {
						// Create the query
						SQLiteQuery sqliteQuery = new SQLiteQuery(connection, sqlQuery, false);
						
						// Iterate over each record
						sqliteQuery.forEachRecord( (record,idx) -> {
							// Emit new SQLiteRecord
							subscriber.onNext(record);
						});

						// Dispose the statement
						sqliteQuery.dispose();
						
					} catch (Exception e) {
						e.printStackTrace();
						subscriber.onError(e);
					}
					
					return null;
    			}

    			protected void jobFinished(SQLiteRecord result) {
    				// Note: result is null here (everything has been passed to the Observable)
					System.out.println("Finishing job from SQLiteObservableRecord");
					
					if (subscriber.isUnsubscribed() == false) {
						// Complete the observer
						subscriber.onCompleted();
					}
    			}
    			

    		};*/
    		
    		queue.execute(sqliteJob);
        };
    }

}
