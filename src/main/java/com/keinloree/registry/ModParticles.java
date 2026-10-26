package com.keinloree.registry;

import com.keinloree.KeinLooreMod;
import net.minecraft.core.particles.ParticleType;
import net.minecraft.core.particles.SimpleParticleType;
import net.minecraftforge.eventbus.api.IEventBus;
import net.minecraftforge.registries.DeferredRegister;
import net.minecraftforge.registries.ForgeRegistries;
import net.minecraftforge.registries.RegistryObject;

/** Particulas propias del motor de VFX (WP2). */
public final class ModParticles {
    public static final DeferredRegister<ParticleType<?>> PARTICLES =
            DeferredRegister.create(ForgeRegistries.PARTICLE_TYPES, KeinLooreMod.MODID);

    public static final RegistryObject<SimpleParticleType> EMBER = PARTICLES.register("ember", () -> new SimpleParticleType(true));
    public static final RegistryObject<SimpleParticleType> GLYPH = PARTICLES.register("glyph", () -> new SimpleParticleType(true));
    public static final RegistryObject<SimpleParticleType> STAR = PARTICLES.register("star", () -> new SimpleParticleType(true));
    public static final RegistryObject<SimpleParticleType> SPARK = PARTICLES.register("spark", () -> new SimpleParticleType(true));
    public static final RegistryObject<SimpleParticleType> SMOKE = PARTICLES.register("smoke", () -> new SimpleParticleType(true));
    public static final RegistryObject<SimpleParticleType> SHARD = PARTICLES.register("shard", () -> new SimpleParticleType(true));

    private ModParticles() {}

    public static void register(IEventBus modBus) {
        PARTICLES.register(modBus);
    }
}
