package net.vami.prevail;

import net.minecraft.core.registries.BuiltInRegistries;
import net.minecraft.resources.ResourceLocation;
import net.neoforged.neoforge.common.ModConfigSpec;

// An example config class. This is not required, but it's a good idea to have one to keep your config organized.
// Demonstrates how to use Neo's config APIs
public class PrevailConfig {
    private static final ModConfigSpec.Builder BUILDER = new ModConfigSpec.Builder();

    public static final ModConfigSpec.BooleanValue MAXHEAL_ENABLE = BUILDER
            .comment("Enable/Disable the Maxheal mechanic " +
                    "\nMaxheal is a 'permanent damage' mechanic that can only be fully healed by potions and such")
            .define("enableMaxheal", true);


    public static final ModConfigSpec.IntValue DESPAIR_THRESHOLD = BUILDER
            .comment("The number of despair stacks needed to reach despair mode " +
                    "\nDespair prevents all melee damage to stop spamming")
            .defineInRange("despairThreshold", 7, 0, Integer.MAX_VALUE);

    public static final ModConfigSpec.IntValue DESPAIR_BUILDUP = BUILDER
            .comment("The number of despair stacks added PER SPAM ATTACK " +
                    "\nDespair prevents all melee damage to stop spamming")
            .defineInRange("despairBuildup", 1, 0, Integer.MAX_VALUE);

    public static final ModConfigSpec.IntValue DESPAIR_SHIELD_BUILDUP = BUILDER
            .comment("The number of despair stacks added per spam attack WITH A SHIELD" +
                    "\nDespair prevents all melee damage to stop spamming")
            .defineInRange("despairShieldBuildup", 2, 0, Integer.MAX_VALUE);

    public static final ModConfigSpec.IntValue DESPAIR_SWEEP_REDUCTION = BUILDER
            .comment("The number of despair stacks removed PER SWEEP ATTACK " +
                    "\nDespair prevents all melee damage to stop spamming")
            .defineInRange("despairSweepReduction", 2, 0, Integer.MAX_VALUE);

    public static final ModConfigSpec.IntValue DESPAIR_FADE = BUILDER
            .comment("The number of despair stacks removed PER SECOND " +
                    "\nDespair prevents all melee damage to stop spamming")
            .defineInRange("despairFade", 1, 0, Integer.MAX_VALUE);

    public static final ModConfigSpec.DoubleValue REACH_DIST_FALLOFF = BUILDER
            .comment("The range ratio after which distance damage takes effect " +
                    "\nHigher value means less falloff")
            .defineInRange("reachDistFalloff", 0.65, 0, 1);

    static final ModConfigSpec SPEC = BUILDER.build();

    private static boolean validateItemName(final Object obj) {
        return obj instanceof String itemName && BuiltInRegistries.ITEM.containsKey(ResourceLocation.parse(itemName));
    }
}
