package com.ricardo.transformacion;

import net.minecraft.server.level.ServerPlayer;
import net.minecraft.world.entity.player.Player;
import net.minecraftforge.event.entity.player.PlayerEvent;
import net.minecraftforge.eventbus.api.SubscribeEvent;

public class TransformEvents {

    /** Cuando alguien empieza a ver a otro jugador, le enviamos el estado de ese jugador. */
    @SubscribeEvent
    public void onStartTracking(PlayerEvent.StartTracking event) {
        if (event.getEntity() instanceof ServerPlayer watcher && event.getTarget() instanceof Player target) {
            TransformNetwork.sendTo(watcher, target);
        }
    }

    @SubscribeEvent
    public void onLogin(PlayerEvent.PlayerLoggedInEvent event) {
        if (event.getEntity() instanceof ServerPlayer sp) {
            TransformNetwork.sendTo(sp, sp);
        }
    }

    @SubscribeEvent
    public void onRespawn(PlayerEvent.PlayerRespawnEvent event) {
        if (event.getEntity() instanceof ServerPlayer sp) {
            TransformNetwork.sendTo(sp, sp);
        }
    }

    @SubscribeEvent
    public void onDimensionChange(PlayerEvent.PlayerChangedDimensionEvent event) {
        if (event.getEntity() instanceof ServerPlayer sp) {
            TransformNetwork.sendTo(sp, sp);
        }
    }

    @SubscribeEvent
    public void onClone(PlayerEvent.Clone event) {
        TransformState.copy(event.getOriginal(), event.getEntity());
    }
}
