package com.lenadorbosque.story;

import com.mojang.brigadier.arguments.StringArgumentType;
import com.mojang.brigadier.context.CommandContext;
import net.minecraft.commands.CommandSourceStack;
import net.minecraft.commands.Commands;
import net.minecraft.server.level.ServerPlayer;
import net.minecraftforge.common.MinecraftForge;
import net.minecraftforge.event.RegisterCommandsEvent;

public final class StoryCommands {

    private StoryCommands() {}

    public static void register() {
        MinecraftForge.EVENT_BUS.addListener(StoryCommands::onRegisterCommands);
    }

    private static void onRegisterCommands(RegisterCommandsEvent event) {
        event.getDispatcher().register(
                Commands.literal("lenadorbosque")
                        .then(Commands.literal("elegir")
                                .then(Commands.argument("id", StringArgumentType.word())
                                        .executes(StoryCommands::choose)))
                        .then(Commands.literal("reiniciar")
                                .requires(src -> src.hasPermission(2))
                                .executes(StoryCommands::reset)));
    }

    private static int choose(CommandContext<CommandSourceStack> ctx) {
        if (!(ctx.getSource().getEntity() instanceof ServerPlayer player)) {
            return 0;
        }
        StoryManager.choose(player, player.serverLevel(), StringArgumentType.getString(ctx, "id"));
        return 1;
    }

    private static int reset(CommandContext<CommandSourceStack> ctx) {
        if (!(ctx.getSource().getEntity() instanceof ServerPlayer player)) {
            return 0;
        }
        StoryManager.reset(player);
        return 1;
    }
}
