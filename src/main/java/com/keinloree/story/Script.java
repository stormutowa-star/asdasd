package com.keinloree.story;

import com.mojang.logging.LogUtils;
import java.io.BufferedReader;
import java.io.IOException;
import java.io.InputStream;
import java.io.InputStreamReader;
import java.nio.charset.StandardCharsets;
import java.util.ArrayList;
import java.util.Collections;
import java.util.LinkedHashMap;
import java.util.List;
import java.util.Map;
import java.util.regex.Matcher;
import java.util.regex.Pattern;
import javax.annotation.Nullable;
import net.minecraft.Util;
import net.minecraft.resources.ResourceKey;
import net.minecraft.server.level.ServerLevel;
import net.minecraft.server.level.ServerPlayer;
import net.minecraft.world.level.biome.Biome;
import org.slf4j.Logger;

/** Guion de la historia: carga y parseo tolerante de guion.txt (una linea mala se avisa y se salta). */
public final class Script {
    private static final Logger LOGGER = LogUtils.getLogger();
    private static final String RESOURCE = "/data/keinloree/story/guion.txt";

    public enum Op { SAY, WAIT, CINE, TITLE, WHISPER, SOUND, FX, ACTION, CHOICE, NEXT }

    /** Una instruccion de escena. Los campos usados dependen de {@link #op}. */
    public static final class Instr {
        public final Op op;
        public final int line;
        /** SAY: id del hablante; CINE/ACTION: nombre; TITLE: titulo; WHISPER: texto; SOUND: id; CHOICE/NEXT: id. */
        public String text = "";
        /** TITLE: subtitulo; CHOICE: pregunta. */
        public String text2 = "";
        /** SAY: texto de la linea. */
        public String body = "";
        /** SAY: duracion forzada (-1 = por defecto); WAIT: ticks; FX: ticks. */
        public int ticks = -1;
        public float a = 1.0F;
        public float b = 1.0F;
        public int color = 0xFFFFFF;
        public int type;
        public String[] args = new String[0];
        public final List<String> keys = new ArrayList<>();
        public final List<String> texts = new ArrayList<>();

        Instr(Op op, int line) {
            this.op = op;
            this.line = line;
        }
    }

    public static final class Scene {
        public final String id;
        public final List<Instr> instrs = new ArrayList<>();

        Scene(String id) {
            this.id = id;
        }

        /** Escena a encadenar al terminar (@escena_siguiente) o null. */
        @Nullable
        public String next() {
            String n = null;
            for (Instr i : instrs) {
                if (i.op == Op.NEXT) {
                    n = i.text;
                }
            }
            return n;
        }
    }

    public record Result(Map<String, Scene> scenes, int instructions, int warnings) {}

    private static final Pattern SCENE = Pattern.compile("^\\[escena\\s+([^\\]\\s]+)\\]$");
    private static final Pattern OPTION = Pattern.compile("^>\\s*([^:\\s]+)\\s*:\\s*(.+)$");
    private static final Pattern SAY = Pattern.compile("^([A-ZÁÉÍÓÚÑ_]+)(?:\\(([^)]*)\\))?\\s*:\\s*(.*)$");
    private static final Pattern FORCED = Pattern.compile("^t\\s*=\\s*(\\d+)$");

    private static volatile Map<String, Scene> scenes = Collections.emptyMap();

    private Script() {}

    public static int size() {
        return scenes.size();
    }

    @Nullable
    public static Scene get(String id) {
        return scenes.get(id);
    }

    public static java.util.Set<String> ids() {
        return scenes.keySet();
    }

    /** Carga el guion del classpath. Se llama al arrancar el servidor. */
    public static void load() {
        try (InputStream in = Script.class.getResourceAsStream(RESOURCE)) {
            if (in == null) {
                LOGGER.error("[Guion] No se encuentra {}", RESOURCE);
                scenes = Collections.emptyMap();
                return;
            }
            Result r = parse(new BufferedReader(new InputStreamReader(in, StandardCharsets.UTF_8)));
            scenes = r.scenes();
            LOGGER.info("[Guion] Cargadas {} escenas ({} instrucciones, {} avisos)", r.scenes().size(), r.instructions(), r.warnings());
        } catch (IOException e) {
            LOGGER.error("[Guion] Error leyendo el guion", e);
        }
    }

