/*
 * Copyright © Wynntils 2023.
 * This file is released under AGPLv3. See LICENSE for full license details.
 */
package com.wynntils.features.chat;

import com.wynntils.core.WynntilsMod;
import com.wynntils.core.config.Category;
import com.wynntils.core.config.Config;
import com.wynntils.core.config.ConfigCategory;
import com.wynntils.core.config.RegisterConfig;
import com.wynntils.core.features.Feature;
import com.wynntils.core.text.PartStyle;
import com.wynntils.core.text.StyledText;
import com.wynntils.core.text.StyledTextPart;
import com.wynntils.handlers.chat.event.ChatMessageReceivedEvent;
import com.wynntils.models.players.GuildModel;
import com.wynntils.utils.type.IterationDecision;
import java.util.regex.Matcher;
import java.util.regex.Pattern;
import net.minecraft.ChatFormatting;
import net.minecraft.network.chat.MutableComponent;
import net.minecraft.network.chat.Style;
import net.minecraftforge.eventbus.api.EventPriority;
import net.minecraftforge.eventbus.api.SubscribeEvent;

@ConfigCategory(Category.CHAT)
public class GuildRankReplacementFeature extends Feature {
    @RegisterConfig
    public final Config<RankType> rankType = new Config<>(RankType.STARS);

    // Test suite: https://regexr.com/7e5gr
    private static final Pattern GUILD_MESSAGE_PATTERN = Pattern.compile("§3\\[(?:§b)?★{0,5}(?:§3)?.{1,16}]§b");

    // Test suite: https://regexr.com/7e66m
    private static final Pattern RECRUIT_USERNAME_PATTERN = Pattern.compile("§3\\[(.{1,16})");

    @SubscribeEvent(priority = EventPriority.HIGH)
    public void onChatMessageReceived(ChatMessageReceivedEvent e) {
        if (rankType.get() == RankType.STARS) return; // default wynn behavior is stars

        StyledText styledText = e.getStyledText();

        Matcher m = styledText.getMatcher(GUILD_MESSAGE_PATTERN);
        if (!m.find()) return;

        MutableComponent modifiedComponent;
        if (rankType.get() == RankType.NONE) {
            modifiedComponent = getModifiedRemovedComponent(styledText);
        } else {
            modifiedComponent = getModifiedNamedComponent(styledText);
        }

        if (modifiedComponent.equals(styledText.getComponent())) return; // no changes
        e.setMessage(modifiedComponent);
    }

    private MutableComponent getModifiedRemovedComponent(StyledText styledText) {
        StyledText modified = styledText.iterate((part, changes) -> {
            if (part.getString(null, PartStyle.StyleType.NONE).contains("★")) {
                changes.remove(part);
            }
            return IterationDecision.BREAK;
        });
        return modified.getComponent();
    }

    private MutableComponent getModifiedNamedComponent(StyledText styledText) {
        StyledText modified = styledText.iterateBackwards((part, changes) -> {
            int stars = (int) part.getString(null, PartStyle.StyleType.NONE)
                    .chars()
                    .filter(c -> c == '★')
                    .count();
            if (stars > 0) {
                // Make sure it's not just the user sending stars, as that part will contain other chars
                int otherCharacters = (int) part.getString(null, PartStyle.StyleType.NONE)
                        .chars()
                        .filter(c -> c != '★')
                        .count();
                if (otherCharacters > 0) return IterationDecision.CONTINUE;

                changes.remove(part);

                String rankName = GuildModel.GuildRank.values()[stars].getName();
                changes.add(new StyledTextPart(rankName, part.getPartStyle().getStyle(), null, Style.EMPTY));
                return IterationDecision.BREAK;
            }

            if (part.getString(null, PartStyle.StyleType.FULL).contains("§3[")) {
                // We've already gone through the rest of the string and did not find any stars, this is a recruit
                Matcher usernameMatcher =
                        RECRUIT_USERNAME_PATTERN.matcher(part.getString(null, PartStyle.StyleType.FULL));
                if (!usernameMatcher.find()) {
                    WynntilsMod.warn(
                            "Could not find recruit username in part: " + part.getString(null, PartStyle.StyleType.FULL)
                                    + " for guild message: " + styledText.getString());
                    return IterationDecision.CONTINUE;
                }
                String username = usernameMatcher.group(1);
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
        return modified.getComponent();
    }

    private enum RankType {
        STARS,
        NAME,
        NONE
    }
}
