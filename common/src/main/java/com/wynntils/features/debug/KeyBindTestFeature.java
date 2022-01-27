/*
 * Copyright © Wynntils 2022.
 * This file is released under AGPLv3. See LICENSE for full license details.
 */
package com.wynntils.features.debug;

import com.google.common.collect.ImmutableList;
import com.mojang.blaze3d.platform.InputConstants;
import com.wynntils.core.features.DebugFeature;
import com.wynntils.mc.utils.keybinds.KeyHolder;
import net.minecraft.client.Minecraft;
import net.minecraft.network.chat.TextComponent;

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
                                    Minecraft.getInstance()
                                            .player
                                            .sendMessage(
                                                    new TextComponent(
                                                            Minecraft.getInstance()
                                                                    .getSplashManager()
                                                                    .getSplash()),
                                                    null);
                                }));
        keybinds.add(
                () ->
                        new KeyHolder(
                                "Add Sticky Splash Text",
                                InputConstants.UNKNOWN.getValue(),
                                "WynntilsTest",
                                true,
                                () -> {
                                    Minecraft.getInstance()
                                            .player
                                            .sendMessage(
                                                    new TextComponent(
                                                            Minecraft.getInstance()
                                                                    .getSplashManager()
                                                                    .getSplash()),
                                                    null);
                                }));
    }
}
