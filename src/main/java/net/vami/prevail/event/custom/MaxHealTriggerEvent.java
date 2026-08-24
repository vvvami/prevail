package net.vami.prevail.event.custom;

import net.minecraft.world.entity.LivingEntity;
import net.neoforged.bus.api.ICancellableEvent;
import net.neoforged.neoforge.event.entity.living.LivingEvent;

// so we implement ICancellable instead of doing @Cancellable
public class MaxHealTriggerEvent extends LivingEvent implements ICancellableEvent {
    public MaxHealTriggerEvent(LivingEntity entity) {
        super(entity);
    }
}
