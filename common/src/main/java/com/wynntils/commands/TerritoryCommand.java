/*
 * Copyright © Wynntils 2022.
 * This file is released under AGPLv3. See LICENSE for full license details.
 */
package com.wynntils.commands;

import com.mojang.brigadier.CommandDispatcher;
import com.mojang.brigadier.arguments.StringArgumentType;
import com.mojang.brigadier.context.CommandContext;
import com.wynntils.core.webapi.WebManager;
import com.wynntils.core.webapi.profiles.TerritoryProfile;
import com.wynntils.managers.CompassManager;
import com.wynntils.mc.utils.commands.CommandBase;
import com.wynntils.utils.objects.Location;
import java.util.HashMap;
import net.minecraft.ChatFormatting;
import net.minecraft.commands.CommandSourceStack;
import net.minecraft.commands.Commands;
import net.minecraft.commands.SharedSuggestionProvider;
import net.minecraft.network.chat.MutableComponent;
import net.minecraft.network.chat.Style;
import net.minecraft.network.chat.TextComponent;

public class TerritoryCommand extends CommandBase {
    @Override
    public void register(CommandDispatcher<CommandSourceStack> dispatcher) {
        dispatcher.register(
                Commands.literal("territory")
                        .then(
                                Commands.argument("territory", StringArgumentType.greedyString())
                                        .suggests(
                                                (context, builder) -> {
                                                    if (!WebManager.isTerritoryListLoaded()
                                                            && !WebManager.tryLoadTerritories()) {
                                                        return null;
                                                    }

                                                    HashMap<String, TerritoryProfile> territories =
                                                            WebManager.getTerritories();

                                                    return SharedSuggestionProvider.suggest(
                                                            territories.keySet().stream(), builder);
                                                })
                                        .executes(this::territory))
                        .executes(this::help));
    }

    private int help(CommandContext<CommandSourceStack> context) {
        context.getSource().sendSuccess(helpComponent(), false);
        return 1;
    }

    private MutableComponent helpComponent() {
        return new TextComponent("Usage: /territory [name] | Ex: /territory Detlas")
                .withStyle(ChatFormatting.RED);
    }

    private int territory(CommandContext<CommandSourceStack> context) {
        if (!WebManager.isTerritoryListLoaded() && !WebManager.tryLoadTerritories()) {
            context.getSource()
                    .sendFailure(
                            new TextComponent("Can't access territory data")
                                    .withStyle(ChatFormatting.RED));
            return 1;
        }

        String territoryArg = context.getArgument("territory", String.class);

        HashMap<String, TerritoryProfile> territories = WebManager.getTerritories();

        if (!territories.containsKey(territoryArg)) {
            context.getSource()
                    .sendFailure(
                            new TextComponent(
                                            "Can't access territory " + "\"" + territoryArg + "\"")
                                    .withStyle(ChatFormatting.RED));
            return 1;
        }

        TerritoryProfile territoryProfile = territories.get(territoryArg);

        int xMiddle = (territoryProfile.getStartX() + territoryProfile.getEndX()) / 2;
        int zMiddle = (territoryProfile.getStartZ() + territoryProfile.getEndZ()) / 2;

        CompassManager.setCompassLocation(new Location(xMiddle, 0, zMiddle)); // update

        MutableComponent separator =
                new TextComponent("-----------------------------------------------------")
                        .withStyle(
                                Style.EMPTY
                                        .withColor(ChatFormatting.DARK_GRAY)
                                        .withStrikethrough(true));

        MutableComponent finalMessage = new TextComponent("");

        finalMessage.append(separator);

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

        finalMessage.append("\n").append(success);

        MutableComponent warn =
                new TextComponent(
                                "Note that this command redirects your"
                                        + " compass to the middle of said territory.")
                        .withStyle(ChatFormatting.AQUA);

        finalMessage.append("\n").append(warn);

        finalMessage.append("\n").append(separator);

        context.getSource().sendSuccess(finalMessage, false);

        return 1;
    }
}
