/*
 * Copyright © Wynntils 2024-2025.
 * This file is released under LGPLv3. See LICENSE for full license details.
 */
package com.wynntils.features;

import com.wynntils.core.components.Models;
import com.wynntils.core.consumers.features.Feature;
import com.wynntils.core.consumers.features.ProfileDefault;
import com.wynntils.core.persisted.Persisted;
import com.wynntils.core.persisted.config.Config;
import com.wynntils.core.persisted.config.ConfigProfile;
import com.wynntils.core.text.StyledText;
import com.wynntils.handlers.labels.event.LabelIdentifiedEvent;
import com.wynntils.handlers.labels.event.TextDisplayChangedEvent;
import com.wynntils.handlers.labels.type.LabelInfo;
import com.wynntils.models.guild.label.GuildSeasonLeaderboardLabelInfo;
import com.wynntils.models.guild.profile.GuildProfile;
import com.wynntils.models.guild.type.GuildLeaderboardInfo;
import com.wynntils.utils.StringUtils;
import com.wynntils.utils.colors.ColorChatFormatting;
import java.util.HashMap;
import java.util.Map;
import java.util.Optional;
import net.minecraft.ChatFormatting;
import net.minecraft.network.chat.Component;
import net.minecraft.network.chat.MutableComponent;
import net.neoforged.bus.api.SubscribeEvent;

public class ExtendedSeasonLeaderboardFeature extends Feature {
    private final Map<Integer, Map<Integer, Long>> seasonRatings = new HashMap<>();

    @Persisted
    private final Config<Boolean> useShortSeasonRankingStrings = new Config<>(false);

    @Persisted
    private final Config<Boolean> highlightOwnGuild = new Config<>(true);

    @Persisted
    private final Config<ColorChatFormatting> guildHighlightColor = new Config<>(ColorChatFormatting.GREEN);

    public ExtendedSeasonLeaderboardFeature() {
        super(new ProfileDefault.Builder()
                .disableFor(ConfigProfile.NEW_PLAYER, ConfigProfile.BLANK_SLATE)
                .build());
    }

    @SubscribeEvent
    public void onLabelIdentified(LabelIdentifiedEvent event) {
        if (event.getLabelInfo() instanceof GuildSeasonLeaderboardLabelInfo guildSeasonLeaderboardLabelInfo) {
            Map<Integer, Long> seasonMap =
                    seasonRatings.getOrDefault(guildSeasonLeaderboardLabelInfo.getSeason(), new HashMap<>());

            guildSeasonLeaderboardLabelInfo
                    .getGuildLeaderboardInfo()
                    .forEach(guildLeaderboardInfo ->
                            seasonMap.put(guildLeaderboardInfo.position(), guildLeaderboardInfo.rating()));

            seasonRatings.put(guildSeasonLeaderboardLabelInfo.getSeason(), seasonMap);
        }
    }

    @SubscribeEvent
    public void onLabelChanged(TextDisplayChangedEvent.Text event) {
        if (event.getLabelInfo().isEmpty()) return;

        LabelInfo labelInfo = event.getLabelInfo().get();

        if (labelInfo instanceof GuildSeasonLeaderboardLabelInfo guildSeasonLeaderboardLabelInfo) {
            event.setText(getUpdatedLeaderboard(guildSeasonLeaderboardLabelInfo));
        }
    }

