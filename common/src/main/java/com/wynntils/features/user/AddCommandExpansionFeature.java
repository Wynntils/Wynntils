/*
 * Copyright © Wynntils 2022.
 * This file is released under AGPLv3. See LICENSE for full license details.
 */
package com.wynntils.features.user;

import static net.minecraft.commands.Commands.argument;
import static net.minecraft.commands.Commands.literal;

import com.mojang.brigadier.arguments.IntegerArgumentType;
import com.mojang.brigadier.arguments.StringArgumentType;
import com.mojang.brigadier.tree.CommandNode;
import com.mojang.brigadier.tree.RootCommandNode;
import com.wynntils.core.features.UserFeature;
import com.wynntils.mc.event.CommandsPacketEvent;
import net.minecraft.commands.CommandSourceStack;
import net.minecraftforge.eventbus.api.SubscribeEvent;

/**
 * Set up Brigadier command structure of known Wynncraft commands. The commands in this file
 * were extracted from https://wynncraft.fandom.com/wiki/Commands,
 * https://wynncraft.com/help?guide=commands and from running the commands in-game.
 */
public class AddCommandExpansionFeature extends UserFeature {
    @SubscribeEvent
    public void onCommandPacket(CommandsPacketEvent event) {
        RootCommandNode root = event.getRoot();

        addArgumentlessCommandNodes(root);
        addChangetagCommandNode(root);
        addFriendCommandNode(root);
        addGuildCommandNode(root);
        addIgnoreCommandNode(root);
        addHousingCommandNode(root);
        addMessagingCommandNodes(root);
        addMiscCommandNodes(root);
        addParticlesCommandNode(root);
        addPartyCommandNode(root);
        addPlayerCommandNodes(root);
        addToggleCommandNode(root);
    }

    private void addArgumentlessCommandNodes(RootCommandNode root) {
        root.addChild(literal("buy").build());
        root.addChild(literal("cash").build());
        root.addChild(literal("change").build());
        root.addChild(literal("claimingredientbomb").build());
        root.addChild(literal("claimitembomb").build());
        root.addChild(literal("class").build());
        root.addChild(literal("classes").build());
        root.addChild(literal("crates").build());
        root.addChild(literal("daily").build());
        root.addChild(literal("die").build());
        root.addChild(literal("fixquests").build());
        root.addChild(literal("fixstart").build());
        root.addChild(literal("forum").build());
        root.addChild(literal("gc").build());
        root.addChild(literal("gold").build());
        root.addChild(literal("goldcoins").build());
        root.addChild(literal("help").build());
        root.addChild(literal("hub").build());
        root.addChild(literal("itemlock").build());
        root.addChild(literal("kill").build());
        root.addChild(literal("lobby").build());
        root.addChild(literal("pet").build());
        root.addChild(literal("pets").build());
        root.addChild(literal("relore").build());
        root.addChild(literal("renameitem").build());
        root.addChild(literal("renamepet").build());
        root.addChild(literal("rules").build());
        root.addChild(literal("shop").build());
        root.addChild(literal("sign").build());
        root.addChild(literal("skiptutorial").build());
        root.addChild(literal("store").build());
        root.addChild(literal("stream").build());
        root.addChild(literal("suicide").build());
        root.addChild(literal("totems").build());
        root.addChild(literal("use").build());
    }

    private void addChangetagCommandNode(RootCommandNode root) {
        root.addChild(literal("changetag")
                .then(literal("VIP"))
                .then(literal("VIP+"))
                .then(literal("HERO"))
                .then(literal("CHAMPION"))
                .then(literal("RESET"))
                .build());
    }

    private void addFriendCommandNode(RootCommandNode root) {
        CommandNode<CommandSourceStack> node = literal("friend")
                .then(literal("list"))
                .then(literal("online"))
                .then(literal("add").then(argument("player", StringArgumentType.string())))
                .then(literal("remove").then(argument("player", StringArgumentType.string())))
                .build();
        root.addChild(node);

        root.addChild(literal("f").redirect(node).build());
    }

    private void addGuildCommandNode(RootCommandNode root) {
        CommandNode<CommandSourceStack> node = literal("guild")
                .then(literal("attack"))
                .then(literal("contribute"))
                .then(literal("defend"))
                .then(literal("invite").then(argument("player", StringArgumentType.string())))
                .then(literal("join").then(argument("tag", StringArgumentType.greedyString())))
                .then(literal("kick").then(argument("player", StringArgumentType.string())))
                .then(literal("leaderboard"))
                .then(literal("leave"))
                .then(literal("list"))
                .then(literal("log"))
                .then(literal("manage"))
                .then(literal("rank")
                        .then(argument("player", StringArgumentType.string()))
                        .then(argument("rank", StringArgumentType.string())))
                .then(literal("rewards"))
                .then(literal("stats"))
                .then(literal("territory"))
                .then(literal("xp").then(argument("amount", IntegerArgumentType.integer())))
                .build();
        root.addChild(node);

        root.addChild(literal("gu").redirect(node).build());
    }

