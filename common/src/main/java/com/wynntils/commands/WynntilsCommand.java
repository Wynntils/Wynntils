/*
 * Copyright © Wynntils 2022.
 * This file is released under AGPLv3. See LICENSE for full license details.
 */
package com.wynntils.commands;

import com.mojang.brigadier.CommandDispatcher;
import com.mojang.brigadier.context.CommandContext;
import com.wynntils.core.webapi.WebManager;
import com.wynntils.mc.utils.commands.WynntilsCommandBase;
import java.util.List;
import net.minecraft.ChatFormatting;
import net.minecraft.commands.CommandSourceStack;
import net.minecraft.commands.Commands;
import net.minecraft.network.chat.*;

public class WynntilsCommand extends WynntilsCommandBase {
    @Override
    public void register(CommandDispatcher<CommandSourceStack> dispatcher) {
        dispatcher.register(
                Commands.literal("wynntils")
                        .then(Commands.literal("help").executes(this::help))
                        .then(Commands.literal("discord").executes(this::discordLink))
                        .then(Commands.literal("donate").executes(this::donateLink))
                        .then(Commands.literal("reloadapi").executes(this::reloadApi))
                        .executes(this::help));
    }

    private int reloadApi(CommandContext<CommandSourceStack> context) {
        WebManager.reset();

        boolean success = WebManager.setupUserAccount();

        success &= WebManager.reloadUsedRoutes();

        if (success) {
            context.getSource()
                    .sendSuccess(
                            new TextComponent("Successfully reloaded all used API routes.")
                                    .withStyle(ChatFormatting.GREEN),
                            false);
        } else {
            context.getSource()
                    .sendFailure(
                            new TextComponent("One or more API routes failed to reload")
                                    .withStyle(ChatFormatting.RED));
        }

        return 1;
    }

    private int donateLink(CommandContext<CommandSourceStack> context) {
        MutableComponent c =
                new TextComponent("You can donate to Wynntils at: ").withStyle(ChatFormatting.AQUA);
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
    }

    private int help(CommandContext<CommandSourceStack> context) {
        MutableComponent text =
                new TextComponent("").withStyle(Style.EMPTY.withColor(ChatFormatting.GOLD));
        text.append("Wynntils' command list: ");
        text.append("\n");
        addCommandDescription(
                text,
                "wynntils",
                List.of("help"),
                "This shows a list of all available commands for Wynntils.");
        text.append("\n");
        addCommandDescription(
                text,
                "wynntils",
                List.of("discord"),
                "This provides you with an invite to our Discord server.");
        text.append("\n");
        //            addCommandDescription(text, "-wynntils", " version", "This shows the
        // installed Wynntils version.");
        //            text.append("\n");
        //            addCommandDescription(text, "-wynntils", " changelog [major/latest]",
        // "This shows the changelog of your installed version.");
        //            text.append("\n");
        addCommandDescription(
                text, "-wynntils", List.of("reloadapi"), "This reloads all API data.");
        text.append("\n");
        addCommandDescription(
                text, "-wynntils", List.of("donate"), "This provides our Patreon link.");
        text.append("\n");
        addCommandDescription(
                text,
                "token",
                List.of(),
                "This provides a clickable token for you to create a Wynntils account to manage"
                        + " your cosmetics.");
        text.append("\n");
        addCommandDescription(
                text,
                "territory",
                List.of(),
                "This makes your compass point towards a specified territory.");
        context.getSource().sendSuccess(text, false);
        return 1;
    }

    private int discordLink(CommandContext<CommandSourceStack> context) {
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
    }

    private static void addCommandDescription(
            MutableComponent text, String prefix, List<String> suffix, String description) {
        MutableComponent clickComponent = new TextComponent("");
        {
            clickComponent.setStyle(
                    clickComponent
                            .getStyle()
                            .withClickEvent(
                                    new ClickEvent(
                                            ClickEvent.Action.SUGGEST_COMMAND,
                                            "/" + prefix + " " + String.join(" ", suffix)))
                            .withHoverEvent(
                                    new HoverEvent(
                                            HoverEvent.Action.SHOW_TEXT,
                                            new TextComponent(
                                                    "Click here to run this commannd."))));

            MutableComponent prefixText =
                    new TextComponent("-" + prefix).withStyle(ChatFormatting.DARK_GRAY);
            clickComponent.append(prefixText);

            if (!suffix.isEmpty()) {
                MutableComponent nameText =
                        new TextComponent(String.join(" ", suffix)).withStyle(ChatFormatting.GREEN);
                clickComponent.append(nameText);
            }

            clickComponent.append(" ");

            MutableComponent descriptionText =
                    new TextComponent(description)
                            .withStyle(Style.EMPTY.withColor(ChatFormatting.GRAY));
            clickComponent.append(descriptionText);
        }

        text.append(clickComponent);
    }
}
