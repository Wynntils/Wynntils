/*
 * Copyright © Wynntils 2022.
 * This file is released under AGPLv3. See LICENSE for full license details.
 */
package com.wynntils.commands.wynntils;

import com.google.common.base.CaseFormat;
import com.mojang.brigadier.arguments.StringArgumentType;
import com.mojang.brigadier.builder.LiteralArgumentBuilder;
import com.mojang.brigadier.context.CommandContext;
import com.mojang.brigadier.suggestion.SuggestionProvider;
import com.wynntils.core.config.ConfigHolder;
import com.wynntils.core.config.ConfigManager;
import com.wynntils.core.features.Feature;
import com.wynntils.core.features.FeatureRegistry;
import java.util.Arrays;
import java.util.Collections;
import java.util.Optional;
import java.util.stream.Collectors;
import net.minecraft.ChatFormatting;
import net.minecraft.commands.CommandSourceStack;
import net.minecraft.commands.Commands;
import net.minecraft.commands.SharedSuggestionProvider;
import net.minecraft.network.chat.ClickEvent;
import net.minecraft.network.chat.HoverEvent;
import net.minecraft.network.chat.MutableComponent;
import net.minecraft.network.chat.TextComponent;
import org.apache.commons.lang3.StringUtils;

public abstract class WynntilsConfigCommand {
    public static final SuggestionProvider<CommandSourceStack> featureSuggestionProvider =
            (context, builder) -> SharedSuggestionProvider.suggest(
                    FeatureRegistry.getFeatures().stream().map(Feature::getShortName), builder);

    public static final SuggestionProvider<CommandSourceStack> featureConfigSuggestionProvider =
            (context, builder) -> SharedSuggestionProvider.suggest(
                    () -> {
                        String featureName = context.getArgument("feature", String.class);

                        Optional<Feature> foundFeature = FeatureRegistry.getFeatureFromString(featureName);

                        if (foundFeature.isEmpty()) return Collections.emptyIterator();

                        return foundFeature.get().getConfigOptions().stream()
                                .map(ConfigHolder::getFieldName)
                                .iterator();
                    },
                    builder);

    public static LiteralArgumentBuilder<CommandSourceStack> buildGetConfigArgBuilder() {
        LiteralArgumentBuilder<CommandSourceStack> getConfigArgBuilder = Commands.literal("get");

        // Feature specified, config option is not, print all configs
        // If a feature and config field is specified, print field specific info
        // /wynntils config get <feature>
        // /wynntils config get <feature> <field>
        getConfigArgBuilder.then(Commands.argument("feature", StringArgumentType.word())
                .suggests(featureSuggestionProvider)
                .then(Commands.argument("config", StringArgumentType.word())
                        .suggests(featureConfigSuggestionProvider)
                        .executes(WynntilsConfigCommand::getSpecificConfigOption))
                .executes(WynntilsConfigCommand::listAllConfigOptions));

        return getConfigArgBuilder;
    }

    public static LiteralArgumentBuilder<CommandSourceStack> buildSetConfigArgBuilder() {
        LiteralArgumentBuilder<CommandSourceStack> setConfigArgBuilder = Commands.literal("set");

        // /wynntils config set <feature> <field> <newValue>
        setConfigArgBuilder.then(Commands.argument("feature", StringArgumentType.word())
                .suggests(featureSuggestionProvider)
                .then(Commands.argument("config", StringArgumentType.word())
                        .suggests(featureConfigSuggestionProvider)
                        .then(Commands.argument("newValue", StringArgumentType.greedyString())
                                .executes(WynntilsConfigCommand::changeFeatureConfig))));

        return setConfigArgBuilder;
    }

    public static LiteralArgumentBuilder<CommandSourceStack> buildResetConfigArgBuilder() {
        LiteralArgumentBuilder<CommandSourceStack> resetConfigArgBuilder = Commands.literal("reset");

        // Feature specified, config option is not, reset all configs
        // If a feature and config field is specified, reset specific field
        // /wynntils config reset <feature>
        // /wynntils config reset <feature> <field>
        resetConfigArgBuilder.then(Commands.argument("feature", StringArgumentType.word())
                .suggests(featureSuggestionProvider)
                .then(Commands.argument("config", StringArgumentType.word())
                        .suggests(featureConfigSuggestionProvider)
                        .executes(WynntilsConfigCommand::resetConfigOption))
                .executes(WynntilsConfigCommand::resetAllConfigOptions));

        return resetConfigArgBuilder;
    }

