package net.vami.prevail.util;

import net.minecraft.world.entity.Entity;
import net.vami.prevail.capability.PlayerCapability;
import net.vami.prevail.capability.PlayerCapabilityProvider;

public class CapabilityUtil {
    // Gets a capability
    public static PlayerCapability getCapability(Entity entity){
        return entity.getCapability(PlayerCapabilityProvider.CAPABILITY).orElse(null);
    }

    // Check a capability before attempting to get or modify its value
    public static boolean checkCapability(Entity entity){
        return entity.getCapability(PlayerCapabilityProvider.CAPABILITY).isPresent();
    }
}
