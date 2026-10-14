package com.ssj8.client;

import java.util.HashMap;
import java.util.Map;
import net.minecraft.client.Minecraft;
import net.minecraft.client.player.AbstractClientPlayer;
import net.minecraftforge.event.TickEvent;
import net.minecraftforge.eventbus.api.SubscribeEvent;

/** Mantiene un loop de carga de ki por cada jugador que este cargando con Z en este cliente. */
public final class KiChargeSoundHandler {
    private static final Map<Integer, KiChargeSound> SOUNDS = new HashMap<>();

    private KiChargeSoundHandler() {
    }

    @SubscribeEvent
    public static void onClientTick(TickEvent.ClientTickEvent event) {
        if (event.phase != TickEvent.Phase.END) {
            return;
        }
        Minecraft mc = Minecraft.getInstance();
        if (mc.level == null || mc.isPaused()) {
            return;
        }
        SOUNDS.values().removeIf(KiChargeSound::isStopped);
        for (AbstractClientPlayer p : mc.level.players()) {
            ClientState cs = ClientState.get(p);
            boolean charging = cs != null && cs.form && cs.mode == 1 && !p.isInvisible();
            KiChargeSound current = SOUNDS.get(p.getId());
            boolean playing = current != null && mc.getSoundManager().isActive(current);
            if (charging && !playing) {
                KiChargeSound sound = new KiChargeSound(p);
                mc.getSoundManager().play(sound);
                SOUNDS.put(p.getId(), sound);
            } else if (!charging && current != null) {
                current.stop();
                SOUNDS.remove(p.getId());
            }
        }
    }
}
