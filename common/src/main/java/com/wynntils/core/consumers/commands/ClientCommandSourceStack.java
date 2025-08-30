/*
 * Copyright © Wynntils 2022-2025.
 * This file is released under LGPLv3. See LICENSE for full license details.
 */
package com.wynntils.core.consumers.commands;

import com.wynntils.utils.mc.McUtils;
import java.util.Collection;
import java.util.Set;
import java.util.stream.Collectors;
import net.minecraft.client.Minecraft;
import net.minecraft.client.player.LocalPlayer;
import net.minecraft.commands.CommandSource;
import net.minecraft.commands.CommandSourceStack;
import net.minecraft.core.RegistryAccess;
import net.minecraft.network.chat.Component;
import net.minecraft.resources.ResourceKey;
import net.minecraft.world.level.Level;

public class ClientCommandSourceStack extends CommandSourceStack {
    public ClientCommandSourceStack(LocalPlayer player) {
        super(
                new ClientCommandSource(),
                player.position(),
                player.getRotationVector(),
                null,
                0,
                player.getDisplayName().toString(),
                player.getName(),
                null,
                player);
    }

    @Override
    public Collection<String> getOnlinePlayerNames() {
        return Minecraft.getInstance().getConnection().getOnlinePlayers().stream()
                .map(e -> e.getProfile().getName())
                .collect(Collectors.toList());
    }

    @Override
    public Collection<String> getAllTeams() {
        return null;
    }

    @Override
    public Set<ResourceKey<Level>> levels() {
        return null;
    }

    @Override
    public RegistryAccess registryAccess() {
        return null;
    }

    private static class ClientCommandSource implements CommandSource {
        @Override
        public void sendSystemMessage(Component component) {
            McUtils.sendMessageToClient(component);
        }

        @Override
        public boolean acceptsSuccess() {
            return true;
        }

        @Override
        public boolean acceptsFailure() {
            return true;
        }

        @Override
        public boolean shouldInformAdmins() {
            return true;
        }
    }
}
