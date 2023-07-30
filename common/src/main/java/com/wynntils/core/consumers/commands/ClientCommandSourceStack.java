/*
 * Copyright © Wynntils 2022-2023.
 * This file is released under AGPLv3. See LICENSE for full license details.
 */
package com.wynntils.core.consumers.commands;

import java.util.Collection;
import java.util.Set;
import java.util.stream.Collectors;
import java.util.stream.Stream;
import net.minecraft.client.Minecraft;
import net.minecraft.client.player.LocalPlayer;
import net.minecraft.commands.CommandSourceStack;
import net.minecraft.core.RegistryAccess;
import net.minecraft.resources.ResourceKey;
import net.minecraft.resources.ResourceLocation;
import net.minecraft.world.level.Level;

public class ClientCommandSourceStack extends CommandSourceStack {
    public ClientCommandSourceStack(LocalPlayer player) {
        super(
                player,
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
    public Stream<ResourceLocation> getRecipeNames() {
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
}
