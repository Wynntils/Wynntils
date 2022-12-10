/*
 * Copyright © Wynntils 2022.
 * This file is released under AGPLv3. See LICENSE for full license details.
 */
package com.wynntils.commands;

import com.mojang.brigadier.CommandDispatcher;
import com.mojang.brigadier.builder.LiteralArgumentBuilder;
import com.mojang.brigadier.context.CommandContext;
import com.wynntils.core.WynntilsMod;
import com.wynntils.core.commands.ClientCommandManager;
import com.wynntils.core.commands.CommandBase;
import com.wynntils.core.features.Feature;
import com.wynntils.core.features.FeatureRegistry;
import com.wynntils.core.net.UrlId;
import com.wynntils.core.net.UrlManager;
import com.wynntils.core.net.athena.WynntilsAccountManager;
import com.wynntils.mc.utils.McUtils;
import com.wynntils.wynn.model.ItemProfilesManager;
import com.wynntils.wynn.model.SplashManager;
import java.util.List;
import net.minecraft.ChatFormatting;
import net.minecraft.commands.CommandSourceStack;
import net.minecraft.commands.Commands;
import net.minecraft.network.chat.ClickEvent;
import net.minecraft.network.chat.HoverEvent;
import net.minecraft.network.chat.MutableComponent;
import net.minecraft.network.chat.Style;
import net.minecraft.network.chat.TextComponent;

public class WynntilsCommand extends CommandBase {
    @Override
    public void register(CommandDispatcher<CommandSourceStack> dispatcher) {
        LiteralArgumentBuilder<CommandSourceStack> builder = getBaseCommandBuilder();

        // Register all commands under the wynntils command as subcommands
        for (CommandBase commandInstance : ClientCommandManager.getCommandInstanceSet()) {
            if (commandInstance == this) continue;

            builder.then(commandInstance.getBaseCommandBuilder());
        }

        dispatcher.register(builder);
    }

    @Override
    public LiteralArgumentBuilder<CommandSourceStack> getBaseCommandBuilder() {
        return Commands.literal("wynntils")
                .then(Commands.literal("help").executes(this::help))
                .then(Commands.literal("discord").executes(this::discordLink))
                .then(Commands.literal("donate").executes(this::donateLink))
                .then(Commands.literal("reload").executes(this::reload))
                .then(Commands.literal("version").executes(this::version))
                .executes(this::help);
    }

    private int version(CommandContext<CommandSourceStack> context) {
        // TODO: Handle if dev env

        MutableComponent buildText;

        if (WynntilsMod.getVersion().isEmpty()) {
            buildText = new TextComponent("Unknown Version");
        } else if (WynntilsMod.isDevelopmentBuild()) {
            buildText = new TextComponent("Development Build");
        } else {
            buildText = new TextComponent("Version " + WynntilsMod.getVersion());
        }

        buildText.setStyle(buildText.getStyle().withColor(ChatFormatting.YELLOW));

        context.getSource().sendSuccess(buildText, false);
        return 1;
    }

    private int reload(CommandContext<CommandSourceStack> context) {
        List<Feature> enabledFeatures = FeatureRegistry.getFeatures().stream()
                .filter(Feature::isEnabled)
                .toList();

        for (Feature feature : enabledFeatures) { // disable all active features before resetting web
            feature.disable();
        }

        // FIXME: This probably does not do what it was intended to do.
        // reset
        ItemProfilesManager.reset();
        // reloads api urls as well as web manager
        UrlManager.reloadUrls();
        ItemProfilesManager.init();
        SplashManager.init();
        WynntilsAccountManager.init();

        for (Feature feature : enabledFeatures) { // re-enable all features which should be
            if (feature.canEnable()) {
                feature.enable();

                if (feature.isEnabled()) {
                    McUtils.sendMessageToClient(new TextComponent("Reloaded ")
                            .withStyle(ChatFormatting.GREEN)
                            .append(new TextComponent(feature.getTranslatedName()).withStyle(ChatFormatting.AQUA)));

                    continue;
                }
            }

            McUtils.sendMessageToClient(new TextComponent("Failed to reload ")
                    .withStyle(ChatFormatting.GREEN)
                    .append(new TextComponent(feature.getTranslatedName()).withStyle(ChatFormatting.RED)));
        }

        context.getSource()
                .sendSuccess(new TextComponent("Finished reloading everything").withStyle(ChatFormatting.GREEN), false);

        return 1;
    }

