package com.ssj8.client;

import java.util.HashMap;
import java.util.Map;
import net.minecraft.client.Minecraft;
import net.minecraft.world.entity.Entity;

public final class ClientState {
    /** Duracion del crecimiento del pelo: coincide con la fase de carga antes del destello (60 ticks en el servidor). */
    public static final float GROW_TICKS = 60.0f;
    /** Ticks que tarda en entrar y salir la pose de carga de ki (igual que el blend de DMZ). */
    public static final float CHARGE_BLEND_TICKS = 5.0f;
    private static final Map<Integer, ClientState> STATES = new HashMap<>();
    public static float localKi;
    public boolean form;
    public int mode;
    public float modeStart;
    public float growStart = -100000.0f;
    public float chargeStart = -100000.0f;
    public float chargeEnd = -100000.0f;
    public float windR;
    public float windU;
    public float windB;
    public double lastX;
    public double lastY;
    public double lastZ;
    public boolean hasLast;

    public static ClientState get(Entity entity) {
        return STATES.get(entity.getId());
    }

    public static ClientState getOrCreate(int id) {
        return STATES.computeIfAbsent(id, k -> new ClientState());
    }

    public static void remove(int id) {
        STATES.remove(id);
    }

    public static void clear() {
        STATES.clear();
        localKi = 0.0f;
    }

    public static float now(float partial) {
        Minecraft mc = Minecraft.getInstance();
        return (float) (mc.level != null ? mc.level.getGameTime() : 0L) + partial;
    }

    public float modeAge(float partial) {
        return now(partial) - this.modeStart;
    }

    /** 0 al inicio de la transformacion y 1 al final de la fase de carga (curva suave). */
    public float growth(float partial) {
        float t = Math.max(0.0f, Math.min(1.0f, (now(partial) - this.growStart) / GROW_TICKS));
        return t * t * (3.0f - 2.0f * t);
    }

    /** Peso (0..1) de la pose de carga de ki: sube al empezar a cargar y baja al soltar Z. */
    public float chargeBlend(float partial) {
        float now = now(partial);
        float t = this.mode == 1
                ? (now - this.chargeStart) / CHARGE_BLEND_TICKS
                : 1.0f - (now - this.chargeEnd) / CHARGE_BLEND_TICKS;
        t = Math.max(0.0f, Math.min(1.0f, t));
        return t * t * (3.0f - 2.0f * t);
    }
}
