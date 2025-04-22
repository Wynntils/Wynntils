/*
 * Copyright © Wynntils 2025.
 * This file is released under LGPLv3. See LICENSE for full license details.
 */
package com.wynntils.models.raid.raids;

import com.wynntils.core.text.StyledText;
import java.util.Collections;
import java.util.Map;

public abstract class RaidKind {
    private String raidName;
    private String abbreviation;
    private StyledText entryTitle;
    private Map<Integer, Map<String, String>> challengeNames;
    private int challengeCount;
    private int bossCount;
    private Map<String, Map<Integer, String>> majorIdBuffs;

    protected RaidKind(
            String raidName,
            String abbreviation,
            StyledText entryTitle,
            Map<Integer, Map<String, String>> challengeNames,
            int challengeCount,
            int bossCount,
            Map<String, Map<Integer, String>> majorIdBuffs) {
        this.raidName = raidName;
        this.abbreviation = abbreviation;
        this.entryTitle = entryTitle;
        this.challengeNames = Collections.unmodifiableMap(challengeNames);
        this.challengeCount = challengeCount;
        this.bossCount = bossCount;
        this.majorIdBuffs = Collections.unmodifiableMap(majorIdBuffs);
    }

    protected RaidKind(
            String raidName,
            String abbreviation,
            StyledText entryTitle,
            Map<Integer, Map<String, String>> challengeNames,
            Map<String, Map<Integer, String>> majorIdBuffs) {
        this(raidName, abbreviation, entryTitle, challengeNames, 3, 1, majorIdBuffs);
    }

    public String getRaidName() {
        return raidName;
    }

    public String getAbbreviation() {
        return abbreviation;
    }

    public StyledText getEntryTitle() {
        return entryTitle;
    }

    public int getChallengeCount() {
        return challengeCount;
    }

    public int getBossCount() {
        return bossCount;
    }

    public String getChallengeName(int roomNum, String challengeLine) {
        if (!challengeNames.containsKey(roomNum)) return "";

        return challengeNames.get(roomNum).getOrDefault(challengeLine, "");
    }

    // Each boss should be one single element at the key roomNum
    public String getBossName(int roomNum) {
        if (!challengeNames.containsKey(roomNum)) return "";

        return challengeNames.get(roomNum).values().stream().toList().getFirst();
    }

    public String majorIdFromBuff(String buff, int tier) {
        if (!majorIdBuffs.containsKey(buff)) return null;

        return majorIdBuffs.get(buff).get(tier);
    }
}
