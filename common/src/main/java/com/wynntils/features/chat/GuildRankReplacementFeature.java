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
import com.wynntils.core.text.PartStyle;
import com.wynntils.core.text.StyledText;
import com.wynntils.core.text.StyledTextPart;
import com.wynntils.handlers.chat.event.ChatMessageReceivedEvent;
import com.wynntils.utils.type.IterationDecision;
import java.util.regex.Matcher;
import java.util.regex.Pattern;
import net.minecraft.ChatFormatting;
import net.minecraft.network.chat.Style;
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

        StyledText styledText = e.getStyledText();

        Matcher m = styledText.getMatcher(RANK_STARS_PATTERN);
        if (!m.find()) return;

        if (rankType.get() == RankType.NONE) {

            StyledText modified = styledText.iterate((part, changes) -> {
                if (part.getString(null, PartStyle.StyleType.NONE).contains("★")) {
                    changes.remove(part);
                }
                return IterationDecision.CONTINUE;
            });
            e.setMessage(modified.getComponent());
            return;
        }

        // rankType == RankType.NAME
        StyledText modified = styledText.iterateBackwards((part, changes) -> {
            int stars = (int) part.getString(null, PartStyle.StyleType.NONE)
                    .chars()
                    .filter(c -> c == '★')
                    .count();
            if (stars > 0) { // This is the part to replace
                changes.remove(part);

                String rankName = "";
                switch (stars) {
                    case 1 -> rankName = "Recruiter ";
                    case 2 -> rankName = "Captain ";
                    case 3 -> rankName = "Strategist ";
                    case 4 -> rankName = "Chief ";
                    case 5 -> rankName = "Owner ";
                }
                changes.add(new StyledTextPart(rankName, part.getPartStyle().getStyle(), null, Style.EMPTY));
                return IterationDecision.BREAK;
            }

            if (part.getString(null, PartStyle.StyleType.NONE).contains("[")) {
                // We've already gone through the rest of the string and did not find any stars, this must be a recruit
                // Since the [ and the username are the same part, we need to do some surgery:
                // Find username from the original part, then remove it, and add our new reconstructed one
                String username = part.getString(null, PartStyle.StyleType.NONE).substring(1);
                changes.remove(part);
                changes.add(new StyledTextPart(
                        "[" + ChatFormatting.AQUA + "Recruit " + ChatFormatting.DARK_AQUA + username,
                        part.getPartStyle().getStyle(),
                        null,
                        Style.EMPTY));
                return IterationDecision.BREAK;
            }

            return IterationDecision.CONTINUE;
        });
        e.setMessage(modified.getComponent());
    }

    private enum RankType {
        STARS,
        NAME,
        NONE
    }
}
