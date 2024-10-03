/*
 * Copyright © Wynntils 2022-2024.
 * This file is released under LGPLv3. See LICENSE for full license details.
 */
package com.wynntils.utils.mc;

import com.wynntils.core.WynntilsMod;
import com.wynntils.core.components.Models;
import com.wynntils.core.text.StyledText;
import com.wynntils.core.text.StyledTextPart;
import com.wynntils.models.items.WynnItem;
import com.wynntils.models.items.encoding.type.EncodingSettings;
import com.wynntils.utils.EncodedByteBuffer;
import com.wynntils.utils.MathUtils;
import com.wynntils.utils.type.ErrorOr;
import com.wynntils.utils.wynn.WynnUtils;
import java.awt.Color;
import java.util.ArrayList;
import java.util.List;
import java.util.Optional;
import java.util.regex.Pattern;
import java.util.stream.Collectors;
import net.minecraft.ChatFormatting;
import net.minecraft.client.Minecraft;
import net.minecraft.client.gui.components.ChatComponent;
import net.minecraft.network.chat.ClickEvent;
import net.minecraft.network.chat.Component;
import net.minecraft.network.chat.FormattedText;
import net.minecraft.network.chat.HoverEvent;
import net.minecraft.network.chat.MutableComponent;
import net.minecraft.network.chat.Style;
import net.minecraft.resources.ResourceLocation;

public final class ComponentUtils {
    private static final Pattern COLOR_CODE_PATTERN = Pattern.compile("(§[1-9a-f])+");
    private static final int RAINBOW_CYCLE_TIME = 5000;
    private static final Pattern NEWLINE_PATTERN = Pattern.compile("\n");
    private static final ResourceLocation CHAT_BANNER_FONT_LOCATION = ResourceLocation.parse("wynntils:chat");
    private static final Style CHAT_BANNER_STYLE =
            Style.EMPTY.withFont(CHAT_BANNER_FONT_LOCATION).withColor(ChatFormatting.DARK_GREEN);
    private static final String CHAT_BANNER_FIRST_LINE = "\uDAFF\uDFFC\uE100\uDAFF\uDFFF\uE002\uDAFF\uDFFE";
    private static final String CHAT_BANNER_LINE_PREFIX = "\uDAFF\uDFFC\uE001\uDB00\uDC06";

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

    public static MutableComponent makeRainbowStyle(String name) {
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

    /**
     * Adds a Wynntils chat banner to the left side of the provided text in the style of Wynncraft 2.1 chat banners.
     * The formatting of the provided text is preserved.
     *
     * @param formattedText the formatted text to add the Wynntils chat banner to
     * @return a {@code Component} holding the formatted text with the Wynntils chat banner added
     */
    public static Component addWynntilsBanner(Component component) {
        Minecraft mc = Minecraft.getInstance();
        List<FormattedText> lines = mc.font
                .getSplitter()
                .splitLines(
                        component,
                        ChatComponent.getWidth(mc.options.chatWidth().get())
                                - mc.font.width(CHAT_BANNER_LINE_PREFIX + " "),
                        Style.EMPTY);

        MutableComponent output = Component.literal(CHAT_BANNER_FIRST_LINE)
                .withStyle(CHAT_BANNER_STYLE)
                .append("\n");

        for (int i = 0; i < lines.size(); i++) {
            output.append(Component.literal(CHAT_BANNER_LINE_PREFIX))
                    .append(Component.literal(" ")
                            .withStyle(Style.EMPTY.withFont(ResourceLocation.withDefaultNamespace("default")))
                            .append(formattedTextToComponent(lines.get(i))));
            if (i != lines.size() - 1) {
                output.append("\n");
            }
        }

        return output;
    }

    /**
     * Creates a new {@link Component} from a given {@link WynnItem} that shows the item's stats when hovered. A
     * {@code Component} with a hoverable error message will be returned if item encoding fails.
     *
     * @param wynnItem the {@code WynnItem} to create a {@code Component} for
     * @return a {@code Component} with the given item's stats, or a {@code Component} with an error message if
     * encoding fails
     */
    public static Component createItemChatComponent(WynnItem wynnItem) {
        EncodingSettings encodingSettings = new EncodingSettings(
                Models.ItemEncoding.extendedIdentificationEncoding.get(), Models.ItemEncoding.shareItemName.get());
        ErrorOr<EncodedByteBuffer> errorOrEncodedByteBuffer =
                Models.ItemEncoding.encodeItem(wynnItem, encodingSettings);
        if (errorOrEncodedByteBuffer.hasError()) {
            WynntilsMod.error("Failed to encode item: " + errorOrEncodedByteBuffer.getError());
            return Component.translatable("feature.wynntils.chatItem.chatItemError")
                    .withStyle(Style.EMPTY
                            .withColor(ChatFormatting.RED)
                            .withUnderlined(true)
                            .withClickEvent(new ClickEvent(
                                    ClickEvent.Action.COPY_TO_CLIPBOARD, errorOrEncodedByteBuffer.getError()))
                            .withHoverEvent(new HoverEvent(
                                    HoverEvent.Action.SHOW_TEXT,
                                    Component.translatable(
                                            "feature.wynntils.chatItem.chatItemErrorEncode",
                                            errorOrEncodedByteBuffer.getError()))));
        }

        if (WynntilsMod.isDevelopmentEnvironment()) {
            WynntilsMod.info("Encoded item: " + errorOrEncodedByteBuffer.getValue());
            WynntilsMod.info("Encoded item UTF-16: "
                    + errorOrEncodedByteBuffer.getValue().toUtf16String());
        }

        return Component.translatable("feature.wynntils.chatItem.chatItemMessage")
                .withStyle(ChatFormatting.DARK_GREEN)
                .withStyle(ChatFormatting.UNDERLINE)
                .withStyle(s -> s.withClickEvent(new ClickEvent(
                        ClickEvent.Action.COPY_TO_CLIPBOARD,
                        errorOrEncodedByteBuffer.getValue().toUtf16String())))
                .withStyle(s -> s.withHoverEvent(new HoverEvent(
                        HoverEvent.Action.SHOW_TEXT,
                        Component.translatable("feature.wynntils.chatItem.chatItemTooltip")
                                .withStyle(ChatFormatting.DARK_AQUA))));
    }
}
