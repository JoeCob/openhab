/**
 * Copyright (c) 2010-2015, openHAB.org and others.
 *
 * All rights reserved. This program and the accompanying materials
 * are made available under the terms of the Eclipse Public License v1.0
 * which accompanies this distribution, and is available at
 * http://www.eclipse.org/legal/epl-v10.html
 */
package org.openhab.binding.obd.internal;

import java.util.Dictionary;
import java.util.Enumeration;
import java.util.HashMap;
import java.util.Timer;
import java.util.Map.Entry;

//import javax.xml.bind.DatatypeConverter;

//import net.astesana.javaluator.DoubleEvaluator;

import org.apache.commons.lang.StringUtils;
//import org.openhab.binding.gpsd.protocol.GPSdSerialConnector;
import org.openhab.binding.obd.OBDBindingProvider;
import org.openhab.binding.obd.protocol.OBDConnectionHelper;
import org.openhab.binding.obd.protocol.OBDJavaConnector;
import org.openhab.binding.obd.protocol.OBDConnector;
import org.openhab.binding.obd.protocol.OBDDataParser;
import org.openhab.binding.obd.protocol.OBDObject;
import org.openhab.binding.obd.protocol.OBDParserRule;
import org.openhab.binding.obd.protocol.OBDSimulator;
import org.openhab.core.binding.AbstractBinding;
import org.openhab.core.library.types.DecimalType;
import org.openhab.core.library.types.PercentType;
import org.openhab.core.library.types.StringType;
import org.openhab.core.transform.TransformationException;
import org.openhab.core.transform.TransformationHelper;
import org.openhab.core.transform.TransformationService;
import org.osgi.service.cm.ConfigurationException;
import org.osgi.service.cm.ManagedService;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;




/**
 * 
 * Binding to receive data from Open Energy Monitor devices.
 * 
 * @author Pauli Anttila
 * @since 1.4.0
 */
