package com.keinloree.story;

import com.mojang.brigadier.CommandDispatcher;
import com.mojang.brigadier.arguments.FloatArgumentType;
import com.mojang.brigadier.arguments.IntegerArgumentType;
import com.mojang.brigadier.arguments.StringArgumentType;
import com.mojang.brigadier.builder.LiteralArgumentBuilder;
import com.mojang.brigadier.context.CommandContext;
import java.util.ArrayList;
import java.util.List;
import java.util.function.Consumer;
import net.minecraft.commands.CommandSourceStack;
import net.minecraft.commands.Commands;
import net.minecraft.commands.SharedSuggestionProvider;
import net.minecraft.network.chat.Component;
import net.minecraft.server.level.ServerLevel;
import net.minecraft.server.level.ServerPlayer;

/** Comandos /keinloree (nivel 2). Otros paquetes anaden subcomandos con {@link #extend}. */
public final class StoryCommands {
    private static final List<Consumer<LiteralArgumentBuilder<CommandSourceStack>>> EXTENSIONS = new ArrayList<>();

    private StoryCommands() {}

    /** Permite que otros paquetes cuelguen subcomandos de /keinloree (llamar antes de RegisterCommandsEvent). */
    public static void extend(Consumer<LiteralArgumentBuilder<CommandSourceStack>> extension) {
        EXTENSIONS.add(extension);
    }

    public static void register(CommandDispatcher<CommandSourceStack> dispatcher) {
        LiteralArgumentBuilder<CommandSourceStack> root = Commands.literal("keinloree").requires(s -> s.hasPermission(2));

        root.then(Commands.literal("story")
                .then(Commands.literal("status").executes(StoryCommands::status))
                .then(Commands.literal("goto").then(Commands.argument("paso", StringArgumentType.word())
                        .suggests((c, b) -> SharedSuggestionProvider.suggest(Steps.ids(), b))
                        .executes(StoryCommands::gotoStep)))
                .then(Commands.literal("skip").executes(StoryCommands::skip))
                .then(Commands.literal("reset").executes(StoryCommands::reset))
                .then(Commands.literal("disable").executes(c -> setDisabled(c, true)))
                .then(Commands.literal("enable").executes(c -> setDisabled(c, false)))
                .then(Commands.literal("choice")
                        .then(Commands.argument("id", StringArgumentType.word())
                                .then(Commands.argument("clave", StringArgumentType.word()).executes(StoryCommands::choice)))));

        root.then(Commands.literal("scene").then(Commands.argument("id", StringArgumentType.word())
                .suggests((c, b) -> SharedSuggestionProvider.suggest(Script.ids(), b))
                .executes(StoryCommands::scene)));

        root.then(Commands.literal("cine").then(Commands.argument("nombre", StringArgumentType.word())
                .suggests((c, b) -> SharedSuggestionProvider.suggest(Cinematics.names(), b))
                .executes(StoryCommands::cine)));

        root.then(Commands.literal("fx").then(Commands.argument("tipo", StringArgumentType.word())
                .suggests((c, b) -> SharedSuggestionProvider.suggest(ScreenFx.names(), b))
                .executes(c -> fx(c, 20, 1.0F, "FFFFFF"))
                .then(Commands.argument("ticks", IntegerArgumentType.integer(1, 2000))
                        .executes(c -> fx(c, IntegerArgumentType.getInteger(c, "ticks"), 1.0F, "FFFFFF"))
                        .then(Commands.argument("intensidad", FloatArgumentType.floatArg(0.0F, 20.0F))
                                .executes(c -> fx(c, IntegerArgumentType.getInteger(c, "ticks"),
                                        FloatArgumentType.getFloat(c, "intensidad"), "FFFFFF"))
                                .then(Commands.argument("color", StringArgumentType.word())
                                        .executes(c -> fx(c, IntegerArgumentType.getInteger(c, "ticks"),
                                                FloatArgumentType.getFloat(c, "intensidad"),
                                                StringArgumentType.getString(c, "color"))))))));

        root.then(Commands.literal("title").then(Commands.argument("texto", StringArgumentType.greedyString())
                .executes(StoryCommands::title)));
        root.then(Commands.literal("whisper").then(Commands.argument("texto", StringArgumentType.greedyString())
                .executes(StoryCommands::whisper)));

        for (Consumer<LiteralArgumentBuilder<CommandSourceStack>> e : EXTENSIONS) {
            e.accept(root);
        }
        dispatcher.register(root);
    }

    private static ServerLevel level(CommandContext<CommandSourceStack> c) {
        return c.getSource().getServer().overworld();
    }

    private static void say(CommandContext<CommandSourceStack> c, String msg) {
        c.getSource().sendSuccess(() -> Component.literal(msg), false);
    }

