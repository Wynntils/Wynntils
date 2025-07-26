/*
 * Copyright © Wynntils 2022-2025.
 * This file is released under LGPLv3. See LICENSE for full license details.
 */
package com.wynntils.utils.mc;

import com.wynntils.core.text.StyledText;
import com.wynntils.core.text.StyledTextPart;
import com.wynntils.utils.MathUtils;
import com.wynntils.utils.colors.CommonColors;
import com.wynntils.utils.wynn.WynnUtils;
import java.awt.Color;
import java.util.ArrayList;
import java.util.List;
import java.util.Optional;
import java.util.regex.Pattern;
import java.util.stream.Collectors;
import net.minecraft.ChatFormatting;
import net.minecraft.network.chat.Component;
import net.minecraft.network.chat.FormattedText;
import net.minecraft.network.chat.MutableComponent;
import net.minecraft.network.chat.Style;
import net.minecraft.resources.ResourceLocation;

public final class ComponentUtils {
    private static final Pattern COLOR_CODE_PATTERN = Pattern.compile("(§[1-9a-f])+");
    private static final int RAINBOW_CYCLE_TIME = 5000;
    private static final Pattern NEWLINE_PATTERN = Pattern.compile("\n");

    private static final ResourceLocation PILL_FONT = ResourceLocation.withDefaultNamespace("banner/pill");
    private static final Style BACKGROUND_STYLE =
            Style.EMPTY.withFont(PILL_FONT).withColor(ChatFormatting.AQUA);
    private static final Style FOREGROUND_STYLE =
            Style.EMPTY.withFont(PILL_FONT).withColor(ChatFormatting.BLACK);
    private static final Component WYNNTILS_BACKGROUND_PILL = Component.literal(
                    "\uE060\uDAFF\uDFFF\uE046\uDAFF\uDFFF\uE048\uDAFF\uDFFF\uE03D\uDAFF\uDFFF\uE03D\uDAFF\uDFFF\uE043\uDAFF\uDFFF\uE038\uDAFF\uDFFF\uE03B\uDAFF\uDFFF\uE042\uDAFF\uDFFF\uE062\uDAFF\uDFD0")
            .withStyle(BACKGROUND_STYLE);
    private static final Component WYNNTILS_FOREGROUND_PILL = Component.literal(
                    "\uE016\uE018\uE00D\uE00D\uE013\uE008\uE00B\uE012\uDB00\uDC02 ")
            .withStyle(FOREGROUND_STYLE);

    public static List<Component> stripDuplicateBlank(List<Component> lore) {
        List<Component> newLore = new ArrayList<>(); // Used to remove duplicate blank lines

        boolean oldBlank = false;
        int index = 0;

        for (; index < lore.size(); index++) { // find first blank
            Component loreLine = lore.get(index);

            String line = WynnUtils.normalizeBadString(loreLine.getString());

            newLore.add(loreLine);

            if (line.isEmpty()) {
                oldBlank = true;
                break;
            }
        }

        if (!oldBlank) {
            return newLore;
        }

        for (; index < lore.size(); index++) {
            Component loreLine = lore.get(index);

            String line = WynnUtils.normalizeBadString(loreLine.getString());

            if (oldBlank && line.isEmpty()) {
                continue; // both blank - do not add; oldBlank still true
            }

            oldBlank = line.isEmpty();

            newLore.add(loreLine);
        }

        return newLore;
    }

    public static Style getLastPartCodes(StyledText lastPart) {
        StyledTextPart lastTextPart = lastPart.getLastPart();

        if (lastTextPart == null) return Style.EMPTY;

        return lastTextPart.getPartStyle().getStyle();
    }

    public static Component formattedTextToComponent(FormattedText formattedText) {
        MutableComponent component = Component.literal("");
        formattedText.visit(
                (style, string) -> {
                    component.append(Component.literal(string).withStyle(style));
                    return Optional.empty();
                },
                Style.EMPTY);

        return component;
    }

