/*
 * Copyright © Wynntils 2022.
 * This file is released under AGPLv3. See LICENSE for full license details.
 */
package com.wynntils.commands;

import com.mojang.brigadier.CommandDispatcher;
import com.wynntils.core.config.ui.ConfigScreen;
import com.wynntils.core.features.FeatureRegistry;
import com.wynntils.features.ItemGuessFeature;
import com.wynntils.mc.utils.Delay;
import com.wynntils.mc.utils.commands.CommandBase;
import net.minecraft.client.Minecraft;
import net.minecraft.commands.CommandSourceStack;
import net.minecraft.commands.Commands;

public class ConfigCommand extends CommandBase {

    @Override
    public void register(CommandDispatcher<CommandSourceStack> dispatcher) {
        dispatcher.register(
                Commands.literal("config")
                        .executes(
                                s -> {
                                    new Delay(
                                            () ->
                                                    Minecraft.getInstance()
                                                            .setScreen(
                                                                    new ConfigScreen(
                                                                            FeatureRegistry
                                                                                    .getFeature(
                                                                                            ItemGuessFeature
                                                                                                    .class))),
                                            1);

                                    return 1;
                                }));
    }
}
