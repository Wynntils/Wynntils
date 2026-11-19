/*
 * Copyright © Wynntils 2026.
 * This file is released under LGPLv3. See LICENSE for full license details.
 */
package com.wynntils.core.consumers.commands;

import com.mojang.brigadier.CommandDispatcher;
import com.mojang.brigadier.ParseResults;
import com.mojang.brigadier.StringReader;
import com.wynntils.utils.mc.McUtils;
import net.minecraft.commands.CommandBox;
import net.minecraft.commands.CommandSourceStack;
import net.minecraft.commands.ParseContext;

public class ClientCommandBox extends CommandBox<ParseContext> {
    public ClientCommandBox() {
        super(new CommandDispatcher<>());
    }

    public static ParseResults<CommandSourceStack> parse(
            CommandDispatcher<CommandSourceStack> dispatcher, StringReader reader, CommandSourceStack source) {
        return runWithParseContext(
                () -> dispatcher.parse(reader, source),
                McUtils.mc().getConnection().getSuggestionsProvider());
    }
}
