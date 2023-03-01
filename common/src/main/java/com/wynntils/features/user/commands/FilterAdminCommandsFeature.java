/*
 * Copyright © Wynntils 2022.
 * This file is released under AGPLv3. See LICENSE for full license details.
 */
package com.wynntils.features.user.commands;

import com.mojang.brigadier.tree.CommandNode;
import com.mojang.brigadier.tree.RootCommandNode;
import com.wynntils.core.features.UserFeature;
import com.wynntils.core.features.properties.FeatureCategory;
import com.wynntils.core.features.properties.FeatureInfo;
import com.wynntils.mc.event.CommandsPacketEvent;
import java.util.Set;
import net.minecraft.commands.SharedSuggestionProvider;
import net.minecraftforge.eventbus.api.EventPriority;
import net.minecraftforge.eventbus.api.SubscribeEvent;

@FeatureInfo(category = FeatureCategory.COMMANDS)
public class FilterAdminCommandsFeature extends UserFeature {
    private static final Set<String> FILTERED_COMMANDS = Set.of(
            "bungee",
            "change",
            "connect",
            "galert",
            "gcountdown",
            "glist",
            "gsend",
            "lobby",
            "perms",
            "pfind",
            "plist",
            "pwlist",
            "sendtoall",
            "servers",
            "sparkb",
            "sparkbungee",
            "wcl",
            "wynnproxy");

    @SubscribeEvent(priority = EventPriority.HIGH)
    public void onCommandPacket(CommandsPacketEvent event) {
        RootCommandNode<SharedSuggestionProvider> root = event.getRoot();

        RootCommandNode<SharedSuggestionProvider> newRoot = new RootCommandNode<>();
        for (CommandNode<SharedSuggestionProvider> child : root.getChildren()) {
            if (!FILTERED_COMMANDS.contains(child.getName())) {
                newRoot.addChild(child);
            }
        }
        event.setRoot(newRoot);
    }
}
