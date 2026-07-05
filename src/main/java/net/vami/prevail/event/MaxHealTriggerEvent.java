package net.vami.prevail.event;

import net.minecraft.world.entity.LivingEntity;
import net.minecraftforge.event.entity.living.LivingEvent;
import net.minecraftforge.eventbus.api.Cancelable;

@Cancelable
public class MaxHealTriggerEvent extends LivingEvent {
    public MaxHealTriggerEvent(LivingEntity entity) {
        super(entity);
    }
}
