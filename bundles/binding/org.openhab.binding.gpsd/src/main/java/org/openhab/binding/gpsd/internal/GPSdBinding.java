/**
 * Copyright (c) 2010-2015, openHAB.org and others.
 *
 * All rights reserved. This program and the accompanying materials
 * are made available under the terms of the Eclipse Public License v1.0
 * which accompanies this distribution, and is available at
 * http://www.eclipse.org/legal/epl-v10.html
 */
package org.openhab.binding.gpsd.internal;

import java.util.Dictionary;
import java.util.Enumeration;
import java.util.HashMap;
import java.util.Map.Entry;

//import javax.xml.bind.DatatypeConverter;

//import net.astesana.javaluator.DoubleEvaluator;

import org.openhab.core.items.ItemNotFoundException;
import org.openhab.core.items.ItemRegistry;

import org.apache.commons.lang.StringUtils;
import org.openhab.binding.gpsd.GPSdBindingProvider;
import org.openhab.binding.gpsd.protocol.GPSd4JavaConnector;
import org.openhab.binding.gpsd.protocol.GPSdConnector;
import org.openhab.binding.gpsd.protocol.GPSdDataParser;
import org.openhab.binding.gpsd.protocol.GPSdParserRule;
//import org.openhab.binding.gpsd.protocol.GPSdSerialConnector;
import org.openhab.binding.gpsd.protocol.GPSdSimulator;
import org.openhab.core.binding.AbstractBinding;
import org.openhab.core.library.types.DecimalType;
import org.openhab.core.library.types.StringType;
import org.openhab.core.transform.TransformationException;
import org.openhab.core.transform.TransformationHelper;
import org.openhab.core.transform.TransformationService;
import org.osgi.service.cm.ConfigurationException;
import org.osgi.service.cm.ManagedService;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import de.taimos.gpsd4java.types.TPVObject;

/**
 * 
 * Binding to receive data from Open Energy Monitor devices.
 * 
 * @author Pauli Anttila
 * @since 1.4.0
 */
