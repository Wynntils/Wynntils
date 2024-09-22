/*
 * Copyright © Wynntils 2024.
 * This file is released under LGPLv3. See LICENSE for full license details.
 */
package com.wynntils.models.containers.type;

import com.wynntils.core.text.StyledText;
import java.util.Optional;
import java.util.regex.Pattern;
import net.minecraft.client.gui.screens.inventory.AbstractContainerScreen;

/**
 * Marker interface for properties that are used in containers that can be scrolled.
 */
public interface ScrollableContainerProperty {
    Pattern getNextItemPattern();

    Pattern getPreviousItemPattern();

    int getNextItemSlot();

    int getPreviousItemSlot();

    default Optional<Integer> getScrollButton(AbstractContainerScreen<?> screen, boolean previousPage) {
        StyledText buttonText = StyledText.fromComponent(screen.getMenu()
                .slots
                .get(previousPage ? getPreviousItemSlot() : getNextItemSlot())
                .getItem()
                .getHoverName());

        if (buttonText
                .getMatcher(previousPage ? getPreviousItemPattern() : getNextItemPattern())
                .matches()) {
            return Optional.of(previousPage ? getPreviousItemSlot() : getNextItemSlot());
        }

        return Optional.empty();
    }
}
