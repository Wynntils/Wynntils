/*
 * Copyright © Wynntils 2022.
 * This file is released under AGPLv3. See LICENSE for full license details.
 */
package com.wynntils.features.user;

import com.wynntils.core.features.UserFeature;
import com.wynntils.wynn.event.ChatMessageReceivedEvent;
import com.wynntils.wynn.objects.GuildRank;
import java.util.ArrayList;
import java.util.List;
import java.util.regex.Matcher;
import java.util.regex.Pattern;
import net.minecraft.network.chat.ClickEvent;
import net.minecraft.network.chat.HoverEvent;
import net.minecraft.network.chat.MutableComponent;
import net.minecraft.network.chat.TextComponent;
import net.minecraftforge.eventbus.api.SubscribeEvent;

public class GuildStarsToRankFeature extends UserFeature {

    private static final Pattern guildMessage = Pattern.compile("§3\\[(?:§b)?(★{0,5})§3(.+)§3\\] (.*)");

    @SubscribeEvent
    public void onChat(ChatMessageReceivedEvent event) {
        String message = event.getCodedMessage();
        Matcher match = guildMessage.matcher(message);
        if (!(match.matches())) return;
        MutableComponent newMessage = new TextComponent(String.format(
                "§3[§b%s §3%s§3] §b",
                GuildRank.fromStars(match.group(1).length()).getRank(), match.group(2)));

        MutableComponent tc = new TextComponent("");
        String sentMessage = match.group(3);
        List<String> urls = extractUrls(sentMessage);
        int previousIndex = 0;
        if (urls.isEmpty()) {
            event.setMessage(newMessage.append(tc.append(sentMessage)));
            return;
        }
        for (String url : urls) {
            int index = sentMessage.indexOf(url);
            tc.append(sentMessage.substring(previousIndex, index));
            TextComponent urlText = new TextComponent(url);
            ClickEvent clickEvent = new ClickEvent(ClickEvent.Action.OPEN_URL, url);
            HoverEvent hoverEvent = new HoverEvent(HoverEvent.Action.SHOW_TEXT, new TextComponent("Open " + url));
            tc.append(urlText.setStyle(
                    urlText.getStyle().withClickEvent(clickEvent).withHoverEvent(hoverEvent)));
            previousIndex = index + 1;
        }

        event.setMessage(newMessage.append(tc));
    }

    private static List<String> extractUrls(String text) {
        List<String> containedUrls = new ArrayList<String>();
        String urlRegex = "((https?|ftp|gopher|telnet|file|http):((//)|(\\\\))+[\\w\\d:#@%/;$()~_?\\+-=\\\\\\.&]*)";
        Pattern pattern = Pattern.compile(urlRegex, Pattern.CASE_INSENSITIVE);
        Matcher urlMatcher = pattern.matcher(text);

        while (urlMatcher.find()) {
            containedUrls.add(text.substring(urlMatcher.start(0), urlMatcher.end(0)));
        }

        return containedUrls;
    }
}
