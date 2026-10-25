package com.keinloree.story;

import com.keinloree.KeinLooreMod;
import com.keinloree.entity.CargoShip;
import com.keinloree.entity.LooreMob;
import net.minecraft.core.registries.Registries;
import net.minecraft.resources.ResourceLocation;
import net.minecraft.server.level.ServerPlayer;
import net.minecraft.tags.TagKey;
import net.minecraft.world.entity.Entity;
import net.minecraft.world.entity.EntityType;
import net.minecraft.world.entity.player.Player;

/**
 * Facciones: la Legion (Kein y los suyos) y las Horas (Calendarios). Los tipos nuevos se anaden con las
 * etiquetas de entidad keinloree:legion y keinloree:hours.
 */
public final class Factions {
    public static final TagKey<EntityType<?>> LEGION_TAG = TagKey.create(Registries.ENTITY_TYPE,
            ResourceLocation.fromNamespaceAndPath(KeinLooreMod.MODID, "legion"));
    public static final TagKey<EntityType<?>> HOURS_TAG = TagKey.create(Registries.ENTITY_TYPE,
            ResourceLocation.fromNamespaceAndPath(KeinLooreMod.MODID, "hours"));

    private Factions() {}

    public static boolean isLegion(Entity e) {
        return e instanceof LooreMob || e instanceof CargoShip || e.getType().is(LEGION_TAG);
    }

    public static boolean isHours(Entity e) {
        return e.getType().is(HOURS_TAG);
    }

    private static Campaign.PlayerTale tale(Player p) {
        return Campaign.get(p.getServer()).tale(p.getUUID());
    }

    /** El jugador acepto la ruta del Nexo. */
    public static boolean legionAlly(Player p) {
        return p instanceof ServerPlayer && tale(p).legionAlly;
    }

    /** El jugador recibio el equipo de las Horas y no las traiciono. */
    public static boolean hoursAlly(Player p) {
        return p instanceof ServerPlayer && tale(p).receivedGear && !Campaign.get(p.getServer()).hasFlag("traicion_horas");
    }

    /** Puede ser elegido como objetivo: ni creativo, ni espectador, ni en cinematica. */
    public static boolean canTarget(Entity e) {
        if (e instanceof Player p) {
            return p.isAlive() && !p.isCreative() && !p.isSpectator() && !Cinematics.isActive(p);
        }
        return true;
    }

    public static boolean hostile(Entity a, Entity b) {
        if (isLegion(a) && isHours(b) || isHours(a) && isLegion(b)) {
            return true;
        }
        if (b instanceof Player p && canTarget(p)) {
            if (isLegion(a)) {
                return !legionAlly(p);
            }
            if (isHours(a)) {
                return Campaign.get(p.getServer()).hasFlag("traicion_horas");
            }
        }
        if (a instanceof Player p && canTarget(p)) {
            return hostile(b, a);
        }
        return false;
    }
}
