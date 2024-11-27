/*
 * Copyright © Endernon 2024.
 * This file is released under LGPLv3. See LICENSE for full license details.
 */
package com.endernon.custom;
import net.minecraft.ChatFormatting;


public class ColorMatch {
    public static ChatFormatting ColorMatcher(String MatcherType) {
        String RealMatcherType = MatcherType.toUpperCase();
        if (RealMatcherType == "BLACK") {
            return ChatFormatting.BLACK;
        }
        if (RealMatcherType == "DARK_BLUE") {
            return ChatFormatting.DARK_BLUE;
        }
        if (RealMatcherType == "DARK_GREEN") {
            return ChatFormatting.DARK_GREEN;
        }
        if (RealMatcherType == "DARK_AQUA") {
            return ChatFormatting.DARK_AQUA;
        }
        if (RealMatcherType == "DARK_RED") {
            return ChatFormatting.DARK_RED;
        }
        if (RealMatcherType == "DARK_PURPLE") {
            return ChatFormatting.DARK_PURPLE;
        }
        if (RealMatcherType == "GOLD") {
            return ChatFormatting.GOLD;
        }
        if (RealMatcherType == "GRAY") {
            return ChatFormatting.GRAY;
        }
        if (RealMatcherType == "DARK_GRAY") {
            return ChatFormatting.DARK_GRAY;
        }
        if (RealMatcherType == "BLUE") {
            return ChatFormatting.BLUE;
        }
        if (RealMatcherType == "GREEN") {
            return ChatFormatting.GREEN;
        }
        if (RealMatcherType == "AQUA") {
            return ChatFormatting.AQUA;
        }
        if (RealMatcherType == "RED") {
            return ChatFormatting.RED;
        }
        if (RealMatcherType == "LIGHT_PURPLE") {
            return ChatFormatting.LIGHT_PURPLE;
        }
        if (RealMatcherType == "YELLOW") {
            return ChatFormatting.YELLOW;
        }
        if (RealMatcherType == "WHITE") {
            return ChatFormatting.WHITE;
        }
        if (RealMatcherType == "OBFUSCATED") {
            return ChatFormatting.OBFUSCATED;
        }
        if (RealMatcherType == "BOLD") {
            return ChatFormatting.BOLD;
        }
        if (RealMatcherType == "STRIKETHROUGH") {
            return ChatFormatting.STRIKETHROUGH;
        }
        if (RealMatcherType == "UNDERLINE") {
            return ChatFormatting.UNDERLINE;
        }
        if (RealMatcherType == "ITALIC") {
            return ChatFormatting.ITALIC;
        }
        if (RealMatcherType == "RESET") {
            return ChatFormatting.RESET;
        }
        return ChatFormatting.DARK_PURPLE; // fallback
    }
}
