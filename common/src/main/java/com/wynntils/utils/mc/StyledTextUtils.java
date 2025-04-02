/*
 * Copyright © Wynntils 2023-2025.
 * This file is released under LGPLv3. See LICENSE for full license details.
 */
package com.wynntils.utils.mc;

import com.wynntils.core.WynntilsMod;
import com.wynntils.core.text.PartStyle;
import com.wynntils.core.text.StyledText;
import com.wynntils.core.text.StyledTextPart;
import com.wynntils.utils.colors.CustomColor;
import com.wynntils.utils.mc.type.Location;
import com.wynntils.utils.type.Pair;
import java.util.ArrayDeque;
import java.util.ArrayList;
import java.util.Deque;
import java.util.List;
import java.util.Optional;
import java.util.regex.Matcher;
import java.util.regex.Pattern;
import net.minecraft.ChatFormatting;
import net.minecraft.network.chat.ClickEvent;
import net.minecraft.network.chat.Component;
import net.minecraft.network.chat.HoverEvent;
import net.minecraft.network.chat.Style;
import net.minecraft.resources.ResourceLocation;

public final class StyledTextUtils {
    private static final Pattern COORDINATE_PATTERN =
            Pattern.compile(".*\\[(-?\\d+)(?:.\\d+)?, ?(-?\\d+)(?:.\\d+)?, ?(-?\\d+)(?:.\\d+)?\\].*");

    // Note: Post Wynncraft 2.1, the hover text is inconsistent, sometimes "'s" is white, sometimes it's gray
    public static final Pattern NICKNAME_PATTERN =
            Pattern.compile("§f(?<nick>.+?)(§7)?'s?(§7)? real username is §f(?<username>.+)");

    private static final Pattern CLASS_PATTERN = Pattern.compile("class=['\"](.*?)['\"]");
    private static final Pattern STYLE_PATTERN = Pattern.compile("style=['\"](.*?)['\"]");
    private static final Pattern TOKEN_PATTERN = Pattern.compile("(<span[^>]*>|</span>|[^<]+)");

    private static final String NEWLINE_PREPARATION = "\n";
    private static final Pattern NEWLINE_WRAP_PATTERN = Pattern.compile("\uDAFF\uDFFC\uE001\uDB00\uDC06");

    public static StyledTextPart createLocationPart(Location location) {
        String locationString = "[%d, %d, %d]".formatted(location.x, location.y, location.z);
        Style style = Style.EMPTY.withColor(ChatFormatting.DARK_AQUA).withUnderlined(true);

        style = style.withClickEvent(new ClickEvent(
                ClickEvent.Action.RUN_COMMAND, "/compass at " + location.x + " " + location.y + " " + location.z));
        style = style.withHoverEvent(new HoverEvent(
                HoverEvent.Action.SHOW_TEXT, Component.translatable("utils.wynntils.component.clickToSetCompass")));

        return new StyledTextPart(locationString, style, null, Style.EMPTY);
    }

    public static Optional<Location> extractLocation(StyledText text) {
        Matcher matcher = text.getMatcher(COORDINATE_PATTERN, PartStyle.StyleType.NONE);
        if (!matcher.matches()) return Optional.empty();

        return Optional.of(new Location(
                Integer.parseInt(matcher.group(1)),
                Integer.parseInt(matcher.group(2)),
                Integer.parseInt(matcher.group(3))));
    }

    public static StyledText joinLines(List<StyledText> lines) {
        String description = String.join(
                        " ", lines.stream().map(StyledText::getString).toList())
                .replaceAll("\\s+", " ")
                .trim();
        return StyledText.fromString(description);
    }

    /**
     * This method is used by {@link com.wynntils.handlers.chat.ChatHandler} to split multi-line messages.
     * Multi-line messages use new lines not just to split the multi-line message, but also to format the message.
     * If a part is only a new line, we know it's a new message, but if the new line is in the middle of a part,
     * we know it's a new line in the same message, which we don't want to split.
     *
     * @param styledText The styled text to split
     * @return A list of styled texts, each representing a line
     */
    public static List<StyledText> splitInLines(StyledText styledText) {
        List<StyledText> newLines = new ArrayList<>();

        List<StyledTextPart> parts = new ArrayList<>();
        for (StyledTextPart part : styledText) {
            String partString = part.getString(null, PartStyle.StyleType.NONE);

            if (partString.equals("\n")) {
                newLines.add(StyledText.fromParts(parts));
                parts.clear();
            } else {
                parts.add(part);
            }
        }

        if (!parts.isEmpty()) {
            newLines.add(StyledText.fromParts(parts));
        }

        return newLines;
    }

