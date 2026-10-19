package com.lenadorbosque.story;

import com.lenadorbosque.ModItems;
import com.lenadorbosque.entity.WoodcutterEntity;
import com.lenadorbosque.world.GroveBuilder;
import net.minecraft.ChatFormatting;
import net.minecraft.core.BlockPos;
import net.minecraft.core.particles.ParticleOptions;
import net.minecraft.core.particles.ParticleTypes;
import net.minecraft.network.chat.ClickEvent;
import net.minecraft.network.chat.Component;
import net.minecraft.network.chat.Style;
import net.minecraft.network.protocol.game.ClientboundSetSubtitleTextPacket;
import net.minecraft.network.protocol.game.ClientboundSetTitleTextPacket;
import net.minecraft.network.protocol.game.ClientboundSetTitlesAnimationPacket;
import net.minecraft.nbt.CompoundTag;
import net.minecraft.server.level.ServerLevel;
import net.minecraft.server.level.ServerPlayer;
import net.minecraft.sounds.SoundEvents;
import net.minecraft.sounds.SoundSource;
import net.minecraft.world.item.ItemStack;
import net.minecraft.world.level.block.Blocks;
import net.minecraft.world.level.levelgen.Heightmap;
import net.minecraft.world.phys.AABB;

/**
 * Máquina de estados de la historia. El progreso vive en los datos persistentes del jugador,
 * así que sobrevive a cerrar el juego. Cada decisión se elige con un enlace en el chat.
 *
 * Etapas: NONE -> TALK (decisión 1) -> DEER (decisión 2) -> FINAL (decisión 3) -> END.
 * Banderas: knowsTruth (preguntó por el roble) y respectedDeer (respetó al ciervo).
 */
public final class StoryManager {

    private static final int NONE = 0;
    private static final int TALK = 1;
    private static final int DEER = 2;
    private static final int FINAL = 3;
    private static final int END = 4;

    private static final String K_STAGE = "lb_stage";
    private static final String K_KNOWS = "lb_knows_truth";
    private static final String K_RESPECT = "lb_respected_deer";
    private static final String K_ORIGIN = "lb_origin";

    private StoryManager() {}

    public static void onLetterUsed(ServerPlayer player, ServerLevel level) {
        int stage = player.getPersistentData().getInt(K_STAGE);
        if (stage == NONE) {
            begin(player, level);
        } else if (stage == END) {
            say(player, "La historia de la Arboleda de Ámbar ya terminó. Un operador puede reiniciarla con /lenadorbosque reiniciar.");
        } else {
            say(player, "Tomás sigue en el bosque, esperándote.");
            offerCurrent(player, stage);
        }
    }

    /** Llamado desde /lenadorbosque elegir <id>. Solo acepta decisiones válidas en la etapa actual. */
    public static void choose(ServerPlayer player, ServerLevel level, String id) {
        CompoundTag data = player.getPersistentData();
        int stage = data.getInt(K_STAGE);
        BlockPos o = origin(data);
        boolean respected = data.getBoolean(K_RESPECT);

        switch (id) {
            case "tomas_pregunta", "tomas_callar" -> {
                if (stage == TALK) {
                    talk(player, level, id.equals("tomas_pregunta"));
                } else {
                    reject(player);
                }
            }
            case "ciervo_respetar", "ciervo_profanar" -> {
                if (stage == DEER) {
                    deer(player, level, id.equals("ciervo_respetar"));
                } else {
                    reject(player);
                }
            }
            case "final_pacto" -> {
                if (stage == FINAL && respected) {
                    pacto(player, level, o);
                } else {
                    reject(player);
                }
            }
            case "final_cenizas" -> {
                if (stage == FINAL && !respected) {
                    cenizas(player, level, o);
                } else {
                    reject(player);
                }
            }
            case "final_relevo" -> {
                if (stage == FINAL) {
                    relevo(player, level, o);
                } else {
                    reject(player);
                }
            }
            default -> reject(player);
        }
    }

    /** Borra el progreso. Solo para pruebas; el comando exige permiso de operador. */
    public static void reset(ServerPlayer player) {
        CompoundTag data = player.getPersistentData();
        data.remove(K_STAGE);
        data.remove(K_KNOWS);
        data.remove(K_RESPECT);
        data.remove(K_ORIGIN);
        say(player, "La historia se ha reiniciado. Usa la carta de nuevo para empezar.");
    }

    // ---------- Escenas ----------

