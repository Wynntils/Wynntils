/*
 * Copyright © Wynntils 2022.
 * This file is released under AGPLv3. See LICENSE for full license details.
 */
package com.wynntils.commands;

import com.google.common.base.CaseFormat;
import com.mojang.brigadier.CommandDispatcher;
import com.mojang.brigadier.arguments.StringArgumentType;
import com.mojang.brigadier.builder.LiteralArgumentBuilder;
import com.mojang.brigadier.context.CommandContext;
import com.mojang.brigadier.suggestion.SuggestionProvider;
import com.wynntils.core.Reference;
import com.wynntils.core.commands.CommandBase;
import com.wynntils.core.config.ConfigManager;
import com.wynntils.core.config.properties.Config;
import com.wynntils.core.features.Feature;
import com.wynntils.core.features.FeatureRegistry;
import com.wynntils.core.webapi.WebManager;
import com.wynntils.mc.utils.McUtils;
import com.wynntils.utils.objects.CustomColor;
import java.lang.reflect.Field;
import java.util.Arrays;
import java.util.Collections;
import java.util.List;
import java.util.Optional;
import java.util.Set;
import java.util.stream.Collectors;
import net.minecraft.ChatFormatting;
import net.minecraft.commands.CommandSourceStack;
import net.minecraft.commands.Commands;
import net.minecraft.commands.SharedSuggestionProvider;
import net.minecraft.network.chat.ClickEvent;
import net.minecraft.network.chat.HoverEvent;
import net.minecraft.network.chat.MutableComponent;
import net.minecraft.network.chat.Style;
import net.minecraft.network.chat.TextComponent;
import org.apache.commons.lang3.StringUtils;
import org.apache.commons.lang3.reflect.FieldUtils;

public class WynntilsCommand extends CommandBase {
    @Override
    public void register(CommandDispatcher<CommandSourceStack> dispatcher) {
        LiteralArgumentBuilder<CommandSourceStack> getConfigArgBuilder = Commands.literal("get");
        LiteralArgumentBuilder<CommandSourceStack> setConfigArgBuilder = Commands.literal("set");

        SuggestionProvider<CommandSourceStack> featureSuggestionProvider =
                (context, builder) -> SharedSuggestionProvider.suggest(
                        FeatureRegistry.getFeatures().stream().map(Feature::getShortName), builder);

        SuggestionProvider<CommandSourceStack> featureConfigSuggestionProvider =
                (context, builder) -> SharedSuggestionProvider.suggest(
                        () -> {
                            String featureName = context.getArgument("feature", String.class);

                            Optional<Feature> foundFeature = FeatureRegistry.getFeatureFromString(featureName);

                            if (foundFeature.isEmpty()) return Collections.emptyIterator();

                            return Arrays.stream(foundFeature.get().getConfigFields())
                                    .map(Field::getName)
                                    .iterator();
                        },
                        builder);

        // Feature specified, config option is not, print all configs
        // If a feature and config field is specified, print field specific info
        // /wynntils config get <feature>
        // /wynntils config get <feature> <field>
        getConfigArgBuilder.then(Commands.argument("feature", StringArgumentType.word())
                .suggests(featureSuggestionProvider)
                .then(Commands.argument("config", StringArgumentType.word())
                        .suggests(featureConfigSuggestionProvider)
                        .executes(this::getSpecificConfigOption))
                .executes(this::listAllConfigOptions));

        // /wynntils config set <feature> <field> <newValue>
        setConfigArgBuilder.then(Commands.argument("feature", StringArgumentType.word())
                .suggests(featureSuggestionProvider)
                .then(Commands.argument("config", StringArgumentType.word())
                        .suggests(featureConfigSuggestionProvider)
                        .then(Commands.argument("newValue", StringArgumentType.greedyString())
                                .executes(this::changeFeatureConfig))));

        dispatcher.register(Commands.literal("wynntils")
                .then(Commands.literal("help").executes(this::help))
                .then(Commands.literal("discord").executes(this::discordLink))
                .then(Commands.literal("donate").executes(this::donateLink))
                .then(Commands.literal("reload").executes(this::reload))
                .then(Commands.literal("version").executes(this::version))
                .then(Commands.literal("config").then(getConfigArgBuilder).then(setConfigArgBuilder))
                .then(Commands.literal("feature").then(Commands.literal("list").executes(this::listFeatures)))
                .executes(this::help));
    }

