/*
 * Copyright © Wynntils 2022-2025.
 * This file is released under LGPLv3. See LICENSE for full license details.
 */
package com.wynntils.features.commands;

import static net.minecraft.commands.Commands.argument;
import static net.minecraft.commands.Commands.literal;

import com.mojang.brigadier.arguments.IntegerArgumentType;
import com.mojang.brigadier.arguments.StringArgumentType;
import com.mojang.brigadier.suggestion.SuggestionProvider;
import com.mojang.brigadier.tree.CommandNode;
import com.mojang.brigadier.tree.RootCommandNode;
import com.wynntils.core.components.Managers;
import com.wynntils.core.components.Models;
import com.wynntils.core.consumers.features.Feature;
import com.wynntils.core.persisted.Persisted;
import com.wynntils.core.persisted.config.Category;
import com.wynntils.core.persisted.config.Config;
import com.wynntils.core.persisted.config.ConfigCategory;
import com.wynntils.mc.event.CommandsAddedEvent;
import com.wynntils.utils.mc.McUtils;
import net.minecraft.commands.CommandSourceStack;
import net.minecraft.commands.SharedSuggestionProvider;
import net.minecraft.commands.arguments.EntityArgument;
import net.neoforged.bus.api.SubscribeEvent;

/**
 * Set up Brigadier command structure of known Wynncraft commands.
 *
 * The commands in this file were extracted from https://wynncraft.fandom.com/wiki/Commands,
 * https://wynncraft.com/help?guide=commands, from running the commands in-game, and from a
 * list of server commands provided by HeyZeer0.
 */
@ConfigCategory(Category.COMMANDS)
public class AddCommandExpansionFeature extends Feature {
    private static final SuggestionProvider<CommandSourceStack> PLAYER_NAME_SUGGESTION_PROVIDER =
            (context, builder) -> SharedSuggestionProvider.suggest(Models.Player.getAllPlayerNames(), builder);

    private static final SuggestionProvider<CommandSourceStack> FRIEND_NAME_SUGGESTION_PROVIDER =
            (context, builder) -> SharedSuggestionProvider.suggest(Models.Friends.getFriends(), builder);

    private static final SuggestionProvider<CommandSourceStack> PARTY_NAME_SUGGESTION_PROVIDER =
            (context, builder) -> SharedSuggestionProvider.suggest(
                    Models.Party.getPartyMembers().stream().filter(p -> !p.equals(McUtils.playerName())), builder);

    private static final SuggestionProvider<CommandSourceStack> SERVERS_SUGGESTION_PROVIDER =
            (context, builder) -> SharedSuggestionProvider.suggest(Models.ServerList.getServers(), builder);

    @Persisted
    private final Config<Boolean> includeDeprecatedCommands = new Config<>(false);

    @Persisted
    private final Config<AliasCommandLevel> includeAliases = new Config<>(AliasCommandLevel.SHORT_FORMS);

    @SubscribeEvent
    public void onCommandPacket(CommandsAddedEvent event) {
        RootCommandNode<SharedSuggestionProvider> root = event.getRoot();

        addArgumentlessCommandNodes(root);
        addChangetagCommandNode(root);
        addEmoteCommandNode(root);
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

        if (includeDeprecatedCommands.get()) {
            addDeprecatedCommandNodes(root);
        }
    }

    private void addNode(
            RootCommandNode<SharedSuggestionProvider> root, CommandNode<? extends SharedSuggestionProvider> node) {
        Managers.Command.addNode(root, node);
    }

    private void addAlias(
            RootCommandNode<SharedSuggestionProvider> root,
            CommandNode<CommandSourceStack> originalNode,
            String aliasName,
            AliasCommandLevel level) {
        if (includeAliases.get().ordinal() >= level.ordinal()) {
            addNode(root, literal(aliasName).redirect(originalNode).build());
        }
    }

