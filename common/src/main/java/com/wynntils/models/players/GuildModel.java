/*
 * Copyright © Wynntils 2023.
 * This file is released under AGPLv3. See LICENSE for full license details.
 */
package com.wynntils.models.players;

import com.wynntils.core.components.Model;
import com.wynntils.core.text.StyledText;
import com.wynntils.utils.mc.LoreUtils;
import java.util.List;
import java.util.Locale;
import java.util.regex.Matcher;
import java.util.regex.Pattern;
import net.minecraft.world.item.ItemStack;

public class GuildModel extends Model {
    private static final Pattern GUILD_NAME_MATCHER = Pattern.compile("§3(.*?)§b.*");
    private static final Pattern GUILD_RANK_MATCHER = Pattern.compile("§7Rank: §f(.*)");

    private String name = "";
    private String rank = "";
    private String rankStars = "";

    public GuildModel() {
        super(List.of());
    }

    public void parseGuildInfoFromGuildMenu(ItemStack guildInfoItem) {
        List<StyledText> lore = LoreUtils.getLore(guildInfoItem);

        for (StyledText line : lore) {
            Matcher guildNameMatcher = line.getMatcher(GUILD_NAME_MATCHER);
            if (guildNameMatcher.matches()) {
                name = guildNameMatcher.group(1);
                continue;
            }

            Matcher rankMatcher = line.getMatcher(GUILD_RANK_MATCHER);

            if (rankMatcher.matches()) {
                rank = rankMatcher.group(1);

                switch (rank.toLowerCase(Locale.ROOT)) {
                    case "owner" -> rankStars = "★★★★★";
                    case "chief" -> rankStars = "★★★★";
                    case "strategist" -> rankStars = "★★★";
                    case "captain" -> rankStars = "★★";
                    case "recruiter" -> rankStars = "★";
                    default -> rankStars = "";
                }
            }
        }
    }

    public String getGuildName() {
        return name;
    }

    public String getGuildRank() {
        return rank;
    }

    public String getGuildRankStars() {
        return rankStars;
    }
}
