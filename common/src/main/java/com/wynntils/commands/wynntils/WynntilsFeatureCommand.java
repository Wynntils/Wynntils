/*
 * Copyright © Wynntils 2022.
 * This file is released under AGPLv3. See LICENSE for full license details.
 */
package com.wynntils.commands.wynntils;

import com.mojang.brigadier.arguments.StringArgumentType;
import com.mojang.brigadier.context.CommandContext;
import com.mojang.brigadier.suggestion.SuggestionProvider;
import com.mojang.brigadier.tree.LiteralCommandNode;
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

public final class WynntilsFeatureCommand {
    private WynntilsFeatureCommand() {}

    private static final SuggestionProvider<CommandSourceStack> userFeatureSuggestionProvider =
            (context, builder) -> SharedSuggestionProvider.suggest(
                    FeatureRegistry.getFeatures().stream()
                            .filter(feature -> feature.getClass().getSuperclass() == UserFeature.class)
                            .map(Feature::getShortName),
                    builder);

    public static LiteralCommandNode<CommandSourceStack> buildListNode() {
        return Commands.literal("list")
                .executes(WynntilsFeatureCommand::listFeatures)
                .build();
    }

    private static int listFeatures(CommandContext<CommandSourceStack> context) {
        Set<Feature> features = FeatureRegistry.getFeatures().stream().collect(Collectors.toUnmodifiableSet());

        MutableComponent response = new TextComponent("Currently registered features:").withStyle(ChatFormatting.AQUA);

        for (Feature feature : features) {
            response.append(new TextComponent("\n - ").withStyle(ChatFormatting.GRAY))
                    .append(new TextComponent(feature.getTranslatedName()).withStyle(ChatFormatting.YELLOW));
        }

        context.getSource().sendSuccess(response, false);

        return 1;
    }

    public static LiteralCommandNode<CommandSourceStack> enableFeatureNode() {
        return Commands.literal("enable")
                .then(Commands.argument("feature", StringArgumentType.word())
                        .suggests(userFeatureSuggestionProvider)
                        .executes(WynntilsFeatureCommand::enableFeature))
                .build();
    }

    private static int enableFeature(CommandContext<CommandSourceStack> context) {
        String featureName = context.getArgument("feature", String.class);

        Optional<Feature> featureOptional = FeatureRegistry.getFeatureFromString(featureName);

        if (featureOptional.isEmpty() || !(featureOptional.get() instanceof UserFeature feature)) {
            context.getSource().sendFailure(new TextComponent("Feature not found!").withStyle(ChatFormatting.RED));
            return 0;
        }

        if (feature.isEnabled()) {
            context.getSource()
                    .sendFailure(new TextComponent("Feature " + feature.getShortName() + " is already enabled!")
                            .withStyle(ChatFormatting.RED));
            return 1;
        }

        feature.setUserEnabled(true);
        feature.tryUserToggle();

        if (!feature.isEnabled()) {
            context.getSource()
                    .sendFailure(new TextComponent("Feature " + feature.getShortName() + " could not be enabled!")
                            .withStyle(ChatFormatting.RED));
            return 1;
        }

        ConfigManager.saveConfig();

        context.getSource()
                .sendSuccess(
                        new TextComponent(feature.getShortName() + " was enabled successfully.")
                                .withStyle(ChatFormatting.GREEN),
                        false);

        return 1;
    }

    public static LiteralCommandNode<CommandSourceStack> disableFeatureNode() {
        return Commands.literal("disable")
                .then(Commands.argument("feature", StringArgumentType.word())
                        .suggests(userFeatureSuggestionProvider)
                        .executes(WynntilsFeatureCommand::disableFeature))
                .build();
    }

    private static int disableFeature(CommandContext<CommandSourceStack> context) {
        String featureName = context.getArgument("feature", String.class);

        Optional<Feature> featureOptional = FeatureRegistry.getFeatureFromString(featureName);

        if (featureOptional.isEmpty() || !(featureOptional.get() instanceof UserFeature feature)) {
            context.getSource().sendFailure(new TextComponent("Feature not found!").withStyle(ChatFormatting.RED));
            return 0;
        }

        if (!feature.isEnabled()) {
            context.getSource()
                    .sendFailure(new TextComponent("Feature " + feature.getShortName() + " is already disabled!")
                            .withStyle(ChatFormatting.RED));
            return 1;
        }

        feature.setUserEnabled(false);
        feature.tryUserToggle();

        if (feature.isEnabled()) {
            context.getSource()
                    .sendFailure(new TextComponent("Feature " + feature.getShortName() + " could not be disabled!")
                            .withStyle(ChatFormatting.RED));
            return 1;
        }

        ConfigManager.saveConfig();

        context.getSource()
                .sendSuccess(
                        new TextComponent(feature.getShortName() + " was disabled successfully.")
                                .withStyle(ChatFormatting.GREEN),
                        false);

        return 1;
    }
}
