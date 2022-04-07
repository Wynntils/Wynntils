/*
 * Copyright © Wynntils 2022.
 * This file is released under AGPLv3. See LICENSE for full license details.
 */
package com.wynntils.commands;

import com.mojang.brigadier.CommandDispatcher;
import com.mojang.brigadier.context.CommandContext;
import com.wynntils.core.webapi.WebManager;
import com.wynntils.mc.utils.commands.CommandBase;
import net.minecraft.ChatFormatting;
import net.minecraft.commands.CommandSourceStack;
import net.minecraft.commands.Commands;
import net.minecraft.network.chat.*;

public class TokenCommand extends CommandBase {
    @Override
    public void register(CommandDispatcher<CommandSourceStack> dispatcher) {
        dispatcher.register(Commands.literal("token").executes(this::token));
    }

    private int token(CommandContext<CommandSourceStack> context) {
        if (WebManager.getAccount() != null && WebManager.getAccount().getToken() != null) {
            MutableComponent text = new TextComponent("Wynntils Token ").withStyle(ChatFormatting.AQUA);

            MutableComponent token = new TextComponent(WebManager.getAccount().getToken())
                    .withStyle(Style.EMPTY
                            .withHoverEvent(new HoverEvent(
                                    HoverEvent.Action.SHOW_TEXT,
                                    new TextComponent("Click me to register an" + " account.")))
                            .withClickEvent((new ClickEvent(
                                    ClickEvent.Action.OPEN_URL,
                                    "https://account.wynntils.com/register.php?token="
                                            + WebManager.getAccount().getToken())))
                            .withColor(ChatFormatting.DARK_AQUA)
                            .withUnderlined(true));

            text.append(token);

            context.getSource().sendSuccess(text, false);
        } else {

            MutableComponent failed = new TextComponent(
                            "Either setting up your Wynntils account or accessing the token failed. To try to set up the Wynntils account again, run ")
                    .withStyle(ChatFormatting.GREEN);
            failed.append(new TextComponent("/wynntils reload")
                    .withStyle(Style.EMPTY
                            .withColor(ChatFormatting.AQUA)
                            .withClickEvent(new ClickEvent(ClickEvent.Action.RUN_COMMAND, "/wynntils reload"))));
            context.getSource().sendFailure(failed);
        }

        return 1;
    }
}
