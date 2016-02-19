/**
 * Copyright (c) 2010-2015, openHAB.org and others.
 *
 * All rights reserved. This program and the accompanying materials
 * are made available under the terms of the Eclipse Public License v1.0
 * which accompanies this distribution, and is available at
 * http://www.eclipse.org/legal/epl-v10.html
 */
package org.openhab.binding.mqttdisplay.internal;

import java.util.Locale;
import java.util.Map;
import java.util.TimeZone;

import org.openhab.binding.mqttdisplay.MqttDisplayBindingProvider;
import org.openhab.core.binding.BindingConfig;
import org.openhab.core.items.Item;
import org.openhab.core.library.items.StringItem;
import org.openhab.core.types.State;
import org.openhab.model.item.binding.AbstractGenericBindingProvider;
import org.openhab.model.item.binding.BindingConfigParseException;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;


/**
 * <p>This class can parse information from the generic binding format and 
 * provides NTP binding information from it. It registers as a 
 * {@link MqttDisplayBindingProvider} service as well.</p>
 * 
 * <p>Here are some examples for valid binding configuration strings:
 * <ul>
 * 	<li><code>{ ntp="Europe/Berlin:de_DE" }</code>
 * 	<li><code>{ ntp="Europe/Berlin" }</code>
 * 	<li><code>{ ntp="" }</code>
 * </ul>
 * 
 * @author Thomas.Eichstaedt-Engelen
 * 
 * @since 0.8.0
 */
public class MqttDisplayGenericBindingProvider extends AbstractGenericBindingProvider implements MqttDisplayBindingProvider {

	/**
	 * {@inheritDoc}
	 */
	
	private static final Logger logger = LoggerFactory.getLogger(MqttDisplayBinding.class);
	
	public String getBindingType() {
		return "mqttdisplay";
	}
	
	/**
	 * {@inheritDoc}
	 */
	@Override
	public void validateItemType(Item item, String bindingConfig) throws BindingConfigParseException {
		if (!(item instanceof  StringItem )) {
			throw new BindingConfigParseException("item '" + item.getName()
					+ "' is of type '" + item.getClass().getSimpleName()
					+ "', only StringItem are allowed - please check your *.items configuration");
		}
	}

	/**
	 * {@inheritDoc}
	 */
	@Override
	public void processBindingConfiguration(String context, Item item, String bindingConfig) throws BindingConfigParseException {
		
		super.processBindingConfiguration(context, item, bindingConfig);
		MqttDisplayBindingConfig config = new MqttDisplayBindingConfig();
		
		String[] configParts = bindingConfig.trim().split(";");
		
		logger.debug("Parsing MQTTDISPLAY setup for {}. Value is {}", item.getName(), bindingConfig.trim());
		
		if (configParts.length < 2 ) {
			throw new BindingConfigParseException("MqTT Display binding configuration must not contain at LEAST  two parts. It has {} parts only. " + configParts.length  );
		} else {
			//config.screen = Integer.valueOf((configParts[0]));
			//config.line = Integer.valueOf((configParts[1]));
			config.content = bindingConfig.toString();
		} 

		addBindingConfig(item, config);
	}
	
	
	/**
	 * {@inheritDoc}
	 */

	
	public int getScreen (String itemName) {
		MqttDisplayBindingConfig config = (MqttDisplayBindingConfig) bindingConfigs.get(itemName);
		return config != null ? config.screen : 1;
	}
	

	/**
	 * {@inheritDoc}
	 */

	
	public int getLine (String itemName) {
		MqttDisplayBindingConfig config = (MqttDisplayBindingConfig) bindingConfigs.get(itemName);
		return config != null ? config.screen : 1;
	}
	
	public Map<String, BindingConfig> configs = this.bindingConfigs;

	@Override
	public Boolean autoUpdate(String itemName) {
		// TODO Auto-generated method stub
		return null;
	}

	@Override
	public MqttDisplayBindingConfig getBindingConfig(String itemName) {
		// TODO Auto-generated method stub
		return (MqttDisplayBindingConfig) bindingConfigs.get(itemName);
		//return null;
	}
	/**
	 * This is an internal data structure to store information from the binding
	 * config strings and use it to answer the requests to the NTP
	 * binding provider.
	 */

}
