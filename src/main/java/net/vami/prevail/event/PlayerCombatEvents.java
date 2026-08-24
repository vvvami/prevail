package net.vami.prevail.event;

import net.minecraft.core.registries.Registries;
import net.minecraft.server.level.ServerLevel;
import net.minecraft.sounds.SoundEvents;
import net.minecraft.sounds.SoundSource;
import net.minecraft.world.InteractionHand;
import net.minecraft.world.damagesource.DamageSource;
import net.minecraft.world.damagesource.DamageTypes;
import net.minecraft.world.entity.Entity;
import net.minecraft.world.entity.LivingEntity;
import net.minecraft.world.entity.ai.attributes.Attributes;
import net.minecraft.world.entity.player.Player;
import net.minecraft.world.item.Item;
import net.minecraft.world.item.ItemStack;
import net.minecraft.world.item.Items;
import net.minecraft.world.phys.AABB;
import net.minecraft.world.phys.Vec3;
import net.minecraftforge.common.ForgeMod;
import net.minecraftforge.event.TickEvent;
import net.minecraftforge.event.entity.living.*;
import net.minecraftforge.event.entity.player.AttackEntityEvent;
import net.minecraftforge.event.entity.player.PlayerSleepInBedEvent;
import net.minecraftforge.event.entity.player.PlayerWakeUpEvent;
import net.minecraftforge.event.entity.player.SleepingTimeCheckEvent;
import net.minecraftforge.event.level.SleepFinishedTimeEvent;
import net.minecraftforge.eventbus.api.Event;
import net.minecraftforge.eventbus.api.EventPriority;
import net.minecraftforge.eventbus.api.SubscribeEvent;
import net.minecraftforge.fml.common.Mod;
import net.minecraftforge.registries.ForgeRegistries;
import net.vami.prevail.ModTags;
import net.vami.prevail.Prevail;
import net.vami.prevail.capability.MobCapability;
import net.vami.prevail.util.CapUtil;
import net.vami.prevail.util.CombatUtil;

import java.util.List;

@Mod.EventBusSubscriber(modid = Prevail.MOD_ID)
public class PlayerCombatEvents {

    // this godforsaken shxtfest of a method has my head spinning
    // and my neck cranked beyond repair. i just manually tweaked it till it worked.
    // please dont make me change it.
    // moved from Mixin to event!
    @SubscribeEvent(priority = EventPriority.HIGHEST)
    public static void distanceDmg(LivingHurtEvent event) {
        DamageSource source = event.getSource();
        double distanceMultiplier = 2;
        float originalAmount = event.getAmount();
        float result;
        if (source.getEntity() == null) return;

        if (!(source.getEntity() instanceof Player sourcePlayer)
                || !source.is(ModTags.DamageTypes.MELEE)) return;

        LivingEntity target = event.getEntity();

        double dist = sourcePlayer.position().distanceTo(target.position());
        double reachCalc = Math.max(0.1, dist) / Math.max(0.1, sourcePlayer.getAttributeValue(ForgeMod.ENTITY_REACH.get()));
        // so basically if the player is more than a portion of the dist away
        // the damage will quickly fall off, n it goes moot (i watched suits once)
        if (reachCalc <= 0.65) {
            result = (float) (originalAmount * (1 + (distanceMultiplier - 1) * (0.75 - (reachCalc))));
        } else {
            result = (float) (originalAmount / (1 + (distanceMultiplier - 1) * (3 * reachCalc - 1)));
        }
        event.setAmount(result);
    }

    // allows sleep all times of day
    @SubscribeEvent
    public static void allowSleep(SleepingTimeCheckEvent event) {
        event.setResult(Event.Result.ALLOW);
    }

    // enforces +12000-13000 cycle on wake-up, even during daysleep
    @SubscribeEvent
    public static void onSleepFinished(SleepFinishedTimeEvent event) {
        long day = event.getLevel().dayTime();

        long time = day % 24000;

        if (time < 12000) {
            long night = day - time + 13000;

            event.setTimeAddition(night);
        }
    }

    // restores up to 80% of maxheal after sleep
    @SubscribeEvent
    public static void onPlayerSlept(PlayerWakeUpEvent event) {
        Player player = event.getEntity();

        MobCapability capability = CapUtil.getCap(player);
        if (capability == null) return;

        if (capability.getMaxHeal() < player.getMaxHealth() * 0.8f) {
            capability.setMaxHeal(player.getMaxHealth() * 0.8f);
        }
    }

    // despair exists so you dont spam attack.
    // despair is reduced automatically with time
    @SubscribeEvent(priority = EventPriority.HIGHEST)
    public static void despairStack(AttackEntityEvent event) {
        if (event.getEntity().level().isClientSide()) return;

        Player player = event.getEntity();
        MobCapability capability = CapUtil.getCap(player);
        if (capability == null) return;

        if (!CombatUtil.isSweep(player)) {
            // make configurable
            // shield increases despair, because fxck shield abusers
            int amount = player.getItemInHand(InteractionHand.OFF_HAND).getItem()
                    == Items.SHIELD ? 2 : 1;

            capability.setDespair(capability.getDespair() + amount);
        } else {
            // if ur sweeping, it means u relaxed
            // good job, have less despair
            capability.setDespair(Math.max(0, capability.getDespair() - 2));
        }
    }

