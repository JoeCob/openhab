package org.openhab.binding.mqttdisplay.internal;

import java.util.Map;

import org.openhab.core.binding.BindingConfig;

public class MqttDisplayBindingConfig implements BindingConfig {
	public int screen;
	public int line;
	public String content;
	public String brokername;
	public String topic;
	
	
}