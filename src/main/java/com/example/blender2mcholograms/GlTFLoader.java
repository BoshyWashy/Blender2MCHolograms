package com.example.blender2mcholograms;

import com.google.gson.JsonElement;
import com.google.gson.JsonObject;
import com.google.gson.JsonParser;

import java.nio.ByteBuffer;
import java.nio.ByteOrder;
import java.nio.file.Files;
import java.nio.file.Path;
import java.util.ArrayList;
import java.util.List;

/*
  Minimal GLB reader (positions/normals/uvs/indices + baseColorFactor).
  For robust texture/materials, replace with a full glTF library.
*/
public class GlTFLoader {
    private final Path path;

    public GlTFLoader(Path path) { this.path = path; }

    public ModelLoader.LoadedModel load() throws Exception {
        byte[] all = Files.readAllBytes(path);
        ByteBuffer bb = ByteBuffer.wrap(all).order(ByteOrder.LITTLE_ENDIAN);

        int magic = bb.getInt();
        if (magic != 0x46546C67) throw new IllegalArgumentException("Not GLB");
        bb.getInt(); // version
        bb.getInt(); // length

        int jsonLen = bb.getInt();
        int jsonType = bb.getInt();
        byte[] jsonBytes = new byte[jsonLen];
        bb.get(jsonBytes);
        String json = new String(jsonBytes, "UTF-8");

        // BIN chunk optional
        byte[] bin = null;
        if (bb.remaining() >= 8) {
            int binLen = bb.getInt();
            int binType = bb.getInt();
            if (binType == 0x004E4942) {
                bin = new byte[binLen];
                bb.get(bin);
            }
        }

        JsonObject root = JsonParser.parseString(json).getAsJsonObject();

        List<ModelLoader.Mesh> meshes = new ArrayList<>();
        float cx = 0, cy = 0, cz = 0; int posCount = 0;

        for (JsonElement meshEl : root.getAsJsonArray("meshes")) {
            JsonObject meshObj = meshEl.getAsJsonObject();
            for (JsonElement primEl : meshObj.getAsJsonArray("primitives")) {
                JsonObject prim = primEl.getAsJsonObject();
                JsonObject attrs = prim.getAsJsonObject("attributes");
                Integer posAcc = attrs.has("POSITION") ? attrs.get("POSITION").getAsInt() : null;
                Integer norAcc = attrs.has("NORMAL") ? attrs.get("NORMAL").getAsInt() : null;
                Integer uvAcc = attrs.has("TEXCOORD_0") ? attrs.get("TEXCOORD_0").getAsInt() : null;
                Integer idxAcc = prim.has("indices") ? prim.get("indices").getAsInt() : null;
                Integer matIdx = prim.has("material") ? prim.get("material").getAsInt() : null;

                float[] positions = readFloatAccessor(root, posAcc, bin);
                float[] normals = norAcc == null ? new float[0] : readFloatAccessor(root, norAcc, bin);
                float[] uvs = uvAcc == null ? new float[0] : readFloatAccessor(root, uvAcc, bin);
                int[] indices = idxAcc == null ? new int[0] : readIndexAccessor(root, idxAcc, bin);

                for (int i = 0; i < positions.length; i += 3) { cx += positions[i]; cy += positions[i+1]; cz += positions[i+2]; posCount++; }

                float[] baseColor = new float[]{1,1,1,1};
                if (matIdx != null && root.has("materials")) {
                    JsonObject mat = root.getAsJsonArray("materials").get(matIdx).getAsJsonObject();
                    JsonObject pbr = mat.has("pbrMetallicRoughness") ? mat.getAsJsonObject("pbrMetallicRoughness") : null;
                    if (pbr != null && pbr.has("baseColorFactor")) {
                        var a = pbr.getAsJsonArray("baseColorFactor");
                        baseColor = new float[]{ a.get(0).getAsFloat(), a.get(1).getAsFloat(), a.get(2).getAsFloat(), a.get(3).getAsFloat() };
                    }
                }

                meshes.add(new ModelLoader.Mesh(positions, normals, uvs, indices, new ModelLoader.Material(baseColor, null)));
            }
        }

        float centerX = posCount == 0 ? 0 : cx / posCount;
        float centerY = posCount == 0 ? 0 : cy / posCount;
        float centerZ = posCount == 0 ? 0 : cz / posCount;

        return new ModelLoader.LoadedModel(meshes.toArray(new ModelLoader.Mesh[0]), centerX, centerY, centerZ);
    }

    private float[] readFloatAccessor(JsonObject root, Integer idx, byte[] bin) throws Exception {
        if (idx == null) return new float[0];
        JsonObject acc = root.getAsJsonArray("accessors").get(idx).getAsJsonObject();
        int count = acc.get("count").getAsInt();
        String type = acc.get("type").getAsString(); // VEC3/VEC2
        int numComp = type.equals("VEC3") ? 3 : 2;
        int bufferView = acc.get("bufferView").getAsInt();
        JsonObject bv = root.getAsJsonArray("bufferViews").get(bufferView).getAsJsonObject();
        int byteOffset = bv.has("byteOffset") ? bv.get("byteOffset").getAsInt() : 0;
        int byteStride = bv.has("byteStride") ? bv.get("byteStride").getAsInt() : numComp * 4;

        float[] out = new float[count * numComp];
        ByteBuffer bb = ByteBuffer.wrap(bin).order(ByteOrder.LITTLE_ENDIAN);
        for (int i = 0; i < count; i++) {
            int base = byteOffset + i * byteStride;
            for (int c = 0; c < numComp; c++) out[i * numComp + c] = bb.getFloat(base + c * 4);
        }
        return out;
    }

    private int[] readIndexAccessor(JsonObject root, Integer idx, byte[] bin) throws Exception {
        if (idx == null) return new int[0];
        JsonObject acc = root.getAsJsonArray("accessors").get(idx).getAsJsonObject();
        int count = acc.get("count").getAsInt();
        int componentType = acc.get("componentType").getAsInt(); // 5123=ushort,5125=uint
        int bufferView = acc.get("bufferView").getAsInt();
        JsonObject bv = root.getAsJsonArray("bufferViews").get(bufferView).getAsJsonObject();
        int byteOffset = bv.has("byteOffset") ? bv.get("byteOffset").getAsInt() : 0;
        int stride = (componentType == 5123) ? 2 : 4;

        int[] out = new int[count];
        ByteBuffer bb = ByteBuffer.wrap(bin).order(ByteOrder.LITTLE_ENDIAN);
        for (int i = 0; i < count; i++) {
            int pos = byteOffset + i * stride;
            out[i] = (componentType == 5123) ? (bb.getShort(pos) & 0xFFFF) : bb.getInt(pos);
        }
        return out;
    }
}
