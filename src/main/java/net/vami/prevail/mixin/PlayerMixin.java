package net.vami.prevail.mixin;

import net.minecraft.world.entity.player.Player;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.Unique;
import org.spongepowered.asm.mixin.injection.At;
import org.spongepowered.asm.mixin.injection.ModifyVariable;

@Mixin(Player.class)
public class PlayerMixin {
    @Unique
    boolean prevailAllowCrit = false; // this just in case i want my own condition

    @ModifyVariable(method = "attack", at = @At(value = "STORE"), name = "flag2")
    private boolean prevailCrit(boolean originalCanCrit) {

        return originalCanCrit && prevailAllowCrit;
    }
}