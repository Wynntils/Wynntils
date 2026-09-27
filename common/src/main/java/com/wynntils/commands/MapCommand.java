/*
 * Copyright © Wynntils 2023-2026.
 * This file is released under LGPLv3. See LICENSE for full license details.
 */
package com.wynntils.commands;

import com.mojang.brigadier.arguments.StringArgumentType;
import com.mojang.brigadier.builder.LiteralArgumentBuilder;
import com.mojang.brigadier.context.CommandContext;
import com.mojang.brigadier.suggestion.SuggestionProvider;
import com.wynntils.core.components.Services;
import com.wynntils.core.consumers.commands.Command;
import com.wynntils.services.mapdata.MapDataService;
import com.wynntils.services.mapdata.providers.MapDataProviderInfo;
import com.wynntils.services.mapdata.providers.json.JsonProviderInfo;
import com.wynntils.utils.mc.McUtils;
import java.net.URI;
import java.nio.file.Path;
import java.util.Map;
import java.util.Optional;
import net.minecraft.ChatFormatting;
import net.minecraft.commands.CommandBuildContext;
import net.minecraft.commands.CommandSourceStack;
import net.minecraft.commands.Commands;
import net.minecraft.commands.SharedSuggestionProvider;
import net.minecraft.network.chat.ClickEvent;
import net.minecraft.network.chat.Component;
import net.minecraft.network.chat.HoverEvent;
import net.minecraft.network.chat.MutableComponent;
import net.minecraft.network.chat.Style;
import net.minecraft.util.Util;

public class MapCommand extends Command {
    private static final SuggestionProvider<CommandSourceStack> PROVIDER_SUGGESTION_PROVIDER =
            (context, builder) -> SharedSuggestionProvider.suggest(
                    Services.MapData.getProviderInfos().entrySet().stream()
                            .filter(entry -> entry.getValue().toggleable())
                            .map(Map.Entry::getKey)
                            .toArray(String[]::new),
                    builder);

