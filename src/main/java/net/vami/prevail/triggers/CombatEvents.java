package net.vami.prevail.triggers;

import net.minecraft.core.registries.Registries;
import net.minecraft.resources.ResourceLocation;
import net.minecraft.server.level.ServerLevel;
import net.minecraft.sounds.SoundEvents;
import net.minecraft.sounds.SoundSource;
import net.minecraft.tags.TagKey;
import net.minecraft.world.InteractionHand;
import net.minecraft.world.damagesource.DamageSource;
import net.minecraft.world.damagesource.DamageTypes;
import net.minecraft.world.entity.Entity;
import net.minecraft.world.entity.LivingEntity;
import net.minecraft.world.entity.player.Player;
import net.minecraft.world.item.Item;
import net.minecraft.world.item.ItemStack;
import net.minecraft.world.item.Items;
import net.minecraft.world.phys.AABB;
import net.minecraft.world.phys.Vec3;
import net.minecraftforge.event.TickEvent;
import net.minecraftforge.event.entity.living.LivingAttackEvent;
import net.minecraftforge.event.entity.living.LivingDeathEvent;
import net.minecraftforge.event.entity.living.LivingEntityUseItemEvent;
import net.minecraftforge.event.entity.living.ShieldBlockEvent;
import net.minecraftforge.event.entity.player.AttackEntityEvent;
import net.minecraftforge.eventbus.api.EventPriority;
import net.minecraftforge.eventbus.api.SubscribeEvent;
import net.minecraftforge.fml.common.Mod;
import net.minecraftforge.registries.ForgeRegistries;
import net.vami.prevail.ModTags;
import net.vami.prevail.Prevail;
import net.vami.prevail.capability.PlayerCapability;
import net.vami.prevail.util.CapabilityUtil;
import net.vami.prevail.util.CombatUtil;

import java.util.List;

@Mod.EventBusSubscriber(modid = Prevail.MOD_ID)
public class CombatEvents {

    @SubscribeEvent(priority = EventPriority.HIGHEST)
    public static void iFrameRemoval(LivingAttackEvent event) {
        if (event.getEntity().level().isClientSide()) return;
        if (event.getSource().is(ModTags.DamageTypes.MELEE)
        || event.getSource().is(TagKey.create(Registries.DAMAGE_TYPE,
                ResourceLocation.parse("minecraft:is_projectile")))) {
            event.getEntity().invulnerableTime = 0;
        }
    }

    @SubscribeEvent(priority = EventPriority.HIGHEST)
    public static void despairStack(AttackEntityEvent event) {
        if (event.getEntity().level().isClientSide()) return;

        Player player = event.getEntity();
        if (CapabilityUtil.checkCapability(player)) {
            PlayerCapability capability = CapabilityUtil.getCapability(player);
            if (!CombatUtil.isSweep(player)) {
                // make configurable
                int amount = player.getItemInHand(InteractionHand.OFF_HAND).getItem()
                        == Items.SHIELD ? 2 : 1;
                capability.despair.set(capability.despair.get() + amount);
            } else {
                capability.despair.set(Math.max(0, capability.despair.get() - 1));
            }
        }
    }

    @SubscribeEvent
    public static void despairEffect(LivingAttackEvent event) {
        if (event.getEntity().level().isClientSide()) return;
        if (!CombatUtil.isMelee(event)) return;

        Entity source = event.getSource().getEntity();
        if (source instanceof Player player
                && CapabilityUtil.checkCapability(player)) {
            PlayerCapability capability = CapabilityUtil.getCapability(player);
            // make configurable
            int despairThreshold = 7;
            if (capability.despair.get() > despairThreshold) {
                event.setCanceled(true);
            }
        }
    }

    @SubscribeEvent
    public static void despairFade(TickEvent.PlayerTickEvent event) {
        if (event.player.level().isClientSide()) return;
        if (event.player.tickCount % 20 == 0
        && CapabilityUtil.checkCapability(event.player)) {
            PlayerCapability capability = CapabilityUtil.getCapability(event.player);
            capability.despair.set(Math.max(0, capability.despair.get() - 1));
        }
    }

    @SubscribeEvent
    public static void resetDespair(LivingDeathEvent  event) {
        if (event.getEntity().level().isClientSide()) return;
        if (event.getEntity() instanceof Player player
        && CapabilityUtil.checkCapability(player)) {
        PlayerCapability capability = CapabilityUtil.getCapability(player);
        capability.despair.set(0);
        }
    }

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

    @SubscribeEvent
    public static void chompCancel(LivingEntityUseItemEvent.Stop event) {
        if (!(event.getEntity() instanceof Player player)) return;
        if (CapabilityUtil.checkCapability(player)) {
            PlayerCapability capability = CapabilityUtil.getCapability(player);
            if (capability.chomped.get()) {capability.chomped.set(false);}
        }
    }

    @SubscribeEvent
    public static void chompCooldown(LivingEntityUseItemEvent.Finish event) {
        if (!(event.getEntity() instanceof Player player)) return;
        if (CapabilityUtil.checkCapability(player)) {
            PlayerCapability capability = CapabilityUtil.getCapability(player);

            if (capability.chomped.get()) {
                for (Item item : ForgeRegistries.ITEMS.getValues()) {
                    if (item.isEdible()) {
                        player.getCooldowns().addCooldown(item, 600);
                    }
                }
                capability.chomped.set(false);
            }
        }
    }

    @SubscribeEvent
    public static void shieldRush(TickEvent.PlayerTickEvent event) {
        if (event.player.level() instanceof ServerLevel server) {
            Player player = event.player;
            if (CapabilityUtil.checkCapability(player)) {
                PlayerCapability capability = CapabilityUtil.getCapability(player);
                if (player.isBlocking()
                        && player.isSprinting()
                        && !player.isCrouching()
                        && player.getFoodData().getFoodLevel() > 6
                        && !player.isInWater()
                        && !player.onGround()) {
                    if (capability.shieldRush.get() >= 10) {
                        capability.shieldRush.set(0);
                        server.playSound(null, player.getX(), player.getY() + 1, player.getZ(),
                                SoundEvents.SHIELD_BREAK, SoundSource.PLAYERS, 1.0F, 0.25F);
                        player.getCooldowns().addCooldown(player.getItemInHand(player.getUsedItemHand()).getItem(),
                                30);
                        player.stopUsingItem();
                        return;
                    }
                    player.hurtMarked = true;
                    player.setDeltaMovement(new Vec3(
                            player.getLookAngle().x,
                            player.getDeltaMovement().y,
                            player.getLookAngle().z));

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
                                    && e.isPickable()
                    );

                    for (Entity e : candidates) {
                        e.hurt(new DamageSource(player.level().registryAccess()
                                .registryOrThrow(Registries.DAMAGE_TYPE)
                                .getHolderOrThrow(DamageTypes.PLAYER_ATTACK), player), 1f);
                        e.setDeltaMovement(new Vec3(
                                (e.getX() - player.getX()),
                                (e.getY() - player.getY()),
                                (e.getZ() - player.getZ())));
                    }
                    capability.shieldRush.set(capability.shieldRush.get() + 1);
                } else if (capability.shieldRush.get() > 0) {
                    player.getCooldowns().addCooldown(player.getItemInHand(player.getUsedItemHand()).getItem(), capability.shieldRush.get() * 3);
                    server.playSound(null, player.getX(), player.getY() + 1, player.getZ(),
                            SoundEvents.SHIELD_BREAK, SoundSource.PLAYERS, 0.3F, 1.25F);
                    capability.shieldRush.set(0);
                }
            }
        }
    }
}
