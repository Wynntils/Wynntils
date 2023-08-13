/*
 * Copyright © Wynntils 2022-2023.
 * This file is released under LGPLv3. See LICENSE for full license details.
 */
package com.wynntils.core.consumers.commands;

import com.mojang.brigadier.builder.LiteralArgumentBuilder;
import java.util.List;
import java.util.stream.Stream;
import net.minecraft.commands.CommandSourceStack;
import net.minecraft.commands.Commands;

public abstract class Command {
    public abstract String getCommandName();

    public List<String> getAliases() {
        return List.of();
    }

    public abstract String getDescription();

    protected abstract LiteralArgumentBuilder<CommandSourceStack> getCommandBuilder(
            LiteralArgumentBuilder<CommandSourceStack> base);

    public final List<LiteralArgumentBuilder<CommandSourceStack>> getCommandBuilders() {
        return Stream.concat(
                        Stream.of(Commands.literal(getCommandName())),
                        getAliases().stream().map(Commands::literal))
                .map(this::getCommandBuilder)
                .toList();
    }
}
