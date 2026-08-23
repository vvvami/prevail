package net.vami.prevail.event;

import net.minecraft.resources.ResourceLocation;
import net.minecraft.world.entity.Entity;
import net.minecraft.world.entity.LivingEntity;
import net.minecraft.world.entity.player.Player;
import net.minecraftforge.common.capabilities.RegisterCapabilitiesEvent;
import net.minecraftforge.event.AttachCapabilitiesEvent;
import net.minecraftforge.event.entity.player.PlayerEvent;
import net.minecraftforge.eventbus.api.SubscribeEvent;
import net.minecraftforge.fml.common.Mod;
import net.vami.prevail.Prevail;
import net.vami.prevail.capability.MobCapability;
import net.vami.prevail.capability.MobCapabilityProvider;

public class ModCapabilityRegistryEvents {

    @Mod.EventBusSubscriber(modid = Prevail.MOD_ID, bus = Mod.EventBusSubscriber.Bus.FORGE)
    private static class ForgeCapabilityEvents {
        @SubscribeEvent
        public static void onAttachCapabilities(AttachCapabilitiesEvent<Entity> event) {
            if (!(event.getObject() instanceof LivingEntity)) return;

            MobCapabilityProvider provider = new MobCapabilityProvider();
            event.addCapability(ResourceLocation.tryBuild(Prevail.MOD_ID, "properties"), provider);

            event.addListener(provider::invalidate);
        }

        @SubscribeEvent
        public static void onPlayerCloned(PlayerEvent.Clone event) {
            if (!event.isWasDeath()) return;

            event.getOriginal().reviveCaps();

            event.getOriginal()
                    .getCapability(MobCapabilityProvider.CAPABILITY)
                    .ifPresent(oldData ->
                            event.getEntity()
                                    .getCapability(MobCapabilityProvider.CAPABILITY)
                                    .ifPresent(newData ->
                                            newData.copyFrom(oldData)));

            event.getOriginal().invalidateCaps();
        }
    }

    @Mod.EventBusSubscriber(modid = Prevail.MOD_ID, bus = Mod.EventBusSubscriber.Bus.MOD)
    private static class ModCapabilityEvents {
        @SubscribeEvent
        public static void onRegisterCapabilities(RegisterCapabilitiesEvent event) {
            event.register(MobCapability.class);
        }
    }

}
