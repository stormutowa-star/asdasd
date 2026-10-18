package com.ricardo.frutigerworld;

import com.mojang.serialization.Codec;
import com.mojang.serialization.codecs.RecordCodecBuilder;
import net.minecraft.core.BlockPos;
import net.minecraft.util.RandomSource;
import net.minecraft.world.level.WorldGenLevel;
import net.minecraft.world.level.levelgen.feature.Feature;
import net.minecraft.world.level.levelgen.feature.FeaturePlaceContext;
import net.minecraft.world.level.levelgen.feature.configurations.FeatureConfiguration;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

/** Una sola feature que reparte las estructuras de Frutiger World según el campo "kind" de su JSON. */
public class FrutigerFeature extends Feature<FrutigerFeature.Config> {

    private static final Logger LOGGER = LoggerFactory.getLogger(FrutigerFeature.class);
    /** Registro opcional de cada intento de colocación (activar con -Dfrutigerworld.debug=true). */
    private static final boolean DEBUG = Boolean.getBoolean("frutigerworld.debug");

    public FrutigerFeature(Codec<Config> codec) {
        super(codec);
    }

    @Override
    public boolean place(FeaturePlaceContext<Config> context) {
        WorldGenLevel level = context.level();
        BlockPos origin = context.origin();
        RandomSource random = context.random();
        String kind = context.config().kind();
        boolean placed = switch (kind) {
            case "dome" -> FrutigerStructures.dome(level, origin, random);
            case "fountain" -> FrutigerStructures.fountain(level, origin, random);
            case "lookout" -> FrutigerStructures.lookout(level, origin, random);
            case "turbine" -> FrutigerStructures.turbine(level, origin, random);
            case "bubble_cluster" -> FrutigerStructures.bubbleCluster(level, origin, random);
            case "palm_grove" -> FrutigerStructures.palmGrove(level, origin, random);
            default -> false;
        };
        if (DEBUG) {
            LOGGER.info("[frutigerworld] {} en {} {} {} -> {}", kind, origin.getX(), origin.getY(), origin.getZ(),
                    placed ? "colocada" : "descartada");
        }
        return placed;
    }

    public record Config(String kind) implements FeatureConfiguration {
        public static final Codec<Config> CODEC = RecordCodecBuilder.create(instance -> instance.group(
                Codec.STRING.fieldOf("kind").forGetter(Config::kind)
        ).apply(instance, Config::new));
    }
}
