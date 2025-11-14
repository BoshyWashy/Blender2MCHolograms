package com.example.blender2mcholograms;

import com.mojang.brigadier.arguments.FloatArgumentType;
import com.mojang.brigadier.arguments.IntegerArgumentType;
import com.mojang.brigadier.arguments.StringArgumentType;
import net.fabricmc.fabric.api.client.command.v2.ClientCommandRegistrationCallback;
import net.minecraft.client.MinecraftClient;
import net.minecraft.text.Text;
import net.minecraft.util.Formatting;

import static net.fabricmc.fabric.api.client.command.v2.ClientCommandManager.argument;
import static net.fabricmc.fabric.api.client.command.v2.ClientCommandManager.literal;

public class Commands {
    private final ModelLoader loader;
    private final ConfigManager config;
    private final Renderer renderer;

    public Commands(ModelLoader loader, ConfigManager config, Renderer renderer) {
        this.loader = loader; this.config = config; this.renderer = renderer;
    }

    public void register() {
        ClientCommandRegistrationCallback.EVENT.register((dispatcher, registryAccess) -> {
            dispatcher.register(literal("blender2mchologram")
                    .then(literal("centre").executes(ctx -> {
                        var player = MinecraftClient.getInstance().player;
                        if (player == null) return 0;
                        config.offsetX = (float) player.getX();
                        config.offsetY = (float) player.getY();
                        config.offsetZ = (float) player.getZ();
                        config.save();
                        player.sendMessage(Text.literal("Blender model center moved to your position").formatted(Formatting.GREEN));
                        return 1;
                    }))
                    .then(literal("displace")
                            .then(argument("axis", StringArgumentType.word())
                                    .suggests((c, b) -> {
                                        b.suggest("x"); b.suggest("y"); b.suggest("z");
                                        return b.buildFuture();
                                    })
                                    .then(argument("number", FloatArgumentType.floatArg())
                                            .executes(ctx -> {
                                                String axis = StringArgumentType.getString(ctx, "axis").toLowerCase();
                                                float amount = FloatArgumentType.getFloat(ctx, "number");
                                                switch (axis) {
                                                    case "x": config.offsetX = amount; break;
                                                    case "y": config.offsetY = amount; break;
                                                    case "z": config.offsetZ = amount; break;
                                                    default:
                                                        var pl = MinecraftClient.getInstance().player;
                                                        if (pl != null) pl.sendMessage(Text.literal("Axis must be x/y/z").formatted(Formatting.RED));
                                                        return 0;
                                                }
                                                config.save();
                                                var pl = MinecraftClient.getInstance().player;
                                                if (pl != null) pl.sendMessage(Text.literal("Displaced " + axis + " to " + amount).formatted(Formatting.GREEN));
                                                return 1;
                                            })
                                    )
                            )
                    )
                    .then(literal("rotateclockwise")
                            .then(argument("number", FloatArgumentType.floatArg())
                                    .executes(ctx -> {
                                        float deg = FloatArgumentType.getFloat(ctx, "number");
                                        config.rotationDegrees = deg;
                                        config.save();
                                        var pl = MinecraftClient.getInstance().player;
                                        if (pl != null) pl.sendMessage(Text.literal("Rotation set to " + deg).formatted(Formatting.GREEN));
                                        return 1;
                                    })
                            )
                    )
                    .then(literal("visibility")
                            .then(argument("radius", IntegerArgumentType.integer(0))
                                    .executes(ctx -> {
                                        int r = IntegerArgumentType.getInteger(ctx, "radius");
                                        config.visibleRadius = r;
                                        config.save();
                                        var pl = MinecraftClient.getInstance().player;
                                        if (pl != null) pl.sendMessage(Text.literal("Visibility radius set to " + r).formatted(Formatting.GREEN));
                                        return 1;
                                    })
                            )
                    )
                    .then(literal("enable").executes(ctx -> {
                        config.enabled = true; config.save();
                        var pl = MinecraftClient.getInstance().player;
                        if (pl != null) pl.sendMessage(Text.literal("Blender2MCHolograms enabled").formatted(Formatting.GREEN));
                        return 1;
                    }))
                    .then(literal("disable").executes(ctx -> {
                        config.enabled = false; config.save();
                        var pl = MinecraftClient.getInstance().player;
                        if (pl != null) pl.sendMessage(Text.literal("Blender2MCHolograms disabled").formatted(Formatting.YELLOW));
                        return 1;
                    }))
                    .then(literal("opacity")
                            .then(argument("value", FloatArgumentType.floatArg(0f, 1f))
                                    .executes(ctx -> {
                                        float v = FloatArgumentType.getFloat(ctx, "value");
                                        config.opacity = v; config.save();
                                        var pl = MinecraftClient.getInstance().player;
                                        if (pl != null) pl.sendMessage(Text.literal("Opacity set to " + (int) (v * 100) + "%").formatted(Formatting.GREEN));
                                        return 1;
                                    })
                            )
                    )
                    .then(literal("reload").executes(ctx -> {
                        loader.reload();
                        var pl = MinecraftClient.getInstance().player;
                        if (pl != null) pl.sendMessage(Text.literal("Blender2MCHolograms reloaded models").formatted(Formatting.GREEN));
                        return 1;
                    }))
            );
        });
    }
}
