/*
 * Copyright © Wynntils 2022.
 * This file is released under AGPLv3. See LICENSE for full license details.
 */
package com.wynntils.core.commands;

import com.mojang.brigadier.CommandDispatcher;
import com.mojang.brigadier.builder.LiteralArgumentBuilder;
import net.minecraft.commands.CommandSourceStack;

public abstract class Command {
    public void register(CommandDispatcher<CommandSourceStack> dispatcher) {
        dispatcher.register(getBaseCommandBuilder());
    }

    public abstract LiteralArgumentBuilder<CommandSourceStack> getBaseCommandBuilder();
}
