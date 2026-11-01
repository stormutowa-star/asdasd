package com.ricardo.retorcido;

import net.minecraft.world.entity.SpawnPlacementTypes;
import net.minecraft.world.entity.monster.Monster;
import net.minecraft.world.level.levelgen.Heightmap;
import net.minecraftforge.event.entity.EntityAttributeCreationEvent;
import net.minecraftforge.event.entity.SpawnPlacementRegisterEvent;
import net.minecraftforge.eventbus.api.SubscribeEvent;
import net.minecraftforge.fml.common.Mod;

@Mod.EventBusSubscriber(modid = RetorcidoMod.MODID, bus = Mod.EventBusSubscriber.Bus.MOD)
public class RetorcidoCommonEvents {
    @SubscribeEvent
    public static void onAttributes(EntityAttributeCreationEvent event) {
        event.put(RetorcidoMod.RETORCIDO.get(), RetorcidoEntity.createAttributes().build());
        event.put(RetorcidoMod.AMIGO.get(), com.ricardo.retorcido.amigo.AmigoEntity.createAttributes().build());
    }

    @SubscribeEvent
    public static void onSpawnPlacements(SpawnPlacementRegisterEvent event) {
        event.register(RetorcidoMod.RETORCIDO.get(),
                SpawnPlacementTypes.ON_GROUND,
                Heightmap.Types.MOTION_BLOCKING_NO_LEAVES,
                Monster::checkMonsterSpawnRules,
                SpawnPlacementRegisterEvent.Operation.REPLACE);
    }
}
