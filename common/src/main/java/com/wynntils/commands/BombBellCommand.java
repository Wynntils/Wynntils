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
import com.wynntils.wynn.model.BombBellModel;
import com.wynntils.wynn.objects.BombInfo;
import com.wynntils.wynn.objects.BombType;
import java.util.Arrays;
import net.minecraft.ChatFormatting;
import net.minecraft.commands.CommandSourceStack;
import net.minecraft.commands.Commands;
import net.minecraft.commands.SharedSuggestionProvider;
import net.minecraft.network.chat.MutableComponent;
import net.minecraft.network.chat.TextComponent;

public class BombBellCommand extends CommandBase {
    private final SuggestionProvider<CommandSourceStack> bombTypeSuggestionProvider = (context, builder) ->
            SharedSuggestionProvider.suggest(Arrays.stream(BombType.values()).map(Enum::name), builder);

    @Override
    public LiteralArgumentBuilder<CommandSourceStack> getBaseCommandBuilder() {
        return Commands.literal("bombbell")
                .then(Commands.literal("list").executes(this::listBombs))
                .then(Commands.literal("get")
                        .then(Commands.argument("bombType", StringArgumentType.word())
                                .suggests(bombTypeSuggestionProvider)
                                .executes(this::getBombTypeList)));
    }

    private int getBombTypeList(CommandContext<CommandSourceStack> context) {

        return 1;
    }

    private int listBombs(CommandContext<CommandSourceStack> context) {
        MutableComponent response = new TextComponent("Bomb Bells: ").withStyle(ChatFormatting.GOLD);

        for (BombInfo bomb : BombBellModel.getBombBells()) {
            response.append(new TextComponent("\n" + bomb.bomb().getName())
                            .withStyle(ChatFormatting.WHITE)
                            .append(" on ")
                            .withStyle(ChatFormatting.GRAY)
                            .append(new TextComponent(bomb.server()).withStyle(ChatFormatting.WHITE)))
                    .append(new TextComponent(" for: ")
                            .withStyle(ChatFormatting.GRAY)
                            .append(new TextComponent(bomb.getRemainingString()).withStyle(ChatFormatting.WHITE)));
        }

        context.getSource().sendSuccess(response, false);

        return 1;
    }
}
