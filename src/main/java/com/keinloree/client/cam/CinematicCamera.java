package com.keinloree.client.cam;

import com.keinloree.entity.CameraEntity;
import com.keinloree.registry.ModEntities;
import com.keinloree.story.Shot;
import com.mojang.logging.LogUtils;
import java.util.ArrayList;
import java.util.List;
import javax.annotation.Nullable;
import net.minecraft.client.CameraType;
import net.minecraft.client.Minecraft;
import net.minecraft.client.multiplayer.ClientLevel;
import net.minecraft.util.Mth;
import net.minecraft.world.entity.Entity;
import net.minecraft.world.phys.Vec3;
import org.slf4j.Logger;

/**
 * Camara cinematica del cliente. Pone una {@link CameraEntity} propia como entidad de camara y la mueve
 * cada tick segun el plano actual (xo/yo/zo y yRotO/xRotO guardados para que la interpolacion sea suave).
 * La camara del jugador se restaura SIEMPRE en {@link #end()} (fin, muerte, cambio de dimension,
 * desconexion o entidad objetivo desaparecida).
 */
public final class CinematicCamera {
    private static final Logger LOGGER = LogUtils.getLogger();
    /** Id fijo y negativo: nunca choca con una entidad real. */
    private static final int CAMERA_ID = -7_771_000;
    /** Ticks de espera tras el ultimo plano antes de cortar por seguridad si el servidor no avisa. */
    private static final int HOLD_LIMIT = 80;
    /** Ticks que se tolera que falte la entidad objetivo antes de abortar. */
    private static final int MISSING_LIMIT = 10;

    private static boolean active;
    private static boolean skippable;
    private static List<Shot> shots = List.of();
    private static int index;
    private static int tickInShot;
    private static int holdTicks;
    private static int missingTicks;
    private static CameraEntity cam;
    private static ClientLevel level;
    private static Entity previousCamera;
    private static boolean savedHideGui;
    private static CameraType savedType;
    private static Vec3 lastPos = Vec3.ZERO;
    private static float lastYaw;
    private static float lastPitch;
    private static float easeNow;
    private static float easePrev;
    @Nullable
    private static Shot currentShot;
    private static float bars;
    private static float barsPrev;

    private CinematicCamera() {}

    public static boolean isActive() {
        return active;
    }

    public static boolean isSkippable() {
        return active && skippable;
    }

    /** Empieza una cinematica (o sustituye los planos de la que ya esta en marcha). */
    public static void start(List<Shot> newShots, boolean canSkip) {
        Minecraft mc = Minecraft.getInstance();
        if (mc.level == null || mc.player == null || newShots.isEmpty()) {
            return;
        }
        shots = new ArrayList<>(newShots);
        skippable = canSkip;
        index = 0;
        tickInShot = 0;
        holdTicks = 0;
        missingTicks = 0;
        if (!active) {
            active = true;
            level = mc.level;
            previousCamera = mc.getCameraEntity();
            savedHideGui = mc.options.hideGui;
            savedType = mc.options.getCameraType();
            cam = new CameraEntity(ModEntities.CAMERA.get(), mc.level);
            cam.setId(CAMERA_ID);
            lastPos = mc.player.getEyePosition();
            lastYaw = mc.player.getYRot();
            lastPitch = mc.player.getXRot();
        }
        currentShot = shots.get(0);
        easeNow = 0.0F;
        easePrev = 0.0F;
        // Posicion inicial sin interpolar desde el origen
        Pose pose = compute(currentShot, 0.0F);
        place(pose != null ? pose : new Pose(lastPos, lastYaw, lastPitch), true);
        mc.options.hideGui = true;
        mc.options.setCameraType(CameraType.FIRST_PERSON);
        mc.setCameraEntity(cam);
    }

    /** Devuelve SIEMPRE la camara al jugador y deshace los cambios de interfaz. */
    public static void end() {
        if (!active) {
            return;
        }
        active = false;
        Minecraft mc = Minecraft.getInstance();
        try {
            mc.options.hideGui = savedHideGui;
            if (savedType != null) {
                mc.options.setCameraType(savedType);
            }
            if (mc.player != null && mc.level != null) {
                mc.setCameraEntity(mc.player);
            } else if (previousCamera != null && mc.level != null && !previousCamera.isRemoved()) {
                mc.setCameraEntity(previousCamera);
            }
        } catch (RuntimeException e) {
            LOGGER.error("[Cine] Fallo al restaurar la camara", e);
        } finally {
            cam = null;
            level = null;
            previousCamera = null;
            currentShot = null;
            shots = List.of();
        }
    }

