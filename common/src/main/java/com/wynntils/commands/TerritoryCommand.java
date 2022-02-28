/*
 * Copyright © Wynntils 2022.
 * This file is released under AGPLv3. See LICENSE for full license details.
 */
package com.wynntils.commands;

import com.mojang.brigadier.Command;
import com.mojang.brigadier.CommandDispatcher;
import com.mojang.brigadier.arguments.StringArgumentType;
import com.wynntils.core.webapi.WebManager;
import com.wynntils.core.webapi.profiles.TerritoryProfile;
import com.wynntils.managers.CompassManager;
import com.wynntils.mc.utils.commands.WynntilsCommandBase;
import com.wynntils.utils.objects.Location;
import java.util.HashMap;
import net.minecraft.ChatFormatting;
import net.minecraft.commands.CommandSourceStack;
import net.minecraft.commands.Commands;
import net.minecraft.commands.SharedSuggestionProvider;
import net.minecraft.network.chat.MutableComponent;
import net.minecraft.network.chat.Style;
import net.minecraft.network.chat.TextComponent;

public class TerritoryCommand extends WynntilsCommandBase {
    @Override
    public void register(CommandDispatcher<CommandSourceStack> dispatcher) {
        HashMap<String, TerritoryProfile> territories = WebManager.getTerritories();

        dispatcher.register(
                Commands.literal("territory")
                        .then(
                                Commands.argument("territory", StringArgumentType.greedyString())
                                        .suggests(
                                                (context, builder) ->
                                                        SharedSuggestionProvider.suggest(
                                                                territories.keySet().stream(),
                                                                builder))
                                        .executes(getTerritory()))
                        .executes(getTerritoryHelp()));
    }

    private Command<CommandSourceStack> getTerritoryHelp() {
        return context -> {
            MutableComponent text =
                    new TextComponent("Usage: /territory [name] | Ex: /territory Detlas")
                            .withStyle(ChatFormatting.RED);
            context.getSource().sendSuccess(text, false);
            return 1;
        };
    }

    private Command<CommandSourceStack> getTerritory() {
        return context -> {
            String territoryArg = context.getArgument("territory", String.class);

            HashMap<String, TerritoryProfile> territories = WebManager.getTerritories();

            if (territories == null) {
                context.getSource()
                        .sendFailure(
                                new TextComponent("Can't access territory data")
                                        .withStyle(ChatFormatting.RED));
                return 1;
            }

            if (!territories.containsKey(territoryArg)) {
                context.getSource()
                        .sendFailure(
                                new TextComponent(
                                                "Invalid territory! Usage: /territory [name] | Ex:"
                                                        + " /territory Detlas")
                                        .withStyle(ChatFormatting.RED));
                return 1;
            }

            TerritoryProfile territoryProfile = territories.get(territoryArg);

            int xMiddle =
                    territoryProfile.getStartX()
                            + ((territoryProfile.getEndX() - territoryProfile.getStartX()) / 2);
            int zMiddle =
                    territoryProfile.getStartZ()
                            + ((territoryProfile.getEndZ() - territoryProfile.getStartZ()) / 2);

            CompassManager.setCompassLocation(new Location(xMiddle, 0, zMiddle)); // update

            MutableComponent territoryComponent =
                    new TextComponent(territoryProfile.getFriendlyName())
                            .withStyle(
                                    Style.EMPTY
                                            .withColor(ChatFormatting.DARK_GREEN)
                                            .withUnderlined(true));
            MutableComponent success =
                    new TextComponent("The compass is now pointing towards ")
                            .withStyle(ChatFormatting.GREEN)
                            .append(territoryComponent)
                            .append(
                                    new TextComponent(" (" + xMiddle + ", " + zMiddle + ")")
                                            .withStyle(ChatFormatting.GREEN));

            MutableComponent warn =
                    new TextComponent(
                                    "\n"
                                        + "Please be sure you know that this command redirects your"
                                        + " compass to the middle of the territory.")
                            .withStyle(ChatFormatting.AQUA);
            success.append(warn);

            MutableComponent separator =
                    new TextComponent("-----------------------------------------------------")
                            .withStyle(
                                    Style.EMPTY
                                            .withColor(ChatFormatting.DARK_GRAY)
                                            .withStrikethrough(true));

            MutableComponent finalMessage = new TextComponent("");

            finalMessage
                    .append(separator)
                    .append("\n")
                    .append(success)
                    .append("\n")
                    .append(separator);

            context.getSource().sendSuccess(finalMessage, false);

            return 1;
        };
    }
}
