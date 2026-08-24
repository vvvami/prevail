package net.vami.prevail.event.custom;

import net.minecraft.world.entity.LivingEntity;
import net.neoforged.bus.api.ICancellableEvent;
import net.neoforged.neoforge.event.entity.living.LivingEvent;

// so we implement ICancellable instead of doing @Cancellable
public class MaxHealDecreaseEvent extends LivingEvent implements ICancellableEvent {
    private float amount = 0;
    public MaxHealDecreaseEvent(LivingEntity entity, float amount) {
        super(entity);
        this.amount = amount;
    }

    public float getAmount() {
        return amount;
    }

    public void setAmount(float amount) {
        this.amount = amount;
    }
}
