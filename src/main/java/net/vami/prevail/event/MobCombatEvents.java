package net.vami.prevail.event;

import it.unimi.dsi.fastutil.objects.ObjectLinkedOpenHashSet;
import net.minecraft.core.particles.ParticleTypes;
import net.minecraft.server.level.ServerLevel;
import net.minecraft.tags.DamageTypeTags;
import net.minecraft.world.damagesource.DamageSource;
import net.minecraft.world.entity.LivingEntity;
import net.minecraft.world.entity.Mob;
import net.minecraft.world.entity.ai.attributes.Attributes;
import net.minecraft.world.entity.ai.goal.Goal;
import net.minecraft.world.entity.ai.goal.GoalSelector;
import net.minecraft.world.entity.ai.goal.WrappedGoal;
import net.minecraft.world.entity.monster.Creeper;
import net.minecraft.world.entity.player.Player;
import net.neoforged.bus.api.EventPriority;
import net.neoforged.bus.api.SubscribeEvent;
import net.neoforged.fml.common.EventBusSubscriber;
import net.neoforged.neoforge.event.entity.EntityJoinLevelEvent;
import net.neoforged.neoforge.event.entity.living.LivingChangeTargetEvent;
import net.neoforged.neoforge.event.entity.living.LivingDamageEvent;
import net.neoforged.neoforge.event.entity.living.LivingIncomingDamageEvent;
import net.neoforged.neoforge.event.tick.EntityTickEvent;
import net.vami.prevail.ModTags;
import net.vami.prevail.Prevail;
import net.vami.prevail.attachment.MobData;
import net.vami.prevail.util.DataUtil;
import org.spongepowered.asm.mixin.injection.At;
import org.spongepowered.asm.mixin.injection.ModifyVariable;

import java.util.*;

@EventBusSubscriber(modid = Prevail.MOD_ID)
public class MobCombatEvents {

    // instead of LivingAttackEvent (forge 1.20.1)
    @SubscribeEvent(priority = EventPriority.LOWEST)
    public static void iFrameRemoval(LivingIncomingDamageEvent event) {
        if (event.getEntity().level().isClientSide()) return;

        if (event.getSource().is(ModTags.DamageTypes.MELEE)
                || event.getSource().is(DamageTypeTags.IS_PROJECTILE)) {

            event.getEntity().invulnerableTime = 0;
            event.setInvulnerabilityTicks(0);
            event.getContainer().setPostAttackInvulnerabilityTicks(0);
        }
    }

    // renaming "capability" to "data" for convenience's sake
    @SubscribeEvent(priority = EventPriority.LOWEST)
    public static void onEntityAdded(EntityJoinLevelEvent event) {
        if (!(event.getEntity() instanceof LivingEntity entity)) return;
        if (entity.level().isClientSide()) return;

        if (entity instanceof Player) return;

        MobData data = DataUtil.getData(entity);

        resetPoise(data, entity);
    }

    // instead of LivingAttackEvent (forge 1.20.1)
    // renaming "capability" to "data" for convenience's sake
    @SubscribeEvent
    public static void staggerOnHit(LivingDamageEvent.Pre event) {
        if (event.getEntity().level().isClientSide()) return;

        if (!(event.getSource().is(ModTags.DamageTypes.MELEE)
                || event.getSource().is(DamageTypeTags.IS_PROJECTILE))) return;

        LivingEntity target = event.getEntity();
        if (target instanceof Player) return;

        MobData data = DataUtil.getData(target);

        if (data.getStagger() > 0) return;

        data.setPoise(data.getPoise() - event.getOriginalDamage());

        if (data.getPoise() <= 0) {
            resetPoise(data, target);
            data.setStagger(50);
        }
    }

    // instead of LivingAttackEvent (forge 1.20.1)
    // LivingIncomingDamageEvent is before the attack is considered successful
    // renaming "capability" to "data" for convenience's sake
    @SubscribeEvent
    public static void preventStaggeredAttack(LivingIncomingDamageEvent event) {
        if (!(event.getSource().getEntity() instanceof LivingEntity source)) return;

        MobData data = DataUtil.getData(source);

        if (data.getStagger() > 0) {
            event.setCanceled(true);
        }
    }

    // instead of LivingEvent.LivingTickEvent
    // renaming "capability" to "data" for convenience's sake
    @SubscribeEvent
    public static void onStaggerTick(EntityTickEvent.Pre event) {
        if (!(event.getEntity() instanceof LivingEntity entity)) return;
        if (!(entity instanceof Mob mob)) return;
        if (!mob.isAlive()) return;

        MobData data = DataUtil.getData(mob);

        if (data.getStagger() > 0) {
            disableAI(mob);

            mob.xxa = 0;
            mob.zza = 0;

            if (mob.level() instanceof ServerLevel serverLevel) {
                serverLevel.sendParticles(ParticleTypes.CRIT,
                        (mob.getX()), (mob.getY() + mob.getBbHeight() / 2), (mob.getZ()),
                        data.getStagger() / 6,
                        (mob.getBbWidth() / 3), (mob.getBbHeight() / 3), (mob.getBbWidth() / 3),
                        0.05);
            }

            data.setStagger(data.getStagger() - 1);

            if (data.getStagger() <= 0) {
                enableAI(mob);
            }
        }
    }

    // renaming "capability" to "data" for convenience's sake
    @SubscribeEvent
    public static void onTarget(LivingChangeTargetEvent event) {
        LivingEntity source = event.getEntity();

        MobData data = DataUtil.getData(source);

        if (data.getStagger() > 0) {
            event.setCanceled(true);
        }
    }

    // renaming "capability" to "data" for convenience's sake
    private static void resetPoise(MobData data, LivingEntity entity) {
        data.setPoise(entity.getMaxHealth() / 1.75f);
    }

    public static void disableAI(Mob mob) {
        mob.getNavigation().stop();

        mob.goalSelector.disableControlFlag(Goal.Flag.MOVE);
        mob.goalSelector.disableControlFlag(Goal.Flag.LOOK);
        mob.goalSelector.disableControlFlag(Goal.Flag.JUMP);
        mob.goalSelector.disableControlFlag(Goal.Flag.TARGET);

        mob.targetSelector.disableControlFlag(Goal.Flag.MOVE);
        mob.targetSelector.disableControlFlag(Goal.Flag.LOOK);
        mob.targetSelector.disableControlFlag(Goal.Flag.JUMP);
        mob.targetSelector.disableControlFlag(Goal.Flag.TARGET);

        if (mob instanceof Creeper creeper) {
            creeper.setSwellDir(-1);
        }
    }

    public static void enableAI(Mob mob) {
        mob.goalSelector.enableControlFlag(Goal.Flag.MOVE);
        mob.goalSelector.enableControlFlag(Goal.Flag.LOOK);
        mob.goalSelector.enableControlFlag(Goal.Flag.JUMP);
        mob.goalSelector.enableControlFlag(Goal.Flag.TARGET);

        mob.targetSelector.enableControlFlag(Goal.Flag.MOVE);
        mob.targetSelector.enableControlFlag(Goal.Flag.LOOK);
        mob.targetSelector.enableControlFlag(Goal.Flag.JUMP);
        mob.targetSelector.enableControlFlag(Goal.Flag.TARGET);
    }

}
