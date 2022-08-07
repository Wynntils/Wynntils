/*
 * Copyright © Wynntils 2022.
 * This file is released under AGPLv3. See LICENSE for full license details.
 */
package com.wynntils.mc.utils;

import com.mojang.blaze3d.platform.Window;
import net.minecraft.Util;
import net.minecraft.client.Minecraft;
import net.minecraft.client.Options;
import net.minecraft.client.player.LocalPlayer;
import net.minecraft.network.chat.Component;
import net.minecraft.network.protocol.Packet;
import net.minecraft.world.entity.player.Inventory;
import net.minecraft.world.inventory.AbstractContainerMenu;
import net.minecraft.world.inventory.InventoryMenu;

/**
 * This is a "high-quality misc" class for Minecraft utilities without an aspect on Wynntils
 * commonly used. Tags used more often should be moved elsewhere Keep the names short, but distinct.
 */
public final class McUtils {
    private McUtils() {}

    public static Minecraft mc() {
        return Minecraft.getInstance();
    }

    public static LocalPlayer player() {
        return mc().player;
    }

    public static Options options() {
        return mc().options;
    }

    public static InventoryMenu inventoryMenu() {
        return player().inventoryMenu;
    }

    public static AbstractContainerMenu containerMenu() {
        return player().containerMenu;
    }

    public static Inventory inventory() {
        return player().getInventory();
    }

    public static Window window() {
        return mc().getWindow();
    }

    public static void sendMessageToClient(Component component) {
        player().sendMessage(component, Util.NIL_UUID);
    }

    public static void sendPacket(Packet<?> packet) {
        mc().getConnection().send(packet);
    }
}
