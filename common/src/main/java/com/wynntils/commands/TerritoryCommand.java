/*
 * Copyright © Wynntils 2022.
 * This file is released under AGPLv3. See LICENSE for full license details.
 */
package com.wynntils.commands;

import com.mojang.brigadier.arguments.StringArgumentType;
import com.mojang.brigadier.builder.LiteralArgumentBuilder;
import com.mojang.brigadier.context.CommandContext;
import com.wynntils.core.commands.CommandBase;
import com.wynntils.core.managers.ManagerRegistry;
import com.wynntils.mc.objects.Location;
import com.wynntils.wynn.model.CompassModel;
import com.wynntils.wynn.model.territory.TerritoryManager;
import com.wynntils.wynn.objects.profiles.TerritoryProfile;
import net.minecraft.ChatFormatting;
import net.minecraft.commands.CommandSourceStack;
import net.minecraft.commands.Commands;
import net.minecraft.commands.SharedSuggestionProvider;
import net.minecraft.network.chat.MutableComponent;
import net.minecraft.network.chat.Style;
import net.minecraft.network.chat.TextComponent;

public class TerritoryCommand extends CommandBase {
    @Override
    public LiteralArgumentBuilder<CommandSourceStack> getBaseCommandBuilder() {
        return Commands.literal("territory")
                .then(Commands.argument("territory", StringArgumentType.greedyString())
                        .suggests((context, builder) ->
                                SharedSuggestionProvider.suggest(TerritoryManager.getTerritoryNames(), builder))
                        .executes(this::territory))
                .executes(this::help);
    }

    private int help(CommandContext<CommandSourceStack> context) {
        context.getSource().sendSuccess(helpComponent(), false);
        return 1;
    }

    private MutableComponent helpComponent() {
        return new TextComponent("Usage: /territory [name] | Ex: /territory Detlas").withStyle(ChatFormatting.RED);
    }

    private int territory(CommandContext<CommandSourceStack> context) {
        String territoryArg = context.getArgument("territory", String.class);

        TerritoryProfile territoryProfile = TerritoryManager.getTerritoryProfile(territoryArg);

        if (territoryProfile == null) {
            context.getSource()
                    .sendFailure(new TextComponent(
                                    "Can't access territory " + "\"" + territoryArg + "\". There likely is a typo.")
                            .withStyle(ChatFormatting.RED));
            return 1;
        }

        int xMiddle = (territoryProfile.getStartX() + territoryProfile.getEndX()) / 2;
        int zMiddle = (territoryProfile.getStartZ() + territoryProfile.getEndZ()) / 2;

        MutableComponent territoryComponent = new TextComponent(territoryProfile.getFriendlyName())
                .withStyle(Style.EMPTY.withColor(ChatFormatting.DARK_GREEN).withUnderlined(true));

        if (!ManagerRegistry.isEnabled(CompassModel.class)) {
            MutableComponent success = territoryComponent
                    .append(": ")
                    .append(new TextComponent(" (" + xMiddle + ", " + zMiddle + ")").withStyle(ChatFormatting.GREEN));
            context.getSource().sendSuccess(success, false);
            return 1;
        }

        CompassModel.setCompassLocation(new Location(xMiddle, 0, zMiddle)); // update

        MutableComponent separator = new TextComponent("-----------------------------------------------------")
                .withStyle(Style.EMPTY.withColor(ChatFormatting.DARK_GRAY).withStrikethrough(true));

        MutableComponent finalMessage = new TextComponent("");

        finalMessage.append(separator);

        MutableComponent success = new TextComponent("The compass is now pointing towards ")
                .withStyle(ChatFormatting.GREEN)
                .append(territoryComponent)
                .append(new TextComponent(" (" + xMiddle + ", " + zMiddle + ")").withStyle(ChatFormatting.GREEN));

        finalMessage.append("\n").append(success);

        MutableComponent warn = new TextComponent(
                        "Note that this command redirects your" + " compass to the middle of said territory.")
                .withStyle(ChatFormatting.AQUA);

        finalMessage.append("\n").append(warn);

        finalMessage.append("\n").append(separator);

        context.getSource().sendSuccess(finalMessage, false);

        return 1;
    }
}
