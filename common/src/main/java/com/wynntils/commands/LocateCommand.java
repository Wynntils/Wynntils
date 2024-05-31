/*
 * Copyright © Wynntils 2022-2024.
 * This file is released under LGPLv3. See LICENSE for full license details.
 */
package com.wynntils.commands;

import com.mojang.brigadier.arguments.StringArgumentType;
import com.mojang.brigadier.builder.LiteralArgumentBuilder;
import com.mojang.brigadier.context.CommandContext;
import com.mojang.brigadier.suggestion.SuggestionProvider;
import com.wynntils.core.components.Services;
import com.wynntils.core.consumers.commands.Command;
import com.wynntils.services.map.type.ServiceKind;
import com.wynntils.services.mapdata.type.MapLocation;
import com.wynntils.utils.StringUtils;
import com.wynntils.utils.mc.McUtils;
import java.util.Arrays;
import java.util.Comparator;
import java.util.List;
import java.util.Optional;
import net.minecraft.ChatFormatting;
import net.minecraft.commands.CommandSourceStack;
import net.minecraft.commands.Commands;
import net.minecraft.commands.SharedSuggestionProvider;
import net.minecraft.network.chat.ClickEvent;
import net.minecraft.network.chat.Component;
import net.minecraft.network.chat.MutableComponent;
import net.minecraft.world.phys.Vec3;

public class LocateCommand extends Command {
    public static final SuggestionProvider<CommandSourceStack> SERVICE_SUGGESTION_PROVIDER = (context, builder) ->
            SharedSuggestionProvider.suggest(Arrays.stream(ServiceKind.values()).map(ServiceKind::getName), builder);

    public static final SuggestionProvider<CommandSourceStack> PLACES_SUGGESTION_PROVIDER =
            (context, builder) -> SharedSuggestionProvider.suggest(
                    Services.MapData.PLACE_LIST_PROVIDER.getFeatures().map(f -> Services.MapData.resolveMapAttributes(f)
                            .label()),
                    builder);

    @Override
    public String getCommandName() {
        return "locate";
    }

    @Override
    public LiteralArgumentBuilder<CommandSourceStack> getCommandBuilder(
            LiteralArgumentBuilder<CommandSourceStack> base) {
        return base.then(Commands.literal("service")
                        .then(Commands.argument("name", StringArgumentType.greedyString())
                                .suggests(LocateCommand.SERVICE_SUGGESTION_PROVIDER)
                                .executes(this::locateService)))
                .then(Commands.literal("place")
                        .then(Commands.argument("name", StringArgumentType.greedyString())
                                .suggests(LocateCommand.PLACES_SUGGESTION_PROVIDER)
                                .executes(this::locatePlace)))
                .then(Commands.literal("npc")
                        .then(Commands.argument("name", StringArgumentType.greedyString())
                                .executes(this::notImplemented)))
                .then(Commands.literal("other")
                        .then(Commands.argument("name", StringArgumentType.greedyString())
                                .executes(this::notImplemented)))
                .executes(this::syntaxError);
    }

    // This is shared between /locate and /compass
    public static ServiceKind getServiceKind(CommandContext<CommandSourceStack> context, String searchedName) {
        List<ServiceKind> matchedKinds = Arrays.stream(ServiceKind.values())
                .filter(kind -> StringUtils.partialMatch(kind.getName(), searchedName))
                .toList();

        if (matchedKinds.isEmpty()) {
            MutableComponent response = Component.literal("Found no services matching '" + searchedName + "'")
                    .withStyle(ChatFormatting.RED);
            context.getSource().sendFailure(response);
            return null;
        }

        if (matchedKinds.size() > 1) {
            // Do we have an exact match for any of these?
            Optional<ServiceKind> exactMatch = matchedKinds.stream()
                    .filter(k -> k.getName().equals(searchedName))
                    .findFirst();
            if (exactMatch.isPresent()) {
                return exactMatch.get();
            }

            MutableComponent response = Component.literal("Found multiple services matching '" + searchedName
                            + "'. Pleace specify with more detail. Matching: ")
                    .withStyle(ChatFormatting.RED);
            response.append(Component.literal(String.join(
                    ", ", matchedKinds.stream().map(ServiceKind::getName).toList())));
            context.getSource().sendFailure(response);
            return null;
        }

        // Got exactly one match
        return matchedKinds.get(0);
    }

