package org.whispersystems.textsecuregcm.mq;

import com.rabbitmq.client.*;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.whispersystems.textsecuregcm.WhisperServerConfiguration;
import org.whispersystems.textsecuregcm.configuration.RabbitConfiguration;

import java.io.IOException;
import java.util.concurrent.TimeoutException;

public class MessageQueueManager {

    private final Logger log = LoggerFactory.getLogger(MessageQueueManager.class);

    private Connection connection;
    private Channel channel;
    private ConnectionFactory factory;
    private String queue;

    public MessageQueueManager(WhisperServerConfiguration config) {
        RabbitConfiguration rabbitConfiguration = config.getRabbitConfiguration();
        this.factory = new ConnectionFactory();
        this.factory.setHost(rabbitConfiguration.getHost());
        this.factory.setUsername(rabbitConfiguration.getUsername());
        this.factory.setPassword(rabbitConfiguration.getPassword());
        this.factory.setPort(rabbitConfiguration.getPort());
        this.factory.setVirtualHost(rabbitConfiguration.getVhost());
        this.queue = rabbitConfiguration.getQueue();
        init();
    }

    public Channel getChannel() {
        return channel;
    }

    public ConnectionFactory getFactory() {
        return factory;
    }

    private void setUpConnection() {
        try {
            connection = factory.newConnection();
        } catch (IOException | TimeoutException e) {
            e.printStackTrace();
        }
    }

    private void setUpChannel() {
        try {
            if (connection != null) {
                channel = connection.createChannel();
            }
        } catch (IOException e) {
            e.printStackTrace();
        }
    }

    private void init() {
        setUpConnection();
        setUpChannel();
    }

    public boolean sendMessage(String message) {
        int tries = 0;
        while (tries < 3 && channel != null) {
            try {
                // channel.queueDeclare(queue, durable, exclusive, autodelete, null);
                log.info("Trying to send message to a broker: " + message);
                channel.queueDeclare(queue, true, false, false, null);
                //channel.basicPublish(exchange, queue, properties, encoding);
                channel.basicPublish("", queue, MessageProperties.PERSISTENT_TEXT_PLAIN, message.getBytes("UTF-8"));
                return true;
            } catch (IOException e) {
                e.printStackTrace();
                init();
            }
            tries++;
        }
        return false;
    }
}