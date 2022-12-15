/*
 * Copyright © Wynntils 2022.
 * This file is released under AGPLv3. See LICENSE for full license details.
 */
package com.wynntils.commands;

import com.mojang.brigadier.arguments.StringArgumentType;
import com.mojang.brigadier.builder.LiteralArgumentBuilder;
import com.mojang.brigadier.context.CommandContext;
import com.mojang.brigadier.suggestion.SuggestionProvider;
import com.wynntils.core.commands.CommandBase;
import com.wynntils.core.managers.Models;
import com.wynntils.mc.objects.Location;
import com.wynntils.mc.utils.LocationUtils;
import com.wynntils.mc.utils.McUtils;
import com.wynntils.utils.StringUtils;
import com.wynntils.wynn.model.map.poi.Poi;
import com.wynntils.wynn.model.map.poi.ServiceKind;
import com.wynntils.wynn.model.map.poi.ServicePoi;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.Comparator;
import java.util.List;
import java.util.Optional;
import java.util.stream.Collectors;
import net.minecraft.ChatFormatting;
import net.minecraft.commands.CommandSourceStack;
import net.minecraft.commands.Commands;
import net.minecraft.commands.SharedSuggestionProvider;
import net.minecraft.commands.arguments.coordinates.Coordinates;
import net.minecraft.commands.arguments.coordinates.Vec3Argument;
import net.minecraft.network.chat.MutableComponent;
import net.minecraft.network.chat.TextComponent;
import net.minecraft.world.entity.player.Player;
import net.minecraft.world.phys.Vec3;

public class CompassCommand extends CommandBase {

    private final SuggestionProvider<CommandSourceStack> shareTargetSuggestionProvider =
            (context, builder) -> SharedSuggestionProvider.suggest(
                    () -> {
                        List<String> suggestions = new ArrayList<>();
                        suggestions.add("party");
                        suggestions.add("guild");

                        suggestions.addAll(McUtils.mc().level.players().stream()
                                .map(Player::getScoreboardName)
                                .collect(Collectors.toSet()));

                        return suggestions.iterator();
                    },
                    builder);

    @Override
    public LiteralArgumentBuilder<CommandSourceStack> getBaseCommandBuilder() {
        return Commands.literal("compass")
                .then(Commands.literal("at")
                        .then(Commands.argument("location", Vec3Argument.vec3()).executes(this::compassAtVec3))
                        .build())
                .then(Commands.literal("share")
                        .then(Commands.literal("location")
                                .then(Commands.argument("target", StringArgumentType.word())
                                        .suggests(shareTargetSuggestionProvider)
                                        .executes(this::shareLocation)))
                        .then(Commands.argument("target", StringArgumentType.word())
                                .suggests(shareTargetSuggestionProvider)
                                .executes(this::shareCompass)))
                .then(Commands.literal("service")
                        .then(Commands.argument("name", StringArgumentType.greedyString())
                                .suggests(LocateCommand.SERVICE_SUGGESTION_PROVIDER)
                                .executes(this::compassService))
                        .build())
                .then(Commands.literal("place")
                        .then(Commands.argument("name", StringArgumentType.greedyString())
                                .suggests(LocateCommand.PLACES_SUGGESTION_PROVIDER)
                                .executes(this::compassPlace))
                        .build())
                .then(Commands.literal("npc")
                        .then(Commands.argument("name", StringArgumentType.greedyString())
                                .executes(this::notImplemented))
                        .build())
                .then(Commands.literal("other")
                        .then(Commands.argument("name", StringArgumentType.greedyString())
                                .executes(this::notImplemented))
                        .build())
                .then(Commands.literal("clear").executes(this::compassClear).build())
                .then(Commands.argument("location", StringArgumentType.greedyString())
                        .executes(this::compassAtString))
                .executes(this::syntaxError);
    }

    private int shareCompass(CommandContext<CommandSourceStack> context) {
        Optional<Location> compassLocation = Models.Compass.getCompassLocation();

        if (compassLocation.isEmpty()) {
            context.getSource()
                    .sendFailure(new TextComponent("You don't have a compass set!").withStyle(ChatFormatting.RED));
            return 1;
        }

        String target = StringArgumentType.getString(context, "target");

        LocationUtils.shareCompass(target, compassLocation.get());

        return 1;
    }

    private int shareLocation(CommandContext<CommandSourceStack> context) {
        String target = StringArgumentType.getString(context, "target");

        LocationUtils.shareLocation(target);

        return 1;
    }

    private int compassAtVec3(CommandContext<CommandSourceStack> context) {
        Coordinates coordinates = Vec3Argument.getCoordinates(context, "location");
        Location location = new Location(coordinates.getBlockPos(context.getSource()));
        Models.Compass.setCompassLocation(location);

        MutableComponent response = new TextComponent("Compass set to ").withStyle(ChatFormatting.AQUA);
        response.append(new TextComponent(location.toString()).withStyle(ChatFormatting.WHITE));
        context.getSource().sendSuccess(response, false);
        return 1;
    }

