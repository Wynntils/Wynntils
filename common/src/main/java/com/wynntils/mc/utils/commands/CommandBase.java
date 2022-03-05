/*
 * Copyright © Wynntils 2022.
 * This file is released under AGPLv3. See LICENSE for full license details.
 */
package com.wynntils.mc.utils.commands;

import com.mojang.brigadier.CommandDispatcher;
import net.minecraft.commands.CommandSourceStack;

public abstract class CommandBase {
    public CommandBase() {}

    public abstract void register(CommandDispatcher<CommandSourceStack> dispatcher);
}
