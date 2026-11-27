/*
 * Copyright © Wynntils 2022-2026.
 * This file is released under LGPLv3. See LICENSE for full license details.
 */
package com.wynntils.core.keybinds;

import java.util.function.Consumer;
import net.minecraft.client.KeyMapping;
import net.minecraft.world.inventory.Slot;

/** Wraps a {@link KeyMapping} with relevant context */
public class KeyBind {
    private final KeyBindDefinition definition;
    private final Runnable onPress;
    private final Consumer<Slot> onInventoryPress;

    private final WynntilsKeyMapping keyMapping;

    public KeyBind(
            KeyBindDefinition definition,
            WynntilsKeyMapping keyMapping,
            Runnable onPress,
            Consumer<Slot> onInventoryPress) {
        this.definition = definition;
        this.keyMapping = keyMapping;
        this.onPress = onPress;
        this.onInventoryPress = onInventoryPress;
    }

    public String getName() {
        return definition.name();
    }

    public KeyMapping getKeyMapping() {
        return keyMapping;
    }

    public boolean onlyFirstPress() {
        return definition.firstPress();
    }

    public boolean consumePress() {
        return keyMapping.consumePress();
    }

    public void resetPress() {
        keyMapping.resetPress();
    }

    void trigger() {
        if (onPress == null) return;

        boolean justPressed = consumePress();
        if (onlyFirstPress() ? justPressed : justPressed || keyMapping.isDown()) {
            onPress();
        }
    }

    public void onPress() {
        if (onPress != null) onPress.run();
    }

    public void onInventoryPress(Slot slot) {
        if (onInventoryPress != null) onInventoryPress.accept(slot);
    }

    @Override
    public String toString() {
        return "'" + keyMapping.getName() + "' ["
                + keyMapping.getTranslatedKeyMessage().getString() + "]";
    }
}
