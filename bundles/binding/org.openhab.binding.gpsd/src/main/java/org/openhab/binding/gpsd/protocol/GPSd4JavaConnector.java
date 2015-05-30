/**
 * Copyright (c) 2010-2015, openHAB.org and others.
 *
 * All rights reserved. This program and the accompanying materials
 * are made available under the terms of the Eclipse Public License v1.0
 * which accompanies this distribution, and is available at
 * http://www.eclipse.org/legal/epl-v10.html
 */
package org.openhab.binding.gpsd.protocol;


/*import java.io.IOException;
import java.net.DatagramPacket;
import java.net.DatagramSocket;
import java.net.SocketException;
import java.nio.ByteBuffer;
import java.util.Arrays;*/

//import Tester;

import org.openhab.binding.gpsd.internal.GPSdException;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import de.taimos.gpsd4java.api.ObjectListener;
import de.taimos.gpsd4java.backend.GPSdEndpoint;
import de.taimos.gpsd4java.backend.ResultParser;
import de.taimos.gpsd4java.types.ATTObject;
import de.taimos.gpsd4java.types.DeviceObject;
import de.taimos.gpsd4java.types.DevicesObject;
import de.taimos.gpsd4java.types.PollObject;
import de.taimos.gpsd4java.types.SATObject;
import de.taimos.gpsd4java.types.SKYObject;
import de.taimos.gpsd4java.types.TPVObject;
import de.taimos.gpsd4java.types.subframes.SUBFRAMEObject;

/**
 * Connector for UDP communication.
 * 
 * @author Pauli Anttila
 * @since 1.4.0
 */
public class GPSd4JavaConnector extends GPSdConnector {

	private static final Logger logger = LoggerFactory
			.getLogger(GPSd4JavaConnector.class);

	static final int MAX_PACKET_SIZE = 255;

	int port = 2947;
	String host = "localhost";
	GPSdEndpoint ep = null; 
	TPVObject locationData = null;
	boolean connected = false;


	/*public GPSd4JavaConnector(String hostname) {
		// TODO Auto-generated constructor stub
		if (host != null) {
			this.host = hostname;
		}
	}*/

	public GPSd4JavaConnector() {
		// TODO Auto-generated constructor stub
		logger.debug("Starting GPSd4JavaConnector");
		this.host = "localhost";
	}

	public GPSd4JavaConnector(String hostname) {
		// TODO Auto-generated constructor stub
		this.host = hostname;
	}

	@Override
	public void connect() throws GPSdException {

		if (host != null) {
			try {
				logger.debug("Connecting to {} port {}", host, port );
				ep = new GPSdEndpoint(host, port, new ResultParser());
				logger.debug("ep set to: {} ", ep.toString());
				ep.addListener(new ObjectListener() {
					@Override
					public void handleTPV(final TPVObject tpv) {
						//logger.debug("TPV: {}" + tpv.toString());
						ep.GPSData = tpv;
					}
					
					@Override
					public void handleSKY(final SKYObject sky) {
						//logger.debug("SKY: {}", sky);
						for (final SATObject sat : sky.getSatellites()) {
							//logger.debug("  SAT: {}", sat);
						}
					}
					
					@Override
					public void handleSUBFRAME(final SUBFRAMEObject subframe) {
						//logger.debug("SUBFRAME: {}", subframe);
					}
					
					@Override
					public void handleATT(final ATTObject att) {
						//logger.debug("ATT: {}", att);
					}
					
					@Override
					public void handleDevice(final DeviceObject device) {
						//logger.debug("Device: {}", device);
					}
					
					@Override
					public void handleDevices(final DevicesObject devices) {
						for (final DeviceObject d : devices.getDevices()) {
							//logger.debug("Device: {}", d);
						}
					}
				});
				ep.start();
				logger.debug("GPSD (gps4java)  message listener started");
				logger.debug("Watch: {}", ep.watch(true, true));
				connected = true;

			} catch (Exception e) {
				throw new GPSdException(e);
			}
		}
	}

	@Override
	public void disconnect() throws GPSdException {

		if (ep != null) {
			ep.stop();
			ep = null;
		}
		connected = false;
	}


	public TPVObject receiveGPSObject() throws GPSdException { 
		
		if (ep.GPSData == null) {
			//ep.notifyAll();
			logger.debug("GPS Object is invalid. {}", ep.toString());
			
		}
		
		return ep.GPSData;
		
	}
	
	public int poll() throws GPSdException { 
		
		PollObject result = new PollObject();
		try {
			result  = ep.poll();
			return result.getActive();
		}
		catch (Exception e) {
			return -1;
		}
	}
	

	public boolean isConnected() {
		return this.connected;
	}
	
	@Override
	public byte[] receiveDatagram() throws GPSdException {

		try {/*

			if (ep.test == null) {
				ep.notifyAll();
			}

			Create a packet
			DatagramPacket packet = new DatagramPacket(new byte[MAX_PACKET_SIZE],
					MAX_PACKET_SIZE);

			 Receive a packet (blocking)
			socket.receive(packet);

			String[] bytes = new String(Arrays.copyOfRange(packet.getData(), 0,
					packet.getLength() - 1)).split(" ");

			ByteBuffer bytebuf = ByteBuffer.allocate(bytes.length);

			for (int i = 0; i < bytes.length; i++) {
				if (bytes[i].isEmpty() == false) {
					byte b = (byte) Integer.parseInt(bytes[i]);
					bytebuf.put(b);
				}
			}

			return bytebuf.array();*/
			return null;

		} catch (Exception e) {
			
			throw new GPSdException(e);
		}
	}
}
