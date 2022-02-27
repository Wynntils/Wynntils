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

public class WynntilsCommand extends WynntilsCommandBase {
    @Override
    public void register(CommandDispatcher<CommandSourceStack> dispatcher) {
        dispatcher.register(
                Commands.literal("wynntils")
                        .then(Commands.literal("help"))
                        .executes(getHelp())
                        .then(Commands.literal("discord").executes(getDiscordInvite()))
                        .then(Commands.literal("donate").executes(getDonateLink()))
                        .executes(getHelp()));
    }

    private Command<CommandSourceStack> getDonateLink() {
        return context -> {
            MutableComponent c =
                    new TextComponent("You can donate to Wynntils at: ")
                            .withStyle(ChatFormatting.AQUA);
            MutableComponent url =
                    new TextComponent("https://www.patreon.com/Wynntils")
                            .withStyle(
                                    Style.EMPTY
                                            .withColor(ChatFormatting.LIGHT_PURPLE)
                                            .withUnderlined(true)
                                            .withClickEvent(
                                                    new ClickEvent(
                                                            ClickEvent.Action.OPEN_URL,
                                                            "https://www.patreon.com/Wynntils"))
                                            .withHoverEvent(
                                                    new HoverEvent(
                                                            HoverEvent.Action.SHOW_TEXT,
                                                            new TextComponent(
                                                                    "Click here to open in your"
                                                                            + " browser."))));

            context.getSource().sendSuccess(c.append(url), false);
            return 1;
        };
    }

    private Command<CommandSourceStack> getHelp() {
        return context -> {
            MutableComponent text =
                    new TextComponent("").withStyle(Style.EMPTY.withColor(ChatFormatting.GOLD));
            text.append("Wynntils' command list: ");
            text.append("\n");
            addCommandDescription(
                    text,
                    "-wynntils",
                    " help",
                    "This shows a list of all available commands for Wynntils.");
            text.append("\n");
            addCommandDescription(
                    text,
                    "-wynntils",
                    " discord",
                    "This provides you with an invite to our Discord server.");
            text.append("\n");
            //            addCommandDescription(text, "-wynntils", " version", "This shows the
            // installed Wynntils version.");
            //            text.append("\n");
            //            addCommandDescription(text, "-wynntils", " changelog [major/latest]",
            // "This shows the changelog of your installed version.");
            //            text.append("\n");
            //            addCommandDescription(text, "-wynntils", " reloadapi", "This reloads all
            // API data.");
            //            text.append("\n");
            addCommandDescription(text, "-wynntils", " donate", "This provides our Patreon link.");
            context.getSource().sendSuccess(text, false);
            return 1;
        };
    }

    private Command<CommandSourceStack> getDiscordInvite() {
        return context -> {
            MutableComponent msg =
                    new TextComponent("You're welcome to join our Discord server at:\n")
                            .withStyle(ChatFormatting.GOLD);
            String discordInvite =
                    WebManager.getApiUrls() == null
                            ? null
                            : WebManager.getApiUrls().get("DiscordInvite");
            MutableComponent link =
                    new TextComponent(
                                    discordInvite == null
                                            ? "<Wynntils servers are down>"
                                            : discordInvite)
                            .withStyle(Style.EMPTY.withColor(ChatFormatting.DARK_AQUA));
            if (discordInvite != null) {
                link.setStyle(
                        link.getStyle()
                                .withClickEvent(
                                        new ClickEvent(ClickEvent.Action.OPEN_URL, discordInvite))
                                .withHoverEvent(
                                        new HoverEvent(
                                                HoverEvent.Action.SHOW_TEXT,
                                                new TextComponent(
                                                        "Click here to join our Discord"
                                                                + " server."))));
            }
            context.getSource().sendSuccess(msg.append(link), false);
            return 1;
        };
    }

    private static void addCommandDescription(
            MutableComponent text, String prefix, String name, String description) {
        MutableComponent prefixText =
                new TextComponent(prefix)
                        .withStyle(Style.EMPTY.withColor(ChatFormatting.DARK_GRAY));
        text.append(prefixText);

        MutableComponent nameText =
                new TextComponent(name).withStyle(Style.EMPTY.withColor(ChatFormatting.RED));
        text.append(nameText);

        text.append(" ");

        MutableComponent descriptionText =
                new TextComponent(description)
                        .withStyle(Style.EMPTY.withColor(ChatFormatting.GRAY));
        text.append(descriptionText);
    }
}
