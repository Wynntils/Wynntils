/*
 * Copyright © Wynntils 2022.
 * This file is released under AGPLv3. See LICENSE for full license details.
 */
package com.wynntils.mc.utils.keybinds;

import com.mojang.blaze3d.platform.InputConstants;
import net.minecraft.client.KeyMapping;

public class KeyHolder {
    private final KeyMapping keybind;
    private final Runnable onPress;
    private final boolean firstPress;

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

    public KeyHolder(
            String name,
            int keyCode,
            String category,
            boolean firstPress,
            Runnable onPress) {
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
