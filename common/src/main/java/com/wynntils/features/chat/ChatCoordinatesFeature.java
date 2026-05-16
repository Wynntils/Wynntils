/*
 * Copyright © Wynntils 2022-2026.
 * This file is released under LGPLv3. See LICENSE for full license details.
 */
package com.wynntils.features.chat;

import com.wynntils.core.components.Models;
import com.wynntils.core.consumers.features.Feature;
import com.wynntils.core.consumers.features.ProfileDefault;
import com.wynntils.core.persisted.config.Category;
import com.wynntils.core.persisted.config.ConfigCategory;
import com.wynntils.core.persisted.config.ConfigProfile;
import com.wynntils.core.text.StyledText;
import com.wynntils.core.text.StyledTextPart;
import com.wynntils.core.text.type.StyleType;
import com.wynntils.handlers.chat.event.ChatMessageEvent;
import com.wynntils.utils.mc.McUtils;
import com.wynntils.utils.mc.StyledTextUtils;
import com.wynntils.utils.mc.type.Location;
import com.wynntils.utils.render.FontRenderer;
import com.wynntils.utils.wynn.LocationUtils;
import net.minecraft.network.chat.Component;
import net.minecraft.network.chat.FontDescription;
import net.minecraft.network.chat.Style;
import net.minecraft.resources.Identifier;
import net.neoforged.bus.api.EventPriority;
import net.neoforged.bus.api.SubscribeEvent;

import java.util.ArrayList;
import java.util.List;
import java.util.Optional;
import java.util.regex.Matcher;
import java.util.regex.Pattern;

@ConfigCategory(Category.CHAT)
public class ChatCoordinatesFeature extends Feature {
    private static final Pattern END_OF_HEADER_PATTERN = Pattern.compile(".*:\\s?");
    private static final String CHAT_PREFIX_LINE_PREFIX = "\uDAFF\uDFFC\uE001\uDB00\uDC06";
    private static final Identifier CHAT_PREFIX_FONT_ID = Identifier.fromNamespaceAndPath("minecraft", "chat/prefix");

    public ChatCoordinatesFeature() {
        super(new ProfileDefault.Builder()
                .enabledFor(ConfigProfile.DEFAULT, ConfigProfile.NEW_PLAYER, ConfigProfile.LITE)
                .build());
    }

    @SubscribeEvent(priority = EventPriority.HIGH)
    public void onChatReceived(ChatMessageEvent.Edit e) {
        if (!Models.WorldState.onWorld()) return;

        StyledText message = e.getMessage();

        StyledText modified = getStyledTextWithCoordinatesInserted(message);

        // No changes were made, there were no coordinates.
        if (message.equals(modified)) return;

        e.setMessage(modified);
    }

    /**
     * Detects coordinates in the plain unwrapped chat string, then rebuilds the styled chat text only when a
     * coordinate was found.
     * <p>
     * Example: {@code wind chime is at -1437,124,-1256 on the side} becomes a clickable
     * {@code [-1437, 124, -1256]} location part, while the surrounding message keeps its original style.
     */
    private static StyledText getStyledTextWithCoordinatesInserted(StyledText styledText) {
        // Cheap filter before unwrapping or regex work; a coordinate cannot exist without a digit.
        if (!containsDigit(styledText.getString(StyleType.NONE))) return styledText;

        StyledText unwrappedText = StyledTextUtils.unwrap(styledText);
        String message = unwrappedText.getString(StyleType.NONE);
        if (message.isEmpty()) return styledText;

        Matcher matcher = LocationUtils.strictCoordinateMatcher(message);
        List<LocationReplacement> replacements = null;
        int headerEndIndex = getHeaderEndIndex(unwrappedText);

        while (matcher.find()) {
            Optional<Location> location = LocationUtils.parseFromString(matcher.group(1));

            if (location.isEmpty()) {
                continue;
            }

            int startIndex = matcher.start(1);
            int endIndex = matcher.end(1);
            if (startIndex < headerEndIndex) {
                continue;
            }

            if (replacements == null) {
                replacements = new ArrayList<>();
            }

            int replacementStartIndex = getBracketAwareStartIndex(message, startIndex);
            int replacementEndIndex = getBracketAwareEndIndex(message, endIndex);
            String trailingText =
                    replacementStartIndex == startIndex && replacementEndIndex == endIndex && shouldInsertComma(message, endIndex)
                            ? ","
                            : "";

            // Replace only the logical coordinate range, then rebuild the chat wrap once at the end.
            replacements.add(new LocationReplacement(
                    replacementStartIndex, replacementEndIndex, trailingText, location.get()));
        }

        if (replacements == null) return styledText;

        StyledText modifiedText = applyLocationReplacements(unwrappedText, replacements);

        return wrapAndAddLinePrefixes(modifiedText, getPrefixStyle(styledText), hasChatPrefixFont(styledText));
    }

