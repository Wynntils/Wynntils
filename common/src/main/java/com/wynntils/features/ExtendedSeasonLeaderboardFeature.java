/*
 * Copyright © Wynntils 2024.
 * This file is released under LGPLv3. See LICENSE for full license details.
 */
package com.wynntils.features;

import com.wynntils.core.components.Models;
import com.wynntils.core.consumers.features.Feature;
import com.wynntils.core.persisted.Persisted;
import com.wynntils.core.persisted.config.Config;
import com.wynntils.core.text.StyledText;
import com.wynntils.handlers.labels.event.EntityLabelChangedEvent;
import com.wynntils.handlers.labels.event.LabelIdentifiedEvent;
import com.wynntils.models.players.label.GuildSeasonLeaderboardLabelInfo;
import com.wynntils.models.players.profile.GuildProfile;
import com.wynntils.models.worlds.event.WorldStateEvent;
import com.wynntils.utils.StringUtils;
import java.util.ArrayList;
import java.util.Comparator;
import java.util.List;
import java.util.Optional;
import net.minecraft.ChatFormatting;
import net.minecraft.network.chat.Component;
import net.minecraft.network.chat.MutableComponent;
import net.minecraftforge.eventbus.api.SubscribeEvent;

public class ExtendedSeasonLeaderboardFeature extends Feature {
    private final List<GuildSeasonLeaderboardLabelInfo> labelInfos = new ArrayList<>();

    @Persisted
    private final Config<Boolean> useShortSeasonRankingStrings = new Config<>(false);

    @SubscribeEvent
    public void onLabelIdentified(LabelIdentifiedEvent event) {
        if (event.getLabelInfo() instanceof GuildSeasonLeaderboardLabelInfo guildSeasonLeaderboardLabelInfo) {
            labelInfos.add(guildSeasonLeaderboardLabelInfo);
        }
    }

    @SubscribeEvent
    public void onLabelChanged(EntityLabelChangedEvent event) {
        if (event.getLabelInfo().isEmpty()
                || !(event.getLabelInfo().get()
                        instanceof GuildSeasonLeaderboardLabelInfo guildSeasonLeaderboardLabelInfo)) {
            return;
        }

        // Find the label info that is "after" the current label info, in ranking
        GuildSeasonLeaderboardLabelInfo nextLabelInfo = labelInfos.stream()
                .filter(labelInfo -> labelInfo.getPlace() > guildSeasonLeaderboardLabelInfo.getPlace())
                .min(Comparator.comparingInt(GuildSeasonLeaderboardLabelInfo::getPlace))
                .orElse(null);

        Optional<GuildProfile> guildProfile = Models.Guild.getGuildProfile(guildSeasonLeaderboardLabelInfo.getGuild());

        // Update the current label info with the additional information

        // Place color:
        // 1-3 - Gold
        // 4-6 - Yellow
        // 7-9 - White
        // 10+ - Gray
        ChatFormatting placeColor = ChatFormatting.GRAY;
        if (guildSeasonLeaderboardLabelInfo.getPlace() <= 3) {
            placeColor = ChatFormatting.GOLD;
        } else if (guildSeasonLeaderboardLabelInfo.getPlace() <= 6) {
            placeColor = ChatFormatting.YELLOW;
        } else if (guildSeasonLeaderboardLabelInfo.getPlace() <= 9) {
            placeColor = ChatFormatting.WHITE;
        }

        String scoreString;
        String scoreDiffString;
        if (useShortSeasonRankingStrings.get()) {
            scoreString = StringUtils.integerToShortString(guildSeasonLeaderboardLabelInfo.getScore());
            scoreDiffString = StringUtils.integerToShortString(
                    nextLabelInfo == null ? 0 : guildSeasonLeaderboardLabelInfo.getScore() - nextLabelInfo.getScore());
        } else {
            scoreString = String.format("%,d", guildSeasonLeaderboardLabelInfo.getScore());
            scoreDiffString = String.format(
                    "%,d",
                    nextLabelInfo == null ? 0 : guildSeasonLeaderboardLabelInfo.getScore() - nextLabelInfo.getScore());
        }

        // §<color><place>§7 - §b<guild name> [<guild tag>]§d (<formatted score> SR) §a(+<score diff to next>)
        MutableComponent newLabel = Component.empty()
                .append(Component.literal(String.valueOf(guildSeasonLeaderboardLabelInfo.getPlace()))
                        .withStyle(
                                guildSeasonLeaderboardLabelInfo.getPlace() == 1
                                        ? ChatFormatting.BOLD
                                        : ChatFormatting.RESET)
                        .withStyle(placeColor))
                .append(Component.literal(" - ").withStyle(ChatFormatting.GRAY))
                .append(Component.literal(guildSeasonLeaderboardLabelInfo.getGuild())
                        .withStyle(ChatFormatting.AQUA))
                .append(Component.literal(
                                " [" + guildProfile.map(GuildProfile::prefix).orElse("???") + "]")
                        .withStyle(ChatFormatting.AQUA))
                .append(Component.literal(" (" + scoreString + " SR)").withStyle(ChatFormatting.LIGHT_PURPLE))
                .append(Component.literal(" (+" + (nextLabelInfo == null ? "???" : scoreDiffString) + ")")
                        .withStyle(ChatFormatting.GREEN));

        event.setName(StyledText.fromComponent(newLabel));
    }

    @SubscribeEvent
    public void onWorldStateChange(WorldStateEvent event) {
        labelInfos.clear();
    }
}
