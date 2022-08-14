/*
 * Copyright © Wynntils 2022.
 * This file is released under AGPLv3. See LICENSE for full license details.
 */
package com.wynntils.commands;

import static net.minecraft.commands.Commands.literal;

import com.mojang.brigadier.CommandDispatcher;
import com.mojang.brigadier.arguments.StringArgumentType;
import com.mojang.brigadier.context.CommandContext;
import com.mojang.brigadier.suggestion.SuggestionProvider;
import com.mojang.brigadier.tree.LiteralCommandNode;
import com.wynntils.core.commands.CommandBase;
import com.wynntils.core.config.ConfigManager;
import com.wynntils.core.features.Feature;
import com.wynntils.core.features.FeatureRegistry;
import com.wynntils.core.features.UserFeature;
import java.util.Optional;
import java.util.Set;
import java.util.stream.Collectors;
import net.minecraft.ChatFormatting;
import net.minecraft.commands.CommandSourceStack;
import net.minecraft.commands.Commands;
import net.minecraft.commands.SharedSuggestionProvider;
import net.minecraft.network.chat.MutableComponent;
import net.minecraft.network.chat.TextComponent;

public class FeatureCommand extends CommandBase {
    private static final SuggestionProvider<CommandSourceStack> USER_FEATURE_SUGGESTION_PROVIDER =
            (context, builder) -> SharedSuggestionProvider.suggest(
                    FeatureRegistry.getFeatures().stream()
                            .filter(feature -> feature.getClass().getSuperclass() == UserFeature.class)
                            .map(Feature::getShortName),
                    builder);

    @Override
    public void register(CommandDispatcher<CommandSourceStack> dispatcher) {
        dispatcher.register(literal("feature")
                .then(this.buildListNode())
                .then(this.enableFeatureNode())
                .then(this.disableFeatureNode())
                .executes(this::syntaxError));
    }

    private int syntaxError(CommandContext<CommandSourceStack> context) {
        context.getSource().sendFailure(new TextComponent("Missing argument").withStyle(ChatFormatting.RED));
        return 0;
    }

    private LiteralCommandNode<CommandSourceStack> buildListNode() {
        return literal("list").executes(this::listFeatures).build();
    }

    private int listFeatures(CommandContext<CommandSourceStack> context) {
        Set<Feature> features = FeatureRegistry.getFeatures().stream().collect(Collectors.toUnmodifiableSet());

        MutableComponent response = new TextComponent("Currently registered features:").withStyle(ChatFormatting.AQUA);

        for (Feature feature : features) {
            response.append(new TextComponent("\n - ").withStyle(ChatFormatting.GRAY))
                    .append(new TextComponent(feature.getTranslatedName())
                            .withStyle(
                                    feature.getClass().getSuperclass() == UserFeature.class
                                            ? ChatFormatting.YELLOW
                                            : ChatFormatting.RED));
        }

        context.getSource().sendSuccess(response, false);

        return 1;
    }

    private LiteralCommandNode<CommandSourceStack> enableFeatureNode() {
        return literal("enable")
                .then(Commands.argument("feature", StringArgumentType.word())
                        .suggests(USER_FEATURE_SUGGESTION_PROVIDER)
                        .executes(this::enableFeature))
                .build();
    }

    private int enableFeature(CommandContext<CommandSourceStack> context) {
        String featureName = context.getArgument("feature", String.class);

        Optional<Feature> featureOptional = FeatureRegistry.getFeatureFromString(featureName);

        if (featureOptional.isEmpty() || !(featureOptional.get() instanceof UserFeature feature)) {
            context.getSource().sendFailure(new TextComponent("Feature not found!").withStyle(ChatFormatting.RED));
            return 0;
        }

        if (feature.isEnabled()) {
            context.getSource()
                    .sendFailure(new TextComponent("Feature " + feature.getTranslatedName() + " is already enabled!")
                            .withStyle(ChatFormatting.RED));
            return 1;
        }

        feature.setUserEnabled(true);
        feature.tryUserToggle();

        if (!feature.isEnabled()) {
            context.getSource()
                    .sendFailure(new TextComponent("Feature " + feature.getTranslatedName() + " could not be enabled!")
                            .withStyle(ChatFormatting.RED));
            return 1;
        }

        ConfigManager.saveConfig();

        context.getSource()
                .sendSuccess(
                        new TextComponent(feature.getTranslatedName() + " was enabled successfully.")
                                .withStyle(ChatFormatting.GREEN),
                        false);

        return 1;
    }

    private LiteralCommandNode<CommandSourceStack> disableFeatureNode() {
        return literal("disable")
                .then(Commands.argument("feature", StringArgumentType.word())
                        .suggests(USER_FEATURE_SUGGESTION_PROVIDER)
                        .executes(this::disableFeature))
                .build();
    }

    private int disableFeature(CommandContext<CommandSourceStack> context) {
        String featureName = context.getArgument("feature", String.class);

        Optional<Feature> featureOptional = FeatureRegistry.getFeatureFromString(featureName);

        if (featureOptional.isEmpty() || !(featureOptional.get() instanceof UserFeature feature)) {
            context.getSource().sendFailure(new TextComponent("Feature not found!").withStyle(ChatFormatting.RED));
            return 0;
        }

        if (!feature.isEnabled()) {
            context.getSource()
                    .sendFailure(new TextComponent("Feature " + feature.getTranslatedName() + " is already disabled!")
                            .withStyle(ChatFormatting.RED));
            return 1;
        }

        feature.setUserEnabled(false);
        feature.tryUserToggle();

        if (feature.isEnabled()) {
            context.getSource()
                    .sendFailure(new TextComponent("Feature " + feature.getTranslatedName() + " could not be disabled!")
                            .withStyle(ChatFormatting.RED));
            return 1;
        }

        ConfigManager.saveConfig();

        context.getSource()
                .sendSuccess(
                        new TextComponent(feature.getTranslatedName() + " was disabled successfully.")
                                .withStyle(ChatFormatting.GREEN),
                        false);

        return 1;
    }
}
