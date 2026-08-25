package net.vami.prevail.event;

import net.minecraft.core.registries.BuiltInRegistries;
import net.minecraft.core.registries.Registries;
import net.minecraft.server.level.ServerLevel;
import net.minecraft.sounds.SoundEvents;
import net.minecraft.sounds.SoundSource;
import net.minecraft.world.InteractionHand;
import net.minecraft.world.damagesource.DamageSource;
import net.minecraft.world.damagesource.DamageTypes;
import net.minecraft.world.entity.Entity;
import net.minecraft.world.entity.EquipmentSlot;
import net.minecraft.world.entity.LivingEntity;
import net.minecraft.world.entity.ai.attributes.Attributes;
import net.minecraft.world.entity.player.Player;
import net.minecraft.world.item.Item;
import net.minecraft.world.item.ItemStack;
import net.minecraft.world.item.Items;
import net.minecraft.world.phys.AABB;
import net.minecraft.world.phys.Vec3;
import net.neoforged.bus.api.EventPriority;
import net.neoforged.bus.api.SubscribeEvent;
import net.neoforged.fml.common.EventBusSubscriber;
import net.neoforged.neoforge.event.entity.living.*;
import net.neoforged.neoforge.event.entity.player.AttackEntityEvent;
import net.neoforged.neoforge.event.entity.player.CanContinueSleepingEvent;
import net.neoforged.neoforge.event.entity.player.CanPlayerSleepEvent;
import net.neoforged.neoforge.event.entity.player.PlayerWakeUpEvent;
import net.neoforged.neoforge.event.level.SleepFinishedTimeEvent;
import net.neoforged.neoforge.event.tick.PlayerTickEvent;
import net.vami.prevail.ModTags;
import net.vami.prevail.Prevail;
import net.vami.prevail.attachment.MobData;
import net.vami.prevail.util.DataUtil;
import net.vami.prevail.util.CombatUtil;

import java.util.List;

@EventBusSubscriber(modid = Prevail.MOD_ID)
public class PlayerCombatEvents {

    // this godforsaken shxtfest of a method has my head spinning
    // and my neck cranked beyond repair. i just manually tweaked it till it worked.
    // please dont make me change it.
    // moved from Mixin to event!
    @SubscribeEvent(priority = EventPriority.HIGHEST)
    public static void distanceDmg(LivingDamageEvent.Pre event) {
        DamageSource source = event.getSource();
        double distanceMultiplier = 2;
        float originalAmount = event.getNewDamage();
        float result;
        if (source.getEntity() == null) return;

        if (!(source.getEntity() instanceof Player sourcePlayer)
                || !source.is(ModTags.DamageTypes.MELEE)) return;

        LivingEntity target = event.getEntity();

        double dist = sourcePlayer.position().distanceTo(target.position());
        double reachCalc = Math.max(0.1, dist) / Math.max(0.1, sourcePlayer.getAttributeValue(Attributes.ENTITY_INTERACTION_RANGE));
        // so basically if the player is more than a portion of the dist away
        // the damage will quickly fall off, n it goes moot (i watched suits once)
        if (reachCalc <= 0.65) {
            result = (float) (originalAmount * (1 + (distanceMultiplier - 1) * (0.75 - (reachCalc))));
        } else {
            result = (float) (originalAmount / (1 + (distanceMultiplier - 1) * (3 * reachCalc - 1)));
        }
        event.setNewDamage(result);
        System.out.println("oldDamage: " + event.getOriginalDamage() + " | Result: " + result + " | newDamage: " + event.getNewDamage());

    }


    // allows sleep all times of day
    @SubscribeEvent
    public static void allowSleep(CanPlayerSleepEvent event) {
        if (event.getProblem() == Player.BedSleepingProblem.NOT_POSSIBLE_NOW) {
            event.setProblem(null);
        }
    }

