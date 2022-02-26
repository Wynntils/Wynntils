/*
 * Copyright © Wynntils 2022.
 * This file is released under AGPLv3. See LICENSE for full license details.
 */
package com.wynntils.core.commands;

import java.util.Collection;
import java.util.stream.Collectors;
import net.minecraft.client.Minecraft;
import net.minecraft.client.player.LocalPlayer;
import net.minecraft.commands.CommandSourceStack;

public class FakeCommandSource extends CommandSourceStack {
    public FakeCommandSource(LocalPlayer player) {
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
}
