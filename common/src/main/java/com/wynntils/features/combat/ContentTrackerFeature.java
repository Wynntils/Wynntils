/*
 * Copyright © Wynntils 2022-2023.
 * This file is released under LGPLv3. See LICENSE for full license details.
 */
package com.wynntils.features.combat;

import com.wynntils.core.consumers.features.Feature;
import com.wynntils.core.persisted.Persisted;
import com.wynntils.core.persisted.config.Category;
import com.wynntils.core.persisted.config.Config;
import com.wynntils.core.persisted.config.ConfigCategory;
import com.wynntils.models.activities.event.ActivityTrackerUpdatedEvent;
import com.wynntils.utils.mc.McUtils;
import net.minecraft.resources.ResourceLocation;
import net.minecraft.sounds.SoundEvent;
import net.minecraftforge.eventbus.api.SubscribeEvent;

@ConfigCategory(Category.COMBAT)
public class ContentTrackerFeature extends Feature {
    private static final ResourceLocation TRACKER_UPDATE_ID = new ResourceLocation("wynntils:ui.tracker.update");
    private static final SoundEvent TRACKER_UPDATE_SOUND = SoundEvent.createVariableRangeEvent(TRACKER_UPDATE_ID);

    @Persisted
    public final Config<Boolean> autoTrackCoordinates = new Config<>(true);

    @Persisted
    public final Config<Boolean> playSoundOnUpdate = new Config<>(true);

    @SubscribeEvent
    public void onTrackerUpdate(ActivityTrackerUpdatedEvent event) {
        if (playSoundOnUpdate.get()) {
            McUtils.playSoundUI(TRACKER_UPDATE_SOUND);
        }
    }
}
