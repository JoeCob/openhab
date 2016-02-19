/**
 * Copyright (c) 2010-2015, openHAB.org and others.
 *
 * All rights reserved. This program and the accompanying materials
 * are made available under the terms of the Eclipse Public License v1.0
 * which accompanies this distribution, and is available at
 * http://www.eclipse.org/legal/epl-v10.html
 */
package org.openhab.binding.mqttdisplay.internal;

import java.io.IOException;
import java.net.InetAddress;
import java.net.UnknownHostException;
import java.text.DateFormat;
import java.text.SimpleDateFormat;
import java.util.ArrayList;
import java.util.Calendar;
import java.util.Date;
import java.util.Dictionary;
import java.util.Locale;
import java.util.TimeZone;
import java.util.regex.Matcher;
import java.util.regex.Pattern;

import org.apache.commons.lang.StringUtils;
import org.apache.commons.net.ntp.NTPUDPClient;
import org.apache.commons.net.ntp.TimeInfo;
import org.openhab.binding.mqttdisplay.MqttDisplayBindingProvider;
import org.openhab.model.item.binding.AbstractGenericBindingProvider;
import org.openhab.model.item.binding.BindingConfigParseException;
import org.openhab.core.binding.AbstractActiveBinding;
import org.openhab.core.binding.BindingConfig;
import org.openhab.core.items.ItemNotFoundException;
import org.openhab.core.items.ItemRegistry;
import org.openhab.core.library.types.DateTimeType;
import org.openhab.core.library.types.StringType;
import org.openhab.core.types.State;
import org.openhab.io.transport.mqtt.MqttSenderChannel;
import org.openhab.io.transport.mqtt.MqttService;
import org.osgi.service.cm.ConfigurationException;
import org.osgi.service.cm.ManagedService;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;


/**
 * The NTP Refresh Service polls the configured timeserver with a configurable 
 * interval and posts a new event of type ({@link DateTimeType} to the event bus.
 * The interval is 15 minutes by default and can be changed via openhab.cfg. 
 * 
 * @author Thomas.Eichstaedt-Engelen
 * @since 0.8.0
 */



// TODO: 
// - Initialization code so we can start the MqTT objects and connection. 
// - String Replacement Code. 
// - MqTT publishing code. 

public class MqttDisplayBinding extends AbstractActiveBinding<MqttDisplayBindingProvider> implements ManagedService {

	private static final Logger logger = LoggerFactory.getLogger(MqttDisplayBinding.class);
	
	/** timeout for requests to the NTP server */
	//private static final int REFRESG = 5000;

	// List of time servers: http://tf.nist.gov/service/time-servers.html
	protected String brokername = "maincontroller";
	protected String topic = "CAR_DSP0_IN";
	
	/** Default refresh interval (currently 15 minutes) */
	private long refreshInterval = 500L;
	
	private ItemRegistry itemRegistry;
	
	private MqttMessagePublisher publisher;
	private MqttService mqttService;
	private MqttSenderChannel senderChannel;
	
	/** for logging purposes */
	private final static DateFormat SDF = 
		SimpleDateFormat.getDateTimeInstance(SimpleDateFormat.FULL, SimpleDateFormat.FULL);
	
    ArrayList<ArrayList<String>> parserRules = new ArrayList<ArrayList<String>>();
    
    int currentScreen = 0;
    int maxScreen = 1;
    int maxLine = 4;
    
	String content;
	MqttDisplayBindingConfig config ;
	
	
    
    private String rx = "(\\$\\{[^}]+\\})";
    
    private Pattern p = Pattern.compile(rx);
    
    
    //Replacement Variables. Allocating here to reduce GC due to constant allocation/deallocation. 
	private StringBuffer sb;
	//Pattern p = Pattern.compile(rx);
	private Matcher m;
	private String varItem = ""; //m.group(1)
	private String opItem = ""; //varItem.substring(2, varItem.length()-1);
	private String repString = null;
    
    
	
	
	@Override
	protected String getName() {
		return "MqTT Displat Refresh Service";
	}
	
	@Override
	protected long getRefreshInterval() {
		return refreshInterval;
	}
		
	/**
	 * @{inheritDoc}
	 */
	@Override
	public void execute() {
		
		if (!bindingsExist()) {
			logger.debug("There is no existing MqTT Display binding configuration => refresh cycle aborted!");
			return;
		}
		
		
		try {
			
			publisher = new MqttMessagePublisher (brokername+":"+topic);
			mqttService.registerMessageProducer(brokername, publisher);
			mqttService.activate();
		} catch (BindingConfigParseException e1) {
			// TODO Auto-generated catch block
			logger.error("Could not initialize MqttPublisher");
			return;
		}
		
		
		//long networkTimeInMillis = getTime(hostname);
		
		
		/* 
		 * 
		 * 	private ArrayList<AlarmDecoderBindingConfig> getItems(String itemName) {
		ArrayList<AlarmDecoderBindingConfig> al = new ArrayList<AlarmDecoderBindingConfig>();
		for (AlarmDecoderBindingProvider bp : providers) {
			al.add(bp.getBindingConfig(itemName));
		}
		return al;
		 * 
		 * 
		 * 
		 * 
		 */
		
		//logger.debug("Got time from {}: {}", hostname, SDF.format(new Date(networkTimeInMillis)));
		
		//BindingConfig config = bindingConfigs.get(itemName);
		while (publisher.isActivated()) {
			try {
				Thread.sleep (refreshInterval);
			} catch (InterruptedException e1) {
				// TODO Auto-generated catch block
				e1.printStackTrace();
			}
			for (MqttDisplayBindingProvider provider : providers) {
				// Gets the content to be parsed. 
				//BindingConfig config = bindingConfigs.get(itemName);
				//ArrayList<MqttDisplayBindingConfig> al = new ArrayList<MqttDisplayBindingConfig>();
				//MqttDisplayBindingConfig config = (MqttDisplayBindingConfig) .get(itemName);
				for (String itemName : provider.getItemNames()) {
					config = provider.getBindingConfig(itemName);
					if (config.screen != currentScreen ) { continue; }
					//if (config.line > maxLine ) { continue; }
					//String parserRule = config.content;
					//ArrayList<String> screenParser = parserRules.get(currentScreen);
					try {	
							content = /*config.screen + */ doReplacements (config.content);
							
							logger.trace("Publishing {} to {}:{}", content, brokername, topic);
							eventPublisher.postUpdate(itemName, new StringType ( content.substring(1)));

						
							
							publisher.publish(content);
							
							// Avoid flooding Arduino. 
							// Thread.sleep(200);

							
					} catch( Exception e ) {
						logger.debug("Failed publishing to {}. Exception was {}", brokername, e.toString());
					}
				}
				//eventPublisher.postUpdate(itemName, new DateTimeType(calendar));
			}
		}
		
	}
	
