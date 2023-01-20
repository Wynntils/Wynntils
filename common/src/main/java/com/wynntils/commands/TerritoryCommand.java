/*
 * Copyright © Wynntils 2022.
 * This file is released under AGPLv3. See LICENSE for full license details.
 */
package com.wynntils.commands;

import com.mojang.brigadier.arguments.StringArgumentType;
import com.mojang.brigadier.builder.LiteralArgumentBuilder;
import com.mojang.brigadier.context.CommandContext;
import com.wynntils.core.commands.CommandBase;
import com.wynntils.core.components.Managers;
import com.wynntils.core.components.ModelRegistry;
import com.wynntils.core.components.Models;
import com.wynntils.mc.objects.Location;
import com.wynntils.models.territories.profile.TerritoryProfile;
import net.minecraft.ChatFormatting;
import net.minecraft.commands.CommandSourceStack;
import net.minecraft.commands.Commands;
import net.minecraft.commands.SharedSuggestionProvider;
import net.minecraft.network.chat.Component;
import net.minecraft.network.chat.MutableComponent;
import net.minecraft.network.chat.Style;

public class TerritoryCommand extends CommandBase {
    @Override
    public LiteralArgumentBuilder<CommandSourceStack> getBaseCommandBuilder() {
        return Commands.literal("territory")
                .then(Commands.argument("territory", StringArgumentType.greedyString())
                        .suggests((context, builder) ->
                                SharedSuggestionProvider.suggest(Managers.Territory.getTerritoryNames(), builder))
                        .executes(this::territory))
                .executes(this::help);
    }

    private int help(CommandContext<CommandSourceStack> context) {
        context.getSource().sendSuccess(helpComponent(), false);
        return 1;
    }

    private MutableComponent helpComponent() {
        return Component.literal("Usage: /territory [name] | Ex: /territory Detlas")
                .withStyle(ChatFormatting.RED);
    }

    private int territory(CommandContext<CommandSourceStack> context) {
        String territoryArg = context.getArgument("territory", String.class);

        TerritoryProfile territoryProfile = Managers.Territory.getTerritoryProfile(territoryArg);

        if (territoryProfile == null) {
            context.getSource()
                    .sendFailure(Component.literal(
                                    "Can't access territory " + "\"" + territoryArg + "\". There likely is a typo.")
                            .withStyle(ChatFormatting.RED));
            return 1;
        }

        int xMiddle = (territoryProfile.getStartX() + territoryProfile.getEndX()) / 2;
        int zMiddle = (territoryProfile.getStartZ() + territoryProfile.getEndZ()) / 2;

        MutableComponent territoryComponent = Component.literal(territoryProfile.getFriendlyName())
                .withStyle(Style.EMPTY.withColor(ChatFormatting.DARK_GREEN).withUnderlined(true));

        if (!ModelRegistry.isEnabled(Models.Compass)) {
            MutableComponent success = territoryComponent
                    .append(": ")
                    .append(Component.literal(" (" + xMiddle + ", " + zMiddle + ")")
                            .withStyle(ChatFormatting.GREEN));
            context.getSource().sendSuccess(success, false);
            return 1;
        }

        Models.Compass.setCompassLocation(new Location(xMiddle, 0, zMiddle)); // update

        MutableComponent separator = Component.literal("-----------------------------------------------------")
                .withStyle(Style.EMPTY.withColor(ChatFormatting.DARK_GRAY).withStrikethrough(true));

        MutableComponent finalMessage = Component.literal("");

        finalMessage.append(separator);

        MutableComponent success = Component.literal("The compass is now pointing towards ")
                .withStyle(ChatFormatting.GREEN)
                .append(territoryComponent)
                .append(Component.literal(" (" + xMiddle + ", " + zMiddle + ")").withStyle(ChatFormatting.GREEN));

        finalMessage.append("\n").append(success);

        MutableComponent warn = Component.literal(
                        "Note that this command redirects your" + " compass to the middle of said territory.")
                .withStyle(ChatFormatting.AQUA);

        finalMessage.append("\n").append(warn);

        finalMessage.append("\n").append(separator);

        context.getSource().sendSuccess(finalMessage, false);

        return 1;
    }
}
