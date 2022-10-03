/*
 * Copyright © Wynntils 2022.
 * This file is released under AGPLv3. See LICENSE for full license details.
 */
package com.wynntils.commands;

import com.mojang.brigadier.builder.LiteralArgumentBuilder;
import com.mojang.brigadier.context.CommandContext;
import com.wynntils.core.WynntilsMod;
import com.wynntils.core.commands.CommandBase;
import com.wynntils.core.managers.UpdateManager;
import java.util.concurrent.CompletableFuture;
import net.minecraft.ChatFormatting;
import net.minecraft.commands.CommandSourceStack;
import net.minecraft.commands.Commands;
import net.minecraft.network.chat.TextComponent;

public class UpdateCommand extends CommandBase {
    @Override
    public LiteralArgumentBuilder<CommandSourceStack> getBaseCommandBuilder() {
        return Commands.literal("update").executes(this::update);
    }

    private int update(CommandContext<CommandSourceStack> context) {
        if (WynntilsMod.isDevelopmentEnvironment()) {
            context.getSource()
                    .sendFailure(new TextComponent("Development environment detected, cannot update!")
                            .withStyle(ChatFormatting.DARK_RED));
            WynntilsMod.error("Development environment detected, cannot update!");
            return 0;
        }

        WynntilsMod.info("Attempting to fetch Wynntils update.");
        CompletableFuture<UpdateManager.UpdateResult> completableFuture = UpdateManager.tryUpdate();

        completableFuture.whenComplete((result, throwable) -> {
            switch (result) {
                case SUCCESSFUL -> context.getSource()
                        .sendSuccess(
                                new TextComponent(
                                                "Successfully downloaded Wynntils/Artemis update. It will apply on shutdown.")
                                        .withStyle(ChatFormatting.DARK_GREEN),
                                false);
                case ERROR -> context.getSource()
                        .sendFailure(new TextComponent("Error applying Wynntils/Artemis update.")
                                .withStyle(ChatFormatting.DARK_RED));
                case ALREADY_ON_LATEST -> context.getSource()
                        .sendSuccess(
                                new TextComponent("Wynntils/Artemis is already on latest version.")
                                        .withStyle(ChatFormatting.YELLOW),
                                false);
            }
        });

        return 1;
    }
}
