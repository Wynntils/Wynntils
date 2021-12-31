/*
 * Copyright © Wynntils 2021.
 * This file is released under AGPLv3. See LICENSE for full license details.
 */
package com.wynntils.utils;

import com.wynntils.WynntilsMod;
import com.wynntils.wc.Models;
import net.minecraft.client.Minecraft;
import net.minecraft.network.chat.Component;
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
}
