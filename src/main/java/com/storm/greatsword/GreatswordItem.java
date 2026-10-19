package com.storm.greatsword;

import net.minecraft.ChatFormatting;
import net.minecraft.core.component.DataComponents;
import net.minecraft.network.chat.Component;
import net.minecraft.server.level.ServerLevel;
import net.minecraft.server.level.ServerPlayer;
import net.minecraft.sounds.SoundEvents;
import net.minecraft.sounds.SoundSource;
import net.minecraft.world.InteractionHand;
import net.minecraft.world.InteractionResultHolder;
import net.minecraft.world.entity.LivingEntity;
import net.minecraft.world.entity.player.Player;
import net.minecraft.world.item.Item;
import net.minecraft.world.item.ItemStack;
import net.minecraft.world.item.SwordItem;
import net.minecraft.world.item.Tier;
import net.minecraft.world.item.TooltipFlag;
import net.minecraft.world.item.UseAnim;
import net.minecraft.world.item.component.ItemAttributeModifiers;
import net.minecraft.world.level.Level;
import software.bernie.geckolib.animatable.GeoItem;
import software.bernie.geckolib.animatable.SingletonGeoAnimatable;
import software.bernie.geckolib.animatable.client.GeoRenderProvider;
import software.bernie.geckolib.animatable.instance.AnimatableInstanceCache;
import software.bernie.geckolib.animation.AnimatableManager;
import software.bernie.geckolib.animation.AnimationController;
import software.bernie.geckolib.animation.AnimationState;
import software.bernie.geckolib.animation.PlayState;
import software.bernie.geckolib.animation.RawAnimation;
import software.bernie.geckolib.constant.DataTickets;
import software.bernie.geckolib.util.GeckoLibUtil;

import java.util.List;
import java.util.function.Consumer;

/**
 * The greatsword item. All of the actual combat logic lives in {@link GreatswordActions} so that it can be
 * shared between the item's use/release callbacks and the action packet.
 */
public class GreatswordItem extends SwordItem implements GeoItem {
    public static final String CONTROLLER = "main";

    // These names must match the keys inside greatsword.animation.json. Blockbench exports animation
    // identifiers with the "animation.<model>." prefix, and GeckoLib looks them up verbatim.
    private static final String ANIMATION_PREFIX = "animation.greatsword.";
    private static final RawAnimation IDLE = RawAnimation.begin().thenLoop(ANIMATION_PREFIX + "idle");
    private static final RawAnimation IDLE_SHEATHED = RawAnimation.begin().thenLoop(ANIMATION_PREFIX + "idle_sheathed");
    private static final RawAnimation DRAW = RawAnimation.begin().thenPlay(ANIMATION_PREFIX + "draw");
    private static final RawAnimation SHEATHE = RawAnimation.begin().thenPlay(ANIMATION_PREFIX + "sheathe");
    private static final RawAnimation SWING_1 = RawAnimation.begin().thenPlay(ANIMATION_PREFIX + "swing1");
    private static final RawAnimation SWING_2 = RawAnimation.begin().thenPlay(ANIMATION_PREFIX + "swing2");
    private static final RawAnimation SWING_3 = RawAnimation.begin().thenPlay(ANIMATION_PREFIX + "swing3");
    private static final RawAnimation CHARGE = RawAnimation.begin().thenPlay(ANIMATION_PREFIX + "charge");
    private static final RawAnimation SLAM = RawAnimation.begin().thenPlay(ANIMATION_PREFIX + "slam");
    private static final RawAnimation TACKLE = RawAnimation.begin().thenPlay(ANIMATION_PREFIX + "tackle");

    private final AnimatableInstanceCache cache = GeckoLibUtil.createInstanceCache(this);

    public GreatswordItem(Tier tier, Properties properties) {
        super(tier, properties);

        // Required for server-triggered animations on a singleton animatable (items).
        SingletonGeoAnimatable.registerSyncedAnimatable(this);
    }

    public static boolean isDrawn(ItemStack stack) {
        return Boolean.TRUE.equals(stack.get(GreatswordMod.DRAWN.get()));
    }

    public static void setDrawn(ItemStack stack, boolean drawn) {
        stack.set(GreatswordMod.DRAWN.get(), drawn);
        stack.set(DataComponents.ATTRIBUTE_MODIFIERS, createAttributes(drawn));
    }

    /**
     * Sheathed: barely useful as a weapon. Drawn: a slow, extremely heavy two-handed blade.
     */
    public static ItemAttributeModifiers createAttributes(boolean drawn) {
        return SwordItem.createAttributes(
                GreatswordTier.INSTANCE,
                drawn ? 7 : -6,
                drawn ? -3.2F : -2.2F
        );
    }

    @Override
    public void createGeoRenderer(Consumer<GeoRenderProvider> consumer) {
        consumer.accept(new GeoRenderProvider() {
            private GreatswordRenderer renderer;

            @Override
            public net.minecraft.client.renderer.BlockEntityWithoutLevelRenderer getGeoItemRenderer() {
                if (this.renderer == null) {
                    this.renderer = new GreatswordRenderer();
                }
                return this.renderer;
            }
        });
    }

