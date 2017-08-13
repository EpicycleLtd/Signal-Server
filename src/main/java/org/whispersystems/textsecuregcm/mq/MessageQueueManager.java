package org.whispersystems.textsecuregcm.mq;

import com.rabbitmq.client.*;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.io.IOException;
import java.util.concurrent.TimeoutException;

import com.rabbitmq.client.MessageProperties;


public class MessageQueueManager {

    private final Logger log = LoggerFactory.getLogger(MessageQueueManager.class);

    private Connection connection;
    private Channel channel;
    private ConnectionFactory factory;

    private String queue;

    public MessageQueueManager(String host,
                               int port,
                               String vhost,
                               String username,
                               String password,
                               String queue) {

        this.factory = new ConnectionFactory();
            this.factory.setHost(host);
            this.factory.setUsername(username);
            this.factory.setPassword(password);
            this.factory.setPort(port);
            this.factory.setVirtualHost(vhost);

        this.queue = queue;
        init();
    }

    public String getQueue() {
        return queue;
    }

    public Connection getConnection() {
        return connection;
    }

    public Channel getChannel() {
        return channel;
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
                channel.queueDeclare(queue, true, false, false, null);
                channel.basicPublish(
                    "",
                    queue,
                    MessageProperties.PERSISTENT_TEXT_PLAIN,
                    message.getBytes("UTF-8")
                );
                log.debug("Send message to broker: " + message);
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
