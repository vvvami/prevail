package net.vami.prevail.mixin;

import net.minecraft.network.chat.Component;
import net.minecraft.util.Mth;
import net.minecraft.world.InteractionHand;
import net.minecraft.world.damagesource.DamageSource;
import net.minecraft.world.entity.LivingEntity;
import net.minecraft.world.entity.player.Player;
import net.minecraft.world.item.ItemStack;
import net.minecraftforge.common.ForgeMod;
import net.vami.prevail.ModTags;
import net.vami.prevail.Prevail;
import net.vami.prevail.capability.PlayerCapability;
import net.vami.prevail.util.CapabilityUtil;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.Shadow;
import org.spongepowered.asm.mixin.Unique;
import org.spongepowered.asm.mixin.injection.*;
import org.spongepowered.asm.mixin.injection.callback.CallbackInfo;

@Mixin(LivingEntity.class)
public abstract class LivingEntityMixin {

    @ModifyVariable(method = "hurt", at = @At(value = "INVOKE", target = "Lnet/minecraft/world/entity/LivingEntity;isSleeping()Z"), argsOnly = true )
    private float prevail$distanceDmg(float originalAmount, DamageSource source, float amount) {
        double distanceMultiplier = 2;
        float result;
        if (source.getEntity() == null) return originalAmount;

        if (!(source.getEntity() instanceof Player sourcePlayer) || !source.is(ModTags.DamageTypes.MELEE)) return originalAmount;

        LivingEntity targetEntity = (LivingEntity) (Object) this;
        if (targetEntity == null) return originalAmount;

        double dist = sourcePlayer.position().distanceTo(targetEntity.position());
        double reachCalc = Math.max(0.1, dist) / Math.max(0.1, sourcePlayer.getAttributeValue(ForgeMod.ENTITY_REACH.get()));
        if (reachCalc <= 0.75) {
            result = (float) (originalAmount * (1 + (distanceMultiplier - 1) * (0.75 - (reachCalc))));
        } else {
            result = (float) (originalAmount / (1 + (distanceMultiplier - 1) * (3 * reachCalc - 1)));
        }
        Prevail.LOGGER.debug("damage: " + result);
        sourcePlayer.sendSystemMessage(Component.literal("damage: " + result));
        return result;
    }


    @ModifyConstant(method = "isBlocking", constant = @Constant(intValue = 5))
    private int prevail$shieldDelay(int constant) {
        return 0;
    }



    @Shadow protected ItemStack useItem;
    @Shadow protected int useItemRemaining;

    @Inject(method = "startUsingItem", at = @At("TAIL"))
    private void prevail$chomp(InteractionHand hand, CallbackInfo ci) {
        LivingEntity self = (LivingEntity) (Object) this;
        ItemStack stack = this.useItem;

        if (stack.isEmpty()) return;
        if (!stack.isEdible()) return;
        if (!(self instanceof Player player)) return;

        if (prevail$chompCondition(player)) {
            this.useItemRemaining = Math.max(1, Mth.ceil(this.useItemRemaining * 0.25F));
            if (CapabilityUtil.checkCapability(player)) {
               PlayerCapability capability = CapabilityUtil.getCapability(player);
               capability.chomped.set(true);
            }
        }
    }

    @Unique
    private static boolean prevail$chompCondition(Player player) {
        return player.isCrouching();
    }
}