    private void addArgumentlessCommandNodes(RootCommandNode<SharedSuggestionProvider> root) {
        addNode(root, literal("claimingredientbomb").build());
        addNode(root, literal("claimitembomb").build());
        addNode(root, literal("daily").build());
        addNode(root, literal("fixquests").build());
        addNode(root, literal("fixstart").build());
        addNode(root, literal("forum").build());
        addNode(root, literal("help").build());
        addNode(root, literal("link").build());
        addNode(root, literal("rules").build());
        addNode(root, literal("sign").build());
        addNode(root, literal("skiptutorial").build());
        addNode(root, literal("tracking").build());

        // There is also a command "server" but it is reserved for those with admin permissions
        // only, so don't include it here.
        // The command "checknickname" is also available but is probably a defunct legacy command.

        // "hub" aliases
        CommandNode<CommandSourceStack> hubNode = literal("hub").build();
        addNode(root, hubNode);

        addAlias(root, hubNode, "change", AliasCommandLevel.ALL);
        addAlias(root, hubNode, "lobby", AliasCommandLevel.ALL);
        addAlias(root, hubNode, "leave", AliasCommandLevel.ALL);
        addAlias(root, hubNode, "port", AliasCommandLevel.ALL);

        // There is also an alias "servers" for "hub", but it conflicts with our command
        // so don't include it here

        // "class" aliases
        CommandNode<CommandSourceStack> classNode = literal("class").build();
        addNode(root, classNode);

        addAlias(root, classNode, "classes", AliasCommandLevel.ALL);

        // "characters" aliases
        CommandNode<CommandSourceStack> charactersNode = literal("characters").build();
        addNode(root, charactersNode);

        addAlias(root, charactersNode, "char", AliasCommandLevel.ALL);

        // "crate" aliases
        CommandNode<CommandSourceStack> crateNode = literal("crate").build();
        addNode(root, crateNode);

        addAlias(root, crateNode, "crates", AliasCommandLevel.ALL);

        // "disguises" aliases
        CommandNode<CommandSourceStack> disguisesNode = literal("disguises").build();
        addNode(root, disguisesNode);

        addAlias(root, disguisesNode, "disguise", AliasCommandLevel.ALL);

        // "effects" aliases
        CommandNode<CommandSourceStack> effectsNode = literal("effects").build();
        addNode(root, effectsNode);

        addAlias(root, effectsNode, "effect", AliasCommandLevel.ALL);

        // "hats" aliases
        CommandNode<CommandSourceStack> hatsNode = literal("hats").build();
        addNode(root, hatsNode);

        addAlias(root, hatsNode, "hat", AliasCommandLevel.ALL);

        // "mounts" aliases
        CommandNode<CommandSourceStack> mountsNode = literal("mounts").build();
        addNode(root, mountsNode);

        addAlias(root, mountsNode, "mount", AliasCommandLevel.ALL);

        // "weapons" aliases
        CommandNode<CommandSourceStack> weaponsNode = literal("weapons").build();
        addNode(root, weaponsNode);

        addAlias(root, weaponsNode, "weapon", AliasCommandLevel.ALL);

        // "consumables" aliases
        CommandNode<CommandSourceStack> consumablesNode = literal("consumables").build();
        addNode(root, consumablesNode);

        addAlias(root, consumablesNode, "bomb", AliasCommandLevel.ALL);
        addAlias(root, consumablesNode, "bombs", AliasCommandLevel.ALL);
        addAlias(root, consumablesNode, "token", AliasCommandLevel.ALL);
        addAlias(root, consumablesNode, "tokens", AliasCommandLevel.ALL);
        addAlias(root, consumablesNode, "consumable", AliasCommandLevel.ALL);

        // "use" aliases
        CommandNode<CommandSourceStack> useNode = literal("use").build();
        addNode(root, useNode);

        addAlias(root, useNode, "rank", AliasCommandLevel.ALL);
        addAlias(root, useNode, "shop", AliasCommandLevel.ALL);
        addAlias(root, useNode, "store", AliasCommandLevel.ALL);

        // "kill" aliases
        CommandNode<CommandSourceStack> killNode = literal("kill").build();
        addNode(root, killNode);

        addAlias(root, killNode, "die", AliasCommandLevel.ALL);
        addAlias(root, killNode, "suicide", AliasCommandLevel.ALL);

        // "itemlock" aliases
        CommandNode<CommandSourceStack> itemlockNode = literal("itemlock").build();
        addNode(root, itemlockNode);

        addAlias(root, itemlockNode, "ilock", AliasCommandLevel.ALL);
        addAlias(root, itemlockNode, "lock", AliasCommandLevel.ALL);
        addAlias(root, itemlockNode, "locki", AliasCommandLevel.ALL);
        addAlias(root, itemlockNode, "lockitem", AliasCommandLevel.ALL);

        // "pet" aliases
        CommandNode<CommandSourceStack> petNode = literal("pet").build();
        addNode(root, petNode);

        addAlias(root, petNode, "pets", AliasCommandLevel.ALL);

        // "partyfinder" aliases
        CommandNode<CommandSourceStack> partyfinderNode = literal("partyfinder").build();
        addNode(root, partyfinderNode);

        addAlias(root, partyfinderNode, "pfinder", AliasCommandLevel.SHORT_FORMS);

        // "stream" aliases
        CommandNode<CommandSourceStack> streamNode = literal("stream").build();
        addNode(root, streamNode);

        addAlias(root, streamNode, "streamer", AliasCommandLevel.ALL);

        // "totem" aliases
        CommandNode<CommandSourceStack> totemNode = literal("totem").build();
        addNode(root, totemNode);

        addAlias(root, totemNode, "totems", AliasCommandLevel.ALL);

        // "hunted" aliases
        CommandNode<CommandSourceStack> huntedNode = literal("hunted").build();
        addNode(root, huntedNode);

        addAlias(root, huntedNode, "pvp", AliasCommandLevel.SHORT_FORMS);

        // "recruit" aliases
        CommandNode<CommandSourceStack> recruitNode = literal("recruit").build();
        addNode(root, recruitNode);

        addAlias(root, recruitNode, "rf", AliasCommandLevel.ALL);
    }