    private static void begin(ServerPlayer player, ServerLevel level) {
        int x = player.getBlockX();
        int z = player.getBlockZ() + 34;
        int y = level.getHeight(Heightmap.Types.MOTION_BLOCKING, x, z);
        BlockPos o = new BlockPos(x, y, z);

        CompoundTag data = player.getPersistentData();
        data.putInt(K_STAGE, TALK);
        data.putIntArray(K_ORIGIN, new int[] {o.getX(), o.getY(), o.getZ()});

        GroveBuilder.build(level, o);
        player.connection.teleport(o.getX() + 11.5D, o.getY(), o.getZ() + 3.5D, 90.0F, 0.0F);

        title(player, "La Arboleda de Ámbar", "Hay algo mal en el bosque");
        say(player, "Desdoblas la carta. Huele a resina y a humo viejo. Solo dice: «Ven al roble. Tomás.»");
        say(player, "Una neblina dorada cubre los árboles. Cuando se despeja, hay una cabaña de troncos junto a un tocón recién pulido.");
        say(player, "Tomás Ibarra, el leñador, tiene las manos vendadas. Te mira sin sorpresa:");
        say(player, "—Llegaste. Ya no puedo con esto solo. Cada mañana aparece un ciervo muerto en el claro del noreste, y no es el único animal que ha caído en este bosque.");
        offerCurrent(player, TALK);
    }

    private static void talk(ServerPlayer player, ServerLevel level, boolean askedAboutOak) {
        CompoundTag data = player.getPersistentData();
        data.putBoolean(K_KNOWS, askedAboutOak);
        data.putInt(K_STAGE, DEER);

        if (askedAboutOak) {
            say(player, "Tomás baja la mirada. «Hace treinta años corté el roble más viejo de la arboleda para levantar esta cabaña. No sabía que tenía dueño. Desde esa noche, los animales del bosque mueren uno a uno, y siempre aparecen en el claro del ciervo.»");
        } else {
            say(player, "Tomás aprieta el mango del hacha y no responde. Solo señala el noreste: «Ve al claro. Él te explicará lo que yo no puedo.»");
        }
        say(player, "Llegas al claro. El ciervo está tendido sobre la hojarasca, con los ojos abiertos y el pecho quieto. Su cornamenta brilla como ámbar.");
        offerCurrent(player, DEER);
    }

    private static void deer(ServerPlayer player, ServerLevel level, boolean respected) {
        CompoundTag data = player.getPersistentData();
        data.putBoolean(K_RESPECT, respected);
        data.putInt(K_STAGE, FINAL);

        if (respected) {
            player.addItem(new ItemStack(ModItems.BROTE_DE_AMBAR.get()));
            say(player, "Te acercas en silencio y apoyas la mano en su costado. Entre el pelaje cae una brizna de ámbar. Dentro hay un brote, todavía vivo.");
        } else {
            player.addItem(new ItemStack(ModItems.ASTA_DE_CIERVO.get()));
            say(player, "El asta se separa con un crujido seco. Por un instante, todo el bosque deja de hacer ruido.");
        }

        say(player, "Cae la noche. Tomás está junto a la fogata y se levanta cuando te ve llegar.");
        if (data.getBoolean(K_KNOWS)) {
            say(player, "—Ya lo sabes, ¿verdad? Lo siento. Nunca debí cortarlo.");
        } else {
            say(player, "—No sé de qué hablas... pero veo lo que llevas en las manos. Dime qué hiciste.");
        }
        offerCurrent(player, FINAL);
    }

    private static void pacto(ServerPlayer player, ServerLevel level, BlockPos o) {
        finish(player);
        say(player, "Plantas el brote sobre el tocón. La madera se abre como una flor de ámbar y una luz sube hasta las copas.");
        WoodcutterEntity tomas = findTomas(level, o);
        if (tomas != null) {
            tomas.kneel();
            tomas.fadeAfter(120);
        }
        burst(level, o, ParticleTypes.END_ROD, 120);
        title(player, "El Pacto", "La arboleda vuelve a respirar");
        say(player, "Tomás cae de rodillas, sonríe por primera vez y se deshace en polvo dorado. Los ciervos ya no aparecen. El bosque sigue otoñal, pero ahora está en paz.");
    }

    private static void cenizas(ServerPlayer player, ServerLevel level, BlockPos o) {
        finish(player);
        say(player, "Tomás ve la antorcha en tu mano y entiende. No te detiene. Da media vuelta y se aleja entre los árboles dorados, sin mirar atrás.");
        WoodcutterEntity tomas = findTomas(level, o);
        if (tomas != null) {
            tomas.flee();
        }
        burnCabin(level, o);
        burst(level, o, ParticleTypes.LARGE_SMOKE, 80);
        title(player, "Cenizas", "El roble ya no tiene quien lo cuide");
        say(player, "La cabaña arde hasta el amanecer. Del tocón solo queda el olor a resina. Esa noche, el claro del ciervo queda vacío.");
    }