    /** Parsea un guion. Las lineas malas se registran con WARN y se saltan. */
    public static Result parse(BufferedReader reader) throws IOException {
        Map<String, Scene> out = new LinkedHashMap<>();
        int[] warn = {0};
        int count = 0;
        Scene cur = null;
        Instr lastChoice = null;
        int n = 0;
        String raw;
        while ((raw = reader.readLine()) != null) {
            n++;
            String line = raw.strip();
            if (n == 1 && line.startsWith("﻿")) {
                line = line.substring(1).strip();
            }
            if (line.isEmpty() || line.startsWith("#")) {
                continue;
            }
            if (line.startsWith("[")) {
                Matcher m = SCENE.matcher(line);
                if (!m.matches()) {
                    warn(warn, n, "cabecera de escena mal formada: " + line);
                    cur = null;
                    continue;
                }
                if (out.containsKey(m.group(1))) {
                    warn(warn, n, "escena repetida (se sustituye): " + m.group(1));
                }
                cur = new Scene(m.group(1));
                out.put(cur.id, cur);
                lastChoice = null;
                continue;
            }
            if (cur == null) {
                warn(warn, n, "instruccion fuera de una escena: " + line);
                continue;
            }
            if (line.startsWith(">")) {
                Matcher m = OPTION.matcher(line);
                if (lastChoice == null) {
                    warn(warn, n, "opcion sin @eleccion: " + line);
                } else if (!m.matches()) {
                    warn(warn, n, "opcion mal formada: " + line);
                } else {
                    lastChoice.keys.add(m.group(1));
                    lastChoice.texts.add(m.group(2).strip());
                }
                continue;
            }
            lastChoice = null;
            Instr in = line.startsWith("@") ? directive(line, n, warn) : speech(line, n, warn);
            if (in != null) {
                cur.instrs.add(in);
                count++;
                if (in.op == Op.CHOICE) {
                    lastChoice = in;
                }
            }
        }
        // Validaciones cruzadas
        for (Scene s : out.values()) {
            for (Instr i : s.instrs) {
                if (i.op == Op.CHOICE && i.keys.isEmpty()) {
                    warn(warn, i.line, "eleccion '" + i.text + "' sin opciones");
                } else if (i.op == Op.NEXT && !out.containsKey(i.text)) {
                    warn(warn, i.line, "@escena_siguiente apunta a una escena inexistente: " + i.text);
                }
            }
        }
        return new Result(out, count, warn[0]);
    }

    private static void warn(int[] counter, int line, String msg) {
        counter[0]++;
        LOGGER.warn("[Guion] guion.txt:{}: {}", line, msg);
    }

    @Nullable
    private static Instr speech(String line, int n, int[] warn) {
        Matcher m = SAY.matcher(line);
        if (!m.matches()) {
            warn(warn, n, "linea no reconocida: " + line);
            return null;
        }
        if (Speaker.get(m.group(1)) == null) {
            warn(warn, n, "hablante desconocido: " + m.group(1));
            return null;
        }
        String body = m.group(3).strip();
        if (body.isEmpty()) {
            warn(warn, n, "linea de dialogo vacia");
            return null;
        }
        Instr i = new Instr(Op.SAY, n);
        i.text = m.group(1);
        i.body = body;
        if (m.group(2) != null) {
            Matcher f = FORCED.matcher(m.group(2).strip());
            if (f.matches()) {
                i.ticks = Integer.parseInt(f.group(1));
            } else {
                warn(warn, n, "modificador de hablante no valido: (" + m.group(2) + ")");
            }
        }
        return i;
    }

