/*
 * Copyright © Wynntils 2022.
 * This file is released under AGPLv3. See LICENSE for full license details.
 */
package com.wynntils.features.user;

import com.wynntils.core.features.UserFeature;
import com.wynntils.core.notifications.NotificationManager;
import com.wynntils.mc.utils.McUtils;
import com.wynntils.wynn.event.ChatMessageReceivedEvent;
import com.wynntils.wynn.objects.GuildRank;
import java.util.ArrayList;
import java.util.List;
import java.util.logging.Logger;
import java.util.regex.Matcher;
import java.util.regex.Pattern;

import net.minecraft.network.chat.*;
import net.minecraftforge.eventbus.api.SubscribeEvent;

public class GuildStarsToRankFeature extends UserFeature {

    private static final Pattern guildMessage = Pattern.compile("(?:§r)?§3\\[(?:§b)?(★{0,5})?(?:§3)?(.*)§3\\](?:§b)? (.*)");

    @SubscribeEvent
    public void onChat(ChatMessageReceivedEvent event) {
        String message = event.getOriginalCodedMessage();
        System.out.println(message);
        Matcher match = guildMessage.matcher(message);
        if (!(match.matches())) return;
        MutableComponent newMessage = new TextComponent(String.format(
                "§3b§b%s §3%s§b] §b%s",
                GuildRank.fromStars(match.group(1).length()).getRank(), match.group(2), match.group(3)));

        event.setMessage(newMessage);
    }

}
