package com.keinloree.story;

import com.keinloree.entity.CalendarioEntity;
import com.keinloree.entity.KeinLooreEntity;
import com.keinloree.entity.LooreMob;
import com.keinloree.entity.RazakoEntity;
import com.keinloree.registry.ModItems;
import com.keinloree.ship.DestroyerEntity;
import com.keinloree.ship.ShipCrew;
import com.keinloree.ship.ShipTransform;
import com.keinloree.world.Pillars;
import com.keinloree.world.design.ShipDesign;
import java.util.UUID;
import javax.annotation.Nullable;
import net.minecraft.core.BlockPos;
import net.minecraft.server.level.ServerLevel;
import net.minecraft.server.level.ServerPlayer;
import net.minecraft.world.entity.Entity;
import net.minecraft.world.entity.player.Player;
import net.minecraft.world.item.ItemStack;
import net.minecraft.world.phys.AABB;
import net.minecraft.world.phys.Vec3;

/** Ayudantes de la historia sobre el mundo: la nave, sus zonas, los personajes clave y el equipo. */
public final class SagaWorld {
    public static final String ALLY_TAG = "keinloree_aliado";

    private SagaWorld() {
    }

    @Nullable
    public static DestroyerEntity ship(ServerLevel level) {
        return DestroyerEntity.find(level);
    }

    @Nullable
    public static DestroyerEntity anchoredShip(ServerLevel level) {
        DestroyerEntity d = ship(level);
        return d != null && d.getState() == DestroyerEntity.ANCHORED ? d : null;
    }

    public static Vec3 local(DestroyerEntity d, int x, int y, int z) {
        return ShipTransform.toWorld(d.position(), d.getYRot(), ShipTransform.cellFloor(new int[]{x, y, z}));
    }

    @Nullable
    public static AABB shipBox(ServerLevel level) {
        DestroyerEntity d = anchoredShip(level);
        return d == null ? null : ShipTransform.worldAABB(d.getAnchor(), d.getRot());
    }

    public static boolean inShip(ServerLevel level, Entity e) {
        AABB b = shipBox(level);
        return b != null && b.contains(e.position());
    }

    @Nullable
    public static AABB hangarBox(ServerLevel level) {
        DestroyerEntity d = anchoredShip(level);
        if (d == null) {
            return null;
        }
        Vec3 a = local(d, -15, ShipDesign.DOCK_Y - 3, ShipDesign.HANGAR_Z0);
        Vec3 b = local(d, 15, 12, ShipDesign.HANGAR_Z1);
        return new AABB(a, b);
    }

    public static boolean inHangar(ServerLevel level, Entity e) {
        AABB b = hangarBox(level);
        return b != null && b.contains(e.position());
    }

    /** Suelo del hangar (punto seguro para aparecer dentro). */
    @Nullable
    public static Vec3 hangarFloor(ServerLevel level) {
        DestroyerEntity d = anchoredShip(level);
        return d == null ? null : local(d, 0, -2, 118);
    }

    /** Boca del hangar (por debajo, donde salen las naves). */
    @Nullable
    public static BlockPos hangarMouth(ServerLevel level) {
        DestroyerEntity d = ship(level);
        return d == null ? null : BlockPos.containing(local(d, 0, ShipDesign.DOCK_Y - 6, ShipDesign.DOCK_Z[1]));
    }

    @Nullable
    public static Vec3 cell(ServerLevel level) {
        DestroyerEntity d = anchoredShip(level);
        return d == null ? null : ShipTransform.toWorld(d.position(), d.getYRot(), ShipTransform.cellFloor(ShipDesign.CELLS[0]));
    }

    @Nullable
    public static Entity crew(ServerLevel level, String key) {
        DestroyerEntity d = ship(level);
        if (d == null) {
            return null;
        }
        UUID id = d.crew().get(key);
        Entity e = id == null ? null : level.getEntity(id);
        return e != null && e.isAlive() ? e : null;
    }

    @Nullable
    public static KeinLooreEntity kein(ServerLevel level) {
        return crew(level, "kein") instanceof KeinLooreEntity k ? k : null;
    }

    @Nullable
    public static Vec3 bridge(ServerLevel level) {
        DestroyerEntity d = ship(level);
        return d == null ? null : ShipCrew.postWorld(d, "kein");
    }

    @Nullable
    public static RazakoEntity razako(ServerLevel level) {
        UUID id = Campaign.get(level).entities.get("razako");
        Entity e = id == null ? null : level.getEntity(id);
        return e instanceof RazakoEntity r && r.isAlive() ? r : null;
    }

    @Nullable
    public static CalendarioEntity hour(ServerLevel level, int h) {
        return Pillars.calendario(level, h);
    }

    public static boolean isAlly(Entity e) {
        return e.getPersistentData().getBoolean(ALLY_TAG);
    }

    public static void makeAlly(Entity e) {
        e.getPersistentData().putBoolean(ALLY_TAG, true);
        if (e instanceof LooreMob m) {
            m.setTarget(null);
        }
    }

    public static boolean hasGear(Player p) {
        for (ItemStack s : p.getInventory().items) {
            if (isGear(s)) {
                return true;
            }
        }
        for (ItemStack s : p.getInventory().armor) {
            if (isGear(s)) {
                return true;
            }
        }
        return isGear(p.getOffhandItem());
    }

    private static boolean isGear(ItemStack s) {
        return s.is(ModItems.RAREIRI.get()) || s.is(ModItems.SEXTA_HORA_HELMET.get()) || s.is(ModItems.SEXTA_HORA_CHESTPLATE.get())
                || s.is(ModItems.SEXTA_HORA_LEGGINGS.get()) || s.is(ModItems.SEXTA_HORA_BOOTS.get());
    }

    /** Teletransporte con fundido a negro. */
    public static void blink(ServerPlayer p, Vec3 to, float yaw) {
        ScreenFx.send(p, ScreenFx.FADE_OUT, 20, 1.0F, 0x000000);
        Later.run(22, () -> {
            if (p.isAlive()) {
                p.teleportTo(p.serverLevel(), to.x, to.y, to.z, yaw, 0.0F);
                p.fallDistance = 0.0F;
                ScreenFx.send(p, ScreenFx.FADE_IN, 30, 1.0F, 0x000000);
            }
        });
    }

    public static boolean openSky(ServerPlayer p) {
        return p.level().canSeeSky(p.blockPosition().above());
    }
}