    @SubscribeEvent
    public static void despairEffect(LivingAttackEvent event) {
        if (event.getEntity().level().isClientSide()) return;
        if (!CombatUtil.isMelee(event)) return;

        Entity source = event.getSource().getEntity();
        if (source instanceof Player player) {
            MobCapability capability = CapUtil.getCap(player);
            if (capability == null) return;

            // needa make it configurable
            int despairThreshold = 7;
            if (capability.getDespair() > despairThreshold) {
                event.setCanceled(true); // you cannot spam. be calm. be relaxed.
            }
        }
    }

    // despair fades away slowly
    // i made it once per second for performance reasons
    // tho honestly might be unnecessary lol
    @SubscribeEvent
    public static void despairFade(TickEvent.PlayerTickEvent event) {
        if (event.player.level().isClientSide()) return;
        if (event.player.tickCount % 20 == 0) {
            MobCapability capability = CapUtil.getCap(event.player);
            if (capability == null) return;

            capability.setDespair(Math.max(0, capability.getDespair() - 1));
        }
    }

    // the only release to despair is death
    @SubscribeEvent
    public static void resetDespair(LivingDeathEvent  event) {
        if (event.getEntity().level().isClientSide()) return;
        if (event.getEntity() instanceof Player player) {
        MobCapability capability = CapUtil.getCap(player);
        if (capability == null) return;

        capability.setDespair(0);
        }
    }

    // when your shield is attacked, you cant use it for a brief period
    // cooldown depends on the damage blocked
    @SubscribeEvent(priority = EventPriority.LOWEST)
    public static void shieldCooldownOnHit(ShieldBlockEvent event) {
        if (event.getEntity().level().isClientSide()) return;
        LivingEntity entity = event.getEntity();
        ItemStack stack = entity.getItemInHand(entity.getUsedItemHand());
        if (event.getBlockedDamage() < 1) return;

        if (entity instanceof Player player) {
            player.getCooldowns().addCooldown(stack.getItem(),
                    (int) event.getOriginalBlockedDamage());
            player.stopUsingItem();
            stack.hurtAndBreak((int) event.getBlockedDamage(), player, (entity1) -> {});
        }
    }

    // if you cancel ur chomp it gotta account for that in the playerCap
    @SubscribeEvent
    public static void chompCancel(LivingEntityUseItemEvent.Stop event) {
        if (!(event.getEntity() instanceof Player player)) return;
        MobCapability capability = CapUtil.getCap(player);
        if (capability == null) return;
        if (capability.hasChomped()) {
            capability.setChomped(false);
        }
    }

    // u cant chomp without consequences. eat slower next time dumbxss
    @SubscribeEvent
    public static void chompCooldown(LivingEntityUseItemEvent.Finish event) {
        if (!(event.getEntity() instanceof Player player)) return;

        MobCapability capability = CapUtil.getCap(player);
        if (capability == null) return;

        if (capability.hasChomped()) {
            for (Item item : ForgeRegistries.ITEMS.getValues()) {
                if (item.isEdible()) {
                    player.getCooldowns().addCooldown(item, 600);
                }
            }
            capability.setChomped(false);
        }
    }


    // si tienes que correr, hazlo!
    @SubscribeEvent
    public static void shieldRush(TickEvent.PlayerTickEvent event) {
        if (event.player.level() instanceof ServerLevel server) {
            Player player = event.player;

            MobCapability capability = CapUtil.getCap(player);
            if (capability == null) return;

            if (player.isBlocking()
                    && player.isSprinting()
                    && !player.isCrouching()
                    && player.getFoodData().getFoodLevel() > 6
                    && !player.isInWater()) {
                if (capability.getShieldRush() >= 10) {

                    capability.setShieldRush(0);

                    server.playSound(null, player.getX(), player.getY() + 1, player.getZ(),
                            SoundEvents.SHIELD_BREAK, SoundSource.PLAYERS, 1.0F, 0.25F);

                    player.getCooldowns().addCooldown(player.getItemInHand(player.getUsedItemHand()).getItem(),
                            30);

                    player.stopUsingItem();
                    return;
                }

                player.setDeltaMovement(new Vec3(
                        player.getLookAngle().x,
                        player.getDeltaMovement().y,
                        player.getLookAngle().z));
                player.hurtMarked = true;

                player.causeFoodExhaustion(0.1f);

                AABB playerBox = player.getBoundingBox().inflate(1.5, 1, 1.5);
                playerBox.inflate(player.getLookAngle().x, 0, player.getLookAngle().z);

                List<Entity> candidates = player.level().getEntities(
                        player,
                        playerBox,
                        e -> e instanceof LivingEntity
                                && e.isAlive()
                                && e != player
                                && !e.isSpectator()
                                && e.isPickable());

                for (Entity e : candidates) {
                    e.hurt(new DamageSource(server.registryAccess()
                            .registryOrThrow(Registries.DAMAGE_TYPE)
                            .getHolderOrThrow(DamageTypes.PLAYER_ATTACK), player), 1f);

                    e.setDeltaMovement(new Vec3(
                            (e.getX() - player.getX()),
                            (e.getY() - player.getY()),
                            (e.getZ() - player.getZ())));
                    e.hurtMarked = true;
                }

                capability.setShieldRush(capability.getShieldRush() + 1);

            } else if (capability.getShieldRush() > 0) {
                player.getCooldowns().addCooldown(player.getItemInHand(player.getUsedItemHand()).getItem(), capability.getShieldRush() * 3);

                server.playSound(null,
                        player.getX(), player.getY() + 1, player.getZ(),
                        SoundEvents.SHIELD_BREAK, SoundSource.PLAYERS,
                        0.3F, 1.25F);

                capability.setShieldRush(0);
            }
        }
    }
}
