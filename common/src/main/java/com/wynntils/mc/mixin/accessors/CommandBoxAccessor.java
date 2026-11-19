/*
 * Copyright © Wynntils 2026.
 * This file is released under LGPLv3. See LICENSE for full license details.
 */
package com.wynntils.mc.mixin.accessors;

import com.mojang.brigadier.CommandDispatcher;
import net.minecraft.commands.CommandBox;
import net.minecraft.commands.suggestions.SuggestionContext;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.gen.Accessor;

@Mixin(CommandBox.class)
public interface CommandBoxAccessor {
    @Accessor("dispatcher")
    CommandDispatcher<SuggestionContext> getDispatcher();
}
