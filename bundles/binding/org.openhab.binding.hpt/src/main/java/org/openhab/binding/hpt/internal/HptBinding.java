/**
 * Copyright (c) 2010-2015, openHAB.org and others.
 *
 * All rights reserved. This program and the accompanying materials
 * are made available under the terms of the Eclipse Public License v1.0
 * which accompanies this distribution, and is available at
 * http://www.eclipse.org/legal/epl-v10.html
 */
package org.openhab.binding.hpt.internal;

import java.util.Dictionary;
import java.util.Enumeration;
import java.util.HashMap;
import java.util.Timer;
import java.util.TimerTask;
import java.util.Map.Entry;

//import javax.xml.bind.DatatypeConverter;

//import net.astesana.javaluator.DoubleEvaluator;

import org.apache.commons.lang.StringUtils;
//import org.openhab.binding.gpsd.protocol.GPSdSerialConnector;
import org.openhab.binding.hpt.HptBindingProvider;
import org.openhab.core.binding.AbstractBinding;
import org.openhab.core.items.Item;
import org.openhab.core.items.ItemNotFoundException;
import org.openhab.core.items.ItemRegistry;
import org.openhab.core.library.types.DecimalType;
import org.openhab.core.library.types.PercentType;
import org.openhab.core.library.types.StringType;
import org.openhab.core.transform.TransformationException;
import org.openhab.core.transform.TransformationHelper;
import org.openhab.core.transform.TransformationService;
import org.openhab.core.types.State;
import org.osgi.framework.BundleContext;
import org.osgi.framework.FrameworkUtil;
import org.osgi.framework.ServiceReference;
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
public class HptBinding extends
	AbstractBinding<HptBindingProvider> implements
		ManagedService {

	private static final Logger logger = LoggerFactory
			.getLogger(HptBinding.class);

	/* configuration variables for communication */
//	private int udpPort = 9997;
//	private String serialPort = null;
	private long refreshInterval = 1000;
	private Timer timer = new Timer();
	private TimerTask task = new HptTimer(this);
	
	private Item item;
	
	private State state;

	
	/** Thread to handle messages from GPSd Server devices */


	public HptBinding() {
	}

	public void activate() {
		logger.debug("Activate High Precision Timer binding");
	}

	public void deactivate() {
		logger.debug("Deactivate High Precision Timer binding");
		timer.cancel();
	}
	
	protected String getName() {
	    	return "High Precision Timer Refresh Service";
	 }

	/**
	 * @{inheritDoc
	 */
	@Override
	public void updated(Dictionary  config)
			throws ConfigurationException {

		logger.debug("High Precision Timer Updated");
		if (config != null) {
			logger.debug("High Precision Timer Configuration not null");
						
			String refreshIntervalString = (String) config.get("refresh");

			if (StringUtils.isNotBlank(refreshIntervalString)) {
				refreshInterval = Long.parseLong(refreshIntervalString);
			}

			this.item = (Item) config.get("item");
			if (this.item != null) {
				logger.error("No Item name configured");
			//	setProperlyConfigured(false);
				throw new ConfigurationException("Item", "The device name can't be empty");
			} else {
				//setNewDeviceName(deviceName);
			}
			// setProperlyConfigured(true);
			timer.scheduleAtFixedRate(task, refreshInterval, refreshInterval);
			
			
		/*	HashMap<String, OBDParserRule> parsingRules = new HashMap<String, OBDParserRule>();

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
				}
				else {

					// process all data parsing rules
				try {
					  OBDParserRule rule = new OBDParserRule(value);
					  parsingRules.put(key, rule);
				 } catch (HptException e) {
						throw new ConfigurationException(key, "invalid parser rule", e);
				 }
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
			
			messageListener = new MessageListener( this );
			messageListener.start(); */
			
			
		} else 
		{
			timer.scheduleAtFixedRate(task, 1000, 1000 );			
			
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
								HptActivator.getContext(),
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

	public void updateValue (long currentTimeMillis) {
		// TODO Auto-generated method stub
		if (item==null) {
			BundleContext bundleContext = FrameworkUtil.getBundle(this.getClass()).getBundleContext();
			ServiceReference<?> serviceReference2 = bundleContext.getServiceReference(ItemRegistry.class.getName());
			if (serviceReference2 != null) {
				ItemRegistry itemregistry = (ItemRegistry) bundleContext.getService(serviceReference2);
				try {
					item = itemregistry.getItem("t");
					
					this.providesBindingFor("timer");
					State state = new DecimalType(currentTimeMillis);;
					
					
					
					logger.warn("Forcing initialization of Item {}", this.item.getName());
					
					eventPublisher.postUpdate(item.getName(), state);
					
				} catch (ItemNotFoundException e) {
					// TODO Auto-generated catch block
					e.printStackTrace();
				}
			} else
				logger.error("itemregistry=null");
			//ItemRegistry registry = (ItemRegistry) bundleContext.getServiceReference(ItemRegistry.class.getName());

			
		} else {
			//logger.info("Item {} set to {}", this.item.getName(), currentTimeMillis);
			state = new DecimalType(currentTimeMillis);;
			eventPublisher.postUpdate(item.getName(), state);
		}
	}
}
