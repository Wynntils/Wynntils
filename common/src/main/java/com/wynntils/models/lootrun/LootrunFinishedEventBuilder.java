/*
 * Copyright © Wynntils 2023.
 * This file is released under AGPLv3. See LICENSE for full license details.
 */
package com.wynntils.models.lootrun;

import com.wynntils.models.lootrun.event.LootrunFinishedEvent;

public abstract class LootrunFinishedEventBuilder {
    public static class Completed {
        private int challengesCompleted;
        private int timeElapsed;
        private int rewardPulls;
        private int rewardRerolls;
        private int experienceGained;
        private int mobsKilled;

        public Completed setChallengesCompleted(int challengesCompleted) {
            this.challengesCompleted = challengesCompleted;
            return this;
        }

        public Completed setTimeElapsed(int timeElapsed) {
            this.timeElapsed = timeElapsed;
            return this;
        }

        public Completed setRewardPulls(int rewardPulls) {
            this.rewardPulls = rewardPulls;
            return this;
        }

        public Completed setRewardRerolls(int rewardRerolls) {
            this.rewardRerolls = rewardRerolls;
            return this;
        }

        public Completed setExperienceGained(int experienceGained) {
            this.experienceGained = experienceGained;
            return this;
        }

        public Completed setMobsKilled(int mobsKilled) {
            this.mobsKilled = mobsKilled;
            return this;
        }

        public LootrunFinishedEvent.Completed build() {
            return new LootrunFinishedEvent.Completed(
                    challengesCompleted, timeElapsed, rewardPulls, rewardRerolls, experienceGained, mobsKilled);
        }
    }

    public static class Failed {
        private int challengesCompleted;
        private int timeElapsed;

        public Failed setChallengesCompleted(int challengesCompleted) {
            this.challengesCompleted = challengesCompleted;
            return this;
        }

        public Failed setTimeElapsed(int timeElapsed) {
            this.timeElapsed = timeElapsed;
            return this;
        }

        public LootrunFinishedEvent.Failed build() {
            return new LootrunFinishedEvent.Failed(challengesCompleted, timeElapsed);
        }
    }
}
