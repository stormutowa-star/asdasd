package com.keinloree.ship;

import com.keinloree.KeinLooreMod;
import com.keinloree.entity.KeinLooreEntity;
import com.keinloree.entity.LooreMob;
import com.keinloree.registry.ModEntities;
import com.keinloree.world.design.ShipDesign;
import java.util.Map;
import java.util.UUID;
import javax.annotation.Nullable;
import net.minecraft.core.BlockPos;
import net.minecraft.network.chat.Component;
import net.minecraft.server.level.ServerLevel;
import net.minecraft.world.entity.Entity;
import net.minecraft.world.entity.EntityType;
import net.minecraft.world.entity.Mob;
import net.minecraft.world.entity.PathfinderMob;
import net.minecraft.world.phys.Vec3;

/**
 * Tripulacion inicial del destructor (con los mobs que ya existen). El sistema de rangos (WP9) puede sustituir
 * {@link #spawner} por el suyo o reutilizar {@link #spawnAt}. Las claves de la tripulacion son "zona.indice"
 * y {@link #pointFor} devuelve su celda local (los puestos se recalculan tras cada anclaje).
 */
public final class ShipCrew {

    /** Genera la tripulacion la primera vez que la nave se materializa. */
    @FunctionalInterface
    public interface CrewSpawner {
        void spawn(ServerLevel level, DestroyerEntity ship);
    }

    public static CrewSpawner spawner = ShipCrew::spawnDefault;

    /** Radio (bloques) que cada tripulante puede alejarse de su puesto. */
    public static final int POST_RADIUS = 4;
    /** Kein vuelve a su puesto si se aleja mas de esto sin combate. */
    public static final double KEIN_LEASH = 6.0D;

    private ShipCrew() {
    }

    public static void spawnDefault(ServerLevel level, DestroyerEntity ship) {
        EntityType<?> droid = ModEntities.DROID.get();
        EntityType<?> commander = ModEntities.LOREE_COMMANDER.get();
        EntityType<?> warlord = ModEntities.LOREE_WARLORD.get();
        // hangar: 6 soldados, 2 pilotos, 1 sargento, 2 tecnicos
        int[][] h = ShipDesign.CREW_HANGAR;
        for (int i = 0; i < h.length; i++) {
            spawnAt(level, ship, "hangar." + i, i == 8 ? commander : droid, null);
        }
        // patrullas: sargento + 2 soldados por ruta
        for (int r = 0; r < ShipDesign.CREW_CORRIDOR.length; r++) {
            for (int i = 0; i < 3; i++) {
                spawnAt(level, ship, "route" + r + "." + i, i == 0 ? commander : droid, null);
            }
        }
        // detencion: 2 guardias + teniente
        for (int i = 0; i < ShipDesign.CREW_DETENTION.length; i++) {
            spawnAt(level, ship, "detention." + i, i == 1 ? commander : droid, null);
        }
        // reactor: 2 tecnicos + 2 soldados
        for (int i = 0; i < ShipDesign.CREW_REACTOR.length; i++) {
            spawnAt(level, ship, "reactor." + i, droid, null);
        }
        // barracones: 4 soldados + teniente
        for (int i = 0; i < ShipDesign.CREW_BARRACKS.length; i++) {
            spawnAt(level, ship, "barracks." + i, i == 4 ? commander : droid, null);
        }
        // puente
        spawnAt(level, ship, "orrel", commander, Component.translatable("name." + KeinLooreMod.MODID + ".orrel"));
        for (int i = 0; i < ShipDesign.BRIDGE_STATIONS.length; i++) {
            spawnAt(level, ship, "station." + i, commander, null);
        }
        for (int i = 0; i < ShipDesign.BRIDGE_GUARDS.length; i++) {
            spawnAt(level, ship, "guard." + i, warlord, Component.translatable("name." + KeinLooreMod.MODID + ".nexo_guard"));
        }
        assignRanks(level, ship);
        Entity kein = spawnAt(level, ship, "kein", ModEntities.KEIN_LOREE.get(), null);
        if (kein instanceof KeinLooreEntity k) {
            k.setLoreDormant(true);
        }
    }

    /** Rangos de la tripulacion segun su puesto. */
    public static void assignRanks(ServerLevel level, DestroyerEntity ship) {
        for (var en : ship.crew().entrySet()) {
            Entity e = level.getEntity(en.getValue());
            if (!(e instanceof com.keinloree.entity.LooreMob m) || m.getRank() != com.keinloree.entity.Rank.NINGUNO) {
                continue;
            }
            String k = en.getKey();
            int dot = k.indexOf('.');
            String zone = dot < 0 ? k : k.substring(0, dot);
            int i = dot < 0 ? 0 : parse(k.substring(dot + 1));
            com.keinloree.entity.Rank r = switch (zone) {
                case "hangar" -> i <= 5 ? com.keinloree.entity.Rank.SOLDADO : i <= 7 ? com.keinloree.entity.Rank.PILOTO
                        : i == 8 ? com.keinloree.entity.Rank.SARGENTO : com.keinloree.entity.Rank.TECNICO;
                case "detention" -> i == 1 ? com.keinloree.entity.Rank.TENIENTE : com.keinloree.entity.Rank.SOLDADO;
                case "reactor" -> i <= 1 ? com.keinloree.entity.Rank.TECNICO : com.keinloree.entity.Rank.SOLDADO;
                case "barracks" -> i == 4 ? com.keinloree.entity.Rank.TENIENTE : com.keinloree.entity.Rank.SOLDADO;
                case "orrel" -> com.keinloree.entity.Rank.ALMIRANTE;
                case "station" -> i % 2 == 0 ? com.keinloree.entity.Rank.COMANDANTE : com.keinloree.entity.Rank.TENIENTE;
                case "guard" -> com.keinloree.entity.Rank.GUARDIA_NEXO;
                case "kein" -> com.keinloree.entity.Rank.NINGUNO;
                default -> zone.startsWith("route") ? (i == 0 ? com.keinloree.entity.Rank.SARGENTO : com.keinloree.entity.Rank.SOLDADO)
                        : com.keinloree.entity.Rank.SOLDADO;
            };
            if (r != com.keinloree.entity.Rank.NINGUNO && !(e instanceof com.keinloree.entity.KeinLooreEntity)) {
                m.setRank(r);
            }
        }
    }

