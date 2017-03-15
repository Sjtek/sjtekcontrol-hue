package nl.sjtek.control.hue;

import com.google.common.eventbus.Subscribe;
import io.habets.javautils.Bus;
import nl.sjtek.control.hue.events.HueConnectedEvent;

/**
 * Created by wouter on 15-3-17.
 */
public class Log {

    public Log() {
        Bus.regsiter(this);
    }

    @Subscribe
    public void onConnected(HueConnectedEvent event) {
        System.out.println("Hue connected");
    }
}
