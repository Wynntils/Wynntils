/*
 * Copyright © Wynntils 2022.
 * This file is released under AGPLv3. See LICENSE for full license details.
 */
package com.wynntils.commands;

import com.mojang.brigadier.builder.LiteralArgumentBuilder;
import com.mojang.brigadier.context.CommandContext;
import com.wynntils.core.WynntilsMod;
import com.wynntils.core.commands.Command;
import com.wynntils.core.components.Managers;
import com.wynntils.core.net.athena.UpdateManager;
import com.wynntils.utils.mc.McUtils;
import java.util.concurrent.CompletableFuture;
import net.minecraft.ChatFormatting;
import net.minecraft.commands.CommandSourceStack;
import net.minecraft.commands.Commands;
import net.minecraft.network.chat.Component;

public class UpdateCommand extends Command {
    @Override
    public LiteralArgumentBuilder<CommandSourceStack> getBaseCommandBuilder() {
        return Commands.literal("update").executes(this::update);
    }

    private int update(CommandContext<CommandSourceStack> context) {
        if (WynntilsMod.isDevelopmentEnvironment()) {
            context.getSource()
                    .sendFailure(Component.translatable("feature.wynntils.updates.error.development")
                            .withStyle(ChatFormatting.DARK_RED));
            WynntilsMod.error("Development environment detected, cannot update!");
            return 0;
        }

        CompletableFuture.runAsync(() -> {
            WynntilsMod.info("Attempting to fetch Wynntils update.");
            CompletableFuture<UpdateManager.UpdateResult> completableFuture = Managers.Update.tryUpdate();

            completableFuture.whenComplete((result, throwable) -> {
                switch (result) {
                    case SUCCESSFUL -> McUtils.sendMessageToClient(
                            Component.translatable("feature.wynntils.updates.result.successful")
                                    .withStyle(ChatFormatting.DARK_GREEN));
                    case ERROR -> McUtils.sendMessageToClient(
                            Component.translatable("feature.wynntils.updates.result.error")
                                    .withStyle(ChatFormatting.DARK_RED));
                    case ALREADY_ON_LATEST -> McUtils.sendMessageToClient(
                            Component.translatable("feature.wynntils.updates.result.latest")
                                    .withStyle(ChatFormatting.YELLOW));
                    case UPDATE_PENDING -> McUtils.sendMessageToClient(
                            Component.translatable("feature.wynntils.updates.result.pending")
                                    .withStyle(ChatFormatting.YELLOW));
                }
            });
        });

        context.getSource()
                .sendSuccess(
                        Component.translatable("feature.wynntils.updates.checking")
                                .withStyle(ChatFormatting.GREEN),
                        false);

        return 1;
    }
}
