package com.eternitywalker.client;

import java.util.List;

import com.eternitywalker.EternityWalkerMod;
import com.eternitywalker.InteriorGrid;
import com.eternitywalker.Net;
import com.eternitywalker.WalkerEntity;

import net.minecraft.client.Minecraft;
import net.minecraft.client.player.Input;
import net.minecraft.client.player.LocalPlayer;
import net.minecraft.network.chat.Component;
import net.minecraft.world.entity.Entity;
import net.minecraft.world.entity.player.Player;
import net.minecraft.world.phys.Vec3;
import net.minecraftforge.api.distmarker.Dist;
import net.minecraftforge.client.event.InputEvent;
import net.minecraftforge.event.TickEvent;
import net.minecraftforge.eventbus.api.SubscribeEvent;
import net.minecraftforge.fml.common.Mod;

/**
 * Fisica del interior (lado cliente): mientras estas dentro del caminante te mueves en el marco local del
 * casco contra la rejilla de colision, asi el interior se comporta como una casa que camina contigo dentro.
 * La escalera bajo la escotilla se trepa con W / espacio y al llegar arriba pasas al interior.
 */
@Mod.EventBusSubscriber(modid = EternityWalkerMod.MODID, value = Dist.CLIENT)
public final class ClientInterior {
    private static final double SHAFT_TOP = 9.55;      // y local (bloques) donde empieza el hueco de la escotilla
    private static final double LADDER_TOP = 12.4;

    private static int boardedId = -1;
    private static double lx, ly, lz, vy;
    private static boolean onGround;
    private static boolean wasPassenger;
    private static boolean needInit;
    private static boolean lastMain, lastSide;

    private ClientInterior() {}

    private static boolean inLadderZone(Vec3 l) {
        return l.x >= -1.5 && l.x <= -0.3 && Math.abs(l.z) <= 0.9 && l.y >= -0.6 && l.y <= 12.6;
    }

    @SubscribeEvent
    public static void onInteractionKey(InputEvent.InteractionKeyMappingTriggered e) {
        Player p = Minecraft.getInstance().player;
        if (p != null && p.getVehicle() instanceof WalkerEntity) {
            e.setCanceled(true);
            e.setSwingHand(false);
        }
    }

    @SubscribeEvent
    public static void onPlayerTick(TickEvent.PlayerTickEvent e) {
        if (e.phase != TickEvent.Phase.END) return;
        Minecraft mc = Minecraft.getInstance();
        Player p = e.player;
        if (mc.level == null || p != mc.player || !(p instanceof LocalPlayer lp)) return;
        if (p.isSpectator()) return;

        // ---- pilotando
        if (p.isPassenger()) {
            Entity v = p.getVehicle();
            if (v instanceof WalkerEntity w) {
                boardedId = w.getId();
                wasPassenger = true;
                if (w.getControllingPassenger() == p) {
                    boolean a = mc.screen == null && mc.options.keyAttack.isDown();
                    boolean u = mc.screen == null && mc.options.keyUse.isDown();
                    if (a || u || lastMain || lastSide) Net.sendControls(a, u);
                    lastMain = a;
                    lastSide = u;
                }
            }
            return;
        }
        lastMain = false;
        lastSide = false;

        WalkerEntity walker = null;
        if (boardedId != -1) {
            Entity en = mc.level.getEntity(boardedId);
            if (en instanceof WalkerEntity ww && !ww.isRemoved()) walker = ww;
            else boardedId = -1;
        }

        if (wasPassenger) {
            wasPassenger = false;
            if (walker != null) needInit = true;
        }

        if (walker != null) {
            boardedTick(lp, walker);
            return;
        }

        // ---- fuera: escalera de la escotilla
        List<WalkerEntity> near = mc.level.getEntitiesOfClass(WalkerEntity.class, p.getBoundingBox().inflate(60));
        for (WalkerEntity w : near) {
            Vec3 l = w.toLocal(p.position());
            if (inLadderZone(l)) {
                ladderOutside(lp, w, l);
                return;
            }
        }
    }

