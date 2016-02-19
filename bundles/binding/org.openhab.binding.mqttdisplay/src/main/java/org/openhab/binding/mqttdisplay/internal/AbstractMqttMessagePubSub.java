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
import org.openhab.core.binding.BindingConfig;
import org.openhab.core.transform.TransformationHelper;
import org.openhab.core.transform.TransformationService;
import org.osgi.framework.BundleContext;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

/**
 * Base class for in and outbound MQTT message configurations on an openHAB
 * item.
 * 
 * @author Davy Vanherbergen
 * @since 1.3.0
 */
public abstract class AbstractMqttMessagePubSub implements BindingConfig {

	private static final Logger logger = LoggerFactory.getLogger(AbstractMqttMessagePubSub.class);

	private static final String TEMP_COLON_REPLACEMENT = "@COLON@";

	public enum MessageType {
		COMMAND, STATE
	}

	private String broker;

	private String topic;


	private MessageType messageType;

	/**
	 * Get the name of broker to use for sending/receiving MQTT messages.
	 * 
	 * @return name as defined in configuration file.
	 */
	public String getBroker() {
		return broker;
	}

	/**
	 * Set the name of broker to use for sending/receiving MQTT messages.
	 * 
	 * @param broker
	 *            name as defined in configuration file.
	 */
	public void setBroker(String broker) {
		this.broker = broker;
	}

	/**
	 * Get the MQTT topic to which to publish/subscribe to.
	 * 
	 * @return MQTT Topic string
	 */
	public String getTopic() {
		return topic;
	}

	/**
	 * Set the MQTT topic to which to publish/subscribe to. Subscription topics
	 * may contain wild cards.
	 * 
	 * @param topic
	 *            MQTT topic string.
	 */
	public void setTopic(String topic) {
		this.topic = topic;
	}

	/**
	 * @return type of messages supported.
	 */
	public MessageType getMessageType() {
		return messageType;
	}


	/**
	 * Split the given string into a string array using ':' as the separator. If
	 * the separator is escaped like '\:', the separator is ignored.
	 * 
	 * @param configString
	 * @return configString split into array.
	 */
	protected String[] splitConfigurationString(String configString) {

		if (StringUtils.isEmpty(configString)) {
			return new String[0];
		}

		String[] result = StringUtils
				.replaceEachRepeatedly(configString, new String[] { "\\:" },
						new String[] { TEMP_COLON_REPLACEMENT }).split(":");
		for (int i = 0; i < result.length; i++) {
			result[i] = StringUtils.replaceEachRepeatedly(result[i],
					new String[] { TEMP_COLON_REPLACEMENT },
					new String[] { ":" });
		}
		return result;
	}

	/**
	 * Set the supported message type.
	 * 
	 * @param messageType
	 */
	public void setMessageType(MessageType messageType) {
		this.messageType = messageType;
	}





}
