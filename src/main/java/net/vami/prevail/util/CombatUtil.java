package net.vami.prevail.util;

import net.minecraft.world.damagesource.DamageSource;
import net.minecraft.world.damagesource.DamageType;
import net.minecraft.world.entity.Entity;
import net.minecraft.world.entity.LivingEntity;
import net.minecraft.world.entity.player.Player;
import net.minecraftforge.event.entity.living.LivingAttackEvent;
import net.minecraftforge.event.entity.living.LivingHurtEvent;
import net.minecraftforge.event.entity.player.AttackEntityEvent;
import net.vami.prevail.ModTags;

public class CombatUtil {
    public static final float SWEEP = 0.848F;
    public static boolean isMelee(DamageSource source, LivingEntity target) {

        return source.is(ModTags.DamageTypes.MELEE)
                && source.getDirectEntity() == source.getEntity()
                && !source.isIndirect();
    }

    public static boolean isMelee(LivingHurtEvent event) {
        DamageSource source = event.getSource();
        LivingEntity target = event.getEntity();

        return source.is(ModTags.DamageTypes.MELEE)
                && source.getDirectEntity() == source.getEntity()
                && !source.isIndirect();
    }

    public static boolean isMelee(LivingAttackEvent event) {
        DamageSource source = event.getSource();
        LivingEntity target = event.getEntity();

        return source.is(ModTags.DamageTypes.MELEE)
                && source.getDirectEntity() == source.getEntity()
                && !source.isIndirect();
    }

    public static boolean isSweep(Player player) {
        return player.getAttackStrengthScale(0.5f) > SWEEP;
    }
}
