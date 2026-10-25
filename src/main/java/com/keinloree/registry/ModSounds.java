package com.keinloree.registry;

import com.keinloree.KeinLooreMod;
import net.minecraft.resources.ResourceLocation;
import net.minecraft.sounds.SoundEvent;
import net.minecraftforge.eventbus.api.IEventBus;
import net.minecraftforge.registries.DeferredRegister;
import net.minecraftforge.registries.ForgeRegistries;
import net.minecraftforge.registries.RegistryObject;

/** Sonidos propios del mod (generados por sintesis, ver assets/keinloree/sounds). */
public final class ModSounds {
    public static final DeferredRegister<SoundEvent> SOUNDS =
            DeferredRegister.create(ForgeRegistries.SOUND_EVENTS, KeinLooreMod.MODID);

    public static final RegistryObject<SoundEvent> BLASTER = reg("blaster");
    public static final RegistryObject<SoundEvent> TURBOLASER = reg("turbolaser");
    public static final RegistryObject<SoundEvent> KEIN_BEAM = reg("kein_beam");
    public static final RegistryObject<SoundEvent> FIGHTER_PASS = reg("fighter_pass");
    public static final RegistryObject<SoundEvent> FIGHTER_ENGINE = reg("fighter_engine");
    public static final RegistryObject<SoundEvent> DESTROYER_HUM = reg("destroyer_hum");
    public static final RegistryObject<SoundEvent> HYPERSPACE = reg("hyperspace");
    public static final RegistryObject<SoundEvent> ALARM = reg("alarm");
    public static final RegistryObject<SoundEvent> EXPLOSION_BIG = reg("explosion_big");
    public static final RegistryObject<SoundEvent> IMPACT = reg("impact");
    public static final RegistryObject<SoundEvent> SLASH_LIGHT = reg("slash_light");
    public static final RegistryObject<SoundEvent> SLASH_HEAVY = reg("slash_heavy");
    public static final RegistryObject<SoundEvent> TIME_STOP = reg("time_stop");
    public static final RegistryObject<SoundEvent> TIME_TICK = reg("time_tick");
    public static final RegistryObject<SoundEvent> STAR_FALL = reg("star_fall");
    public static final RegistryObject<SoundEvent> SHADOW_STEP = reg("shadow_step");
    public static final RegistryObject<SoundEvent> SOLAR_FLARE = reg("solar_flare");
    public static final RegistryObject<SoundEvent> RAREIRI_HUM = reg("rareiri_hum");
    public static final RegistryObject<SoundEvent> RAREIRI_SWING = reg("rareiri_swing");
    public static final RegistryObject<SoundEvent> RAREIRI_ULTIMATE = reg("rareiri_ultimate");
    public static final RegistryObject<SoundEvent> KEIN_WHISPER = reg("kein_whisper");
    public static final RegistryObject<SoundEvent> GLITCH = reg("glitch");
    public static final RegistryObject<SoundEvent> DRONE_AMBIENT = reg("drone_ambient");
    public static final RegistryObject<SoundEvent> HEARTBEAT = reg("heartbeat");
    public static final RegistryObject<SoundEvent> DIALOGUE_BLIP = reg("dialogue_blip");
    public static final RegistryObject<SoundEvent> CHOICE = reg("choice");
    public static final RegistryObject<SoundEvent> CHAPTER_STING = reg("chapter_sting");
    public static final RegistryObject<SoundEvent> PILLAR_HUM = reg("pillar_hum");
    public static final RegistryObject<SoundEvent> DOOR = reg("door");
    public static final RegistryObject<SoundEvent> RAZAKO_ROAR = reg("razako_roar");
    public static final RegistryObject<SoundEvent> CHAIN = reg("chain");
    public static final RegistryObject<SoundEvent> HOLOGRAM = reg("hologram");

    private ModSounds() {}

    private static RegistryObject<SoundEvent> reg(String id) {
        return SOUNDS.register(id, () -> SoundEvent.createVariableRangeEvent(
                ResourceLocation.fromNamespaceAndPath(KeinLooreMod.MODID, id)));
    }

    public static void register(IEventBus modBus) {
        SOUNDS.register(modBus);
    }
}
