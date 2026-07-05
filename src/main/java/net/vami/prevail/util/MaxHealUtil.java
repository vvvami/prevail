package net.vami.prevail.util;

import net.minecraft.world.entity.player.Player;
import net.minecraftforge.common.MinecraftForge;
import net.vami.prevail.capability.PlayerCapability;
import net.vami.prevail.event.MaxHealDecreaseEvent;
import net.vami.prevail.event.MaxHealIncreaseEvent;

public class MaxHealUtil {

    public static void set(PlayerCapability capability, Player player, float setAmount) {
        float diff;
        if (setAmount < capability.maxHeal.get()) {
            diff = capability.maxHeal.get() - setAmount;

            MaxHealDecreaseEvent event = new MaxHealDecreaseEvent(player, diff);
            MinecraftForge.EVENT_BUS.post(event);

            if (event.isCanceled()) return;

            setAmount = capability.maxHeal.get() - event.getAmount();

        } else if (setAmount > capability.maxHeal.get()) {
            diff = setAmount - capability.maxHeal.get();
            MaxHealIncreaseEvent event = new MaxHealIncreaseEvent(player, diff);
            MinecraftForge.EVENT_BUS.post(event);

            if (event.isCanceled()) return;

            setAmount = capability.maxHeal.get() + event.getAmount();
        }

        setAmount = Math.max(1, setAmount);
        setAmount = Math.min(setAmount, player.getMaxHealth());
        capability.maxHeal.set(setAmount);
    }

    public static double get(Player player) {
        if (CapabilityUtil.checkCapability(player)) {
            PlayerCapability capability = CapabilityUtil.getCapability(player);
            return capability.maxHeal.get();
        }
        return 0;
    }
}
