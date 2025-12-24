/*
 * Copyright © Wynntils 2022-2025.
 * This file is released under LGPLv3. See LICENSE for full license details.
 */
package com.wynntils.features.redirects;

import com.wynntils.core.components.Managers;
import com.wynntils.core.consumers.features.Feature;
import com.wynntils.core.consumers.features.ProfileDefault;
import com.wynntils.core.persisted.config.Category;
import com.wynntils.core.persisted.config.ConfigCategory;
import com.wynntils.core.persisted.config.ConfigProfile;
import com.wynntils.core.text.StyledText;
import com.wynntils.handlers.chat.event.ChatMessageEvent;
import com.wynntils.mc.event.SubtitleSetTextEvent;
import java.util.regex.Matcher;
import java.util.regex.Pattern;
import net.neoforged.bus.api.SubscribeEvent;
import org.apache.commons.lang3.StringUtils;

@ConfigCategory(Category.REDIRECTS)
public class TerritoryMessageRedirectFeature extends Feature {
    private static final Pattern TERRITORY_MESSAGE_PATTERN = Pattern.compile("§7\\[You are now (\\S+) (.+)\\]");

    public TerritoryMessageRedirectFeature() {
        super(new ProfileDefault.Builder()
                .disableFor(ConfigProfile.NEW_PLAYER, ConfigProfile.BLANK_SLATE)
                .build());
    }

    // Handles the subtitle text event.
    @SubscribeEvent
    public void onSubtitleSetText(SubtitleSetTextEvent event) {
        StyledText styledText = StyledText.fromComponent(event.getComponent());
        Matcher matcher = styledText.getMatcher(TERRITORY_MESSAGE_PATTERN);
        if (!matcher.matches()) return;

        event.setCanceled(true);

        String rawDirection = matcher.group(1);
        String rawTerritoryName = matcher.group(2);
        String directionalArrow;

        switch (rawDirection) {
            case "entering" -> directionalArrow = "→";
            case "leaving" -> directionalArrow = "←";
            default -> {
                return;
            }
        }

        // Want to account for weird stuff like "the Forgery" and make it "The Forgery"
        // for the sake of our brief message (looks odd otherwise).
        String territoryName = StringUtils.capitalize(rawTerritoryName);

        StyledText enteringMessage = StyledText.fromString(String.format("§7%s %s", directionalArrow, territoryName));
        Managers.Notification.queueMessage(enteringMessage);
    }

    // Handles the chat log message event, we don't want a duplicate so just cancel the event and rely on the subtitle
    // text event.
    @SubscribeEvent
    public void onChat(ChatMessageEvent.Match event) {
        if (event.getMessage().getMatcher(TERRITORY_MESSAGE_PATTERN).matches()) {
            event.cancelChat();
        }
    }
}
