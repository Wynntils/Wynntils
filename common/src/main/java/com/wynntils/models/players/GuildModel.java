/*
 * Copyright © Wynntils 2023.
 * This file is released under AGPLv3. See LICENSE for full license details.
 */
package com.wynntils.models.players;

import com.wynntils.core.components.Model;
import com.wynntils.core.text.StyledText;
import com.wynntils.models.character.CharacterModel;
import com.wynntils.models.players.type.GuildRank;
import com.wynntils.utils.mc.LoreUtils;
import java.util.List;
import java.util.regex.Matcher;
import java.util.regex.Pattern;
import net.minecraft.world.item.ItemStack;

public class GuildModel extends Model {
    private static final Pattern GUILD_NAME_MATCHER = Pattern.compile("§3(.*?)§b.*");
    private static final Pattern GUILD_RANK_MATCHER = Pattern.compile("§7Rank: §f(.*)");

    private String name = "";
    private GuildRank rank;

    public GuildModel(CharacterModel characterModel) {
        super(List.of(characterModel));
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
                for (GuildRank guildRank : GuildRank.values()) {
                    if (guildRank.getName().equals(rankMatcher.group(1))) {
                        rank = guildRank;
                    }
                }
            }
        }
    }

    public String getGuildName() {
        return name;
    }

    public GuildRank getGuildRank() {
        return rank;
    }
}
