/*
 * Copyright © Wynntils 2026.
 * This file is released under LGPLv3. See LICENSE for full license details.
 */
package com.wynntils.mc.event;

import com.wynntils.utils.type.PauseMenuButtonType;
import net.minecraft.client.gui.components.Button;
import net.minecraft.client.gui.components.SpriteIconButton;
import net.neoforged.bus.api.Event;

public abstract class PauseMenuButtonReplaceEvent extends Event {
    private final PauseMenuButtonType buttonType;

    protected PauseMenuButtonReplaceEvent(PauseMenuButtonType buttonType) {
        this.buttonType = buttonType;
    }

    public PauseMenuButtonType getButtonType() {
        return buttonType;
    }

    public static class Text extends PauseMenuButtonReplaceEvent {
        private Button button;

        public Text(PauseMenuButtonType buttonType, Button button) {
            super(buttonType);

            this.button = button;
        }

        public void setButton(Button button) {
            this.button = button;
        }

        public Button getButton() {
            return button;
        }
    }

    public static class SpriteIcon extends PauseMenuButtonReplaceEvent {
        private SpriteIconButton button;

        public SpriteIcon(PauseMenuButtonType buttonType, SpriteIconButton button) {
            super(buttonType);

            this.button = button;
        }

        public void setButton(SpriteIconButton button) {
            this.button = button;
        }

        public SpriteIconButton getButton() {
            return button;
        }
    }
}
