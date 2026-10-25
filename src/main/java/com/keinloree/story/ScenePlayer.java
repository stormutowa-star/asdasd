package com.keinloree.story;

import com.keinloree.net.ChoiceS2C;
import com.keinloree.net.DialogueS2C;
import com.keinloree.net.Net;
import com.mojang.logging.LogUtils;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.UUID;
import javax.annotation.Nullable;
import net.minecraft.core.registries.BuiltInRegistries;
import net.minecraft.resources.ResourceLocation;
import net.minecraft.server.MinecraftServer;
import net.minecraft.server.level.ServerLevel;
import net.minecraft.server.level.ServerPlayer;
import net.minecraft.sounds.SoundEvent;
import net.minecraft.sounds.SoundSource;
import net.minecraftforge.server.ServerLifecycleHooks;
import org.slf4j.Logger;

/**
 * Reproductor de escenas del guion. Una escena activa a la vez (mas una cola). El estado persistente
 * (escena, siguiente instruccion, cola) vive en {@link Campaign}; aqui solo hay temporizadores.
 */
public final class ScenePlayer {
    private static final Logger LOGGER = LogUtils.getLogger();
    private static final int WHISPER_TICKS = 70;
    private static final int MIN_SKIP_AGE = 20;
    private static final int CHOICE_RESEND = 100;

    private static int wait;
    private static int lineAge;
    private static boolean cut;
    @Nullable
    private static Script.Instr waitingChoice;
    private static int choiceAge;
    @Nullable
    private static UUID choiceSentTo;
    private static boolean headless;
    private static final Map<String, List<Runnable>> FINISH = new HashMap<>();

    private ScenePlayer() {}

    /** Modo de pruebas sin jugadores: avanza igualmente y elige la primera opcion. */
    public static void setHeadless(boolean value) {
        headless = value;
    }

    public static boolean isHeadless() {
        return headless;
    }

    /** Olvida los temporizadores (al arrancar o parar el servidor). */
    public static void reset() {
        wait = 0;
        lineAge = 0;
        cut = false;
        waitingChoice = null;
        choiceSentTo = null;
        headless = false;
        FINISH.clear();
    }

    @Nullable
    private static Campaign campaign() {
        MinecraftServer s = ServerLifecycleHooks.getCurrentServer();
        return s == null ? null : Campaign.get(s);
    }

    /** Encola (o inicia) una escena. Devuelve false si no existe. */
    public static boolean play(String id) {
        Campaign c = campaign();
        if (c == null) {
            return false;
        }
        if (Script.get(id) == null) {
            LOGGER.warn("[Escena] No existe la escena '{}'", id);
            return false;
        }
        if (c.sceneId.isEmpty()) {
            start(c, id);
        } else {
            c.sceneQueue.add(id);
        }
        c.setDirty();
        return true;
    }

    public static boolean isPlaying(String id) {
        Campaign c = campaign();
        return c != null && (id.equals(c.sceneId) || c.sceneQueue.contains(id));
    }

    public static boolean isIdle() {
        Campaign c = campaign();
        return c == null || (c.sceneId.isEmpty() && c.sceneQueue.isEmpty());
    }

    /** Ejecuta algo cuando la escena termine (no se guarda: se pierde al reiniciar). */
    public static void onFinished(String id, Runnable action) {
        FINISH.computeIfAbsent(id, k -> new ArrayList<>()).add(action);
    }

    /** Corta la escena actual y vacia la cola. */
    public static void stopAll(ServerLevel level) {
        Campaign c = Campaign.get(level);
        c.sceneId = "";
        c.scenePc = 0;
        c.sceneQueue.clear();
        c.setDirty();
        closeChoice(level);
        wait = 0;
        sendClear(level);
    }

    private static void start(Campaign c, String id) {
        c.sceneId = id;
        c.scenePc = 0;
        wait = 0;
        lineAge = 0;
        cut = false;
        waitingChoice = null;
        choiceSentTo = null;
        LOGGER.info("[Escena] Empieza '{}'", id);
    }