    private static int status(CommandContext<CommandSourceStack> c) {
        ServerLevel lv = level(c);
        StoryCtx ctx = new StoryCtx(lv);
        Campaign cp = ctx.campaign;
        ServerPlayer hero = ctx.hero();
        say(c, "Historia: paso=" + (cp.step.isEmpty() ? "(ninguno)" : cp.step) + " (" + ctx.ticksInStep() + " ticks)"
                + (cp.disabled ? " [DESACTIVADA]" : "") + ", ruta=" + cp.path);
        say(c, "Heroe: " + (hero != null ? hero.getGameProfile().getName() : cp.hero != null ? "desconectado" : "ninguno")
                + ", ticks de mundo=" + cp.worldTicks + ", banderas=" + cp.flags.size());
        say(c, "Escena: " + (cp.sceneId.isEmpty() ? "(ninguna)" : cp.sceneId + " #" + cp.scenePc)
                + ", en cola=" + cp.sceneQueue.size() + ", guion=" + Script.size() + " escenas");
        return 1;
    }

    private static int gotoStep(CommandContext<CommandSourceStack> c) {
        String id = StringArgumentType.getString(c, "paso");
        if (Steps.get(id) == null) {
            c.getSource().sendFailure(Component.literal("No existe el paso " + id));
            return 0;
        }
        new StoryCtx(level(c)).goTo(id);
        say(c, "Paso: " + id);
        return 1;
    }

    private static int skip(CommandContext<CommandSourceStack> c) {
        ServerLevel lv = level(c);
        StoryCtx ctx = new StoryCtx(lv);
        Step step = Steps.get(ctx.campaign.step);
        ScenePlayer.stopAll(lv);
        if (step == null) {
            c.getSource().sendFailure(Component.literal("No hay paso actual"));
            return 0;
        }
        step.skip(ctx);
        say(c, "Paso saltado; ahora: " + ctx.campaign.step);
        return 1;
    }

    private static int reset(CommandContext<CommandSourceStack> c) {
        ServerLevel lv = level(c);
        ScenePlayer.stopAll(lv);
        Cinematics.stopAll(lv.getServer());
        Saga.resetWorld(lv);
        Campaign.get(lv).resetRun();
        Atmosphere.set(lv, 0, 0, 0);
        Director.reset();
        for (ServerPlayer p : lv.players()) {
            Hud.clear(p);
        }
        say(c, "Historia reiniciada");
        return 1;
    }

    private static int setDisabled(CommandContext<CommandSourceStack> c, boolean value) {
        Campaign cp = Campaign.get(level(c));
        cp.disabled = value;
        cp.setDirty();
        say(c, value ? "Historia desactivada" : "Historia activada");
        return 1;
    }

    private static int choice(CommandContext<CommandSourceStack> c) {
        String id = StringArgumentType.getString(c, "id");
        String key = StringArgumentType.getString(c, "clave");
        ServerLevel lv = level(c);
        if (!ScenePlayer.choose(lv, id, key)) {
            Campaign cp = Campaign.get(lv);
            cp.choices.put(id, key);
            cp.setDirty();
        }
        say(c, "Eleccion " + id + " = " + key);
        return 1;
    }

    private static int scene(CommandContext<CommandSourceStack> c) {
        String id = StringArgumentType.getString(c, "id");
        if (!ScenePlayer.play(id)) {
            c.getSource().sendFailure(Component.literal("No existe la escena " + id));
            return 0;
        }
        say(c, "Escena en cola: " + id);
        return 1;
    }

    private static int cine(CommandContext<CommandSourceStack> c) {
        String name = StringArgumentType.getString(c, "nombre");
        if (!Cinematics.playNamed(new StoryCtx(level(c)), name, new String[0])) {
            c.getSource().sendFailure(Component.literal("No se pudo reproducir la cinematica " + name
                    + " (disponibles: " + Cinematics.names() + ")"));
            return 0;
        }
        return 1;
    }

    private static int fx(CommandContext<CommandSourceStack> c, int ticks, float intensity, String color) {
        String name = StringArgumentType.getString(c, "tipo");
        int type = ScreenFx.parse(name);
        if (type < 0) {
            c.getSource().sendFailure(Component.literal("Tipo desconocido: " + name + " (" + ScreenFx.names() + ")"));
            return 0;
        }
        ScreenFx.sendAll(level(c), type, ticks, intensity, Script.parseColor(color));
        return 1;
    }

    private static int title(CommandContext<CommandSourceStack> c) {
        String text = StringArgumentType.getString(c, "texto");
        int bar = text.indexOf('|');
        String t = (bar < 0 ? text : text.substring(0, bar)).strip();
        String s = bar < 0 ? "" : text.substring(bar + 1).strip();
        TitleCards.sendAll(level(c), t, s, 0xFFFFFF, TitleCards.DEFAULT_TICKS);
        return 1;
    }

    private static int whisper(CommandContext<CommandSourceStack> c) {
        Whisper.sendAll(level(c), StringArgumentType.getString(c, "texto"));
        return 1;
    }
}
