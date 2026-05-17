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

    @Override
    public SegmentMatcher getSegmentMatcher() {
        return PARTY_MATCHER;
    }

    @Override
    public void onSegmentChange(ScoreboardSegment newValue) {
        List<PartyMember> parsedMembers = parsePartyMembers(newValue.getContent());
        List<String> partyMembers = Models.Party.getPartyMembers();

        // Sort entries so that name matches with one stored in PartyModel
        // but scoreboard can trim names so we use startsWith to check match
        parsedMembers = parsedMembers.stream()
                .filter(m -> {
                    for (String member : partyMembers) {
                        if (member.startsWith(m.name())) return true;
                    }
                    return false;
                })
                .sorted(Comparator.comparingInt(m -> {
                    for (int i = 0; i < partyMembers.size(); i++) {
                        if (partyMembers.get(i).startsWith(m.name())) return i;
                    }
                    return Integer.MAX_VALUE;
                }))
                .toList();

        for (PartyMember member : parsedMembers) {
            Models.Party.addPartyMemberHealth(member.health());
            Models.Party.addPartyMemberLevel(member.level());
            Models.Party.addPartyMemberOnline(member.online());
            Models.Party.addPartyMemberAlive(member.alive());
        }
    }

    @Override
    public void onSegmentRemove(ScoreboardSegment segment) {
        for (PartyMember member : parsePartyMembers(segment.getContent())) {
            Models.Party.removePartyMemberHealth(member.health());
            Models.Party.removePartyMemberLevel(member.level());
            Models.Party.removePartyMemberOnline(member.online());
            Models.Party.removePartyMemberAlive(member.alive());
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

    private static Boolean isAlive(String s) {
        // Dead player's name is prefixed with §7§m
        // Alive player's name is prefixed with §f
        return Objects.equals(s, "§f");
    }

    private static List<PartyMember> parsePartyMembers(List<StyledText> lines) {
        List<PartyMember> members = new ArrayList<>();
        for (StyledText line : lines) {
            Matcher onlineMatcher = line.getMatcher(ONLINE_PLAYER);
            if (onlineMatcher.matches()) {
                members.add(
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
            Matcher offlineMatcher = line.getMatcher(OFFLINE_PLAYER);
            if (offlineMatcher.matches()) {
                members.add(new PartyMember(offlineMatcher.group("name"), 0, 0, false, false));
            }
        }
        return members;
    }

    private record PartyMember(String name, Integer health, Integer level, Boolean online, Boolean alive) {
    }
}
