/*
 * Copyright © Wynntils 2023-2026.
 * This file is released under LGPLv3. See LICENSE for full license details.
 */
package com.wynntils.utils.mc;

import com.wynntils.core.WynntilsMod;
import com.wynntils.core.text.StyledText;
import com.wynntils.core.text.StyledTextPart;
import com.wynntils.core.text.type.StyleType;
import com.wynntils.utils.mc.type.Location;
import com.wynntils.utils.render.FontRenderer;
import com.wynntils.utils.type.IterationDecision;
import com.wynntils.utils.type.Pair;
import java.util.ArrayList;
import java.util.List;
import java.util.Optional;
import java.util.regex.Matcher;
import java.util.regex.Pattern;
import net.minecraft.ChatFormatting;
import net.minecraft.network.chat.ClickEvent;
import net.minecraft.network.chat.Component;
import net.minecraft.network.chat.FontDescription;
import net.minecraft.network.chat.HoverEvent;
import net.minecraft.network.chat.Style;
import net.minecraft.resources.Identifier;

public final class StyledTextUtils {
    private static final Pattern COORDINATE_PATTERN =
            Pattern.compile(".*\\[(-?\\d+)(?:.\\d+)?, ?(-?\\d+)(?:.\\d+)?, ?(-?\\d+)(?:.\\d+)?\\].*");

    // Note: Post Wynncraft 2.1, the hover text is inconsistent, sometimes "'s" is white, sometimes it's gray
    // And yes, the "user" needs to be optional
    public static final Pattern NICKNAME_PATTERN =
            Pattern.compile("§f(?<nick>.+?)(§7)?'s?(§7)? real (user)?name is §f(?<username>.+)");

    private static final String NEWLINE_PREPARATION = "\n";
    private static final Pattern NEWLINE_WRAP_PATTERN = Pattern.compile("\uDAFF\uDFFC\uE001\uDB00\uDC06");
    private static final Pattern WHITESPACES_PATTERN = Pattern.compile("\\s+");

    private static final FontDescription CHAT_PREFIX_FONT =
            new FontDescription.Resource(Identifier.parse("wynntils:prefix"));
    private static final Style CHAT_PREFIX_STYLE =
            Style.EMPTY.withFont(CHAT_PREFIX_FONT).withColor(ChatFormatting.DARK_GREEN);
    private static final String CHAT_PREFIX_FIRST_LINE = "\uDAFF\uDFFC\uE100\uDAFF\uDFFF\uE002\uDAFF\uDFFE ";
    private static final String CHAT_PREFIX_LINE_PREFIX = "\uDAFF\uDFFC\uE001\uDB00\uDC06 ";

    public static StyledTextPart createLocationPart(Location location) {
        String locationString = "[%d, %d, %d]".formatted(location.x, location.y, location.z);
        Style style = Style.EMPTY.withColor(ChatFormatting.DARK_AQUA).withUnderlined(true);

        style = style.withClickEvent(
                new ClickEvent.RunCommand("/compass at " + location.x + " " + location.y + " " + location.z));
        style = style.withHoverEvent(
                new HoverEvent.ShowText(Component.translatable("utils.wynntils.component.clickToSetCompass")));

        return new StyledTextPart(locationString, style, null, Style.EMPTY);
    }

    public static Optional<Location> extractLocation(StyledText text) {
        Matcher matcher = text.getMatcher(COORDINATE_PATTERN, StyleType.NONE);
        if (!matcher.matches()) return Optional.empty();

        return Optional.of(new Location(
                Integer.parseInt(matcher.group(1)),
                Integer.parseInt(matcher.group(2)),
                Integer.parseInt(matcher.group(3))));
    }

    public static StyledText joinLines(List<StyledText> lines) {
        String description = WHITESPACES_PATTERN
                .matcher(String.join(
                        " ", lines.stream().map(StyledText::getString).toList()))
                .replaceAll(" ")
                .trim();
        return StyledText.fromString(description);
    }

