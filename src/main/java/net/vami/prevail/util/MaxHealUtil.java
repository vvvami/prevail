package net.vami.prevail.util;

import net.minecraft.world.entity.player.Player;
import net.neoforged.neoforge.common.NeoForge;
import net.vami.prevail.attachment.MobData;
import net.vami.prevail.event.custom.MaxHealDecreaseEvent;
import net.vami.prevail.event.custom.MaxHealIncreaseEvent;

public class MaxHealUtil {

    // so i made a bunch of events to allow compat with other mods.
    // the idea is that, for example, if farmers delight or some shxt
    // wanted to add plated golden apple soup that restored maxheal,
    // they can do that using the events. joy

    public static void set(MobData capability, Player player, float setAmount) {
        float diff;
        if (setAmount < capability.getMaxHeal()) {
            diff = capability.getMaxHeal() - setAmount;

            MaxHealDecreaseEvent event = new MaxHealDecreaseEvent(player, diff);
            NeoForge.EVENT_BUS.post(event);

            if (event.isCanceled()) return;

            setAmount = capability.getMaxHeal() - event.getAmount();

        } else if (setAmount > capability.getMaxHeal()) {
            diff = setAmount - capability.getMaxHeal();
            MaxHealIncreaseEvent event = new MaxHealIncreaseEvent(player, diff);
            NeoForge.EVENT_BUS.post(event);

            if (event.isCanceled()) return;

            setAmount = capability.getMaxHeal() + event.getAmount();
        }

        setAmount = Math.max(1, setAmount);
        setAmount = Math.min(setAmount, player.getMaxHealth());
        capability.setMaxHeal(setAmount);
    }

    public static double get(Player player) {
        MobData capability = DataUtil.getData(player);
        if (capability == null) return player.getMaxHealth();

        return capability.getMaxHeal();
    }
}