    public static int getOptimalTooltipWidth(List<Component> tooltips, int screenWidth, int mouseX) {
        int tooltipWidth =
                tooltips.stream().mapToInt(McUtils.mc().font::width).max().orElse(0);
        int tooltipX = mouseX + 12;
        if (tooltipX + tooltipWidth + 4 > screenWidth) {
            tooltipX = mouseX - 16 - tooltipWidth;
            if (tooltipX < 4) // if the tooltip doesn't fit on the screen
            {
                if (mouseX > screenWidth / 2) tooltipWidth = mouseX - 12 - 8;
                else tooltipWidth = screenWidth - 16 - mouseX;
            }
        }
        return tooltipWidth;
    }

    public static List<Component> wrapTooltips(List<Component> tooltips, int maxWidth) {
        return tooltips.stream()
                .flatMap(x -> splitComponent(x, maxWidth).stream())
                .toList();
    }

    public static List<Component> splitComponent(Component component, int maxWidth) {
        List<Component> split = McUtils.mc().font.getSplitter().splitLines(component, maxWidth, Style.EMPTY).stream()
                .map(ComponentUtils::formattedTextToComponent)
                .collect(Collectors.toList());
        if (split.isEmpty()) split.add(Component.literal(""));
        return split;
    }

    public static MutableComponent makeRainbowStyle(String name, boolean useShader) {
        if (useShader) {
            return Component.literal(name)
                    .withColor(CommonColors.RAINBOW.asInt())
                    .withStyle(ChatFormatting.BOLD);
        }

        MutableComponent newName = Component.literal("").withStyle(ChatFormatting.BOLD);

        // This math was originally based off Avaritia code.
        // Special thanks for Morpheus1101 and SpitefulFox
        // Avaritia Repo: https://github.com/Morpheus1101/Avaritia
        int time = (int) (System.currentTimeMillis() % RAINBOW_CYCLE_TIME);
        for (int i = 0; i < name.length(); i++) {
            int hue = (time + i * RAINBOW_CYCLE_TIME / 7) % RAINBOW_CYCLE_TIME;
            Style color = Style.EMPTY
                    .withColor(Color.HSBtoRGB((hue / (float) RAINBOW_CYCLE_TIME), 0.8F, 0.8F))
                    .withItalic(false);

            newName.append(Component.literal(String.valueOf(name.charAt(i))).setStyle(color));
        }

        return newName;
    }

    public static MutableComponent makeObfuscated(
            String name, float obfuscationChanceStart, float obfuscationChanceEnd) {
        MutableComponent newName = Component.literal("").withStyle(ChatFormatting.BOLD, ChatFormatting.DARK_RED);

        boolean obfuscated = Math.random() < obfuscationChanceStart;
        StringBuilder current = new StringBuilder();

        for (int i = 0; i < name.length() - 1; i++) {
            current.append(name.charAt(i));

            float chance =
                    MathUtils.lerp(obfuscationChanceStart, obfuscationChanceEnd, (i + 1) / (float) (name.length() - 1));

            if (!obfuscated && Math.random() < chance) {
                newName.append(Component.literal(current.toString()));
                current = new StringBuilder();

                obfuscated = true;
            } else if (obfuscated && Math.random() > chance) {
                newName.append(Component.literal(current.toString()).withStyle(Style.EMPTY.withObfuscated(true)));
                current = new StringBuilder();

                obfuscated = false;
            }
        }

        current.append(name.charAt(name.length() - 1));

        if (obfuscated) {
            newName.append(Component.literal(current.toString()).withStyle(Style.EMPTY.withObfuscated(true)));
        } else {
            newName.append(Component.literal(current.toString()));
        }
        return newName;
    }

    public static MutableComponent addWynntilsPillHeader(Component component) {
        return Component.empty()
                .append(WYNNTILS_BACKGROUND_PILL)
                .append(WYNNTILS_FOREGROUND_PILL)
                .append(component);
    }
}
