package net.vami.prevail.mixin;

import net.minecraft.world.effect.MobEffect;
import net.minecraft.world.entity.Entity;
import net.minecraft.world.entity.LivingEntity;
import net.minecraft.world.entity.player.Player;
import net.vami.prevail.attachment.MobData;
import net.vami.prevail.util.DataUtil;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.injection.At;
import org.spongepowered.asm.mixin.injection.Inject;
import org.spongepowered.asm.mixin.injection.callback.CallbackInfo;

@Mixin(MobEffect.class)
public class MobEffectMixin {

    // renaming "capability" to "data" for convenience's sake
    @Inject(method = "applyInstantenousEffect", at = @At(value = "HEAD"))
    private void prevailInstantHealthCheck(Entity pSource, Entity pIndirectSource, LivingEntity pLivingEntity, int pAmplifier, double pHealth, CallbackInfo ci) {
        if (!(pLivingEntity instanceof Player player)) return;

        MobData data = DataUtil.getData(player);

        data.setMaxHeal((float) (Math.min(player.getMaxHealth(), data.getMaxHeal() + pHealth)));
    }
}