    private StyledText getUpdatedLeaderboard(GuildSeasonLeaderboardLabelInfo guildSeasonLeaderboardLabelInfo) {
        // As the leaderboard is now one text display we need to reconstruct the entire leaderboard.
        MutableComponent newLabel = Component.empty()
                .append(Component.literal("Season " + guildSeasonLeaderboardLabelInfo.getSeason() + " Leaderboard")
                        .withStyle(ChatFormatting.LIGHT_PURPLE, ChatFormatting.BOLD));

        if (guildSeasonLeaderboardLabelInfo.isCurrentSeason()) {
            int seasonEnd = guildSeasonLeaderboardLabelInfo.getEndingDate().getFirst();

            newLabel.append(Component.literal("\nSeason ends in ").withStyle(ChatFormatting.GRAY));

            newLabel.append(Component.literal(seasonEnd + " " + guildSeasonLeaderboardLabelInfo.getTimeUnit() + " \n\n")
                    .withStyle(ChatFormatting.AQUA, ChatFormatting.BOLD));
        } else {
            int endMonth = guildSeasonLeaderboardLabelInfo.getEndingDate().getFirst();
            int endDay = guildSeasonLeaderboardLabelInfo.getEndingDate().get(1);
            int endYear = guildSeasonLeaderboardLabelInfo.getEndingDate().get(2);

            String formattedDate = String.format("%02d/%02d/%d", endMonth, endDay, endYear);

            newLabel.append(Component.literal("\nSeason ended at ").withStyle(ChatFormatting.GRAY))
                    .append(Component.literal(formattedDate + "\n\n")
                            .withStyle(ChatFormatting.AQUA, ChatFormatting.BOLD));
        }

        Map<Integer, Long> seasonMap =
                seasonRatings.getOrDefault(guildSeasonLeaderboardLabelInfo.getSeason(), new HashMap<>());

        for (GuildLeaderboardInfo leaderboardPos : guildSeasonLeaderboardLabelInfo.getGuildLeaderboardInfo()) {
            // Place color:
            // 1-3 - Gold
            // 4-6 - Yellow
            // 7-9 - White
            // 10+ - Gray
            ChatFormatting placeColor = ChatFormatting.GRAY;
            if (leaderboardPos.position() <= 3) {
                placeColor = ChatFormatting.GOLD;
            } else if (leaderboardPos.position() <= 6) {
                placeColor = ChatFormatting.YELLOW;
            } else if (leaderboardPos.position() <= 9) {
                placeColor = ChatFormatting.WHITE;
            }

            MutableComponent placement =
                    Component.literal(String.valueOf(leaderboardPos.position())).withStyle(placeColor);

            // 1st is bold
            if (leaderboardPos.position() == 1) {
                placement.withStyle(ChatFormatting.BOLD);
            }

            newLabel.append(placement).append(Component.literal(" - ")).withStyle(ChatFormatting.GRAY);

            Optional<GuildProfile> guildProfile = Models.Guild.getGuildProfile(leaderboardPos.guildName());
            String prefix = "[" + guildProfile.map(GuildProfile::prefix).orElse("???") + "]";

            ChatFormatting guildColor = ChatFormatting.AQUA;

            if (highlightOwnGuild.get() && leaderboardPos.guildName().equals(Models.Guild.getGuildName())) {
                guildColor = guildHighlightColor.get().getChatFormatting();
            }

            String scoreString;

            if (useShortSeasonRankingStrings.get()) {
                scoreString = "(" + StringUtils.integerToShortString(leaderboardPos.rating()) + ") SR";
            } else {
                scoreString = String.format("(%,d SR)", leaderboardPos.rating());
            }

            long lowerGuildRating = seasonMap.getOrDefault(leaderboardPos.position() + 1, -1L);
            String ratingDifference = " (+???)";

            if (lowerGuildRating != -1L) {
                long difference = leaderboardPos.rating() - lowerGuildRating;

                if (useShortSeasonRankingStrings.get()) {
                    ratingDifference = " (+" + StringUtils.integerToShortString(difference) + ")";
                } else {
                    ratingDifference = String.format(" (+%,d)", difference);
                }
            } else if (guildSeasonLeaderboardLabelInfo.isLastPage()) {
                // If on the last page and we don't have a rating for the lower guild then we can assume this is the
                // lowest guild
                ratingDifference = "";
            }

            // §<color><place>§7 - §b<guild name> [<guild tag>]§d (<formatted score> SR) §a(+<score diff to next>)
            newLabel.append(Component.literal(leaderboardPos.guildName() + " " + prefix)
                            .withStyle(guildColor))
                    .append(Component.literal(" " + scoreString).withStyle(ChatFormatting.LIGHT_PURPLE))
                    .append(Component.literal(ratingDifference).withStyle(ChatFormatting.GREEN))
                    .append("\n");
        }

        ChatFormatting previousPageColor =
                guildSeasonLeaderboardLabelInfo.isFirstPage() ? ChatFormatting.GRAY : ChatFormatting.GREEN;
        ChatFormatting nextPageColor =
                guildSeasonLeaderboardLabelInfo.isLastPage() ? ChatFormatting.GRAY : ChatFormatting.GREEN;

        newLabel.append(Component.literal("\n«").withStyle(previousPageColor, ChatFormatting.BOLD))
                .append(Component.literal(" ⬟ ").withStyle(ChatFormatting.YELLOW))
                .append(Component.literal("»").withStyle(nextPageColor, ChatFormatting.BOLD))
                .append(Component.literal("\nClick for Options").withStyle(ChatFormatting.YELLOW));

        return StyledText.fromComponent(newLabel);
    }
}
