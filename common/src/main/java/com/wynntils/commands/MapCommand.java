/*
 * Copyright © Wynntils 2023-2025.
 * This file is released under LGPLv3. See LICENSE for full license details.
 */
package com.wynntils.commands;

import com.mojang.brigadier.arguments.StringArgumentType;
import com.mojang.brigadier.builder.LiteralArgumentBuilder;
import com.mojang.brigadier.context.CommandContext;
import com.mojang.brigadier.suggestion.SuggestionProvider;
import com.wynntils.core.components.Services;
import com.wynntils.core.consumers.commands.Command;
import com.wynntils.services.mapdata.providers.json.JsonProviderInfo;
import java.io.File;
import java.net.URI;
import java.util.Optional;
import net.minecraft.ChatFormatting;
import net.minecraft.commands.CommandBuildContext;
import net.minecraft.commands.CommandSourceStack;
import net.minecraft.commands.Commands;
import net.minecraft.commands.SharedSuggestionProvider;
import net.minecraft.network.chat.Component;
import net.minecraft.network.chat.MutableComponent;

public class MapCommand extends Command {
    private static final SuggestionProvider<CommandSourceStack> PROVIDER_SUGGESTION_PROVIDER =
            (context, builder) -> SharedSuggestionProvider.suggest(
                    Services.MapData.getJsonProviderInfos().keySet().stream()
                            .map(JsonProviderInfo::providerId)
                            .toArray(String[]::new),
                    builder);

    @Override
    public String getCommandName() {
        return "map";
    }

    @Override
    public LiteralArgumentBuilder<CommandSourceStack> getCommandBuilder(
            LiteralArgumentBuilder<CommandSourceStack> base, CommandBuildContext context) {
        return Commands.literal("map")
                .then(Commands.literal("provider")
                        .then(Commands.literal("add")
                                .then(Commands.argument("name", StringArgumentType.string())
                                        .then(Commands.argument("urlOrPath", StringArgumentType.string())
                                                .executes(this::addProvider))))
                        .then(Commands.literal("remove")
                                .then(Commands.argument("providerId", StringArgumentType.greedyString())
                                        .suggests(PROVIDER_SUGGESTION_PROVIDER)
                                        .executes(this::removeProvider)))
                        .then(Commands.literal("list").executes(this::listPoiProviders))
                        .then(Commands.literal("reload").executes(this::reloadProviders))
                        .then(Commands.literal("toggle")
                                .then(Commands.argument("providerId", StringArgumentType.greedyString())
                                        .suggests(PROVIDER_SUGGESTION_PROVIDER)
                                        .executes(this::toggleProvider))));
    }

    private int reloadProviders(CommandContext<CommandSourceStack> context) {
        Services.MapData.reloadData();

        context.getSource()
                .sendSuccess(
                        () -> Component.literal("Successfully reloaded all mapdata providers.")
                                .withStyle(ChatFormatting.GREEN),
                        false);

        return 1;
    }

    private int addProvider(CommandContext<CommandSourceStack> context) {
        String name = context.getArgument("name", String.class);
        String urlOrPath = context.getArgument("urlOrPath", String.class);

        try {
            URI uri = URI.create(urlOrPath);
            Services.MapData.addJsonProvider(JsonProviderInfo.createRemote(name, urlOrPath));
            context.getSource()
                    .sendSuccess(
                            () -> Component.literal("Successfully added remote mapdata provider.")
                                    .withStyle(ChatFormatting.GREEN),
                            false);
            return 1;
        } catch (IllegalArgumentException e) {
            // continue, it might be a file
        }

        File file = new File(urlOrPath);
        if (file.exists()) {
            Services.MapData.addJsonProvider(JsonProviderInfo.createLocal(name, urlOrPath));
            context.getSource()
                    .sendSuccess(
                            () -> Component.literal("Successfully added local mapdata provider.")
                                    .withStyle(ChatFormatting.GREEN),
                            false);
            return 1;
        }

        context.getSource()
                .sendFailure(Component.literal("The provided URL or path is not valid.")
                        .withStyle(ChatFormatting.RED));

        return 0;
    }

    private int removeProvider(CommandContext<CommandSourceStack> context) {
        String name = context.getArgument("providerId", String.class);

        if (!Services.MapData.removeJsonProvider(name)) {
            context.getSource()
                    .sendFailure(Component.literal("The provided name does not match any mapdata provider.")
                            .withStyle(ChatFormatting.RED));
            return 0;
        }

        context.getSource()
                .sendSuccess(
                        () -> Component.literal("Successfully removed mapdata provider.")
                                .withStyle(ChatFormatting.GREEN),
                        false);
        return 1;
    }

    private int listPoiProviders(CommandContext<CommandSourceStack> context) {
        MutableComponent message = Component.literal("Mapdata providers: ").withStyle(ChatFormatting.YELLOW);

        for (JsonProviderInfo providerInfo :
                Services.MapData.getJsonProviderInfos().keySet()) {
            boolean enabled = Services.MapData.isJsonProviderEnabled(providerInfo.providerId());

            message.append(Component.literal("\n"));
            message.append(Component.literal(providerInfo.providerId()).withStyle(ChatFormatting.GOLD));
            message.append(Component.literal(enabled ? " (enabled)" : " (disabled)")
                    .withStyle(enabled ? ChatFormatting.GREEN : ChatFormatting.RED));
            message.append(Component.empty());
            message.append(Component.literal(" (").withStyle(ChatFormatting.GRAY));
            message.append(Component.literal(providerInfo.path()).withStyle(ChatFormatting.GRAY));
            message.append(Component.literal(")").withStyle(ChatFormatting.GRAY));
        }

        context.getSource().sendSuccess(() -> message, false);
        return 1;
    }

    private int toggleProvider(CommandContext<CommandSourceStack> context) {
        String providerId = context.getArgument("providerId", String.class);

        Optional<JsonProviderInfo> providerOpt = Services.MapData.getJsonProviderInfos().keySet().stream()
                .filter(provider -> provider.providerId().equals(providerId))
                .findFirst();

        if (providerOpt.isEmpty()) {
            context.getSource()
                    .sendFailure(Component.literal("The provided id does not match any mapdata provider.")
                            .withStyle(ChatFormatting.RED));
            return 0;
        }

        if (Services.MapData.toggleJsonProvider(providerOpt.get().providerId())) {
            context.getSource()
                    .sendSuccess(
                            () -> Component.literal("Successfully toggled mapdata provider ")
                                    .append(Component.literal(providerId).withStyle(ChatFormatting.GREEN))
                                    .append(Component.literal(" to "))
                                    .append(Component.literal(
                                                    Services.MapData.isJsonProviderEnabled(providerId)
                                                            ? "enabled"
                                                            : "disabled")
                                            .withStyle(ChatFormatting.UNDERLINE)),
                            false);
            return 1;
        }

        context.getSource()
                .sendFailure(Component.literal("Could not toggle the mapdata provider with the provided id.")
                        .withStyle(ChatFormatting.RED));
        return 0;
    }
}