    private void addChangetagCommandNode(RootCommandNode<SharedSuggestionProvider> root) {
        addNode(
                root,
                literal("changetag")
                        .then(literal("VIP"))
                        .then(literal("VIP+"))
                        .then(literal("HERO"))
                        .then(literal("HERO+"))
                        .then(literal("CHAMPION"))
                        .then(literal("RESET"))
                        .build());
    }

    private void addEmoteCommandNode(RootCommandNode<SharedSuggestionProvider> root) {
        // FIXME: We should only provide the emotes that the player has access to
        CommandNode<CommandSourceStack> node = literal("emote")
                .then(literal("cheer"))
                .then(literal("clap"))
                .then(literal("dance"))
                .then(literal("explode"))
                .then(literal("faint"))
                .then(literal("flop"))
                .then(literal("hug"))
                .then(literal("relax"))
                .then(literal("jump"))
                .then(literal("wave"))
                .build();
        addNode(root, node);

        addAlias(root, node, "emotes", AliasCommandLevel.ALL);
    }

    private void addFriendCommandNode(RootCommandNode<SharedSuggestionProvider> root) {
        CommandNode<CommandSourceStack> node = literal("friend")
                .then(literal("list"))
                .then(literal("online"))
                .then(literal("add")
                        .then(argument("player", EntityArgument.players()).suggests(PLAYER_NAME_SUGGESTION_PROVIDER)))
                .then(literal("remove")
                        .then(argument("player", StringArgumentType.word()).suggests(FRIEND_NAME_SUGGESTION_PROVIDER)))
                .build();
        addNode(root, node);

        addAlias(root, node, "f", AliasCommandLevel.SHORT_FORMS);
        addAlias(root, node, "friends", AliasCommandLevel.ALL);
        addAlias(root, node, "buddy", AliasCommandLevel.ALL);
        addAlias(root, node, "buddies", AliasCommandLevel.ALL);
    }

    private void addGuildCommandNode(RootCommandNode<SharedSuggestionProvider> root) {
        CommandNode<CommandSourceStack> node = literal("guild")
                .then(literal("attack"))
                .then(literal("contribute"))
                .then(literal("defend"))
                .then(literal("invite")
                        .then(argument("player", EntityArgument.players()).suggests(PLAYER_NAME_SUGGESTION_PROVIDER)))
                .then(literal("join").then(argument("tag", StringArgumentType.greedyString())))
                .then(literal("kick")
                        .then(argument("player", EntityArgument.players()).suggests(PLAYER_NAME_SUGGESTION_PROVIDER)))
                .then(literal("leaderboard"))
                .then(literal("leave"))
                .then(literal("list"))
                .then(literal("log"))
                .then(literal("manage"))
                .then(literal("rank")
                        .then(argument("player", EntityArgument.players())
                                .suggests(PLAYER_NAME_SUGGESTION_PROVIDER)
                                .then(argument("rank", StringArgumentType.string()))))
                .then(literal("rewards"))
                .then(literal("stats"))
                .then(literal("territory"))
                .then(literal("xp").then(argument("amount", IntegerArgumentType.integer())))
                .build();
        addNode(root, node);

        addAlias(root, node, "gu", AliasCommandLevel.SHORT_FORMS);
        addAlias(root, node, "guilds", AliasCommandLevel.ALL);
    }

    private void addIgnoreCommandNode(RootCommandNode<SharedSuggestionProvider> root) {
        addNode(
                root,
                literal("ignore")
                        .then(literal("add")
                                .then(argument("player", EntityArgument.players())
                                        .suggests(PLAYER_NAME_SUGGESTION_PROVIDER)))
                        .then(literal("remove")
                                .then(argument("player", EntityArgument.players())
                                        .suggests(PLAYER_NAME_SUGGESTION_PROVIDER)))
                        .build());
    }

