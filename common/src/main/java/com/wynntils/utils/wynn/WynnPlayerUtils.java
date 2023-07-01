/*
 * Copyright © Wynntils 2022.
 * This file is released under AGPLv3. See LICENSE for full license details.
 */
package com.wynntils.utils.wynn;

import net.minecraft.ChatFormatting;

public final class WynnPlayerUtils {
    public static String getFormattedRank(String rank) {
        ChatFormatting primaryColor;
        ChatFormatting secondaryColor;

        switch (rank) {
            case "VIP" -> {
                primaryColor = ChatFormatting.DARK_GREEN;
                secondaryColor = ChatFormatting.GREEN;
            }
            case "VIP+" -> {
                primaryColor = ChatFormatting.DARK_AQUA;
                secondaryColor = ChatFormatting.AQUA;
            }
            case "HERO" -> {
                primaryColor = ChatFormatting.DARK_PURPLE;
                secondaryColor = ChatFormatting.LIGHT_PURPLE;
            }
            case "CHAMPION" -> {
                primaryColor = ChatFormatting.YELLOW;
                secondaryColor = ChatFormatting.GOLD;
            }
            case "Mod" -> {
                primaryColor = ChatFormatting.GOLD;
                secondaryColor = ChatFormatting.YELLOW;
            }
            case "YT" -> {
                primaryColor = ChatFormatting.LIGHT_PURPLE;
                secondaryColor = ChatFormatting.DARK_PURPLE;
            }
            case "Build", "Art", "Item" -> {
                primaryColor = ChatFormatting.AQUA;
                secondaryColor = ChatFormatting.DARK_AQUA;
            }
            case "Admin" -> {
                primaryColor = ChatFormatting.RED;
                secondaryColor = ChatFormatting.DARK_RED;
            }
            default -> {
                // Should not happen
                primaryColor = ChatFormatting.DARK_GRAY;
                secondaryColor = ChatFormatting.DARK_GRAY;
            }
        }

        return primaryColor + "[" + secondaryColor + rank + primaryColor + "] " + secondaryColor;
    }
}