    private int locateService(CommandContext<CommandSourceStack> context) {
        String searchedName = context.getArgument("name", String.class);

        ServiceKind selectedKind = LocateCommand.getServiceKind(context, searchedName);
        if (selectedKind == null) return 0;

        // Only keep the 4 closest results
        Vec3 currentPosition = McUtils.player().position();

        List<MapLocation> services = Services.MapData.SERVICE_LIST_PROVIDER
                .getFeatures()
                .filter(f1 -> f1.getCategoryId().startsWith("wynntils:service:" + selectedKind.getMapDataId()))
                .map(f -> (MapLocation) f)
                .sorted(Comparator.comparingDouble(loc -> currentPosition.distanceToSqr(
                        loc.getLocation().x(),
                        loc.getLocation().y(),
                        loc.getLocation().z())))
                .limit(4)
                .toList();

        MutableComponent response = Component.literal("Found " + selectedKind.getName() + " services:")
                .withStyle(ChatFormatting.AQUA);

        for (MapLocation service : services) {
            response.append(Component.literal("\n - ").withStyle(ChatFormatting.GRAY))
                    .append(Component.literal(Services.MapData.resolveMapAttributes(service)
                                            .label() + " ")
                            .withStyle(ChatFormatting.YELLOW)
                            .withStyle((style) -> style.withClickEvent(new ClickEvent(
                                    ClickEvent.Action.RUN_COMMAND,
                                    "/compass at " + service.getLocation().asChatCoordinates()))))
                    .append(Component.literal(service.getLocation().toString())
                            .withStyle(ChatFormatting.WHITE)
                            .withStyle((style) -> style.withClickEvent(new ClickEvent(
                                    ClickEvent.Action.RUN_COMMAND,
                                    "/compass at " + service.getLocation().asChatCoordinates()))));
        }

        context.getSource().sendSuccess(() -> response, false);
        return 1;
    }

    private int locatePlace(CommandContext<CommandSourceStack> context) {
        String searchedName = context.getArgument("name", String.class);

        // Sort in order of closeness to the player
        Vec3 currentPosition = McUtils.player().position();

        List<MapLocation> places = Services.MapData.PLACE_LIST_PROVIDER
                .getFeatures()
                .map(f -> (MapLocation) f)
                .filter(loc -> StringUtils.partialMatch(
                        Services.MapData.resolveMapAttributes(loc).label(), searchedName))
                .sorted(Comparator.comparingDouble(loc -> currentPosition.distanceToSqr(
                        loc.getLocation().x(),
                        loc.getLocation().y(),
                        loc.getLocation().z())))
                .toList();

        if (places.isEmpty()) {
            MutableComponent response = Component.literal("Found no places matching '" + searchedName + "'")
                    .withStyle(ChatFormatting.RED);
            context.getSource().sendFailure(response);
            return 0;
        }

        MutableComponent response = Component.literal("Found places matching '" + searchedName + "':")
                .withStyle(ChatFormatting.AQUA);

        for (MapLocation place : places) {
            String placeName = Services.MapData.resolveMapAttributes(place).label();
            response.append(Component.literal("\n - ").withStyle(ChatFormatting.GRAY))
                    .append(Component.literal(placeName + " ")
                            .withStyle(ChatFormatting.YELLOW)
                            .withStyle((style) -> style.withClickEvent(
                                    new ClickEvent(ClickEvent.Action.RUN_COMMAND, "/compass place " + placeName))))
                    .append(Component.literal(place.getLocation().toString())
                            .withStyle(ChatFormatting.WHITE)
                            .withStyle((style) -> style.withClickEvent(
                                    new ClickEvent(ClickEvent.Action.RUN_COMMAND, "/compass place " + placeName))));
        }

        context.getSource().sendSuccess(() -> response, false);
        return 1;
    }

    private int notImplemented(CommandContext<CommandSourceStack> context) {
        context.getSource().sendFailure(Component.literal("Not implemented yet").withStyle(ChatFormatting.RED));
        return 0;
    }

    private int syntaxError(CommandContext<CommandSourceStack> context) {
        context.getSource().sendFailure(Component.literal("Missing argument").withStyle(ChatFormatting.RED));
        return 0;
    }
}
