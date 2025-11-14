package com.example.blender2mcholograms;

import net.fabricmc.fabric.api.client.keybinding.v1.KeyBindingHelper;
import net.minecraft.client.MinecraftClient;
import net.minecraft.client.option.KeyBinding;
import net.minecraft.client.util.InputUtil;
import net.minecraft.text.Text;

public class Keybinds {
    private final ConfigManager config;
    private KeyBinding toggleBind;

    public Keybinds(ConfigManager config) {
        this.config = config;
    }

    public void register() {
        toggleBind = KeyBindingHelper.registerKeyBinding(new KeyBinding(
                "key.blender2mcholograms.toggle",
                InputUtil.Type.KEYSYM,
                InputUtil.UNKNOWN_KEY.getCode(), // Not bound by default
                "category.blender2mcholograms"
        ));
    }

    public void pollAndHandle(MinecraftClient client) {
        if (toggleBind != null && toggleBind.wasPressed()) {
            config.enabled = !config.enabled;
            config.save();
            if (client.player != null) {
                client.player.sendMessage(Text.literal("Blender2MCHolograms " + (config.enabled ? "enabled" : "disabled")), false);
            }
        }
    }
}
