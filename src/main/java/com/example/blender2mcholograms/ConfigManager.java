package com.example.blender2mcholograms;

import com.google.gson.Gson;
import com.google.gson.GsonBuilder;
import com.google.gson.JsonObject;
import com.google.gson.JsonParser;

import java.io.Reader;
import java.io.Writer;
import java.nio.file.Files;
import java.nio.file.Path;
import java.nio.file.Paths;

public class ConfigManager {
    private static final Path CONFIG_DIR = Paths.get(System.getProperty("user.home"), ".minecraft", "config", "blender2mcholograms");
    private static final Path CONFIG_FILE = CONFIG_DIR.resolve("config.json");

    public boolean enabled = false;            // default: Disabled
    public float opacity = 0.8f;               // default: 80%
    public int visibleRadius = 50;             // default: 50 blocks
    public float rotationDegrees = 0f;         // default: 0 (clockwise)
    public float offsetX = 0f;
    public float offsetY = 0f;
    public float offsetZ = 0f;
    public String blenderExecutable = "";      // optional path to Blender
    public String modelFileName = "model.glb";
    // hotkey KeyBinding stored in Keybinds class

    public ConfigManager() {
        try { Files.createDirectories(CONFIG_DIR); } catch (Exception ignored) {}
    }

    public void load() {
        try {
            if (Files.exists(CONFIG_FILE)) {
                try (Reader r = Files.newBufferedReader(CONFIG_FILE)) {
                    JsonObject o = JsonParser.parseReader(r).getAsJsonObject();
                    if (o.has("enabled")) enabled = o.get("enabled").getAsBoolean();
                    if (o.has("opacity")) opacity = o.get("opacity").getAsFloat();
                    if (o.has("visibleRadius")) visibleRadius = o.get("visibleRadius").getAsInt();
                    if (o.has("rotationDegrees")) rotationDegrees = o.get("rotationDegrees").getAsFloat();
                    if (o.has("offsetX")) offsetX = o.get("offsetX").getAsFloat();
                    if (o.has("offsetY")) offsetY = o.get("offsetY").getAsFloat();
                    if (o.has("offsetZ")) offsetZ = o.get("offsetZ").getAsFloat();
                    if (o.has("blenderExecutable")) blenderExecutable = o.get("blenderExecutable").getAsString();
                    if (o.has("modelFileName")) modelFileName = o.get("modelFileName").getAsString();
                }
            } else {
                save();
            }
        } catch (Exception e) {
            e.printStackTrace();
        }
    }

    public void save() {
        try {
            JsonObject o = new JsonObject();
            o.addProperty("enabled", enabled);
            o.addProperty("opacity", opacity);
            o.addProperty("visibleRadius", visibleRadius);
            o.addProperty("rotationDegrees", rotationDegrees);
            o.addProperty("offsetX", offsetX);
            o.addProperty("offsetY", offsetY);
            o.addProperty("offsetZ", offsetZ);
            o.addProperty("blenderExecutable", blenderExecutable);
            o.addProperty("modelFileName", modelFileName);
            try (Writer w = Files.newBufferedWriter(CONFIG_FILE)) {
                new GsonBuilder().setPrettyPrinting().create().toJson(o, w);
            }
        } catch (Exception e) {
            e.printStackTrace();
        }
    }

    public Path getModelsFolder() { return CONFIG_DIR.resolve("models"); }
    public Path getModelPath() { return getModelsFolder().resolve(modelFileName); }

    public void ensureModelsFolder() {
        try { Files.createDirectories(getModelsFolder()); } catch (Exception ignored) {}
    }
}
