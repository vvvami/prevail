package net.vami.prevail.mixin;

import net.minecraft.world.entity.player.Player;
import net.minecraft.world.food.FoodData;
import net.neoforged.neoforge.common.NeoForge;
import net.vami.prevail.attachment.MobData;
import net.vami.prevail.event.custom.MaxHealTriggerEvent;
import net.vami.prevail.util.DataUtil;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.Shadow;
import org.spongepowered.asm.mixin.Unique;
import org.spongepowered.asm.mixin.injection.At;
import org.spongepowered.asm.mixin.injection.ModifyArg;
import org.spongepowered.asm.mixin.injection.ModifyVariable;

@Mixin(FoodData.class)
public class FoodDataMixin {
    // this is meant to reduce natural healing, cause currently, its bullshxt
    // how you can instantly heal like four hearts from eating a steak
    // what kinda magical fxcking steak is that

    @Shadow
    private float saturationLevel;

    // maxheal enforcement
    @ModifyVariable(method = "tick", at = @At("STORE"), name = "flag")
    private boolean prevailNaturalRegen(boolean originalValue, Player pPlayer) {

        MobData capability = DataUtil.getData(pPlayer);
        if (capability == null) return originalValue;

        float healAmount = Math.min(saturationLevel, 6.0F) / 6;

        // restricts healing if it goes beyond maxHeal
        if (pPlayer.getHealth() + healAmount > capability.getMaxHeal()) {

            // trigger the event
            MaxHealTriggerEvent triggerEvent = new MaxHealTriggerEvent(pPlayer);
            NeoForge.EVENT_BUS.post(triggerEvent);
            if (triggerEvent.isCanceled()) {
                return originalValue;
            }

            pPlayer.heal(capability.getMaxHeal() - pPlayer.getHealth());
            return false;
        }

        return originalValue;
    }

    @Unique
    private float amount = 6f; // it unique so it shouldnt have a problem right?
    // reduces the actual natural healing from the quickheal
    @ModifyArg(method = "tick", at = @At(value = "INVOKE",
            target = "Lnet/minecraft/world/entity/player/Player;heal(F)V", ordinal = 0))
    private float prevailFoodHeal(float par1) {
        return 1 / amount;
    }

    // we also reduce the exhaustion rate, because we really dont want
    // the player losing saturation as if they rapid healing
    // while not actually rapid-healing, entiendes?
    @ModifyArg(method = "tick", at = @At(value = "INVOKE",
            target = "Lnet/minecraft/world/food/FoodData;addExhaustion(F)V", ordinal = 0))
    private float prevailExhaustReduction(float par1) {
        return par1 / amount;
    }
}