    public static int getLineCount(StyledText styledText) {
        List<StyledText> lines = List.of(styledText.split("\n", true));
        int lineCount = lines.size();
        return lineCount;
    }

    /**
     * Removes all newlines from the given styled text.
     */
    public static StyledText joinAllLines(StyledText styledText) {
        return styledText.replaceAll("\n", "");
    }

    public static List<StyledText> stripEventsAndLinks(List<StyledText> lines) {
        List<StyledText> linesWithoutEvents = lines.stream()
                .map(s -> s.getString())
                .map(str -> StyledText.fromString(str))
                .toList();
        return linesWithoutEvents;
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
            String partString = part.getString(null, StyleType.NONE);

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
                    String lastPartWithoutNewline = lastWrappedPart.getString(null, StyleType.NONE);
                    lastPartWithoutNewline = lastPartWithoutNewline.substring(
                            0, lastPartWithoutNewline.length() - NEWLINE_PREPARATION.length());

                    // Check if the style of the current part matches with the one we added last time,
                    // if so, we merge them
                    if (!newParts.isEmpty()
                            && newParts.getLast().getPartStyle().equals(lastWrappedPart.getPartStyle())) {
                        StyledTextPart lastPart = newParts.removeLast();
                        lastPartWithoutNewline = lastPart.getString(null, StyleType.NONE) + lastPartWithoutNewline;

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

                    // After a soft wrap, we need to insert a space
                    if (!newParts.getLast().getString(null, StyleType.NONE).equals(" ")) {
                        newParts.add(new StyledTextPart(
                                " ", lastWrappedPart.getPartStyle().getStyle(), null, null));
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
                partString = lastPart.getString(null, StyleType.NONE) + partString;

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

        // If we inserted a space after a soft wrap but never saw a following part, drop that trailing space
        if (!newParts.isEmpty()
                && newParts.getLast().getString(null, StyleType.NONE).equals(" ")) {
            newParts.removeLast();
        }

        return StyledText.fromParts(newParts);
    }

    /**
     * @param styledText The styled text to soft-wrap
     * @param maxWidth The width at wich soft-wrap begins
     * @return The soft-wraped styled text
     */
    public static StyledText softWrap(StyledText styledText, int maxWidth) {
        List<StyledTextPart> newParts = new ArrayList<>();

        int currentWidth = 0;

        StyledText[] lines = styledText.split("\n");
        for (int i = 0; i < lines.length; i++) {
            StyledText line = lines[i];

            for (StyledTextPart part : line) {
                int displayLength = FontRenderer.getInstance().getFont().width(part.getComponent());

                // If the currentPart is short enough to fit into the current line
                // add the whole part at once
                if (currentWidth + displayLength <= maxWidth) {
                    currentWidth += displayLength;
                    newParts.add(part);
                    continue;
                }

                // If the currentPart is to long and we need to wrap - try to add every word seperate
                currentWidth = wrapPartAsWords(part, maxWidth, currentWidth, newParts);
            }

            // If this is not the last line
            if (i < lines.length - 1) {
                newParts.add(new StyledTextPart(NEWLINE_PREPARATION, Style.EMPTY, null, null));
                currentWidth = 0;
            }
        }

        return StyledText.fromParts(newParts);
    }

    private static int wrapPartAsWords(
            StyledTextPart part,
            int maxWidth,
            int lastWidth,
            List<StyledTextPart> newParts
    ) {
        int currentWidth = lastWidth;

        StyledText[] split = StyledText.fromPart(part).split("\s+");
        for (StyledText splitText : split) {
            if (splitText.getPartCount() == 0) {
                // If orignal part started with space
                currentWidth += FontRenderer.getInstance().getFont().width(" ");
                newParts.add(new StyledTextPart(" ", Style.EMPTY, null, null));
                continue;
            }

            if (splitText.getPartCount() > 1) {
                // this should never happen since we split only a single StyledTextPart
                WynntilsMod.warn("Unexpected multiPart StyledText - " + splitText);
                continue;
            }

            StyledTextPart splitPart = splitText.getFirstPart();
            int splitDisplayLength = FontRenderer.getInstance()
                    .getFont()
                    .width(splitPart.getComponent().append(" "));

            // If word fits into the current line - append it including a space
            if (currentWidth + splitDisplayLength <= maxWidth) {
                currentWidth += splitDisplayLength;
                newParts.add(splitPart);
                newParts.add(
                        new StyledTextPart(" ", splitPart.getPartStyle().getStyle(), null, null));
                continue;
            }

            if (currentWidth <= 0) {
                currentWidth = wrapPartAsChars(splitPart, maxWidth, currentWidth, newParts);
                newParts.add(
                        new StyledTextPart(" ", splitPart.getPartStyle().getStyle(), null, null));
                currentWidth += FontRenderer.getInstance().getFont().width(" ");
                continue;
            }

            newParts.add(new StyledTextPart(
                    NEWLINE_PREPARATION, splitPart.getPartStyle().getStyle(), null, null));
            newParts.add(splitPart);
            newParts.add(
                    new StyledTextPart(" ", splitPart.getPartStyle().getStyle(), null, null));
            currentWidth = splitDisplayLength;
        }

        return currentWidth;
    }

    private static int wrapPartAsChars(
            StyledTextPart part,
            int maxWidth,
            int lastWidth,
            List<StyledTextPart> newParts
    ) {
        int currentWidth = lastWidth;

        StyledText[] split = StyledText.fromPart(part).split("");
        for (StyledText splitText : split) {
            if (splitText.getPartCount() != 1) {
                // this should never happen since we split only a single StyledTextPart
                WynntilsMod.warn("Unexpected multiPart StyledText - " + splitText);
                continue;
            }

            StyledTextPart splitPart = splitText.getFirstPart();
            int splitDisplayLength = FontRenderer.getInstance()
                    .getFont()
                    .width(splitPart.getComponent());

            // If char fits into the current line - append it including a space
            if (currentWidth + splitDisplayLength <= maxWidth) {
                currentWidth += splitDisplayLength;
                newParts.add(splitPart);
                continue;
            }

            newParts.add(new StyledTextPart(
                    NEWLINE_PREPARATION, splitPart.getPartStyle().getStyle(), null, null));
            newParts.add(splitPart);
            currentWidth = splitDisplayLength;
        }

        return currentWidth;
    }

    /**
     * Adds a prefix like Wynn gives to all messages post 2.1.
     *
     * @param styledText The styled text to add a prefix to
     * @return The text with a prefix
     */
    public static StyledText addWynntilsPrefix(StyledText styledText) {
        int maxWidth = McUtils.getChatWidth()
                - FontRenderer.getInstance()
                        .getFont()
                        .width(Component.literal(CHAT_PREFIX_FIRST_LINE).setStyle(CHAT_PREFIX_STYLE));

        StyledText text = softWrap(styledText, maxWidth)
                .prependPart(new StyledTextPart(CHAT_PREFIX_FIRST_LINE, CHAT_PREFIX_STYLE, null, null));

        return text.iterate((current, changes) -> {
            if (current.endsWith("\n")) {
                changes.add(new StyledTextPart(CHAT_PREFIX_LINE_PREFIX, CHAT_PREFIX_STYLE, null, null));
            }

            return IterationDecision.CONTINUE;
        });
    }

    /**
     * @param styledText Entire StyledText containing the nickname segment
     * @return [username, nick] pair if a nickname is found, null otherwise
     */
    public static Pair<String, String> extractNameAndNick(Iterable<StyledTextPart> styledText) {
        for (StyledTextPart part : styledText) {
            HoverEvent hoverEvent = part.getPartStyle().getStyle().getHoverEvent();

            if (hoverEvent == null || hoverEvent.action() != HoverEvent.Action.SHOW_TEXT) {
                continue;
            }

            HoverEvent.ShowText showTextHoverEvent = (HoverEvent.ShowText) hoverEvent;
            StyledText[] partTexts =
                    StyledText.fromComponent(showTextHoverEvent.value()).split("\n");

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
