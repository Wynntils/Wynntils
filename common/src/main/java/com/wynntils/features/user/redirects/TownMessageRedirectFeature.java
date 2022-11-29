package com.wynntils.features.user.redirects;

import com.wynntils.core.config.Config;
import com.wynntils.core.notifications.NotificationManager;
import com.wynntils.mc.event.SubtitleSetTextEvent;
import com.wynntils.mc.utils.ComponentUtils;
import java.util.regex.Matcher;
import java.util.regex.Pattern;
import net.minecraftforge.eventbus.api.EventPriority;
import net.minecraftforge.eventbus.api.SubscribeEvent;
import com.wynntils.wynn.event.ChatMessageReceivedEvent;
import com.wynntils.core.features.UserFeature;

public class TownMessageRedirectFeature extends UserFeature {
    private static final Pattern TOWN_MESSAGE_PATTERN = Pattern.compile("§7\\[You are now (\\S+) (.+)\\]");

    @Config
    public boolean redirectTownMessages = true;

    // Handles the subtitle text event.
    @SubscribeEvent
    public void onSubtitleSetText(SubtitleSetTextEvent event) {
        if (!redirectTownMessages) {
            return;
        }
        String codedString = ComponentUtils.getCoded(event.getComponent());
        Matcher matcher = TOWN_MESSAGE_PATTERN.matcher(codedString);
        if (matcher.matches())
            event.setCanceled(true);
            String direction = matcher.group(1);
            String townName = matcher.group(2);

            switch (direction) {
                case "entering":
                    String enteringMessage = String.format("\uFFEBNow Entering §l%s", townName);
                    NotificationManager.queueMessage(enteringMessage);
                    return;
                case "leaving":
                    String leavingMessage = String.format("Now Leaving §l%s§r\uFFEB", townName);
                    NotificationManager.queueMessage(leavingMessage);
                    return;}
            }

    // Handles the chat log message event, we don't want a duplicate so just cancel the event and rely on the subtitle text event.
    @SubscribeEvent(priority = EventPriority.LOW)
    public void onChat(ChatMessageReceivedEvent event)
    {
        if (!redirectTownMessages) {
            return;
        }
        String codedString = ComponentUtils.getCoded(event.getMessage());
        Matcher matcher = TOWN_MESSAGE_PATTERN.matcher(codedString);
        if (matcher.matches())
            event.setCanceled(true);
    }
}
