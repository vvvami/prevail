package net.vami.prevail.mixin;

import net.minecraft.util.Mth;
import net.minecraft.world.InteractionHand;
import net.minecraft.world.damagesource.DamageSource;
import net.minecraft.world.entity.LivingEntity;
import net.minecraft.world.entity.player.Player;
import net.minecraft.world.item.ItemStack;
import net.minecraftforge.common.ForgeMod;
import net.vami.prevail.ModTags;
import net.vami.prevail.capability.MobCapability;
import net.vami.prevail.util.CapUtil;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.Shadow;
import org.spongepowered.asm.mixin.injection.*;
import org.spongepowered.asm.mixin.injection.callback.CallbackInfo;

@Mixin(LivingEntity.class)
public abstract class LivingEntityMixin {

    // stops the stupid shield-use delay bullshxt
    @ModifyConstant(method = "isBlocking", constant = @Constant(intValue = 5))
    private int prevailShieldDelay(int constant) {
        return 0;
    }

    @Shadow protected ItemStack useItem;
    @Shadow protected int useItemRemaining;

    // quick eating stuff, is currently x4 faster
    @Inject(method = "startUsingItem", at = @At("TAIL"))
    private void prevailChomp(InteractionHand hand, CallbackInfo ci) {
        LivingEntity self = (LivingEntity) (Object) this;
        ItemStack stack = this.useItem;

        if (stack.isEmpty()) return;
        if (!stack.isEdible()) return;
        if (!(self instanceof Player player)) return;
        // u gotta sneak for it to work
        if (player.isCrouching()) {
            this.useItemRemaining = Math.max(1, Mth.ceil(this.useItemRemaining * 0.25F));

            MobCapability capability = CapUtil.getCap(player);
            if (capability == null) return;

            capability.setChomped(true);
        }
    }
}
