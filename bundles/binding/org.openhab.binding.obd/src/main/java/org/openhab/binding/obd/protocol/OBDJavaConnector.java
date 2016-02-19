/**
 * Copyright (c) 2010-2015, openHAB.org and others.
 *
 * All rights reserved. This program and the accompanying materials
 * are made available under the terms of the Eclipse Public License v1.0
 * which accompanies this distribution, and is available at
 * http://www.eclipse.org/legal/epl-v10.html
 */
package org.openhab.binding.obd.protocol;


/*import java.io.IOException;
import java.net.DatagramPacket;
import java.net.DatagramSocket;
import java.net.SocketException;
import java.nio.ByteBuffer;
import java.util.Arrays;*/

//import Tester;

import org.openhab.binding.obd.internal.OBDException;
//import org.openhab.binding.serial.internal.InitializationException;


//import java.io.DataOutputStream;
import java.io.IOException;
import java.io.InputStream;
import java.io.OutputStream;


//import org.apache.commons.io.IOUtils;

//import gnu.io.CommPort;
import gnu.io.CommPortIdentifier;
//import gnu.io.NRSerialPort;
import gnu.io.PortInUseException;
import gnu.io.SerialPort;
import gnu.io.UnsupportedCommOperationException;


import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import pt.lighthouselabs.obd.commands.protocol.FastInitObdCommand;
import pt.lighthouselabs.obd.commands.protocol.InitObdCommand;
import pt.lighthouselabs.obd.commands.protocol.EchoOffObdCommand;
import pt.lighthouselabs.obd.commands.protocol.LineFeedOffObdCommand;
import pt.lighthouselabs.obd.commands.protocol.SelectProtocolObdCommand;
import pt.lighthouselabs.obd.commands.protocol.TimeoutObdCommand;
import pt.lighthouselabs.obd.enums.ObdProtocols;


/**
 * Connector for UDP communication.
 * 
 * @author Pauli Anttila
 * @since 1.4.0
 */
public class OBDJavaConnector extends OBDConnector  {

	private static final Logger logger = LoggerFactory
			.getLogger(OBDJavaConnector.class);

	
	static final int MAX_PACKET_SIZE = 255;

	CommPortIdentifier portIdentifier = null;
	//CommPort commPort = null;

	
	SerialPort serialPort = null;

	
	InputStream in = null;
	OutputStream out = null;
	String device = "/dev/ttyUSB01";
	boolean connected = false; 
	OBDObject ecuData = new OBDObject();
	int speed = 9600;
	int commDelay = 50;
	
	

	public OBDJavaConnector() {
		// TODO Auto-generated constructor stub
		logger.debug("Starting OBDJavaConnector");
	}

	public OBDJavaConnector(String device, int speed) {
		// TODO Auto-generated constructor stub
		this.device = device;
		this.speed = speed;
	}
	
	public OBDJavaConnector(String device, int speed, int delay) {
		// TODO Auto-generated constructor stub
		this.device = device;
		this.speed = speed;
		//this.commDelay = delay;
		//ecuData.setDelay ( delay );
	}


