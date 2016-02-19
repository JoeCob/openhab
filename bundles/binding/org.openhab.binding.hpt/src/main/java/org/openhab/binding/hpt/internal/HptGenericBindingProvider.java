/**
 * Copyright (c) 2010-2015, openHAB.org and others.
 *
 * All rights reserved. This program and the accompanying materials
 * are made available under the terms of the Eclipse Public License v1.0
 * which accompanies this distribution, and is available at
 * http://www.eclipse.org/legal/epl-v10.html
 */
package org.openhab.binding.hpt.internal;

import java.util.regex.Matcher;
import java.util.regex.Pattern;

//import org.openhab.binding.openenergymonitor.OpenEnergyMonitorBindingProvider;
import org.openhab.binding.hpt.HptBindingProvider;
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
public class HptGenericBindingProvider extends
		AbstractGenericBindingProvider implements
		HptBindingProvider {

	/** RegEx to extract a transformation string <code>'(.*?)\((.*)\)'</code> */
	private static final Pattern REGEX_EXTRACT_PATTERN = Pattern
			.compile("(.*?)\\((.*)\\)");

	
	private static Logger logger = LoggerFactory
			.getLogger(HptActivator.class);
	/**
	 * {@inheritDoc}
	 */
	public String getBindingType() {
		return "hpt";
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

		HptBindingConfig config = new HptBindingConfig();

		String[] configParts = bindingConfig.trim().split(":");
   
		//logger.debug("processBindingConfiguration {}", item.toString());
		//logger.debug("processBindingConfiguration config {}", bindingConfig.toString() );
		
		if (configParts.length > 1) {
			throw new BindingConfigParseException(
					"OBD binding must contain 1-2 parts separated by ':'");
		}

		config.name = item.getName();
		config.interval = Integer.parseInt(configParts[0]);


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

	class HptBindingConfig implements BindingConfig {
		public String name = null;
		public int  interval = 1000;
		
		@Override
		public String toString() {
			return "HptBindingConfigElement ["
					+ "item =" + name 
					+ ", refresh =" + String.valueOf(interval);
		}

	}

	@Override
	public String getVariable(String itemName) {
		//logger.debug("getVariable config {}", itemName.toString() );
		HptBindingConfig config = (HptBindingConfig) bindingConfigs
				.get(itemName);
		return config != null ? config.name : null;
	}

}
