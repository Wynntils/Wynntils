/*
 * Copyright © Wynntils 2023-2026.
 * This file is released under LGPLv3. See LICENSE for full license details.
 */
package com.wynntils.commands;

import com.mojang.brigadier.arguments.StringArgumentType;
import com.mojang.brigadier.builder.LiteralArgumentBuilder;
import com.mojang.brigadier.context.CommandContext;
import com.mojang.brigadier.suggestion.SuggestionProvider;
import com.wynntils.core.components.Managers;
import com.wynntils.core.components.Services;
import com.wynntils.core.consumers.commands.Command;
import com.wynntils.screens.maps.managers.ProviderManagementScreen;
import com.wynntils.screens.maps.paths.PathCreationScreen;
import com.wynntils.services.mapdata.MapDataService;
import com.wynntils.services.mapdata.MapPathsService;
import com.wynntils.services.mapdata.attributes.MapAttributesBuilder;
import com.wynntils.services.mapdata.paths.PathCategoriesProvider;
import com.wynntils.services.mapdata.providers.MapDataProviderInfo;
import com.wynntils.services.mapdata.providers.json.JsonProviderInfo;
import com.wynntils.utils.mc.McUtils;
import java.net.URI;
import java.nio.file.Path;
import java.util.ArrayList;
import java.util.List;
import java.util.Map;
import java.util.Optional;
import net.minecraft.ChatFormatting;
import net.minecraft.commands.CommandBuildContext;
import net.minecraft.commands.CommandSourceStack;
import net.minecraft.commands.Commands;
import net.minecraft.commands.SharedSuggestionProvider;
import net.minecraft.commands.arguments.coordinates.BlockPosArgument;
import net.minecraft.core.BlockPos;
import net.minecraft.network.chat.ClickEvent;
import net.minecraft.network.chat.Component;
import net.minecraft.network.chat.HoverEvent;
import net.minecraft.network.chat.MutableComponent;
import net.minecraft.network.chat.Style;
import net.minecraft.util.Util;
import net.minecraft.world.phys.Vec3;

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
                .then(buildPathCommand())
                .then(Commands.literal("provider")
                        .then(Commands.literal("browse").executes(context1 -> openProviderScreen()))
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

        List<Map.Entry<String, MapDataProviderInfo>> builtInProviders = new ArrayList<>();
        List<Map.Entry<String, MapDataProviderInfo>> bundledProviders = new ArrayList<>();
        List<Map.Entry<String, MapDataProviderInfo>> localProviders = new ArrayList<>();
        List<Map.Entry<String, MapDataProviderInfo>> remoteProviders = new ArrayList<>();

        for (Map.Entry<String, MapDataProviderInfo> entry :
                Services.MapData.getProviderInfos().entrySet()) {
            Optional<JsonProviderInfo> jsonProviderInfo =
                    Services.MapData.getJsonProviderInfoByCompleteId(entry.getKey());

            if (jsonProviderInfo.isEmpty()) {
                builtInProviders.add(entry);
                continue;
            }

            switch (jsonProviderInfo.get().providerType()) {
                case BUNDLED -> bundledProviders.add(entry);
                case LOCAL -> localProviders.add(entry);
                case REMOTE -> remoteProviders.add(entry);
            }
        }

        boolean firstSection = true;

        if (!builtInProviders.isEmpty()) {
            if (appendProviderSection(
                            context, message, "command.wynntils.map.builtInHeader", builtInProviders, firstSection)
                    == 0) {
                return 0;
            }
            firstSection = false;
        }
        if (!bundledProviders.isEmpty()) {
            if (appendProviderSection(
                            context, message, "command.wynntils.map.bundledHeader", bundledProviders, firstSection)
                    == 0) {
                return 0;
            }
            firstSection = false;
        }
        if (!localProviders.isEmpty()) {
            if (appendProviderSection(
                            context, message, "command.wynntils.map.localHeader", localProviders, firstSection)
                    == 0) {
                return 0;
            }
            firstSection = false;
        }
        if (!remoteProviders.isEmpty()) {
            if (appendProviderSection(
                            context, message, "command.wynntils.map.remoteHeader", remoteProviders, firstSection)
                    == 0) {
                return 0;
            }
        }

        context.getSource().sendSuccess(() -> message, false);
        return 1;
    }

    private int appendProviderSection(
            CommandContext<CommandSourceStack> context,
            MutableComponent message,
            String headerKey,
            List<Map.Entry<String, MapDataProviderInfo>> entries,
            boolean firstSection) {
        if (!firstSection) {
            message.append("\n");
        }

        message.append("\n").append(Component.translatable(headerKey).withStyle(ChatFormatting.YELLOW));

        for (Map.Entry<String, MapDataProviderInfo> entry : entries) {
            String providerId = entry.getKey();
            MapDataProviderInfo providerInfo = entry.getValue();

            boolean enabled = Services.MapData.isProviderEnabled(providerId);
            ChatFormatting statusColor = enabled ? ChatFormatting.GREEN : ChatFormatting.RED;
            String statusKey = enabled ? "command.wynntils.map.enabled" : "command.wynntils.map.disabled";

            message.append("\n")
                    .append(Component.literal(providerId)
                            .withStyle(Style.EMPTY
                                    .withColor(ChatFormatting.GOLD)
                                    .withHoverEvent(new HoverEvent.ShowText(Component.translatable(
                                                    "command.wynntils.map.ownedBy", providerInfo.getOwnerName())
                                            .withStyle(ChatFormatting.AQUA)))))
                    .append(Component.literal(" (").withStyle(statusColor))
                    .append(Component.translatable(statusKey).withStyle(statusColor))
                    .append(Component.literal(")").withStyle(statusColor));

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

    private static LiteralArgumentBuilder<CommandSourceStack> buildPathCommand() {
        LiteralArgumentBuilder<CommandSourceStack> root =
                Commands.literal("path").executes(context -> openPathScreen());
        root.then(Commands.literal("screen").executes(context -> openPathScreen()));
        root.then(Commands.literal("record").executes(MapCommand::recordPath));
        root.then(Commands.literal("cancel").executes(MapCommand::cancelPath));
        root.then(Commands.literal("save")
                .then(Commands.argument("label", StringArgumentType.string()).executes(MapCommand::savePath)));
        root.then(Commands.literal("edit")
                .then(Commands.argument("name", StringArgumentType.string())
                        .suggests((context, builder) -> SharedSuggestionProvider.suggest(
                                Services.MapPaths.getStoredPaths().stream()
                                        .map(path -> StringArgumentType.escapeIfRequired(path.label())),
                                builder))
                        .executes(MapCommand::editPath)));
        root.then(Commands.literal("highlight")
                .then(Commands.literal("add")
                        .then(Commands.argument("pos", BlockPosArgument.blockPos())
                                .executes(context ->
                                        addHighlight(context, BlockPosArgument.getBlockPos(context, "pos")))))
                .then(Commands.literal("remove")
                        .then(Commands.argument("pos", BlockPosArgument.blockPos())
                                .executes(MapCommand::removeHighlight))));
        LiteralArgumentBuilder<CommandSourceStack> notes = Commands.literal("note");
        notes.then(Commands.literal("add")
                .then(Commands.argument("text", StringArgumentType.greedyString())
                        .executes(MapCommand::addNote)));
        notes.then(Commands.literal("list").executes(MapCommand::listNotes));
        notes.then(Commands.literal("delete")
                .then(Commands.argument("pos", BlockPosArgument.blockPos()).executes(MapCommand::deleteNote)));
        root.then(notes);

        return root;
    }

    private static int cancelPath(CommandContext<CommandSourceStack> context) {
        Services.MapPaths.cancel();

        return pathSuccess(context);
    }

    private static int savePath(CommandContext<CommandSourceStack> context) {
        if (Services.MapPaths.save(StringArgumentType.getString(context, "label"))
                .isEmpty()) {
            context.getSource()
                    .sendFailure(Component.translatable("command.wynntils.map.path.saveFailed")
                            .withStyle(ChatFormatting.RED));

            return 0;
        }

        return pathSuccess(context);
    }

    private static int editPath(CommandContext<CommandSourceStack> context) {
        Optional<MapPathsService.StoredPath> path =
                Services.MapPaths.findPathByName(StringArgumentType.getString(context, "name"));
        if (path.isEmpty()) {
            return pathError(context, "Path not found or multiple paths match; use the feature ID instead");
        }

        if (!Services.MapPaths.edit(path.get())) {
            return pathError(context, "Save or cancel your current path before editing another");
        }

        openPathScreen();

        return pathSuccess(context);
    }

    private static int addNote(CommandContext<CommandSourceStack> context) {
        if (!Services.MapPaths.addNote(Component.literal(StringArgumentType.getString(context, "text")))) {
            return pathError(context, "Start recording or edit a path before adding notes");
        }

        return pathSuccess(context);
    }

    private static int listNotes(CommandContext<CommandSourceStack> context) {
        Services.MapPaths.getNotes()
                .forEach(note -> context.getSource()
                        .sendSuccess(
                                () -> Component.literal(BlockPos.containing(note.position())
                                                        .toShortString() + ": ")
                                        .withStyle(ChatFormatting.GREEN)
                                        .append(note.component()),
                                false));

        return pathSuccess(context);
    }

    private static int deleteNote(CommandContext<CommandSourceStack> context) {
        if (!Services.MapPaths.deleteNote(BlockPosArgument.getBlockPos(context, "pos"))) {
            return pathError(context, "Note not found");
        }

        return pathSuccess(context);
    }

    private static int addHighlight(CommandContext<CommandSourceStack> context, BlockPos pos) {
        if (!Services.MapPaths.isRecording()) {
            return pathError(context, "Start recording a path before adding highlights");
        }

        if (!Services.MapPaths.addHighlight(Vec3.atLowerCornerOf(pos))) {
            return pathError(context, "A highlight already exists at this position");
        }

        return pathSuccess(context);
    }

    private static int removeHighlight(CommandContext<CommandSourceStack> context) {
        if (!Services.MapPaths.removeHighlight(BlockPosArgument.getBlockPos(context, "pos"))) {
            return pathError(context, "Highlight not found");
        }

        return pathSuccess(context);
    }

    private static int recordPath(CommandContext<CommandSourceStack> context) {
        if (!Services.MapPaths.hasDraft()) {
            Services.MapPaths.configure(
                    PathCategoriesProvider.PATH_CATEGORY,
                    "",
                    new MapAttributesBuilder().asPathAttributes().build(),
                    true);
        }

        if (!Services.MapPaths.startRecording()) return 0;

        context.getSource()
                .sendSuccess(
                        () -> Component.translatable("command.wynntils.map.path.recordingStarted")
                                .withStyle(ChatFormatting.GREEN),
                        false);

        return 1;
    }

    private static int openPathScreen() {
        Managers.TickScheduler.scheduleLater(() -> McUtils.setScreen(PathCreationScreen.create(null)), 2);

        return 1;
    }

    private static int openProviderScreen() {
        Managers.TickScheduler.scheduleLater(() -> McUtils.setScreen(ProviderManagementScreen.create(null)), 2);

        return 1;
    }

    private static int pathSuccess(CommandContext<CommandSourceStack> context) {
        context.getSource()
                .sendSuccess(
                        () -> Component.translatable("command.wynntils.map.path.success")
                                .withStyle(ChatFormatting.GREEN),
                        false);

        return 1;
    }

    private static int pathError(CommandContext<CommandSourceStack> context, String message) {
        context.getSource()
                .sendFailure(Component.translatable("command.wynntils.map.path.error", message)
                        .withStyle(ChatFormatting.RED));

        return 0;
    }
}