    private void addIgnoreCommandNode(RootCommandNode root) {
        root.addChild(literal("ignore")
                .then(literal("add").then(argument("player", StringArgumentType.string())))
                .then(literal("remove").then(argument("player", StringArgumentType.string())))
                .build());
    }

    private void addHousingCommandNode(RootCommandNode root) {
        CommandNode<CommandSourceStack> node = literal("housing")
                .then(literal("allowedit").then(argument("player", StringArgumentType.string())))
                .then(literal("ban").then(argument("player", StringArgumentType.string())))
                .then(literal("disallowedit").then(argument("player", StringArgumentType.string())))
                .then(literal("edit"))
                .then(literal("invite").then(argument("player", StringArgumentType.string())))
                .then(literal("kick").then(argument("player", StringArgumentType.string())))
                .then(literal("kickall"))
                .then(literal("leave"))
                .then(literal("public"))
                .then(literal("unban").then(argument("player", StringArgumentType.string())))
                .then(literal("visit"))
                .build();
        root.addChild(node);

        root.addChild(literal("is").redirect(node).build());
    }

    private void addMessagingCommandNodes(RootCommandNode root) {
        root.addChild(literal("g")
                .then(argument("msg", StringArgumentType.greedyString()))
                .build());

        root.addChild(literal("p")
                .then(argument("msg", StringArgumentType.greedyString()))
                .build());

        root.addChild(literal("r")
                .then(argument("msg", StringArgumentType.greedyString()))
                .build());

        CommandNode<CommandSourceStack> node = literal("msg")
                .then(argument("player", StringArgumentType.string())
                        .then(argument("msg", StringArgumentType.greedyString())))
                .build();
        root.addChild(node);
        root.addChild(literal("tell").redirect(node).build());
    }

    private void addMiscCommandNodes(RootCommandNode root) {
        root.addChild(literal("report")
                .then(argument("player", StringArgumentType.string())
                        .then(argument("reason", StringArgumentType.greedyString())))
                .build());

        root.addChild(literal("switch")
                .then(argument("world", StringArgumentType.string()))
                .build());
    }

    private void addParticlesCommandNode(RootCommandNode root) {
        CommandNode<CommandSourceStack> node = literal("particles")
                .then(literal("off"))
                .then(literal("low"))
                .then(literal("medium"))
                .then(literal("high"))
                .then(literal("veryhigh"))
                .then(literal("highest"))
                .then(argument("particles_per_tick", IntegerArgumentType.integer()))
                .build();
        root.addChild(node);
        root.addChild(literal("pq").redirect(node).build());
    }

    private void addPartyCommandNode(RootCommandNode root) {
        root.addChild(literal("party")
                .then(literal("ban").then(argument("player", StringArgumentType.string())))
                .then(literal("create"))
                .then(literal("disband"))
                .then(literal("finder"))
                .then(literal("invite").then(argument("player", StringArgumentType.string())))
                .then(literal("join").then(argument("player", StringArgumentType.string())))
                .then(literal("kick").then(argument("player", StringArgumentType.string())))
                .then(literal("leave"))
                .then(literal("list"))
                .then(literal("promote").then(argument("player", StringArgumentType.string())))
                .then(literal("unban").then(argument("player", StringArgumentType.string())))
                .build());
    }

    private void addPlayerCommandNodes(RootCommandNode root) {
        CommandNode<CommandSourceStack> duelNode = literal("duel")
                .then(argument("player", StringArgumentType.string()))
                .build();
        root.addChild(duelNode);
        root.addChild(literal("d").redirect(duelNode).build());

        CommandNode<CommandSourceStack> tradeNode = literal("trade")
                .then(argument("player", StringArgumentType.string()))
                .build();
        root.addChild(tradeNode);
        root.addChild(literal("share").redirect(tradeNode).build());

        root.addChild(literal("find")
                .then(argument("player", StringArgumentType.string()))
                .build());
    }

    private void addToggleCommandNode(RootCommandNode root) {
        root.addChild(literal("toggle")
                .then(literal("100"))
                .then(literal("attacksound"))
                .then(literal("autojoin"))
                .then(literal("autotracking"))
                .then(literal("beacon"))
                .then(literal("blood"))
                .then(literal("bombbell"))
                .then(literal("combatbar"))
                .then(literal("friendpopups"))
                .then(literal("ghosts")
                        .then(literal("none"))
                        .then(literal("low"))
                        .then(literal("medium"))
                        .then(literal("high"))
                        .then(literal("all")))
                .then(literal("guildjoin"))
                .then(literal("guildpopups"))
                .then(literal("insults"))
                .then(literal("music"))
                .then(literal("outlines"))
                .then(literal("popups"))
                .then(literal("pouchmsg"))
                .then(literal("pouchpickup"))
                .then(literal("queststartbeacon"))
                .then(literal("rpwarning"))
                .then(literal("sb"))
                .then(literal("swears"))
                .then(literal("vet"))
                .then(literal("war"))
                .build());
    }
}
