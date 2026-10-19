package com.storm.greatsword;

import net.minecraft.core.BlockPos;
import net.minecraft.core.particles.BlockParticleOption;
import net.minecraft.core.particles.ParticleTypes;
import net.minecraft.network.chat.Component;
import net.minecraft.server.level.ServerLevel;
import net.minecraft.server.level.ServerPlayer;
import net.minecraft.sounds.SoundEvents;
import net.minecraft.sounds.SoundSource;
import net.minecraft.world.InteractionHand;
import net.minecraft.world.damagesource.DamageSource;
import net.minecraft.world.entity.EquipmentSlot;
import net.minecraft.world.entity.LivingEntity;
import net.minecraft.world.entity.ai.attributes.Attributes;
import net.minecraft.world.item.ItemStack;
import net.minecraft.world.item.enchantment.EnchantmentHelper;
import net.minecraft.world.level.block.state.BlockState;
import net.minecraft.world.phys.Vec3;
import software.bernie.geckolib.animatable.GeoItem;

import java.util.HashMap;
import java.util.Map;
import java.util.UUID;

/**
 * Server-side combat logic for the greatsword. Keeping it here means the item callbacks and the network
 * packet share exactly the same rules.
 */
public final class GreatswordActions {
    private static final int COMBO_WINDOW_TICKS = 36;
    private static final int SWING_COOLDOWN_TICKS = 14;
    private static final int SLAM_COOLDOWN_TICKS = 50;
    private static final int TACKLE_COOLDOWN_TICKS = 34;

    private static final Map<UUID, CombatState> STATES = new HashMap<>();

    private GreatswordActions() {}

    private static final class CombatState {
        int combo;
        long lastSwing;
        long lastSlam;
        long lastTackle;
    }

    private static CombatState state(ServerPlayer player) {
        return STATES.computeIfAbsent(player.getUUID(), ignored -> new CombatState());
    }

    public static void clear(ServerPlayer player) {
        STATES.remove(player.getUUID());
    }

    private static ItemStack heldGreatsword(ServerPlayer player) {
        ItemStack stack = player.getMainHandItem();
        return stack.getItem() instanceof GreatswordItem ? stack : ItemStack.EMPTY;
    }

    // ------------------------------------------------------------------
    // Draw / sheathe
    // ------------------------------------------------------------------

    public static void toggleDraw(ServerPlayer player) {
        ItemStack stack = heldGreatsword(player);
        if (stack.isEmpty()) {
            player.displayClientMessage(Component.translatable("msg.greatsword.not_holding"), true);
            return;
        }

        boolean drawn = GreatswordItem.isDrawn(stack);

        if (!drawn && !player.getOffhandItem().isEmpty()) {
            player.displayClientMessage(Component.translatable("msg.greatsword.need_free_offhand"), true);
            return;
        }

        // Always cancel an in-progress charge when the state changes.
        player.stopUsingItem();
        GreatswordItem.setDrawn(stack, !drawn);

        if (stack.getItem() instanceof GreatswordItem sword) {
            long id = GeoItem.getOrAssignId(stack, player.serverLevel());
            sword.triggerAnim(player, id, GreatswordItem.CONTROLLER, drawn ? "sheathe" : "draw");
        }

        player.level().playSound(null, player.blockPosition(),
                SoundEvents.ARMOR_EQUIP_IRON.value(), SoundSource.PLAYERS,
                1.0F, drawn ? 0.55F : 0.75F);

        if (!drawn) {
            player.level().playSound(null, player.blockPosition(),
                    SoundEvents.MACE_SMASH_AIR, SoundSource.PLAYERS, 0.55F, 0.85F);
        }

        player.displayClientMessage(Component.translatable(
                drawn ? "msg.greatsword.sheathed" : "msg.greatsword.drawn"), true);

        // Attribute components changed, so resend the inventory data to the owner.
        player.inventoryMenu.sendAllDataToRemote();
    }

    public static void beginCharge(ServerPlayer player, ItemStack stack) {
        if (!GreatswordItem.isDrawn(stack)) {
            return;
        }

        if (stack.getItem() instanceof GreatswordItem sword) {
            long id = GeoItem.getOrAssignId(stack, player.serverLevel());
            sword.triggerAnim(player, id, GreatswordItem.CONTROLLER, "charge");
        }

        player.level().playSound(null, player.blockPosition(),
                SoundEvents.AMETHYST_BLOCK_CHIME, SoundSource.PLAYERS, 0.6F, 0.65F);
    }

    // ------------------------------------------------------------------
    // Combo attacks
    // ------------------------------------------------------------------

