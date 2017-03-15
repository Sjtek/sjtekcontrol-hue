package nl.sjtek.control.hue;

import java.io.IOException;

/**
 * Created by wouter on 15-3-17.
 */
public class SjtekControlHue {

    public static void main(String args[]) throws IOException {
        String configPath = "/opt/sjtekcontrol-hue/config.json";
        if (args.length == 1) {
            configPath = args[0];
        }
        Config config = Config.init(configPath);
        Log log = new Log();
        AMQP amqp = new AMQP(config.getAmqpHost(), config.getAmqpUsername(), config.getAmqpPassword());
        Hue hue = new Hue(config.getHueHost(), config.getHueUser());
    }
}
