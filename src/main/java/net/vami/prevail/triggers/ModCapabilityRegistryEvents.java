package net.vami.prevail.triggers;

import net.minecraft.resources.ResourceLocation;
import net.minecraft.world.entity.Entity;
import net.minecraft.world.entity.player.Player;
import net.minecraftforge.common.capabilities.RegisterCapabilitiesEvent;
import net.minecraftforge.event.AttachCapabilitiesEvent;
import net.minecraftforge.event.entity.player.PlayerEvent;
import net.minecraftforge.eventbus.api.SubscribeEvent;
import net.minecraftforge.fml.common.Mod;
import net.vami.prevail.Prevail;
import net.vami.prevail.capability.PlayerCapability;
import net.vami.prevail.capability.PlayerCapabilityProvider;

@Mod.EventBusSubscriber(modid = Prevail.MOD_ID)
public class ModCapabilityRegistryEvents {

    // Add capabilities when spawn  //
    @SubscribeEvent
    public static void onAttachCapabilitiesPlayer(AttachCapabilitiesEvent<Entity> event) {
        if(event.getObject() instanceof Player) {
            if(!event.getObject().getCapability(PlayerCapabilityProvider.CAPABILITY).isPresent()) {
                event.addCapability(ResourceLocation.tryBuild(Prevail.MOD_ID, "properties"), new PlayerCapabilityProvider());
            }
        }
    }

    // Add capabilities back on death //
    @SubscribeEvent
    public static void onPlayerCloned(PlayerEvent.Clone event) {
        if(event.isWasDeath()) {
            event.getOriginal().getCapability(PlayerCapabilityProvider.CAPABILITY).ifPresent(oldStore -> {
                event.getEntity().getCapability(PlayerCapabilityProvider.CAPABILITY).ifPresent(newStore -> {
                    newStore.copyFrom(oldStore);
                });
            });
        }
    }

    // Register player capabilities //
    @SubscribeEvent
    public static void onRegisterCapabilities(RegisterCapabilitiesEvent event) {
        event.register(PlayerCapability.class);
    }
}
