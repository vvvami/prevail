package net.vami.prevail.mixin;

import net.minecraft.world.damagesource.DamageSource;
import net.minecraft.world.entity.LivingEntity;
import net.minecraft.world.entity.ai.attributes.AttributeMap;
import net.minecraft.world.entity.player.Player;
import net.minecraftforge.common.ForgeMod;
import net.vami.prevail.ModTags;
import net.vami.prevail.Prevail;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.Shadow;
import org.spongepowered.asm.mixin.injection.At;
import org.spongepowered.asm.mixin.injection.Constant;
import org.spongepowered.asm.mixin.injection.ModifyConstant;
import org.spongepowered.asm.mixin.injection.ModifyVariable;

@Mixin(LivingEntity.class)
public abstract class LivingEntityMixin {

    @Shadow
    public abstract AttributeMap getAttributes();

    @ModifyVariable(method = "hurt", at = @At(value = "INVOKE", target = "Lnet/minecraft/world/entity/LivingEntity;isSleeping()Z"), argsOnly = true )
    private float distanceDamageCalc(float originalAmount, DamageSource source, float amount) {
        double distanceMultiplier = 2;
        float result = originalAmount;

        if (!(source.getEntity() instanceof Player sourcePlayer) || !source.is(ModTags.DamageTypes.MELEE)) return originalAmount;
        LivingEntity targetEntity = (LivingEntity) (Object) this;

        double dist = sourcePlayer.position().distanceTo(targetEntity.position());
        double reachCalc = Math.max(0.1, dist) / Math.max(0.1, sourcePlayer.getAttributeValue(ForgeMod.ENTITY_REACH.get()));
        if (reachCalc <= 0.5) {
            result = (float) (originalAmount * (1 + (distanceMultiplier - 1) * (1 - 1.5 * reachCalc)));
        } else {
            result = (float) (originalAmount / (1 + (distanceMultiplier - 1) * (2 * reachCalc - 1)));
        }
        Prevail.LOGGER.debug("damage: " + result);
        return result;
    }


    @ModifyConstant(method = "isBlocking", constant = @Constant(intValue = 5))
    private int setShieldUseDelay(int constant) {
        return 0;
    }
}
