/*
 * Copyright © Wynntils 2023-2026.
 * This file is released under LGPLv3. See LICENSE for full license details.
 */
package com.wynntils.models.players.scoreboard;

import com.wynntils.core.WynntilsMod;
import com.wynntils.core.components.Models;
import com.wynntils.core.text.StyledText;
import com.wynntils.handlers.scoreboard.ScoreboardPart;
import com.wynntils.handlers.scoreboard.ScoreboardSegment;
import com.wynntils.handlers.scoreboard.type.SegmentMatcher;
import com.wynntils.models.players.type.PartyMember;
import java.util.ArrayList;
import java.util.List;
import java.util.regex.Matcher;
import java.util.regex.Pattern;

public class PartyScoreboardPart extends ScoreboardPart {
    private static final SegmentMatcher PARTY_MATCHER = SegmentMatcher.fromPattern("Party:\\s\\[Lv. (\\d+)]");

    private static final Pattern ONLINE_PLAYER = Pattern.compile(
            "§e- §4\\[(?:§c|§8|\\|)+(?<health>(?:\\||§|[0-9])+)\\|+§4] (?<alive>§7§m|§f)(?<name>[^§]+)(?:§r)?§7 \\[(?<level>[0-9]+)\\]");
    private static final Pattern OFFLINE_PLAYER = Pattern.compile("§e- §7(?<name>.+)");

    @Override
    public SegmentMatcher getSegmentMatcher() {
        return PARTY_MATCHER;
    }

    @Override
    public void onSegmentChange(ScoreboardSegment newValue) {
        List<PartyMember> parsedMembers = parsePartyMembers(newValue.getContent());

        Models.Party.resetScoreboardData();
        for (PartyMember parsedMember : parsedMembers) {
            Models.Party.addSbPartyMember(parsedMember);
        }
    }

    @Override
    public void onSegmentRemove(ScoreboardSegment segment) {
        for (int i = Models.Party.getSbPartyMemberCount() - 1; i >= 0; i--) {
            for (StyledText line : segment.getContent()) {
                // Reference equality usage instead .equals() is intended
                // to work around possible scoreboard name duplication so we can be sure that
                // the PartyMember we are about to remove came from this line specifically
                if (line == Models.Party.getSbPartyMember(i).line()) {
                    Models.Party.removeSbPartyMember(i);
                }
            }
        }
    }

    @Override
    public void reset() {
        Models.Party.resetScoreboardData();
    }

    @Override
    public String toString() {
        return "PartyScoreboardPart{}";
    }

    private static int safeParseInt(String s) {
        try {
            return Integer.parseInt(s);
        } catch (NumberFormatException e) {
            WynntilsMod.error("Failed to parse integer in PartyScoreboardPart: '" + s + "'");
            return 0;
        }
    }

    private static List<PartyMember> parsePartyMembers(List<StyledText> lines) {
        List<PartyMember> members = new ArrayList<>();
        for (StyledText line : lines) {
            Matcher onlineMatcher = line.getMatcher(ONLINE_PLAYER);
            if (onlineMatcher.matches()) {
                members.add(new PartyMember(
                        line,
                        onlineMatcher.group("name"),
                        safeParseInt(
                                onlineMatcher.group("health").replace("§8", "").replace("|", "")),
                        safeParseInt(onlineMatcher.group("level")),
                        true,
                        onlineMatcher.group("alive").equals("§f")));
                continue;
            }
            Matcher offlineMatcher = line.getMatcher(OFFLINE_PLAYER);
            if (offlineMatcher.matches()) {
                members.add(new PartyMember(line, offlineMatcher.group("name"), 0, 0, false, false));
            }
        }
        return members;
    }
}
