package nl.sjtek.control.hue;

import com.google.common.eventbus.Subscribe;
import com.rabbitmq.client.*;
import io.habets.javautils.Bus;
import io.habets.javautils.PingThread;
import nl.sjtek.control.data.amqp.SwitchEvent;
import nl.sjtek.control.data.amqp.SwitchStateEvent;
import nl.sjtek.control.hue.events.ShutdownEvent;

import java.io.IOException;
import java.util.concurrent.TimeoutException;

/**
 * Created by wouter on 15-3-17.
 */
public class AMQP {
    private static final String EXCHANGE_LIGHTS = "lights";
    private static final String EXCHANGE_LIGHTS_STATE = "lights_state";
    private final ConnectionFactory factory;
    private Channel channelAction;
    private Channel channelStates;
    private Connection connection;

    public AMQP(String host, String username, String password) {
        factory = new ConnectionFactory();
        factory.setHost(host);
        factory.setUsername(username);
        factory.setPassword(password);
        factory.setAutomaticRecoveryEnabled(true);
        new PingThread("https://sjtek.nl/rabbitmq", 1000, this::connect).start();
        Bus.regsiter(this);
    }

    private void connect() {
        try {
            connection = factory.newConnection();

            channelAction = connection.createChannel();
            channelAction.exchangeDeclare(EXCHANGE_LIGHTS, "fanout");
            String updateQueueName = channelAction.queueDeclare().getQueue();
            channelAction.queueBind(updateQueueName, EXCHANGE_LIGHTS, "");
            channelAction.basicConsume(updateQueueName, true, new MessageConsumer(channelAction));

            channelStates = connection.createChannel();
            channelStates.exchangeDeclare(EXCHANGE_LIGHTS_STATE, "fanout");

            System.out.println("Connected to broker.");
        } catch (IOException | TimeoutException e) {
            e.printStackTrace();
        }
    }

    @Subscribe
    public void disconnect(ShutdownEvent event) {
        try {
            if (channelAction != null) channelAction.close();
            if (channelStates != null) channelStates.close();
            if (connection != null) connection.close();
        } catch (IOException | TimeoutException e) {
            e.printStackTrace();
        }
    }

    @Subscribe
    public void onStateUpdateEvent(SwitchStateEvent event) {
        try {
            if (channelStates != null)
                channelStates.basicPublish(EXCHANGE_LIGHTS_STATE, "", null, event.toMessage());
        } catch (IOException e) {
            e.printStackTrace();
        }
    }

    private static class MessageConsumer extends DefaultConsumer {

        /**
         * Constructs a new instance and records its association to the passed-in channel.
         *
         * @param channel the channel to which this consumer is attached
         */
        public MessageConsumer(Channel channel) {
            super(channel);
        }

        @Override
        public void handleDelivery(String consumerTag, Envelope envelope, com.rabbitmq.client.AMQP.BasicProperties properties, byte[] body) throws IOException {
            SwitchEvent event = SwitchEvent.Companion.fromMessage(new String(body));
            if (event == null) return;
            Bus.post(event);
        }
    }
}
