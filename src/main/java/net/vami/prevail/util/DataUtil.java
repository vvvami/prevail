package net.vami.prevail.util;

import net.minecraft.world.entity.Entity;
import net.vami.prevail.attachment.MobData;
import net.vami.prevail.attachment.ModAttachments;

public class DataUtil {

    public static MobData getData(Entity entity) {
        return entity.getData(ModAttachments.MOB);
    }
}