/*
 * Copyright © Wynntils 2023.
 * This file is released under AGPLv3. See LICENSE for full license details.
 */
package com.wynntils.features.embellishments;

import com.wynntils.core.features.Feature;
import com.wynntils.models.containers.event.MythicFoundEvent;
import com.wynntils.utils.mc.McUtils;
import net.minecraft.resources.ResourceLocation;
import net.minecraft.sounds.SoundEvent;
import net.minecraftforge.eventbus.api.SubscribeEvent;

public class MythicFoundSoundFeature extends Feature {
    private static final ResourceLocation MYTHIC_FOUND_ID = new ResourceLocation("wynntils:misc.mythic-found");
    private static final SoundEvent MYTHIC_FOUND_SOUND = SoundEvent.createVariableRangeEvent(MYTHIC_FOUND_ID);

    @SubscribeEvent
    public void onMythicFound(MythicFoundEvent event) {
        McUtils.playSoundAmbient(MYTHIC_FOUND_SOUND);
    }
}