    // ---- bucle ----

    public static void tick(Minecraft mc) {
        barsPrev = bars;
        if (!active) {
            bars = Math.max(0.0F, bars - 0.08F);
            return;
        }
        bars = Math.min(1.0F, bars + 0.08F);
        if (mc.level == null || mc.player == null || mc.level != level || !mc.player.isAlive()) {
            end();
            return;
        }
        if (mc.getCameraEntity() != cam) {
            mc.setCameraEntity(cam);
        }
        mc.options.hideGui = true;
        mc.options.setCameraType(CameraType.FIRST_PERSON);

        if (index >= shots.size()) {
            // Ultimo plano agotado: se mantiene el ultimo fotograma esperando el aviso del servidor
            if (++holdTicks > HOLD_LIMIT) {
                end();
            }
            return;
        }
        Shot shot = shots.get(index);
        boolean cut = shot != currentShot;
        currentShot = shot;
        easePrev = cut ? 0.0F : easeNow;
        float u = shot.ticks <= 0 ? 1.0F : Math.min(1.0F, tickInShot / (float) shot.ticks);
        Pose pose = compute(shot, u);
        if (pose == null) {
            // Entidad objetivo desaparecida
            if (++missingTicks > MISSING_LIMIT) {
                LOGGER.warn("[Cine] La entidad objetivo del plano {} desaparecio: se cancela la cinematica", index);
                end();
            }
            return;
        }
        missingTicks = 0;
        easeNow = shot.ease == 1 ? u * u * (3.0F - 2.0F * u) : u;
        place(pose, cut && index > 0);
        tickInShot++;
        if (tickInShot >= shot.ticks) {
            index++;
            tickInShot = 0;
            holdTicks = 0;
        }
    }

    /** Coloca la entidad camara; {@code snap} evita interpolar desde la pose anterior (cortes). */
    private static void place(@Nullable Pose pose, boolean snap) {
        if (pose == null || cam == null) {
            return;
        }
        if (snap) {
            cam.xo = pose.pos.x;
            cam.yo = pose.pos.y - CameraEntity.EYE_HEIGHT;
            cam.zo = pose.pos.z;
            cam.yRotO = pose.yaw;
            cam.xRotO = pose.pitch;
        } else {
            cam.xo = cam.getX();
            cam.yo = cam.getY();
            cam.zo = cam.getZ();
            cam.yRotO = cam.getYRot();
            cam.xRotO = cam.getXRot();
        }
        cam.xOld = cam.xo;
        cam.yOld = cam.yo;
        cam.zOld = cam.zo;
        cam.setPos(pose.pos.x, pose.pos.y - CameraEntity.EYE_HEIGHT, pose.pos.z);
        cam.setYRot(pose.yaw);
        cam.setXRot(pose.pitch);
        lastPos = pose.pos;
        lastYaw = pose.yaw;
        lastPitch = pose.pitch;
    }

    // ---- calculo de planos ----

    private record Pose(Vec3 pos, float yaw, float pitch) {}

    private static float ease(Shot shot, float u) {
        return shot.ease == 1 ? u * u * (3.0F - 2.0F * u) : u;
    }

    @Nullable
    private static Entity entity(int id) {
        Minecraft mc = Minecraft.getInstance();
        return id < 0 || mc.level == null ? null : mc.level.getEntity(id);
    }

    private static Vec3 center(Entity e) {
        return e.position().add(0.0D, e.getBbHeight() * 0.5D, 0.0D);
    }

