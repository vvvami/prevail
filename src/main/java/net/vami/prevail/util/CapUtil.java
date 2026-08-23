package net.vami.prevail.util;

import net.minecraft.world.entity.Entity;
import net.minecraft.world.entity.LivingEntity;
import net.vami.prevail.capability.MobCapability;
import net.vami.prevail.capability.MobCapabilityProvider;

import javax.annotation.Nullable;

public class CapUtil {

    @Nullable
    public static MobCapability getCap(Entity entity) {
        return entity.getCapability(MobCapabilityProvider.CAPABILITY)
                .orElse(null);
    }
}