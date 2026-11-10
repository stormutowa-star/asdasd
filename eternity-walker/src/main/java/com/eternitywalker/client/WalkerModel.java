package com.eternitywalker.client;

import java.io.InputStream;
import java.io.InputStreamReader;
import java.nio.charset.StandardCharsets;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

import com.google.gson.JsonArray;
import com.google.gson.JsonObject;
import com.google.gson.JsonParser;

/** Modelo del Eternity Walker (exportado del .bbmodel: huesos con quads ya texturizados). */
public final class WalkerModel {
    public static final int NONE = 0, LEG = 1, TURRET_YAW = 2, MAIN_PITCH = 3, SIDE_YAW = 4, SIDE_PITCH = 5,
            RECOIL_MAIN = 6, RECOIL_L = 7, RECOIL_R = 8, SENSOR = 9;

    public static final class Bone {
        public String name;
        public int parent;
        public float px, py, pz;
        public float[] q;
        public int kind;
        public float[] walk;
        public int[] children;
    }

    public static Bone[] bones;
    public static int[] roots;
    private static boolean loaded;

    private WalkerModel() {}

    public static synchronized void load() {
        if (loaded) return;
        try (InputStream in = WalkerModel.class.getResourceAsStream("/assets/eternitywalker/walker.json")) {
            JsonObject root = JsonParser.parseReader(new InputStreamReader(in, StandardCharsets.UTF_8)).getAsJsonObject();
            JsonObject walk = root.getAsJsonObject("walk");
            Map<String, float[]> walkKf = new HashMap<>();
            for (String k : walk.keySet()) {
                JsonArray a = walk.getAsJsonArray(k);
                float[] f = new float[a.size()];
                for (int i = 0; i < f.length; i++) f[i] = a.get(i).getAsFloat();
                walkKf.put(k, f);
            }
            JsonArray ba = root.getAsJsonArray("bones");
            bones = new Bone[ba.size()];
            List<List<Integer>> kids = new ArrayList<>();
            List<Integer> rootList = new ArrayList<>();
            for (int i = 0; i < bones.length; i++) kids.add(new ArrayList<>());
            for (int i = 0; i < bones.length; i++) {
                JsonObject o = ba.get(i).getAsJsonObject();
                Bone b = new Bone();
                b.name = o.get("name").getAsString();
                b.parent = o.get("parent").getAsInt();
                JsonArray pv = o.getAsJsonArray("pivot");
                b.px = pv.get(0).getAsFloat();
                b.py = pv.get(1).getAsFloat();
                b.pz = pv.get(2).getAsFloat();
                JsonArray qa = o.getAsJsonArray("q");
                b.q = new float[qa.size()];
                for (int j = 0; j < b.q.length; j++) b.q[j] = qa.get(j).getAsFloat();
                b.walk = walkKf.get(b.name);
                b.kind = kindOf(b.name, b.walk != null);
                bones[i] = b;
                if (b.parent < 0) rootList.add(i);
                else kids.get(b.parent).add(i);
            }
            for (int i = 0; i < bones.length; i++) {
                List<Integer> k = kids.get(i);
                bones[i].children = new int[k.size()];
                for (int j = 0; j < k.size(); j++) bones[i].children[j] = k.get(j);
            }
            roots = new int[rootList.size()];
            for (int i = 0; i < roots.length; i++) roots[i] = rootList.get(i);
            loaded = true;
        } catch (Exception e) {
            throw new RuntimeException("No se pudo cargar el modelo del Eternity Walker", e);
        }
    }

    private static int kindOf(String n, boolean leg) {
        if (leg) return LEG;
        return switch (n) {
            case "torreta" -> TURRET_YAW;
            case "canon_principal" -> MAIN_PITCH;
            case "soporte_trasero_izq", "soporte_trasero_der" -> SIDE_YAW;
            case "canon_trasero_izq", "canon_trasero_der" -> SIDE_PITCH;
            case "canon_principal_retroceso" -> RECOIL_MAIN;
            case "canon_trasero_izq_retroceso" -> RECOIL_L;
            case "canon_trasero_der_retroceso" -> RECOIL_R;
            case "sensor_der" -> SENSOR;
            default -> NONE;
        };
    }

    /** Catmull-Rom ciclico sobre los 12 fotogramas de la animacion de caminar. phase en [0,1). */
    public static float sample(float[] kf, float phase) {
        int n = kf.length;
        float f = phase * n;
        int i = (int) Math.floor(f);
        float t = f - i;
        float p0 = kf[Math.floorMod(i - 1, n)], p1 = kf[Math.floorMod(i, n)], p2 = kf[Math.floorMod(i + 1, n)], p3 = kf[Math.floorMod(i + 2, n)];
        return 0.5f * ((2 * p1) + (-p0 + p2) * t + (2 * p0 - 5 * p1 + 4 * p2 - p3) * t * t + (-p0 + 3 * p1 - 3 * p2 + p3) * t * t * t);
    }
}
