/*
 * Copyright © Wynntils 2024.
 * This file is released under LGPLv3. See LICENSE for full license details.
 */
package com.wynntils.handlers.labels.event;

import com.wynntils.core.text.StyledText;
import net.minecraft.world.entity.Display;
import net.neoforged.bus.api.Event;
import net.neoforged.bus.api.ICancellableEvent;

/**
 * This event is called when data of a {@link net.minecraft.world.entity.Display.TextDisplay} is changed.
 * <p>
 * The various events are cancellable.
 */
public abstract class TextDisplayChangedEvent extends Event implements ICancellableEvent {
    private final Display.TextDisplay textDisplay;

    protected TextDisplayChangedEvent(Display.TextDisplay textDisplay) {
        this.textDisplay = textDisplay;
    }

    public Display.TextDisplay getTextDisplay() {
        return textDisplay;
    }

    public static class Text extends TextDisplayChangedEvent {
        private StyledText text;

        public Text(Display.TextDisplay textDisplay, StyledText text) {
            super(textDisplay);
            this.text = text;
        }

        public StyledText getText() {
            return text;
        }

        public void setText(StyledText text) {
            this.text = text;
        }
    }
}
