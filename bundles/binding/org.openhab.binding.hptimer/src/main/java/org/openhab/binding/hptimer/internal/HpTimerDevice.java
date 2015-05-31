/**
 * Copyright (c) 2010-2015, openHAB.org and others.
 *
 * All rights reserved. This program and the accompanying materials
 * are made available under the terms of the Eclipse Public License v1.0
 * which accompanies this distribution, and is available at
 * http://www.eclipse.org/legal/epl-v10.html
 */
package org.openhab.binding.hptimer.internal;

import gnu.io.CommPortIdentifier;
import gnu.io.PortInUseException;
import gnu.io.SerialPort;
import gnu.io.SerialPortEvent;
import gnu.io.SerialPortEventListener;
import gnu.io.UnsupportedCommOperationException;

import java.io.IOException;
import java.io.InputStream;
import java.io.OutputStream;
import java.util.Enumeration;
import java.util.Timer;
import java.util.TimerTask;
import java.util.TooManyListenersException;

import org.apache.commons.io.IOUtils;
import org.openhab.core.events.EventPublisher;
import org.openhab.core.library.types.DecimalType;
import org.openhab.core.library.types.OnOffType;
import org.openhab.core.library.types.StringType;
import org.openhab.core.types.State;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

/**
 * This class represents a serial device that is linked to exactly one String item and/or Switch item.
 * 
 * @author Kai Kreuzer
 *
 */
public class HpTimerDevice  {

	private static final Logger logger = LoggerFactory.getLogger(HpTimerDevice.class);

	private int refresh = 1000;
	private String item;
	private String stringItemName;
	private String switchItemName;
	
	private TimerTask task = new HpTimerTask( this );
	private Timer timer = new Timer();

	private EventPublisher eventPublisher;

	public HpTimerDevice(int refresh) {
		this.refresh = refresh;
	}

	public HpTimerDevice(String item, int refresh) {
		this.item = item;
		this.refresh = refresh;
	}

	public void setEventPublisher(EventPublisher eventPublisher) {
		this.eventPublisher = eventPublisher;
	}

	public void unsetEventPublisher(EventPublisher eventPublisher) {
		this.eventPublisher = null;
	}

	public String getStringItemName() {
		return stringItemName;
	}

	public void setStringItemName(String stringItemName) {
		this.stringItemName = stringItemName;
	}

	public String getSwitchItemName() {
		return switchItemName;
	}

	public void setSwitchItemName(String switchItemName) {
		this.switchItemName = switchItemName;
	}


	/**
	 * Initialize this device and open the serial port
	 * 
	 * @throws InitializationException if port can not be opened
	 */
	@SuppressWarnings("rawtypes")
	public void initialize() throws InitializationException {
		// parse ports and if the default port is found, initialized the reader
		
			logger.info("Initializing HPT. Refresh is {}", refresh );
			
			timer.scheduleAtFixedRate(task, refresh, refresh);

			throw new InitializationException("Failed to initialize Timer. ");
	}





	/**
	 * Post Update to Item
	 */

	public void update() {
		// TODO Auto-generated method stub
		
		State state = new DecimalType ( System.currentTimeMillis() );
		eventPublisher.postUpdate(item, state);
		
	}

}