    private static void ladderOutside(LocalPlayer p, WalkerEntity w, Vec3 l) {
        Input in = p.input;
        boolean up = in.forwardImpulse > 0.1f || in.jumping;
        boolean hold = in.shiftKeyDown;
        double ny;
        if (up) ny = p.yo + 0.17;
        else if (hold) ny = Math.max(p.getY(), p.yo);
        else ny = Math.max(p.getY(), p.yo - 0.12);

        double nlx = l.x, nlz = l.z;
        if (up) {
            nlx = Math.min(Math.max(l.x, -0.9), -0.4);
            nlz = Math.min(Math.max(l.z, -0.8), 0.8);
        }
        double nly = ny - w.getY();
        Vec3 wp = w.toWorld(nlx, nly, nlz);
        p.setPos(wp.x, wp.y, wp.z);
        Vec3 vel = p.getDeltaMovement();
        p.setDeltaMovement(vel.x * 0.3, 0, vel.z * 0.3);
        p.fallDistance = 0;

        if (nly >= SHAFT_TOP && nlx >= -0.95 && Math.abs(nlz) <= 0.9) {
            boardedId = w.getId();
            lx = nlx;
            ly = nly;
            lz = nlz;
            vy = 0;
            onGround = false;
            needInit = false;
            p.displayClientMessage(Component.translatable("eternitywalker.msg.hint"), true);
        }
    }

    // ------------------------------------------------------------------ interior

    private static boolean ladderBoarded() {
        return lx >= -1.05 && lx <= -0.45 && Math.abs(lz) <= 0.6 && ly <= LADDER_TOP && ly >= SHAFT_TOP - 0.5;
    }

    private static void stepHoriz(double dx, double dz) {
        if (!InteriorGrid.collides(lx + dx, ly, lz + dz)) {
            lx += dx;
            lz += dz;
            return;
        }
        for (double lift = 0.05; lift <= 0.6 + 1e-9; lift += 0.05) {
            if (!InteriorGrid.collides(lx + dx, ly + lift, lz + dz)) {
                lx += dx;
                lz += dz;
                ly += lift;
                return;
            }
        }
    }

    /** mueve en vertical en pasos finos; devuelve true si choco */
    private static boolean moveY(double dy) {
        double dir = Math.signum(dy), rem = Math.abs(dy);
        while (rem > 1e-9) {
            double m = Math.min(0.01, rem);
            if (InteriorGrid.collides(lx, ly + dir * m, lz)) return true;
            ly += dir * m;
            rem -= m;
        }
        return false;
    }

    private static void boardedTick(LocalPlayer p, WalkerEntity w) {
        InteriorGrid.load();
        Input in = p.input;

        if (needInit) {
            Vec3 l = w.toLocal(p.position());
            lx = l.x;
            ly = l.y;
            lz = l.z;
            vy = 0;
            onGround = true;
            needInit = false;
        }

        float f = in.forwardImpulse, s = in.leftImpulse;
        double len = Math.hypot(f, s);
        if (len > 1.0) {
            f /= (float) len;
            s /= (float) len;
        }
        double speed = 0.215 * (p.isSprinting() ? 1.3 : 1.0);
        double yr = Math.toRadians(p.getYRot());
        double wx = (s * Math.cos(yr) - f * Math.sin(yr)) * speed;
        double wz = (f * Math.cos(yr) + s * Math.sin(yr)) * speed;
        double a = Math.toRadians(w.getYRot() - 90.0);
        double c = Math.cos(a), sn = Math.sin(a);
        double dxL = wx * c + wz * sn;
        double dzL = -wx * sn + wz * c;

        int n = (int) Math.ceil(Math.max(Math.abs(dxL), Math.abs(dzL)) / 0.0625);
        n = Math.max(n, 1);
        for (int i = 0; i < n; i++) {
            stepHoriz(dxL / n, 0);
            stepHoriz(0, dzL / n);
        }

        boolean up = f > 0.1f || in.jumping;
        if (ladderBoarded()) {
            vy = up ? 0.17 : (in.shiftKeyDown ? 0.0 : -0.1);
            onGround = false;
        } else {
            if (onGround && in.jumping) vy = 0.42;
            vy = (vy - 0.08) * 0.98;
        }
        boolean hit = moveY(vy);
        if (hit) {
            onGround = vy < 0;
            vy = 0;
        } else {
            onGround = false;
        }

        // salir del casco (por la escotilla): vuelve la fisica normal
        if (ly < SHAFT_TOP - 0.2 || ly > 40 || Math.abs(lx) > 45 || Math.abs(lz) > 25) {
            boardedId = -1;
            Vec3 wp = w.toWorld(lx, ly, lz);
            p.setPos(wp.x, wp.y, wp.z);
            p.setDeltaMovement(0, 0, 0);
            p.fallDistance = 0;
            return;
        }

        Vec3 wp = w.toWorld(lx, ly, lz);
        p.setPos(wp.x, wp.y, wp.z);
        p.setDeltaMovement(Vec3.ZERO);
        p.setOnGround(onGround);
        p.fallDistance = 0;
    }
}
