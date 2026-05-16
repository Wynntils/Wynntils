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
import com.wynntils.utils.mc.StyledTextUtils;
import com.wynntils.utils.mc.type.Location;
import com.wynntils.utils.wynn.LocationUtils;
import net.minecraft.network.chat.Style;
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
            String trailingText = replacementStartIndex == startIndex
                            && replacementEndIndex == endIndex
                            && shouldInsertComma(message, endIndex)
                    ? ","
                    : "";

            // Replace only the logical coordinate range; ChatHandler rebuilds the chat wrap after edit events.
            replacements.add(
                    new LocationReplacement(replacementStartIndex, replacementEndIndex, trailingText, location.get()));
        }

        if (replacements == null) return styledText;

        return applyLocationReplacements(unwrappedText, replacements);
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
