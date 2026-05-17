/*
 * Copyright © Wynntils 2023.
 * This file is released under LGPLv3. See LICENSE for full license details.
 */
package com.wynntils.models.players.scoreboard;

import com.wynntils.core.WynntilsMod;
import com.wynntils.core.components.Models;
import com.wynntils.core.text.StyledText;
import com.wynntils.handlers.scoreboard.ScoreboardPart;
import com.wynntils.handlers.scoreboard.ScoreboardSegment;
import com.wynntils.handlers.scoreboard.type.SegmentMatcher;

import java.util.ArrayList;
import java.util.Comparator;
import java.util.List;
import java.util.Objects;
import java.util.regex.Matcher;
import java.util.regex.Pattern;

public class PartyScoreboardPart extends ScoreboardPart {
    private static final SegmentMatcher PARTY_MATCHER = SegmentMatcher.fromPattern("Party:\\s\\[Lv. (\\d+)]");

    private static final Pattern ONLINE_PLAYER = Pattern.compile("§e- §4\\[(?:§c|§8|\\|)+(?<health>(?:§|[0-9])+)\\|+§4] (?<alive>§7§m|§f)(?<name>[^§]+)(?:§r)?§7 \\[(?<level>[0-9]+)\\]");
    private static final Pattern OFFLINE_PLAYER = Pattern.compile("§e- §7(?<name>.+)");
    private static final Pattern TOTAL_LEVEL = Pattern.compile("§e§lParty:§6 \\[Lv. (?<level>[0-9]+)\\]");

    @Override
    public SegmentMatcher getSegmentMatcher() {
        return PARTY_MATCHER;
    }

    @Override
    public void onSegmentChange(ScoreboardSegment newValue) {
        List<PartyMember> foundMembers = new ArrayList<>();
        for (StyledText text : newValue.getContent()) {
            Matcher onlineMatcher = text.getMatcher(ONLINE_PLAYER);
            if (onlineMatcher.matches()) {
                foundMembers.add(
                        new PartyMember(
                                onlineMatcher.group("name"),
                                safeParseInt(onlineMatcher.group("health").replace("§8", "")),
                                safeParseInt(onlineMatcher.group("level")),
                                true,
                                isAlive(onlineMatcher.group("alive"))
                        )
                );
                continue;
            }
            Matcher offlineMatcher = text.getMatcher(OFFLINE_PLAYER);
            if (offlineMatcher.matches()) {
                foundMembers.add(new PartyMember(offlineMatcher.group("name"), 0, 0, false, false));
            }
        }

        List<String> members = Models.Party.getPartyMembers();

        // There shouldn't be any members in scoreboard that aren't in /party list
        // but just to be safe let's filter it
        // Also sort health and level so that name matches in the list stored in PartyModel
        // but scoreboard can trim names so we use startsWith to check
        List<PartyMember> sbMembers = foundMembers.stream()
                .filter(m -> {
                    for (String member : members) {
                        if (member.startsWith(m.name())) return true;
                    }
                    return false;
                })
                .sorted(Comparator.comparingInt(m -> {
                    for (int i = 0; i < members.size(); i++) {
                        if (members.get(i).startsWith(m.name())) return i;
                    }
                    return Integer.MAX_VALUE;
                }))
                .toList();

        Models.Party.setPartyMemberHealths(sbMembers.stream().map(PartyMember::health).toList());
        Models.Party.setPartyMemberLevels(sbMembers.stream().map(PartyMember::level).toList());
        Models.Party.setPartyMemberOnlines(sbMembers.stream().map(PartyMember::online).toList());
        Models.Party.setPartyMemberAlives(sbMembers.stream().map(PartyMember::alive).toList());

        Matcher totalLevelMatcher = newValue.getHeader().getMatcher(TOTAL_LEVEL);
        if (totalLevelMatcher.matches()) {
            Models.Party.setTotalPartyLevel(safeParseInt(totalLevelMatcher.group("level")));
        }
    }

    @Override
    public void onSegmentRemove(ScoreboardSegment segment) {
    }

    @Override
    public void reset() {
        Models.Party.setPartyMemberHealths(List.of());
        Models.Party.setPartyMemberLevels(List.of());
        Models.Party.setPartyMemberOnlines(List.of());
        Models.Party.setPartyMemberAlives(List.of());
        Models.Party.setTotalPartyLevel(0);
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

    private static Boolean isAlive(String s) {
        // Dead player's name is prefixed with §7§m
        // Alive player's name is prefixed with §f
        return Objects.equals(s, "§f");
    }

    private record PartyMember(String name, Integer health, Integer level, Boolean online, Boolean alive) {
    }
}