    private static void relevo(ServerPlayer player, ServerLevel level, BlockPos o) {
        finish(player);
        player.addItem(new ItemStack(ModItems.HACHA_DE_TOMAS.get()));
        say(player, "Tomás te pone el hacha en las manos. Sus dedos vendados tiemblan. «Ahora te toca cargarlo a ti.» Luego se desvanece entre los árboles.");
        WoodcutterEntity tomas = findTomas(level, o);
        if (tomas != null) {
            tomas.fadeAfter(60);
        }
        burst(level, o, ParticleTypes.SOUL, 60);
        title(player, "El Relevo", "Los ciervos seguirán cayendo");
        say(player, "Te quedas solo con el hacha y el bosque. Al fondo del claro, el ciervo muerto vuelve a brillar.");
    }

    // ---------- Ayudas ----------

    private static void offerCurrent(ServerPlayer player, int stage) {
        if (stage == TALK) {
            choice(player, "tomas_pregunta", "Preguntarle qué pasó con el roble");
            choice(player, "tomas_callar", "Callar y mirar el tocón");
        } else if (stage == DEER) {
            choice(player, "ciervo_respetar", "Acercarte con cuidado y tocar al ciervo");
            choice(player, "ciervo_profanar", "Arrancarle una asta y llevártela");
        } else if (stage == FINAL) {
            if (player.getPersistentData().getBoolean(K_RESPECT)) {
                choice(player, "final_pacto", "Plantar el brote de ámbar en el tocón");
            } else {
                choice(player, "final_cenizas", "Prender fuego a la cabaña");
            }
            choice(player, "final_relevo", "Tomar el hacha de Tomás");
        }
    }

    private static void finish(ServerPlayer player) {
        player.getPersistentData().putInt(K_STAGE, END);
    }

    private static BlockPos origin(CompoundTag data) {
        int[] a = data.getIntArray(K_ORIGIN);
        return a.length == 3 ? new BlockPos(a[0], a[1], a[2]) : BlockPos.ZERO;
    }

    private static WoodcutterEntity findTomas(ServerLevel level, BlockPos o) {
        return level.getEntitiesOfClass(WoodcutterEntity.class, new AABB(o).inflate(40.0D))
                .stream().findFirst().orElse(null);
    }

    private static void burnCabin(ServerLevel level, BlockPos o) {
        for (int x = 1; x <= 7; x++) {
            for (int z = 1; z <= 5; z++) {
                BlockPos p = o.offset(x, 1, z);
                if (level.isEmptyBlock(p)) {
                    level.setBlockAndUpdate(p, Blocks.FIRE.defaultBlockState());
                }
            }
        }
    }

    private static void burst(ServerLevel level, BlockPos o, ParticleOptions particle, int count) {
        double cx = o.getX() + 4.5D;
        double cy = o.getY() + 1.0D;
        double cz = o.getZ() + 2.5D;
        level.sendParticles(particle, cx, cy, cz, count, 5.0D, 2.0D, 5.0D, 0.02D);
        level.playSound(null, cx, cy, cz, SoundEvents.TOTEM_USE, SoundSource.PLAYERS, 1.0F, 0.8F);
    }

    private static void title(ServerPlayer player, String title, String subtitle) {
        player.connection.send(new ClientboundSetTitlesAnimationPacket(10, 80, 20));
        player.connection.send(new ClientboundSetTitleTextPacket(Component.literal(title).withStyle(ChatFormatting.GOLD)));
        player.connection.send(new ClientboundSetSubtitleTextPacket(Component.literal(subtitle).withStyle(ChatFormatting.YELLOW)));
    }

    private static void say(ServerPlayer player, String text) {
        player.sendSystemMessage(Component.literal(text).withStyle(ChatFormatting.GRAY, ChatFormatting.ITALIC));
    }

    private static void choice(ServerPlayer player, String id, String label) {
        player.sendSystemMessage(Component.literal("> " + label).setStyle(
                Style.EMPTY.withColor(ChatFormatting.AQUA)
                        .withUnderlined(true)
                        .withClickEvent(new ClickEvent(ClickEvent.Action.RUN_COMMAND, "/lenadorbosque elegir " + id))));
    }

    private static void reject(ServerPlayer player) {
        say(player, "Esa decisión no es posible ahora.");
    }
}
