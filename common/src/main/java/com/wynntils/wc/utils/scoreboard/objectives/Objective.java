/*
 * Copyright © Wynntils 2022.
 * This file is released under AGPLv3. See LICENSE for full license details.
 */
package com.wynntils.wc.utils.scoreboard.objectives;

import java.util.Objects;
import java.util.regex.Matcher;
import java.util.regex.Pattern;
import net.minecraft.ChatFormatting;

/*
 *  This class is an info holder for Wynncraft's daily and guild objectives
 */
public record Objective(String goal, int score, int maxScore, long updatedAt, String original) {
    public static final Pattern OBJECTIVE_PARSER_PATTERN = Pattern.compile("^[- ] (.*): *(\\d+)/(\\d+)$");

    public static Objective parseObjectiveLine(String objectiveLine) {
        String stripped = ChatFormatting.stripFormatting(objectiveLine);

        assert stripped != null;

        Matcher matcher = OBJECTIVE_PARSER_PATTERN.matcher(stripped);
        String goal = null;
        int score = 0;
        int maxScore = 0;

        // Match objective strings like "- Slay Lv. 20+ Mobs: 8/140" or "- Craft Items: 0/6"
        // Could also be multi line like:
        // "- Trade 24E with"
        // "  lvl 10 players: 0/24"
        if (matcher.find()) {
            goal = matcher.group(1);
            try {
                score = Integer.parseInt(matcher.group(2));
                maxScore = Integer.parseInt(matcher.group(3));
            } catch (NumberFormatException e) {
                // Ignored, goal is already null
            }
        }

        return new Objective(goal, score, maxScore, System.currentTimeMillis(), objectiveLine);
    }

    @Override
    public String toString() {
        return goal + " [" + score + "/" + maxScore + "]";
    }

    public boolean hasProgress() {
        return this.maxScore > 0;
    }

    public float getProgress() {
        return (float) this.score / (float) this.maxScore;
    }

    public long getUpdatedAt() {
        return updatedAt;
    }

    public String getGoal() {
        return this.goal;
    }

    public String getOriginal() {
        return this.original;
    }

    public int getMaxScore() {
        return maxScore;
    }

    public boolean isSameObjective(Objective other) {
        return Objects.equals(this.getGoal(), other.getGoal()) && this.getMaxScore() == other.getMaxScore();
    }
}
