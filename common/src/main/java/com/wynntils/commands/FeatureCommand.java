/*
 * Copyright © Wynntils 2022.
 * This file is released under AGPLv3. See LICENSE for full license details.
 */
package com.wynntils.commands;

import com.mojang.brigadier.arguments.StringArgumentType;
import com.mojang.brigadier.builder.LiteralArgumentBuilder;
import com.mojang.brigadier.context.CommandContext;
import com.mojang.brigadier.suggestion.SuggestionProvider;
import com.mojang.brigadier.tree.LiteralCommandNode;
import com.wynntils.core.WynntilsMod;
import com.wynntils.core.commands.CommandBase;
import com.wynntils.core.components.Managers;
import com.wynntils.core.features.DebugFeature;
import com.wynntils.core.features.Feature;
import com.wynntils.core.features.FeatureRegistry;
import com.wynntils.core.features.StateManagedFeature;
import com.wynntils.core.features.UserFeature;
import com.wynntils.core.features.properties.FeatureCategory;
import java.util.List;
import java.util.Optional;
import net.minecraft.ChatFormatting;
import net.minecraft.commands.CommandSourceStack;
import net.minecraft.commands.Commands;
import net.minecraft.commands.SharedSuggestionProvider;
import net.minecraft.network.chat.Component;
import net.minecraft.network.chat.HoverEvent;
import net.minecraft.network.chat.MutableComponent;

public class FeatureCommand extends CommandBase {
    private static final SuggestionProvider<CommandSourceStack> USER_FEATURE_SUGGESTION_PROVIDER =
            (context, builder) -> SharedSuggestionProvider.suggest(
                    FeatureRegistry.getFeatures().stream()
                            .filter(feature -> feature instanceof UserFeature && isVisible(feature))
                            .map(Feature::getShortName),
                    builder);

    @Override
    public LiteralArgumentBuilder<CommandSourceStack> getBaseCommandBuilder() {
        return Commands.literal("feature")
                .then(this.buildListNode())
                .then(this.enableFeatureNode())
                .then(this.disableFeatureNode())
                .then(this.reloadFeatureNode())
                .executes(this::syntaxError);
    }

    private int syntaxError(CommandContext<CommandSourceStack> context) {
        context.getSource().sendFailure(Component.literal("Missing argument").withStyle(ChatFormatting.RED));
        return 0;
    }

    private LiteralCommandNode<CommandSourceStack> buildListNode() {
        return Commands.literal("list").executes(this::listFeatures).build();
    }

    private int listFeatures(CommandContext<CommandSourceStack> context) {
        List<Feature> features = FeatureRegistry.getFeatures().stream()
                .filter(FeatureCommand::isVisible)
                .sorted(Feature::compareTo)
                .toList();

        MutableComponent response =
                Component.literal("Currently registered features:").withStyle(ChatFormatting.AQUA);

        FeatureCategory lastCategory = null;

        for (Feature feature : features) {
            Class<?> superclass = feature.getClass().getSuperclass();

            ChatFormatting color;
            String translatedName = feature.getTranslatedName();

            if (feature instanceof DebugFeature) {
                color = ChatFormatting.YELLOW;

                if (feature.isEnabled()) {
                    translatedName += " {ENABLED DEBUG}";
                } else {
                    translatedName += " {DISABLED DEBUG}";
                }
            } else {
                if (feature.isEnabled()) {
                    color = ChatFormatting.GREEN;
                } else {
                    color = ChatFormatting.RED;
                }
                if (feature instanceof StateManagedFeature) {
                    translatedName += " {SYSTEM CONTROLLED}";
                }
            }

            if (lastCategory != feature.getCategory()) {
                lastCategory = feature.getCategory();
                response.append(Component.literal("\n" + lastCategory.toString() + ":")
                        .withStyle(ChatFormatting.LIGHT_PURPLE)
                        .withStyle(ChatFormatting.BOLD));
            }

            response.append(Component.literal("\n - ").withStyle(ChatFormatting.GRAY))
                    .append(Component.literal(translatedName)
                            .withStyle(style -> style.withHoverEvent(new HoverEvent(
                                    HoverEvent.Action.SHOW_TEXT, Component.literal(superclass.getSimpleName()))))
                            .withStyle(color));
        }

        context.getSource().sendSuccess(response, false);

        return 1;
    }

    private static boolean isVisible(Feature feature) {
        if (!(feature instanceof DebugFeature)) return true;
        return WynntilsMod.isDevelopmentEnvironment();
    }

    private LiteralCommandNode<CommandSourceStack> enableFeatureNode() {
        return Commands.literal("enable")
                .then(Commands.argument("feature", StringArgumentType.word())
                        .suggests(USER_FEATURE_SUGGESTION_PROVIDER)
                        .executes(this::enableFeature))
                .build();
    }