    public static void attack(ServerPlayer player) {
        ItemStack stack = heldGreatsword(player);
        if (stack.isEmpty() || !GreatswordItem.isDrawn(stack)) {
            return;
        }
        if (player.getCooldowns().isOnCooldown(stack.getItem())) {
            return;
        }

        CombatState state = state(player);
        long now = player.level().getGameTime();

        if (now - state.lastSwing > COMBO_WINDOW_TICKS) {
            state.combo = 0;
        } else {
            state.combo = (state.combo + 1) % 3;
        }
        state.lastSwing = now;

        int combo = state.combo;
        float multiplier = switch (combo) {
            case 0 -> 0.8F;
            case 1 -> 1.0F;
            default -> 1.4F;
        };
        // Use the player's real attack damage so Strength and other modifiers carry over. Enchantment
        // damage (Sharpness/Smite) is added per target inside damageCone.
        float damage = (float) player.getAttributeValue(Attributes.ATTACK_DAMAGE) * multiplier;

        player.stopUsingItem();
        player.getCooldowns().addCooldown(stack.getItem(), SWING_COOLDOWN_TICKS);
        player.swing(InteractionHand.MAIN_HAND, true);
        player.causeFoodExhaustion(0.15F);

        if (stack.getItem() instanceof GreatswordItem sword) {
            long id = GeoItem.getOrAssignId(stack, player.serverLevel());
            sword.triggerAnim(player, id, GreatswordItem.CONTROLLER, "swing" + (combo + 1));
        }

        if (combo == 2) {
            // The third hit is a spinning finisher.
            damageCone(player, stack, 3.5D, 180.0D, damage, 1.0D, 0.55D);
            player.level().playSound(null, player.blockPosition(),
                    SoundEvents.MACE_SMASH_GROUND_HEAVY, SoundSource.PLAYERS, 0.9F, 1.0F);
            slamParticles(player, 2.4D, 16);
            damageItem(player, stack, 2);
        } else {
            damageCone(player, stack, 3.4D, 70.0D, damage, 0.75D, 0.22D);
            player.level().playSound(null, player.blockPosition(),
                    SoundEvents.PLAYER_ATTACK_SWEEP, SoundSource.PLAYERS,
                    1.0F, combo == 0 ? 0.7F : 0.85F);
            player.level().playSound(null, player.blockPosition(),
                    SoundEvents.MACE_SMASH_AIR, SoundSource.PLAYERS, 0.35F, 1.25F);
            sweepParticles(player);
            damageItem(player, stack, 1);
        }
    }

    public static void chargedSlam(ServerPlayer player, ItemStack stack, int chargeTicks) {
        if (!GreatswordItem.isDrawn(stack) || player.getMainHandItem() != stack) {
            return;
        }
        if (player.getCooldowns().isOnCooldown(stack.getItem())) {
            return;
        }

        float charge = Math.min(1.0F, chargeTicks / 30.0F);
        float damage = (float) player.getAttributeValue(Attributes.ATTACK_DAMAGE)
                * (1.3F + 0.7F * charge);

        player.getCooldowns().addCooldown(stack.getItem(), SLAM_COOLDOWN_TICKS);
        player.swing(InteractionHand.MAIN_HAND, true);
        player.causeFoodExhaustion(0.5F);

        if (stack.getItem() instanceof GreatswordItem sword) {
            long id = GeoItem.getOrAssignId(stack, player.serverLevel());
            sword.triggerAnim(player, id, GreatswordItem.CONTROLLER, "slam");
        }

        damageCone(player, stack, 3.8D, 180.0D, damage, 1.15D, 0.65D);
        damageItem(player, stack, 2);

        player.level().playSound(null, player.blockPosition(),
                SoundEvents.MACE_SMASH_GROUND_HEAVY, SoundSource.PLAYERS, 1.0F, 0.85F);
        player.level().playSound(null, player.blockPosition(),
                SoundEvents.GENERIC_EXPLODE.value(), SoundSource.PLAYERS, 0.55F, 1.35F);

        slamParticles(player, 3.2D, 36);
    }

    public static void tackle(ServerPlayer player) {
        ItemStack stack = heldGreatsword(player);
        if (stack.isEmpty() || !GreatswordItem.isDrawn(stack)) {
            return;
        }
        if (player.getCooldowns().isOnCooldown(stack.getItem())) {
            return;
        }

        CombatState state = state(player);
        long now = player.level().getGameTime();
        if (now - state.lastTackle < TACKLE_COOLDOWN_TICKS) {
            return;
        }
        state.lastTackle = now;

        player.stopUsingItem();
        player.getCooldowns().addCooldown(stack.getItem(), TACKLE_COOLDOWN_TICKS);
        player.causeFoodExhaustion(0.35F);

        Vec3 look = player.getLookAngle();
        player.setDeltaMovement(player.getDeltaMovement().add(look.x * 0.85D, 0.18D, look.z * 0.85D));
        player.hurtMarked = true;

        if (stack.getItem() instanceof GreatswordItem sword) {
            long id = GeoItem.getOrAssignId(stack, player.serverLevel());
            sword.triggerAnim(player, id, GreatswordItem.CONTROLLER, "tackle");
        }

        float damage = (float) player.getAttributeValue(Attributes.ATTACK_DAMAGE) * 0.6F;
        damageCone(player, stack, 2.9D, 65.0D, damage, 0.9D, 0.3D);
        damageItem(player, stack, 1);

        player.level().playSound(null, player.blockPosition(),
                SoundEvents.RAVAGER_ATTACK, SoundSource.PLAYERS, 0.85F, 1.35F);
        player.level().playSound(null, player.blockPosition(),
                SoundEvents.MACE_SMASH_AIR, SoundSource.PLAYERS, 0.5F, 0.8F);

        ServerLevel level = player.serverLevel();
        Vec3 center = player.position().add(look.scale(1.2D)).add(0, 0.9D, 0);
        level.sendParticles(ParticleTypes.CRIT, center.x, center.y, center.z,
                12, 0.35, 0.35, 0.35, 0.08);
    }

