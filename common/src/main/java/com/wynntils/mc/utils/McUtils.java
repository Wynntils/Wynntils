/*
 * Copyright © Wynntils 2022.
 * This file is released under AGPLv3. See LICENSE for full license details.
 */
package com.wynntils.mc.utils;

import net.minecraft.Util;
import net.minecraft.client.Minecraft;
import net.minecraft.client.Options;
import net.minecraft.client.player.LocalPlayer;
import net.minecraft.network.chat.Component;
import net.minecraft.world.entity.player.Inventory;
import net.minecraft.world.inventory.AbstractContainerMenu;
import net.minecraft.world.inventory.InventoryMenu;

/**
 * This is a "high-quality misc" class for Minecraft utilities without an aspect on Wynntils
 * commonly used. Tags used more often should be moved elsewhere Keep the names short, but distinct.
 */
public class McUtils {
    public static Minecraft mc() {
        return Minecraft.getInstance();
    }

    public static LocalPlayer player() {
        return Minecraft.getInstance().player;
    }

    public static Options options() {
        return Minecraft.getInstance().options;
    }

    public static InventoryMenu inventoryMenu() {
        return Minecraft.getInstance().player.inventoryMenu;
    }

    public static AbstractContainerMenu containerMenu() {
        return Minecraft.getInstance().player.containerMenu;
    }

    public static Inventory inventory() {
        return Minecraft.getInstance().player.getInventory();
    }

    public static void sendMessageToClient(Component component) {
        Minecraft.getInstance().player.sendMessage(component, Util.NIL_UUID);
    }
}