	/**
	 * Queries the given timeserver <code>hostname</code> and returns the time
	 * in milliseconds.
	 * 
	 * @param hostname the timeserver to query
	 * @return the time in milliseconds or the current time of the system if an
	 * error occurs.
	 */
	/*protected static long getTime(String hostname) {
		
		try {
			NTPUDPClient timeClient = new NTPUDPClient();
			timeClient.setDefaultTimeout(NTP_TIMEOUT);
			InetAddress inetAddress = InetAddress.getByName(hostname);
			TimeInfo timeInfo = timeClient.getTime(inetAddress);
			
			return timeInfo.getReturnTime();
		} 
		catch (UnknownHostException uhe) {
			logger.warn("the given hostname '{}' of the timeserver is unknown -> returning current sytem time instead", hostname);
		}
		catch (IOException ioe) {
			logger.warn("couldn't establish network connection [host '{}'] -> returning current sytem time instead", hostname);
		}
		
		return System.currentTimeMillis();
	}*/
		
	
	/**
	 * {@inheritDoc}
	 */
	@SuppressWarnings("rawtypes")
	public void updated(Dictionary config) throws ConfigurationException {
		
		// Limits to 10 screens maximum. 
		if ( parserRules.size() == 0) {
			for (int i=0;i<10;i++) {
				parserRules.add(new ArrayList<String>());
			}
		}
		
		if (config != null) {
			String hostnameString = (String) config.get("brokername");
			if (StringUtils.isNotBlank(hostnameString)) {
				brokername = hostnameString;
			}
			
			String refreshIntervalString = (String) config.get("refresh");
			if (StringUtils.isNotBlank(refreshIntervalString)) {
				refreshInterval = Long.parseLong(refreshIntervalString);
			}
			
			/*String content = (String) config.get("content");
			if (StringUtils.isNotBlank(content)) {
				String fields[] = content.split("|");
				int screen = Integer.valueOf(fields[0]);
				int line = Integer.valueOf(fields[1]);
				String displayText = fields[2];
				if ( screen > 0 && line > 0) {
					parserRules.get(screen).set(line, displayText);
				}
				if ( screen > maxScreen ) {
					maxScreen = screen;
				}
				if ( line > maxLine ) {
					maxLine = line;
				}*/
			
			
			setProperlyConfigured(true);
		}
		

	}
	private String doReplacements ( String inputRule ) {
	

			//String line ="${env1}sojods${env2}${env3}";
			//String rx = "(\\$\\{[^}]+\\})";

			 sb = new StringBuffer();
			//Pattern p = Pattern.compile(rx);
			 m = p.matcher(inputRule);

			while (m.find())
			{
			    // Avoids throwing a NullPointerException in the case that you
			    // Don't have a replacement defined in the map for the match

			    
			    varItem = m.group(1);
			    opItem = varItem.substring(2, varItem.length()-1);
			    repString = null;
				try {
					repString = itemRegistry.getItem(opItem).getState().toString();
					if ( repString == "Uninitialized") { 
						repString = " ";
					}
				} catch (ItemNotFoundException e) {
					// TODO Auto-generated catch block
					logger.debug("No value found for item {} in registry", opItem);
					repString = "NOT_FOUND";
				}
			    //System.out.println(opItem);
			    logger.debug("Replacing {} with item {}. Item value is {}.", varItem, opItem, repString);
			    if (repString != null)    
			        m.appendReplacement(sb, repString);
			}
			m.appendTail(sb);

			return sb.toString();
		
	}
	
	/**
	 * Setter for Declarative Services. Adds the MqttService instance.
	 * 
	 * @param mqttService
	 *            Service.
	 */
	public void setMqttService(MqttService mqttService) {
		this.mqttService = mqttService;
	}

	/**
	 * Unsetter for Declarative Services.
	 * 
	 * @param mqttService
	 *            Service to remove.
	 */
	public void unsetMqttService(MqttService mqttService) {
		this.mqttService = null;
	}
	

	public void setItemRegistry(ItemRegistry itemRegistry) {
		this.itemRegistry = itemRegistry;
	}

	public void unsetItemRegistry(ItemRegistry itemRegistry) {
		this.itemRegistry = null;
	}
	
	public void setSenderChannel(MqttSenderChannel channel) {
		senderChannel = channel;
	}
	
}
