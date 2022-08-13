/*
 * Copyright © Wynntils 2022.
 * This file is released under AGPLv3. See LICENSE for full license details.
 */
package com.wynntils.features.user;

import static net.minecraft.commands.Commands.argument;
import static net.minecraft.commands.Commands.literal;

import com.mojang.brigadier.arguments.StringArgumentType;
import com.mojang.brigadier.builder.LiteralArgumentBuilder;
import com.mojang.brigadier.suggestion.SuggestionProvider;
import com.mojang.brigadier.tree.CommandNode;
import com.mojang.brigadier.tree.RootCommandNode;
import com.wynntils.core.features.UserFeature;
import com.wynntils.mc.event.CommandsPacketEvent;
import net.minecraft.commands.CommandSourceStack;
import net.minecraftforge.eventbus.api.SubscribeEvent;

public class AddCommandExpansionFeature extends UserFeature {
    private static final SuggestionProvider<CommandSourceStack> FRIEND_NAMESUGGESTION_PROVIDER = null;

    @SubscribeEvent
    public void onCommandPacket(CommandsPacketEvent event) {
        RootCommandNode root = event.getRoot();
        CommandNode<CommandSourceStack> n = getFriendCommandNode();
        root.addChild(n);
    }

    private CommandNode<CommandSourceStack> getFriendCommandNode() {
        LiteralArgumentBuilder<CommandSourceStack> friendCommandBuilder = literal("friend")
                .then(literal("list"))
                .then(literal("online"))
                .then(literal("add")
                        .then(argument("name", StringArgumentType.string()).suggests(FRIEND_NAMESUGGESTION_PROVIDER)))
                .then(literal("remove")
                        .then(argument("name", StringArgumentType.string()).suggests(FRIEND_NAMESUGGESTION_PROVIDER)));

        return friendCommandBuilder.build();
    }
}
