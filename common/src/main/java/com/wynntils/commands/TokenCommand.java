/*
 * Copyright © Wynntils 2022.
 * This file is released under AGPLv3. See LICENSE for full license details.
 */
package com.wynntils.commands;

import com.mojang.brigadier.builder.LiteralArgumentBuilder;
import com.mojang.brigadier.context.CommandContext;
import com.wynntils.core.commands.Command;
import com.wynntils.core.components.Managers;
import com.wynntils.core.net.UrlId;
import java.util.Map;
import net.minecraft.ChatFormatting;
import net.minecraft.commands.CommandSourceStack;
import net.minecraft.commands.Commands;
import net.minecraft.network.chat.ClickEvent;
import net.minecraft.network.chat.Component;
import net.minecraft.network.chat.HoverEvent;
import net.minecraft.network.chat.MutableComponent;
import net.minecraft.network.chat.Style;

public class TokenCommand extends Command {
    @Override
    public LiteralArgumentBuilder<CommandSourceStack> getBaseCommandBuilder() {
        return Commands.literal("token").executes(this::token);
    }

    private int token(CommandContext<CommandSourceStack> context) {
        if (!Managers.WynntilsAccount.isLoggedIn()) {
            MutableComponent failed = Component.literal(
                            "Either setting up your Wynntils account or accessing the token failed. To try to set up the Wynntils account again, run ")
                    .withStyle(ChatFormatting.GREEN);
            failed.append(Component.literal("/wynntils reauth")
                    .withStyle(Style.EMPTY
                            .withColor(ChatFormatting.AQUA)
                            .withClickEvent(new ClickEvent(ClickEvent.Action.RUN_COMMAND, "/wynntils reauth"))));
            context.getSource().sendFailure(failed);
            return 1;
        }

        String token = Managers.WynntilsAccount.getToken();

        MutableComponent text = Component.literal("Wynntils Token ").withStyle(ChatFormatting.AQUA);
        MutableComponent response = Component.literal(token)
                .withStyle(Style.EMPTY
                        .withHoverEvent(new HoverEvent(
                                HoverEvent.Action.SHOW_TEXT, Component.literal("Click me to register an account.")))
                        .withClickEvent((new ClickEvent(
                                ClickEvent.Action.OPEN_URL,
                                Managers.Url.buildUrl(UrlId.LINK_WYNNTILS_REGISTER_ACCOUNT, Map.of("token", token)))))
                        .withColor(ChatFormatting.DARK_AQUA)
                        .withUnderlined(true));
        text.append(response);

        context.getSource().sendSuccess(text, false);

        return 1;
    }
}
