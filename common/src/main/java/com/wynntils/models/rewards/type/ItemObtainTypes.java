/*
 * Copyright © Wynntils 2026.
 * This file is released under LGPLv3. See LICENSE for full license details.
 */
package com.wynntils.models.rewards.type;

import java.util.EnumSet;
import net.minecraft.ChatFormatting;
import net.minecraft.network.chat.Component;
import net.minecraft.network.chat.MutableComponent;

public final class ItemObtainTypes {
    public static MutableComponent component(EnumSet<ItemObtainType> itemObtainTypes) {
        MutableComponent component = Component.translatable("screens.wynntils.wynntilsGuides.obtain.prefix")
                .withStyle(ChatFormatting.DARK_GRAY);

        boolean firstPass = true;
        for (ItemObtainType itemObtainType : itemObtainTypes) {
            if (!firstPass) {
                component = component.append(Component.literal(", ").withStyle(ChatFormatting.AQUA));
            }

            component = component.append(itemObtainType.getTranslatedComponent().withStyle(ChatFormatting.AQUA));
            firstPass = false;
        }

        return component;
    }
}