    private void addHousingCommandNode(RootCommandNode<SharedSuggestionProvider> root) {
        CommandNode<CommandSourceStack> node = literal("housing")
                .then(literal("allowedit")
                        .then(argument("player", EntityArgument.players()).suggests(PLAYER_NAME_SUGGESTION_PROVIDER)))
                .then(literal("ban")
                        .then(argument("player", EntityArgument.players()).suggests(PLAYER_NAME_SUGGESTION_PROVIDER)))
                .then(literal("disallowedit")
                        .then(argument("player", EntityArgument.players()).suggests(PLAYER_NAME_SUGGESTION_PROVIDER)))
                .then(literal("edit"))
                .then(literal("invite")
                        .then(argument("player", EntityArgument.players()).suggests(PLAYER_NAME_SUGGESTION_PROVIDER)))
                .then(literal("kick")
                        .then(argument("player", EntityArgument.players()).suggests(PLAYER_NAME_SUGGESTION_PROVIDER)))
                .then(literal("kickall"))
                .then(literal("leave"))
                .then(literal("public"))
                .then(literal("unban")
                        .then(argument("player", EntityArgument.players()).suggests(PLAYER_NAME_SUGGESTION_PROVIDER)))
                .then(literal("visit"))
                .build();
        addNode(root, node);

        addAlias(root, node, "is", AliasCommandLevel.SHORT_FORMS);
        addAlias(root, node, "hs", AliasCommandLevel.SHORT_FORMS);
        addAlias(root, node, "home", AliasCommandLevel.ALL);
        addAlias(root, node, "house", AliasCommandLevel.ALL);
        addAlias(root, node, "island", AliasCommandLevel.ALL);
        addAlias(root, node, "plot", AliasCommandLevel.ALL);
    }

    private void addMessagingCommandNodes(RootCommandNode<SharedSuggestionProvider> root) {
        addNode(
                root,
                literal("g")
                        .then(argument("msg", StringArgumentType.greedyString()))
                        .build());

        addNode(
                root,
                literal("p")
                        .then(argument("msg", StringArgumentType.greedyString()))
                        .build());

        addNode(
                root,
                literal("r")
                        .then(argument("msg", StringArgumentType.greedyString()))
                        .build());

        CommandNode<CommandSourceStack> node = literal("msg")
                .then(argument("player", EntityArgument.players())
                        .suggests(PLAYER_NAME_SUGGESTION_PROVIDER)
                        .then(argument("msg", StringArgumentType.greedyString())))
                .build();
        addNode(root, node);
    }

    private void addMiscCommandNodes(RootCommandNode<SharedSuggestionProvider> root) {
        addNode(
                root,
                literal("report")
                        .then(argument("player", EntityArgument.players())
                                .suggests(PLAYER_NAME_SUGGESTION_PROVIDER)
                                .then(argument("reason", StringArgumentType.greedyString())))
                        .build());

        addNode(
                root,
                literal("switch")
                        .then(argument("world", StringArgumentType.string()).suggests(SERVERS_SUGGESTION_PROVIDER))
                        .build());

        addNode(
                root,
                literal("relore")
                        .then(argument("lore", StringArgumentType.greedyString()))
                        .build());

        // first option is 0; not really supposed to be run by users
        addNode(
                root,
                literal("dialogue")
                        .then(argument("option", IntegerArgumentType.integer()))
                        .build());

        // not really supposed to be run by users
        addNode(
                root,
                literal("thankyou")
                        .then(argument("player", EntityArgument.players()).suggests(PLAYER_NAME_SUGGESTION_PROVIDER))
                        .build());

        // "renameitem" aliases
        CommandNode<CommandSourceStack> renameitemNode = literal("renameitem")
                .then(argument("name", StringArgumentType.greedyString()))
                .build();
        addNode(root, renameitemNode);

        addAlias(root, renameitemNode, "renameitems", AliasCommandLevel.ALL);

        // "renameitem" aliases
        CommandNode<CommandSourceStack> renamepetNode = literal("renamepet")
                .then(argument("name", StringArgumentType.greedyString()))
                .build();
        addNode(root, renamepetNode);

        addAlias(root, renamepetNode, "renamepets", AliasCommandLevel.ALL);

        addNode(root, literal("ironman").build());
    }

