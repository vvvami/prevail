package net.vami.prevail.event;

import net.minecraft.core.particles.ParticleTypes;
import net.minecraft.server.level.ServerLevel;
import net.minecraft.tags.DamageTypeTags;
import net.minecraft.world.entity.Entity;
import net.minecraft.world.entity.LivingEntity;
import net.minecraft.world.entity.Mob;
import net.minecraft.world.entity.ai.goal.WrappedGoal;
import net.minecraft.world.entity.player.Player;
import net.minecraftforge.event.entity.EntityAttributeCreationEvent;
import net.minecraftforge.event.entity.EntityJoinLevelEvent;
import net.minecraftforge.event.entity.living.LivingAttackEvent;
import net.minecraftforge.event.entity.living.LivingChangeTargetEvent;
import net.minecraftforge.event.entity.living.LivingEvent;
import net.minecraftforge.eventbus.api.EventPriority;
import net.minecraftforge.eventbus.api.SubscribeEvent;
import net.minecraftforge.fml.common.Mod;
import net.vami.prevail.ModTags;
import net.vami.prevail.Prevail;
import net.vami.prevail.capability.MobCapability;
import net.vami.prevail.util.CapUtil;

@Mod.EventBusSubscriber(modid = Prevail.MOD_ID, bus = Mod.EventBusSubscriber.Bus.FORGE)
public class MobCombatEvents {

    // melee attacks and projectiles grant NO iframes
    @SubscribeEvent(priority = EventPriority.LOWEST)
    public static void iFrameRemoval(LivingAttackEvent event) {
        if (event.getEntity().level().isClientSide()) return;
        if (event.getSource().is(ModTags.DamageTypes.MELEE)
                || event.getSource().is(DamageTypeTags.IS_PROJECTILE)) {
            event.getEntity().invulnerableTime = 0;
        }
    }

    @SubscribeEvent(priority = EventPriority.LOWEST)
    public static void onEntityAdded(EntityJoinLevelEvent event) {
        if (!(event.getEntity() instanceof LivingEntity entity)) return;
        if (entity.level().isClientSide()) return;

        if (entity instanceof Player) return;

        MobCapability capability = CapUtil.getCap(entity);
        if (capability == null) return;

        resetPoise(capability, entity);
    }

    @SubscribeEvent
    public static void staggerOnHit(LivingAttackEvent event) {
        if (event.getEntity().level().isClientSide()) return;

        if (!(event.getSource().is(ModTags.DamageTypes.MELEE)
                || event.getSource().is(DamageTypeTags.IS_PROJECTILE))) return;

        LivingEntity target = event.getEntity();
        if (target instanceof Player) return;

        MobCapability capability = CapUtil.getCap(target);
        if (capability == null) return;

        if (capability.getStagger() > 0) return;

        capability.setPoise(capability.getPoise() - event.getAmount());

        if (capability.getPoise() <= 0) {
            resetPoise(capability, target);
            capability.setStagger(60);

        }
    }

    @SubscribeEvent
    public static void preventStaggeredAttack(LivingAttackEvent event) {
        if (!(event.getSource().getEntity() instanceof LivingEntity source)) return;

        MobCapability capability = CapUtil.getCap(source);
        if (capability == null) return;

        if (capability.getStagger() > 0) {
            event.setCanceled(true);
        }
    }

    @SubscribeEvent
    public static void onStaggerTick(LivingEvent.LivingTickEvent event) {
        LivingEntity entity = event.getEntity();
        if (!(entity instanceof Mob mob)) return;
        if (!mob.isAlive()) return;

        MobCapability capability = CapUtil.getCap(mob);
        if (capability == null) return;

        if (capability.getStagger() > 0) {
            mob.setNoAi(!mob.isNoAi());

            mob.getNavigation().stop();
            mob.goalSelector.getRunningGoals().forEach(WrappedGoal::stop);

            mob.xxa = 0;
            mob.zza = 0;

            if (mob.level() instanceof ServerLevel serverLevel) {
                serverLevel.sendParticles(ParticleTypes.CRIT,
                        (mob.getX()), (mob.getY() + mob.getBbHeight() / 2), (mob.getZ()),
                        5,
                        (mob.getBbWidth() / 3), (mob.getBbHeight() / 3), (mob.getBbWidth() / 3),
                        0.05);
            }

            mob.setNoAi(!mob.isNoAi());

            capability.setStagger(capability.getStagger() - 1);
        }
    }

    @SubscribeEvent
    public static void onTarget(LivingChangeTargetEvent event) {
        LivingEntity source = event.getEntity();

        MobCapability capability = CapUtil.getCap(source);
        if (capability == null) return;

        if (capability.getStagger() > 0) {
            event.setCanceled(true);
        }
    }

    private static void resetPoise(MobCapability capability, LivingEntity entity) {
        capability.setPoise(entity.getMaxHealth() / 2);
    }
}
