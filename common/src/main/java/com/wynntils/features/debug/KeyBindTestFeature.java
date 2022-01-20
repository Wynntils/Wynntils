/*
 * Copyright © Wynntils 2022.
 * This file is released under AGPLv3. See LICENSE for full license details.
 */
package com.wynntils.features.debug;

import com.wynntils.core.features.AbstractFeature;
import com.wynntils.mc.utils.keybinds.KeyHolder;
import net.minecraft.client.Minecraft;
import net.minecraft.network.chat.TextComponent;
import org.lwjgl.glfw.GLFW;

public class KeyBindTestFeature extends AbstractFeature {
    {
        keybinds.add(
                () ->
                        new KeyHolder(
                                this,
                                "testKeybind",
                                GLFW.GLFW_KEY_0,
                                "WynntilsTest",
                                false,
                                () -> {
                                    Minecraft.getInstance()
                                            .player
                                            .sendMessage(
                                                    new TextComponent("keybind pressed"), null);
                                }));
        keybinds.add(
                () ->
                        new KeyHolder(
                                this,
                                "testKeybindOnce",
                                GLFW.GLFW_KEY_1,
                                "WynntilsTest",
                                true,
                                () -> {
                                    Minecraft.getInstance()
                                            .player
                                            .sendMessage(
                                                    new TextComponent("onetime keybind pressed"),
                                                    null);
                                }));
    }
}
