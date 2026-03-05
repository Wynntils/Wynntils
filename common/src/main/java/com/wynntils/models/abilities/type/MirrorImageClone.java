/*
 * Copyright © Wynntils 2024-2026.
 * This file is released under LGPLv3. See LICENSE for full license details.
 */
package com.wynntils.models.abilities.type;

import net.minecraft.ChatFormatting;

public class MirrorImageClone {
    private final ActiveState activeState;

    public MirrorImageClone(ActiveState activeState) {
        this.activeState = activeState;
    }

    public int getActiveState() {
        return switch (activeState) {
            case ALIVE -> 1;
            case DEAD -> 0;
        };
    }

    public String getString() {
        return activeState.getColor().toString() + "유";
    } // placeholder icon, replace with real one if possible

    public enum ActiveState {
        ALIVE(ChatFormatting.GREEN),
        DEAD(ChatFormatting.GRAY);

        private final ChatFormatting color;

        ActiveState(ChatFormatting color) {
            this.color = color;
        }

        public static ActiveState fromColor(ChatFormatting color) {
            for (ActiveState value : ActiveState.values()) {
                if (value.getColor() == color) {
                    return value;
                }
            }

            return null;
        }

        public ChatFormatting getColor() {
            return color;
        }
    }
}
