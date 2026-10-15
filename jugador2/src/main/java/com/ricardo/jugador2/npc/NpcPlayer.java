package com.ricardo.jugador2.npc;

import com.mojang.authlib.GameProfile;
import net.minecraft.server.MinecraftServer;
import net.minecraft.server.level.ClientInformation;
import net.minecraft.server.level.ServerLevel;
import net.minecraft.server.level.ServerPlayer;
import net.minecraft.server.network.CommonListenerCookie;
import net.minecraft.world.phys.Vec3;

/** Jugador real del servidor sin cliente: aparece en la lista de jugadores y usa las mismas APIs que cualquier jugador. */
public final class NpcPlayer extends ServerPlayer {
    private NpcPlayer(MinecraftServer server, ServerLevel level, GameProfile profile) {
        super(server, level, profile, ClientInformation.createDefault());
    }

    /**
     * El servidor tickea a un jugador real en dos sitios: el nivel llama a {@code tick()} (modo de juego, temporizadores,
     * menús) y la conexión del jugador llama a {@code doTick()} (física, vida, comida, uso de objetos). El NPC no tiene
     * conexión que haga la segunda llamada, así que la hace aquí. También renueva el ticket del chunk cada 10 ticks,
     * como un jugador real al moverse, porque el servidor solo tickea entidades de chunks con ticket de jugador.
     */
    @Override
    public void tick() {
        super.tick();
        // Un NPC muerto no tickea su cuerpo: así no recoge sus propios objetos antes de que el gestor lo retire.
        if (!isDeadOrDying()) {
            doTick();
        }
        if (getServer().getTickCount() % 10 == 0) {
            serverLevel().getChunkSource().move(this);
        }
    }

    /** Como {@link #spawnNew}, pero con la vida que tenía el cuerpo al cerrar el servidor, para retomarlo igual. */
    public static NpcPlayer restore(MinecraftServer server, ServerLevel level, GameProfile profile, Vec3 pos,
                                    float health) {
        NpcPlayer npc = spawnNew(server, level, profile, pos);
        npc.setHealth(Math.max(1.0F, Math.min(health, npc.getMaxHealth())));
        return npc;
    }

    /** Crea el jugador, lo registra en la lista de jugadores y lo coloca en {@code pos} con vida completa. */
    public static NpcPlayer spawnNew(MinecraftServer server, ServerLevel level, GameProfile profile, Vec3 pos) {
        NpcPlayer npc = new NpcPlayer(server, level, profile);
        server.getPlayerList().placeNewPlayer(
                new FakeConnection(),
                npc,
                new CommonListenerCookie(profile, 0, npc.clientInformation(), false));
        npc.teleportTo(level, pos.x, pos.y, pos.z, 0f, 0f);
        // El servidor solo tickea a un jugador si su chunk tiene ticket de jugador, y ese ticket lo pone el propio
        // tick: sin esta llamada el NPC nunca tickea (ni minar, ni comer, ni atacar).
        npc.serverLevel().getChunkSource().move(npc);
        npc.setHealth(npc.getMaxHealth());
        return npc;
    }
}
