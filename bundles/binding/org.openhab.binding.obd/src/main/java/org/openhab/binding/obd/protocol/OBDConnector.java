/**
 * Copyright (c) 2010-2015, openHAB.org and others.
 *
 * All rights reserved. This program and the accompanying materials
 * are made available under the terms of the Eclipse Public License v1.0
 * which accompanies this distribution, and is available at
 * http://www.eclipse.org/legal/epl-v10.html
 */
package org.openhab.binding.obd.protocol;


import org.openhab.binding.obd.internal.OBDException;


/**
 * Base class for Open Energy Monitor communication.
 * 
 * @author Pauli Anttila
 * @since 1.4.0
 */
public abstract class OBDConnector {

	/**
	 * Procedure for connect to Open Energy Monitor system.
	 * 
	 * @throws OBDException
	 */
	public abstract void connect() throws OBDException;

	/**
	 * Procedure for disconnect from Open Energy Monitor system.
	 * 
	 * @throws OBDException
	 */
	public abstract void disconnect() throws OBDException;

	/**
	 * Procedure for receiving datagram from Open Energy Monitor devices.
	 * 
	 * @throws OBDException
	 */
	public abstract byte[] receiveDatagram() throws OBDException;

	public OBDObject receiveOBDObject() throws OBDException {
		// TODO Auto-generated method stub
		return null;
	}

	/**
	 * Procedure for receiving GPS info Object  from GPSD deamon.
	 * 
	 * @throws OBDException
	 */
	
	public boolean  poll() throws OBDException {
		// TODO Auto-generated method stub
		return false;
	}
	
	public boolean isConnected() throws OBDException {
		// TODO Auto-generated method stub
		return false;
	}
	
}
