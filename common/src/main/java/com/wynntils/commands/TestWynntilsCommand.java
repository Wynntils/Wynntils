/*
 * Copyright © Wynntils 2022.
 * This file is released under AGPLv3. See LICENSE for full license details.
 */
package com.wynntils.commands;

import static com.mojang.brigadier.arguments.IntegerArgumentType.getInteger;
import static com.mojang.brigadier.arguments.IntegerArgumentType.integer;
import static net.minecraft.commands.Commands.*;

import com.mojang.brigadier.CommandDispatcher;
import com.wynntils.core.commands.WynntilsCommand;
import net.minecraft.commands.CommandSourceStack;
import net.minecraft.commands.Commands;
import net.minecraft.network.chat.TextComponent;

public class TestWynntilsCommand extends WynntilsCommand {
    @Override
    public void register(CommandDispatcher<CommandSourceStack> dispatcher) {
        dispatcher.register(
                literal("foo")
                        .then(
                                Commands.argument("bar", integer())
                                        .executes(
                                                c -> {
                                                    // System.out.println("Bar is " + getInteger(c,
                                                    // "bar"));
                                                    c.getSource()
                                                            .sendSuccess(
                                                                    new TextComponent(
                                                                            "Bar is "
                                                                                    + getInteger(
                                                                                            c,
                                                                                            "bar")),
                                                                    false);
                                                    return 1;
                                                }))
                        .executes(
                                c -> {
                                    // System.out.println("Called foo with no arguments");
                                    c.getSource()
                                            .sendSuccess(
                                                    new TextComponent(
                                                            "Called foo with no arguments"),
                                                    false);
                                    return 1;
                                }));
    }
}
