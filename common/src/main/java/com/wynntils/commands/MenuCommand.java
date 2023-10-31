/*
 * Copyright © Wynntils 2022-2023.
 * This file is released under LGPLv3. See LICENSE for full license details.
 */
package com.wynntils.commands;

import com.mojang.brigadier.builder.LiteralArgumentBuilder;
import com.mojang.brigadier.context.CommandContext;
import com.wynntils.core.components.Managers;
import com.wynntils.core.consumers.commands.Command;
import com.wynntils.screens.base.WynntilsMenuScreenBase;
import com.wynntils.screens.wynntilsmenu.WynntilsMenuScreen;
import net.minecraft.commands.CommandSourceStack;

public class MenuCommand extends Command {
    @Override
    public String getCommandName() {
        return "menu";
    }

    @Override
    protected LiteralArgumentBuilder<CommandSourceStack> getCommandBuilder(
            LiteralArgumentBuilder<CommandSourceStack> base) {
        return base.executes(this::openMenu);
    }

    private int openMenu(CommandContext<CommandSourceStack> commandSourceStackCommandContext) {
        // Delay is needed to prevent chat screen overwriting the lootrun screen
        Managers.TickScheduler.scheduleLater(() -> WynntilsMenuScreenBase.openBook(WynntilsMenuScreen.create()), 2);
        return 0;
    }
}
