package net.vami.prevail.capability;

import net.minecraft.nbt.CompoundTag;
import net.minecraft.world.item.ItemStack;

import java.util.ArrayList;
import java.util.Collections;

public class PlayerCapability {

    public CapabilityContainer<Float> maxHeal
            = new CapabilityContainer<>(0f);
    public CapabilityContainer<Integer> shieldRush
            = new CapabilityContainer<>(0);
    public CapabilityContainer<Boolean> chomped
            = new CapabilityContainer<>(false);
    public CapabilityContainer<Integer> despair
            = new CapabilityContainer<>(0);
    // If you want to add a new capability, Add it to copyfrom, saveNBTdata and LoadNBTdata
    // These only fire when the capabilities are registered in mod events

    public void copyFrom(PlayerCapability source){
        this.maxHeal = source.maxHeal;
        this.chomped = source.chomped;
        this.shieldRush = source.shieldRush;
        this.despair = source.despair;
    }

    public void SaveNBTData(CompoundTag nbt) {
        nbt.putFloat("maxHeal", maxHeal.get());
        nbt.putBoolean("chomped", chomped.get());
        nbt.putInt("shieldRush", shieldRush.get());
        nbt.putInt("despair", despair.get());
    }

    public void loadNBTData(CompoundTag nbt) {
        maxHeal.set(nbt.getFloat("maxHeal"));
        chomped.set(nbt.getBoolean("chomped"));
        shieldRush.set(nbt.getInt("shieldRush"));
        despair.set(nbt.getInt("despair"));
    }
}
