/*
 * Copyright © Wynntils 2022-2025.
 * This file is released under LGPLv3. See LICENSE for full license details.
 */
package com.wynntils.mc.event;

import com.mojang.brigadier.tree.RootCommandNode;
import com.wynntils.core.events.BaseEvent;
import net.minecraft.commands.CommandBuildContext;
import net.minecraft.commands.SharedSuggestionProvider;

public final class CommandsAddedEvent extends BaseEvent {
    private final CommandBuildContext context;

    private RootCommandNode<SharedSuggestionProvider> root;

    public CommandsAddedEvent(RootCommandNode<SharedSuggestionProvider> root, CommandBuildContext context) {
        this.root = root;
        this.context = context;
    }

    public RootCommandNode<SharedSuggestionProvider> getRoot() {
        return root;
    }

    public CommandBuildContext getContext() {
        return context;
    }

    public void setRoot(RootCommandNode<SharedSuggestionProvider> root) {
        this.root = root;
    }
}
