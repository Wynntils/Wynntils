/*
 * Copyright © Wynntils 2022-2025.
 * This file is released under LGPLv3. See LICENSE for full license details.
 */
package com.wynntils.models.objectives;

import com.wynntils.core.text.StyledText;
import com.wynntils.core.text.type.StyleType;
import com.wynntils.utils.type.CappedValue;
import java.util.Objects;
import java.util.regex.Matcher;
import java.util.regex.Pattern;

/*
 *  This class is an info holder for Wynncraft's daily and guild objectives
 */
public final class WynnObjective {
    private static final Pattern OBJECTIVE_PARSER_PATTERN = Pattern.compile("^[- ] (.*): *(\\d+)/(\\d+)$");
    private final String goal;
    private CappedValue score;
    private long updatedAt;
    private final StyledText original;
    private final boolean isGuildObjective;
    private final boolean hasEventBonus;

    public static final WynnObjective DEMO_GUILD = new WynnObjective(
            "Slay Loot Chests", new CappedValue(5, 15), 0, StyledText.fromString("Slay Loot Chests 5/15"), true, false);

    private WynnObjective(
            String goal,
            CappedValue score,
            long updatedAt,
            StyledText original,
            boolean isGuildObjective,
            boolean hasEventBonus) {
        this.goal = goal;
        this.score = score;
        this.updatedAt = updatedAt;
        this.original = original;
        this.isGuildObjective = isGuildObjective;
        this.hasEventBonus = hasEventBonus;
    }

    static WynnObjective parseObjectiveLine(StyledText objectiveLine, boolean isGuildObjective, boolean hasEventBonus) {
        String stripped = objectiveLine.getString(StyleType.NONE);

        Matcher matcher = OBJECTIVE_PARSER_PATTERN.matcher(stripped);
        String goal = null;
        int score = 0;
        int maxScore = 0;

        // Match objective strings like "- Slay Lv. 20+ Mobs: 8/140" or "- Craft Items: 0/6"
        if (matcher.find()) {
            goal = matcher.group(1);
            try {
                score = Integer.parseInt(matcher.group(2));
                maxScore = Integer.parseInt(matcher.group(3));
            } catch (NumberFormatException e) {
                // Ignored, goal is already null
            }
        }

        return new WynnObjective(
                goal,
                new CappedValue(score, maxScore),
                System.currentTimeMillis(),
                objectiveLine,
                isGuildObjective,
                hasEventBonus);
    }

    @Override
    public String toString() {
        return goal + ": " + score;
    }

    public String asObjectiveString() {
        return (hasEventBonus ? "★ " : "") + this.getGoal() + ": " + getScore();
    }

    private void updateTimestamp() {
        this.updatedAt = System.currentTimeMillis();
    }

    public boolean hasProgress() {
        return this.score.max() > 0;
    }

    public float getProgress() {
        return (float) this.score.getProgress();
    }

    public long getUpdatedAt() {
        return updatedAt;
    }

    public String getGoal() {
        return this.goal;
    }

    public StyledText getOriginal() {
        return this.original;
    }

    public CappedValue getScore() {
        return score;
    }

    public boolean isGuildObjective() {
        return isGuildObjective;
    }

    public boolean isSameObjective(WynnObjective other) {
        return Objects.equals(this.getGoal(), other.getGoal())
                && getScore().max() == other.getScore().max();
    }

    public void setCurrentScore(int newCurrentScore) {
        this.score = score.withCurrent(newCurrentScore);
        updateTimestamp();
    }
}