    private int compassAtString(CommandContext<CommandSourceStack> context) {
        String coordinates = StringArgumentType.getString(context, "location");
        Optional<Location> location = LocationUtils.parseFromString(coordinates);
        if (location.isEmpty()) {
            context.getSource().sendFailure(new TextComponent("Incorrect coordinates!"));
            return 0;
        }

        Models.Compass.setCompassLocation(location.get());

        MutableComponent response = new TextComponent("Compass set to ").withStyle(ChatFormatting.AQUA);
        response.append(new TextComponent(location.get().toString()).withStyle(ChatFormatting.WHITE));
        context.getSource().sendSuccess(response, false);
        return 1;
    }

    private int compassService(CommandContext<CommandSourceStack> context) {
        String searchedName = context.getArgument("name", String.class);

        List<ServiceKind> matchedKinds = Arrays.stream(ServiceKind.values())
                .filter(kind -> StringUtils.partialMatch(kind.getName(), searchedName))
                .toList();

        if (matchedKinds.isEmpty()) {
            MutableComponent response = new TextComponent("Found no services matching '" + searchedName + "'")
                    .withStyle(ChatFormatting.RED);
            context.getSource().sendFailure(response);
            return 0;
        }

        if (matchedKinds.size() > 1) {
            MutableComponent response = new TextComponent("Found multiple services matching '" + searchedName
                            + "'. Pleace specify with more detail. Matching: ")
                    .withStyle(ChatFormatting.RED);
            response.append(new TextComponent(String.join(
                    ", ", matchedKinds.stream().map(ServiceKind::getName).toList())));
            context.getSource().sendFailure(response);
            return 0;
        }

        ServiceKind selectedKind = matchedKinds.get(0);

        Vec3 currentLocation = McUtils.player().position();
        Optional<ServicePoi> closestServiceOptional = Models.Map.getServicePois().stream()
                .filter(poi -> poi.getKind().equals(selectedKind))
                .min(Comparator.comparingDouble(poi -> currentLocation.distanceToSqr(
                        poi.getLocation().getX(),
                        poi.getLocation().getY().orElse((int) currentLocation.y),
                        poi.getLocation().getZ())));
        if (closestServiceOptional.isEmpty()) {
            // This really should not happen...
            MutableComponent response = new TextComponent("Found no services of kind '" + selectedKind.getName() + "'")
                    .withStyle(ChatFormatting.RED);
            context.getSource().sendFailure(response);
            return 0;
        }
        Poi closestService = closestServiceOptional.get();
        Models.Compass.setCompassLocation(
                closestService.getLocation().asLocation(),
                closestServiceOptional.get().getIcon());

        MutableComponent response =
                new TextComponent("Compass set to " + selectedKind.getName() + " at ").withStyle(ChatFormatting.AQUA);
        response.append(new TextComponent(closestService.getLocation().toString()).withStyle(ChatFormatting.WHITE));
        context.getSource().sendSuccess(response, false);
        return 1;
    }

    private int compassPlace(CommandContext<CommandSourceStack> context) {
        String searchedName = context.getArgument("name", String.class);

        List<Poi> places = new ArrayList<>(Models.Map.getLabelPois().stream()
                .filter(poi -> StringUtils.partialMatch(poi.getName(), searchedName))
                .toList());

        if (places.isEmpty()) {
            MutableComponent response =
                    new TextComponent("Found no places matching '" + searchedName + "'").withStyle(ChatFormatting.RED);
            context.getSource().sendFailure(response);
            return 0;
        }

        Poi place;

        if (places.size() > 1) {
            // Try to find one with an exact match, to differentiate e.g. "Detlas" from "Detlas Suburbs"
            Optional<Poi> exactMatch = places.stream()
                    .filter(poi -> poi.getName().equals(searchedName))
                    .findFirst();
            if (exactMatch.isEmpty()) {
                MutableComponent response = new TextComponent("Found multiple places matching '" + searchedName
                                + "', but none matched exactly. Matching: ")
                        .withStyle(ChatFormatting.RED);
                response.append(new TextComponent(
                        String.join(", ", places.stream().map(Poi::getName).toList())));
                context.getSource().sendFailure(response);
                return 0;
            }
            place = exactMatch.get();
        } else {
            place = places.get(0);
        }

        Models.Compass.setCompassLocation(place.getLocation().asLocation());

        MutableComponent response =
                new TextComponent("Setting compass to " + place.getName() + " at ").withStyle(ChatFormatting.AQUA);
        response.append(new TextComponent(place.getLocation().toString()).withStyle(ChatFormatting.WHITE));
        context.getSource().sendSuccess(response, false);
        return 1;
    }

    private int compassClear(CommandContext<CommandSourceStack> context) {
        Models.Compass.reset();

        MutableComponent response = new TextComponent("Compass cleared").withStyle(ChatFormatting.AQUA);
        context.getSource().sendSuccess(response, false);
        return 1;
    }

    private int notImplemented(CommandContext<CommandSourceStack> context) {
        context.getSource().sendFailure(new TextComponent("Not implemented yet").withStyle(ChatFormatting.RED));
        return 0;
    }

    private int syntaxError(CommandContext<CommandSourceStack> context) {
        context.getSource().sendFailure(new TextComponent("Missing argument").withStyle(ChatFormatting.RED));
        return 0;
    }
}