    @Nullable
    private static Instr directive(String line, int n, int[] warn) {
        String[] parts = line.substring(1).strip().split("\\s+", 2);
        String name = parts[0].toLowerCase(java.util.Locale.ROOT);
        String rest = parts.length > 1 ? parts[1].strip() : "";
        String[] tok = rest.isEmpty() ? new String[0] : rest.split("\\s+");
        switch (name) {
            case "espera" -> {
                Integer t = tok.length == 1 ? parseInt(tok[0]) : null;
                if (t == null || t < 0) {
                    warn(warn, n, "@espera necesita ticks: " + line);
                    return null;
                }
                Instr i = new Instr(Op.WAIT, n);
                i.ticks = t;
                return i;
            }
            case "cinematica", "accion" -> {
                if (tok.length == 0) {
                    warn(warn, n, "@" + name + " necesita un nombre");
                    return null;
                }
                Instr i = new Instr(name.equals("accion") ? Op.ACTION : Op.CINE, n);
                i.text = tok[0];
                i.args = java.util.Arrays.copyOfRange(tok, 1, tok.length);
                return i;
            }
            case "titulo" -> {
                if (rest.isEmpty()) {
                    warn(warn, n, "@titulo vacio");
                    return null;
                }
                Instr i = new Instr(Op.TITLE, n);
                int bar = rest.indexOf('|');
                i.text = (bar < 0 ? rest : rest.substring(0, bar)).strip();
                i.text2 = bar < 0 ? "" : rest.substring(bar + 1).strip();
                return i;
            }
            case "susurro" -> {
                if (rest.isEmpty()) {
                    warn(warn, n, "@susurro vacio");
                    return null;
                }
                Instr i = new Instr(Op.WHISPER, n);
                i.body = rest;
                return i;
            }
            case "sonido" -> {
                if (tok.length < 1) {
                    warn(warn, n, "@sonido necesita un id");
                    return null;
                }
                Instr i = new Instr(Op.SOUND, n);
                i.text = tok[0].contains(":") ? tok[0] : "keinloree:" + tok[0];
                i.a = tok.length > 1 ? parseFloat(tok[1], 1.0F) : 1.0F;
                i.b = tok.length > 2 ? parseFloat(tok[2], 1.0F) : 1.0F;
                return i;
            }
            case "fx" -> {
                int type = tok.length > 0 ? ScreenFx.parse(tok[0]) : -1;
                if (type < 0) {
                    warn(warn, n, "@fx con tipo desconocido: " + line);
                    return null;
                }
                Instr i = new Instr(Op.FX, n);
                i.type = type;
                i.ticks = tok.length > 1 ? Math.max(1, parseFloatInt(tok[1], 20)) : 20;
                i.a = tok.length > 2 ? parseFloat(tok[2], 1.0F) : 1.0F;
                i.color = tok.length > 3 ? parseColor(tok[3]) : 0xFFFFFF;
                return i;
            }
            case "eleccion" -> {
                if (tok.length == 0) {
                    warn(warn, n, "@eleccion necesita un id");
                    return null;
                }
                Instr i = new Instr(Op.CHOICE, n);
                i.text = tok[0];
                i.text2 = rest.substring(tok[0].length()).strip();
                return i;
            }
            case "escena_siguiente" -> {
                if (tok.length != 1) {
                    warn(warn, n, "@escena_siguiente necesita un id");
                    return null;
                }
                Instr i = new Instr(Op.NEXT, n);
                i.text = tok[0];
                return i;
            }
            default -> {
                warn(warn, n, "directiva desconocida: " + line);
                return null;
            }
        }
    }

    @Nullable
    private static Integer parseInt(String s) {
        try {
            return Integer.parseInt(s);
        } catch (NumberFormatException e) {
            return null;
        }
    }

    private static float parseFloat(String s, float def) {
        try {
            return Float.parseFloat(s);
        } catch (NumberFormatException e) {
            return def;
        }
    }

    private static int parseFloatInt(String s, int def) {
        return Math.round(parseFloat(s, def));
    }

    /** Color RGB en hexadecimal (con o sin #). */
    public static int parseColor(String s) {
        try {
            return Integer.parseInt(s.startsWith("#") ? s.substring(1) : s, 16) & 0xFFFFFF;
        } catch (NumberFormatException e) {
            return 0xFFFFFF;
        }
    }

    /** Duracion por defecto de una linea: clamp(40 + 1.6 * largo, 50, 240). */
    public static int defaultTicks(String text) {
        return (int) Math.max(50, Math.min(240, 40 + 1.6 * text.length()));
    }

    /**
     * Rellena los marcadores de una linea: {jugador}, {y}, {muertes}, {dist}, {dia}, [[arma]], [[bioma]].
     * (Omniscience, WP12, puede afinarlos.) Otros marcadores [[clave]] los traduce el cliente.
     */
    public static String fill(String text, @Nullable ServerPlayer hero, ServerLevel level, Campaign c) {
        if (text.indexOf('{') < 0 && !text.contains("[[")) {
            return text;
        }
        String name = hero != null ? hero.getGameProfile().getName() : "Testigo";
        String out = text.replace("{jugador}", name);
        if (hero != null) {
            out = out.replace("{y}", Integer.toString(hero.getBlockY()))
                    .replace("{muertes}", Integer.toString(c.tale(hero.getUUID()).deaths))
                    .replace("{dist}", Integer.toString(c.counter("distancia")))
                    .replace("{dia}", Long.toString(level.getDayTime() / 24000L + 1));
            if (out.contains("[[arma]]")) {
                out = out.replace("[[arma]]", "[[" + hero.getMainHandItem().getDescriptionId() + "]]");
            }
            if (out.contains("[[bioma]]")) {
                String key = hero.level().getBiome(hero.blockPosition()).unwrapKey()
                        .map(ResourceKey::location)
                        .map(l -> Util.makeDescriptionId("biome", l))
                        .orElse("biome.minecraft.plains");
                out = out.replace("[[bioma]]", "[[" + key + "]]");
            }
        }
        return out;
    }
}
