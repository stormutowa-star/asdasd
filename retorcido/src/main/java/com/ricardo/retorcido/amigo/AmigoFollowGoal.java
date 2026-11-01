package com.ricardo.retorcido.amigo;

import net.minecraft.server.level.ServerLevel;
import net.minecraft.server.level.ServerPlayer;
import net.minecraft.world.entity.ai.goal.Goal;
import net.minecraft.world.phys.Vec3;

import java.util.EnumSet;
import java.util.Set;

/** Sigue al dueno: se acerca a >5, sprint a >10, quieto a <3, teletransporte a >24 o en otra dimension. */
public class AmigoFollowGoal extends Goal {
    private final AmigoEntity mob;
    private ServerPlayer owner;
    private int recalc;

    public AmigoFollowGoal(AmigoEntity mob) {
        this.mob = mob;
        this.setFlags(EnumSet.of(Flag.MOVE));
    }

    @Override
    public boolean canUse() {
        if (mob.isScripted() || mob.getStareTicks() > 0) return false;
        owner = mob.getOwner();
        return owner != null && owner.isAlive() && !owner.isSpectator();
    }

    @Override
    public boolean canContinueToUse() {
        return canUse();
    }

    @Override
    public void start() {
        recalc = 0;
    }

    @Override
    public void stop() {
        mob.getNavigation().stop();
    }

    @Override
    public void tick() {
        if (owner == null) return;
        boolean sameLevel = owner.level() == mob.level();
        double dist = sameLevel ? mob.distanceTo(owner) : Double.MAX_VALUE;

        if (!sameLevel || dist > 24.0D) {
            if (owner.onGround() && owner.level() instanceof ServerLevel target) {
                Vec3 pos = AmigoManager.findStandPos(target, owner.position(), mob.getType().getDimensions(), 3);
                mob.getNavigation().stop();
                mob.teleportTo(target, pos.x, pos.y, pos.z, Set.of(), owner.getYRot(), 0.0F);
            }
            return;
        }

        mob.getLookControl().setLookAt(owner, 10.0F, mob.getMaxHeadXRot());
        if (dist < 3.0D) {
            mob.getNavigation().stop();
        } else if (dist > 5.0D && --recalc <= 0) {
            recalc = 10;
            mob.getNavigation().moveTo(owner, dist > 10.0D ? 1.4D : 1.0D);
        }
    }
}
