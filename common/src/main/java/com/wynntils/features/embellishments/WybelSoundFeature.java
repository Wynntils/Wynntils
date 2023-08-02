/*
 * Copyright © Wynntils 2023.
 * This file is released under AGPLv3. See LICENSE for full license details.
 */
package com.wynntils.features.embellishments;

import com.wynntils.core.config.Category;
import com.wynntils.core.config.Config;
import com.wynntils.core.config.ConfigCategory;
import com.wynntils.core.consumers.features.Feature;
import com.wynntils.core.persisted.Persisted;
import com.wynntils.core.text.StyledText;
import com.wynntils.handlers.chat.event.ChatMessageReceivedEvent;
import com.wynntils.handlers.chat.type.RecipientType;
import com.wynntils.utils.mc.McUtils;
import net.minecraft.resources.ResourceLocation;
import net.minecraft.sounds.SoundEvent;
import net.minecraftforge.eventbus.api.SubscribeEvent;

@ConfigCategory(Category.EMBELLISHMENTS)
public class WybelSoundFeature extends Feature {
    private static final ResourceLocation WYBEL_SQUEAK_ID = new ResourceLocation("wynntils:wybel.squeak");
    private static final SoundEvent WYBEL_SQUEAK_SOUND = SoundEvent.createVariableRangeEvent(WYBEL_SQUEAK_ID);

    private static final ResourceLocation WYBEL_PURR_ID = new ResourceLocation("wynntils:wybel.purr");
    private static final SoundEvent WYBEL_PURR_SOUND = SoundEvent.createVariableRangeEvent(WYBEL_PURR_ID);

    @Persisted
    public final Config<Boolean> hideText = new Config<>(false);

    @SubscribeEvent
    public void onChat(ChatMessageReceivedEvent event) {
        if (event.getRecipientType() != RecipientType.PETS) return;

        StyledText msg = event.getStyledText();
        if (msg.contains("squeak")) {
            McUtils.playSoundAmbient(WYBEL_SQUEAK_SOUND);
            if (hideText.get()) {
                event.setCanceled(true);
            }
        }
        if (msg.contains("purr")) {
            McUtils.playSoundAmbient(WYBEL_PURR_SOUND);
            if (hideText.get()) {
                event.setCanceled(true);
            }
        }
    }
}
