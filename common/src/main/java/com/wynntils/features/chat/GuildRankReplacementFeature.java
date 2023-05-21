/*
 * Copyright © Wynntils 2023.
 * This file is released under AGPLv3. See LICENSE for full license details.
 */
package com.wynntils.features.chat;

import com.wynntils.core.config.Category;
import com.wynntils.core.config.Config;
import com.wynntils.core.config.ConfigCategory;
import com.wynntils.core.config.RegisterConfig;
import com.wynntils.core.features.Feature;
import com.wynntils.core.text.StyledText;
import com.wynntils.handlers.chat.event.ChatMessageReceivedEvent;
import java.util.regex.Matcher;
import java.util.regex.Pattern;
import net.minecraft.ChatFormatting;
import net.minecraftforge.eventbus.api.EventPriority;
import net.minecraftforge.eventbus.api.SubscribeEvent;

@ConfigCategory(Category.CHAT)
public class GuildRankReplacementFeature extends Feature {
    @RegisterConfig
    public final Config<RankType> rankType = new Config<>(RankType.STARS);

    // Test suite: https://regexr.com/7e5gr
    private static final Pattern RANK_STARS_PATTERN = Pattern.compile("(§3\\[(?:§b)?)(★{0,5})((?:§3)?.{1,16}])");

    @SubscribeEvent(priority = EventPriority.HIGH)
    public void onChatMessageReceived(ChatMessageReceivedEvent e) {
        if (rankType.get() == RankType.STARS) return; // default wynn behavior is stars

        Matcher m = RANK_STARS_PATTERN.matcher(
                StyledText.fromComponent(e.getMessage()).getString());
        if (!m.find()) return;

        if (rankType.get() == RankType.NONE) {
            e.setMessage(StyledText.fromString(m.replaceFirst("$1")).getComponent());
            return;
        }

        String rank = m.group(2);
        int stars = (int) rank.chars().filter(c -> c == '★').count();
        String rankName = "";
        switch (stars) {
            case 0 -> rankName = ChatFormatting.AQUA + "Recruit "
                    + ChatFormatting.DARK_AQUA; // Recruit doesn't come with color codes
            case 1 -> rankName = "Recruiter ";
            case 2 -> rankName = "Captain ";
            case 3 -> rankName = "Strategist ";
            case 4 -> rankName = "Chief ";
            case 5 -> rankName = "Owner ";
        }
        e.setMessage(
                StyledText.fromString(m.replaceFirst("$1" + rankName + "$3")).getComponent());
    }

    private enum RankType {
        STARS,
        NAME,
        NONE
    }
}
