/*
 * Copyright © Wynntils 2022-2026.
 * This file is released under LGPLv3. See LICENSE for full license details.
 */
package com.wynntils.mc.event;

import com.mojang.brigadier.tree.RootCommandNode;
import net.minecraft.commands.CommandBuildContext;
import net.minecraft.commands.suggestions.SuggestionContext;
import net.neoforged.bus.api.Event;

public class CommandsAddedEvent extends Event {
    private final CommandBuildContext context;
    private final RootCommandNode<SuggestionContext> root;

    public CommandsAddedEvent(RootCommandNode<SuggestionContext> root, CommandBuildContext context) {
        this.root = root;
        this.context = context;
    }

    public RootCommandNode<SuggestionContext> getRoot() {
        return root;
    }

    public CommandBuildContext getContext() {
        return context;
    }
}
