package org.openhab.persistence.ibmiot.internal;

import java.util.TimerTask;

import org.openhab.io.transport.mqtt.MqttSenderChannel;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import com.mongodb.BasicDBObject;
import com.mongodb.DBObject;

public class IbmIotPersistenceTick extends TimerTask {

	IbmIotOfflineCache offlineCache;
	MqttSenderChannel channel;
	BasicDBObject dbobj;
	String topic;
	IbmIotPersistencePublisher publisher;
	String message;
	
	
	private static Logger logger = LoggerFactory.getLogger(IbmIotPersistenceService.class);

	
	public IbmIotPersistenceTick(
			IbmIotOfflineCache offlineCache , MqttSenderChannel channel, String topic, IbmIotPersistencePublisher publisher) {
		// TODO Auto-generated constructor stub
		this.channel = channel;
		this.offlineCache = offlineCache;
		this.topic = topic;
		this.publisher = publisher;
	}

	@Override
	public void run() {
		// TODO Auto-generated method stub
		logger.debug ("Message cleaner awake");
		try {

			// logger.debug("Sending {} messages. Packet Size is {}", numDocs,
			// response.toString().length());
			if (channel != null) {
				if (offlineCache.query().iterator().hasNext()) {
					dbobj = offlineCache.query().iterator().next();
					logger.info("Flushing offline cache");
					try {
						channel.publish(topic,
								dbobj.toString().getBytes("utf-8"));
						offlineCache.remove(dbobj);
						logger.debug ("Message published by cleaner and removed from cache");
					} catch (Exception ex) {
						logger.error("Error sending offline message to broker. Finished cleaner. ");
						return;
					}
					// offlineCache.query().iterator().remove();
					//offlineCache.remove(dbobj);
				} else {
					logger.debug ("Message backlog is at {} messages", publisher.queue.size());
					message = publisher.pool();
					if (message != null) {
						try {
							channel.publish(topic, message.getBytes("utf-8"));
							logger.debug ("Message published by cleaner");
						} catch (Exception ex) {
							publisher.enqueue(message);
							logger.error("Error sending  message to broker. Finishing cleaner");
							return;
						}
						// channel.publish(topic,
						// dbobj.toString().getBytes("utf-8")); publisher.pool()

					} else {
						logger.debug("Nothing to publish");
					}
				}
			} else {
				logger.debug("Channel is null");
				logger.info("MQTT Channel is not opened. Storing message for resending");
				message = publisher.pool();
				offlineCache.store(message);
//				hasCache = true;
				//offlineCache.store(response);
//				response = new JSONObject();
				// Store into MongoDB. 
				//throw new Exception();
				
			}
		} catch (Exception ex) {
			logger.debug("Exceptoin publishing message {} to channel {}", this.message, this.topic);
		}
		logger.debug("Message cleaner is done.");
	}
}
