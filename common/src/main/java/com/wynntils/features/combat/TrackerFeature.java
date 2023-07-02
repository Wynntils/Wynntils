/*
 * Copyright © Wynntils 2022.
 * This file is released under AGPLv3. See LICENSE for full license details.
 */
package com.wynntils.features.combat;

import com.wynntils.core.components.Models;
import com.wynntils.core.config.Category;
import com.wynntils.core.config.Config;
import com.wynntils.core.config.ConfigCategory;
import com.wynntils.core.config.RegisterConfig;
import com.wynntils.core.features.Feature;
import com.wynntils.models.tracker.event.TrackerUpdatedEvent;
import com.wynntils.utils.mc.McUtils;
import net.minecraft.resources.ResourceLocation;
import net.minecraft.sounds.SoundEvent;
import net.minecraftforge.eventbus.api.SubscribeEvent;

@ConfigCategory(Category.COMBAT)
public class TrackerFeature extends Feature {
    private static final ResourceLocation TRACKER_UPDATE_ID = new ResourceLocation("wynntils:ui.tracker.update");
    private static final SoundEvent TRACKER_UPDATE_SOUND = SoundEvent.createVariableRangeEvent(TRACKER_UPDATE_ID);

    @RegisterConfig
    public final Config<Boolean> autoTrackCoordinates = new Config<>(true);

    @RegisterConfig
    public final Config<Boolean> playSoundOnUpdate = new Config<>(true);

    @SubscribeEvent
    public void onTrackerUpdate(TrackerUpdatedEvent event) {
        if (event.getQuestInfo() == null) return;

        if (autoTrackCoordinates.get()) {
            Models.Compass.setDynamicCompassLocation(Models.Quest::getTrackedQuestNextLocation);
        }

        if (playSoundOnUpdate.get()) {
            McUtils.playSoundUI(TRACKER_UPDATE_SOUND);
        }
    }
}
