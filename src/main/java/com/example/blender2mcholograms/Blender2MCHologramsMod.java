package com.example.blender2mcholograms;

import net.fabricmc.api.ClientModInitializer;
import net.fabricmc.api.EnvType;
import net.fabricmc.api.Environment;
import net.fabricmc.fabric.api.client.event.lifecycle.v1.ClientTickEvents;

@Environment(EnvType.CLIENT)
public class Blender2MCHologramsMod implements ClientModInitializer {
    public static final String MODID = "blender2mcholograms";
    public static Blender2MCHologramsMod INSTANCE;

    private ConfigManager config;
    private ModelLoader modelLoader;
    private Renderer renderer;
    private Commands commands;
    private Keybinds keybinds;

    @Override
    public void onInitializeClient() {
        INSTANCE = this;
        this.config = new ConfigManager();
        this.modelLoader = new ModelLoader(config);
        this.renderer = new Renderer(config, modelLoader);
        this.commands = new Commands(modelLoader, config, renderer);
        this.keybinds = new Keybinds(config);

        config.load();
        keybinds.register();
        modelLoader.loadModelIfPresent();
        renderer.registerRenderTick();
        commands.register();

        ClientTickEvents.END_CLIENT_TICK.register(client -> keybinds.pollAndHandle(client));
    }

    public ConfigManager getConfig() { return config; }
    public ModelLoader getModelLoader() { return modelLoader; }
    public Renderer getRenderer() { return renderer; }
}
