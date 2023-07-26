/*
 * Copyright © Wynntils 2023.
 * This file is released under AGPLv3. See LICENSE for full license details.
 */
package com.wynntils.models.lootrun.event;

import net.minecraftforge.eventbus.api.Event;

public abstract class LootrunFinishedEvent extends Event {
    protected final int challengesCompleted;
    protected final int timeElapsed;

    protected LootrunFinishedEvent(int challengesCompleted, int timeElapsed) {
        this.challengesCompleted = challengesCompleted;
        this.timeElapsed = timeElapsed;
    }

    public int getChallengesCompleted() {
        return challengesCompleted;
    }

    public int getTimeElapsed() {
        return timeElapsed;
    }

    public static class Completed extends LootrunFinishedEvent {
        protected final int rewardPulls;
        protected final int rewardRerolls;
        private final int experienceGained;
        private final int mobsKilled;

        public Completed(
                int challengesCompleted,
                int timeElapsed,
                int rewardPulls,
                int rewardRerolls,
                int experienceGained,
                int mobsKilled) {
            super(challengesCompleted, timeElapsed);
            this.rewardPulls = rewardPulls;
            this.rewardRerolls = rewardRerolls;
            this.experienceGained = experienceGained;
            this.mobsKilled = mobsKilled;
        }

        public int getRewardPulls() {
            return rewardPulls;
        }

        public int getRewardRerolls() {
            return rewardRerolls;
        }

        public int getExperienceGained() {
            return experienceGained;
        }

        public int getMobsKilled() {
            return mobsKilled;
        }

        public static class Builder {
            private int challengesCompleted;
            private int timeElapsed;
            private int rewardPulls;
            private int rewardRerolls;
            private int experienceGained;
            private int mobsKilled;

            public Builder setChallengesCompleted(int challengesCompleted) {
                this.challengesCompleted = challengesCompleted;
                return this;
            }

            public Builder setTimeElapsed(int timeElapsed) {
                this.timeElapsed = timeElapsed;
                return this;
            }

            public Builder setRewardPulls(int rewardPulls) {
                this.rewardPulls = rewardPulls;
                return this;
            }

            public Builder setRewardRerolls(int rewardRerolls) {
                this.rewardRerolls = rewardRerolls;
                return this;
            }

            public Builder setExperienceGained(int experienceGained) {
                this.experienceGained = experienceGained;
                return this;
            }

            public Builder setMobsKilled(int mobsKilled) {
                this.mobsKilled = mobsKilled;
                return this;
            }

            public Completed build() {
                return new Completed(
                        challengesCompleted, timeElapsed, rewardPulls, rewardRerolls, experienceGained, mobsKilled);
            }
        }
    }

    public static class Failed extends LootrunFinishedEvent {
        public Failed(int challengesCompleted, int timeElapsed) {
            super(challengesCompleted, timeElapsed);
        }

        public static class Builder {
            private int challengesCompleted;
            private int timeElapsed;

            public Builder setChallengesCompleted(int challengesCompleted) {
                this.challengesCompleted = challengesCompleted;
                return this;
            }

            public Builder setTimeElapsed(int timeElapsed) {
                this.timeElapsed = timeElapsed;
                return this;
            }

            public Failed build() {
                return new Failed(challengesCompleted, timeElapsed);
            }
        }
    }
}
