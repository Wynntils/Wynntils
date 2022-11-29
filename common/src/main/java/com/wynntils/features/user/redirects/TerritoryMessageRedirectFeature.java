/*
 * Copyright © Wynntils 2022.
 * This file is released under AGPLv3. See LICENSE for full license details.
 */
package com.wynntils.features.user.redirects;

import com.wynntils.core.config.Config;
import com.wynntils.core.features.UserFeature;
import com.wynntils.core.notifications.NotificationManager;
import com.wynntils.mc.event.SubtitleSetTextEvent;
import com.wynntils.mc.utils.ComponentUtils;
import com.wynntils.wynn.event.ChatMessageReceivedEvent;
import java.util.regex.Matcher;
import java.util.regex.Pattern;
import net.minecraftforge.eventbus.api.EventPriority;
import net.minecraftforge.eventbus.api.SubscribeEvent;

public class TerritoryMessageRedirectFeature extends UserFeature {
    private static final Pattern TERRITORY_MESSAGE_PATTERN = Pattern.compile("§7\\[You are now (\\S+) (.+)\\]");

    @Config
    public boolean redirectTerritoryMessages = true;

    // Handles the subtitle text event.
    @SubscribeEvent
    public void onSubtitleSetText(SubtitleSetTextEvent event) {
        if (!redirectTerritoryMessages) return;

        String codedString = ComponentUtils.getCoded(event.getComponent());
        Matcher matcher = TERRITORY_MESSAGE_PATTERN.matcher(codedString);
        if (matcher.matches()) event.setCanceled(true);
        String direction = matcher.group(1);
        String rawTerritoryName = matcher.group(2);
        // Want to account for weird stuff like "the Forgery" and make it "The Forgery" for the sake of our brief
        // message (looks odd otherwise).
        String territoryName = rawTerritoryName.substring(0, 1).toUpperCase() + rawTerritoryName.substring(1);

        String enteringMessage = String.format("§7Now §o%s§r %s", direction, territoryName);
        NotificationManager.queueMessage(enteringMessage);
    }

    // Handles the chat log message event, we don't want a duplicate so just cancel the event and rely on the subtitle
    // text event.
    @SubscribeEvent(priority = EventPriority.LOW)
    public void onChat(ChatMessageReceivedEvent event) {
        if (!redirectTerritoryMessages) {
            return;
        }
        String codedString = ComponentUtils.getCoded(event.getMessage());
        Matcher matcher = TERRITORY_MESSAGE_PATTERN.matcher(codedString);
        if (matcher.matches()) event.setCanceled(true);
    }
}