    /**
     * Replaces matched coordinate ranges with {@link StyledTextUtils#createLocationPart(Location)} while streaming the
     * original parts once. This keeps the original prefix/name/message styling instead of rebuilding the whole message
     * from one flat color.
     */
    private static StyledText applyLocationReplacements(StyledText styledText, List<LocationReplacement> replacements) {
        List<StyledTextPart> parts = new ArrayList<>();
        int globalIndex = 0;
        int replacementIndex = 0;

        // Stream original parts once, only coordinate ranges are replaced with the normalized location part.
        for (StyledTextPart part : styledText) {
            String partText = part.getString(null, StyleType.NONE);
            int partStartIndex = globalIndex;
            int partEndIndex = partStartIndex + partText.length();

            while (replacementIndex < replacements.size()
                    && replacements.get(replacementIndex).endIndex <= partStartIndex) {
                replacementIndex++;
            }

            if (replacementIndex >= replacements.size()
                    || replacements.get(replacementIndex).startIndex >= partEndIndex) {
                parts.add(part);
                globalIndex = partEndIndex;
                continue;
            }

            int partOffset = 0;
            while (replacementIndex < replacements.size()) {
                LocationReplacement replacement = replacements.get(replacementIndex);
                if (replacement.startIndex >= partEndIndex) break;

                int replacementStartInPart = Math.max(replacement.startIndex - partStartIndex, 0);
                int replacementEndInPart = Math.min(replacement.endIndex - partStartIndex, partText.length());

                addOriginalPart(parts, part, partText.substring(partOffset, replacementStartInPart));

                if (replacement.startIndex >= partStartIndex) {
                    parts.add(StyledTextUtils.createLocationPart(replacement.location));
                    addOriginalPart(parts, part, replacement.trailingText);
                }

                partOffset = replacementEndInPart;
                if (replacement.endIndex <= partEndIndex) {
                    replacementIndex++;
                } else {
                    break;
                }
            }

            addOriginalPart(parts, part, partText.substring(partOffset));
            globalIndex = partEndIndex;
        }

        return StyledText.fromParts(parts);
    }

    /**
     * Adds plain text using an original part's style and merges adjacent parts with the same style.
     * <p>
     * This avoids creating one component per word/space during replacement.
     */
    private static void addOriginalPart(List<StyledTextPart> parts, StyledTextPart originalPart, String text) {
        addStyledPart(parts, text, originalPart.getPartStyle().getStyle());
    }

    /**
     * Finds the end of the chat header, such as the player name plus colon, so anything in the prefix are ignored.
     */
    private static int getHeaderEndIndex(StyledText styledText) {
        int currentIndex = 0;

        for (StyledTextPart part : styledText) {
            String partText = part.getString(null, StyleType.NONE);
            currentIndex += partText.length();

            if (END_OF_HEADER_PATTERN.matcher(partText).matches()) {
                return currentIndex;
            }
        }

        return 0;
    }

    /**
     * Uses the original first part's style for generated continuation bars so the bar color follows the chat line.
     */
    private static Style getPrefixStyle(StyledText styledText) {
        StyledTextPart firstPart = styledText.getFirstPart();
        return firstPart == null ? Style.EMPTY : firstPart.getPartStyle().getStyle();
    }

    /**
     * Wraps the rebuilt message to the current Minecraft chat width. The Wynn continuation bar is only added for
     * messages whose first part uses Minecraft's chat prefix font.
     * <p>
     * This is intentionally local instead of using {@link StyledTextUtils#softWrap(StyledText, int)} because the
     * coordinate replacement path needs exact control over spaces: a generated continuation line should start with the
     * bar and then the next token, not a copied leading space.
     */
    private static StyledText wrapAndAddLinePrefixes(StyledText styledText, Style prefixStyle, boolean addLinePrefix) {
        WrappedTextBuilder builder = new WrappedTextBuilder(McUtils.getChatWidth(), prefixStyle, addLinePrefix);

        for (StyledTextPart part : styledText) {
            String partText = part.getString(null, StyleType.NONE);
            Style partStyle = part.getPartStyle().getStyle();

            if (part.getPartStyle().getClickEvent() != null) {
                // Keep the coordinate clickable, but allow it to wrap inside [x, y, z] like normal chat text.
                builder.appendClickableText(partText, partStyle);
                continue;
            }

            int index = 0;
            while (index < partText.length()) {
                char character = partText.charAt(index);

                if (Character.isWhitespace(character)) {
                    builder.appendWhitespace(character, partStyle);
                    index++;
                    continue;
                }

                int tokenStartIndex = index;
                while (index < partText.length() && !Character.isWhitespace(partText.charAt(index))) {
                    index++;
                }

                builder.appendText(partText.substring(tokenStartIndex, index), partStyle);
            }
        }

        return builder.build();
    }

