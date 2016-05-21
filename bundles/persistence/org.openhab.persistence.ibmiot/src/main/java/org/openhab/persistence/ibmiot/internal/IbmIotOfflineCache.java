package org.openhab.persistence.ibmiot.internal;

import java.util.ArrayList;
import java.util.Calendar;
import java.util.Collections;
import java.util.Date;
import java.util.List;

import org.bson.types.ObjectId;
import org.json.JSONException;
import org.json.JSONObject;

//import org.openhab.persistence.mongodb.internal.MongoDBPersistenceService;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import com.mongodb.BasicDBObject;
import com.mongodb.DBCollection;
import com.mongodb.DBCursor;
import com.mongodb.DBObject;
import com.mongodb.MongoClient;
import com.mongodb.MongoClientURI; 

public class IbmIotOfflineCache {

	private static final Logger logger = LoggerFactory
			.getLogger(IbmIotOfflineCache.class);
	
	private boolean initialized = false;
	
	private MongoClient cl;
	private DBCollection mongoCollection;
	int connectRetry = 0;
	DBObject obj = new BasicDBObject();
	DBCursor cursor = null;
	List<BasicDBObject> items;
	String s;
	public boolean hasCache = true;
	
	public void activate() {
		logger.debug("IBMIoT Offline Cache Starting.");
		this.connectToDatabase();
	}
	

	public void deactivate() {
		logger.debug("IBMIoT Offline Cache stopping. Disconnecting from database.");
		disconnectFromDatabase();
	}
	
	
	/**
	 * @{inheritDoc
	 */
	
	public void store(JSONObject item ) {
		this.store (item.toString());
	}
	
	public void store(String item ) {

		connectRetry = 0;
		try { 
			// Don't log undefined/uninitialised data
			if (item == null) {
				return;
			}

			// Connect to mongodb server if we're not already connected
			while  (!isConnected()) {
				logger.trace("Connecting to database");
				connectToDatabase();
				Thread.sleep(1000);
			}

			// If we still didn't manage to connect, then return!
			if (!isConnected()) {
				if (connectRetry++ > 10) { 
					logger.error("Aborting Mongo Connection.");
					return;
				}
				logger.error(
						"IBM IoT Offline Cache: No connection to database. Can not persist item '{}'! Will retry connecting to database next time.",
						item);
				return;
			}

			logger.trace ("Preparing Object");
			obj = new BasicDBObject();
			obj.put("_id", new ObjectId());
			obj.put("doc", item.getBytes("utf-8"));
			logger.trace ("Storing Object");
			this.mongoCollection.save(obj);
			logger.trace ("Done storing object into Mongo. Size was {}", item.length());
			
			this.hasCache = true;

			logger.debug("IBM IoT Offline Cache save {}", item.toString());
		} catch (Exception ex) { 
			logger.error("Error saving message to offline cache: {}", ex.toString() );
			ex.printStackTrace();
		}
	}


	/**
	 * Checks if we have a database connection
	 * 
	 * @return true if connection has been established, false otherwise
	 */
	private boolean isConnected() {
		return cl != null;
	}
	
	/**
	 * Connects to the database
	 */
	private void connectToDatabase() {
		try {
			logger.debug("Connect MongoDB");
			this.cl =  new MongoClient( "localhost" , 27017 );
			mongoCollection = cl.getDB("ibmiot").getCollection("offline_docs");
			

			//BasicDBObject idx = new BasicDBObject();
			//idx.append(FIELD_TIMESTAMP, 1).append(FIELD_ITEM, 1);
			//this.mongoCollection.createIndex(idx);
			logger.debug("Connect MongoDB ... done");
			initialized = true;
		} catch (Exception e) {
			logger.error("Failed to connect to database");
			throw new RuntimeException("Cannot connect to database", e);
		}
	}

	/**
	 * Disconnects from the database
	 */
	private void disconnectFromDatabase() {
		this.mongoCollection = null;
		if (this.cl != null) {
			this.cl.close();
		}
		cl = null;
	}
	
	public void remove(BasicDBObject item) { 
		try {
			//obj = new BasicDBObject();
			//obj.put("_id", item.getString("_id"));
			this.mongoCollection.remove(item);
		} catch (Exception e) {
			// TODO Auto-generated catch block
			logger.error("Failed to delete doc from offline cache. Item: {}", item.toString());
		}
	}
	
	public Iterable<BasicDBObject> query() {
		if (!initialized) {
			logger.error("Returning empty list from offline cache. Not Initialized" );
			return Collections.emptyList();
		}

		if (!isConnected())
			connectToDatabase();

		if (!isConnected()) {
			logger.error("Returning empty list from offline cache. Not Connected" );
			return Collections.emptyList();
		}
		
		items = new ArrayList<BasicDBObject>();

		//Integer sortDir = 0;
		cursor = this.mongoCollection.find()
				.sort(new BasicDBObject("_id", 0));

		while (cursor.hasNext()) {
			items.add((BasicDBObject) cursor.next());

			

			//items.add((JSONObject)obj.get("doc"));
		}
		
		logger.info("Returning {} itens from offline cache.", items.size());
		return items;
	}
}
