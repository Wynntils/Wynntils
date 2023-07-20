/*
 * Copyright © Wynntils 2023.
 * This file is released under AGPLv3. See LICENSE for full license details.
 */
package com.wynntils.features;

import com.wynntils.core.components.Models;
import com.wynntils.core.config.Config;
import com.wynntils.core.features.Feature;
import com.wynntils.models.containers.event.MythicFoundEvent;
import com.wynntils.utils.mc.McUtils;
import net.minecraft.ChatFormatting;
import net.minecraft.network.chat.Component;
import net.minecraft.resources.ResourceLocation;
import net.minecraft.sounds.SoundEvent;
import net.minecraftforge.eventbus.api.SubscribeEvent;

public class MythicFoundFeature extends Feature {
    private static final ResourceLocation MYTHIC_FOUND_ID = new ResourceLocation("wynntils:misc.mythic-found");
    private static final SoundEvent MYTHIC_FOUND_SOUND = SoundEvent.createVariableRangeEvent(MYTHIC_FOUND_ID);

    private final Config<Boolean> playSound = new Config<>(true);
    private final Config<Boolean> showDryStreakMessage = new Config<>(true);

    @SubscribeEvent
    public void onMythicFound(MythicFoundEvent event) {
        if (playSound.get()) {
            McUtils.playSoundAmbient(MYTHIC_FOUND_SOUND);
        }

        if (showDryStreakMessage.get()) {
            McUtils.sendMessageToClient(Component.literal("Dry streak broken! Found a ")
                    .withStyle(ChatFormatting.LIGHT_PURPLE)
                    .append(event.getMythicBoxItem().getHoverName())
                    .append(Component.literal(" in chest ")
                            .withStyle(ChatFormatting.LIGHT_PURPLE)
                            .append(Component.literal("#" + Models.LootChest.getChestCount())
                                    .withStyle(ChatFormatting.GOLD)))
                    .append(Component.literal(" after ")
                            .withStyle(ChatFormatting.LIGHT_PURPLE)
                            .append(Component.literal(String.valueOf(Models.LootChest.getDryCount()))
                                    .withStyle(ChatFormatting.DARK_PURPLE)))
                    .append(Component.literal(" dry chests and "))
                    .append(Component.literal(String.valueOf(Models.LootChest.getDryBoxes()))
                            .withStyle(ChatFormatting.DARK_PURPLE))
                    .append(Component.literal(" dry boxes.")));
        }
    }
}