    // ---- bucle ----

    public static void tick(ServerLevel level) {
        Campaign c = Campaign.get(level);
        if (c.sceneId.isEmpty()) {
            if (c.sceneQueue.isEmpty()) {
                return;
            }
            start(c, c.sceneQueue.remove(0));
            c.setDirty();
        }
        if (level.players().isEmpty() && !headless) {
            return;
        }
        Script.Scene scene = Script.get(c.sceneId);
        if (scene == null) {
            LOGGER.warn("[Escena] La escena guardada '{}' ya no existe; se descarta", c.sceneId);
            c.sceneId = "";
            c.setDirty();
            return;
        }
        if (waitingChoice != null) {
            tickChoice(level, c);
            return;
        }
        lineAge++;
        if (wait > 0 && --wait > 0) {
            return;
        }
        while (true) {
            if (c.scenePc >= scene.instrs.size()) {
                finish(level, c, scene);
                return;
            }
            Script.Instr in = scene.instrs.get(c.scenePc);
            if (in.op == Script.Op.CHOICE) {
                waitingChoice = in;
                choiceSentTo = null;
                choiceAge = 0;
                tickChoice(level, c);
                return;
            }
            c.scenePc++;
            c.setDirty();
            int w = exec(level, c, scene, in);
            if (w > 0) {
                wait = w;
                return;
            }
        }
    }

    private static void finish(ServerLevel level, Campaign c, Script.Scene scene) {
        LOGGER.info("[Escena] Termina '{}'", scene.id);
        if (cut) {
            sendClear(level);
            cut = false;
        }
        String next = scene.next();
        c.sceneId = "";
        c.scenePc = 0;
        if (next != null) {
            c.sceneQueue.add(0, next);
        }
        c.setDirty();
        List<Runnable> done = FINISH.remove(scene.id);
        if (done != null) {
            for (Runnable r : done) {
                try {
                    r.run();
                } catch (RuntimeException e) {
                    LOGGER.error("[Escena] Fallo en onFinished de {}", scene.id, e);
                }
            }
        }
    }

    /** Ejecuta una instruccion y devuelve los ticks que hay que esperar. */
    private static int exec(ServerLevel level, Campaign c, Script.Scene scene, Script.Instr in) {
        StoryCtx ctx = new StoryCtx(level);
        ServerPlayer hero = ctx.hero();
        if (headless) {
            LOGGER.info("[Escena] {} #{} {} {}", scene.id, in.line, in.op, in.text + (in.body.isEmpty() ? "" : " | " + in.body));
        }
        switch (in.op) {
            case SAY -> {
                return say(level, c, in.text, in.body, in.ticks);
            }
            case WAIT -> {
                return in.ticks;
            }
            case CINE -> Cinematics.playNamed(ctx, in.text, in.args);
            case TITLE -> TitleCards.sendAll(level, Script.fill(in.text, hero, level, c),
                    Script.fill(in.text2, hero, level, c), 0xFFFFFF, TitleCards.DEFAULT_TICKS);
            case WHISPER -> {
                com.keinloree.story.Whisper.sendAll(level, Script.fill(in.body, hero, level, c));
                return WHISPER_TICKS;
            }
            case SOUND -> playSound(level, in);
            case FX -> ScreenFx.sendAll(level, in.type, in.ticks, in.a, in.color);
            case ACTION -> ScriptActions.run(ctx, in.text, in.args);
            default -> {
            }
        }
        return 0;
    }

    private static void playSound(ServerLevel level, Script.Instr in) {
        ResourceLocation id = ResourceLocation.tryParse(in.text);
        SoundEvent ev = id == null ? null : BuiltInRegistries.SOUND_EVENT.getOptional(id).orElse(null);
        if (ev == null) {
            LOGGER.warn("[Escena] Sonido desconocido en el guion (linea {}): {}", in.line, in.text);
            return;
        }
        for (ServerPlayer p : level.players()) {
            p.playNotifySound(ev, SoundSource.MASTER, in.a, in.b);
        }
    }