    private int enableFeature(CommandContext<CommandSourceStack> context) {
        String featureName = context.getArgument("feature", String.class);

        Optional<Feature> featureOptional = FeatureRegistry.getFeatureFromString(featureName);

        if (featureOptional.isEmpty() || !(featureOptional.get() instanceof UserFeature feature)) {
            context.getSource()
                    .sendFailure(Component.literal("Feature not found!").withStyle(ChatFormatting.RED));
            return 0;
        }

        if (feature.isEnabled()) {
            context.getSource()
                    .sendFailure(Component.literal("Feature " + feature.getTranslatedName() + " is already enabled!")
                            .withStyle(ChatFormatting.RED));
            return 1;
        }

        feature.setUserEnabled(true);
        feature.tryUserToggle();

        if (!feature.isEnabled()) {
            context.getSource()
                    .sendFailure(Component.literal("Feature " + feature.getTranslatedName() + " could not be enabled!")
                            .withStyle(ChatFormatting.RED));
            return 1;
        }

        Managers.Config.saveConfig();

        context.getSource()
                .sendSuccess(
                        Component.literal(feature.getTranslatedName() + " was enabled successfully.")
                                .withStyle(ChatFormatting.GREEN),
                        false);

        return 1;
    }

    private LiteralCommandNode<CommandSourceStack> disableFeatureNode() {
        return Commands.literal("disable")
                .then(Commands.argument("feature", StringArgumentType.word())
                        .suggests(USER_FEATURE_SUGGESTION_PROVIDER)
                        .executes(this::disableFeature))
                .build();
    }

    private int disableFeature(CommandContext<CommandSourceStack> context) {
        String featureName = context.getArgument("feature", String.class);

        Optional<Feature> featureOptional = FeatureRegistry.getFeatureFromString(featureName);

        if (featureOptional.isEmpty() || !(featureOptional.get() instanceof UserFeature feature)) {
            context.getSource()
                    .sendFailure(Component.literal("Feature not found!").withStyle(ChatFormatting.RED));
            return 0;
        }

        if (!feature.isEnabled()) {
            context.getSource()
                    .sendFailure(Component.literal("Feature " + feature.getTranslatedName() + " is already disabled!")
                            .withStyle(ChatFormatting.RED));
            return 1;
        }

        feature.setUserEnabled(false);
        feature.tryUserToggle();

        if (feature.isEnabled()) {
            context.getSource()
                    .sendFailure(Component.literal("Feature " + feature.getTranslatedName() + " could not be disabled!")
                            .withStyle(ChatFormatting.RED));
            return 1;
        }

        Managers.Config.saveConfig();

        context.getSource()
                .sendSuccess(
                        Component.literal(feature.getTranslatedName() + " was disabled successfully.")
                                .withStyle(ChatFormatting.GREEN),
                        false);

        return 1;
    }

    private LiteralCommandNode<CommandSourceStack> reloadFeatureNode() {
        return Commands.literal("reload")
                .then(Commands.argument("feature", StringArgumentType.word())
                        .suggests(USER_FEATURE_SUGGESTION_PROVIDER)
                        .executes(this::reloadFeature))
                .build();
    }

    private int reloadFeature(CommandContext<CommandSourceStack> context) {
        String featureName = context.getArgument("feature", String.class);

        Optional<Feature> featureOptional = FeatureRegistry.getFeatureFromString(featureName);

        if (featureOptional.isEmpty() || !(featureOptional.get() instanceof UserFeature feature)) {
            context.getSource()
                    .sendFailure(Component.literal("Feature not found!").withStyle(ChatFormatting.RED));
            return 0;
        }

        if (!feature.isEnabled()) {
            context.getSource()
                    .sendFailure(Component.literal("Feature " + feature.getTranslatedName()
                                    + " is already disabled, cannot reload a disabled feature!")
                            .withStyle(ChatFormatting.RED));
            return 1;
        }

        feature.disable();

        if (feature.isEnabled()) {
            context.getSource()
                    .sendFailure(Component.literal("Feature " + feature.getTranslatedName() + " could not be disabled!")
                            .withStyle(ChatFormatting.RED));
            return 1;
        }

        feature.enable();

        if (!feature.isEnabled()) {
            context.getSource()
                    .sendFailure(Component.literal("Feature " + feature.getTranslatedName() + " could not be enabled!")
                            .withStyle(ChatFormatting.RED));
            return 1;
        }

        context.getSource()
                .sendSuccess(
                        Component.literal(feature.getTranslatedName() + " was reloaded successfully.")
                                .withStyle(ChatFormatting.GREEN),
                        false);

        return 1;
    }
}
