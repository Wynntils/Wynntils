/*
 * Copyright © Wynntils 2022-2023.
 * This file is released under AGPLv3. See LICENSE for full license details.
 */
package com.wynntils.features.combat;

import com.wynntils.core.components.Models;
import com.wynntils.core.config.Category;
import com.wynntils.core.config.Config;
import com.wynntils.core.config.ConfigCategory;
import com.wynntils.core.config.RegisterConfig;
import com.wynntils.core.consumers.features.Feature;
import com.wynntils.mc.event.SetSpawnEvent;
import com.wynntils.models.activities.event.ActivityTrackerUpdatedEvent;
import com.wynntils.utils.mc.McUtils;
import com.wynntils.utils.mc.type.Location;
import net.minecraft.resources.ResourceLocation;
import net.minecraft.sounds.SoundEvent;
import net.minecraftforge.eventbus.api.SubscribeEvent;

@ConfigCategory(Category.COMBAT)
public class ContentTrackerFeature extends Feature {
    private static final ResourceLocation TRACKER_UPDATE_ID = new ResourceLocation("wynntils:ui.tracker.update");
    private static final SoundEvent TRACKER_UPDATE_SOUND = SoundEvent.createVariableRangeEvent(TRACKER_UPDATE_ID);

    @RegisterConfig
    public final Config<Boolean> autoTrackCoordinates = new Config<>(true);

    @RegisterConfig
    public final Config<Boolean> playSoundOnUpdate = new Config<>(true);

    @SubscribeEvent
    public void onTrackerUpdate(ActivityTrackerUpdatedEvent event) {
        if (autoTrackCoordinates.get()) {
            Location trackedLocation = Models.Activity.getTrackedLocation();

            // Ideally, we would clear the compass if we do not have a
            // tracked location. However, the event is not reliable, since
            // it is sent multiple times with partial lore. So
            // trackedLocation == null might also just indicate that we
            // failed to parse, not that the quest is missing a location.
            if (trackedLocation != null) {
                Models.Compass.setCompassLocation(trackedLocation);
            }
        }

        if (playSoundOnUpdate.get()) {
            McUtils.playSoundUI(TRACKER_UPDATE_SOUND);
        }
    }

    @SubscribeEvent
    public void onSetSpawn(SetSpawnEvent e) {
        if (!autoTrackCoordinates.get()) return;

        Models.Compass.setCompassToSpawnTracker();
    }
}