    /** Pose del plano en el instante u (0..1) o null si falta una entidad que el plano necesita. */
    @Nullable
    private static Pose compute(Shot s, float u) {
        float e = ease(s, u);
        Entity target = entity(s.targetEntity);
        if (s.targetEntity >= 0 && target == null) {
            return null;
        }
        Vec3 pos;
        Vec3 tangent = null;
        Vec3 defaultLook = null;
        switch (s.mode) {
            case DOLLY -> {
                pos = s.a.lerp(s.b, e);
                tangent = s.b.subtract(s.a);
            }
            case ORBIT -> {
                Vec3 c = target != null ? center(target) : s.a;
                double ang = Math.toRadians(Mth.lerp(e, s.angleFrom, s.angleTo));
                pos = c.add(Math.cos(ang) * s.radius, s.height, Math.sin(ang) * s.radius);
                defaultLook = c;
            }
            case TRACK -> {
                if (target == null) {
                    return null;
                }
                double yaw = Math.toRadians(target.getYRot());
                Vec3 fwd = new Vec3(-Math.sin(yaw), 0.0D, Math.cos(yaw));
                Vec3 right = new Vec3(-Math.cos(yaw), 0.0D, -Math.sin(yaw));
                pos = target.position()
                        .add(right.scale(s.offset.x))
                        .add(0.0D, s.offset.y, 0.0D)
                        .add(fwd.scale(s.offset.z));
                defaultLook = center(target);
            }
            case PATH -> {
                pos = catmullRom(s.path, e, s.a);
                tangent = catmullRom(s.path, Math.min(1.0F, e + 0.02F), s.a).subtract(pos);
            }
            default -> pos = s.a;
        }
        Vec3 look = null;
        Entity lookEnt = entity(s.lookEntity);
        if (s.lookEntity >= 0) {
            if (lookEnt == null) {
                return null;
            }
            look = center(lookEnt);
        } else if (s.lookPos != null) {
            look = s.lookPos;
        } else if (defaultLook != null) {
            look = defaultLook;
        } else if (tangent != null && tangent.lengthSqr() > 1.0E-6D) {
            look = pos.add(tangent);
        }
        float yaw = lastYaw;
        float pitch = lastPitch;
        if (look != null) {
            Vec3 d = look.subtract(pos);
            double h = Math.sqrt(d.x * d.x + d.z * d.z);
            if (d.lengthSqr() > 1.0E-6D) {
                yaw = (float) (Mth.atan2(-d.x, d.z) * (180.0D / Math.PI));
                pitch = (float) (-(Mth.atan2(d.y, h) * (180.0D / Math.PI)));
            }
        }
        // Mantiene el yaw continuo (sin saltos de 360 grados) para interpolar bien
        yaw = lastYaw + Mth.wrapDegrees(yaw - lastYaw);
        return new Pose(pos, yaw, Mth.clamp(pitch, -89.9F, 89.9F));
    }

    /** Curva Catmull-Rom uniforme por los puntos; con menos de 2 devuelve {@code fallback}. */
    private static Vec3 catmullRom(List<Vec3> pts, float t, Vec3 fallback) {
        int n = pts.size();
        if (n == 0) {
            return fallback;
        }
        if (n == 1) {
            return pts.get(0);
        }
        float f = Mth.clamp(t, 0.0F, 1.0F) * (n - 1);
        int i = Math.min((int) f, n - 2);
        float k = f - i;
        Vec3 p0 = pts.get(Math.max(i - 1, 0));
        Vec3 p1 = pts.get(i);
        Vec3 p2 = pts.get(i + 1);
        Vec3 p3 = pts.get(Math.min(i + 2, n - 1));
        return new Vec3(cr(p0.x, p1.x, p2.x, p3.x, k), cr(p0.y, p1.y, p2.y, p3.y, k), cr(p0.z, p1.z, p2.z, p3.z, k));
    }

    private static double cr(double a, double b, double c, double d, double t) {
        double t2 = t * t;
        double t3 = t2 * t;
        return 0.5D * ((2.0D * b) + (-a + c) * t + (2.0D * a - 5.0D * b + 4.0D * c - d) * t2 + (-a + 3.0D * b - 3.0D * c + d) * t3);
    }

    // ---- ganchos de render ----

    private static float easeAt(float partial) {
        return Mth.lerp(partial, easePrev, easeNow);
    }

    /** FOV del plano actual, o {@code base} si no cambia. */
    public static double modifyFov(double base, float partial) {
        Shot s = currentShot;
        if (!active || s == null || (s.fovFrom <= 0.0F && s.fovTo <= 0.0F)) {
            return base;
        }
        double from = s.fovFrom > 0.0F ? s.fovFrom : base;
        double to = s.fovTo > 0.0F ? s.fovTo : base;
        return Mth.lerp(easeAt(partial), from, to);
    }

    /** Inclinacion (roll) en grados del plano actual. */
    public static float roll(float partial) {
        Shot s = currentShot;
        if (!active || s == null) {
            return 0.0F;
        }
        return Mth.lerp(easeAt(partial), s.rollFrom, s.rollTo);
    }

    /** Altura de cada barra de cine como fraccion de la pantalla (0 a 0.12). */
    public static float barFraction(float partial) {
        float b = Mth.lerp(partial, barsPrev, bars);
        return 0.12F * (b * b * (3.0F - 2.0F * b));
    }
}
