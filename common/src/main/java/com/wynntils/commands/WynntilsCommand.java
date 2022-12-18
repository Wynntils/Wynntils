/*
 * Copyright © Wynntils 2022.
 * This file is released under AGPLv3. See LICENSE for full license details.
 */
package com.wynntils.commands;

import com.mojang.brigadier.CommandDispatcher;
import com.mojang.brigadier.builder.LiteralArgumentBuilder;
import com.mojang.brigadier.context.CommandContext;
import com.wynntils.core.WynntilsMod;
import com.wynntils.core.commands.CommandBase;
import com.wynntils.core.features.Feature;
import com.wynntils.core.features.FeatureRegistry;
import com.wynntils.core.managers.Managers;
import com.wynntils.core.net.UrlId;
import com.wynntils.mc.utils.McUtils;
import java.util.List;
import java.util.Set;
import net.minecraft.ChatFormatting;
import net.minecraft.commands.CommandSourceStack;
import net.minecraft.commands.Commands;
import net.minecraft.network.chat.ClickEvent;
import net.minecraft.network.chat.Component;
import net.minecraft.network.chat.HoverEvent;
import net.minecraft.network.chat.MutableComponent;
import net.minecraft.network.chat.Style;

public class WynntilsCommand extends CommandBase {
    @Override
    public void register(CommandDispatcher<CommandSourceStack> dispatcher) {
        throw new UnsupportedOperationException("WynntilsCommand need special treatment");
    }

    public void registerWithCommands(CommandDispatcher<CommandSourceStack> dispatcher, Set<CommandBase> commands) {
        LiteralArgumentBuilder<CommandSourceStack> builder = getBaseCommandBuilder();

        // Register all commands under the wynntils command as subcommands
        for (CommandBase commandInstance : commands) {
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
            buildText = Component.literal("Unknown Version");
        } else if (WynntilsMod.isDevelopmentBuild()) {
            buildText = Component.literal("Development Build");
        } else {
            buildText = Component.literal("Version " + WynntilsMod.getVersion());
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

        // Try to reload downloaded data.
        // It is highly unclear if this achieves anything like what it was supposed
        // to do. The entire /wynntils reload needs to be rethought. See
        // https://github.com/Wynntils/Artemis/issues/824

        Managers.ItemProfiles.reset();
        Managers.Url.reloadUrls();
        Managers.Splash.reset();
        Managers.WynntilsAccount.reset();

        for (Feature feature : enabledFeatures) { // re-enable all features which should be
            if (feature.canEnable()) {
                feature.enable();

                if (feature.isEnabled()) {
                    McUtils.sendMessageToClient(Component.literal("Reloaded ")
                            .withStyle(ChatFormatting.GREEN)
                            .append(Component.literal(feature.getTranslatedName())
                                    .withStyle(ChatFormatting.AQUA)));

                    continue;
                }
            }

            McUtils.sendMessageToClient(Component.literal("Failed to reload ")
                    .withStyle(ChatFormatting.GREEN)
                    .append(Component.literal(feature.getTranslatedName()).withStyle(ChatFormatting.RED)));
        }

        context.getSource()
                .sendSuccess(Component.literal("Finished reloading everything").withStyle(ChatFormatting.GREEN), false);

        return 1;
    }

    private int donateLink(CommandContext<CommandSourceStack> context) {
        MutableComponent c =
                Component.literal("You can donate to Wynntils at: ").withStyle(ChatFormatting.AQUA);
        MutableComponent url = Component.literal(Managers.Url.getUrl(UrlId.LINK_WYNNTILS_PATREON))
                .withStyle(Style.EMPTY
                        .withColor(ChatFormatting.LIGHT_PURPLE)
                        .withUnderlined(true)
                        .withClickEvent(new ClickEvent(
                                ClickEvent.Action.OPEN_URL, Managers.Url.getUrl(UrlId.LINK_WYNNTILS_PATREON)))
                        .withHoverEvent(new HoverEvent(
                                HoverEvent.Action.SHOW_TEXT,
                                Component.literal("Click here to open in your" + " browser."))));

        context.getSource().sendSuccess(c.append(url), false);
        return 1;
    }

    private int help(CommandContext<CommandSourceStack> context) {
        MutableComponent text =
                Component.literal("Wynntils' command list: ").withStyle(Style.EMPTY.withColor(ChatFormatting.GOLD));
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
                new Component.literal("You're welcome to join our Discord server at:\n").withStyle(ChatFormatting.GOLD);
        String discordInvite = Managers.Url.getUrl(UrlId.LINK_WYNNTILS_DISCORD_INVITE);
        MutableComponent link =
                new Component.literal(discordInvite).withStyle(Style.EMPTY.withColor(ChatFormatting.DARK_AQUA));
        link.setStyle(link.getStyle()
                .withClickEvent(new ClickEvent(ClickEvent.Action.OPEN_URL, discordInvite))
                .withHoverEvent(new HoverEvent(
                        HoverEvent.Action.SHOW_TEXT,
                        new Component.literal("Click here to join our Discord" + " server."))));
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

        MutableComponent clickComponent = Component.literal("");
        {
            clickComponent.setStyle(clickComponent
                    .getStyle()
                    .withClickEvent(new ClickEvent(ClickEvent.Action.RUN_COMMAND, "/" + prefix + suffixString))
                    .withHoverEvent(new HoverEvent(
                            HoverEvent.Action.SHOW_TEXT, Component.literal("Click here to run this command."))));

            MutableComponent prefixText = Component.literal("-" + prefix).withStyle(ChatFormatting.DARK_GRAY);
            clickComponent.append(prefixText);

            if (!suffix.isEmpty()) {
                MutableComponent nameText =
                        Component.literal(suffixString.toString()).withStyle(ChatFormatting.GREEN);
                clickComponent.append(nameText);
            }

            clickComponent.append(" ");

            MutableComponent descriptionText =
                    Component.literal(description).withStyle(Style.EMPTY.withColor(ChatFormatting.GRAY));
            clickComponent.append(descriptionText);
        }

        text.append(clickComponent);
    }
}
