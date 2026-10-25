package com.keinloree.story;

import java.util.ArrayList;
import java.util.List;
import net.minecraft.network.FriendlyByteBuf;
import net.minecraft.world.phys.Vec3;

/**
 * Un plano de cinematica, serializable por red. El cliente (CinematicCamera) lo interpreta asi:
 * <ul>
 * <li>STATIC: camara fija en {@code a}.</li>
 * <li>DOLLY: la camara va de {@code a} a {@code b}.</li>
 * <li>ORBIT: gira alrededor del centro ({@code a}, o la posicion de {@code targetEntity} si es &gt;= 0)
 *     a {@code radius} bloques y {@code height} de altura, de {@code angleFrom} a {@code angleTo} grados.</li>
 * <li>TRACK: sigue a {@code targetEntity} con el desplazamiento {@code offset} en ejes locales del objetivo.</li>
 * <li>PATH: recorre {@code path} con una curva Catmull-Rom.</li>
 * </ul>
 * Mira hacia {@code lookEntity} (si es &gt;= 0) o hacia {@code lookPos}; si no hay ninguno, hacia el centro/objetivo.
 * {@code fovFrom/fovTo} &lt;= 0 significa "sin cambio". {@code ease}: 0 lineal, 1 suave.
 */
public final class Shot {
    public enum Mode { STATIC, DOLLY, ORBIT, TRACK, PATH }

    public Mode mode = Mode.STATIC;
    public int ticks = 40;
    public Vec3 a = Vec3.ZERO;
    public Vec3 b = Vec3.ZERO;
    /** Punto al que mira, o null. */
    public Vec3 lookPos;
    public int lookEntity = -1;
    public int targetEntity = -1;
    public float radius;
    public float height;
    public float angleFrom;
    public float angleTo;
    public float fovFrom;
    public float fovTo;
    public float rollFrom;
    public float rollTo;
    public int ease = 1;
    public List<Vec3> path = new ArrayList<>();
    public Vec3 offset = Vec3.ZERO;

    private Shot(Mode mode, int ticks) {
        this.mode = mode;
        this.ticks = ticks;
    }

    // ---- constructores comodos ----

    public static Shot fixed(Vec3 pos, int ticks) {
        Shot s = new Shot(Mode.STATIC, ticks);
        s.a = pos;
        return s;
    }

    public static Shot dolly(Vec3 from, Vec3 to, int ticks) {
        Shot s = new Shot(Mode.DOLLY, ticks);
        s.a = from;
        s.b = to;
        return s;
    }

    /** Orbita alrededor de una entidad (id) o, si id &lt; 0, alrededor de {@code center}. */
    public static Shot orbit(Vec3 center, int entityId, float radius, float height, float angleFrom, float angleTo, int ticks) {
        Shot s = new Shot(Mode.ORBIT, ticks);
        s.a = center;
        s.targetEntity = entityId;
        s.radius = radius;
        s.height = height;
        s.angleFrom = angleFrom;
        s.angleTo = angleTo;
        return s;
    }

    public static Shot track(int entityId, Vec3 localOffset, int ticks) {
        Shot s = new Shot(Mode.TRACK, ticks);
        s.targetEntity = entityId;
        s.offset = localOffset;
        return s;
    }

    public static Shot path(List<Vec3> points, int ticks) {
        Shot s = new Shot(Mode.PATH, ticks);
        s.path = new ArrayList<>(points);
        return s;
    }

    // ---- modificadores encadenables ----

    public Shot lookAt(Vec3 pos) {
        this.lookPos = pos;
        return this;
    }

    public Shot lookAtEntity(int entityId) {
        this.lookEntity = entityId;
        return this;
    }

    public Shot fov(float from, float to) {
        this.fovFrom = from;
        this.fovTo = to;
        return this;
    }

    public Shot roll(float from, float to) {
        this.rollFrom = from;
        this.rollTo = to;
        return this;
    }

    public Shot ease(int ease) {
        this.ease = ease;
        return this;
    }

    // ---- red ----

    public void write(FriendlyByteBuf buf) {
        buf.writeByte(mode.ordinal());
        buf.writeVarInt(ticks);
        buf.writeVec3(a);
        buf.writeVec3(b);
        buf.writeBoolean(lookPos != null);
        if (lookPos != null) {
            buf.writeVec3(lookPos);
        }
        buf.writeVarInt(lookEntity + 1);
        buf.writeVarInt(targetEntity + 1);
        buf.writeFloat(radius);
        buf.writeFloat(height);
        buf.writeFloat(angleFrom);
        buf.writeFloat(angleTo);
        buf.writeFloat(fovFrom);
        buf.writeFloat(fovTo);
        buf.writeFloat(rollFrom);
        buf.writeFloat(rollTo);
        buf.writeByte(ease);
        buf.writeVarInt(path.size());
        for (Vec3 p : path) {
            buf.writeVec3(p);
        }
        buf.writeVec3(offset);
    }

    public static Shot read(FriendlyByteBuf buf) {
        Mode[] modes = Mode.values();
        Shot s = new Shot(modes[Math.floorMod(buf.readByte(), modes.length)], buf.readVarInt());
        s.a = buf.readVec3();
        s.b = buf.readVec3();
        s.lookPos = buf.readBoolean() ? buf.readVec3() : null;
        s.lookEntity = buf.readVarInt() - 1;
        s.targetEntity = buf.readVarInt() - 1;
        s.radius = buf.readFloat();
        s.height = buf.readFloat();
        s.angleFrom = buf.readFloat();
        s.angleTo = buf.readFloat();
        s.fovFrom = buf.readFloat();
        s.fovTo = buf.readFloat();
        s.rollFrom = buf.readFloat();
        s.rollTo = buf.readFloat();
        s.ease = buf.readByte();
        int n = buf.readVarInt();
        s.path = new ArrayList<>(n);
        for (int i = 0; i < n; i++) {
            s.path.add(buf.readVec3());
        }
        s.offset = buf.readVec3();
        return s;
    }
}
