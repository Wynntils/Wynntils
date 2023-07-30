/*
 * Copyright © Wynntils 2023.
 * This file is released under AGPLv3. See LICENSE for full license details.
 */
package com.wynntils.models.players;

import com.wynntils.core.WynntilsMod;
import com.wynntils.core.components.Model;
import com.wynntils.core.text.StyledText;
import com.wynntils.handlers.chat.event.ChatMessageReceivedEvent;
import com.wynntils.models.character.CharacterModel;
import com.wynntils.models.players.type.GuildRank;
import com.wynntils.utils.mc.LoreUtils;
import com.wynntils.utils.mc.McUtils;
import java.util.List;
import java.util.Locale;
import java.util.regex.Matcher;
import java.util.regex.Pattern;
import net.minecraft.world.item.ItemStack;
import net.minecraftforge.eventbus.api.EventPriority;
import net.minecraftforge.eventbus.api.SubscribeEvent;

public class GuildModel extends Model {
    // Test suite: https://regexr.com/7hob9
    private static final Pattern GUILD_NAME_MATCHER = Pattern.compile("§3([a-zA-Z ]*?)§b \\[[a-zA-Z]{3,4}]");

    // Test suite: https://regexr.com/7hobf
    // Recruiter is intentionally first, otherwise Recruit will incorrectly match first
    // This applies to all other patterns in this class involving guild ranks
    private static final Pattern GUILD_RANK_MATCHER =
            Pattern.compile("^§7Rank: §f(Recruit|Recruiter|Captain|Strategist|Chief|Owner)$");

    // Test suite: https://regexr.com/7hoae
    private static final Pattern MSG_LEFT_GUILD = Pattern.compile("§3You have left §b[a-zA-Z ]*§3!");

    // Test suite: https://regexr.com/7hoah
    private static final Pattern MSG_JOINED_GUILD = Pattern.compile("§3You have joined §b([a-zA-Z ]*)§3!");

    // Test suite: https://regexr.com/7hra2
    private static final Pattern MSG_RANK_CHANGED = Pattern.compile(
            "^§3\\[INFO]§b [\\w]{1,16} has set ([\\w]{1,16})'s? guild rank from (?:Recruit|Recruiter|Captain|Strategist|Chief|Owner) to (Recruit|Recruiter|Captain|Strategist|Chief|Owner)$");

    private String guildName = "";
    private GuildRank guildRank;

    public GuildModel(CharacterModel characterModel) {
        super(List.of(characterModel));
    }

    // This needs to run before any chat modifications (eg. chat mentions, filter, etc)
    // Side note; it is currently impossible to detect when we get kicked as there are no messages sent at all
    @SubscribeEvent(priority = EventPriority.HIGHEST)
    public void onChatMessage(ChatMessageReceivedEvent e) {
        StyledText message = StyledText.fromComponent(e.getMessage());

        if (message.matches(MSG_LEFT_GUILD)) {
            guildName = "";
            guildRank = null;
            WynntilsMod.info("User left guild");
            return;
        }

        Matcher joinedGuildMatcher = message.getMatcher(MSG_JOINED_GUILD);
        if (joinedGuildMatcher.matches()) {
            guildName = joinedGuildMatcher.group(1);
            guildRank = GuildRank.RECRUIT;
            WynntilsMod.info("User joined guild " + guildName + " as a " + guildRank);
            return;
        }

        Matcher rankChangedMatcher = message.getMatcher(MSG_RANK_CHANGED);
        if (rankChangedMatcher.matches()) {
            if (!rankChangedMatcher.group(1).equals(McUtils.playerName())) return;
            guildRank = GuildRank.valueOf(rankChangedMatcher.group(2).toUpperCase(Locale.ROOT));
            WynntilsMod.info("User's guild rank changed to " + guildRank);
        }
    }

    public void parseGuildInfoFromGuildMenu(ItemStack guildInfoItem) {
        List<StyledText> lore = LoreUtils.getLore(guildInfoItem);

        for (StyledText line : lore) {
            Matcher guildNameMatcher = line.getMatcher(GUILD_NAME_MATCHER);
            if (guildNameMatcher.matches()) {
                guildName = guildNameMatcher.group(1);
                continue;
            }

            Matcher rankMatcher = line.getMatcher(GUILD_RANK_MATCHER);

            if (rankMatcher.matches()) {
                guildRank = GuildRank.valueOf(rankMatcher.group(1).toUpperCase(Locale.ROOT));
            }
        }

        WynntilsMod.info("Successfully parsed guild info, " + guildRank + " of " + guildName);
    }

    public String getGuildName() {
        return guildName;
    }

    public GuildRank getGuildRank() {
        return guildRank;
    }
}