	@Override
	public void connect() throws OBDException {

		try {
			portIdentifier = CommPortIdentifier.getPortIdentifier( device );

			if( portIdentifier.isCurrentlyOwned() ) {
				logger.error( "Error: Port is currently in use" );
				PortInUseException e = new PortInUseException();
				throw new IOException(e);
			} else {
				int timeout = 2000;
				if ( portIdentifier.getPortType() == CommPortIdentifier.PORT_SERIAL){
					/*serialPort.setSerialPortParams( speed,
			                                          SerialPort.DATABITS_8,
			                                          SerialPort.STOPBITS_1,
			                                          SerialPort.PARITY_NONE );
			          in = serialPort.getInputStream();
			          out = serialPort.getOutputStream(); */
					logger.debug("Serial Port Found");
					if (portIdentifier != null) {
						// initialize serial port
						try {
							serialPort = (SerialPort) portIdentifier.open( "obd", 2000 );
							serialPort.sendBreak(timeout);
							
						} catch (PortInUseException e) {
							logger.debug("Port in use {}: {} ", device, e.toString() );
							throw new IOException(e);
						}

						try {
							// set port parameters
							serialPort.setSerialPortParams(speed, SerialPort.DATABITS_8, SerialPort.STOPBITS_1,
									SerialPort.PARITY_NONE);
							//serialPort.addEventListener();
						} catch (Exception e) {
							logger.debug("Failed to set parameters:  {}: {} ", serialPort.toString(), e.toString() );
							throw new IOException(e);
						}
						
						try {
							  serialPort.setFlowControlMode(
							        SerialPort.FLOWCONTROL_NONE);
							  // OR
							  // If CTS/RTS is needed
							  //serialPort.setFlowControlMode(
							  //      SerialPort.FLOWCONTROL_RTSCTS_IN | 
							  //      SerialPort.FLOWCONTROL_RTSCTS_OUT);
							} catch (UnsupportedCommOperationException ex) {
							  System.err.println(ex.getMessage());
						}

						try {
							in = serialPort.getInputStream();
						} catch (IOException e) {
							logger.debug("Input string failed:  {}: {} ", in.toString(), e.toString() );
							throw new IOException(e);
						}


						try {
							// get the output stream
							out = serialPort.getOutputStream();
							out.flush();
						} catch (IOException e) {
							logger.debug("Output string failed:  {}: {} ", in.toString(), e.toString() );
							throw new IOException(e);
						}
						
						if (!serialPort.isCD()) {
							logger.debug("Carrier not detected.");
							//throw new IOException(new Exception("Failed to detect carrier"));
						}
					}
				
					
					

				logger.debug("Initializing Data Object" );

				ecuData.init ( serialPort , commDelay);

				logger.debug("OBD Serial Connected " );

				connected = true;
				
				}
			}
		}  catch  ( IOException i) {
			logger.debug("IO connecting serial port {}: {} ", device, i.toString() );
			serialPort.close();
			//throw new OBDException ("IO connecting serial port ");
		}  catch (Exception e) {
			logger.debug("Exception connecting serial port {}: {} ", device, e.toString() );
			serialPort.close();
			//throw new OBDException ("IO connecting serial port ");
		}
	}

	@Override
	public void disconnect() throws OBDException {
		connected = false;
		serialPort.close();

	}

    @Override
	public OBDObject receiveOBDObject() throws OBDException { 
		
		if (ecuData == null) {
			//ep.notifyAll();
			logger.debug("OBD Object is null");
			
		}
		
		return ecuData;
		
	}
	
	public int poll() throws OBDException { 
		
		try {
		     // Should return a status. 0 is ok, -1 is error, 1 is warning like unable to connect. 
			return ecuData.refresh();
		}
		catch (Exception e) {
			logger.error("Pool failed with error {}", e.toString());
			e.printStackTrace();
			return -1;
		}
	}
	
	@Override
	public byte[] receiveDatagram() throws OBDException {

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
			
			throw new OBDException(e);
		}
	}
	
	public boolean isConnected() throws OBDException {
	
		return connected;
		
		
	}
	
	public boolean reinit() { 
		
		try {
			this.disconnect();
			Thread.sleep(5000);
			this.connect();
			
			return this.connected;
		} catch (Exception e) {
			// TODO Auto-generated catch block
			e.printStackTrace();
			return false;
		}
		
	}
	
	
	public boolean fullInit () {
		
		try {
			logger.debug("Re-Initializing OBD Adapter");
			
			new InitObdCommand().run(in, out);
			
			
			logger.debug("Setting OBD Timeout");
			
			new TimeoutObdCommand(100).run(in, out);
			

			logger.debug("Setting Echo Off");
			new EchoOffObdCommand().run(in, out);

			logger.debug("Setting LineFeed Off " );
			new LineFeedOffObdCommand().run(in, out);

			logger.debug(" Setting Protocol " );

			SelectProtocolObdCommand command = new SelectProtocolObdCommand(ObdProtocols.AUTO);
			command.run(in, out);
			
			logger.debug("Protocol set to {}", command.getFormattedResult() );
			
			return true;
		} catch (Exception e) {
			// TODO Auto-generated catch block
			e.printStackTrace();
			return false;
		}
	
	}
	
	
	public boolean fastInit()  {
		try {
			logger.debug("Fatst Re-Initializing OBD Adapter");
			
			new FastInitObdCommand().run(in, out);
			
			
			return true;
		} catch (Exception e) {
			// TODO Auto-generated catch block
			e.printStackTrace();
			return false;
		}
	
	}

}