    /**
     * Adds a styled part and merges it with the previous part when possible.
     */
    private static void addStyledPart(List<StyledTextPart> parts, String text, Style style) {
        if (text.isEmpty()) return;

        if (!parts.isEmpty() && parts.getLast().getPartStyle().getStyle().equals(style)) {
            StyledTextPart lastPart = parts.removeLast();
            String mergedText = lastPart.getString(null, StyleType.NONE) + text;
            parts.add(new StyledTextPart(mergedText, style, null, null));
            return;
        }

        parts.add(new StyledTextPart(text, style, null, null));
    }

    /**
     * Measures rendered chat width with the same style that will be used for the final component.
     */
    private static int getTextWidth(String text, Style style) {
        return FontRenderer.getInstance().getFont().width(Component.literal(text).withStyle(style));
    }

    /**
     * Only messages that start with Minecraft's chat prefix font should receive the continuation bar. Other message
     * layouts already own their left-side rendering, so generated wraps only get a newline and one padding space.
     */
    private static boolean hasChatPrefixFont(StyledText styledText) {
        StyledTextPart firstPart = styledText.getFirstPart();
        if (firstPart == null) return false;

        return firstPart.getPartStyle().getFont() instanceof FontDescription.Resource resource
                && CHAT_PREFIX_FONT_ID.equals(resource.id());
    }

    /**
     * Fast pre-check before regex/unwrapping work; valid coordinates always contain at least one digit.
     */
    private static boolean containsDigit(String message) {
        for (int i = 0; i < message.length(); i++) {
            if (Character.isDigit(message.charAt(i))) return true;
        }

        return false;
    }

    /**
     * If the coordinate starts after an opening bracket, include the bracket and any inner whitespace in the replacement.
     */
    private static int getBracketAwareStartIndex(String message, int coordinateStartIndex) {
        int index = coordinateStartIndex - 1;
        while (index >= 0 && Character.isWhitespace(message.charAt(index))) {
            index--;
        }

        return index >= 0 && message.charAt(index) == '[' ? index : coordinateStartIndex;
    }

    /**
     * If the coordinate ends before a closing bracket, include any inner whitespace and the bracket in the replacement.
     */
    private static int getBracketAwareEndIndex(String message, int coordinateEndIndex) {
        int index = coordinateEndIndex;
        while (index < message.length() && Character.isWhitespace(message.charAt(index))) {
            index++;
        }

        return index < message.length() && message.charAt(index) == ']' ? index + 1 : coordinateEndIndex;
    }

    /**
     * Small stateful builder for wrapping already-rebuilt text.
     * <p>
     * It keeps three pieces of state that are important for this feature:
     * line width for chat wrapping, whether the line already has real text, and whether one normal space is pending
     * between two tokens. The continuation bar and its padding space contribute width but do not count as real text,
     * which prevents copied message spaces from appearing after the bar.
     */
    private static class WrappedTextBuilder {
        private final List<StyledTextPart> parts = new ArrayList<>();
        private final int chatWidth;
        private final Style prefixStyle;
        private final int prefixWidth;
        private final boolean addLinePrefix;

        private int currentLineWidth = 0;
        private boolean lineHasText = false;
        private boolean pendingSpace = false;
        private Style pendingSpaceStyle = Style.EMPTY;

        private WrappedTextBuilder(int chatWidth, Style prefixStyle, boolean addLinePrefix) {
            this.chatWidth = chatWidth;
            this.prefixStyle = prefixStyle;
            this.prefixWidth = addLinePrefix ? getTextWidth(CHAT_PREFIX_LINE_PREFIX, prefixStyle) : 0;
            this.addLinePrefix = addLinePrefix;
        }