public class OBDBinding extends
	AbstractBinding<OBDBindingProvider> implements
		ManagedService {

	private static final Logger logger = LoggerFactory
			.getLogger(OBDBinding.class);

	/* configuration variables for communication */
//	private int udpPort = 9997;
//	private String serialPort = null;
	private boolean simulate = false;
	private String  device = "/dev/ttyUSB01";
	private int speed = 9600;
	private int refresh = 500;
	private int retry = 60000;
	private int delay = 50;

	private OBDDataParser dataParser = null;
	private Timer reconnectTimer = null;
	private boolean running = false;

	/** Thread to handle messages from GPSd Server devices */
	private MessageListener messageListener = null;

	public OBDBinding() {
	}

	public void activate() {
		logger.debug("Activate OBD binding");
	}

	public void deactivate() {
		logger.debug("Deactivate OBD binding");
		messageListener.setInterrupted(true);
	}
	
	protected String getName() {
	    	return "OBD Refresh Service";
	 }

	/**
	 * @{inheritDoc
	 */
	@Override
	public synchronized void updated(Dictionary  config)
			throws ConfigurationException {

		logger.debug("OBD Updated");
		if (config != null) {
			logger.debug("OBD Configuration not null");
			if (config != null) {
				String deviceString = (String) config.get("device");
				if (StringUtils.isNotBlank(deviceString)) {
					device = deviceString;
					logger.trace("device setting is {} ", device);
				}
				
				String speedString = (String) config.get("speed");
				if (StringUtils.isNotBlank(speedString)) {
					speed = Integer.parseInt(speedString);
					logger.trace("speed setting is{} ", speed);
				}
				
				String readDelay = (String) config.get("delay");
				if (StringUtils.isNotBlank(readDelay)) {
					delay = Integer.parseInt(readDelay);
					logger.trace("comm delay setting is {} ", delay);
				}
				
				//setProperlyConfigured(true);
				
			}
			
			HashMap<String, OBDParserRule> parsingRules = new HashMap<String, OBDParserRule>();

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

				if ("device".equals(key)) {
					if (StringUtils.isNotBlank(value)) {
						device = value;
						logger.debug("Using serial device {}", device.toString());
					}
				} else if ("speed".equals(key)) {
					speed = Integer.parseInt(value);
					logger.debug("Speed  set to {}", speed);
				} else if ("refresh".equals(key)) {
					refresh = Integer.parseInt(value);
					logger.debug("Refresh  set to {}", refresh);
				} else if ("retry".equals(key)) {
						retry = Integer.parseInt(value);
						logger.debug("Retry set to {}", retry );
				}  else if ("delay".equals(key)) {
					delay = Integer.parseInt(value);
					logger.debug("Delay set to {}", retry );
				}
				else {

					// process all data parsing rules
				try {
					  OBDParserRule rule = new OBDParserRule(value);
					  parsingRules.put(key, rule);
				 } catch (OBDException e) {
						throw new ConfigurationException(key, "invalid parser rule", e);
				 }
				 continue;
				}

			}
            
			if (parsingRules != null) {
				logger.debug("OBD Data Parser called");
				dataParser = new OBDDataParser(parsingRules);
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
			
			if (!running) { 
				messageListener = new MessageListener( this );
				messageListener.start();
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
        private OBDBinding binding = null;
        private int reInitCount;
        
		MessageListener( OBDBinding binding) {
			this.binding = binding;
		}

		public void setInterrupted(boolean interrupted) {
			this.interrupted = interrupted;
			messageListener.interrupt();
		}

		@Override
		public void run() {

			logger.debug("OBD data pooler  started");
			running = true;

			OBDConnector connector;

			while (!interrupted) { 
				if (simulate == true)
					connector = new OBDSimulator();
				else 
					connector = new OBDJavaConnector(device, speed, delay);
				try {
					while ((connector == null || !connector.isConnected()) && !interrupted) {
						connector.connect();
						if (!connector.isConnected()) { Thread.sleep(retry) ;}
					}
				} catch (Exception e) {
					logger.error( "Error occured when connecting OBD device", e);
					logger.warn("Closing OBD message listener");

					OBDConnectionHelper helper = new OBDConnectionHelper( binding );
					Timer reconnectTimer = new Timer(true);
					reconnectTimer.schedule(helper, 10000);

					// exit
					interrupted = true;
				}
				
				reInitCount = 0;
				int retryCount = 0;
				long previousTimer = System.currentTimeMillis();

				// as long as we are connected, continue running
				try {
					while ( connector.isConnected() ) {

						long currentTimer = System.currentTimeMillis();
					
						
						int poolReturnCode = -1;

						// as long as we are connected and not interruped is requested, continue running

						try {
							try {
								while ( (currentTimer - previousTimer) < refresh ) {
									sleep (refresh - (currentTimer - previousTimer));
									currentTimer = System.currentTimeMillis() ;
								}
							} catch (InterruptedException e1) {
								// TODO Auto-generated catch block
								logger.error("General Error at OBD listener thread: {}", e1.toString());	
							}
													
							
							logger.debug("Timing - Pooling at {}. Previous took {}", currentTimer, currentTimer - previousTimer );
							previousTimer = currentTimer; //previousTimer marks when the previous read started. 
							
							poolReturnCode = connector.poll();
							

							logger.debug("OBD Pooling by binding returned {}", poolReturnCode);
							switch (poolReturnCode) {
							case -1: {
								logger.debug("Poll returned false. Sleeping 60 seconds.");
								sleep (15000);
								if (retryCount++ < 4) { 
									logger.debug("Poll returned false. Retrying." );
								}else {
									logger.debug("Poll returned false after 10 trys. Disconnecting." );
									connector.disconnect();
									sleep (30000);
								}
								continue;
							}
							case -2: {
								logger.debug("Poll returned Unable to Connect. Waiting 60 secs and retrying." );
								//Thread.sleep(30000);
								//connector.disconnect();
								Thread.sleep(60000);
								continue;}
							case 1: { 
								if ( reInitCount > 4 ) { 
									logger.info("Too many NODATA. FastInit not efective. Trying fullInit at OBD due to too many NO DATA errors");
									//Thread.sleep(30000);
									connector.disconnect();
									reInitCount = 0;
								} else {
									logger.debug("NO DATA. Trying adapter fast reInit in 30secs. Retry # {}.", reInitCount );
									Thread.sleep(15000);
									connector.fastInit() ;
									reInitCount++;
								}
								continue;
								}
							case 0: {
								retryCount = 0;
								break;
							}
							}
								
							OBDObject data = connector.receiveOBDObject();

							double beginItemMeasurement = 0;
							
							for (OBDBindingProvider provider : providers) {
								for (String itemName : provider.getItemNames()) {
									org.openhab.core.types.State state = null;
									boolean found = false;
									beginItemMeasurement  = System.currentTimeMillis();
									
									logger.debug("Timing - Item being parsed is {}.", provider.getVariable(itemName).toLowerCase() );

									
									try {

										switch ( provider.getVariable(itemName).toLowerCase() ) {
										case "enginerpm":
											state = new DecimalType(data.getEngineRpm());
											found = true;
											break;
										case "maf":
											state = new DecimalType(data.getMaf());
											found = true;
											break;			
										case "map":
											state = new DecimalType(data.getIntakeManifoldPressure());
											found = true;
											break;										
										case "oxygen1voltage":
											state = new DecimalType(data.getOxygenSensor1Voltage());
											found = true;
											break;
										case "oxygen1percent":
											state = new DecimalType(data.getOxygenSensor1Percent());
											found = true;
											break;
										case "engineload":
											state = new DecimalType(data.getEngineLoad());
											found = true;
											break;
										case "airintaketemp":
											state = new DecimalType(data.getAirIntakeTemp());
											found = true;
											break;
										case "ambientairtemp":
											state = new DecimalType(data.getAmbientAirTemp());
											found = true;
											break;
										case "mpg":
											state = new DecimalType(data.getMPG());
											found = true;
											break;
										case "mpglt":
											state = new DecimalType(data.getMPGLongTerm());
											found = true;
											break;
										case "kml":
											state = new DecimalType(data.getKML());
											found = true;
											break;
										case "kmllt":
											state = new DecimalType(data.getKMLLongTerm());
											found = true;
											break;
										case "fueltype":
											state = new DecimalType(data.getFuelType());
											found = true;
											break;
										case "fuelconsumption":
											state = new DecimalType(data.getFuelConsumption());
											found = true;
											break;			
										case "enginecoolanttemp":
											state = new DecimalType(data.getEngineCoolantTemp());
											found = true;
											break;	
										case "throttle":
											state = new DecimalType(data.getThrottle());
											found = true;
											break;		
										case "fuellevel":
											state = new DecimalType(data.getFuelLevel());
											found = true;
											break;		
										case "speed":
											state = new DecimalType(data.getSpeed());
											found = true;
											break;		

										}
										logger.debug("Timing  {} result  {} runtime {} miliseconds", provider.getVariable(itemName).toLowerCase(), state.toString(), System.currentTimeMillis() - beginItemMeasurement );
									} catch (Exception e) {
										// TODO Auto-generated catch block
										logger.error("Error setting OBD value for {} : {}", itemName.toLowerCase() , e.toString());
										continue;
									}											



									if (found) {
										if ( state == null ) 
										{ state = new DecimalType("-1"); }
										state = transformData(
												provider.getTransformationType(itemName),
												provider.getTransformationFunction(itemName),
												state);
										eventPublisher.postUpdate(itemName, state);
										
									} else  { 
										logger.error("Invalid Item: {}", itemName.toString());
									}
									if (interrupted) {
										break;
									}
								}

							}
						}

						catch (Exception e) {

							logger.error(
									"Error occured when received data from OBD device",
									e);

						}
						
					}
				} catch (OBDException e1) {
					// TODO Auto-generated catch block
					e1.printStackTrace();
				}
				try {
					connector.disconnect();
				} catch (OBDException e) {
					logger.error("Error occured when disconnecting form OBDII  device",e);
				}
				connector = null;
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
								OBDActivator.getContext(),
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
}