    // (neoforge only) allows continuing to sleep
    @SubscribeEvent
    public static void continueSleeping(CanContinueSleepingEvent event) {
        if (event.getProblem() == Player.BedSleepingProblem.NOT_POSSIBLE_NOW) {
            event.setContinueSleeping(true);
        }
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
    // renaming "capability" to "data" for convenience's sake
    @SubscribeEvent
    public static void onPlayerSlept(PlayerWakeUpEvent event) {
        Player player = event.getEntity();

        MobData data = DataUtil.getData(player);

        if (data.getMaxHeal() < player.getMaxHealth() * 0.8f) {
            data.setMaxHeal(player.getMaxHealth() * 0.8f);
        }
    }

    // despair exists so you dont spam attack.
    // despair is reduced automatically with time
    // renaming "capability" to "data" for convenience's sake
    @SubscribeEvent(priority = EventPriority.HIGHEST)
    public static void despairStack(AttackEntityEvent event) {
        if (event.getEntity().level().isClientSide()) return;

        Player player = event.getEntity();
        MobData data = DataUtil.getData(player);

        if (!CombatUtil.isSweep(player)) {
            // make configurable
            // shield increases despair, because fxck shield abusers
            int amount = player.getItemInHand(InteractionHand.OFF_HAND).getItem()
                    == Items.SHIELD ? 2 : 1;

            data.setDespair(data.getDespair() + amount);
        } else {
            // if ur sweeping, it means u relaxed
            // good job, have less despair
            data.setDespair(Math.max(0, data.getDespair() - 2));
        }
    }

    // renaming "capability" to "data" for convenience's sake
    @SubscribeEvent
    public static void despairEffect(LivingIncomingDamageEvent event) {
        if (event.getEntity().level().isClientSide()) return;
        if (!CombatUtil.isMelee(event)) return;

        Entity source = event.getSource().getEntity();
        if (source instanceof Player player) {
            MobData data = DataUtil.getData(player);

            // needa make it configurable
            int despairThreshold = 7;
            if (data.getDespair() > despairThreshold) {
                event.setCanceled(true); // you cannot spam. be calm. be relaxed.
            }
        }
    }

    // despair fades away slowly
    // i made it once per second for performance reasons
    // tho honestly might be unnecessary lol
    // renaming "capability" to "data" for convenience's sake
    @SubscribeEvent
    public static void despairFade(PlayerTickEvent.Pre event) {
        Player player = event.getEntity();
        if (player.level().isClientSide()) return;

        if (player.tickCount % 20 == 0) {
            MobData data = DataUtil.getData(player);

            data.setDespair(Math.max(0, data.getDespair() - 1));
        }
    }

    // the only release to despair is death
    // renaming "capability" to "data" for convenience's sake
    @SubscribeEvent
    public static void resetDespair(LivingDeathEvent event) {
        if (event.getEntity().level().isClientSide()) return;
        if (event.getEntity() instanceof Player player) {
        MobData data = DataUtil.getData(player);

            data.setDespair(0);
        }
    }

    // when your shield is attacked, you cant use it for a brief period
    // cooldown depends on the damage blocked
    // changed from ShieldBlockEvent (1.20.1 forge)
    @SubscribeEvent(priority = EventPriority.LOWEST)
    public static void shieldCooldownOnHit(LivingShieldBlockEvent event) {
        if (event.getEntity().level().isClientSide()) return;
        LivingEntity entity = event.getEntity();
        ItemStack stack = entity.getItemInHand(entity.getUsedItemHand());
        if (event.getBlockedDamage() < 1) return;

        if (entity instanceof Player player) {
            player.getCooldowns().addCooldown(stack.getItem(),
                    (int) event.getOriginalBlockedDamage());
            player.stopUsingItem();
            stack.hurtAndBreak((int) event.getBlockedDamage(), player,
                    entity.getEquipmentSlotForItem(stack));
        }
    }

    // if you cancel ur chomp it gotta account for that in the data
    // renaming "capability" to "data" for convenience's sake
    @SubscribeEvent
    public static void chompCancel(LivingEntityUseItemEvent.Stop event) {
        if (!(event.getEntity() instanceof Player player)) return;
        MobData data = DataUtil.getData(player);

        if (data.hasChomped()) {
            data.setChomped(false);
        }
    }

    // u cant chomp without consequences. eat slower next time dumbxss
    // renaming "capability" to "data" for convenience's sake
    @SubscribeEvent
    public static void chompCooldown(LivingEntityUseItemEvent.Finish event) {
        if (!(event.getEntity() instanceof Player player)) return;

        MobData data = DataUtil.getData(player);

        if (data.hasChomped()) {
            for (Item item : BuiltInRegistries.ITEM) {
                if (item.getFoodProperties(item.getDefaultInstance(), player) != null) {
                    player.getCooldowns().addCooldown(item, 600);
                }
            }
            data.setChomped(false);
        }
    }

    // this exists because there's an annoying thing that happens
    // when you eat with a shield while sprinting, you can sometimes
    // rush without meaning to. this is made to prevent that.
    @SubscribeEvent
    public static void shieldRushWaiting(LivingEntityUseItemEvent.Finish event) {
        if (!(event.getEntity() instanceof Player player)) return;

        ItemStack item = event.getItem();
        if (item.getFoodProperties(player) == null) return;

        MobData data = DataUtil.getData(player);
        data.setShieldRushWait(7);
    }

    // the timer for the method above so it fades out
    // and you can shield rush again
    @SubscribeEvent
    public static void shieldRushWaitTimer(PlayerTickEvent.Pre event) {
        Player player = event.getEntity();

        MobData data = DataUtil.getData(player);
        if (data.getShieldRushWait() > 0) {
            data.setShieldRushWait(data.getShieldRushWait() - 1);
        }
    }

    // si tienes que correr, hazlo!
    // renaming "capability" to "data" for convenience's sake
    @SubscribeEvent
    public static void shieldRush(PlayerTickEvent.Pre event) {
        if (event.getEntity().level() instanceof ServerLevel server) {
            Player player = event.getEntity();

            MobData data = DataUtil.getData(player);
            if (data.getShieldRushWait() > 0) return;

            if (player.isBlocking()
                    && player.isSprinting()
                    && !player.isCrouching()
                    && player.getFoodData().getFoodLevel() > 6
                    && !player.isInWater()) {
                if (data.getShieldRush() >= 10) {

                    data.setShieldRush(0);

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

                data.setShieldRush(data.getShieldRush() + 1);

            } else if (data.getShieldRush() > 0) {
                player.getCooldowns().addCooldown(player.getItemInHand(player.getUsedItemHand()).getItem(), data.getShieldRush() * 3);

                server.playSound(null,
                        player.getX(), player.getY() + 1, player.getZ(),
                        SoundEvents.SHIELD_BREAK, SoundSource.PLAYERS,
                        0.3F, 1.25F);

                data.setShieldRush(0);
            }
        }
    }
}