    private int listFeatures(CommandContext<CommandSourceStack> context) {
        Set<Feature> features = FeatureRegistry.getFeatures().stream().collect(Collectors.toUnmodifiableSet());

        MutableComponent response = new TextComponent("Currently registered features:").withStyle(ChatFormatting.AQUA);

        for (Feature feature : features) {
            String longFeatureName = Arrays.stream(CaseFormat.LOWER_CAMEL
                            .to(CaseFormat.LOWER_UNDERSCORE, feature.getShortName())
                            .split("_"))
                    .map(StringUtils::capitalize)
                    .collect(Collectors.joining(" "));
            response.append(new TextComponent("\n - ").withStyle(ChatFormatting.GRAY))
                    .append(new TextComponent(longFeatureName).withStyle(ChatFormatting.YELLOW));
        }

        context.getSource().sendSuccess(response, false);

        return 1;
    }

    private int changeFeatureConfig(CommandContext<CommandSourceStack> context) {
        String featureName = context.getArgument("feature", String.class);
        String configName = context.getArgument("config", String.class);

        Optional<Feature> foundFeature = FeatureRegistry.getFeatureFromString(featureName);

        if (foundFeature.isEmpty()) {
            context.getSource().sendFailure(new TextComponent("Feature not found!").withStyle(ChatFormatting.RED));
            return 0;
        }

        Feature actualFeature = foundFeature.get();
        Optional<Field> configField = Arrays.stream(actualFeature.getConfigFields())
                .filter(field -> field.getAnnotation(Config.class).visible())
                .filter(field -> field.getName().equals(configName))
                .findFirst();

        if (configField.isEmpty()) {
            context.getSource().sendFailure(new TextComponent("Config not found!").withStyle(ChatFormatting.RED));
            return 0;
        }

        Field config = configField.get();

        String newValue = context.getArgument("newValue", String.class);

        Object parsedValue = tryParseNewValue(config.getType(), newValue);

        if (parsedValue == null) {
            context.getSource()
                    .sendFailure(new TextComponent("Failed to parse the inputted value to the correct type!")
                            .withStyle(ChatFormatting.RED));
            return 0;
        }

        Object oldValue;

        try {
            oldValue = FieldUtils.readField(config, actualFeature, true);

            if (oldValue == parsedValue) {
                context.getSource()
                        .sendFailure(new TextComponent("The new value is the same as the current setting.")
                                .withStyle(ChatFormatting.RED));
                return 0;
            }

            FieldUtils.writeField(config, actualFeature, parsedValue, true);
        } catch (IllegalAccessException ignored) {
            context.getSource()
                    .sendFailure(new TextComponent("Failed to set config field!").withStyle(ChatFormatting.RED));
            return 0;
        }

        ConfigManager.saveConfig();

        context.getSource()
                .sendSuccess(
                        new TextComponent("Successfully set ")
                                .withStyle(ChatFormatting.GREEN)
                                .append(new TextComponent(config.getAnnotation(Config.class)
                                                .displayName())
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

    private int getSpecificConfigOption(CommandContext<CommandSourceStack> context) {
        String featureName = context.getArgument("feature", String.class);
        String configName = context.getArgument("config", String.class);

        Optional<Feature> foundFeature = FeatureRegistry.getFeatureFromString(featureName);

        if (foundFeature.isEmpty()) {
            context.getSource().sendFailure(new TextComponent("Feature not found!").withStyle(ChatFormatting.RED));
            return 0;
        }

        Feature actualFeature = foundFeature.get();
        Optional<Field> configField = Arrays.stream(actualFeature.getConfigFields())
                .filter(field -> field.getAnnotation(Config.class).visible())
                .filter(field -> field.getName().equals(configName))
                .findFirst();

        if (configField.isEmpty()) {
            context.getSource().sendFailure(new TextComponent("Config not found!").withStyle(ChatFormatting.RED));
            return 0;
        }

        Field config = configField.get();

        Object value = null;

        try {
            value = FieldUtils.readField(config, actualFeature, true);
        } catch (IllegalAccessException ignored) {
        }

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
                .append(new TextComponent(config.getAnnotation(Config.class).displayName())
                        .withStyle(ChatFormatting.YELLOW))
                .append("\n"));

        response.append(new TextComponent("Value: ")
                        .withStyle(ChatFormatting.WHITE)
                        .append(new TextComponent(configTypeString))
                        .append(new TextComponent(": "))
                        .append(new TextComponent(valueString).withStyle(ChatFormatting.GREEN)))
                .append("\n");
        response.append(new TextComponent("Subcategory: ")
                        .withStyle(ChatFormatting.WHITE)
                        .append(new TextComponent(
                                config.getAnnotation(Config.class).subcategory())))
                .append("\n");
        response.append(new TextComponent("Description: ")
                        .withStyle(ChatFormatting.WHITE)
                        .append(new TextComponent(
                                config.getAnnotation(Config.class).description())))
                .append("\n");

        context.getSource().sendSuccess(response, false);

        return 1;
    }

    private int listAllConfigOptions(CommandContext<CommandSourceStack> context) {
        String featureName = context.getArgument("feature", String.class);

        Optional<Feature> foundFeature = FeatureRegistry.getFeatureFromString(featureName);

        if (foundFeature.isEmpty()) {
            context.getSource().sendFailure(new TextComponent("Feature not found!").withStyle(ChatFormatting.RED));
            return 0;
        }

        Feature actualFeature = foundFeature.get();
        Set<Field> configs = Arrays.stream(actualFeature.getConfigFields())
                .filter(field -> field.getAnnotation(Config.class).visible())
                .collect(Collectors.toUnmodifiableSet());

        MutableComponent response = new TextComponent(featureName)
                .withStyle(ChatFormatting.YELLOW)
                .append(new TextComponent("'s config options:\n").withStyle(ChatFormatting.WHITE));

        for (Field config : configs) {
            Object value = null;
            try {
                value = FieldUtils.readField(config, actualFeature, true);
            } catch (IllegalAccessException ignored) {
            }

            String configNameString = config.getAnnotation(Config.class).displayName();
            String configTypeString = " (" + config.getType().getSimpleName() + ")";
            String valueString = value == null ? "Couldn't get value." : value.toString();

            MutableComponent current = new TextComponent("\n - ")
                    .withStyle(ChatFormatting.GRAY)
                    .append(new TextComponent(configNameString)
                            .withStyle(style -> style.withHoverEvent(new HoverEvent(
                                    HoverEvent.Action.SHOW_TEXT,
                                    new TextComponent("Description: "
                                                    + config.getAnnotation(Config.class)
                                                            .description())
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
                    "/wynntils config set " + featureName + " " + config.getName() + " ")));

            response.append(current);
        }

        context.getSource().sendSuccess(response, false);

        return 1;
    }

    private int version(CommandContext<CommandSourceStack> context) {
        // TODO: Handle if dev env

        MutableComponent buildText;

        if (Reference.VERSION.isEmpty()) {
            buildText = new TextComponent("Unknown Version");
        } else {
            buildText = new TextComponent("Version " + Reference.VERSION);
        }

        buildText.append("\n");

        if (Reference.BUILD_NUMBER == -1) {
            buildText.append(new TextComponent("Unknown Build"));
        } else {
            buildText.append(new TextComponent("Build " + Reference.BUILD_NUMBER));
        }

        buildText.setStyle(buildText.getStyle().withColor(ChatFormatting.YELLOW));

        context.getSource().sendSuccess(buildText, false);
        return 1;
    }

    private int reload(CommandContext<CommandSourceStack> context) {
        for (Feature feature : FeatureRegistry.getFeatures()) { // disable all active features before resetting web
            if (feature.isEnabled()) {
                feature.disable();
            }
        }

        WebManager.reset();

        WebManager.init(); // reloads api urls as well as web manager

        for (Feature feature : FeatureRegistry.getFeatures()) { // re-enable all features which should be
            if (feature.canEnable()) {
                feature.enable();

                if (!feature.isEnabled()) {
                    McUtils.sendMessageToClient(new TextComponent("Failed to reload ")
                            .withStyle(ChatFormatting.GREEN)
                            .append(new TextComponent(feature.getTranslatedName()).withStyle(ChatFormatting.AQUA)));
                } else {
                    McUtils.sendMessageToClient(new TextComponent("Reloaded ")
                            .withStyle(ChatFormatting.GREEN)
                            .append(new TextComponent(feature.getTranslatedName()).withStyle(ChatFormatting.AQUA)));
                }
            }
        }

        context.getSource()
                .sendSuccess(new TextComponent("Finished reloading everything").withStyle(ChatFormatting.GREEN), false);

        return 1;
    }

    private int donateLink(CommandContext<CommandSourceStack> context) {
        MutableComponent c = new TextComponent("You can donate to Wynntils at: ").withStyle(ChatFormatting.AQUA);
        MutableComponent url = new TextComponent("https://www.patreon.com/Wynntils")
                .withStyle(Style.EMPTY
                        .withColor(ChatFormatting.LIGHT_PURPLE)
                        .withUnderlined(true)
                        .withClickEvent(new ClickEvent(ClickEvent.Action.OPEN_URL, "https://www.patreon.com/Wynntils"))
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
        addCommandDescription(text, "-wynntils", List.of(" version"), "This shows the installed Wynntils version.");
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
        String discordInvite =
                WebManager.getApiUrls() == null ? null : WebManager.getApiUrls().get("DiscordInvite");
        MutableComponent link = new TextComponent(discordInvite == null ? "<Wynntils servers are down>" : discordInvite)
                .withStyle(Style.EMPTY.withColor(ChatFormatting.DARK_AQUA));
        if (discordInvite != null) {
            link.setStyle(link.getStyle()
                    .withClickEvent(new ClickEvent(ClickEvent.Action.OPEN_URL, discordInvite))
                    .withHoverEvent(new HoverEvent(
                            HoverEvent.Action.SHOW_TEXT,
                            new TextComponent("Click here to join our Discord" + " server."))));
        }
        context.getSource().sendSuccess(msg.append(link), false);
        return 1;
    }

    private static void addCommandDescription(
            MutableComponent text, String prefix, List<String> suffix, String description) {
        text.append("\n");

        StringBuilder suffixString = new StringBuilder("");

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

    private Object tryParseNewValue(Class<?> typeToParse, String value) {
        if (typeToParse == String.class) {
            return value;
        } else if (typeToParse == Boolean.TYPE) {
            if (value.equalsIgnoreCase("true") || value.equalsIgnoreCase("false")) {
                return Boolean.valueOf(value);
            } else {
                return null;
            }
        } else if (typeToParse == CustomColor.class) {
            CustomColor customColor = CustomColor.fromString(value);
            return customColor == CustomColor.NONE ? null : customColor;
        } else if (typeToParse == Integer.TYPE) {
            try {
                return Integer.parseInt(value);
            } catch (NumberFormatException exception) {
                return null;
            }
        } else if (typeToParse == Float.TYPE) {
            try {
                return Float.parseFloat(value);
            } catch (NumberFormatException exception) {
                return null;
            }
        } else if (typeToParse == Double.TYPE) {
            try {
                return Double.parseDouble(value);
            } catch (NumberFormatException exception) {
                return null;
            }
        }

        return null;
    }
}
