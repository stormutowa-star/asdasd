package com.ssj8.client;

import com.ssj8.SsjSounds;
import net.minecraft.client.resources.sounds.AbstractTickableSoundInstance;
import net.minecraft.client.resources.sounds.SoundInstance;
import net.minecraft.sounds.SoundSource;
import net.minecraft.world.entity.player.Player;

/**
 * Loop de carga de ki (ki_charge_loop, el mismo archivo que usa DragonMine Z en AuraLoopSound).
 * Suena en bucle mientras el jugador mantiene Z y sigue su posicion; se detiene al soltar.
 */
public final class KiChargeSound extends AbstractTickableSoundInstance {
    private final Player player;

    public KiChargeSound(Player player) {
        super(SsjSounds.KI_CHARGE, SoundSource.PLAYERS, SoundInstance.createUnseededRandom());
        this.player = player;
        this.looping = true;
        this.delay = 0;
        this.volume = 0.5f;
        this.pitch = 1.0f;
        this.x = player.getX();
        this.y = player.getY();
        this.z = player.getZ();
    }

    @Override
    public boolean canStartSilent() {
        return true;
    }

    @Override
    public void tick() {
        ClientState cs = ClientState.get(this.player);
        if (this.player.isRemoved() || cs == null || !cs.form || cs.mode != 1) {
            this.stop();
            return;
        }
        this.x = this.player.getX();
        this.y = this.player.getY();
        this.z = this.player.getZ();
    }
}
