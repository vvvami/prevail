package net.vami.prevail.event;

import net.minecraft.world.entity.LivingEntity;
import net.minecraftforge.event.entity.living.LivingEvent;
import net.minecraftforge.eventbus.api.Cancelable;
import net.minecraftforge.fml.event.IModBusEvent;

@Cancelable
public class MaxHealDecreaseEvent extends LivingEvent {
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