    /** Envia una linea de dialogo a todos los jugadores y devuelve su duracion. */
    static int say(ServerLevel level, Campaign c, String speakerId, String raw, int forced) {
        Speaker sp = Speaker.get(speakerId);
        if (sp == null) {
            LOGGER.warn("[Escena] Hablante desconocido: {}", speakerId);
            return 0;
        }
        ServerPlayer hero = new StoryCtx(level).hero();
        String text = Script.fill(raw, hero, level, c);
        String name = sp.name();
        if (Speaker.PLAYER_ID.equals(sp.id())) {
            name = hero != null ? hero.getGameProfile().getName() : "Testigo";
        }
        int ticks = forced > 0 ? forced : Script.defaultTicks(text);
        Net.toAll(level, new DialogueS2C(sp.id(), name, sp.color(), text, ticks, sp.flags()));
        lineAge = 0;
        cut = false;
        return ticks;
    }

    private static void sendClear(ServerLevel level) {
        Net.toAll(level, new DialogueS2C("", "", 0xFFFFFF, "", 0, DialogueS2C.LIMPIAR));
    }

    // ---- elecciones ----

    private static void tickChoice(ServerLevel level, Campaign c) {
        Script.Instr in = waitingChoice;
        if (in == null) {
            return;
        }
        if (headless) {
            if (++choiceAge >= 20 && !in.keys.isEmpty()) {
                choose(level, in.text, in.keys.get(0));
            }
            return;
        }
        ServerPlayer hero = new StoryCtx(level).hero();
        if (hero == null) {
            choiceSentTo = null;
            return;
        }
        if (!hero.getUUID().equals(choiceSentTo) || ++choiceAge >= CHOICE_RESEND) {
            List<String> texts = new ArrayList<>();
            for (String t : in.texts) {
                texts.add(Script.fill(t, hero, level, c));
            }
            Net.toPlayer(hero, new ChoiceS2C(in.text, Script.fill(in.text2, hero, level, c), new ArrayList<>(in.keys), texts));
            choiceSentTo = hero.getUUID();
            choiceAge = 0;
        }
    }

    private static void closeChoice(ServerLevel level) {
        Script.Instr in = waitingChoice;
        waitingChoice = null;
        ServerPlayer hero = new StoryCtx(level).hero();
        if (in != null && hero != null) {
            Net.toPlayer(hero, new ChoiceS2C(in.text, "", List.of(), List.of()));
        }
    }

    /** Respuesta del cliente: solo vale si la envia el heroe y la eleccion esta pendiente. */
    public static void onChoice(ServerPlayer sender, String id, String key) {
        Campaign c = Campaign.get(sender.getServer());
        if (c.hero == null || !c.hero.equals(sender.getUUID())) {
            return;
        }
        choose((ServerLevel) sender.level().getServer().overworld(), id, key);
    }

    /** Aplica una eleccion (tambien la usa el comando). Devuelve true si estaba pendiente. */
    public static boolean choose(ServerLevel level, String id, String key) {
        Campaign c = Campaign.get(level);
        Script.Instr in = waitingChoice;
        if (in == null || !in.text.equals(id) || !in.keys.contains(key)) {
            return false;
        }
        c.choices.put(id, key);
        c.scenePc++;
        c.setDirty();
        closeChoice(level);
        choiceSentTo = null;
        LOGGER.info("[Escena] Eleccion '{}' = {}", id, key);
        return true;
    }

    /** El heroe adelanta la linea actual si ya lleva suficiente tiempo. */
    public static void skip(ServerPlayer sender) {
        Campaign c = Campaign.get(sender.getServer());
        if (c.hero == null || !c.hero.equals(sender.getUUID())) {
            return;
        }
        if (waitingChoice == null && wait > 1 && lineAge >= MIN_SKIP_AGE) {
            wait = 1;
            cut = true;
        }
    }
}