public class GPSdBinding extends
	AbstractBinding<GPSdBindingProvider> implements
		ManagedService {

	private static final Logger logger = LoggerFactory
			.getLogger(GPSdBinding.class);

	/* configuration variables for communication */
//	private int udpPort = 9997;
//	private String serialPort = null;
	private boolean simulate = false;
	private String  hostname = "10.0.1.23";
	private int	    port	 = 2947;
	private boolean movementTrack = false;
	private String  movementItem = "";
	private ItemRegistry itemRegistry;

//	private GPSdDataParser dataParser = null;
	private GPSdConnector connector;

	/** Thread to handle messages from GPSd Server devices */
	private MessageListener messageListener = null;

	private int connectionRetryTime = 30000;

	private int connectionRetryCount = 100;
	
	private double maxChangeThres = 30; 
	

	private int refresh =1000;

	public GPSdBinding() {
	}

	
	protected String getName() {
    	return "GPS Refresh Service";
	}
	
	public void activate() {
		logger.debug("Activate gpsd binding");
	}

	public void deactivate() {
		logger.debug("Deactivate gpsd binding");
		messageListener.setInterrupted(true);
	}

	/**
	 * @{inheritDoc
	 */
	@Override
	public void updated(Dictionary  config)
			throws ConfigurationException {

		logger.debug("GPSd Updated");
		if (config != null) {
			logger.debug("GPSD Configuration not null");
			if (config != null) {
				String hostnameString = (String) config.get("hostname");
				if (StringUtils.isNotBlank(hostnameString)) {
					hostname = hostnameString;
				}
				
				String portString = (String) config.get("port");
				if (StringUtils.isNotBlank(portString)) {
					port = Integer.parseInt(portString);
				}
				
				//setProperlyConfigured(true);
				
			}
			
			HashMap<String, GPSdParserRule> parsingRules = new HashMap<String, GPSdParserRule>();

			Enumeration<String> keys = config.keys();

			while (keys.hasMoreElements()) {
				String key = (String) keys.nextElement();

				// the config-key enumeration contains additional keys that we
				// don't want to process here ...
				if ("service.pid".equals(key)) {
					continue;
				}
				
				if ("simulate".equals(key)) {
					continue;
				}

				String value = (String) config.get(key);


				if (key.equals("connectionretrycount")) {
					this.setConnectionRetryCount(Integer.parseInt(value));
					
				}
				
				
				
				if ("hostname".equals(key)) {
					if (StringUtils.isNotBlank(value)) {
						hostname = value;
						logger.info("Hostname set to {}", hostname);
					}
				} else if ("port".equals(key)) {
					port = Integer.parseInt(value);
					logger.info("Port  set to {}", port);
					
				} else if (key.equals("connectionretrytime")) {
					this.setConnectionRetryTime(Integer.parseInt(value));
					logger.info("connectionretrytime  set to {}", value);
					
				} else if (key.equals("movementtracking")) {
					this.setMovementTrack(value.toLowerCase().equalsIgnoreCase("true") ?  true : false);
					logger.info("trackMovement  set to {}" , this.movementTrack );
				
				} else if (key.equals("maxchangethres")) {
						this.maxChangeThres = Double.parseDouble(value);
						logger.info("Movement Threshold   set to {}" , this.maxChangeThres );
						
				} else 	if (key.equals("movementitem")) {
					if (StringUtils.isNotBlank(value)) { 
						this.setMovementItem(value);
					}
					logger.info("movementitem  set to {}", value);
					
				} else if (key.equals("refresh")) {
					this.setRefresh(Integer.parseInt(value));
					logger.info("Refresh  set to {}", value);
					
				} else 	if (key.equals("connectionretrycount")) {
					this.setConnectionRetryCount(Integer.parseInt(value));
					logger.info("connectionretrycount  set to {}", value);
					
				} else {

					// process all data parsing rules
					try {
						GPSdParserRule rule = new GPSdParserRule(value);
						parsingRules.put(key, rule);
					} catch (GPSdException e) {
						throw new ConfigurationException(key, "invalid parser rule", e);
					}
				}

			}
            
			if (parsingRules != null) {
				logger.debug("GPSd Data Parser called");
//				dataParser = new GPSdDataParser(parsingRules);
				
				
			}
			
			
			if (messageListener != null) {

				logger.debug("Close previous message listener");

				messageListener.setInterrupted(true);
				try {
					messageListener.join();
				} catch (InterruptedException e) {
					logger.info("Previous message listener closing interrupted", e);
				}
			}
			
			
			
			if (simulate == true)
				connector = new GPSdSimulator();
			else 
				connector = new GPSd4JavaConnector(hostname);
			try {
				int count = 0;
				while ( !connector.isConnected() && connectionRetryCount > count ) {
					count++;
					connector.connect();
					if (!connector.isConnected()) { 
						if ( connectionRetryCount == count ) {
							throw new GPSdException ("Out of retries");
						}
						else 
						{
							Thread.sleep (connectionRetryTime) ;}
						}
				}
				
				messageListener = new MessageListener();
				messageListener.start();
				
			} catch (Exception e) {
				logger.error( "Error occured when connecting GPSD device", e);
				logger.warn("Closing GPSd message listener");

			}
			
		}
	}

	/**
	 * The MessageListener runs as a separate thread.
	 * 
	 * Thread listening message from Open Energy Monitoring devices and send
	 * updates to openHAB bus.
	 * 
	 */
	private class MessageListener extends Thread {

		private boolean interrupted = false;
		
		TPVObject location = new TPVObject();
		TPVObject previousLocation = new TPVObject();

		MessageListener() {
		}

		public void setInterrupted(boolean interrupted) {
			this.interrupted = interrupted;
			messageListener.interrupt();
		}

		@Override
		public void run() {

			logger.info("GPSd message listener started");
			
			long currentTimer = System.currentTimeMillis();
			long previousTimer = System.currentTimeMillis();
			long previousUpdate = System.currentTimeMillis();
			int locationAgeLimt = 300000; //Time in milliseconds we force location to be sent, even if not updated. 
			int  pollReturnCode = 0;
			boolean found = false;
			//boolean changed; // Controls weather we should force a new read due to a major change (like course change detection). 
			TPVObject newLocation = null;
			
			// as long as no interrupt is requested, continue running
			while (!interrupted && connector.isConnected() ) {
				//changed = false; //Clears any change detected in previous updates. 
				

				try {
					if (!shouldUpdate() && previousLocation.isValid ) { 
						Thread.sleep(2000);
						continue;
					}
					while ( (currentTimer - previousTimer) < getRefresh() ) {
						sleep (1000);
						newLocation = connector.receiveGPSObject();
						
						//Fix any invalid previous location.
						if (!previousLocation.isValid && newLocation.isValid) {
							previousLocation = newLocation;
						}
						
						//Detects course change above 20 degrees 
						if (newLocation != null && newLocation.isValid) {
							if ((Math.abs(newLocation.getCourse() - previousLocation.getCourse()) >  getCourseChangeThres())  && ( newLocation.getCourse() > 0 ) && ( previousLocation.getCourse() > 0 ) ) {
								logger.info ( "Location Changed above threshold. Previous Course was {}. New Course is {}", previousLocation.getCourse(), newLocation.getCourse());
								break;
								//changed = true;
							}
						} else { 
							//logger.warn("Location is null or invalid. Probably NOFIX");
							continue;
						}
						currentTimer = System.currentTimeMillis() ;
						//logger.trace("Gpsd looping");
					} // Add Code to detect change in direction. 
					
					logger.trace ("Location being updated");
					
				} catch (GPSdException exgps) { 
					logger.info("GPS seems not initialized. Waiting for first fix. ");
				}
				catch (Exception e1) {
					// TODO Auto-generated catch block
					logger.error("General Error at GPSd listener thread: {}", e1.toString());
					e1.printStackTrace();
				}
				try {
					previousTimer = currentTimer;
				

					//TPVObject newLocation = connector.receiveGPSObject();
					
					if (newLocation != null && newLocation.isValid() || location == null) { 
						logger.trace("Valid  location recieved from GPS.");
						/*if ( newLocation.equals(location)) { 
							logger.debug("Location did not change. Skipping for now");
							continue;
						} else {
							logger.debug("Updating location to {}", newLocation.toString());
						}*/
						previousLocation = location;
						location = newLocation;
					} else {
						logger.debug("Invalid Location recieved from GPS. Not updating location. ");
						// Add a force overtime if we stay too long with invalid gps dat
						if ( (currentTimer - previousUpdate) > locationAgeLimt ) {
							logger.trace("Continuing location update due to locationAgeLimt ");
						} else {
							continue;	
						}
					}
					
					previousUpdate = currentTimer;

					for (GPSdBindingProvider provider : providers) {
						for (String itemName : provider.getItemNames()) {
							org.openhab.core.types.State state = null;
							found = false;
							try {
								switch ( provider.getVariable(itemName).toLowerCase() ) {
								case "latitude":
									found = true;
									state = new DecimalType(location.getLatitude());
									break;
								case "longitude":
									found = true;
									state = new DecimalType(location.getLongitude());
									break;
								case "longitudeerror":
									found = true;
									state = new DecimalType(location.getLongitudeError());
									break;
								case "latitudeerror":
									found = true;
									state = new DecimalType(location.getLatitudeError());
									break;
								case "altitude":
									found = true;
									state = new DecimalType(location.getAltitude());
									break;
								case "altitudeerror":
									found = true;
									state = new DecimalType(location.getAltitudeError());
									break;
								case "speed":
									found = true;
									state = new DecimalType(location.getSpeed());
									break;
								case "speederror":
									found = true;
									state = new DecimalType(location.getSpeedError());
									break;
								case "nmea":
									found = true;
									state = new  StringType(location.getNmeastring());
									break;
								case "timestamp":
									found = true;
									state = new DecimalType(location.getTimestamp());
									break;
								case "tag":
									found = true;
									state = new StringType(location.getTag().toString());
									break;
								case "timestamperror":
									found = true;
									state = new DecimalType(location.getTimestampError());
									break;
								case "course":
									found = true;
									state = new DecimalType(location.getCourse());
									break;
								case "climbrate":
									found = true;
									state = new DecimalType(location.getClimbRate());
									break;
								case "courseerror":
									found = true;
									//state = new DecimalType(location.getCourseError());
									state = new DecimalType(-1);
									break;
								case "climbrateerror":
									found = true;
									state = new DecimalType(location.getClimbRate());
									break;
								case "mode":
									found = true;
									state = new StringType(location.getMode().toString());
									break;
								case "location":
									found = true;
									state = new StringType( String.valueOf(location.getLatitude()) + "," + String.valueOf(location.getLongitude()));
									break;
								}
							} catch (Exception e) {
								// TODO Auto-generated catch block
								logger.debug("Exception {}", e.toString());
							}											

							if (found) {
								   if ( state == null ) { 
									    logger.debug("Item {} found but value is invalid. Leaving previous value", itemName.toString());
								   		//state = new DecimalType("-1"); 
								   	   }
								   state = transformData(
								   provider.getTransformationType(itemName),
								   provider.getTransformationFunction(itemName),
								   state); 
		
							} else  { 
								logger.error("Invalid Item: {}", itemName.toString());
							}
							//logger.trace ("Item being parsed is {} with value {}", itemName.toLowerCase(), state.toString() );
							if (state != null) {
								eventPublisher.postUpdate(itemName, state);
							} 
							if (interrupted) {
								break;
							}
						}

					}
				}
				
				catch (Exception e) {

					logger.error(
							"Error occured when received data from GPSD device",
							e);

				}
			}
			
			logger.debug("GPSd Run Thread exited.");
			
			try {
				connector.disconnect();
			} catch (GPSdException e) {
				logger.error("Error occured when disconnecting form GPSd device",
						e);
			}

		}



	}
	private String replaceVariables(HashMap<String, Number> vals,
			String variable) {
		for (Entry<String, Number> entry : vals.entrySet()) {
			String key = entry.getKey();
			Object value = entry.getValue();

			variable = variable.replace(key, String.valueOf(value));

		}

		return variable;
	}

	/**
	 * Transform received data by Transformation service.
	 * 
	 */
	protected org.openhab.core.types.State transformData(
			String transformationType, String transformationFunction,
			org.openhab.core.types.State data) {
		
		if (transformationType != null && transformationFunction != null) {
			String transformedResponse = null;

			try {
				TransformationService transformationService = TransformationHelper
						.getTransformationService(
								GPSdActivator.getContext(),
								transformationType);
				if (transformationService != null) {
					transformedResponse = transformationService.transform(
							transformationFunction, String.valueOf(data));
				} else {
					logger.warn(
							"couldn't transform response because transformationService of type '{}' is unavailable",
							transformationType);
				}
			} catch (TransformationException te) {
				logger.error(
						"transformation throws exception [transformation type="
								+ transformationType
								+ ", transformation function="
								+ transformationFunction + ", response=" + data
								+ "]", te);
			}

			logger.debug("transformed response is '{}'", transformedResponse);

			if (transformedResponse != null) {
				return new DecimalType(transformedResponse);
			}
		}

		return data;
	}
	
	/**
	 * @return the refresh
	 */
	private int getRefresh() {
		return refresh;
	}

	/**
	 * @param refresh the refresh to set
	 */
	private void setRefresh(int refresh) {
		this.refresh = refresh;
	}

	/**
	 * @return the connectionRetryCount
	 */
	private  int getConnectionRetryCount() {
		return connectionRetryCount;
	}

	/**
	 * @param connectionRetryCount the connectionRetryCount to set
	 */
	private void setConnectionRetryCount(int connectionRetryCount) {
		this.connectionRetryCount = connectionRetryCount;
	}

	/**
	 * @return the connectionRetryTime
	 */
	private int getConnectionRetryTime() {
		return connectionRetryTime;
	}

	/**
	 * @param connectionRetryTime the connectionRetryTime to set
	 */
	private  void setConnectionRetryTime(int connectionRetryTime) {
		this.connectionRetryTime = connectionRetryTime;
	}


	public boolean isMovementTrack() {
		return movementTrack;
	}

	private boolean shouldUpdate() { 
		if (isMovementTrack() && !getMovementItem().isEmpty()) { 
			try {
				return itemRegistry.getItem(getMovementItem()).getState().toString() == "ON" ?  true:  false;
			} catch (ItemNotFoundException e) {
				// TODO Auto-generated catch block
				logger.trace ("Movement Tracking Item not found.");
				return false;
			}
		} else {
			return true; //Defaults to Update
		}
		
		
		
	}

	private void setMovementTrack(boolean movementTrack) {
		this.movementTrack = movementTrack;
	}


	public String getMovementItem() {
		return movementItem;
	}


	public void setMovementItem(String movementItem) {
		this.movementItem = movementItem;
	}
	
	public void setItemRegistry(ItemRegistry itemRegistry) {
		this.itemRegistry = itemRegistry;
	}

	public void unsetItemRegistry(ItemRegistry itemRegistry) {
		this.itemRegistry = null;
	}
	
	private double getCourseChangeThres() {
		// TODO Auto-generated method stub
		return this.maxChangeThres;
	}
	
	
}
