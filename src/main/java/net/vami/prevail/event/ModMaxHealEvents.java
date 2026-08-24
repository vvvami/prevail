package net.vami.prevail.event;

import net.minecraft.world.effect.MobEffects;
import net.minecraft.world.entity.player.Player;
import net.neoforged.bus.api.EventPriority;
import net.neoforged.bus.api.SubscribeEvent;
import net.neoforged.fml.common.EventBusSubscriber;
import net.neoforged.neoforge.event.entity.EntityJoinLevelEvent;
import net.neoforged.neoforge.event.entity.living.LivingDamageEvent;
import net.neoforged.neoforge.event.entity.living.LivingDeathEvent;
import net.neoforged.neoforge.event.tick.PlayerTickEvent;
import net.vami.prevail.Prevail;
import net.vami.prevail.attachment.MobData;
import net.vami.prevail.util.DataUtil;
import net.vami.prevail.util.MaxHealUtil;

@EventBusSubscriber(modid = Prevail.MOD_ID)
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

    // renaming "capability" to "data" for convenience's sake
    @SubscribeEvent(priority = EventPriority.LOWEST)
    public static void maxHealOnJoin(EntityJoinLevelEvent event) {
        if (event.getEntity().level().isClientSide()) return;

        if (!(event.getEntity() instanceof Player player)) return;

        MobData data = DataUtil.getData(player);

        if (data.getMaxHeal() > 0) return;
        data.setMaxHeal(player.getMaxHealth());
        Prevail.LOGGER.info("max heal: " + data.getMaxHeal());

    }

    // resets maxheal on death
    // renaming "capability" to "data" for convenience's sake
    @SubscribeEvent
    public static void maxHealOnDeath(LivingDeathEvent event) {
        if (event.getEntity().level().isClientSide()) return;
        if (!(event.getEntity() instanceof Player player)) return;

        MobData data = DataUtil.getData(player);

        data.setMaxHeal(0f);
    }

    // when u take damage u take some perma loss to ur HP
    // renaming "capability" to "data" for convenience's sake
    @SubscribeEvent(priority = EventPriority.LOWEST)
    public static void maxHealDecrease(LivingDamageEvent.Pre event) {
        if (event.getEntity().level().isClientSide()) return;

        float amount = event.getNewDamage();
        if (amount < 1) return;

        if (event.getEntity() instanceof Player player) {
            MobData data = DataUtil.getData(player);

            // this means that taking five hearts of dmg
            // result in one heart of perma lost health
            float result = amount / 5;
            MaxHealUtil.set(data, player, data.getMaxHeal() - result);
        }
    }

    // increases maxHeal if the player has regeneration
    // renaming "capability" to "data" for convenience's sake
    @SubscribeEvent
    public static void maxHealIncrease(PlayerTickEvent.Pre event) {
        if (event.getEntity().level().isClientSide()) return;

        Player player = event.getEntity();
        if (player.hasEffect(MobEffects.REGENERATION)) {

            MobData data = DataUtil.getData(player);

            // make diff configurable
            float diff = 0.01f;
            diff *= player.getEffect(MobEffects.REGENERATION).getAmplifier() + 1;
            MaxHealUtil.set(data, player, data.getMaxHeal() + diff);
        }

    }
}
