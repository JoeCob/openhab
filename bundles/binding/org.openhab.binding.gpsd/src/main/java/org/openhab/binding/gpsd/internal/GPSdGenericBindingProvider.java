/**
 * Copyright (c) 2010-2015, openHAB.org and others.
 *
 * All rights reserved. This program and the accompanying materials
 * are made available under the terms of the Eclipse Public License v1.0
 * which accompanies this distribution, and is available at
 * http://www.eclipse.org/legal/epl-v10.html
 */
package org.openhab.binding.gpsd.internal;

import java.util.regex.Matcher;
import java.util.regex.Pattern;

//import org.openhab.binding.openenergymonitor.OpenEnergyMonitorBindingProvider;
import org.openhab.binding.gpsd.GPSdBindingProvider;
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
 * Open Energy Monitor binding information from it.
 * 
 * <p>
 * Examples for valid binding configuration strings:
 * 
 * <ul>
 * <li><code>openenergymonitor="realPower"</code></li>
 * <li><code>openenergymonitor="phase1Current:JS(divideby100.js)"</code></li>
 * <li><code>openenergymonitor="phase1RealPower+phase2RealPower+phase3RealPower"</code></li>
 * <li><code>openenergymonitor="phase1Current+phase2Current+phase3Current:JS(divideby100.js)"</code></li>
 * <li><code>openenergymonitor="phase1RealPower+phase2RealPower+phase3RealPower"</code></li>
 * </ul>
 * 
 * @author Pauli Anttila
 * @since 1.4.0
 */
public class GPSdGenericBindingProvider extends
		AbstractGenericBindingProvider implements
		GPSdBindingProvider {

	/** RegEx to extract a transformation string <code>'(.*?)\((.*)\)'</code> */
	private static final Pattern REGEX_EXTRACT_PATTERN = Pattern
			.compile("(.*?)\\((.*)\\)");

	
	private static Logger logger = LoggerFactory
			.getLogger(GPSdActivator.class);
	/**
	 * {@inheritDoc}
	 */
	public String getBindingType() {
		return "gpsd";
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

		GPSdBindingConfig config = new GPSdBindingConfig();

		String[] configParts = bindingConfig.trim().split(":");
   
		//logger.debug("processBindingConfiguration {}", item.toString());
		//logger.debug("processBindingConfiguration config {}", bindingConfig.toString() );
		
		if (configParts.length > 1) {
			throw new BindingConfigParseException(
					"GPSd binding must contain 1-2 parts separated by ':'");
		}

		config.variable = configParts[0].trim();

		if (configParts.length == 2) {
			String[] parts = splitTransformationConfig(configParts[1].trim());
			config.transformationType = parts[0];
			config.transformationFunction = parts[1];
		} else {
			config.transformationType = null;
			config.transformationFunction = null;
		}

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

	class GPSdBindingConfig implements BindingConfig {
		public String variable = null;
		String transformationType = null;
		String transformationFunction = null;

		@Override
		public String toString() {
			return "GPSdBindingConfigElement ["
					+ "variable=" + variable 
					+ ", transformation type=" + transformationType
					+ ", transformation function=" + transformationFunction
					+ "]";
		}

	}

	@Override
	public String getVariable(String itemName) {
		//logger.debug("getVariable config {}", itemName.toString() );
		GPSdBindingConfig config = (GPSdBindingConfig) bindingConfigs
				.get(itemName);
		return config != null ? config.variable : null;
	}

	@Override
	public String getTransformationType(String itemName) {
		//logger.debug("getTransformationType {}", itemName.toString() );
		GPSdBindingConfig config = (GPSdBindingConfig) bindingConfigs
				.get(itemName);
		return config != null ? config.transformationType : null;
	}

	@Override
	public String getTransformationFunction(String itemName) {
		GPSdBindingConfig config = (GPSdBindingConfig) bindingConfigs
				.get(itemName);
		return config != null ? config.transformationFunction : null;
	}

}
