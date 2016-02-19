/**
 * Copyright (c) 2010-2015, openHAB.org and others.
 *
 * All rights reserved. This program and the accompanying materials
 * are made available under the terms of the Eclipse Public License v1.0
 * which accompanies this distribution, and is available at
 * http://www.eclipse.org/legal/epl-v10.html
 */
package org.openhab.binding.geolocation.internal;

import java.util.regex.Matcher;
import java.util.regex.Pattern;

import org.openhab.binding.geolocation.GeoLocationBindingProvider;
import org.openhab.core.binding.BindingConfig;
import org.openhab.core.items.Item;
import org.openhab.core.library.items.NumberItem;
import org.openhab.model.item.binding.AbstractGenericBindingProvider;
import org.openhab.model.item.binding.BindingConfigParseException;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

/**
 * <p>
 * This class can parse information from the generic binding format and provides
 * geoLocation binding information from it.
 * 
 * <p>
 * Examples for valid binding configuration strings:
 * 
 * <ul>
 * <li><code>locationItem="ItemName"</code></li>
 * </ul>
 * 
 * @author Julio Boehl
 * @since 1.7.0
 */
public class GeoLocationGenericBindingProvider extends
		AbstractGenericBindingProvider implements
		GeoLocationBindingProvider {

	/** RegEx to extract a transformation string <code>'(.*?)\((.*)\)'</code> */
	private static final Pattern REGEX_EXTRACT_PATTERN = Pattern
			.compile("(.*?)\\((.*)\\)");

	
	private static Logger logger = LoggerFactory
			.getLogger(GeoLocationActivator.class);
	/**
	 * {@inheritDoc}
	 */
	public String getBindingType() {
		return "geolocation";
	}

	/**
	 * @{inheritDoc
	 */
	@Override
	public void validateItemType(Item item, String bindingConfig)
			throws BindingConfigParseException {
		/*
		//logger.debug("validateItemType {}", item.toString());
		//logger.debug("validateItemType config {}", bindingConfig.toString() );
		if (!(item instanceof NumberItem)) {
			throw new BindingConfigParseException(
					"item '"
							+ item.getName()
							+ "' is of type '"
							+ item.getClass().getSimpleName()
							+ "', only NumberItems are allowed - please check your *.items configuration");
		}*/
	}

	/**
	 * {@inheritDoc}
	 */
	@Override
	public void processBindingConfiguration(String context, Item item,
			String bindingConfig) throws BindingConfigParseException {
		super.processBindingConfiguration(context, item, bindingConfig);

		GeoLocationBindingConfig config = new GeoLocationBindingConfig();

		String[] configParts = bindingConfig.trim().split(":");
   
		//logger.debug("processBindingConfiguration {}", item.toString());
		//logger.debug("processBindingConfiguration config {}", bindingConfig.toString() );
		
		if (configParts.length > 1) {
			throw new BindingConfigParseException(
					"GeoLocation binding must contain 1 part");
		}

		config.itemName = configParts[0].toString();
		logger.debug ("Setting GeoLocation source location item to {}", config.itemName);
		
        
		addBindingConfig(item, config);
	}

	/**
	 * Splits a transformation configuration string into its two parts - the
	 * transformation type and the function/pattern to apply.
	 * 
	 * @param transformation
	 *            the string to split
	 * @return a string array with exactly two entries for the type and the
	 *         function
	 */
	protected String[] splitTransformationConfig(String transformation) {
		Matcher matcher = REGEX_EXTRACT_PATTERN.matcher(transformation);

		if (!matcher.matches()) {
			throw new IllegalArgumentException(
					"given transformation function '"
							+ transformation
							+ "' does not follow the expected pattern '<function>(<pattern>)'");
		}
		matcher.reset();

		matcher.find();
		String type = matcher.group(1);
		String pattern = matcher.group(2);

		return new String[] { type, pattern };
	}

	class GeoLocationBindingConfig implements BindingConfig {
		public String itemName = null;

		@Override
		public String toString() {
			return "GeoLocationBindingConfigElement ["
					+ "item =" + itemName;
		}

	}

	@Override
	public String getVariable(String itemName) {
		//logger.debug("getVariable config {}", itemName.toString() );
		GeoLocationBindingConfig config = (GeoLocationBindingConfig) bindingConfigs
				.get(itemName);
		return config != null ? config.itemName : null;
	}

}
