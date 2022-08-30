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
import com.wynntils.wynn.model.questbook.QuestBookModel;
import com.wynntils.wynn.model.questbook.QuestInfo;
import java.util.List;
import java.util.Locale;
import net.minecraft.ChatFormatting;
import net.minecraft.commands.CommandSourceStack;
import net.minecraft.commands.Commands;
import net.minecraft.commands.SharedSuggestionProvider;
import net.minecraft.network.chat.ClickEvent;
import net.minecraft.network.chat.HoverEvent;
import net.minecraft.network.chat.MutableComponent;
import net.minecraft.network.chat.TextComponent;

public class QuestCommand extends CommandBase {
    private static final SuggestionProvider<CommandSourceStack> QUEST_SUGGESTION_PROVIDER =
            (context, builder) -> SharedSuggestionProvider.suggest(
                    QuestBookModel.getQuests().stream()
                            .map(questInfo -> questInfo.getName().replaceAll(" ", " ")),
                    builder);

    @Override
    public LiteralArgumentBuilder<CommandSourceStack> getBaseCommandBuilder() {
        return Commands.literal("quest")
                .then(Commands.literal("list").executes(this::listQuests))
                .then(Commands.literal("info")
                        .then(Commands.argument("quest", StringArgumentType.greedyString())
                                .suggests(QUEST_SUGGESTION_PROVIDER)
                                .executes(this::questInfo)))
                .then(Commands.literal("track")
                        .then(Commands.argument("quest", StringArgumentType.greedyString())
                                .suggests(QUEST_SUGGESTION_PROVIDER)
                                .executes(this::trackQuest)))
                .then(Commands.literal("untrack").executes(this::untrackQuest))
                .then(Commands.literal("wiki")
                        .then(Commands.argument("quest", StringArgumentType.greedyString())
                                .suggests(QUEST_SUGGESTION_PROVIDER)
                                .executes(this::lookupOnWiki)))
                .executes(this::syntaxError);
    }

    private int listQuests(CommandContext<CommandSourceStack> context) {
        List<QuestInfo> quests = QuestBookModel.getQuests();
        // FIXME: sort argument "level", "distance", "alphabetical"
        // FIXME: filter "started", "can start", etc

        MutableComponent response = new TextComponent("All known quests:").withStyle(ChatFormatting.AQUA);

        for (QuestInfo quest : quests) {
            response.append(new TextComponent("\n - ").withStyle(ChatFormatting.GRAY))
                    .append(new TextComponent(quest.getName())
                            .withStyle(style -> style.withClickEvent(new ClickEvent(
                                    ClickEvent.Action.RUN_COMMAND,
                                    "/quest info " + quest.getName().replaceAll(" ", " "))))
                            .withStyle(style -> style.withHoverEvent(
                                    new HoverEvent(HoverEvent.Action.SHOW_TEXT, new TextComponent("Click for info"))))
                            .withStyle(ChatFormatting.WHITE));
        }

        context.getSource().sendSuccess(response, false);
        return 1;
    }

    private int questInfo(CommandContext<CommandSourceStack> context) {
        String questName = context.getArgument("quest", String.class).toLowerCase(Locale.ROOT);
        List<QuestInfo> matchingQuests = QuestBookModel.getQuests().stream()
                .filter(questInfo ->
                        questInfo.getName().toLowerCase(Locale.ROOT).contains(questName))
                .toList();

        if (matchingQuests.size() < 1) {
            context.getSource()
                    .sendFailure(
                            new TextComponent("Quest '" + questName + "' not found").withStyle(ChatFormatting.RED));
            return 0;
        } else if (matchingQuests.size() > 1) {
            MutableComponent error =
                    new TextComponent("Quest '" + questName + "' match several quests:").withStyle(ChatFormatting.RED);
            for (var quest : matchingQuests) {
                error.append(new TextComponent("\n - ").withStyle(ChatFormatting.GRAY))
                        .append(new TextComponent(quest.getName()).withStyle(ChatFormatting.WHITE));
            }
            context.getSource().sendFailure(error);
            return 0;
        }

        QuestInfo quest = matchingQuests.get(0);

        MutableComponent response =
                new TextComponent("Info for quest: " + quest.getName()).withStyle(ChatFormatting.AQUA);
        response.append(new TextComponent("\n - Status: " + quest.getStatus()).withStyle(ChatFormatting.WHITE))
                .append(new TextComponent("\n - Level: " + quest.getLevel()).withStyle(ChatFormatting.WHITE))
                .append(new TextComponent("\n - Length: " + quest.getLength()).withStyle(ChatFormatting.WHITE))
                // FIXME: additional requirements are missing
                .append(new TextComponent("\n - Next Task: " + quest.getNextTask()).withStyle(ChatFormatting.WHITE))
                .append(new TextComponent("\n"))
                .append(new TextComponent("[Track Quest]")
                        .withStyle(style -> style.withClickEvent(new ClickEvent(
                                        ClickEvent.Action.RUN_COMMAND, "/quest track " + quest.getName()))
                                .withHoverEvent(new HoverEvent(
                                        HoverEvent.Action.SHOW_TEXT, new TextComponent("Click to track quest"))))
                        .withStyle(ChatFormatting.WHITE))
                .append(new TextComponent(" "))
                .append(new TextComponent("[Lookup on Wiki]")
                        .withStyle(style -> style.withClickEvent(
                                        new ClickEvent(ClickEvent.Action.RUN_COMMAND, "/quest wiki " + quest.getName()))
                                .withHoverEvent(new HoverEvent(
                                        HoverEvent.Action.SHOW_TEXT,
                                        new TextComponent("Click to lookup quest on wiki"))))
                        .withStyle(ChatFormatting.WHITE));

        context.getSource().sendSuccess(response, false);
        return 1;
    }

    private int trackQuest(CommandContext<CommandSourceStack> context) {
        String questName = context.getArgument("quest", String.class);

        context.getSource()
                .sendFailure(new TextComponent("Track Quest not implemented yet for " + questName)
                        .withStyle(ChatFormatting.RED));
        return 0;
    }

    private int untrackQuest(CommandContext<CommandSourceStack> context) {
        context.getSource()
                .sendFailure(new TextComponent("Untrack Quest not implemented yet ").withStyle(ChatFormatting.RED));
        return 0;
    }

    private int lookupOnWiki(CommandContext<CommandSourceStack> context) {
        String questName = context.getArgument("quest", String.class);

        context.getSource()
                .sendFailure(new TextComponent("Lookup Quest not implemented yet for " + questName)
                        .withStyle(ChatFormatting.RED));
        return 0;
    }

    private int syntaxError(CommandContext<CommandSourceStack> context) {
        context.getSource().sendFailure(new TextComponent("Missing argument").withStyle(ChatFormatting.RED));
        return 0;
    }
}
