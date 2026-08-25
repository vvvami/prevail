package net.vami.prevail.attachment;

import net.minecraft.core.HolderLookup;
import net.minecraft.nbt.CompoundTag;
import net.neoforged.neoforge.common.util.INBTSerializable;

// this is replacing the forge 1.20.1 Capability
// honestly i wish forge had this instead of Capabilities
// it's so much simpler
public class MobData implements INBTSerializable<CompoundTag> {
    // you'll find that these are separated n that's because
    // they actually apply for mobs, not players
    private float poise;
    private int stagger;

    // these below apply to players only
    private float maxHeal;
    private int shieldRush;
    private int shieldRushWait; // no shield rush right after eating
    private boolean chomped;
    private int despair;

    public float getPoise() {
        return poise;
    }

    public void setPoise(float poise) {
        this.poise = poise;
    }

    public int getStagger() {
        return stagger;
    }

    public void setStagger(int stagger) {
        this.stagger = stagger;
    }

    public float getMaxHeal() {
        return maxHeal;
    }

    public void setMaxHeal(float maxHeal) {
        this.maxHeal = maxHeal;
    }

    public int getShieldRush() {
        return shieldRush;
    }

    public void setShieldRush(int shieldRush) {
        this.shieldRush = shieldRush;
    }

    public int getShieldRushWait() {
        return shieldRushWait;
    }

    public void setShieldRushWait(int shieldRushWait) {
        this.shieldRushWait = shieldRushWait;
    }

    public boolean hasChomped() {
        return chomped;
    }

    public void setChomped(boolean chomped) {
        this.chomped = chomped;
    }

    public int getDespair() {
        return despair;
    }

    public void setDespair(int despair) {
        this.despair = despair;
    }

    public void copyFrom(MobData source) {
        this.poise = source.poise;
        this.stagger = source.stagger;

        this.maxHeal = source.maxHeal;
        this.chomped = source.chomped;
        this.shieldRush = source.shieldRush;
        this.shieldRushWait = source.shieldRushWait;
        this.despair = source.despair;
    }

    // this is opposed to our normal capability serializeNBT
    public CompoundTag serializeNBT(HolderLookup.Provider provider) {
        CompoundTag tag = new CompoundTag();

        tag.putFloat("Poise", poise);
        tag.putInt("Stagger", stagger);

        tag.putFloat("MaxHeal", maxHeal);
        tag.putInt("ShieldRush", shieldRush);
        tag.putInt("ShieldRushWait", shieldRushWait);
        tag.putBoolean("Chomped", chomped);
        tag.putInt("Despair", despair);

        return tag;
    }

    // same as the serialize, we add the HolderLookup.Provider thing
    // tho i got no idea what it does.
    public void deserializeNBT(HolderLookup.Provider provider, CompoundTag tag) {
        poise = tag.getFloat("Poise");
        stagger = tag.getInt("Stagger");

        maxHeal = tag.getFloat("MaxHeal");
        shieldRush = tag.getInt("ShieldRush");
        shieldRushWait = tag.getInt("ShieldRushWait");
        chomped = tag.getBoolean("Chomped");
        despair = tag.getInt("Despair");
    }
}