/*
 * Copyright © Wynntils 2022.
 * This file is released under AGPLv3. See LICENSE for full license details.
 */
package com.wynntils.features.user;

import static net.minecraft.commands.Commands.argument;
import static net.minecraft.commands.Commands.literal;

import com.mojang.brigadier.arguments.StringArgumentType;
import com.mojang.brigadier.builder.LiteralArgumentBuilder;
import com.mojang.brigadier.tree.CommandNode;
import com.mojang.brigadier.tree.RootCommandNode;
import com.wynntils.core.features.UserFeature;
import com.wynntils.mc.event.CommandsPacketEvent;
import java.util.Arrays;
import java.util.List;
import net.minecraft.commands.CommandSourceStack;
import net.minecraftforge.eventbus.api.SubscribeEvent;

public class AddCommandExpansionFeature extends UserFeature {
    // These commands are extracted from https://wynncraft.fandom.com/wiki/Commands

    private static final List<String> WYNN_COMMANDS = Arrays.asList(
            "buy",
            "changetag",
            "claimingredientbomb",
            "claimitembomb",
            "class",
            "crates",
            "daily",
            "duel",
            "find",
            "fixquests",
            "fixstart",
            "forum",
            "g",
            "guild",
            "housing",
            "hub",
            "itemlock",
            "kill",
            "msg",
            "p",
            "particles",
            "party",
            "pet",
            "r",
            "relore",
            "renameitem",
            "renamepet",
            "report",
            "rules",
            "skiptutorial",
            "stream",
            "switch",
            "toggle",
            "totems",
            "trade",
            "use");

    private static final List<String> WYNN_ALIASES = Arrays.asList(
            "cash",
            "change",
            "classes",
            "die",
            "f",
            "gc",
            "gold",
            "goldcoins",
            "gu",
            "is",
            "lobby",
            "pets",
            "pq",
            "share",
            "shop",
            "store",
            "suicide",
            "tell",
            "trade");

    @SubscribeEvent
    public void onCommandPacket(CommandsPacketEvent event) {
        RootCommandNode root = event.getRoot();
        // Add commands with no structured arguments
        // FIXME: Some of these can be provided with structure
        for (String command : WYNN_COMMANDS) {
            root.addChild(literal(command).build());
        }
        // Add aliases with no structured arguments
        for (String command : WYNN_ALIASES) {
            root.addChild(literal(command).build());
        }
        // Add commands with structured arguments
        root.addChild(getFriendCommandNode());
    }

    private CommandNode<CommandSourceStack> getFriendCommandNode() {
        LiteralArgumentBuilder<CommandSourceStack> friendCommandBuilder = literal("friend")
                .then(literal("list"))
                .then(literal("online"))
                .then(literal("add").then(argument("name", StringArgumentType.string())))
                .then(literal("remove").then(argument("name", StringArgumentType.string())));

        return friendCommandBuilder.build();
    }
}
