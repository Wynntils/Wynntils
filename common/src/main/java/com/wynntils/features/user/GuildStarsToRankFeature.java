/*
 * Copyright © Wynntils 2022.
 * This file is released under AGPLv3. See LICENSE for full license details.
 */
package com.wynntils.features.user;

import com.wynntils.core.features.UserFeature;
import com.wynntils.core.features.properties.StartDisabled;
import com.wynntils.wynn.event.ChatMessageReceivedEvent;
import com.wynntils.wynn.objects.GuildRank;
import java.util.regex.Matcher;
import java.util.regex.Pattern;
import net.minecraft.network.chat.MutableComponent;
import net.minecraft.network.chat.TextComponent;
import net.minecraftforge.eventbus.api.SubscribeEvent;

@StartDisabled
public class GuildStarsToRankFeature extends UserFeature {

    private static final Pattern guildMessage =
            Pattern.compile("(?:§r)?§3\\[(?:§b)?(★{0,5})?(?:§3)?(.*)§3\\](?:§b)? .*");

    @SubscribeEvent
    public void onChat(ChatMessageReceivedEvent event) {
        String message = event.getOriginalCodedMessage();
        Matcher match = guildMessage.matcher(message);
        if (!(match.matches()) || match.group(2).equals("INFO")) return;
        MutableComponent newMessage = new TextComponent(String.format(
                "§b[§b%s §3%s§b] §b",
                GuildRank.fromStars(match.group(1).length()).getRank(), match.group(2)));
        event.getMessage().getSiblings().forEach((sibling) -> {
            String text = sibling.getString().substring(1);
            newMessage.append(new TextComponent(text).withStyle(sibling.getStyle()));
        });
        event.setMessage(newMessage);
    }
}
