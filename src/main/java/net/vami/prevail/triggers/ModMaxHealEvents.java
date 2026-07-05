package net.vami.prevail.triggers;

import net.minecraft.network.chat.Component;
import net.minecraft.server.level.ServerLevel;
import net.minecraft.world.effect.MobEffects;
import net.minecraft.world.entity.player.Player;
import net.minecraftforge.common.MinecraftForge;
import net.minecraftforge.event.TickEvent;
import net.minecraftforge.event.entity.EntityJoinLevelEvent;
import net.minecraftforge.event.entity.living.LivingDeathEvent;
import net.minecraftforge.event.entity.living.LivingHealEvent;
import net.minecraftforge.event.entity.living.LivingHurtEvent;
import net.minecraftforge.eventbus.api.EventPriority;
import net.minecraftforge.eventbus.api.SubscribeEvent;
import net.minecraftforge.fml.common.Mod;
import net.vami.prevail.Prevail;
import net.vami.prevail.capability.PlayerCapability;
import net.vami.prevail.event.MaxHealTriggerEvent;
import net.vami.prevail.util.CapabilityUtil;
import net.vami.prevail.util.MaxHealUtil;

@Mod.EventBusSubscriber(modid = Prevail.MOD_ID)
public class ModMaxHealEvents {

    @SubscribeEvent
    public static void maxHealTick(TickEvent.PlayerTickEvent event) {
        if (event.player.level().isClientSide()) return;

        Player player = event.player;
        if (event.phase == TickEvent.Phase.START
        && player.tickCount % 10 == 0) {
            player.displayClientMessage(
                    Component.literal("Maxheal: " + MaxHealUtil.get(player)),
                    true);
        }
    }

    @SubscribeEvent(priority = EventPriority.HIGHEST)
    public static void maxHealRestrict(LivingHealEvent event) {
        if (event.getEntity().level().isClientSide()) return;

        if (event.getEntity() instanceof Player player
        && CapabilityUtil.checkCapability(player)) {
            PlayerCapability capability = CapabilityUtil.getCapability(player);

            // increases maxHeal based on stuff like instant health
            if (event.getAmount() > 1) {
                capability.maxHeal.set(capability.maxHeal.get() + event.getAmount() / 10);
            }

            // restricts healing if it goes beyond maxHeal
            if (player.getHealth() + event.getAmount()
            > capability.maxHeal.get()) {

                // trigger the event
                MaxHealTriggerEvent triggerEvent = new MaxHealTriggerEvent(player);
                MinecraftForge.EVENT_BUS.post(triggerEvent);
                if (triggerEvent.isCanceled()) {
                    return;
                }
                event.setCanceled(true);
                Prevail.LOGGER.debug("no healing! maxheal: " + capability.maxHeal.get());
            }
        }
    }

    @SubscribeEvent(priority = EventPriority.LOWEST)
    public static void maxHealOnJoin(EntityJoinLevelEvent event) {
        if (event.getEntity().level().isClientSide()) return;

        if (!(event.getEntity() instanceof Player player)) return;
        if (CapabilityUtil.checkCapability(player)) {
            PlayerCapability capability = CapabilityUtil.getCapability(player);
            if (capability.maxHeal.get() > 0) return;
            capability.maxHeal.set(player.getMaxHealth());
            Prevail.LOGGER.info("max heal: " + capability.maxHeal.get());
        }
    }

    @SubscribeEvent
    public static void maxHealOnDeath(LivingDeathEvent event) {
        if (event.getEntity().level().isClientSide()) return;
        if (!(event.getEntity() instanceof Player player)) return;
        if (CapabilityUtil.checkCapability(player)) {
            PlayerCapability capability = CapabilityUtil.getCapability(player);
            capability.maxHeal.set(0f);
        }
    }

    @SubscribeEvent(priority = EventPriority.LOWEST)
    public static void maxHealDecrease(LivingHurtEvent event) {
        if (event.getEntity().level().isClientSide()) return;

        float amount = event.getAmount();
        if (amount < 1) return;

        if (event.getEntity() instanceof Player player
        && CapabilityUtil.checkCapability(player)) {
            float result = amount / Math.max(1, player.getArmorValue());
            result = (float) Math.sqrt(result);
            PlayerCapability capability = CapabilityUtil.getCapability(player);

            MaxHealUtil.set(capability, player, capability.maxHeal.get() - result);
        }
    }

    // increases maxHeal if the player has regeneration
    @SubscribeEvent
    public static void maxHealIncrease(TickEvent.PlayerTickEvent event) {
        if (event.player.level().isClientSide()) return;

        Player player = event.player;
        if (event.phase == TickEvent.Phase.START) {
            if (player.hasEffect(MobEffects.REGENERATION)
                    && CapabilityUtil.checkCapability(player)) {

                PlayerCapability capability = CapabilityUtil.getCapability(player);
                // make diff configurable
                float diff = 0.01f;
                diff *= player.getEffect(MobEffects.REGENERATION).getAmplifier() + 1;
                MaxHealUtil.set(capability, player, capability.maxHeal.get() + diff);
            }
        }
    }
}