    /**
     * This will parse a HTML String into StyledText.
     * The rules for this parsing are based on the API documentation https://docs.wynncraft.com/docs/#api-markup-parser
     *
     * @param htmlString The HTML string to parse
     * @return The input parsed into StyledText
     */
    public static StyledText parseHtml(String htmlString) {
        if (htmlString.trim().equals("</br>")) {
            return StyledText.EMPTY;
        }

        List<StyledTextPart> parts = new ArrayList<>();
        Deque<Style> styles = new ArrayDeque<>();
        styles.push(Style.EMPTY);
        Style currentStyle = Style.EMPTY;

        StringBuilder plainText = new StringBuilder();

        Matcher matcher = TOKEN_PATTERN.matcher(htmlString);

        while (matcher.find()) {
            String token = matcher.group();

            if (token.startsWith("<span")) {
                if (!plainText.isEmpty()) {
                    parts.add(new StyledTextPart(plainText.toString(), currentStyle, null, Style.EMPTY));
                    plainText = new StringBuilder();
                }

                Style parentStyle = styles.peek();
                Style newStyle = Style.EMPTY.withColor(parentStyle.getColor());

                Matcher classMatcher = CLASS_PATTERN.matcher(token);
                if (classMatcher.find()) {
                    String fontName = classMatcher.group(1).trim();
                    ResourceLocation font =
                            switch (fontName) {
                                case "font-ascii", "font-default" -> ResourceLocation.withDefaultNamespace("default");
                                case "font-common" -> ResourceLocation.withDefaultNamespace("common");
                                case "font-five" -> ResourceLocation.withDefaultNamespace("language/five");
                                case "font-wynnic" -> ResourceLocation.withDefaultNamespace("language/wynnic");
                                case "font-high_gavelian" ->
                                    ResourceLocation.withDefaultNamespace("language/high_gavelian");
                                default -> {
                                    WynntilsMod.warn("Unknown font in HTML parsing: " + fontName);
                                    yield ResourceLocation.withDefaultNamespace("default");
                                }
                            };
                    newStyle = newStyle.withFont(font);
                }

                Matcher styleMatcher = STYLE_PATTERN.matcher(token);
                if (styleMatcher.find()) {
                    String style = styleMatcher.group(1);

                    for (String styleEntry : style.split(";")) {
                        styleEntry = styleEntry.trim();

                        if (styleEntry.isEmpty()) continue;

                        String[] stylePair = styleEntry.split(":");
                        String styleKey = stylePair[0].trim();
                        String styleValue = stylePair[1].trim();

                        switch (styleKey) {
                            case "text-decoration" -> {
                                if (styleValue.equals("underline")) {
                                    newStyle = newStyle.withUnderlined(true);
                                } else if (styleValue.equals("line-through")) {
                                    newStyle = newStyle.withStrikethrough(true);
                                } else {
                                    WynntilsMod.warn("Unknown text decoration type in HTML parsing: " + styleValue);
                                }
                            }
                            case "font-style" -> {
                                if (styleValue.equals("italic")) {
                                    newStyle = newStyle.withItalic(true);
                                } else {
                                    WynntilsMod.warn("Unknown font style type in HTML parsing: " + styleValue);
                                }
                            }
                            case "font-weight" -> {
                                if (styleValue.equals("bolder")) {
                                    newStyle = newStyle.withBold(true);
                                } else {
                                    WynntilsMod.warn("Unknown font weight type in HTML parsing: " + styleValue);
                                }
                            }
                            case "color" -> {
                                newStyle = newStyle.withColor(
                                        CustomColor.fromHexString(styleValue).asInt());
                            }
                            case "margin-left" -> {
                                if (styleValue.equals("7.5px")) {
                                    // FIXME: Currently a guess, there are no items with this margin but it is
                                    // a possible value according to the docs
                                    plainText.append("À");
                                } else if (styleValue.equals("20px")) {
                                    plainText.append("ÀÀÀÀ");
                                } else {
                                    WynntilsMod.warn("Unexpected margin-left in HTML parsing: " + styleValue);
                                }
                            }
                            default -> {
                                WynntilsMod.warn("Unknown style type in HTML parsing: " + styleKey);
                            }
                        }
                    }
                }

                styles.push(newStyle);
                currentStyle = newStyle;
            } else if (token.startsWith("</span>")) {
                if (!plainText.isEmpty()) {
                    parts.add(new StyledTextPart(plainText.toString(), currentStyle, null, Style.EMPTY));
                    plainText = new StringBuilder();
                }

                if (!styles.isEmpty()) {
                    styles.pop();
                }

                Style parentStyle = styles.isEmpty() ? Style.EMPTY : styles.peek();
                if (!parentStyle.equals(currentStyle)) {
                    parts.add(new StyledTextPart("", parentStyle, null, Style.EMPTY));
                }

                currentStyle = parentStyle;
            } else {
                plainText.append(token);
            }
        }

        if (!plainText.isEmpty()) {
            parts.add(new StyledTextPart(plainText.toString(), currentStyle, null, Style.EMPTY));
        }

        return StyledText.fromParts(parts);
    }

