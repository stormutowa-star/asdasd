package com.keinloree.story;

import com.mojang.logging.LogUtils;
import java.util.List;
import java.util.Objects;
import javax.annotation.Nullable;
import net.minecraft.core.BlockPos;
import net.minecraft.server.level.ServerLevel;
import net.minecraft.server.level.ServerPlayer;
import org.slf4j.Logger;

/** Director de la campana: ejecuta el paso actual, gestiona al heroe y envia el HUD de objetivo. */
public final class Director {
    private static final Logger LOGGER = LogUtils.getLogger();
    /** 5 minutos sin el heroe antes de pasar el papel a otro jugador. */
    private static final int HERO_TIMEOUT = 6000;

    private record HudState(String objective, @Nullable BlockPos marker, int cur, int max) {}

    private static int heroAbsent;
    @Nullable
    private static HudState lastHud;
    private static boolean warnedUnknown;

    private Director() {}

    public static void reset() {
        heroAbsent = 0;
        lastHud = null;
        warnedUnknown = false;
    }

    /** Fuerza el reenvio del HUD (por ejemplo al entrar el heroe). */
    public static void invalidateHud() {
        lastHud = null;
    }

    public static void tick(ServerLevel level) {
        Campaign c = Campaign.get(level);
        List<ServerPlayer> players = level.players();
        if (c.disabled || players.isEmpty()) {
            return;
        }
        c.worldTicks++;
        if (c.worldTicks % 100 == 0) {
            c.setDirty();
        }
        StoryCtx ctx = new StoryCtx(level);
        updateHero(c, ctx, players);

        Step step = Steps.get(c.step);
        if (step == null) {
            if (!c.step.isEmpty() && !warnedUnknown) {
                warnedUnknown = true;
                LOGGER.warn("[Director] Paso desconocido '{}'; se vuelve a {}", c.step, Steps.START);
            }
            goTo(ctx, Steps.START);
            return;
        }
        ServerPlayer hero = ctx.hero();
        if (hero == null) {
            return;
        }
        step.tick(ctx);
        // El paso pudo cambiar durante su tick
        Step now = Steps.get(c.step);
        if (now != null && c.worldTicks % 20 == 0) {
            sendHud(ctx, now, hero);
        }
    }

    private static void updateHero(Campaign c, StoryCtx ctx, List<ServerPlayer> players) {
        if (c.hero == null) {
            setHero(c, players.get(0));
            return;
        }
        if (ctx.hero() == null) {
            if (++heroAbsent > HERO_TIMEOUT) {
                setHero(c, players.get(0));
            }
        } else {
            heroAbsent = 0;
        }
    }

    private static void setHero(Campaign c, ServerPlayer p) {
        c.hero = p.getUUID();
        c.setDirty();
        heroAbsent = 0;
        lastHud = null;
        LOGGER.info("[Director] Heroe: {}", p.getGameProfile().getName());
    }

    private static void sendHud(StoryCtx ctx, Step step, ServerPlayer hero) {
        String obj = step.objective(ctx);
        HudState s = new HudState(obj == null ? "" : obj, step.marker(ctx), step.progress(ctx), step.progressMax(ctx));
        if (Objects.equals(s, lastHud)) {
            return;
        }
        lastHud = s;
        Hud.send(hero, s.objective(), s.marker(), "", s.cur(), s.max(), 0x4CFF7A);
    }

    /** Cambia de paso: exit del anterior, enter del nuevo. */
    public static void goTo(StoryCtx ctx, String id) {
        Step next = Steps.get(id);
        if (next == null) {
            LOGGER.warn("[Director] No existe el paso '{}'", id);
            return;
        }
        Campaign c = ctx.campaign;
        Step old = Steps.get(c.step);
        if (old != null) {
            old.exit(ctx);
        }
        c.step = id;
        c.stepStart = c.worldTicks;
        c.setDirty();
        lastHud = null;
        LOGGER.info("[Director] Paso '{}'", id);
        next.enter(ctx);
    }
}
