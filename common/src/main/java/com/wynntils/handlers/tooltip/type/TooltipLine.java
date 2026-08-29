/*
 * Copyright © Wynntils 2026.
 * This file is released under LGPLv3. See LICENSE for full license details.
 */
package com.wynntils.handlers.tooltip.type;

import net.minecraft.network.chat.Component;

public sealed interface TooltipLine permits TooltipLine.Fixed, TooltipLine.Aligned, TooltipLine.Centered {
    Component unaligned();

    record Fixed(Component component) implements TooltipLine {
        @Override
        public Component unaligned() {
            return component;
        }
    }

    record Aligned(Component left, Component right) implements TooltipLine {
        @Override
        public Component unaligned() {
            return Component.empty().append(left.copy()).append(right.copy());
        }
    }

    record Centered(Component component) implements TooltipLine {
        @Override
        public Component unaligned() {
            return component;
        }
    }
}
