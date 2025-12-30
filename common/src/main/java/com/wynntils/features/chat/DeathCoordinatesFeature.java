/*
 * Copyright © Wynntils 2023-2025.
 * This file is released under LGPLv3. See LICENSE for full license details.
 */
package com.wynntils.features.chat;

import com.wynntils.core.consumers.features.Feature;
import com.wynntils.core.consumers.features.ProfileDefault;
import com.wynntils.core.persisted.config.Category;
import com.wynntils.core.persisted.config.ConfigCategory;
import com.wynntils.core.persisted.config.ConfigProfile;
import com.wynntils.core.text.StyledText;
import com.wynntils.models.character.event.CharacterDeathEvent;
import com.wynntils.utils.mc.McUtils;
import com.wynntils.utils.mc.StyledTextUtils;
import net.minecraft.ChatFormatting;
import net.minecraft.network.chat.Component;
import net.neoforged.bus.api.SubscribeEvent;

@ConfigCategory(Category.CHAT)
public class DeathCoordinatesFeature extends Feature {
    public DeathCoordinatesFeature() {
        super(new ProfileDefault.Builder()
                .disableFor(ConfigProfile.LITE, ConfigProfile.MINIMAL, ConfigProfile.BLANK_SLATE)
                .build());
    }

    @SubscribeEvent
    public void onCharacterDeath(CharacterDeathEvent e) {
        StyledText deathMessage =
                StyledText.fromComponent(Component.translatable("feature.wynntils.deathCoordinates.diedAt")
                        .withStyle(ChatFormatting.DARK_RED));
        deathMessage = deathMessage.appendPart(StyledTextUtils.createLocationPart(e.getLocation()));

        McUtils.sendMessageToClient(deathMessage.getComponent());
    }
}