    private static final SuggestionProvider<CommandSourceStack> REMOVABLE_PROVIDER_SUGGESTION_PROVIDER =
            (context, builder) -> SharedSuggestionProvider.suggest(
                    Services.MapData.getJsonProviderInfos().values().stream()
                            .filter(info -> info.providerType() == JsonProviderInfo.JsonProviderType.REMOTE)
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
                        .then(Commands.literal("remote")
                                .then(Commands.literal("add")
                                        .then(Commands.argument("name", StringArgumentType.string())
                                                .then(Commands.argument("url", StringArgumentType.string())
                                                        .executes(this::addProvider))))
                                .then(Commands.literal("remove")
                                        .then(Commands.argument("name", StringArgumentType.greedyString())
                                                .suggests(REMOVABLE_PROVIDER_SUGGESTION_PROVIDER)
                                                .executes(this::removeProvider))))
                        .then(Commands.literal("local")
                                .then(Commands.literal("folder").executes(this::localProvidersFolder)))
                        .then(Commands.literal("toggle")
                                .then(Commands.argument("name", StringArgumentType.greedyString())
                                        .suggests(PROVIDER_SUGGESTION_PROVIDER)
                                        .executes(this::toggleProvider)))
                        .then(Commands.literal("reload").executes(this::reloadProviders))
                        .then(Commands.literal("list").executes(this::listProviders)));
    }

    private int reloadProviders(CommandContext<CommandSourceStack> context) {
        Services.MapData.reloadData();

        context.getSource()
                .sendSuccess(
                        () -> Component.translatable("command.wynntils.map.reloadProviders")
                                .withStyle(ChatFormatting.GREEN),
                        false);

        return 1;
    }

    private int localProvidersFolder(CommandContext<CommandSourceStack> context) {
        Util.getPlatform().openFile(MapDataService.LOCAL_PROVIDERS);
        return 1;
    }

    private int addProvider(CommandContext<CommandSourceStack> context) {
        String name = context.getArgument("name", String.class);
        String url = context.getArgument("url", String.class);

        URI uri;
        try {
            uri = URI.create(url);
        } catch (IllegalArgumentException e) {
            context.getSource()
                    .sendFailure(Component.translatable("command.wynntils.map.invalidUrl")
                            .withStyle(ChatFormatting.RED));
            return 0;
        }

        String scheme = uri.getScheme();
        if (scheme == null || !(scheme.equals("http") || scheme.equals("https"))) {
            context.getSource()
                    .sendFailure(Component.translatable("command.wynntils.map.invalidUrlScheme")
                            .withStyle(ChatFormatting.RED));
            return 0;
        }

        Services.MapData.addJsonProvider(JsonProviderInfo.createRemote(name, url));
        context.getSource()
                .sendSuccess(
                        () -> Component.translatable("command.wynntils.map.providerAdded")
                                .withStyle(ChatFormatting.GREEN),
                        false);
        return 1;
    }

    private int removeProvider(CommandContext<CommandSourceStack> context) {
        String name = context.getArgument("name", String.class);

        Optional<JsonProviderInfo> providerOpt =
                Optional.ofNullable(Services.MapData.getJsonProviderInfos().get(name));

        if (providerOpt.isEmpty()) {
            context.getSource()
                    .sendFailure(Component.translatable("command.wynntils.map.jsonProviderNotFound")
                            .withStyle(ChatFormatting.RED));
            return 0;
        }

        if (providerOpt.get().providerType() != JsonProviderInfo.JsonProviderType.REMOTE) {
            MutableComponent folderCommand = Component.literal("/wynntils map provider local folder")
                    .withStyle(Style.EMPTY
                            .withColor(ChatFormatting.YELLOW)
                            .withClickEvent(new ClickEvent.RunCommand("/wynntils map provider local folder"))
                            .withHoverEvent(new HoverEvent.ShowText(
                                    Component.translatable("command.wynntils.map.openFolder"))));

            context.getSource()
                    .sendFailure(Component.translatable("command.wynntils.map.onlyRemoteRemovable")
                            .withStyle(ChatFormatting.RED)
                            .append(folderCommand));
            return 0;
        }

        if (!Services.MapData.removeJsonProvider(name)) {
            context.getSource()
                    .sendFailure(Component.translatable("command.wynntils.map.removeFailed")
                            .withStyle(ChatFormatting.RED));
            return 0;
        }

        context.getSource()
                .sendSuccess(
                        () -> Component.translatable("command.wynntils.map.providerRemoved")
                                .withStyle(ChatFormatting.GREEN),
                        false);
        return 1;
    }

    private int listProviders(CommandContext<CommandSourceStack> context) {
        MutableComponent message =
                Component.translatable("command.wynntils.map.providersHeader").withStyle(ChatFormatting.YELLOW);

        for (Map.Entry<String, MapDataProviderInfo> entry :
                Services.MapData.getProviderInfos().entrySet()) {
            String providerId = entry.getKey();
            MapDataProviderInfo providerInfo = entry.getValue();

            boolean enabled = Services.MapData.isProviderEnabled(providerId);
            ChatFormatting statusColor = enabled ? ChatFormatting.GREEN : ChatFormatting.RED;
            String statusKey = enabled ? "command.wynntils.map.enabled" : "command.wynntils.map.disabled";

            message.append("\n")
                    .append(Component.literal(providerId).withStyle(ChatFormatting.GOLD))
                    .append(Component.literal(" (").withStyle(statusColor))
                    .append(Component.translatable(statusKey).withStyle(statusColor))
                    .append(Component.literal(")").withStyle(statusColor))
                    .append(Component.literal(" [")
                            .append(Component.translatable("command.wynntils.map.ownedBy", providerInfo.getOwnerName()))
                            .append("]")
                            .withStyle(ChatFormatting.AQUA));

            if (!providerInfo.toggleable()) {
                message.append(Component.literal(" [")
                        .append(Component.translatable("command.wynntils.map.required"))
                        .append("]")
                        .withStyle(ChatFormatting.DARK_AQUA));
            }

            Optional<JsonProviderInfo> jsonProviderInfo = Services.MapData.getJsonProviderInfoByCompleteId(providerId);
            if (jsonProviderInfo.isEmpty()) continue;

            Optional<MutableComponent> pathComponent = getPathComponent(jsonProviderInfo.get());
            if (pathComponent.isEmpty()) {
                context.getSource()
                        .sendFailure(Component.translatable("command.wynntils.map.invalidUrl")
                                .withStyle(ChatFormatting.RED));
                return 0;
            }

            message.append(Component.literal("\n  ").withStyle(ChatFormatting.GRAY))
                    .append(pathComponent.get());
        }

        context.getSource().sendSuccess(() -> message, false);
        return 1;
    }

    private Optional<MutableComponent> getPathComponent(JsonProviderInfo providerInfo) {
        String path = providerInfo.path();

        switch (providerInfo.providerType()) {
            case LOCAL -> {
                String displayPath = path;
                try {
                    Path fullPath = Path.of(path);
                    Path mcDir = McUtils.getGameDirectory().toPath();
                    if (fullPath.startsWith(mcDir)) {
                        displayPath = mcDir.relativize(fullPath).toString();
                    }
                } catch (Exception e) {
                    // fall back to the full path if anything goes wrong
                }

                return Optional.of(Component.literal(displayPath)
                        .withStyle(Style.EMPTY
                                .withColor(ChatFormatting.WHITE)
                                .withClickEvent(new ClickEvent.OpenFile(path))
                                .withHoverEvent(new HoverEvent.ShowText(
                                        Component.translatable("command.wynntils.map.openFile")))));
            }
            case REMOTE -> {
                URI uri;
                try {
                    uri = URI.create(path);
                } catch (IllegalArgumentException e) {
                    return Optional.empty();
                }

                return Optional.of(Component.literal(path)
                        .withStyle(Style.EMPTY
                                .withColor(ChatFormatting.WHITE)
                                .withClickEvent(new ClickEvent.OpenUrl(uri))
                                .withHoverEvent(new HoverEvent.ShowText(
                                        Component.translatable("command.wynntils.map.openUrl")))));
            }
            default -> {
                return Optional.of(Component.literal(path).withStyle(ChatFormatting.GRAY));
            }
        }
    }

    private int toggleProvider(CommandContext<CommandSourceStack> context) {
        String name = context.getArgument("name", String.class);

        // Also accept the bare name of JSON providers, as used by the other provider commands
        String providerId = Optional.ofNullable(
                        Services.MapData.getJsonProviderInfos().get(name))
                .map(JsonProviderInfo::completeId)
                .orElse(name);

        MapDataProviderInfo providerInfo = Services.MapData.getProviderInfos().get(providerId);

        if (providerInfo == null) {
            context.getSource()
                    .sendFailure(Component.translatable("command.wynntils.map.providerNotFound")
                            .withStyle(ChatFormatting.RED));
            return 0;
        }

        if (!providerInfo.toggleable()) {
            context.getSource()
                    .sendFailure(Component.translatable("command.wynntils.map.providerNotToggleable")
                            .withStyle(ChatFormatting.RED));
            return 0;
        }

        if (Services.MapData.toggleProvider(providerId)) {
            boolean enabled = Services.MapData.isProviderEnabled(providerId);
            ChatFormatting statusColor = enabled ? ChatFormatting.GREEN : ChatFormatting.RED;

            MutableComponent statusComponent = Component.translatable(
                            enabled ? "command.wynntils.map.enabled" : "command.wynntils.map.disabled")
                    .withStyle(statusColor);

            MutableComponent message = Component.translatable("command.wynntils.map.providerToggledPrefix")
                    .withStyle(ChatFormatting.GREEN)
                    .append(Component.literal(providerId).withStyle(ChatFormatting.WHITE))
                    .append(Component.translatable("command.wynntils.map.providerToggledSuffix")
                            .withStyle(ChatFormatting.GREEN))
                    .append(statusComponent);

            context.getSource().sendSuccess(() -> message, false);
            return 1;
        }

        context.getSource()
                .sendFailure(Component.translatable("command.wynntils.map.toggleFailed")
                        .withStyle(ChatFormatting.RED));
        return 0;
    }
}
