/**
 * Copyright (c) 2010-2015, openHAB.org and others.
 *
 * All rights reserved. This program and the accompanying materials
 * are made available under the terms of the Eclipse Public License v1.0
 * which accompanies this distribution, and is available at
 * http://www.eclipse.org/legal/epl-v10.html
 */
package org.openhab.binding.gpsd.protocol;


import org.openhab.binding.gpsd.internal.GPSdException;

import de.taimos.gpsd4java.types.TPVObject;

/**
 * Base class for Open Energy Monitor communication.
 * 
 * @author Pauli Anttila
 * @since 1.4.0
 */
public abstract class GPSdConnector {

	/**
	 * Procedure for connect to Open Energy Monitor system.
	 * 
	 * @throws GPSdException
	 */
	public abstract void connect() throws GPSdException;

	/**
	 * Procedure for disconnect from Open Energy Monitor system.
	 * 
	 * @throws GPSdException
	 */
	public abstract void disconnect() throws GPSdException;

	/**
	 * Procedure for receiving datagram from Open Energy Monitor devices.
	 * 
	 * @throws GPSdException
	 */
	public abstract byte[] receiveDatagram() throws GPSdException;

	/**
	 * Procedure for receiving GPS info Object  from GPSD deamon.
	 * 
	 * @throws GPSdException
	 */
	public abstract TPVObject receiveGPSObject() throws GPSdException;
	
	public int poll() throws GPSdException {
		return -1;
	}
	

	public boolean isConnected() {
		// TODO Auto-generated method stub
		return false;
	}
	
}
