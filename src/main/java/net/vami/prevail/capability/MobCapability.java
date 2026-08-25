package net.vami.prevail.capability;

import net.minecraft.nbt.CompoundTag;

public class MobCapability {
    // youll find that these are separated n that's because
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

    public void copyFrom(MobCapability source) {
        this.poise = source.poise;
        this.stagger = source.stagger;

        this.maxHeal = source.maxHeal;
        this.chomped = source.chomped;
        this.shieldRush = source.shieldRush;
        this.shieldRushWait = source.shieldRushWait;
        this.despair = source.despair;
    }

    public CompoundTag serializeNBT() {
        CompoundTag tag = new CompoundTag();

        tag.putFloat("Poise", poise);
        tag.putInt("Stagger", stagger);

        tag.putFloat("MaxHeal", maxHeal);
        tag.putInt("ShieldRush", shieldRush);
        tag.putInt("ShieldRushWait", shieldRush);
        tag.putBoolean("Chomped", chomped);
        tag.putInt("Despair", despair);

        return tag;
    }

    public void deserializeNBT(CompoundTag tag) {
        poise = tag.getFloat("Poise");
        stagger = tag.getInt("Stagger");

        maxHeal = tag.getFloat("MaxHeal");
        shieldRush = tag.getInt("ShieldRush");
        shieldRushWait = tag.getInt("ShieldRushWait");
        chomped = tag.getBoolean("Chomped");
        despair = tag.getInt("Despair");
    }
}