        /**
         * Appends normal, non-clickable text. If a normal space is pending, the space is merged into this same style.
         */
        private void appendText(String text, Style style) {
            if (text.isEmpty()) return;

            String textToAdd = lineHasText && pendingSpace ? " " + text : text;
            int textWidth = getTextWidth(textToAdd, style);

            // If this token starts a wrapped line, drop the pending space so the bar touches the text cleanly.
            if (lineHasText && currentLineWidth + textWidth > chatWidth) {
                appendLinePrefix();
                textToAdd = text;
                textWidth = getTextWidth(textToAdd, style);
            }

            addStyledPart(parts, textToAdd, style);
            currentLineWidth += textWidth;
            lineHasText = true;
            pendingSpace = false;
        }

        /**
         * Appends a clickable coordinate. The separator before {@code [} stays non-clickable, but spaces inside
         * {@code [x, y, z]} use the clickable style and may be dropped at a generated wrap.
         */
        private void appendClickableText(String text, Style style) {
            if (text.isEmpty()) return;

            if (pendingSpace && lineHasText) {
                String firstToken = text.substring(0, getFirstWhitespaceIndex(text));
                int spaceWidth = getTextWidth(" ", pendingSpaceStyle);
                int firstTokenWidth = getTextWidth(firstToken, style);

                // Keep the separator before a location in the surrounding chat style, not in the clickable style.
                if (currentLineWidth + spaceWidth + firstTokenWidth > chatWidth) {
                    appendLinePrefix();
                } else {
                    addStyledPart(parts, " ", pendingSpaceStyle);
                    currentLineWidth += spaceWidth;
                    pendingSpace = false;
                }
            }

            int index = 0;
            while (index < text.length()) {
                if (Character.isWhitespace(text.charAt(index))) {
                    pendingSpace = lineHasText;
                    index++;
                    continue;
                }

                int tokenStartIndex = index;
                while (index < text.length() && !Character.isWhitespace(text.charAt(index))) {
                    index++;
                }

                appendText(text.substring(tokenStartIndex, index), style);
            }
        }

        private int getFirstWhitespaceIndex(String text) {
            for (int i = 0; i < text.length(); i++) {
                if (Character.isWhitespace(text.charAt(i))) return i;
            }

            return text.length();
        }

        /**
         * Collapses any run of normal whitespace into one pending space. Newlines immediately emit a continuation bar.
         */
        private void appendWhitespace(char character, Style style) {
            if (character == '\n') {
                appendLinePrefix();
                return;
            }

            // Wynn unwrap can leave duplicate spaces around a soft wrap; only keep one between real tokens.
            if (lineHasText) {
                pendingSpace = true;
                pendingSpaceStyle = style;
            }
        }

        /**
         * Adds a generated newline plus the Wynn continuation bar and one padding space. Pending message spaces are
         * discarded so each continuation line gets exactly one space before its first token.
         */
        private void appendLinePrefix() {
            addStyledPart(parts, "\n", Style.EMPTY);
            if (addLinePrefix) {
                parts.add(new StyledTextPart(CHAT_PREFIX_LINE_PREFIX, prefixStyle, null, null));
            }
            addStyledPart(parts, " ", Style.EMPTY);
            currentLineWidth = prefixWidth + getTextWidth(" ", Style.EMPTY);
            lineHasText = false;
            pendingSpace = false;
            pendingSpaceStyle = Style.EMPTY;
        }

        /**
         * Creates the final styled text once all wrapping and prefix insertion is done.
         */
        private StyledText build() {
            return StyledText.fromParts(parts);
        }
    }

    /**
     * Adds a comma after a normalized coordinate only when the original message had plain whitespace after it.
     * <p>
     * Example: {@code at -1437,124,-1256 on the side} becomes {@code at [-1437, 124, -1256], on the side}.
     */
    private static boolean shouldInsertComma(String message, int coordinateEndIndex) {
        for (int i = coordinateEndIndex; i < message.length(); i++) {
            char character = message.charAt(i);
            if (character == ',' || character == '.' || character == ';' || character == ':') return false;
            if (Character.isWhitespace(character)) return true;
            return false;
        }

        return false;
    }

    private static class LocationReplacement {
        private final int startIndex;
        private final int endIndex;
        private final String trailingText;
        private final Location location;

        private LocationReplacement(int startIndex, int endIndex, String trailingText, Location location) {
            this.startIndex = startIndex;
            this.endIndex = endIndex;
            this.trailingText = trailingText;
            this.location = location;
        }
    }
}
