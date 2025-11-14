package com.example.blender2mcholograms;

import java.nio.file.Files;
import java.nio.file.Path;
import java.util.Optional;

public class ModelLoader {
    private final ConfigManager config;
    private volatile LoadedModel currentModel;

    public ModelLoader(ConfigManager config) {
        this.config = config;
        this.config.ensureModelsFolder();
    }

    public void loadModelIfPresent() {
        try {
            Path modelsDir = config.getModelsFolder();
            Optional<Path> glb = Files.list(modelsDir)
                    .filter(p -> {
                        String n = p.getFileName().toString().toLowerCase();
                        return n.endsWith(".glb") || n.endsWith(".gltf");
                    })
                    .findFirst();

            if (glb.isPresent()) {
                loadFromGlb(glb.get());
                config.modelFileName = glb.get().getFileName().toString();
                config.save();
                return;
            }

            Optional<Path> blend = Files.list(modelsDir)
                    .filter(p -> p.getFileName().toString().toLowerCase().endsWith(".blend"))
                    .findFirst();

            if (blend.isPresent()) {
                Path exported = tryExportBlendToGlb(blend.get());
                if (exported != null) {
                    loadFromGlb(exported);
                    config.modelFileName = exported.getFileName().toString();
                    config.save();
                    return;
                }
            }

            currentModel = null;
        } catch (Exception ex) {
            ex.printStackTrace();
        }
    }

    private Path tryExportBlendToGlb(Path blendFile) {
        String blenderExe = (config.blenderExecutable == null || config.blenderExecutable.isEmpty())
                ? "blender"
                : config.blenderExecutable;
        Path out = config.getModelsFolder().resolve("exported_model.glb");
        try {
            ProcessBuilder pb = new ProcessBuilder(
                    blenderExe, "-b",
                    blendFile.toAbsolutePath().toString(),
                    "--python-expr",
                    "import bpy; bpy.ops.export_scene.gltf(filepath=r'" + out.toAbsolutePath() + "', export_format='GLB')"
            );
            pb.redirectErrorStream(true);
            Process p = pb.start();
            int exit = p.waitFor();
            if (exit == 0 && Files.exists(out)) return out;
        } catch (Exception ignored) {}
        return null;
    }

    private void loadFromGlb(Path glb) {
        try {
            GlTFLoader loader = new GlTFLoader(glb);
            LoadedModel model = loader.load();
            this.currentModel = model;
        } catch (Exception ex) {
            ex.printStackTrace();
            this.currentModel = null;
        }
    }

    public LoadedModel getCurrentModel() { return currentModel; }
    public void reload() { loadModelIfPresent(); }

    public static class LoadedModel {
        public final Mesh[] meshes;
        public final float centerX, centerY, centerZ;
        public LoadedModel(Mesh[] meshes, float centerX, float centerY, float centerZ) {
            this.meshes = meshes; this.centerX = centerX; this.centerY = centerY; this.centerZ = centerZ;
        }
    }

    public static class Mesh {
        public final float[] vertices;
        public final float[] normals;
        public final float[] uvs;
        public final int[] indices;
        public final Material material;
        public Mesh(float[] vertices, float[] normals, float[] uvs, int[] indices, Material material) {
            this.vertices = vertices; this.normals = normals; this.uvs = uvs; this.indices = indices; this.material = material;
        }
    }

    public static class Material {
        public final float[] baseColor; // r,g,b,a
        public final String texturePath; // optional
        public Material(float[] baseColor, String texturePath) { this.baseColor = baseColor; this.texturePath = texturePath; }
    }
}
