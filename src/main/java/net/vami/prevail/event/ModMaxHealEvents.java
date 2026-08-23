package net.vami.prevail.event;

import net.minecraft.network.chat.Component;
import net.minecraft.world.effect.MobEffects;
import net.minecraft.world.entity.player.Player;
import net.minecraftforge.common.MinecraftForge;
import net.minecraftforge.event.TickEvent;
import net.minecraftforge.event.entity.EntityJoinLevelEvent;
import net.minecraftforge.event.entity.living.LivingDamageEvent;
import net.minecraftforge.event.entity.living.LivingDeathEvent;
import net.minecraftforge.event.entity.living.LivingHealEvent;
import net.minecraftforge.eventbus.api.EventPriority;
import net.minecraftforge.eventbus.api.SubscribeEvent;
import net.minecraftforge.fml.common.Mod;
import net.vami.prevail.Prevail;
import net.vami.prevail.capability.MobCapability;
import net.vami.prevail.event.custom.MaxHealTriggerEvent;
import net.vami.prevail.util.CapUtil;
import net.vami.prevail.util.MaxHealUtil;

import java.text.DecimalFormat;

@Mod.EventBusSubscriber(modid = Prevail.MOD_ID)
public class ModMaxHealEvents {

    // this is for personal testing/debugging purposes
    /*@SubscribeEvent
    public static void maxHealTick(TickEvent.PlayerTickEvent event) {
        if (event.player.level().isClientSide()) return;

        Player player = event.player;
        if (event.phase == TickEvent.Phase.START
        && player.tickCount % 10 == 0) {
            player.displayClientMessage(
                    Component.literal("Maxheal: " + new DecimalFormat("##.##").format(MaxHealUtil.get(player))),
                    true);
        }
    }*/

    // commented this out cause i thought maybe a mixin would be better
    // FoodDataMixin.java :)
    /*@SubscribeEvent(priority = EventPriority.HIGHEST)
    public static void maxHealRestrict(LivingHealEvent event) {
        if (event.getEntity().level().isClientSide()) return;

        if (event.getAmount() > 1) return;

        if (event.getEntity() instanceof Player player) {
            MobCapability capability = CapUtil.getCap(player);
            if (capability == null) return;

            // restricts healing if it goes beyond maxHeal
            if (player.getHealth() + event.getAmount() > capability.getMaxHeal()) {

                // trigger the event
                MaxHealTriggerEvent triggerEvent = new MaxHealTriggerEvent(player);
                MinecraftForge.EVENT_BUS.post(triggerEvent);
                if (triggerEvent.isCanceled()) {
                    return;
                }

                player.setHealth(capability.getMaxHeal());
                event.setCanceled(true);
            }
        }
    }*/

    @SubscribeEvent(priority = EventPriority.LOWEST)
    public static void maxHealOnJoin(EntityJoinLevelEvent event) {
        if (event.getEntity().level().isClientSide()) return;

        if (!(event.getEntity() instanceof Player player)) return;

        MobCapability capability = CapUtil.getCap(player);
        if (capability == null) return;

        if (capability.getMaxHeal() > 0) return;
        capability.setMaxHeal(player.getMaxHealth());
        Prevail.LOGGER.info("max heal: " + capability.getMaxHeal());

    }

    // resets maxheal on death
    @SubscribeEvent
    public static void maxHealOnDeath(LivingDeathEvent event) {
        if (event.getEntity().level().isClientSide()) return;
        if (!(event.getEntity() instanceof Player player)) return;

        MobCapability capability = CapUtil.getCap(player);
        if (capability == null) return;

        capability.setMaxHeal(0f);
    }

    // when u take damage u take some perma loss to ur HP
    @SubscribeEvent(priority = EventPriority.LOWEST)
    public static void maxHealDecrease(LivingDamageEvent event) {
        if (event.getEntity().level().isClientSide()) return;

        float amount = event.getAmount();
        if (amount < 1) return;

        if (event.getEntity() instanceof Player player) {
            MobCapability capability = CapUtil.getCap(player);
            if (capability == null) return;

            // this means that taking five hearts of dmg
            // result in one heart of perma lost health
            float result = amount / 5;
            MaxHealUtil.set(capability, player, capability.getMaxHeal() - result);
        }
    }

    // increases maxHeal if the player has regeneration
    @SubscribeEvent
    public static void maxHealIncrease(TickEvent.PlayerTickEvent event) {
        if (event.player.level().isClientSide()) return;

        Player player = event.player;
        if (event.phase == TickEvent.Phase.START) {
            if (player.hasEffect(MobEffects.REGENERATION)) {

                MobCapability capability = CapUtil.getCap(player);
                if (capability == null) return;

                // make diff configurable
                float diff = 0.01f;
                diff *= player.getEffect(MobEffects.REGENERATION).getAmplifier() + 1;
                MaxHealUtil.set(capability, player, capability.getMaxHeal() + diff);
            }
        }
    }
}
