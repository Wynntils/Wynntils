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
    }

    public static class Failed extends LootrunFinishedEvent {
        public Failed(int challengesCompleted, int timeElapsed) {
            super(challengesCompleted, timeElapsed);
        }
    }
}
