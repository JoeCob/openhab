/**
 * Copyright (c) 2010-2015, openHAB.org and others.
 *
 * All rights reserved. This program and the accompanying materials
 * are made available under the terms of the Eclipse Public License v1.0
 * which accompanies this distribution, and is available at
 * http://www.eclipse.org/legal/epl-v10.html
 */
package org.openhab.binding.mqttdisplay.internal;

import org.apache.commons.lang.StringUtils;
import org.openhab.io.transport.mqtt.MqttMessageProducer;
import org.openhab.io.transport.mqtt.MqttSenderChannel;
import org.openhab.model.item.binding.BindingConfigParseException;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

/**
 * Message publisher configuration for items which send outbound MQTT messages.
 * 
 * @author Davy Vanherbergen
 * @since 1.3.0
 */
public class MqttMessagePublisher extends AbstractMqttMessagePubSub implements
		MqttMessageProducer {

	private static final Logger logger = LoggerFactory.getLogger(MqttMessagePublisher.class);

	private MqttSenderChannel senderChannel;

	private String trigger;

	/**
	 * Create new MqttMessagePublisher from config string.
	 * 
	 * @param configuration
	 *            config string
	 * @throws BindingConfigParseException
	 *             if the config string is invalid
	 */
	public MqttMessagePublisher(String configuration) throws BindingConfigParseException {

		String[] config = splitConfigurationString(configuration);
		try {

			if (config.length != 2) {
				throw new BindingConfigParseException(
						"Configuration requires 2 parameters separated by ':'");
			}

			if (StringUtils.isEmpty(config[0])) {
				throw new BindingConfigParseException("Missing broker name.");
			} else {
				setBroker(config[0].trim());
			}

			if (StringUtils.isEmpty(config[1]) || config[1].indexOf('+') != -1
					|| config[1].indexOf('#') != -1) {
				throw new BindingConfigParseException("Invalid topic.");
			} else {
				setTopic(config[1].trim());
			}
			

		} catch (BindingConfigParseException e) {
			throw new BindingConfigParseException("Configuration '"
					+ configuration
					+ "' is not a valid outbound configuration: "
					+ e.getMessage());
		}
	}




	/**
	 * Publish a messge to the given topic.
	 * 
	 * @param topic
	 * @param message
	 */
	public void publish(String topic, String message) {
		if (senderChannel == null) {
			return;
		}
		
		try {
			senderChannel.publish(topic, message.getBytes());
		} catch (Exception e) {
			logger.error("Error publishing...", e);
		}
	}
	
	public void publish( String message) {
		if (senderChannel == null) {
			logger.error("Publishing failed. Channe is null");
			return;
		}
		
		
		try {
			senderChannel.publish(this.getTopic(), message.getBytes());
		} catch (Exception e) {
			logger.error("Error publishing...", e);
		}
	}

	@Override
	public void setSenderChannel(MqttSenderChannel channel) {
		senderChannel = channel;
	}

	/**
	 * @return string representation of state or command which triggers the
	 *         sending of a message.
	 */
	public String getTrigger() {
		return trigger;
	}

	/**
	 * @return true if this publisher has been activated by the
	 *         MqttBrokerConnection.
	 */
	public boolean isActivated() {
	
		return senderChannel != null;
	}

	/**
	 * Get the topic and replace ${item} in the topic with the actual name.
	 */
	public String getTopic(String itemName) {
		return StringUtils.replace(getTopic(), "${item}", itemName);
	}
	
}