    private static int getSpecificConfigOption(CommandContext<CommandSourceStack> context) {
        String featureName = context.getArgument("feature", String.class);
        String configName = context.getArgument("config", String.class);

        Optional<Feature> featureOptional = FeatureRegistry.getFeatureFromString(featureName);

        if (featureOptional.isEmpty()) {
            context.getSource().sendFailure(new TextComponent("Feature not found!").withStyle(ChatFormatting.RED));
            return 0;
        }

        Feature feature = featureOptional.get();
        Optional<ConfigHolder> configOptional = feature.getConfigOptionFromString(configName);

        if (configOptional.isEmpty()) {
            context.getSource().sendFailure(new TextComponent("Config not found!").withStyle(ChatFormatting.RED));
            return 0;
        }

        ConfigHolder config = configOptional.get();
        Object value = config.getValue();

        String valueString = value == null ? "Couldn't get value." : value.toString();
        String configTypeString = "(" + config.getType().getSimpleName() + ")";

        String longFeatureName = Arrays.stream(CaseFormat.LOWER_CAMEL
                        .to(CaseFormat.LOWER_UNDERSCORE, featureName)
                        .split("_"))
                .map(StringUtils::capitalize)
                .collect(Collectors.joining(" "));

        MutableComponent response = new TextComponent(longFeatureName + "\n").withStyle(ChatFormatting.YELLOW);
        response.append(new TextComponent("Config option: ")
                .withStyle(ChatFormatting.WHITE)
                .append(new TextComponent(config.getMetadata().displayName()).withStyle(ChatFormatting.YELLOW))
                .append("\n"));

        response.append(new TextComponent("Value: ")
                        .withStyle(ChatFormatting.WHITE)
                        .append(new TextComponent(configTypeString))
                        .append(new TextComponent(": "))
                        .append(new TextComponent(valueString).withStyle(ChatFormatting.GREEN)))
                .append("\n");
        response.append(new TextComponent("Subcategory: ")
                        .withStyle(ChatFormatting.WHITE)
                        .append(new TextComponent(config.getMetadata().subcategory())))
                .append("\n");
        response.append(new TextComponent("Description: ")
                        .withStyle(ChatFormatting.WHITE)
                        .append(new TextComponent(config.getMetadata().description())))
                .append("\n");

        context.getSource().sendSuccess(response, false);

        return 1;
    }

    private static int listAllConfigOptions(CommandContext<CommandSourceStack> context) {
        String featureName = context.getArgument("feature", String.class);

        Optional<Feature> featureOptional = FeatureRegistry.getFeatureFromString(featureName);

        if (featureOptional.isEmpty()) {
            context.getSource().sendFailure(new TextComponent("Feature not found!").withStyle(ChatFormatting.RED));
            return 0;
        }

        Feature feature = featureOptional.get();

        MutableComponent response = new TextComponent(featureName)
                .withStyle(ChatFormatting.YELLOW)
                .append(new TextComponent("'s config options:\n").withStyle(ChatFormatting.WHITE));

        for (ConfigHolder config : feature.getVisibleConfigOptions()) {
            Object value = config.getValue();

            String configNameString = config.getMetadata().displayName();
            String configTypeString = " (" + config.getType().getSimpleName() + ")";
            String valueString = value == null ? "Couldn't get value." : value.toString();

            MutableComponent current = new TextComponent("\n - ")
                    .withStyle(ChatFormatting.GRAY)
                    .append(new TextComponent(configNameString)
                            .withStyle(style -> style.withHoverEvent(new HoverEvent(
                                    HoverEvent.Action.SHOW_TEXT,
                                    new TextComponent("Description: "
                                                    + config.getMetadata().description())
                                            .withStyle(ChatFormatting.LIGHT_PURPLE))))
                            .withStyle(ChatFormatting.YELLOW)
                            .append(new TextComponent(configTypeString).withStyle(ChatFormatting.WHITE))
                            .append(new TextComponent(": "))
                            .append(new TextComponent(valueString)
                                    .withStyle(ChatFormatting.GREEN)
                                    .withStyle(style -> style.withHoverEvent(new HoverEvent(
                                            HoverEvent.Action.SHOW_TEXT,
                                            new TextComponent("Click here to change this setting."))))));

            current.withStyle(style -> style.withClickEvent(new ClickEvent(
                    ClickEvent.Action.SUGGEST_COMMAND,
                    "/wynntils config set " + featureName + " " + config.getFieldName() + " ")));

            response.append(current);
        }

        context.getSource().sendSuccess(response, false);

        return 1;
    }