    private int donateLink(CommandContext<CommandSourceStack> context) {
        MutableComponent c = new TextComponent("You can donate to Wynntils at: ").withStyle(ChatFormatting.AQUA);
        MutableComponent url = new TextComponent(UrlManager.getUrl(UrlId.LINK_WYNNTILS_PATREON))
                .withStyle(Style.EMPTY
                        .withColor(ChatFormatting.LIGHT_PURPLE)
                        .withUnderlined(true)
                        .withClickEvent(new ClickEvent(
                                ClickEvent.Action.OPEN_URL, UrlManager.getUrl(UrlId.LINK_WYNNTILS_PATREON)))
                        .withHoverEvent(new HoverEvent(
                                HoverEvent.Action.SHOW_TEXT,
                                new TextComponent("Click here to open in your" + " browser."))));

        context.getSource().sendSuccess(c.append(url), false);
        return 1;
    }

    private int help(CommandContext<CommandSourceStack> context) {
        MutableComponent text =
                new TextComponent("Wynntils' command list: ").withStyle(Style.EMPTY.withColor(ChatFormatting.GOLD));
        addCommandDescription(
                text, "wynntils", List.of("help"), "This shows a list of all available commands for Wynntils.");
        addCommandDescription(
                text, "wynntils", List.of("discord"), "This provides you with an invite to our Discord server.");
        addCommandDescription(text, "wynntils", List.of("version"), "This shows the installed Wynntils version.");
        //            addCommandDescription(text, "-wynntils", " changelog [major/latest]",
        // "This shows the changelog of your installed version.");
        //            text.append("\n");
        addCommandDescription(text, "wynntils", List.of("reload"), "This reloads all API data.");
        addCommandDescription(text, "wynntils", List.of("donate"), "This provides our Patreon link.");
        addCommandDescription(
                text,
                "token",
                List.of(),
                "This provides a clickable token for you to create a Wynntils account to manage" + " your cosmetics.");
        addCommandDescription(
                text, "territory", List.of(), "This makes your compass point towards a specified territory.");
        context.getSource().sendSuccess(text, false);
        return 1;
    }

    private int discordLink(CommandContext<CommandSourceStack> context) {
        MutableComponent msg =
                new TextComponent("You're welcome to join our Discord server at:\n").withStyle(ChatFormatting.GOLD);
        String discordInvite = UrlManager.getUrl(UrlId.LINK_WYNNTILS_DISCORD_INVITE);
        MutableComponent link =
                new TextComponent(discordInvite).withStyle(Style.EMPTY.withColor(ChatFormatting.DARK_AQUA));
        link.setStyle(link.getStyle()
                .withClickEvent(new ClickEvent(ClickEvent.Action.OPEN_URL, discordInvite))
                .withHoverEvent(new HoverEvent(
                        HoverEvent.Action.SHOW_TEXT,
                        new TextComponent("Click here to join our Discord" + " server."))));
        context.getSource().sendSuccess(msg.append(link), false);
        return 1;
    }

    private static void addCommandDescription(
            MutableComponent text, String prefix, List<String> suffix, String description) {
        text.append("\n");

        StringBuilder suffixString = new StringBuilder();

        for (String argument : suffix) {
            suffixString.append(" ").append(argument);
        }

        MutableComponent clickComponent = new TextComponent("");
        {
            clickComponent.setStyle(clickComponent
                    .getStyle()
                    .withClickEvent(new ClickEvent(ClickEvent.Action.RUN_COMMAND, "/" + prefix + suffixString))
                    .withHoverEvent(new HoverEvent(
                            HoverEvent.Action.SHOW_TEXT, new TextComponent("Click here to run this command."))));

            MutableComponent prefixText = new TextComponent("-" + prefix).withStyle(ChatFormatting.DARK_GRAY);
            clickComponent.append(prefixText);

            if (!suffix.isEmpty()) {
                MutableComponent nameText = new TextComponent(suffixString.toString()).withStyle(ChatFormatting.GREEN);
                clickComponent.append(nameText);
            }

            clickComponent.append(" ");

            MutableComponent descriptionText =
                    new TextComponent(description).withStyle(Style.EMPTY.withColor(ChatFormatting.GRAY));
            clickComponent.append(descriptionText);
        }

        text.append(clickComponent);
    }
}
