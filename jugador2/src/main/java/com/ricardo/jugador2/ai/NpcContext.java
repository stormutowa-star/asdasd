package com.ricardo.jugador2.ai;

import com.ricardo.jugador2.actions.NpcActions;
import com.ricardo.jugador2.nav.Cell;
import com.ricardo.jugador2.nav.NpcNavigator;
import com.ricardo.jugador2.nav.WorldBlockAccess;
import com.ricardo.jugador2.npc.NpcPlayer;
import com.ricardo.jugador2.npc.NpcSettings;
import net.minecraft.core.BlockPos;
import net.minecraft.server.MinecraftServer;
import net.minecraft.server.level.ServerLevel;

import java.util.List;

/** Lo que una tarea necesita en cada tick: el NPC, sus acciones, su movimiento, la configuración y la orden activa. */
public record NpcContext(MinecraftServer server, NpcPlayer npc, NpcActions actions, NpcSettings settings,
                         OrderState order, Movement movement) {

    /** Calcula la ruta hasta goal y, si existe, la sigue. Llamar como mucho una vez por tick. */
    public boolean walkTo(BlockPos goal) {
        ServerLevel level = npc.serverLevel();
        BlockPos start = npc.blockPosition();
        List<Cell> path = NpcNavigator.findPath(new WorldBlockAccess(level),
                new Cell(start.getX(), start.getY(), start.getZ()),
                new Cell(goal.getX(), goal.getY(), goal.getZ()),
                settings.pathSearchNodeLimit());
        if (path == null) {
            return false;
        }
        movement.follow(path);
        return true;
    }
}
