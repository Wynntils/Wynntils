/*
 * Copyright © Wynntils 2023-2024.
 * This file is released under LGPLv3. See LICENSE for full license details.
 */
package com.wynntils.models.lootrun.event;

public abstract class LootrunFinishedEventBuilder {
    public static class Completed {
        private int challengesCompleted;
        private int timeElapsed;
        private int rewardPulls;
        private int rewardRerolls;
        private int rewardSacrifices;
        private int experienceGained;
        private int mobsKilled;
        private int chestsOpened;

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

        public Completed setRewardSacrifices(int rewardSacrifices) {
            this.rewardSacrifices = rewardSacrifices;
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

        public Completed setChestsOpened(int chestsOpened) {
            this.chestsOpened = chestsOpened;
            return this;
        }

        public LootrunFinishedEvent.Completed build() {
            return new LootrunFinishedEvent.Completed(
                    challengesCompleted,
                    timeElapsed,
                    rewardPulls,
                    rewardRerolls,
                    rewardSacrifices,
                    experienceGained,
                    mobsKilled,
                    chestsOpened);
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
