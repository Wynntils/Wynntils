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
import com.wynntils.utils.StringUtils;
import com.wynntils.wynn.model.map.MapModel;
import com.wynntils.wynn.model.map.poi.LabelPoi;
import com.wynntils.wynn.model.map.poi.Poi;
import com.wynntils.wynn.model.map.poi.ServiceKind;
import com.wynntils.wynn.model.map.poi.ServicePoi;
import java.util.Arrays;
import java.util.List;
import net.minecraft.ChatFormatting;
import net.minecraft.commands.CommandSourceStack;
import net.minecraft.commands.Commands;
import net.minecraft.commands.SharedSuggestionProvider;
import net.minecraft.network.chat.MutableComponent;
import net.minecraft.network.chat.TextComponent;

public class LocateCommand extends CommandBase {
    private final SuggestionProvider<CommandSourceStack> serviceSuggestionProvider = (context, builder) ->
            SharedSuggestionProvider.suggest(Arrays.stream(ServiceKind.values()).map(ServiceKind::getName), builder);

    private final SuggestionProvider<CommandSourceStack> placesSuggestionProvider =
            (context, builder) -> SharedSuggestionProvider.suggest(
                    MapModel.getAllPois().stream()
                            .filter(poi -> poi instanceof LabelPoi)
                            .map(Poi::getName),
                    builder);

    @Override
    public LiteralArgumentBuilder<CommandSourceStack> getBaseCommandBuilder() {
        return Commands.literal("locate")
                .then(Commands.literal("service")
                        .then(Commands.argument("name", StringArgumentType.greedyString())
                                .suggests(serviceSuggestionProvider)
                                .executes(this::locateService))
                        .build())
                .then(Commands.literal("place")
                        .then(Commands.argument("name", StringArgumentType.greedyString())
                                .suggests(placesSuggestionProvider)
                                .executes(this::locatePlace))
                        .build())
                .then(Commands.literal("npc")
                        .then(Commands.argument("name", StringArgumentType.greedyString())
                                .executes(this::notImplemented))
                        .build())
                .then(Commands.literal("other")
                        .then(Commands.argument("name", StringArgumentType.greedyString())
                                .suggests(serviceSuggestionProvider)
                                .executes(this::notImplemented))
                        .build())
                .executes(this::syntaxError);
    }

    private int locateService(CommandContext<CommandSourceStack> context) {
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
                    ", ", matchedKinds.stream().map(kind -> kind.getName()).toList())));
            context.getSource().sendFailure(response);
            return 0;
        }

        ServiceKind selectedKind = matchedKinds.get(0);

        List<Poi> services = MapModel.getAllPois().stream()
                .filter(poi -> poi instanceof ServicePoi servicePoi
                        && servicePoi.getKind().equals(selectedKind))
                .toList();

        MutableComponent response =
                new TextComponent("Found " + selectedKind.getName() + " services:").withStyle(ChatFormatting.AQUA);

        for (Poi service : services) {
            response.append(new TextComponent("\n - ").withStyle(ChatFormatting.GRAY))
                    .append(new TextComponent(service.getName() + " ").withStyle(ChatFormatting.YELLOW))
                    .append(new TextComponent(service.getLocation().toString()).withStyle(ChatFormatting.WHITE));
        }

        context.getSource().sendSuccess(response, false);
        return 1;
    }

    private int locatePlace(CommandContext<CommandSourceStack> context) {
        String searchedName = context.getArgument("name", String.class);

        List<Poi> places = MapModel.getAllPois().stream()
                .filter(poi -> poi instanceof LabelPoi && StringUtils.partialMatch(poi.getName(), searchedName))
                .toList();

        if (places.isEmpty()) {
            MutableComponent response =
                    new TextComponent("Found no places matching '" + searchedName + "'").withStyle(ChatFormatting.RED);
            context.getSource().sendFailure(response);
            return 0;
        }

        MutableComponent response =
                new TextComponent("Found places matching '" + searchedName + "':").withStyle(ChatFormatting.AQUA);

        for (Poi place : places) {
            response.append(new TextComponent("\n - ").withStyle(ChatFormatting.GRAY))
                    .append(new TextComponent(place.getName() + " ").withStyle(ChatFormatting.YELLOW))
                    .append(new TextComponent(place.getLocation().toString()).withStyle(ChatFormatting.WHITE));
        }

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
