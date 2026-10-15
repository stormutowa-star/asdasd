package com.ricardo.jugador2.commands;

import com.mojang.brigadier.CommandDispatcher;
import com.ricardo.jugador2.Jugador2Mod;
import com.ricardo.jugador2.npc.NpcManager;
import com.ricardo.jugador2.npc.NpcPlayer;
import net.minecraft.commands.CommandSourceStack;
import net.minecraft.commands.Commands;
import net.minecraft.network.chat.Component;
import net.minecraft.server.level.ServerLevel;
import net.minecraft.world.phys.Vec3;

/** Comandos /jugador2 para operadores (nivel 2): spawn, remove y status. */
public final class Jugador2Command {
    // Nombre fijo hasta la Tarea 8, que lo sustituye por el nombre generado para el mundo.
    private static final String NPC_NAME = "Jugador2";

    private Jugador2Command() {
    }

    public static void register(CommandDispatcher<CommandSourceStack> dispatcher) {
        dispatcher.register(Commands.literal("jugador2")
                .requires(source -> source.hasPermission(2))
                .then(Commands.literal("spawn").executes(ctx -> spawn(ctx.getSource())))
                .then(Commands.literal("remove").executes(ctx -> remove(ctx.getSource())))
                .then(Commands.literal("status").executes(ctx -> status(ctx.getSource()))));
    }

    private static int spawn(CommandSourceStack source) {
        NpcManager manager = Jugador2Mod.manager();
        ServerLevel level = source.getLevel();
        Vec3 pos = source.getPosition();
        NpcPlayer npc = manager.spawn(NpcManager.offlineProfile(NPC_NAME), level, pos);
        source.sendSuccess(() -> Component.literal("NPC activo: " + npc.getGameProfile().getName()), true);
        return 1;
    }

    private static int remove(CommandSourceStack source) {
        boolean removed = Jugador2Mod.manager().remove();
        source.sendSuccess(() -> Component.literal(removed ? "NPC retirado" : "No hay NPC activo"), true);
        return removed ? 1 : 0;
    }

    private static int status(CommandSourceStack source) {
        source.sendSuccess(() -> Component.literal(Jugador2Mod.manager().status()), false);
        return 1;
    }
}
