/*
 * Copyright © Wynntils 2022.
 * This file is released under AGPLv3. See LICENSE for full license details.
 */

package com.wynntils.commands;

import com.mojang.brigadier.Command;
import com.mojang.brigadier.CommandDispatcher;
import com.wynntils.core.commands.WynntilsCommandBase;
import com.wynntils.core.webapi.WebManager;
import net.minecraft.ChatFormatting;
import net.minecraft.commands.CommandSourceStack;
import net.minecraft.commands.Commands;
import net.minecraft.network.chat.*;

public class TokenCommand extends WynntilsCommandBase {
    @Override
    public void register(CommandDispatcher<CommandSourceStack> dispatcher) {
        dispatcher.register(
                Commands.literal("token")
                        .executes(getToken()));
    }

    private Command<CommandSourceStack> getToken() {
        return context -> {
            if (WebManager.getAccount() != null && WebManager.getAccount().getToken() != null) {
                MutableComponent text = new TextComponent("").withStyle(ChatFormatting.AQUA);
                text.append("Wynntils Token ");

                MutableComponent token = new TextComponent(WebManager.getAccount().getToken())
                        .withStyle(Style.EMPTY.withHoverEvent(new HoverEvent(HoverEvent.Action.SHOW_TEXT,
                                new TextComponent("Click me to register an account.")))
                                .withClickEvent((new ClickEvent(ClickEvent.Action.OPEN_URL,
                                        "https://account.wynntils.com/register.php?token=" + WebManager.getAccount().getToken())))
                                .withColor(ChatFormatting.DARK_AQUA)
                                .withUnderlined(true));

                text.append(token);

                context.getSource().sendSuccess(text, false);
                return 1;
            }

            MutableComponent text = new TextComponent("Error when getting token, try restarting your client").withStyle(ChatFormatting.RED);

            context.getSource().sendFailure(text);
            return 1;
        };
    }
}
