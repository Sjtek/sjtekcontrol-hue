package nl.sjtek.control.hue;

import com.google.common.eventbus.Subscribe;
import com.philips.lighting.hue.listener.PHLightListener;
import com.philips.lighting.hue.sdk.PHAccessPoint;
import com.philips.lighting.hue.sdk.PHHueSDK;
import com.philips.lighting.hue.sdk.PHMessageType;
import com.philips.lighting.hue.sdk.PHSDKListener;
import com.philips.lighting.hue.sdk.heartbeat.PHHeartbeatManager;
import com.philips.lighting.hue.sdk.utilities.PHUtilities;
import com.philips.lighting.model.*;
import io.habets.javautils.Bus;
import nl.sjtek.control.data.amqp.SwitchEvent;
import nl.sjtek.control.data.amqp.SwitchStateEvent;
import nl.sjtek.control.hue.events.HueConnectedEvent;
import nl.sjtek.control.hue.events.ShutdownEvent;

import java.util.HashMap;
import java.util.List;
import java.util.Map;

/**
 * Created by wouter on 15-3-17.
 */
public class Hue {
    private static final long HB_LIGHT = 1000;
    private static final long HB_FULL = 5000;
    private static final String SYNC_STATE = "light_states";
    private final SDKListener sdkListener = new SDKListener();
    private final PHHueSDK sdk;
    private final Map<Integer, Boolean> lightStates = new HashMap<>();

    public Hue(String host, String username) {
        sdk = PHHueSDK.getInstance();
        sdk.getNotificationManager().registerSDKListener(sdkListener);
        PHAccessPoint accessPoint = new PHAccessPoint();
        accessPoint.setUsername(username);
        accessPoint.setIpAddress(host);
        sdk.connect(accessPoint);
        Bus.regsiter(this);
    }

    @Subscribe
    public void stop(ShutdownEvent event) {
        sdk.removeBridge(sdk.getSelectedBridge());
        sdk.destroySDK();
    }

    @Subscribe
    public void onLightEvent(SwitchEvent event) {
        String roomId = Config.getInstance().getRoom(event.getId());
        if (roomId == null) return;
        System.out.println(event.toString());
        PHLightState state = new PHLightState();
        if (event.getState() && event.getUseRgb()) {
            float xy[] = PHUtilities.calculateXYFromRGB(event.getR(), event.getG(), event.getB(), null);
            state.setX(xy[0]);
            state.setY(xy[1]);
        }
        state.setOn(event.getState());

        sdk.getSelectedBridge().updateLightState(roomId, state, new PHLightListener() {
            @Override
            public void onReceivingLightDetails(PHLight phLight) {

            }

            @Override
            public void onReceivingLights(List<PHBridgeResource> list) {

            }

            @Override
            public void onSearchComplete() {

            }

            @Override
            public void onSuccess() {

            }

            @Override
            public void onError(int i, String s) {
                System.err.println("Lamp error " + s);
            }

            @Override
            public void onStateUpdate(Map<String, String> map, List<PHHueError> list) {

            }
        });
    }

    private class SDKListener implements PHSDKListener {

        @Override
        public void onCacheUpdated(List<Integer> list, PHBridge phBridge) {
            if (list.contains(PHMessageType.LIGHTS_CACHE_UPDATED)) {
                PHBridgeResourcesCache cache = phBridge.getResourceCache();
                synchronized (SYNC_STATE) {
                    lightStates.clear();
                    for (PHLight light : cache.getAllLights()) {
                        boolean enabled = light.getLastKnownLightState().isOn();
                        String lightId = light.getIdentifier();
                        int sjtekLightId = Config.getInstance().getSjtekLightId(lightId);
                        if (sjtekLightId != -1) {
                            lightStates.put(sjtekLightId, enabled);
                            Bus.post(new SwitchStateEvent(sjtekLightId, enabled));
                        }
                    }
                }
            }
        }

        @Override
        public void onBridgeConnected(PHBridge phBridge, String s) {
            sdk.setSelectedBridge(phBridge);
            PHHeartbeatManager heartbeatManager = sdk.getHeartbeatManager();
            heartbeatManager.enableLightsHeartbeat(phBridge, HB_LIGHT);
            heartbeatManager.enableFullConfigHeartbeat(phBridge, HB_FULL);
            Bus.post(new HueConnectedEvent());
        }

        @Override
        public void onAuthenticationRequired(PHAccessPoint phAccessPoint) {

        }

        @Override
        public void onAccessPointsFound(List<PHAccessPoint> list) {

        }

        @Override
        public void onError(int i, String s) {
            System.err.println("Cache error " + s);
        }

        @Override
        public void onConnectionResumed(PHBridge phBridge) {

        }

        @Override
        public void onConnectionLost(PHAccessPoint phAccessPoint) {

        }

        @Override
        public void onParsingErrors(List<PHHueParsingError> list) {

        }
    }

}
