/*
 * Copyright © Wynntils 2024.
 * This file is released under LGPLv3. See LICENSE for full license details.
 */
package com.wynntils.features.embellishments;

import com.wynntils.core.consumers.features.Feature;
import com.wynntils.core.persisted.config.Category;
import com.wynntils.core.persisted.config.ConfigCategory;
import com.wynntils.models.territories.event.GuildWarQueuedEvent;
import com.wynntils.utils.mc.McUtils;
import net.minecraft.resources.ResourceLocation;
import net.minecraft.sounds.SoundEvent;
import net.minecraftforge.eventbus.api.SubscribeEvent;

@ConfigCategory(Category.EMBELLISHMENTS)
public class WarHornFeature extends Feature {
    private static final ResourceLocation WAR_HORN_ID = new ResourceLocation("wynntils:war.horn");
    private static final SoundEvent WAR_HORN_SOUND = SoundEvent.createVariableRangeEvent(WAR_HORN_ID);

    @SubscribeEvent
    public void onWarQueued(GuildWarQueuedEvent event) {
        McUtils.playSoundAmbient(WAR_HORN_SOUND);
    }
}
