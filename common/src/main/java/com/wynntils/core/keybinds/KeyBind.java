/*
 * Copyright © Wynntils 2022-2024.
 * This file is released under LGPLv3. See LICENSE for full license details.
 */
package com.wynntils.core.keybinds;

import com.mojang.blaze3d.platform.InputConstants;
import java.util.function.Consumer;
import net.minecraft.client.KeyMapping;
import net.minecraft.world.inventory.Slot;

/** Wraps a {@link KeyMapping} with relevant context */
public class KeyBind {
    private static final String CATEGORY = "Wynntils";
    private final Runnable onPress;
    private final Consumer<Slot> onInventoryPress;
    private final boolean firstPress;

    private final KeyMapping keyMapping;

    private boolean isPressed = false;

    /**
     * @param name             Name of the keybind
     * @param keyCode          The keyCode of the default keybind. Use {@link org.lwjgl.glfw.GLFW} for easy
     *                         key code getting
     * @param firstPress       Boolean for whether onPress should only be done on first press
     * @param onPress          Code to run when button is pressed, or null
     * @param onInventoryPress Code to run when key is pressed in inventory, or null
     */
    public KeyBind(String name, int keyCode, boolean firstPress, Runnable onPress, Consumer<Slot> onInventoryPress) {
        this.firstPress = firstPress;
        this.keyMapping = new KeyMapping(name, InputConstants.Type.KEYSYM, keyCode, CATEGORY);

        // Unbind keybind, bound after registration by options reload
        keyMapping.setKey(InputConstants.UNKNOWN);
        KeyMapping.resetMapping();

        this.onPress = onPress;
        this.onInventoryPress = onInventoryPress;
    }

    /**
     * @param name             Name of the keybind
     * @param keyCode          The keyCode of the default keybind. Use {@link org.lwjgl.glfw.GLFW} for easy
     *                         key code getting
     * @param type             Type of key
     * @param firstPress       Boolean for whether onPress should only be done on first press
     * @param onPress          Code to run when button is pressed, or null
     * @param onInventoryPress Code to run when key is pressed in inventory, or null
     */
    public KeyBind(
            String name,
            int keyCode,
            InputConstants.Type type,
            boolean firstPress,
            Runnable onPress,
            Consumer<Slot> onInventoryPress) {
        this.firstPress = firstPress;
        this.keyMapping = new KeyMapping(name, type, keyCode, CATEGORY);

        // Unbind keybind, bound after registration by options reload
        keyMapping.setKey(InputConstants.UNKNOWN);
        KeyMapping.resetMapping();

        this.onPress = onPress;
        this.onInventoryPress = onInventoryPress;
    }

    /**
     * @param name             Name of the keybind
     * @param keyCode          The keyCode of the default keybind. Use {@link org.lwjgl.glfw.GLFW} for easy
     *                         key code getting
     * @param type             Type of key
     * @param firstPress       Boolean for whether onPress should only be done on first press
     * @param onPress          Code to run when button is pressed, or null
     */
    public KeyBind(String name, int keyCode, InputConstants.Type type, boolean firstPress, Runnable onPress) {
        this.firstPress = firstPress;
        this.keyMapping = new KeyMapping(name, type, keyCode, CATEGORY);

        // Unbind keybind, bound after registration by options reload
        keyMapping.setKey(InputConstants.UNKNOWN);
        KeyMapping.resetMapping();

        this.onPress = onPress;
        this.onInventoryPress = null;
    }

    public KeyBind(String name, int keyCode, boolean firstPress, Runnable onPress) {
        this(name, keyCode, firstPress, onPress, null);
    }

    public boolean onlyFirstPress() {
        return firstPress;
    }

    public boolean isPressed() {
        return isPressed;
    }

    public void setIsPressed(boolean isPressed) {
        this.isPressed = isPressed;
    }

    public KeyMapping getKeyMapping() {
        return keyMapping;
    }

    public void onPress() {
        if (onPress == null) return;

        onPress.run();
    }

    public void onInventoryPress(Slot hoveredSlot) {
        if (onInventoryPress == null) return;

        onInventoryPress.accept(hoveredSlot);
    }

    public String getName() {
        return keyMapping.getName();
    }

    public String getCategory() {
        return keyMapping.getCategory();
    }

    @Override
    public String toString() {
        return "'" + getName() + "' ["
                + getKeyMapping().getTranslatedKeyMessage().getString() + "]";
    }
}
