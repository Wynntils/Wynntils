/*
 * Copyright © Wynntils 2023-2025.
 * This file is released under LGPLv3. See LICENSE for full license details.
 */
package com.wynntils.features.embellishments;

import com.wynntils.core.consumers.features.Feature;
import com.wynntils.core.consumers.features.ProfileDefault;
import com.wynntils.core.persisted.Persisted;
import com.wynntils.core.persisted.config.Category;
import com.wynntils.core.persisted.config.Config;
import com.wynntils.core.persisted.config.ConfigCategory;
import com.wynntils.core.persisted.config.ConfigProfile;
import com.wynntils.core.text.StyledText;
import com.wynntils.handlers.chat.event.ChatMessageEvent;
import com.wynntils.handlers.chat.type.RecipientType;
import com.wynntils.utils.mc.McUtils;
import net.minecraft.resources.ResourceLocation;
import net.minecraft.sounds.SoundEvent;
import net.neoforged.bus.api.SubscribeEvent;

@ConfigCategory(Category.EMBELLISHMENTS)
public class WybelSoundFeature extends Feature {
    private static final ResourceLocation WYBEL_SQUEAK_ID =
            ResourceLocation.fromNamespaceAndPath("wynntils", "wybel.squeak");
    private static final SoundEvent WYBEL_SQUEAK_SOUND = SoundEvent.createVariableRangeEvent(WYBEL_SQUEAK_ID);

    private static final ResourceLocation WYBEL_PURR_ID =
            ResourceLocation.fromNamespaceAndPath("wynntils", "wybel.purr");
    private static final SoundEvent WYBEL_PURR_SOUND = SoundEvent.createVariableRangeEvent(WYBEL_PURR_ID);

    @Persisted
    private final Config<Boolean> hideText = new Config<>(false);

    public WybelSoundFeature() {
        super(new ProfileDefault.Builder()
                .disableFor(
                        ConfigProfile.NEW_PLAYER, ConfigProfile.LITE, ConfigProfile.MINIMAL, ConfigProfile.BLANK_SLATE)
                .build());
    }

    @SubscribeEvent
    public void onChat(ChatMessageEvent.Match event) {
        if (event.getRecipientType() != RecipientType.PETS) return;

        StyledText message = event.getMessage();
        if (message.contains("squeak")) {
            McUtils.playSoundAmbient(WYBEL_SQUEAK_SOUND);
            if (hideText.get()) {
                event.cancelChat();
            }
        }
        if (message.contains("purr")) {
            McUtils.playSoundAmbient(WYBEL_PURR_SOUND);
            if (hideText.get()) {
                event.cancelChat();
            }
        }
    }
}
