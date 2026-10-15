package com.ricardo.jugador2.commands;

import com.mojang.brigadier.CommandDispatcher;
import com.mojang.brigadier.arguments.IntegerArgumentType;
import com.mojang.brigadier.arguments.StringArgumentType;
import com.ricardo.jugador2.Jugador2Mod;
import com.ricardo.jugador2.chat.Follow;
import com.ricardo.jugador2.chat.GoTo;
import com.ricardo.jugador2.chat.Stop;
import com.ricardo.jugador2.npc.NpcManager;
import com.ricardo.jugador2.npc.NpcPlayer;
import net.minecraft.commands.CommandSourceStack;
import net.minecraft.commands.Commands;
import net.minecraft.network.chat.Component;

/** Comandos /jugador2 para operadores (nivel 2): spawn, remove, status y las órdenes follow, goto y stop. */
public final class Jugador2Command {
    private Jugador2Command() {
    }

    public static void register(CommandDispatcher<CommandSourceStack> dispatcher) {
        dispatcher.register(Commands.literal("jugador2")
                .requires(source -> source.hasPermission(2))
                .then(Commands.literal("spawn").executes(ctx -> spawn(ctx.getSource())))
                .then(Commands.literal("remove").executes(ctx -> remove(ctx.getSource())))
                .then(Commands.literal("status").executes(ctx -> status(ctx.getSource())))
                .then(Commands.literal("follow")
                        .then(Commands.argument("jugador", StringArgumentType.word())
                                .executes(ctx -> follow(ctx.getSource(), StringArgumentType.getString(ctx, "jugador")))))
                .then(Commands.literal("goto")
                        .then(Commands.argument("x", IntegerArgumentType.integer())
                                .then(Commands.argument("y", IntegerArgumentType.integer())
                                        .then(Commands.argument("z", IntegerArgumentType.integer())
                                                .executes(ctx -> goTo(ctx.getSource(),
                                                        IntegerArgumentType.getInteger(ctx, "x"),
                                                        IntegerArgumentType.getInteger(ctx, "y"),
                                                        IntegerArgumentType.getInteger(ctx, "z")))))))
                .then(Commands.literal("stop").executes(ctx -> stop(ctx.getSource()))));
    }

    private static int spawn(CommandSourceStack source) {
        NpcManager manager = Jugador2Mod.manager();
        NpcPlayer npc = manager.spawn(manager.profile(), source.getLevel(), source.getPosition());
        if (npc == null) {
            source.sendFailure(Component.literal("El NPC está esperando para reaparecer"));
            return 0;
        }
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

    private static int follow(CommandSourceStack source, String player) {
        if (!hasActiveNpc(source)) {
            return 0;
        }
        if (source.getServer().getPlayerList().getPlayerByName(player) == null) {
            source.sendFailure(Component.literal(player + " no está conectado"));
            return 0;
        }
        Jugador2Mod.manager().handleOrder(new Follow(player), source.getTextName());
        return 1;
    }

    private static int goTo(CommandSourceStack source, int x, int y, int z) {
        if (!hasActiveNpc(source)) {
            return 0;
        }
        Jugador2Mod.manager().handleOrder(new GoTo(x, y, z), source.getTextName());
        return 1;
    }

    private static int stop(CommandSourceStack source) {
        if (!hasActiveNpc(source)) {
            return 0;
        }
        Jugador2Mod.manager().handleOrder(new Stop(), source.getTextName());
        return 1;
    }

    private static boolean hasActiveNpc(CommandSourceStack source) {
        if (Jugador2Mod.manager().npc() == null) {
            source.sendFailure(Component.literal("No hay NPC activo"));
            return false;
        }
        return true;
    }
}
