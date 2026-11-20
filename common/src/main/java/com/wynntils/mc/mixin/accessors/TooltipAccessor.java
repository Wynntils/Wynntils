/*
 * Copyright © Wynntils 2026.
 * This file is released under LGPLv3. See LICENSE for full license details.
 */
package com.wynntils.mc.mixin.accessors;

import net.minecraft.client.gui.components.Tooltip;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.Mutable;
import org.spongepowered.asm.mixin.gen.Accessor;

@Mixin(Tooltip.class)
public interface TooltipAccessor {
    @Mutable
    @Accessor("splitLines")
    void setSplitLines(boolean splitLines);
}
