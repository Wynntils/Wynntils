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

        StyledText message = StyledTextUtils.unwrap(e.getMessage());

        StyledText modified = getStyledTextWithCoordinatesInserted(message);

        // No changes were made, there were no coordinates.
        if (message.equals(modified)) return;

        e.setMessage(modified);
    }

    private static StyledText getStyledTextWithCoordinatesInserted(StyledText styledText) {
        String message = styledText.getString(StyleType.NONE);
        int headerEndIndex = getHeaderEndIndex(styledText);

        StringBuilder searchableMessage = new StringBuilder(message.length());
        int[] originalIndexes = new int[message.length()];

        // Match against one plain string so coordinates split across styled chat parts still count as one location.
        for (int i = 0; i < message.length(); i++) {
            char character = message.charAt(i);
            if (character == '\n' || character == '\r') continue;

            originalIndexes[searchableMessage.length()] = i;
            searchableMessage.append(character);
        }

        Matcher matcher = LocationUtils.strictCoordinateMatcher(searchableMessage.toString());
        List<StyledTextPart> parts = null;
        int currentIndex = 0;

        while (matcher.find()) {
            Optional<Location> location = LocationUtils.parseFromString(matcher.group(1));

            if (location.isEmpty()) {
                continue;
            }

            int startIndex = originalIndexes[matcher.start(1)];
            int endIndex = originalIndexes[matcher.end(1) - 1] + 1;

            if (startIndex < headerEndIndex) {
                continue;
            }

            if (startIndex > 0
                    && endIndex < message.length()
                    && message.charAt(startIndex - 1) == '['
                    && message.charAt(endIndex) == ']') {
                startIndex--;
                endIndex++;
            }

            if (parts == null) {
                parts = new ArrayList<>();
            }

            // Rebuild from original StyledText ranges so formatting around the coordinate is preserved.
            addParts(parts, styledText.substring(currentIndex, startIndex, StyleType.NONE));
            parts.add(StyledTextUtils.createLocationPart(location.get()));
            currentIndex = endIndex;
        }

        if (parts == null) return styledText;

        addParts(parts, styledText.substring(currentIndex, StyleType.NONE));

        return StyledText.fromParts(parts);
    }

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

    private static void addParts(List<StyledTextPart> parts, StyledText text) {
        for (StyledTextPart part : text) {
            parts.add(part);
        }
    }
}