    /** Celda local del puesto de un tripulante, o null si la clave no es conocida. */
    @Nullable
    public static int[] pointFor(String key) {
        int dot = key.indexOf('.');
        String zone = dot < 0 ? key : key.substring(0, dot);
        int i = dot < 0 ? 0 : parse(key.substring(dot + 1));
        int[][] list = switch (zone) {
            case "hangar" -> ShipDesign.CREW_HANGAR;
            case "detention" -> ShipDesign.CREW_DETENTION;
            case "reactor" -> ShipDesign.CREW_REACTOR;
            case "barracks" -> ShipDesign.CREW_BARRACKS;
            case "station" -> ShipDesign.BRIDGE_STATIONS;
            case "guard" -> ShipDesign.BRIDGE_GUARDS;
            case "orrel" -> new int[][]{ShipDesign.ORREL_POST};
            case "kein" -> new int[][]{ShipDesign.KEIN_POST};
            default -> null;
        };
        if (list == null && zone.startsWith("route")) {
            int r = parse(zone.substring(5));
            if (r >= 0 && r < ShipDesign.CREW_CORRIDOR.length) {
                int[][] route = ShipDesign.CREW_CORRIDOR[r];
                // los tres de una patrulla empiezan juntos en el primer punto, separados un bloque
                int[] p = route[0];
                return new int[]{p[0], p[1], p[2] + Math.max(0, i)};
            }
        }
        return list != null && i >= 0 && i < list.length ? list[i] : null;
    }

    private static int parse(String s) {
        try {
            return Integer.parseInt(s);
        } catch (NumberFormatException e) {
            return -1;
        }
    }

    /** Posicion de mundo del puesto (centro inferior de la celda). */
    @Nullable
    public static Vec3 postWorld(DestroyerEntity ship, String key) {
        int[] p = pointFor(key);
        return p == null ? null : ShipTransform.toWorld(ship.position(), ship.getYRot(), ShipTransform.cellFloor(p));
    }

    /** Genera un tripulante en su puesto y lo apunta en la nave (no duplica si sigue vivo). */
    @Nullable
    public static Entity spawnAt(ServerLevel level, DestroyerEntity ship, String key, EntityType<?> type, @Nullable Component name) {
        UUID old = ship.crew().get(key);
        if (old != null) {
            Entity e = level.getEntity(old);
            if (e != null && e.isAlive()) {
                return e;
            }
        }
        Vec3 at = postWorld(ship, key);
        if (at == null) {
            return null;
        }
        Entity e = type.create(level);
        if (e == null) {
            return null;
        }
        float yaw = ship.getYRot();
        e.moveTo(at.x, at.y, at.z, yaw, 0.0F);
        e.setYHeadRot(yaw);
        if (name != null) {
            e.setCustomName(name);
        }
        if (e instanceof Mob m) {
            m.setPersistenceRequired();
            m.setYBodyRot(yaw);
        }
        if (e instanceof LooreMob lm) {
            lm.setSpawnTicks(0);
        }
        if (e instanceof PathfinderMob pm) {
            pm.restrictTo(BlockPos.containing(at), POST_RADIUS);
        }
        level.addFreshEntity(e);
        ship.crew().put(key, e.getUUID());
        return e;
    }

    /** Re-genera a quien falte (si fue eliminado). Devuelve cuantos se generaron. */
    public static int respawnMissing(ServerLevel level, DestroyerEntity ship) {
        int before = ship.crew().size();
        int alive = 0;
        for (Map.Entry<String, UUID> en : ship.crew().entrySet()) {
            Entity e = level.getEntity(en.getValue());
            if (e != null && e.isAlive()) {
                alive++;
            }
        }
        if (alive == before) {
            return 0;
        }
        spawner.spawn(level, ship);
        return before - alive;
    }

    /** Mantiene los puestos tras moverse la nave y devuelve a Kein a su sitio si se aleja sin combate. */
    public static void keepPosts(ServerLevel level, DestroyerEntity ship) {
        for (Map.Entry<String, UUID> en : ship.crew().entrySet()) {
            Entity e = level.getEntity(en.getValue());
            if (e == null || !e.isAlive() || e.isPassenger()) {
                continue;
            }
            Vec3 post = postWorld(ship, en.getKey());
            if (post == null) {
                continue;
            }
            BlockPos pp = BlockPos.containing(post);
            if (e instanceof PathfinderMob pm && !pm.getRestrictCenter().equals(pp)) {
                pm.restrictTo(pp, POST_RADIUS);
            }
            if (e instanceof KeinLooreEntity k && k.getTarget() == null && k.position().distanceTo(post) > KEIN_LEASH) {
                k.teleportTo(post.x, post.y, post.z);
                k.setYRot(ship.getYRot());
                k.setYHeadRot(ship.getYRot());
            }
        }
    }

    /** Tripulantes vivos (cargados). */
    public static int alive(ServerLevel level, DestroyerEntity ship) {
        int n = 0;
        for (UUID id : ship.crew().values()) {
            Entity e = level.getEntity(id);
            if (e != null && e.isAlive()) {
                n++;
            }
        }
        return n;
    }
}
