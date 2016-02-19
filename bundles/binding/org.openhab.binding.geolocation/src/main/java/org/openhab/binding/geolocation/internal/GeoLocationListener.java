package org.openhab.binding.geolocation.internal;

import java.io.BufferedReader;
import java.io.InputStreamReader;
import java.net.URL;
import java.util.TimerTask;

import org.json.JSONException;
import org.json.JSONObject;
import org.openhab.core.items.Item;
import org.openhab.core.items.StateChangeListener;
import org.openhab.core.types.State;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

public class GeoLocationListener implements StateChangeListener {
	
	private GeoLocationBinding binding;
	public GeoLocation geolocation = new GeoLocation();
	JSONObject locationDoc = new JSONObject();
	String oldLocation = "";
	double latitude = -1;
	double longitude = -1; 
	String newLocation;
	String[] parts;
	String URL = "";
	String response = "";
	
	JSONObject json;
	
	
	
    private URL url;// = new URL();
    BufferedReader reader = null;
    StringBuffer buffer = new StringBuffer();
    int read;
    char[] chars = new char[1024];
	
	private static final Logger logger = LoggerFactory
			.getLogger(GeoLocationListener.class);


	public GeoLocationListener(GeoLocationBinding binding) {
		// TODO Auto-generated constructor stub
		this.binding =  binding;
	}

	public GeoLocationListener() {
		// TODO Auto-generated constructor stub
	}


	@Override
	public synchronized void  stateChanged(Item item, State oldState, State newState) {
		// TODO Auto-generated method stub
		

		newLocation = newState.toString();
		parts = newLocation.split(",");
		latitude = Double.parseDouble(parts[0]);
		longitude = Double.parseDouble(parts[1]); 

		// Update object from OpenMaps.
		logger.debug ("Received notification received for item {}, values is {}", item.toString(), newState.toString());
		if (newLocation == oldLocation ) { 
			logger.debug ("Disregarding");
		}
		
		try { 
			logger.debug("Getting data for {}", newLocation );
			locationDoc = this.getData(latitude, longitude);
			if (locationDoc == null ) { 
				logger.debug("Location doc is null.");
			} else { 
				logger.debug("Parsing location. Data is {}", locationDoc.toString());
			    geolocation.parseJson(locationDoc); 	
			}
			
		} catch ( Exception e ) {
			logger.error ( "Error getting location data. {}", e.toString());
		}
		
		// Call the binding to UpdateGeoLocation Itens from Object. 
		
		if (this.binding != null) { 
			logger.debug("triggering binding update. Returning {}", geolocation.toString());
			this.binding.updateValue(geolocation);
		}
		
		oldLocation = newLocation;
	
		
	}

	@Override
	public void stateUpdated(Item item, State state) {
		// TODO Auto-generated method stub
		
	}
	
	private JSONObject getData (Double latitude, Double longitude){
		
		URL = "";
		response = "";
		
		try{
			URL = "http://nominatim.openstreetmap.org/reverse?lat=" +  latitude.toString() + "&lon=" + longitude.toString() + "&format=json&extratags=1&zoom=17&addressdetails=1&namedetails=1";
			logger.debug("Getting response for {}", URL);
			response = readUrl(URL);
			json = new JSONObject( response );
			return json;
			
			
		} catch (JSONException jex) { 
			logger.debug("Exception parsing JSON data, {}", response, jex.toString());
			return null;
		}  catch (Exception ex) { 
			logger.debug("Exception gettingData from {}, {}", URL, ex.toString());
			return null;
		} 
		
	}
	
	private  String readUrl(String urlString) throws Exception {

	    try {
	        url = new URL(urlString);
	        reader = new BufferedReader(new InputStreamReader(url.openStream()));
	        buffer = new StringBuffer();
	        chars = new char[1024];
	        while ((read = reader.read(chars)) != -1)
	            buffer.append(chars, 0, read); 

	        return buffer.toString();
	    } catch (Exception ex) { 
	    	logger.debug("Error getting data from OpenMaps site: {}", ex.toString());
	    	return null;
	    }  finally {
	        if (reader != null)
	            reader.close();
	    } 
	}

}
