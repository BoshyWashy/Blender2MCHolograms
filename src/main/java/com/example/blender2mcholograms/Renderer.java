package com.example.blender2mcholograms;

import com.example.blender2mcholograms.ModelLoader.LoadedModel;
import com.example.blender2mcholograms.ModelLoader.Mesh;
import net.fabricmc.api.EnvType;
import net.fabricmc.api.Environment;
import net.fabricmc.fabric.api.client.rendering.v1.WorldRenderEvents;
import net.minecraft.client.MinecraftClient;
import net.minecraft.client.util.math.MatrixStack;
import net.minecraft.util.math.RotationAxis;
import net.minecraft.util.math.Vec3d;
import org.lwjgl.opengl.GL11;

@Environment(EnvType.CLIENT)
public class Renderer {
    private final ConfigManager config;
    private final ModelLoader loader;

    public Renderer(ConfigManager config, ModelLoader loader) {
        this.config = config;
        this.loader = loader;
    }

    public void registerRenderTick() {
        WorldRenderEvents.LAST.register(ctx -> render(ctx.matrixStack()));
    }

    private void render(MatrixStack matrices) {
        if (!config.enabled) return;
        LoadedModel model = loader.getCurrentModel();
        if (model == null) return;

        MinecraftClient client = MinecraftClient.getInstance();
        if (client.player == null) return;
        Vec3d playerPos = client.player.getPos();

        matrices.push();
        matrices.translate(playerPos.x, playerPos.y, playerPos.z);

        // Align to Blender project center (model center) then apply displacement
        matrices.translate(config.offsetX - model.centerX, config.offsetY - model.centerY, config.offsetZ - model.centerZ);
        matrices.multiply(RotationAxis.POSITIVE_Y.rotationDegrees(config.rotationDegrees));

        float opacity = config.opacity;

        GL11.glPushAttrib(GL11.GL_ALL_ATTRIB_BITS);
        GL11.glPushMatrix();
        GL11.glEnable(GL11.GL_BLEND);
        GL11.glBlendFunc(GL11.GL_SRC_ALPHA, GL11.GL_ONE_MINUS_SRC_ALPHA);
        GL11.glDisable(GL11.GL_TEXTURE_2D);

        for (Mesh m : model.meshes) {
            float[] v = m.vertices;
            int[] idx = m.indices;
            float[] baseColor = m.material.baseColor;

            GL11.glColor4f(baseColor[0], baseColor[1], baseColor[2], baseColor[3] * opacity);

            GL11.glBegin(GL11.GL_TRIANGLES);
            for (int i = 0; i < idx.length; i += 3) {
                int ia = idx[i] * 3, ib = idx[i + 1] * 3, ic = idx[i + 2] * 3;

                Vec3d pa = new Vec3d(v[ia], v[ia + 1], v[ia + 2]);
                Vec3d pb = new Vec3d(v[ib], v[ib + 1], v[ib + 2]);
                Vec3d pc = new Vec3d(v[ic], v[ic + 1], v[ic + 2]);

                double r2 = (double) config.visibleRadius * (double) config.visibleRadius;

                boolean aIn = pa.squaredDistanceTo(Vec3d.ZERO) <= r2;
                boolean bIn = pb.squaredDistanceTo(Vec3d.ZERO) <= r2;
                boolean cIn = pc.squaredDistanceTo(Vec3d.ZERO) <= r2;

                // We’re rendering in the model’s local space positioned at player, so compare to origin (Vec3d.ZERO)
                if (!(aIn || bIn || cIn)) continue;

                GL11.glVertex3f(v[ia], v[ia + 1], v[ia + 2]);
                GL11.glVertex3f(v[ib], v[ib + 1], v[ib + 2]);
                GL11.glVertex3f(v[ic], v[ic + 1], v[ic + 2]);
            }
            GL11.glEnd();
        }

        GL11.glPopMatrix();
        GL11.glPopAttrib();
        matrices.pop();
    }

    public void setEnabled(boolean enabled) {
        config.enabled = enabled;
        config.save();
    }
}
