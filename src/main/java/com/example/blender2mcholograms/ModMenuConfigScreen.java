package com.example.blender2mcholograms;

import net.minecraft.client.MinecraftClient;
import net.minecraft.client.gui.screen.Screen;
import net.minecraft.client.gui.widget.ButtonWidget;
import net.minecraft.client.gui.widget.TextFieldWidget;
import net.minecraft.client.util.math.MatrixStack;
import net.minecraft.text.Text;

public class ModMenuConfigScreen {
    static class ConfigScreen extends Screen {
        private final Screen parent;
        private final ConfigManager config;

        protected ConfigScreen(Screen parent) {
            super(Text.literal("Blender2MCHolograms Settings"));
            this.parent = parent;
            this.config = Blender2MCHologramsMod.INSTANCE.getConfig();
        }

        @Override
        protected void init() {
            int y = this.height / 4;

            addDrawableChild(ButtonWidget.builder(Text.literal("Enabled: " + (config.enabled ? "True" : "False")), (btn) -> {
                config.enabled = !config.enabled;
                btn.setMessage(Text.literal("Enabled: " + (config.enabled ? "True" : "False")));
                config.save();
            }).dimensions(this.width / 2 - 150, y, 300, 20).build());

            y += 24;
            TextFieldWidget opacityField = new TextFieldWidget(this.textRenderer, this.width / 2 - 150, y, 300, 20, Text.literal("Opacity"));
            opacityField.setText(String.valueOf(config.opacity));
            addDrawableChild(opacityField);

            y += 24;
            addDrawableChild(ButtonWidget.builder(Text.literal("Apply Opacity"), (btn) -> {
                try {
                    float v = Float.parseFloat(opacityField.getText());
                    config.opacity = Math.max(0f, Math.min(1f, v));
                    config.save();
                } catch (NumberFormatException ignored) {}
            }).dimensions(this.width / 2 - 150, y, 300, 20).build());

            y += 24;
            TextFieldWidget visField = new TextFieldWidget(this.textRenderer, this.width / 2 - 150, y, 300, 20, Text.literal("Visible radius"));
            visField.setText(String.valueOf(config.visibleRadius));
            addDrawableChild(visField);

            y += 24;
            addDrawableChild(ButtonWidget.builder(Text.literal("Apply Visible Radius"), (btn) -> {
                try {
                    int r = Integer.parseInt(visField.getText());
                    config.visibleRadius = Math.max(0, r);
                    config.save();
                } catch (NumberFormatException ignored) {}
            }).dimensions(this.width / 2 - 150, y, 300, 20).build());

            y += 24;
            TextFieldWidget rotField = new TextFieldWidget(this.textRenderer, this.width / 2 - 150, y, 95, 20, Text.literal("Rotation"));
            rotField.setText(String.valueOf(config.rotationDegrees));
            addDrawableChild(rotField);

            TextFieldWidget xField = new TextFieldWidget(this.textRenderer, this.width / 2 - 50, y, 50, 20, Text.literal("X"));
            xField.setText(String.valueOf(config.offsetX));
            addDrawableChild(xField);

            TextFieldWidget yField = new TextFieldWidget(this.textRenderer, this.width / 2 + 10, y, 50, 20, Text.literal("Y"));
            yField.setText(String.valueOf(config.offsetY));
            addDrawableChild(yField);

            TextFieldWidget zField = new TextFieldWidget(this.textRenderer, this.width / 2 + 70, y, 50, 20, Text.literal("Z"));
            zField.setText(String.valueOf(config.offsetZ));
            addDrawableChild(zField);

            y += 24;
            addDrawableChild(ButtonWidget.builder(Text.literal("Apply Transform"), (btn) -> {
                try {
                    config.rotationDegrees = Float.parseFloat(rotField.getText());
                    config.offsetX = Float.parseFloat(xField.getText());
                    config.offsetY = Float.parseFloat(yField.getText());
                    config.offsetZ = Float.parseFloat(zField.getText());
                    config.save();
                } catch (NumberFormatException ignored) {}
            }).dimensions(this.width / 2 - 150, y, 300, 20).build());

            addDrawableChild(ButtonWidget.builder(Text.literal("Reload Model"), (btn) -> {
                Blender2MCHologramsMod.INSTANCE.getModelLoader().reload();
            }).dimensions(this.width / 2 - 150, this.height - 40, 150, 20).build());

            addDrawableChild(ButtonWidget.builder(Text.literal("Done"), (btn) -> {
                MinecraftClient.getInstance().setScreen(parent);
            }).dimensions(this.width / 2 + 10, this.height - 40, 150, 20).build());
        }

        @Override
        public void render(MatrixStack matrices, int mouseX, int mouseY, float delta) {
            this.renderBackground(matrices);
            drawCenteredText(matrices, this.textRenderer, this.title, this.width / 2, 20, 0xFFFFFF);
            super.render(matrices, mouseX, mouseY, delta);
        }
    }
}
