/*
 * Copyright © Wynntils 2022-2023.
 * This file is released under LGPLv3. See LICENSE for full license details.
 */
package com.wynntils.utils.mc;

import com.mojang.blaze3d.platform.Window;
import com.wynntils.core.WynntilsMod;
import net.minecraft.ChatFormatting;
import net.minecraft.client.Minecraft;
import net.minecraft.client.Options;
import net.minecraft.client.multiplayer.prediction.PredictiveAction;
import net.minecraft.client.player.LocalPlayer;
import net.minecraft.client.resources.sounds.SimpleSoundInstance;
import net.minecraft.network.chat.Component;
import net.minecraft.network.protocol.Packet;
import net.minecraft.sounds.SoundEvent;
import net.minecraft.world.entity.player.Inventory;
import net.minecraft.world.inventory.AbstractContainerMenu;
import net.minecraft.world.inventory.InventoryMenu;

/**
 * This is a "high-quality misc" class for Minecraft utilities without an aspect on Wynntils
 * commonly used. Tags used more often should be moved elsewhere Keep the names short, but distinct.
 */
public final class McUtils {
    public static Minecraft mc() {
        return Minecraft.getInstance();
    }

    public static LocalPlayer player() {
        return mc().player;
    }

    public static String playerName() {
        return player().getName().getString();
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

    public static double guiScale() {
        return window().getGuiScale();
    }

    public static void playSoundUI(SoundEvent sound) {
        mc().getSoundManager().play(SimpleSoundInstance.forUI(sound, 1.0F));
    }

    public static void playSoundAmbient(SoundEvent sound) {
        mc().getSoundManager().play(SimpleSoundInstance.forLocalAmbience(sound, 1.0F, 1.0F));
    }

    public static void sendMessageToClient(Component component) {
        if (player() == null) {
            WynntilsMod.error(
                    "Tried to send message to client: \"" + component.getString() + "\", but player was null.");
            return;
        }
        player().sendSystemMessage(component);
    }

    public static void sendErrorToClient(String errorMsg) {
        WynntilsMod.warn("Chat error message sent: " + errorMsg);
        McUtils.sendMessageToClient(Component.literal(errorMsg).withStyle(ChatFormatting.RED));
    }

    public static void sendPacket(Packet<?> packet) {
        mc().getConnection().send(packet);
    }

    public static void sendSequencedPacket(PredictiveAction predictiveAction) {
        mc().gameMode.startPrediction(mc().level, predictiveAction);
    }

    /**
     * Sends the specified command to the server.
     * @param command The command to send. The leading '/' should not be included.
     */
    public static void sendCommand(String command) {
        mc().getConnection().sendCommand(command);
    }

    public static void sendChat(String command) {
        mc().getConnection().sendChat(command);
    }
}
