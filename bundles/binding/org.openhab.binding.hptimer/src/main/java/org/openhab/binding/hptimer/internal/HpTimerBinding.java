/**
 * Copyright (c) 2010-2015, openHAB.org and others.
 *
 * All rights reserved. This program and the accompanying materials
 * are made available under the terms of the Eclipse Public License v1.0
 * which accompanies this distribution, and is available at
 * http://www.eclipse.org/legal/epl-v10.html
 */
package org.openhab.binding.hptimer.internal;

import java.util.HashMap;
import java.util.HashSet;
import java.util.Map;
import java.util.Set;

import org.openhab.core.binding.AbstractBinding;
import org.openhab.core.events.AbstractEventSubscriber;
import org.openhab.core.events.EventPublisher;
import org.openhab.core.items.Item;
import org.openhab.core.library.items.StringItem;
import org.openhab.core.library.items.SwitchItem;
import org.openhab.core.library.types.StringType;
import org.openhab.core.types.Command;
import org.openhab.core.types.State;
import org.openhab.model.item.binding.BindingConfigParseException;
import org.openhab.model.item.binding.BindingConfigReader;

/**
 * <p>This class implements a binding of serial devices to openHAB.
 * The binding configurations are provided by the {@link GenericItemProvider}.</p>
 * 
 * <p>The format of the binding configuration is simple and looks like this:</p>
 * serial="&lt;port&gt;" where &lt;port&gt; is the identification of the serial port on the host system, e.g.
 * "COM1" on Windows, "/dev/ttyS0" on Linux or "/dev/tty.PL2303-0000103D" on Mac
 * <p>Switch items with this binding will receive an ON-OFF update on the bus, whenever data becomes available on the serial interface<br/>
 * String items will receive the submitted data in form of a string value as a status update, while openHAB commands to a Switch item is
 * sent out as data through the serial interface.</p>
 * 
 * @author Kai Kreuzer
 *
 */
public class HpTimerBinding extends AbstractBinding implements BindingConfigReader  {

	private Map<String, HpTimerDevice> hpTimerDevices = new HashMap<String, HpTimerDevice>();

	/** stores information about the which items are associated to which port. The map has this content structure: itemname -> port */ 
	private Map<String, String> itemMap = new HashMap<String, String>();
	
	/** stores information about the context of items. The map has this content structure: context -> Set of itemNames */ 
	private Map<String, Set<String>> contextMap = new HashMap<String, Set<String>>();

	private EventPublisher eventPublisher = null;
	
	public void setEventPublisher(EventPublisher eventPublisher) {
		this.eventPublisher = eventPublisher;
		
		for(HpTimerDevice hpTimerDevice : hpTimerDevices.values()) {
			hpTimerDevice.setEventPublisher(eventPublisher);
		}
	}
	
	public void unsetEventPublisher(EventPublisher eventPublisher) {
		this.eventPublisher = null;

		for(HpTimerDevice hpTimerDevice : hpTimerDevices.values()) {
			hpTimerDevice.setEventPublisher(null);
		}
	}
	

	/**
	 * {@inheritDoc}
	 */
	public void receiveUpdate(String itemName, State newStatus) {
		// ignore any updates
	}

	/**
	 * {@inheritDoc}
	 */
	public String getBindingType() {
		return "hptimer";
	}

	/**
	 * {@inheritDoc}
	 */
	public void validateItemType(Item item, String bindingConfig) throws BindingConfigParseException {
		if (!(item instanceof Number )) {
			throw new BindingConfigParseException("item '" + item.getName()
					+ "' is of type '" + item.getClass().getSimpleName()
					+ "', only Number ist allowed - please check your *.items configuration");
		}
	}
	
	/**
	 * {@inheritDoc}
	 */
	public void processBindingConfiguration(String context, Item item, String bindingConfig) throws BindingConfigParseException {
		String portConfig[] = bindingConfig.split("@");
        
        //String name = portConfig[0];
        String updateFreq = portConfig[0];

  
        
        HpTimerDevice hpTimerDevice = hpTimerDevices.get(item.getName());
        
		if (hpTimerDevice == null) {
              hpTimerDevice = new HpTimerDevice(item.getName(),  Integer.parseInt(updateFreq) );

			itemMap.put(item.getName(),"singleTimer");
			
			try {
				hpTimerDevice.initialize();
				hpTimerDevices.put(item.getName(), hpTimerDevice);
				
			} catch (InitializationException e) {
				// TODO Auto-generated catch block
				e.printStackTrace();
			}
			
			
		}
	
	}

	/**
	 * {@inheritDoc}
	 */
	public void removeConfigurations(String context) {
		Set<String> itemNames = contextMap.get(context);
		if(itemNames!=null) {
			for(String itemName : itemNames) {
				// we remove all information in the serial devices
				HpTimerDevice hpTimerDevice = hpTimerDevices.get(itemMap.get(itemName));
				itemMap.remove(itemName);
				if(hpTimerDevice==null) {
					continue;
				}
				if(itemName.equals(hpTimerDevice.getStringItemName())) {
					hpTimerDevice.setStringItemName(null);
				}
				if(itemName.equals(hpTimerDevice.getSwitchItemName())) {
					hpTimerDevice.setSwitchItemName(null);
				}
				// if there is no binding left, dispose this device
				if(hpTimerDevice.getStringItemName()==null && hpTimerDevice.getSwitchItemName()==null) {
				}
			}
			contextMap.remove(context);
		}
	}

}
