/**
 * Copyright (c) 2010-2015, openHAB.org and others.
 *
 * All rights reserved. This program and the accompanying materials
 * are made available under the terms of the Eclipse Public License v1.0
 * which accompanies this distribution, and is available at
 * http://www.eclipse.org/legal/epl-v10.html
 */
package org.openhab.binding.geolocation.internal;

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
import org.openhab.binding.geolocation.GeoLocationBindingProvider;
import org.openhab.core.binding.AbstractBinding;
import org.openhab.core.items.GenericItem;
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
public class GeoLocationBinding extends
	AbstractBinding<GeoLocationBindingProvider> implements
		ManagedService {

	private static final Logger logger = LoggerFactory
			.getLogger(GeoLocationBinding.class);


	
	private Item item;
	private GenericItem location;

	private ItemRegistry itemRegistry;
	private GeoLocationListener listener;
	
	/** Thread to handle messages from GPSd Server devices */


	public GeoLocationBinding() {
	}

	public void activate() {
		logger.debug("Activate GeoLocation binding");
	}

	public void deactivate() {
		logger.debug("Deactivate GeoLocation binding");
	}
	
	protected String getName() {
	    	return "GeoLocation Refresh Service";
	 }

	/**
	 * @{inheritDoc
	 */
	@Override
	public void updated(Dictionary  config)
			throws ConfigurationException {

		logger.debug("GeoLocation Updated");
		if (config != null) {
			logger.debug("GeoLocation Configuration not null");
						
			String location_item_name = (String) config.get("location_item");

			if (StringUtils.isNotBlank(location_item_name)) {
				try {
					listener = new GeoLocationListener (this);
					location = (GenericItem) itemRegistry.getItem(location_item_name);
					location.addStateChangeListener(listener);
					logger.debug ("Registered listener for changes on {}", location_item_name );
					
				} catch (ItemNotFoundException e) {
					// TODO Auto-generated catch block
					logger.debug("Location item not found in registry");;
				}
			}
		} 
	}

	/*private String replaceVariables(HashMap<String, Number> vals,
			String variable) {
		for (Entry<String, Number> entry : vals.entrySet()) {
			String key = entry.getKey();
			Object value = entry.getValue();

			variable = variable.replace(key, String.valueOf(value));

		}

		return variable;
	}*/

	/**
	 * Transform received data by Transformation service.
	 * 
	 */
	/*protected org.openhab.core.types.State transformData(
			String transformationType, String transformationFunction,
			org.openhab.core.types.State data) {
		
		if (transformationType != null && transformationFunction != null) {
			String transformedResponse = null;

			try {
				TransformationService transformationService = TransformationHelper
						.getTransformationService(
								GeoLocationActivator.getContext(),
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
	} */

	public void updateValue (GeoLocation location) {
		// TODO Auto-generated method stub
		
		/*	private int place_id;
		private int osm_id;
		private String osm_type = null;
		private String display_name = null;
		private String road_name;
		private String city;
		private String county;
		private String state_district;
		private String state;
		private String country;
		private String country_code;
		private int lanes;
		private boolean oneway;
		private String surface_type;
		private int maxspeed;
		 */
		
		for (GeoLocationBindingProvider provider : providers) {
			for (String itemName : provider.getItemNames()) {
				try {
					logger.debug ( "Updating {}", itemName.toLowerCase()  );
					switch ( provider.getVariable(itemName).toLowerCase() ) {
					case "maxspeed":
						    logger.debug ( "Updating maxspeed {} with {}", itemName, location.getMaxspeed());
							eventPublisher.postUpdate(itemName, new DecimalType(location.getMaxspeed()));
							break;
					case "streetname":
						logger.debug 	( "Updating streetname {} with {}", itemName, location.getRoad_name());
						eventPublisher.postUpdate(itemName, new StringType(location.getRoad_name()));
					break;
					case "house_number":
						logger.debug 	( "Updating house_number {} with {}", itemName, location.getHouse_number());
						eventPublisher.postUpdate(itemName, new DecimalType(location.getHouse_number() ) );
					break;
					case "suburb":
						logger.debug 	( "Updating suburb {} with {}", itemName, location.getSuburb());
						eventPublisher.postUpdate(itemName, new StringType(location.getSuburb() ) );
					break;
					case "display_name":
						logger.debug 	( "Updating display_name {} with {}", itemName, location.getDisplay_name());
						eventPublisher.postUpdate(itemName, new StringType(location.getDisplay_name() ) );
					break;
					case "county":
						logger.debug 	( "Updating county {} with {}", itemName, location.getCounty());
						eventPublisher.postUpdate(itemName, new StringType(location.getCounty() ) );
					break;
					
				} 
				} catch (Exception e) {
					// TODO Auto-generated catch block
					logger.debug("Exception {} while updating {}", e.toString(), itemName);
				}	
			}
		}

	
	}
	
	
	public void setItemRegistry(ItemRegistry itemRegistry) {
		this.itemRegistry = itemRegistry;
	}

	public void unsetItemRegistry(ItemRegistry itemRegistry) {
		this.itemRegistry = null;
	}
	
	
}
