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
import com.wynntils.core.features.properties.StartDisabled;
import com.wynntils.core.text.PartStyle;
import com.wynntils.core.text.StyledText;
import com.wynntils.core.text.StyledTextPart;
import com.wynntils.handlers.chat.event.ChatMessageReceivedEvent;
import com.wynntils.models.players.GuildModel;
import com.wynntils.utils.type.IterationDecision;
import java.util.regex.Matcher;
import java.util.regex.Pattern;
import net.minecraft.ChatFormatting;
import net.minecraft.network.chat.Style;
import net.minecraftforge.eventbus.api.EventPriority;
import net.minecraftforge.eventbus.api.SubscribeEvent;

@StartDisabled
@ConfigCategory(Category.CHAT)
public class GuildRankReplacementFeature extends Feature {
    private static final char STAR = '★';

    @RegisterConfig
    public final Config<RankType> rankType = new Config<>(RankType.NAME);

    // Test suite: https://regexr.com/7e5gr
    private static final Pattern GUILD_MESSAGE_PATTERN = Pattern.compile("§3\\[(?:§b)?★{0,5}(?:§3)?.{1,16}]§b");

    // Test suite: https://regexr.com/7e66m
    private static final Pattern RECRUIT_USERNAME_PATTERN = Pattern.compile("§3\\[(.{1,16})");

    @SubscribeEvent(priority = EventPriority.HIGH)
    public void onChatMessageReceived(ChatMessageReceivedEvent e) {
        StyledText originalStyledText = e.getStyledText();

        Matcher m = originalStyledText.getMatcher(GUILD_MESSAGE_PATTERN);
        if (!m.find()) return;

        StyledText modified =
                switch (rankType.get()) {
                    case NONE -> modifyByRemovingRank(originalStyledText);
                    case NAME -> modifyByAddingTextRank(originalStyledText);
                };

        if (originalStyledText.equals(modified)) return; // no changes

        e.setMessage(modified.getComponent());
    }

    private StyledText modifyByRemovingRank(StyledText styledText) {
        StyledText modified = styledText.iterate((part, changes) -> {
            if (part.getString(null, PartStyle.StyleType.NONE).contains(String.valueOf(STAR))) {
                changes.remove(part);
                return IterationDecision.BREAK;
            }
            return IterationDecision.CONTINUE;
        });
        return modified;
    }

    private StyledText modifyByAddingTextRank(StyledText styledText) {
        StyledText modified = styledText.iterateBackwards((part, changes) -> {
            int stars = (int) part.getString(null, PartStyle.StyleType.NONE)
                    .chars()
                    .filter(c -> c == STAR)
                    .count();

            if (stars > 0) {
                String rankName = GuildModel.GuildRank.values()[stars].getName();

                changes.remove(part);
                changes.add(new StyledTextPart(rankName, part.getPartStyle().getStyle(), null, Style.EMPTY));
                // Add a space to separate the rank from the username
                changes.add(new StyledTextPart(" ", part.getPartStyle().getStyle(), null, Style.EMPTY));

                return IterationDecision.BREAK;
            }

            // Check for recruit rank (0 stars)
            Matcher usernameMatcher = RECRUIT_USERNAME_PATTERN.matcher(part.getString(null, PartStyle.StyleType.FULL));
            if (usernameMatcher.find()) {
                String username = usernameMatcher.group(1);

                Style originalStyle = part.getPartStyle().getStyle();

                // Remove the alias formatting
                Style defaultNameStyle = originalStyle.withHoverEvent(null).withItalic(false);

                changes.remove(part);
                changes.add(new StyledTextPart("[", defaultNameStyle, null, Style.EMPTY));

                changes.add(new StyledTextPart(
                        "Recruit", defaultNameStyle.withColor(ChatFormatting.AQUA), null, Style.EMPTY));

                changes.add(
                        new StyledTextPart(" ", defaultNameStyle.withColor(ChatFormatting.AQUA), null, Style.EMPTY));

                changes.add(new StyledTextPart(
                        username, originalStyle.withColor(ChatFormatting.DARK_AQUA), null, Style.EMPTY));

                return IterationDecision.BREAK;
            }

            return IterationDecision.CONTINUE;
        });

        return modified;
    }

    private enum RankType {
        NAME,
        NONE
    }
}