    private void addParticlesCommandNode(RootCommandNode<SharedSuggestionProvider> root) {
        CommandNode<CommandSourceStack> node = literal("particles")
                .then(literal("off"))
                .then(literal("low"))
                .then(literal("medium"))
                .then(literal("high"))
                .then(literal("veryhigh"))
                .then(literal("highest"))
                .then(literal("unlimited"))
                .then(argument("particles_per_tick", IntegerArgumentType.integer()))
                .build();
        addNode(root, node);
        addAlias(root, node, "pq", AliasCommandLevel.SHORT_FORMS);
        addAlias(root, node, "particlequality", AliasCommandLevel.ALL);
        addAlias(root, node, "particlesquality", AliasCommandLevel.ALL);
    }

    private void addPartyCommandNode(RootCommandNode<SharedSuggestionProvider> root) {
        CommandNode<CommandSourceStack> node = literal("party")
                .then(literal("ban")
                        .then(argument("player", EntityArgument.players()).suggests(PLAYER_NAME_SUGGESTION_PROVIDER)))
                .then(literal("create"))
                .then(literal("disband"))
                .then(literal("finder"))
                .then(literal("invite")
                        .then(argument("player", EntityArgument.players()).suggests(PLAYER_NAME_SUGGESTION_PROVIDER)))
                .then(literal("join")
                        .then(argument("player", EntityArgument.players()).suggests(PLAYER_NAME_SUGGESTION_PROVIDER)))
                .then(literal("kick")
                        .then(argument("player", EntityArgument.players()).suggests(PARTY_NAME_SUGGESTION_PROVIDER)))
                .then(literal("leave"))
                .then(literal("lobby"))
                .then(literal("list"))
                .then(literal("promote")
                        .then(argument("player", EntityArgument.players()).suggests(PARTY_NAME_SUGGESTION_PROVIDER)))
                .then(literal("unban")
                        .then(argument("player", EntityArgument.players()).suggests(PLAYER_NAME_SUGGESTION_PROVIDER)))
                .build();
        addNode(root, node);

        addAlias(root, node, "pa", AliasCommandLevel.SHORT_FORMS);
        addAlias(root, node, "group", AliasCommandLevel.ALL);
    }

    private void addPlayerCommandNodes(RootCommandNode<SharedSuggestionProvider> root) {
        CommandNode<CommandSourceStack> duelNode = literal("duel")
                .then(argument("player", EntityArgument.players()).suggests(PLAYER_NAME_SUGGESTION_PROVIDER))
                .build();
        addNode(root, duelNode);
        addAlias(root, duelNode, "d", AliasCommandLevel.SHORT_FORMS);

        CommandNode<CommandSourceStack> tradeNode = literal("trade")
                .then(argument("player", EntityArgument.players()).suggests(PLAYER_NAME_SUGGESTION_PROVIDER))
                .build();
        addNode(root, tradeNode);
        addAlias(root, tradeNode, "tr", AliasCommandLevel.SHORT_FORMS);

        addNode(
                root,
                literal("find")
                        .then(argument("player", EntityArgument.players()).suggests(PLAYER_NAME_SUGGESTION_PROVIDER))
                        .build());
    }

    private void addToggleCommandNode(RootCommandNode<SharedSuggestionProvider> root) {
        addNode(
                root,
                literal("toggle")
                        .then(literal("100"))
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
                        .then(literal("sb"))
                        .then(literal("swears"))
                        .then(literal("war"))
                        .build());
    }

    private void addDeprecatedCommandNodes(RootCommandNode<SharedSuggestionProvider> root) {
        // "legacystore" aliases
        CommandNode<CommandSourceStack> legacystoreNode = literal("legacystore").build();
        addNode(root, legacystoreNode);

        addAlias(root, legacystoreNode, "buy", AliasCommandLevel.ALL);
        addAlias(root, legacystoreNode, "cash", AliasCommandLevel.ALL);
        addAlias(root, legacystoreNode, "cashshop", AliasCommandLevel.ALL);
        addAlias(root, legacystoreNode, "gc", AliasCommandLevel.ALL);
        addAlias(root, legacystoreNode, "gold", AliasCommandLevel.ALL);
        addAlias(root, legacystoreNode, "goldcoins", AliasCommandLevel.ALL);
        addAlias(root, legacystoreNode, "goldshop", AliasCommandLevel.ALL);

        // "rename" aliases
        CommandNode<CommandSourceStack> renameNode = literal("rename").build();
        addNode(root, renameNode);

        addAlias(root, renameNode, "name", AliasCommandLevel.ALL);
    }

    public enum AliasCommandLevel {
        NONE,
        SHORT_FORMS,
        ALL
    }
}
