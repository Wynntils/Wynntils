/*
 * Copyright © Wynntils 2023.
 * This file is released under AGPLv3. See LICENSE for full license details.
 */
package com.wynntils.features.chat;

import com.wynntils.core.config.Category;
import com.wynntils.core.config.ConfigCategory;
import com.wynntils.core.features.Feature;
import com.wynntils.models.character.event.CharacterDeathEvent;
import com.wynntils.utils.mc.ComponentUtils;
import com.wynntils.utils.mc.McUtils;
import net.minecraft.ChatFormatting;
import net.minecraft.network.chat.Component;
import net.minecraft.network.chat.MutableComponent;
import net.minecraftforge.eventbus.api.SubscribeEvent;

@ConfigCategory(Category.CHAT)
public class DeathCoordinatesFeature extends Feature {

    @SubscribeEvent
    public void onCharacterDeath(CharacterDeathEvent e) {

        MutableComponent deathMessage = Component.translatable("feature.wynntils.deathCoordinates.diedAt")
                .withStyle(ChatFormatting.DARK_RED);
        Component coordMessage = ComponentUtils.createLocationComponent(e.getLocation());

        McUtils.player().sendSystemMessage(deathMessage.append(coordMessage));
    }
}
