package com.ricardo.transformacion;

import com.ricardo.transformacion.TransformNetwork;
import com.ricardo.transformacion.TransformState;
import net.minecraft.server.level.ServerPlayer;
import net.minecraft.world.entity.player.Player;
import net.minecraftforge.event.entity.player.PlayerEvent;
import net.minecraftforge.eventbus.api.SubscribeEvent;

public class TransformEvents {
    @SubscribeEvent
    public void onStartTracking(PlayerEvent.StartTracking event) {
        Player player = event.getEntity();
        if (player instanceof ServerPlayer) {
            ServerPlayer watcher = (ServerPlayer)player;
            if (event.getTarget() instanceof Player target) {
                TransformNetwork.sendTo(watcher, target);
            }
        }
    }

    @SubscribeEvent
    public void onLogin(PlayerEvent.PlayerLoggedInEvent event) {
        Player player = event.getEntity();
        if (player instanceof ServerPlayer) {
            ServerPlayer sp = (ServerPlayer)player;
            TransformNetwork.sendTo(sp, sp);
        }
    }

    @SubscribeEvent
    public void onRespawn(PlayerEvent.PlayerRespawnEvent event) {
        Player player = event.getEntity();
        if (player instanceof ServerPlayer) {
            ServerPlayer sp = (ServerPlayer)player;
            TransformNetwork.sendTo(sp, sp);
        }
    }

    @SubscribeEvent
    public void onDimensionChange(PlayerEvent.PlayerChangedDimensionEvent event) {
        Player player = event.getEntity();
        if (player instanceof ServerPlayer) {
            ServerPlayer sp = (ServerPlayer)player;
            TransformNetwork.sendTo(sp, sp);
        }
    }

    @SubscribeEvent
    public void onClone(PlayerEvent.Clone event) {
        TransformState.copy(event.getOriginal(), event.getEntity());
    }
}