    /**
     * Removes all newlines from the given styled text.
     */
    public static StyledText joinAllLines(StyledText styledText) {
        return styledText.replaceAll("\n", "");
    }

    /**
     * Removes the soft-wrap Wynn gives to all messages post 2.1.
     *
     * @param styledText The styled text to unwrap
     * @return The unwrapped styled text
     */
    public static StyledText unwrap(StyledText styledText) {
        List<StyledTextPart> newParts = new ArrayList<>();

        StyledTextPart lastWrappedPart = null;
        boolean expectEmptySpaceAfterWrap = false;
        for (StyledTextPart part : styledText) {
            String partString = part.getString(null, PartStyle.StyleType.NONE);

            // After a wrap, Wynn injects an empty space, which we want to remove
            if (expectEmptySpaceAfterWrap) {
                // There is an edge-case where this space is the whole part, in which case we skip it
                if (partString.equals(" ")) {
                    expectEmptySpaceAfterWrap = false;
                    continue;
                }

                // If the part starts with a space, we remove it
                if (partString.startsWith(" ")) {
                    partString = partString.substring(1);
                    part = new StyledTextPart(partString, part.getPartStyle().getStyle(), null, null);
                    expectEmptySpaceAfterWrap = false;
                } else {
                    // Log the edge-case
                    WynntilsMod.warn("Unexpected edge-case in unwrap: " + part);
                }

                // Continue with execution, a part may have a wrap before and after it
            }

            // If the part ends with a newline, it may be a preparation for a wrap
            if (partString.endsWith(NEWLINE_PREPARATION)) {
                lastWrappedPart = part;
                continue;
            }

            // Confirm whether the last part was wrapped
            if (lastWrappedPart != null) {
                if (NEWLINE_WRAP_PATTERN.matcher(partString).matches()) {
                    // Skip the current part, add back the last part, without the newline
                    String lastPartWithoutNewline = lastWrappedPart.getString(null, PartStyle.StyleType.NONE);
                    lastPartWithoutNewline = lastPartWithoutNewline.substring(
                            0, lastPartWithoutNewline.length() - NEWLINE_PREPARATION.length());

                    // Check if the style of the current part matches with the one we added last time,
                    // if so, we merge them
                    if (!newParts.isEmpty()
                            && newParts.getLast().getPartStyle().equals(lastWrappedPart.getPartStyle())) {
                        StyledTextPart lastPart = newParts.removeLast();
                        lastPartWithoutNewline =
                                lastPart.getString(null, PartStyle.StyleType.NONE) + lastPartWithoutNewline;

                        newParts.add(new StyledTextPart(
                                lastPartWithoutNewline,
                                lastWrappedPart.getPartStyle().getStyle(),
                                null,
                                null));
                    } else {
                        newParts.add(new StyledTextPart(
                                lastPartWithoutNewline,
                                lastWrappedPart.getPartStyle().getStyle(),
                                null,
                                null));
                    }

                    expectEmptySpaceAfterWrap = true;
                } else {
                    // The last part had a newline, but it was not a wrap, so we add it to the new parts
                    newParts.add(lastWrappedPart);
                    newParts.add(part);
                }

                lastWrappedPart = null;
                continue;
            }

            // Check if the style of the current part matches with the one we added last time,
            // if so, we merge them
            if (!newParts.isEmpty() && newParts.getLast().getPartStyle().equals(part.getPartStyle())) {
                StyledTextPart lastPart = newParts.removeLast();
                partString = lastPart.getString(null, PartStyle.StyleType.NONE) + partString;

                newParts.add(new StyledTextPart(partString, part.getPartStyle().getStyle(), null, null));

                continue;
            }

            // If nothing special happened, we just add the part to the new parts
            newParts.add(part);
        }

        // If there is a part that turned out not to be wrapped, we add it to the new parts
        if (lastWrappedPart != null) {
            newParts.add(lastWrappedPart);
        }

        return StyledText.fromParts(newParts);
    }

    /**
     * @param styledText Entire StyledText containing the nickname segment
     * @return [username, nick] pair if a nickname is found, null otherwise
     */
    public static Pair<String, String> extractNameAndNick(Iterable<StyledTextPart> styledText) {
        for (StyledTextPart part : styledText) {
            HoverEvent hoverEvent = part.getPartStyle().getStyle().getHoverEvent();

            if (hoverEvent == null || hoverEvent.getAction() != HoverEvent.Action.SHOW_TEXT) {
                continue;
            }

            StyledText[] partTexts = StyledText.fromComponent(hoverEvent.getValue(HoverEvent.Action.SHOW_TEXT))
                    .split("\n");

            for (StyledText partText : partTexts) {
                Matcher nicknameMatcher = partText.getMatcher(NICKNAME_PATTERN);

                if (nicknameMatcher.matches()) {
                    return new Pair<>(nicknameMatcher.group("username"), nicknameMatcher.group("nick"));
                }
            }
        }

        return null;
    }
}
