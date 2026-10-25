package com.keinloree.story;

import com.keinloree.net.Net;
import com.keinloree.net.VfxS2C;
import java.util.List;
import java.util.UUID;
import javax.annotation.Nullable;
import net.minecraft.core.BlockPos;
import net.minecraft.server.MinecraftServer;
import net.minecraft.server.level.ServerLevel;
import net.minecraft.server.level.ServerPlayer;
import net.minecraft.sounds.SoundEvent;
import net.minecraft.sounds.SoundSource;
import net.minecraft.world.entity.Entity;
import net.minecraft.world.phys.Vec3;

/** Contexto que reciben los pasos y las acciones: nivel, campana, heroe y ayudantes. */
public final class StoryCtx {
    public final ServerLevel level;
    public final Campaign campaign;

    public StoryCtx(ServerLevel overworld) {
        this.level = overworld;
        this.campaign = Campaign.get(overworld);
    }

    public MinecraftServer server() {
        return level.getServer();
    }

    /** El heroe si esta conectado; si no, null (el paso espera). */
    @Nullable
    public ServerPlayer hero() {
        UUID id = campaign.hero;
        return id == null ? null : server().getPlayerList().getPlayer(id);
    }

    /** Jugadores del Overworld. */
    public List<ServerPlayer> players() {
        return level.players();
    }

    public long ticksInStep() {
        return campaign.worldTicks - campaign.stepStart;
    }

    public void goTo(String stepId) {
        Director.goTo(this, stepId);
    }

    public boolean scene(String id) {
        return ScenePlayer.play(id);
    }

    /** Linea suelta de dialogo para todos (hablante del guion, p. ej. "KEIN"). */
    public void say(String speaker, String text) {
        ScenePlayer.say(level, campaign, speaker, text, -1);
    }

    public void title(String title, String subtitle) {
        TitleCards.sendAll(level, title, subtitle, 0xFFFFFF, TitleCards.DEFAULT_TICKS);
    }

    public void whisper(String text) {
        Whisper.sendAll(level, text);
    }

    public void fx(int type, int ticks, float intensity, int color) {
        ScreenFx.sendAll(level, type, ticks, intensity, color);
    }

    public void cinematic(String name, String... args) {
        Cinematics.playNamed(this, name, args);
    }

    /** HUD del heroe. */
    public void hud(String objective, @Nullable BlockPos marker, String label, int cur, int max, int color) {
        ServerPlayer h = hero();
        if (h != null) {
            Hud.send(h, objective, marker, label, cur, max, color);
        }
    }

    public void sound(SoundEvent sound, float volume, float pitch) {
        for (ServerPlayer p : players()) {
            p.playNotifySound(sound, SoundSource.MASTER, volume, pitch);
        }
    }

    /** Envia un efecto del motor de VFX a los jugadores cercanos (256 bloques). */
    public void vfx(VfxS2C msg) {
        Net.toNear(level, new Vec3(msg.x(), msg.y(), msg.z()), 256.0D, msg);
    }

    public boolean flag(String f) {
        return campaign.hasFlag(f);
    }

    public void setFlag(String f) {
        campaign.setFlag(f);
    }

    public int counter(String key) {
        return campaign.counter(key);
    }

    public int inc(String key) {
        return campaign.inc(key, 1);
    }

    @Nullable
    public BlockPos pos(String key) {
        return campaign.positions.get(key);
    }

    public void setPos(String key, BlockPos pos) {
        campaign.positions.put(key, pos);
        campaign.setDirty();
    }

    @Nullable
    public Entity entity(String key) {
        UUID id = campaign.entities.get(key);
        return id == null ? null : level.getEntity(id);
    }

    public void setEntity(String key, Entity e) {
        campaign.entities.put(key, e.getUUID());
        campaign.setDirty();
    }
}
