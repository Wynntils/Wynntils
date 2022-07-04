/*
 * Copyright © Wynntils 2022.
 * This file is released under AGPLv3. See LICENSE for full license details.
 */
package com.wynntils.mc.utils;

import com.mojang.blaze3d.platform.Window;
import net.minecraft.Util;
import net.minecraft.client.Camera;
import net.minecraft.client.Minecraft;
import net.minecraft.client.Options;
import net.minecraft.client.gui.Font;
import net.minecraft.client.gui.Gui;
import net.minecraft.client.gui.screens.Screen;
import net.minecraft.client.multiplayer.ClientLevel;
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
public class McUtils {

    private static final Minecraft mc = Minecraft.getInstance();

    // Discouraged for use due to AutoCloseable warnings
    public static Minecraft mc() {
        return Minecraft.getInstance();
    }

    public static LocalPlayer player() {
        return mc.player;
    }

    public static Options options() {
        return mc.options;
    }

    public static InventoryMenu inventoryMenu() {
        return mc.player.inventoryMenu;
    }

    public static AbstractContainerMenu containerMenu() {
        return mc.player.containerMenu;
    }

    public static Inventory inventory() {
        return mc.player.getInventory();
    }

    public static Screen screen() {
        return mc.screen;
    }

    public static ClientLevel level() {
        return mc.level;
    }

    public static Gui gui() {
        return mc.gui;
    }

    public static Font font() {
        return mc.font;
    }

    public static Camera mainCamera() {
        return mc.gameRenderer.getMainCamera();
    }

    public static Window window() {
        return McUtils.mc().getWindow();
    }

    public static void sendMessageToClient(Component component) {
        mc.player.sendMessage(component, Util.NIL_UUID);
    }

    public static void sendPacket(Packet<?> packet) {
        mc.getConnection().send(packet);
    }
}
