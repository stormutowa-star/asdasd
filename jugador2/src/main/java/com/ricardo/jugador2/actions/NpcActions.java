package com.ricardo.jugador2.actions;

import com.ricardo.jugador2.npc.NpcPlayer;
import net.minecraft.commands.arguments.EntityAnchorArgument;
import net.minecraft.core.BlockPos;
import net.minecraft.core.Direction;
import net.minecraft.core.component.DataComponents;
import net.minecraft.network.protocol.game.ServerboundPlayerActionPacket;
import net.minecraft.server.level.ServerLevel;
import net.minecraft.world.InteractionHand;
import net.minecraft.world.InteractionResult;
import net.minecraft.world.entity.LivingEntity;
import net.minecraft.world.item.BlockItem;
import net.minecraft.world.item.ItemStack;
import net.minecraft.world.level.block.state.BlockState;
import net.minecraft.world.phys.BlockHitResult;
import net.minecraft.world.phys.Vec3;

/** Acciones del NPC con las mismas APIs que usa el juego para un jugador real. */
public final class NpcActions {
    private final NpcPlayer npc;
    private BlockPos miningPos;
    private int miningTicks;

    public NpcActions(NpcPlayer npc) {
        this.npc = npc;
    }

    /**
     * Empieza a minar pos. Hay que llamar a {@link #continueMining()} en cada tick hasta que el bloque caiga.
     * Devuelve false si no hay nada que minar.
     */
    public boolean startMining(BlockPos pos) {
        ServerLevel level = npc.serverLevel();
        if (level.getBlockState(pos).isAir() || level.getBlockState(pos).getDestroySpeed(level, pos) < 0) {
            return false;
        }
        miningPos = pos.immutable();
        miningTicks = 0;
        npc.gameMode.handleBlockBreakAction(pos, ServerboundPlayerActionPacket.Action.START_DESTROY_BLOCK,
                Direction.UP, level.getMaxBuildHeight(), 0);
        return true;
    }

    /**
     * Avanza el minado un tick. El juego solo avanza la animación del progreso: el bloque se rompe cuando el cliente
     * avisa con STOP_DESTROY_BLOCK al llegar a 1. Como el NPC no tiene cliente, avisa él con la misma cuenta que usa
     * el servidor para aceptar el aviso. Devuelve true si el bloque se rompió en este tick.
     */
    public boolean continueMining() {
        if (miningPos == null) {
            return false;
        }
        ServerLevel level = npc.serverLevel();
        BlockState state = level.getBlockState(miningPos);
        miningTicks++;
        if (!state.isAir() && state.getDestroyProgress(npc, level, miningPos) * (miningTicks + 1) >= 1.0F) {
            npc.gameMode.handleBlockBreakAction(miningPos, ServerboundPlayerActionPacket.Action.STOP_DESTROY_BLOCK,
                    Direction.UP, level.getMaxBuildHeight(), 0);
        }
        if (level.getBlockState(miningPos).isAir()) {
            miningPos = null;
            return true;
        }
        return false;
    }

    public void stopMining(BlockPos pos) {
        ServerLevel level = npc.serverLevel();
        npc.gameMode.handleBlockBreakAction(pos, ServerboundPlayerActionPacket.Action.ABORT_DESTROY_BLOCK,
                Direction.UP, level.getMaxBuildHeight(), 0);
        miningPos = null;
    }

    /** Coloca el bloque de la ranura en target, apoyándolo en la cara face del bloque vecino. */
    public boolean placeBlock(BlockPos target, Direction face, int hotbarSlot) {
        selectHotbarSlot(hotbarSlot);
        ItemStack stack = npc.getMainHandItem();
        if (!(stack.getItem() instanceof BlockItem)) {
            return false;
        }
        BlockPos clicked = target.relative(face.getOpposite());
        Vec3 hitLocation = Vec3.atCenterOf(clicked)
                .add(face.getStepX() * 0.5, face.getStepY() * 0.5, face.getStepZ() * 0.5);
        BlockHitResult hit = new BlockHitResult(hitLocation, face, clicked, false);
        InteractionResult result = npc.gameMode.useItemOn(npc, npc.serverLevel(), stack, InteractionHand.MAIN_HAND, hit);
        return result.consumesAction();
    }

    /** Selecciona la ranura y empieza a comer; el juego termina de comer en sus ticks. */
    public boolean eat(int hotbarSlot) {
        selectHotbarSlot(hotbarSlot);
        if (!npc.getMainHandItem().has(DataComponents.FOOD)) {
            return false;
        }
        npc.startUsingItem(InteractionHand.MAIN_HAND);
        return true;
    }

    /** Ataca solo cuando el cooldown de ataque está listo, como un jugador. */
    public boolean attack(LivingEntity target) {
        if (npc.getAttackStrengthScale(0.0F) < 1.0F) {
            return false;
        }
        npc.attack(target);
        npc.swing(InteractionHand.MAIN_HAND);
        npc.resetAttackStrengthTicker();
        return true;
    }

    public void selectHotbarSlot(int slot) {
        npc.getInventory().selected = slot;
    }

    public void lookAt(Vec3 point) {
        npc.lookAt(EntityAnchorArgument.Anchor.EYES, point);
    }
}
