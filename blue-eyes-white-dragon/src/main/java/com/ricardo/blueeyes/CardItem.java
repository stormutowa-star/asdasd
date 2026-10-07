package com.ricardo.blueeyes;

import java.util.List;

import net.minecraft.ChatFormatting;
import net.minecraft.core.BlockPos;
import net.minecraft.core.Direction;
import net.minecraft.network.chat.Component;
import net.minecraft.server.level.ServerLevel;
import net.minecraft.world.InteractionHand;
import net.minecraft.world.InteractionResultHolder;
import net.minecraft.world.entity.player.Player;
import net.minecraft.world.item.Item;
import net.minecraft.world.item.ItemStack;
import net.minecraft.world.item.TooltipFlag;
import net.minecraft.world.level.Level;
import net.minecraft.world.phys.BlockHitResult;
import net.minecraft.world.phys.HitResult;
import net.minecraft.world.phys.Vec3;
import net.minecraft.world.phys.shapes.VoxelShape;

/** La carta "Dragon Blanco de Ojos Azules". Clic derecho sobre el campo: la carta queda boca arriba y el dragon aparece encima. */
public class CardItem extends Item {

    private static final double RANGE = 24.0D;

    public CardItem(Properties properties) {
        super(properties);
    }

    @Override
    public InteractionResultHolder<ItemStack> use(Level level, Player player, InteractionHand hand) {
        ItemStack stack = player.getItemInHand(hand);
        HitResult hit = player.pick(RANGE, 1.0F, false);
        if (hit.getType() != HitResult.Type.BLOCK) {
            if (!level.isClientSide) {
                player.displayClientMessage(Component.translatable("message.blueeyes.aim_at_ground"), true);
            }
            return InteractionResultHolder.fail(stack);
        }
        if (level instanceof ServerLevel serverLevel) {
            Vec3 pos = fieldPosition(level, (BlockHitResult) hit);
            FieldCardEntity.summon(serverLevel, player, pos);
            if (!player.getAbilities().instabuild) {
                stack.shrink(1);
            }
            player.getCooldowns().addCooldown(this, 40);
        }
        return InteractionResultHolder.sidedSuccess(stack, level.isClientSide());
    }

    /** Punto del suelo donde queda la carta (boca arriba, pegada al terreno). */
    private static Vec3 fieldPosition(Level level, BlockHitResult hit) {
        if (hit.getDirection() == Direction.UP) {
            Vec3 loc = hit.getLocation();
            return new Vec3(loc.x, loc.y + 0.02D, loc.z);
        }
        // pared o techo: usar la celda libre contigua y dejarla caer hasta el suelo
        BlockPos cell = hit.getBlockPos().relative(hit.getDirection());
        int steps = 0;
        while (steps < 12 && cell.getY() > level.getMinBuildHeight()
                && level.getBlockState(cell.below()).getCollisionShape(level, cell.below()).isEmpty()) {
            cell = cell.below();
            steps++;
        }
        BlockPos below = cell.below();
        VoxelShape shape = level.getBlockState(below).getCollisionShape(level, below);
        double y = shape.isEmpty() ? cell.getY() : below.getY() + shape.max(Direction.Axis.Y);
        return new Vec3(cell.getX() + 0.5D, y + 0.02D, cell.getZ() + 0.5D);
    }

    @Override
    public void appendHoverText(ItemStack stack, Item.TooltipContext context, List<Component> tips, TooltipFlag flag) {
        super.appendHoverText(stack, context, tips, flag);
        tips.add(Component.literal("[Dragon / Normal]  Nivel 8  LUZ").withStyle(ChatFormatting.GOLD));
        tips.add(Component.literal("ATK 3000 / DEF 2500").withStyle(ChatFormatting.YELLOW));
        tips.add(Component.literal("Este legendario dragon es una poderosa maquina de destruccion.").withStyle(ChatFormatting.GRAY));
        tips.add(Component.literal("Clic derecho sobre el campo: invoca al dragon.").withStyle(ChatFormatting.AQUA));
        tips.add(Component.literal("Ataca a tus enemigos con White Lightning.").withStyle(ChatFormatting.AQUA));
        tips.add(Component.literal("Clic der. a la carta del campo: desactivar / activar.").withStyle(ChatFormatting.DARK_AQUA));
        tips.add(Component.literal("Agachado + clic der. al dragon, o di \"desactivar\": vuelve a la carta.").withStyle(ChatFormatting.DARK_AQUA));
        tips.add(Component.literal("Agachado + clic der. a la carta: recogerla.").withStyle(ChatFormatting.DARK_AQUA));
    }
}
