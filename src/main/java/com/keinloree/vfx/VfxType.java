package com.keinloree.vfx;

import java.util.Locale;

/** Tipos del motor de VFX. Los ids viajan por red: NO reordenar ni reutilizar, solo anadir al final. */
public enum VfxType {
    BEAM(0),
    RING(1),
    SHOCKWAVE(2),
    SLASH(3),
    ORB(4),
    PILLAR(5),
    RUNE_CIRCLE(6),
    LIGHTNING(7),
    CODE_RAIN(8),
    BURST(9),
    DISTORTION(10),
    TIME_FREEZE(11),
    HYPERSPACE(12),
    TRAIL(13),
    EXPLOSION(14),
    MUZZLE(15);

    public final int id;

    VfxType(int id) {
        this.id = id;
    }

    /** Devuelve el tipo con ese id o null si no existe. */
    public static VfxType byId(int id) {
        for (VfxType t : values()) {
            if (t.id == id) {
                return t;
            }
        }
        return null;
    }

    /** Nombre para el comando (minusculas). */
    public String cmd() {
        return name().toLowerCase(Locale.ROOT);
    }
}
