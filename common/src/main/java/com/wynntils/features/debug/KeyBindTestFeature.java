/*
 * Copyright © Wynntils 2022.
 * This file is released under AGPLv3. See LICENSE for full license details.
 */
package com.wynntils.features.debug;

import com.google.common.collect.ImmutableList;
import com.mojang.blaze3d.platform.InputConstants;
import com.wynntils.core.features.DebugFeature;
import com.wynntils.mc.utils.ComponentUtils;
import com.wynntils.mc.utils.McUtils;
import com.wynntils.mc.utils.keybinds.KeyHolder;
import java.util.Optional;
import net.minecraft.client.Minecraft;
import net.minecraft.client.player.AbstractClientPlayer;
import net.minecraft.network.chat.TextComponent;
import net.minecraft.world.scores.Team;

public class KeyBindTestFeature extends DebugFeature {
    @Override
    protected void init(
            ImmutableList.Builder<WebProviderSupplier> apis,
            ImmutableList.Builder<KeySupplier> keybinds,
            ImmutableList.Builder<Condition> conditions) {
        super.init(apis, keybinds, conditions);
        keybinds.add(
                () ->
                        new KeyHolder(
                                "Add Splash Text",
                                InputConstants.UNKNOWN.getValue(),
                                "WynntilsTest",
                                false,
                                () -> {
                                    McUtils.sendMessageToClient(
                                            new TextComponent(
                                                    Minecraft.getInstance()
                                                            .getSplashManager()
                                                            .getSplash()));
                                }));
        keybinds.add(
                () ->
                        new KeyHolder(
                                "Get Player Info",
                                InputConstants.UNKNOWN.getValue(),
                                "WynntilsTest",
                                true,
                                () -> {
                                    for (AbstractClientPlayer player :
                                            McUtils.mc().level.players()) {
                                        McUtils.sendMessageToClient(
                                                new TextComponent(
                                                        String.format(
                                                                "\"%s\" has team \"%s\" with name"
                                                                        + " \"%s\"",
                                                                player.getScoreboardName(),
                                                                Optional.ofNullable(
                                                                                player.getTeam())
                                                                        .map(Team::getName)
                                                                        .orElse("n/a"),
                                                                ComponentUtils.fromComponent(
                                                                        player.getDisplayName()))));
                                    }
                                }));
    }
}