    private static int changeFeatureConfig(CommandContext<CommandSourceStack> context) {
        String featureName = context.getArgument("feature", String.class);
        String configName = context.getArgument("config", String.class);

        Optional<Feature> foundFeature = FeatureRegistry.getFeatureFromString(featureName);

        if (foundFeature.isEmpty()) {
            context.getSource().sendFailure(new TextComponent("Feature not found!").withStyle(ChatFormatting.RED));
            return 0;
        }

        Feature actualFeature = foundFeature.get();
        Optional<ConfigHolder> configOptional = actualFeature.getConfigOptionFromString(configName);

        if (configOptional.isEmpty()) {
            context.getSource().sendFailure(new TextComponent("Config not found!").withStyle(ChatFormatting.RED));
            return 0;
        }

        ConfigHolder config = configOptional.get();

        String newValue = context.getArgument("newValue", String.class);
        Object parsedValue = config.tryParseStringValue(newValue);

        if (parsedValue == null) {
            context.getSource()
                    .sendFailure(new TextComponent("Failed to parse the inputted value to the correct type!")
                            .withStyle(ChatFormatting.RED));
            return 0;
        }

        Object oldValue = config.getValue();

        if (oldValue.equals(parsedValue)) {
            context.getSource()
                    .sendFailure(new TextComponent("The new value is the same as the current setting.")
                            .withStyle(ChatFormatting.RED));
            return 0;
        }

        if (!config.setValue(parsedValue)) {
            context.getSource()
                    .sendFailure(new TextComponent("Failed to set config field!").withStyle(ChatFormatting.RED));
            return 0;
        }

        ConfigManager.saveConfig();

        context.getSource()
                .sendSuccess(
                        new TextComponent("Successfully set ")
                                .withStyle(ChatFormatting.GREEN)
                                .append(new TextComponent(config.getMetadata().displayName())
                                        .withStyle(ChatFormatting.UNDERLINE)
                                        .withStyle(ChatFormatting.YELLOW))
                                .append(new TextComponent(" from ").withStyle(ChatFormatting.GREEN))
                                .append(new TextComponent(oldValue.toString())
                                        .withStyle(ChatFormatting.BOLD)
                                        .withStyle(ChatFormatting.RED))
                                .append(new TextComponent(" to ").withStyle(ChatFormatting.GREEN))
                                .append(new TextComponent(parsedValue.toString())
                                        .withStyle(ChatFormatting.BOLD)
                                        .withStyle(ChatFormatting.GREEN))
                                .append(new TextComponent(".").withStyle(ChatFormatting.GREEN)),
                        false);

        return 1;
    }

    private static int resetConfigOption(CommandContext<CommandSourceStack> context) {
        String featureName = context.getArgument("feature", String.class);
        String configName = context.getArgument("config", String.class);

        Optional<Feature> featureOptional = FeatureRegistry.getFeatureFromString(featureName);

        if (featureOptional.isEmpty()) {
            context.getSource().sendFailure(new TextComponent("Feature not found!").withStyle(ChatFormatting.RED));
            return 0;
        }

        Feature feature = featureOptional.get();
        Optional<ConfigHolder> configOptional = feature.getConfigOptionFromString(configName);

        if (configOptional.isEmpty()) {
            context.getSource().sendFailure(new TextComponent("Config not found!").withStyle(ChatFormatting.RED));
            return 0;
        }

        ConfigHolder config = configOptional.get();
        config.reset();

        ConfigManager.saveConfig();

        context.getSource()
                .sendSuccess(
                        new TextComponent("Successfully reset ")
                                .withStyle(ChatFormatting.GREEN)
                                .append(new TextComponent(config.getMetadata().displayName())
                                        .withStyle(ChatFormatting.UNDERLINE)
                                        .withStyle(ChatFormatting.YELLOW))
                                .append(new TextComponent(".").withStyle(ChatFormatting.GREEN)),
                        false);
        return 1;
    }

    private static int resetAllConfigOptions(CommandContext<CommandSourceStack> context) {
        String featureName = context.getArgument("feature", String.class);

        Optional<Feature> featureOptional = FeatureRegistry.getFeatureFromString(featureName);

        if (featureOptional.isEmpty()) {
            context.getSource().sendFailure(new TextComponent("Feature not found!").withStyle(ChatFormatting.RED));
            return 0;
        }

        Feature feature = featureOptional.get();
        feature.getVisibleConfigOptions().forEach(ConfigHolder::reset);

        ConfigManager.saveConfig();

        context.getSource()
                .sendSuccess(
                        new TextComponent("Successfully reset ")
                                .withStyle(ChatFormatting.GREEN)
                                .append(new TextComponent(featureName).withStyle(ChatFormatting.YELLOW))
                                .append(new TextComponent("'s config options.").withStyle(ChatFormatting.GREEN)),
                        false);
        return 1;
    }
}
