/*
 * Copyright © Wynntils 2022.
 * This file is released under AGPLv3. See LICENSE for full license details.
 */
package com.wynntils.features.user;

import com.mojang.brigadier.tree.CommandNode;
import com.mojang.brigadier.tree.RootCommandNode;
import com.wynntils.core.features.UserFeature;
import com.wynntils.mc.event.CommandsPacketEvent;
import java.util.Arrays;
import java.util.List;
import net.minecraft.commands.SharedSuggestionProvider;
import net.minecraftforge.eventbus.api.SubscribeEvent;

public class FilterAdminCommandsFeature extends UserFeature {
    private static final List<String> FILTERED_COMMANDS = Arrays.asList(new String[] {
        "bungee",
        "connect",
        "galert",
        "gcountdown",
        "glist",
        "gsend",
        "perms",
        "pfind",
        "plist",
        "pwlist",
        "sendtoall",
        "servers",
        "sparkb",
        "sparkbungee",
        "wcl",
        "wynnproxy"
    });

    @SubscribeEvent
    public void onCommandPacket(CommandsPacketEvent event) {
        RootCommandNode<SharedSuggestionProvider> root = event.getRoot();

        RootCommandNode<SharedSuggestionProvider> newRoot = new RootCommandNode<>();
        for (CommandNode<SharedSuggestionProvider> child : root.getChildren()) {
            if (!FILTERED_COMMANDS.contains(child.getName())) {
                newRoot.addChild(child);
            }
        }

        // We also need to add the arguments for all commands
        newRoot.addChild(root.getChild("args"));
        event.setRoot(newRoot);
    }
}
