/*
 * Copyright © Wynntils 2022.
 * This file is released under AGPLv3. See LICENSE for full license details.
 */
package com.wynntils.commands;

import com.mojang.brigadier.builder.LiteralArgumentBuilder;
import com.mojang.brigadier.context.CommandContext;
import com.wynntils.core.commands.CommandBase;
import com.wynntils.core.net.athena.WynntilsAccountManager;
import net.minecraft.ChatFormatting;
import net.minecraft.commands.CommandSourceStack;
import net.minecraft.commands.Commands;
import net.minecraft.network.chat.ClickEvent;
import net.minecraft.network.chat.Component;
import net.minecraft.network.chat.HoverEvent;
import net.minecraft.network.chat.MutableComponent;
import net.minecraft.network.chat.Style;

public class TokenCommand extends CommandBase {
    @Override
    public LiteralArgumentBuilder<CommandSourceStack> getBaseCommandBuilder() {
        return Commands.literal("token").executes(this::token);
    }

    private int token(CommandContext<CommandSourceStack> context) {
        if (!WynntilsAccountManager.isLoggedIn()) {
            MutableComponent failed = Component.literal(
                            "Either setting up your Wynntils account or accessing the token failed. To try to set up the Wynntils account again, run ")
                    .withStyle(ChatFormatting.GREEN);
            failed.append(Component.literal("/wynntils reload")
                    .withStyle(Style.EMPTY
                            .withColor(ChatFormatting.AQUA)
                            .withClickEvent(new ClickEvent(ClickEvent.Action.RUN_COMMAND, "/wynntils reload"))));
            context.getSource().sendFailure(failed);
            return 1;
        }

        String token = WynntilsAccountManager.getToken();

        MutableComponent text = Component.literal("Wynntils Token ").withStyle(ChatFormatting.AQUA);
        MutableComponent response = Component.literal(token)
                .withStyle(Style.EMPTY
                        .withHoverEvent(new HoverEvent(
                                HoverEvent.Action.SHOW_TEXT, Component.literal("Click me to register an account.")))
                        .withClickEvent((new ClickEvent(
                                ClickEvent.Action.OPEN_URL,
                                "https://account.wynntils.com/register.php?token=" + token)))
                        .withColor(ChatFormatting.DARK_AQUA)
                        .withUnderlined(true));
        text.append(response);

        context.getSource().sendSuccess(text, false);

        return 1;
    }
}
