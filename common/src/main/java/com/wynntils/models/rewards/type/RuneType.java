/*
 * Copyright © Wynntils 2026.
 * This file is released under LGPLv3. See LICENSE for full license details.
 */
package com.wynntils.models.rewards.type;

import com.wynntils.utils.colors.CustomColor;
import net.minecraft.ChatFormatting;

public enum RuneType {
    AZ(CustomColor.fromChatFormatting(ChatFormatting.AQUA)),
    NII(CustomColor.fromChatFormatting(ChatFormatting.DARK_RED)),
    UTH(CustomColor.fromChatFormatting(ChatFormatting.DARK_AQUA)),
    TOL(CustomColor.fromChatFormatting(ChatFormatting.DARK_GREEN));

    private CustomColor color;

    RuneType(CustomColor color) {
        this.color = color;
    }

    public CustomColor getColor() {
        return color;
    }
}