    // ------------------------------------------------------------------
    // Helpers
    // ------------------------------------------------------------------

    private static void damageCone(ServerPlayer player, ItemStack weapon, double range,
                                   double halfAngleDegrees, float damage, double knockback, double lift) {
        ServerLevel level = player.serverLevel();
        DamageSource source = player.damageSources().playerAttack(player);
        Vec3 look = player.getLookAngle();
        Vec3 flatLook = new Vec3(look.x, 0, look.z).normalize();
        double cosLimit = Math.cos(Math.toRadians(halfAngleDegrees));

        for (LivingEntity target : level.getEntitiesOfClass(
                LivingEntity.class,
                player.getBoundingBox().inflate(range + 1.0D, 2.0D, range + 1.0D),
                entity -> entity != player && entity.isAlive() && !entity.isSpectator())) {

            Vec3 delta = target.position().subtract(player.position());
            double horizontal = delta.horizontalDistance();

            if (horizontal < 0.05D || horizontal > range) {
                continue;
            }

            Vec3 flatDelta = new Vec3(delta.x, 0, delta.z).normalize();
            if (flatDelta.dot(flatLook) < cosLimit) {
                continue;
            }

            float finalDamage = EnchantmentHelper.modifyDamage(level, weapon, target, source, damage);
            if (finalDamage <= 0.0F || !target.hurt(source, finalDamage)) {
                continue;
            }

            EnchantmentHelper.doPostAttackEffectsWithItemSource(level, target, source, weapon);

            float finalKnockback = EnchantmentHelper.modifyKnockback(
                    level, weapon, target, source, (float) knockback);
            double dx = delta.x / horizontal;
            double dz = delta.z / horizontal;
            target.push(dx * finalKnockback, lift, dz * finalKnockback);
            target.hurtMarked = true;

            level.sendParticles(ParticleTypes.SWEEP_ATTACK,
                    target.getX(), target.getY() + target.getBbHeight() * 0.6D, target.getZ(),
                    2, 0.15, 0.15, 0.15, 0.0);
            level.sendParticles(ParticleTypes.CRIT,
                    target.getX(), target.getY() + target.getBbHeight() * 0.5D, target.getZ(),
                    6, 0.25, 0.3, 0.25, 0.12);
        }
    }

    private static void damageItem(ServerPlayer player, ItemStack stack, int amount) {
        // The greatsword is a heavy weapon: each swing slowly wears it down. It can be repaired with
        // iron ingots like iron gear.
        stack.hurtAndBreak(amount, player, EquipmentSlot.MAINHAND);
    }

    private static void sweepParticles(ServerPlayer player) {
        ServerLevel level = player.serverLevel();
        Vec3 look = player.getLookAngle();
        Vec3 right = new Vec3(-look.z, 0, look.x).normalize();

        for (int i = -2; i <= 2; i++) {
            Vec3 point = player.position()
                    .add(look.scale(2.1D))
                    .add(right.scale(i * 0.55D))
                    .add(0, 1.15D, 0);
            level.sendParticles(ParticleTypes.SWEEP_ATTACK,
                    point.x, point.y, point.z, 1, 0, 0, 0, 0);
        }
    }

    private static void slamParticles(ServerPlayer player, double radius, int points) {
        ServerLevel level = player.serverLevel();
        BlockPos below = player.blockPosition().below();
        BlockState state = level.getBlockState(below);

        for (int i = 0; i < points; i++) {
            double angle = (Math.PI * 2.0D * i) / points;
            double x = player.getX() + Math.cos(angle) * radius;
            double z = player.getZ() + Math.sin(angle) * radius;
            level.sendParticles(ParticleTypes.SWEEP_ATTACK, x, player.getY() + 0.35D, z,
                    1, 0, 0, 0, 0);
            level.sendParticles(ParticleTypes.CRIT, x, player.getY() + 0.3D, z,
                    2, 0.1, 0.1, 0.1, 0.05);
        }

        if (!state.isAir()) {
            level.sendParticles(new BlockParticleOption(ParticleTypes.BLOCK, state),
                    player.getX(), player.getY() + 0.15D, player.getZ(),
                    points * 2, radius, 0.08D, radius, 0.18D);
        }

        level.sendParticles(ParticleTypes.EXPLOSION,
                player.getX(), player.getY() + 0.5D, player.getZ(), 1, 0, 0, 0, 0);
    }
}
