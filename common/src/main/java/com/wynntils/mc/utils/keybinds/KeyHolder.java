/*
 * Copyright © Wynntils 2022.
 * This file is released under AGPLv3. See LICENSE for full license details.
 */
package com.wynntils.mc.utils.keybinds;

import com.mojang.blaze3d.platform.InputConstants;
import net.minecraft.client.KeyMapping;

/** Wraps a {@link KeyMapping} with relevant context */
public class KeyHolder {
    private final KeyMapping keybind;
    private final Runnable onPress;
    private final boolean firstPress;

    /**
     * @param name Name of the keybind
     * @param keyCode The keyCode of the default keybind. Use {@link org.lwjgl.glfw.GLFW} for easy
     *     key code getting
     * @param category Category of keybind
     * @param type Type of keybind (mouse, keyboard, scancode)
     * @param firstPress Boolean for whether onPress should only be done on first press
     * @param onPress {@link Runnable} ran on button press
     */
    public KeyHolder(
            String name,
            int keyCode,
            String category,
            InputConstants.Type type,
            boolean firstPress,
            Runnable onPress) {
        this.firstPress = firstPress;
        this.keybind = new KeyMapping(name, type, keyCode, category);
        this.onPress = onPress;
    }

    /**
     * Same as {@link this#KeyHolder(String, int, String, InputConstants.Type, boolean, Runnable)}
     * but Type is of {@link InputConstants.Type}'s KEYSYM
     */
    public KeyHolder(
            String name, int keyCode, String category, boolean firstPress, Runnable onPress) {
        this(name, keyCode, category, InputConstants.Type.KEYSYM, firstPress, onPress);
    }

    public boolean isFirstPress() {
        return firstPress;
    }

    public KeyMapping getKeybind() {
        return keybind;
    }

    public void onPress() {
        onPress.run();
    }

    public String getName() {
        return keybind.getName();
    }

    public String getCategory() {
        return keybind.getCategory();
    }
}
