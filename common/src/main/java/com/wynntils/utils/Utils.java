/*
 * Copyright © Wynntils 2021.
 * This file is released under AGPLv3. See LICENSE for full license details.
 */
package com.wynntils.utils;

import com.wynntils.core.WynntilsMod;
import com.wynntils.wc.Models;
import java.util.Arrays;
import java.util.Optional;
import net.minecraft.ChatFormatting;
import net.minecraft.client.Minecraft;
import net.minecraft.network.chat.Component;
import net.minecraft.network.chat.Style;
import net.minecraftforge.eventbus.api.IEventBus;

/**
 * This is a "high-quality misc" class. Helper methods that are commonly used throughout the project
 * can be put here. Keep the names short, but distinct.
 */
public class Utils {
    public static IEventBus getEventBus() {
        return WynntilsMod.getEventBus();
    }

    public static boolean onServer() {
        return Models.getWorldState().onServer();
    }

    public static boolean onWorld() {
        return Models.getWorldState().onWorld();
    }

    public static void logUnknown(String msg, Object obj) {
        System.out.println("Could not handle input from Wynncraft " + msg);
        System.out.println(obj);
    }

    public static String getUnformatted(Component msg) {
        return msg.getString();
    }

    public static Minecraft mc() {
        return Minecraft.getInstance();
    }

    public static String fromComponent(Component component) {
        StringBuilder result = new StringBuilder();

        //Really naive but more of a PoC than anything
        component.visit((style, string) -> {
           // result.append(ChatFormatting.RESET); //reset
            if (style.getColor() != null) {
                Optional<ChatFormatting> color = Arrays.stream(ChatFormatting.values()).filter(c -> c.isColor() && style.getColor().getValue() == c.getColor()).findFirst();
                if (color.isPresent()) result.append(color.get());
            }

            if (style.isBold()) result.append(ChatFormatting.BOLD);
            if (style.isItalic()) result.append(ChatFormatting.ITALIC);
            if (style.isUnderlined()) result.append(ChatFormatting.UNDERLINE);
            if (style.isStrikethrough()) result.append(ChatFormatting.STRIKETHROUGH);
            if (style.isObfuscated()) result.append(ChatFormatting.OBFUSCATED);

            result.append(string);

            return Optional.empty(); //dont break
        }, Style.EMPTY);

        return result.toString();
    }
}
