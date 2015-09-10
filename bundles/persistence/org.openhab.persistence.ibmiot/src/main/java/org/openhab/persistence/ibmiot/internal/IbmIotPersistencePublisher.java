/**
 * Copyright (c) 2010-2015, openHAB.org and others.
 *
 * All rights reserved. This program and the accompanying materials
 * are made available under the terms of the Eclipse Public License v1.0
 * which accompanies this distribution, and is available at
 * http://www.eclipse.org/legal/epl-v10.html
 */
package org.openhab.persistence.ibmiot.internal;


import java.math.BigDecimal;
import java.math.RoundingMode;
import java.nio.ByteBuffer;
import java.util.Arrays;
import java.util.TimerTask;

import org.json.JSONObject;
import org.openhab.core.items.Item;
import org.openhab.core.library.types.DecimalType;
import org.openhab.core.library.types.PercentType;
import org.openhab.io.transport.mqtt.MqttMessageProducer;
import org.openhab.io.transport.mqtt.MqttSenderChannel;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.json.JSONObject;

/**
 * MQTT Message publisher for composing and sending persistence messages.
 * 
 * @author Davy Vanherbergen
 * @since 1.3.0
 */
public class IbmIotPersistencePublisher implements MqttMessageProducer {

	private MqttSenderChannel channel;
	
	private static Logger logger = LoggerFactory.getLogger(IbmIotPersistenceService.class);
	

	//private String messageTemplate;

	private String topic;
	
	long previousUpdate = System.currentTimeMillis();
	
	long numDocs = 0;
	
	private int timeout = 300000;
	
	private int messageGroupLimit = 500;
	
	private int messageLimit = 4096;
	
	JSONObject response = new JSONObject();
	
	/**
	 * Initialize publisher with a given topic and template.
	 * 
	 * @param topic
	 *            MQTT publish topic.
	 * @param messageTemplate
	 *            message payload template.
	 */
	public IbmIotPersistencePublisher(String topic, int timeout, int messageGroupLimit, int messageLimit) {
		this.topic = topic;
//		this.messageTemplate = messageTemplate;
		this.messageGroupLimit = messageGroupLimit;
		this.timeout = timeout;
		this.messageLimit = messageLimit;
	}

	@Override
	public void setSenderChannel(MqttSenderChannel channel) {
		this.channel = channel;
	}

	/**
	 * Publish a persistence message for a given item.
	 * 
	 * Topic and template will be reformatted by String.format using the
	 * following parameters:
	 * 
	 * <pre>
	 * 	%1 item name 
	 * 	%2 alias (as defined in mqtt.persist)
	 * 	%3 item state 
	 * 	%4 current timestamp
	 * </pre>
	 * 
	 * @param item
	 *            item which to persist the state of.
	 * @param alias
	 *            null or as defined in persistence configuration.
	 * @throws Exception
	 *             when no MQTT message could be sent.
	 */
	public void publish(Item item, String alias) throws Exception {

		

		long currentTimer = System.currentTimeMillis();

		JSONObject data = new JSONObject();

		
		try {
			numDocs++;

				logger.debug("Item is: {}", item.getState().toString());
				
			    data = generateJsonResponse(item);
				
			    logger.debug("Response generated was: {}", data.toString());
			    
				if ( ( (currentTimer - previousUpdate) > timeout ) || ( numDocs >= messageGroupLimit ) || ( response.toString().length() + data.toString().length() > messageLimit) )
				{
					try { 
					logger.info("Sending {} messages. Packet Size is {}", numDocs, response.toString().length());
					channel.publish(topic, response.toString().getBytes("utf-8"));
					} catch (Exception ex)
					{
						logger.debug("Error sending message {}", ex.toString());
					}
					numDocs = 0;
					previousUpdate = System.currentTimeMillis() ;
					response = new JSONObject();
				} else 
				{
					response.accumulate("docs", data );
				}
			}
			
			catch (Exception e) {
				logger.debug("Error publishing message {}", e.toString());
				logger.debug(e.getStackTrace().toString());
			}
	}
	
	static JSONObject generateJsonResponse(Item item) {
		JSONObject data = new JSONObject();
		//byte [] bytes = ByteBuffer.allocate(8).putLong(System.currentTimeMillis()).array();
		
		//String arr = String.valueOf(System.currentTimeMillis());
		//Arrays.toString(bytes);
		
		//JSONObject response =  new JSONObject();
		try { 
			//data.put("device", device );
		    if (item.getState() != null) {
			if (( item.getState().toString() == "Uninitialized" ) || item.getState().toString() == "Undefined" ) {
					data.put( item.getName() , -999 );}
				else {
						if (  item.getState() instanceof DecimalType ) { 
							BigDecimal bd = new BigDecimal ( item.getState().toString()).setScale(4,  RoundingMode.HALF_UP);
							logger.debug("Decimal type. Input value is {}, output value is {}.", item.getState().toString(), bd.toString());
							data.put( item.getName() , bd.setScale(4, RoundingMode.HALF_UP) );
						} else if (  item.getState() instanceof PercentType ) { 
							BigDecimal bd = new BigDecimal ( item.getState().toString()).setScale(4,  RoundingMode.HALF_UP);
							logger.debug("Percent type. Input value is {}, output value is {}.", item.getState().toString(), bd.toString());
							data.put( item.getName() , bd.setScale(4,  RoundingMode.HALF_UP) );
						} else 
						{
							data.put( item.getName() ,item.getState() );
						}
					}
			 }
			return data;
		} catch ( Exception e){
			logger.debug("Exception generating respose for item {}. Exceptions was: ", item.getState().toString(), e.toString());
			logger.debug(e.getMessage());
			e.printStackTrace();
			
		}
		return null;
	}
	
	
}
