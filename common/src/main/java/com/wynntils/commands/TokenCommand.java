/*
 * Copyright © Wynntils 2022.
 * This file is released under AGPLv3. See LICENSE for full license details.
 */
package com.wynntils.commands;

import com.mojang.brigadier.builder.LiteralArgumentBuilder;
import com.mojang.brigadier.context.CommandContext;
import com.wynntils.core.commands.CommandBase;
import com.wynntils.core.net.UrlId;
import com.wynntils.core.net.UrlManager;
import com.wynntils.core.net.athena.WynntilsAccountManager;
import java.util.Map;
import net.minecraft.ChatFormatting;
import net.minecraft.commands.CommandSourceStack;
import net.minecraft.commands.Commands;
import net.minecraft.network.chat.ClickEvent;
import net.minecraft.network.chat.HoverEvent;
import net.minecraft.network.chat.MutableComponent;
import net.minecraft.network.chat.Style;
import net.minecraft.network.chat.TextComponent;

public class TokenCommand extends CommandBase {
    @Override
    public LiteralArgumentBuilder<CommandSourceStack> getBaseCommandBuilder() {
        return Commands.literal("token").executes(this::token);
    }

    private int token(CommandContext<CommandSourceStack> context) {
        if (!WynntilsAccountManager.isLoggedIn()) {
            MutableComponent failed = new TextComponent(
                            "Either setting up your Wynntils account or accessing the token failed. To try to set up the Wynntils account again, run ")
                    .withStyle(ChatFormatting.GREEN);
            failed.append(new TextComponent("/wynntils reload")
                    .withStyle(Style.EMPTY
                            .withColor(ChatFormatting.AQUA)
                            .withClickEvent(new ClickEvent(ClickEvent.Action.RUN_COMMAND, "/wynntils reload"))));
            context.getSource().sendFailure(failed);
            return 1;
        }

        String token = WynntilsAccountManager.getToken();

        MutableComponent text = new TextComponent("Wynntils Token ").withStyle(ChatFormatting.AQUA);
        MutableComponent response = new TextComponent(token)
                .withStyle(Style.EMPTY
                        .withHoverEvent(new HoverEvent(
                                HoverEvent.Action.SHOW_TEXT, new TextComponent("Click me to register an account.")))
                        .withClickEvent((new ClickEvent(
                                ClickEvent.Action.OPEN_URL,
                                UrlManager.buildUrl(UrlId.LINK_WYNNTILS_REGISTER_ACCOUNT, Map.of("token", token)))))
                        .withColor(ChatFormatting.DARK_AQUA)
                        .withUnderlined(true));
        text.append(response);

        context.getSource().sendSuccess(text, false);

        return 1;
    }
}