    @Override
    public void registerControllers(AnimatableManager.ControllerRegistrar controllers) {
        controllers.add(new AnimationController<>(this, CONTROLLER, 2, this::animationPredicate)
                .triggerableAnim("draw", DRAW)
                .triggerableAnim("sheathe", SHEATHE)
                .triggerableAnim("swing1", SWING_1)
                .triggerableAnim("swing2", SWING_2)
                .triggerableAnim("swing3", SWING_3)
                .triggerableAnim("charge", CHARGE)
                .triggerableAnim("slam", SLAM)
                .triggerableAnim("tackle", TACKLE));
    }

    private PlayState animationPredicate(AnimationState<GreatswordItem> state) {
        ItemStack stack = state.getData(DataTickets.ITEMSTACK);

        if (stack != null && !isDrawn(stack)) {
            return state.setAndContinue(IDLE_SHEATHED);
        }
        return state.setAndContinue(IDLE);
    }

    @Override
    public AnimatableInstanceCache getAnimatableInstanceCache() {
        return this.cache;
    }

    // ---------------------------------------------------------------------
    // Right click: draw when sheathed, charge a heavy slam when drawn.
    // ---------------------------------------------------------------------

    @Override
    public InteractionResultHolder<ItemStack> use(Level level, Player player, InteractionHand hand) {
        ItemStack stack = player.getItemInHand(hand);

        if (hand != InteractionHand.MAIN_HAND) {
            return InteractionResultHolder.pass(stack);
        }

        if (!isDrawn(stack)) {
            if (!level.isClientSide && player instanceof ServerPlayer serverPlayer) {
                GreatswordActions.toggleDraw(serverPlayer);
            }
            return InteractionResultHolder.sidedSuccess(stack, level.isClientSide);
        }

        if (!level.isClientSide && player instanceof ServerPlayer serverPlayer) {
            GreatswordActions.beginCharge(serverPlayer, stack);
        }
        return InteractionResultHolder.consume(stack);
    }

    @Override
    public int getUseDuration(ItemStack stack, LivingEntity entity) {
        return 72000;
    }

    @Override
    public UseAnim getUseAnimation(ItemStack stack) {
        // BOW gives a natural both-arms-forward holding pose while charging.
        return UseAnim.BOW;
    }

    @Override
    public void onUseTick(Level level, LivingEntity living, ItemStack stack, int remainingUseDuration) {
        if (!(living instanceof ServerPlayer player) || level.isClientSide) {
            return;
        }
        if (!isDrawn(stack)) {
            return;
        }

        int elapsed = getUseDuration(stack, player) - remainingUseDuration;

        if (elapsed == 30) {
            level.playSound(null, player.blockPosition(), SoundEvents.AMETHYST_BLOCK_RESONATE,
                    SoundSource.PLAYERS, 0.8F, 0.8F);
            player.displayClientMessage(Component.translatable("msg.greatsword.charged"), true);
        }

        if (elapsed > 10 && elapsed % 4 == 0) {
            ServerLevel serverLevel = (ServerLevel) level;
            var look = player.getLookAngle();
            double x = player.getX() + look.x * 1.35;
            double y = player.getY() + 1.55;
            double z = player.getZ() + look.z * 1.35;
            serverLevel.sendParticles(net.minecraft.core.particles.ParticleTypes.ENCHANT,
                    x, y, z, 4, 0.18, 0.25, 0.18, 0.015);
            serverLevel.sendParticles(net.minecraft.core.particles.ParticleTypes.CRIT,
                    x, y, z, 2, 0.12, 0.2, 0.12, 0.02);
        }
    }

    @Override
    public void releaseUsing(ItemStack stack, Level level, LivingEntity living, int timeLeft) {
        if (level.isClientSide || !(living instanceof ServerPlayer player)) {
            return;
        }

        int chargeTicks = getUseDuration(stack, player) - timeLeft;
        ItemStack mainHand = player.getMainHandItem();
        if (chargeTicks >= 8 && mainHand.getItem() instanceof GreatswordItem && isDrawn(mainHand)) {
            GreatswordActions.chargedSlam(player, mainHand, chargeTicks);
        }
    }

    // ---------------------------------------------------------------------
    // Tooltip
    // ---------------------------------------------------------------------

    @Override
    public void appendHoverText(ItemStack stack, Item.TooltipContext context, List<Component> tooltip, TooltipFlag flag) {
        super.appendHoverText(stack, context, tooltip, flag);

        tooltip.add(Component.translatable("tooltip.greatsword.two_handed").withStyle(ChatFormatting.GOLD));
        tooltip.add(Component.translatable(isDrawn(stack)
                        ? "tooltip.greatsword.state.drawn"
                        : "tooltip.greatsword.state.sheathed")
                .withStyle(ChatFormatting.GRAY));
        tooltip.add(Component.translatable("tooltip.greatsword.controls").withStyle(ChatFormatting.DARK_GRAY));
        tooltip.add(Component.translatable("tooltip.greatsword.controls.charge").withStyle(ChatFormatting.DARK_GRAY));
        tooltip.add(Component.translatable("tooltip.greatsword.controls.tackle").withStyle(ChatFormatting.DARK_GRAY));
    }
}
