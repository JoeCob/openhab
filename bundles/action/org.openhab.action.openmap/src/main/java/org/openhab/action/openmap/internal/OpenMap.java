/**
 * Copyright (c) 2010-2015, openHAB.org and others.
 *
 * All rights reserved. This program and the accompanying materials
 * are made available under the terms of the Eclipse Public License v1.0
 * which accompanies this distribution, and is available at
 * http://www.eclipse.org/legal/epl-v10.html
 */
package org.openhab.action.openmap.internal;

import org.apache.commons.io.IOUtils;
import org.apache.commons.lang.StringUtils;
import org.openhab.core.scriptengine.action.ActionDoc;
import org.openhab.core.scriptengine.action.ParamDoc;
import org.openhab.io.net.http.HttpUtil;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.json.JSONException;
import org.json.JSONObject;

/** 
 * This class provides static methods that can be used in automation rules
 * for sending XBMC notifications
 * 
 * @author Ben Jones
 * @author Panos Kastanidis
 * @since 1.3.0
 */




public class OpenMap {

	/** Constant which represents the content type <code>application/json</code> */
	public final static String CONTENT_TYPE = "application/json";
	
	private static JSONObject response = new JSONObject();
	private static String host = "nominatim.openstreetmap.org";

	private static Logger logger = LoggerFactory.getLogger(OpenMapActivator.class); 
	
	
	
	/** 
	* Gets the current streetName  
	 * @throws Exception 
	*
	*/ 
	@ActionDoc(text="Gets the street name for the current location. ")
	static public String getStreetName () throws Exception
			{ 
		 if (response.has("address:road")) {
			 return (String) response.get("address:road");
		 } else {
			 return null;
		 }
	} 
	
	
	/** 
	* Updates the GeoLocation Data internal Object 
	*
	* @param latitude the XBMC client to be notified
	* @param longitude the XBMC web server port
	*/ 
	@ActionDoc(text="Updates the geo information for a specific location point. ")
	static public void updateGeoData (
			@ParamDoc(name="latitude") double latitude, 
			@ParamDoc(name="longitude") double  longitude )
			{ 
		 getReverseGeolocationObject (latitude, longitude);
		 logger.debug ( "GeoObject is {}", response.toString() );
	} 
	
	
	
	
	/** 
	* Gets the openmap geolocation information via GET-HTTP. Errors will be logged, returned values just ignored.
	* Additional implementation to be able to show also images and to define a display time 
	*
	* @param latitude
	* @param longitude
	*/
	@ActionDoc(text="Gets the openmap reverse geolocation information via GET-HTTP. Errors will be logged, returned values just ignored.")
	static private void  getReverseGeolocationObject(@ParamDoc(name="latitude") double latitude,@ParamDoc(name="longitude") double longitude) { 
		String url = "http://" + host + "/reverse";
		// http://nominatim.openstreetmap.org/reverse?lat=-29.9403436&lon=-50.8941909&format=json&extratags=1

		StringBuilder content = new StringBuilder();
		
		content.append("?lat="+String.valueOf(latitude));
		content.append("&lon="+String.valueOf(longitude));
		content.append("&format=json&extratags=1");
		
		
        
		String httpResponse = HttpUtil.executeUrl("GET", url, null , CONTENT_TYPE, 1000); 
		
		try {
			response = new JSONObject(httpResponse);
		} catch (JSONException e) {
			// TODO Auto-generated catch block
			e.printStackTrace();
		}
	
	}
}
