package nl.sjtek.control.hue;

import com.google.common.base.Charsets;
import com.google.common.io.Files;
import com.google.gson.Gson;

import java.io.File;
import java.io.IOException;
import java.util.Map;

/**
 * Created by wouter on 15-3-17.
 */
public class Config {

    private static Config instance;
    private final String amqpUsername;
    private final String amqpPassword;
    private final String amqpHost;
    private final String hueHost;
    private final String hueUser;
    private final Map<String, String> lights;

    public Config(String amqpUsername, String amqpPassword, String amqpHost, String hueHost, String hueUser, Map<String, String> lights) {
        this.amqpUsername = amqpUsername;
        this.amqpPassword = amqpPassword;
        this.amqpHost = amqpHost;
        this.hueHost = hueHost;
        this.hueUser = hueUser;
        this.lights = lights;
    }

    public synchronized static Config getInstance() {
        return instance;
    }

    public synchronized static Config init(String path) throws IOException {
        String json = Files.toString(new File(path), Charsets.UTF_8);
        instance = new Gson().fromJson(json, Config.class);
        if (instance == null) throw new NullPointerException("Error while reading the config");
        return instance;
    }

    public String toJson() {
        return new Gson().toJson(this);
    }

    public String getAmqpUsername() {
        return amqpUsername;
    }

    public String getAmqpPassword() {
        return amqpPassword;
    }

    public String getAmqpHost() {
        return amqpHost;
    }

    public String getHueHost() {
        return hueHost;
    }

    public String getHueUser() {
        return hueUser;
    }

    public String getRoom(int sjtekLightId) {
        return lights.get(String.valueOf(sjtekLightId));
    }

    public int getSjtekLightId(String room) {
        for (Map.Entry<String, String> entry : lights.entrySet()) {
            if (entry.getValue().equals(room)) return Integer.valueOf(entry.getKey());
        }
        System.err.println("Room not found: " + room);
        return -1;
    }
}
