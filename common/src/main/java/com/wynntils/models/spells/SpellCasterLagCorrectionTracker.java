/*
 * Copyright © Wynntils 2026.
 * This file is released under LGPLv3. See LICENSE for full license details.
 */
package com.wynntils.models.spells;

import com.wynntils.models.spells.type.CombatClickType;
import java.util.ArrayDeque;
import java.util.Deque;

final class SpellCasterLagCorrectionTracker {
    static final int MAX_SAMPLE_COUNT = 5;
    static final int MIN_OBSERVED_SAMPLE_COUNT = 3;
    static final int MAX_EXTRA_DELAY_MS = 40;
    static final long IDLE_RESET_MS = 250L;
    static final long STALE_PENDING_SAMPLE_MS = 2_000L;
    private static final int ADJUST_MIN_EXCESS_MS = 25;
    private static final int ADJUST_MIN_SPREAD_MS = 20;
    private static final int ADJUST_MIN_LATEST_OVER_AVERAGE_MS = 8;
    private static final int ADJUST_PENDING_BACKLOG = 3;
    private static final int ADJUST_STEP_MS = 4;
    private static final int DECAY_STEP_MS = 6;
    private static final int EMERGENCY_EXCESS_MS = 120;
    private static final int EMERGENCY_ABSOLUTE_LAG_MS = 400;
    private static final int EMERGENCY_PENDING_BACKLOG = 4;
    private static final int EMERGENCY_STEP_MS = 10;

    private final Deque<Long> observedSpellLagMs = new ArrayDeque<>();
    private final Deque<Long> observedItemCooldownLagMs = new ArrayDeque<>();
    private final Deque<PendingInput> pendingInputs = new ArrayDeque<>();

    private boolean adaptiveWindowActive = false;
    private long lastActivityAtMs = -1L;
    private int spellAppliedExtraDelayMs = 0;
    private int itemCooldownAppliedExtraDelayMs = 0;

    synchronized void beginAdaptiveWindow(long nowMs) {
        adaptiveWindowActive = true;
        lastActivityAtMs = nowMs;
    }

    synchronized void onInputSent(CombatClickType click, long nowMs) {
        if (!adaptiveWindowActive) return;

        pendingInputs.addLast(new PendingInput(feedbackChannel(click), nowMs));
        lastActivityAtMs = nowMs;
    }

    synchronized void onSpellProgressObserved(long nowMs) {
        onProgressObserved(FeedbackChannel.SPELL_PROGRESS, nowMs);
    }

    synchronized void onItemCooldownObserved(long nowMs) {
        onProgressObserved(FeedbackChannel.ITEM_COOLDOWN, nowMs);
    }

    private void onProgressObserved(FeedbackChannel feedbackChannel, long nowMs) {
        if (!adaptiveWindowActive) return;

        PendingInput pendingInput = pollFirstPending(feedbackChannel);
        if (pendingInput == null) return;

        addSample(getObservedSamples(feedbackChannel), nowMs - pendingInput.sentAtMs());
        lastActivityAtMs = nowMs;
    }

    synchronized int computeExtraDelayMs(CombatClickType click, int baseDelayMs) {
        FeedbackChannel feedbackChannel = feedbackChannel(click);
        Deque<Long> observedLagMs = getObservedSamples(feedbackChannel);
        if (!adaptiveWindowActive || observedLagMs.size() < MIN_OBSERVED_SAMPLE_COUNT) {
            setAppliedExtraDelayMs(feedbackChannel, 0);
            return 0;
        }

        double averageLagMs = average(observedLagMs);
        long latestLagMs = observedLagMs.peekLast();
        long lagSpreadMs = spread(observedLagMs);
        int pendingBacklog = countPendingInputs(feedbackChannel);

        int appliedExtraDelayMs = getAppliedExtraDelayMs(feedbackChannel);
        if (isEmergency(baseDelayMs, latestLagMs, pendingBacklog)) {
            appliedExtraDelayMs = Math.min(appliedExtraDelayMs + EMERGENCY_STEP_MS, MAX_EXTRA_DELAY_MS);
        } else if (shouldAdjust(baseDelayMs, averageLagMs, latestLagMs, lagSpreadMs, pendingBacklog)) {
            appliedExtraDelayMs = Math.min(appliedExtraDelayMs + ADJUST_STEP_MS, MAX_EXTRA_DELAY_MS);
        } else if (appliedExtraDelayMs > 0) {
            appliedExtraDelayMs = Math.max(appliedExtraDelayMs - DECAY_STEP_MS, 0);
        }

        setAppliedExtraDelayMs(feedbackChannel, appliedExtraDelayMs);
        return appliedExtraDelayMs;
    }

    synchronized void onTick(long nowMs, boolean sendingInputs) {
        if (!adaptiveWindowActive || sendingInputs) return;

        pruneStalePendingInputs(nowMs);
        if (!pendingInputs.isEmpty()) {
            return;
        }

        if (lastActivityAtMs == -1L || nowMs - lastActivityAtMs > IDLE_RESET_MS) {
            expireAdaptiveWindow();
        }
    }

    synchronized void reset() {
        expireAdaptiveWindow();
        observedSpellLagMs.clear();
        observedItemCooldownLagMs.clear();
    }

    private void expireAdaptiveWindow() {
        adaptiveWindowActive = false;
        lastActivityAtMs = -1L;
        spellAppliedExtraDelayMs = 0;
        itemCooldownAppliedExtraDelayMs = 0;
        pendingInputs.clear();
    }

    synchronized int getObservedSampleCount(CombatClickType click) {
        return getObservedSamples(feedbackChannel(click)).size();
    }

    synchronized boolean isAdaptiveWindowActive() {
        return adaptiveWindowActive;
    }

    synchronized int getPendingSendCount() {
        return pendingInputs.size();
    }

    synchronized int getAppliedExtraDelayMs(CombatClickType click) {
        return getAppliedExtraDelayMs(feedbackChannel(click));
    }

    private void pruneStalePendingInputs(long nowMs) {
        pendingInputs.removeIf(pendingInput -> nowMs - pendingInput.sentAtMs() > STALE_PENDING_SAMPLE_MS);
    }

    private PendingInput pollFirstPending(FeedbackChannel feedbackChannel) {
        PendingInput matchedInput = null;
        for (PendingInput pendingInput : pendingInputs) {
            if (pendingInput.feedbackChannel() != feedbackChannel) {
                continue;
            }

            matchedInput = pendingInput;
            break;
        }
        if (matchedInput != null) {
            pendingInputs.removeFirstOccurrence(matchedInput);
        }

        return matchedInput;
    }

    private static FeedbackChannel feedbackChannel(CombatClickType click) {
        return click == CombatClickType.MELEE ? FeedbackChannel.ITEM_COOLDOWN : FeedbackChannel.SPELL_PROGRESS;
    }

    private static boolean shouldAdjust(
            int baseDelayMs, double averageLagMs, long latestLagMs, long lagSpreadMs, int pendingBacklog) {
        return latestLagMs >= Math.max(baseDelayMs, 0) + ADJUST_MIN_EXCESS_MS
                && latestLagMs >= Math.round(averageLagMs) + ADJUST_MIN_LATEST_OVER_AVERAGE_MS
                && (lagSpreadMs >= ADJUST_MIN_SPREAD_MS || pendingBacklog >= ADJUST_PENDING_BACKLOG);
    }

    private static boolean isEmergency(int baseDelayMs, long latestLagMs, int pendingBacklog) {
        return latestLagMs >= Math.max(EMERGENCY_ABSOLUTE_LAG_MS, Math.max(baseDelayMs, 0) + EMERGENCY_EXCESS_MS)
                || pendingBacklog >= EMERGENCY_PENDING_BACKLOG;
    }

    private Deque<Long> getObservedSamples(FeedbackChannel feedbackChannel) {
        return switch (feedbackChannel) {
            case SPELL_PROGRESS -> observedSpellLagMs;
            case ITEM_COOLDOWN -> observedItemCooldownLagMs;
        };
    }

    private int getAppliedExtraDelayMs(FeedbackChannel feedbackChannel) {
        return switch (feedbackChannel) {
            case SPELL_PROGRESS -> spellAppliedExtraDelayMs;
            case ITEM_COOLDOWN -> itemCooldownAppliedExtraDelayMs;
        };
    }

    private void setAppliedExtraDelayMs(FeedbackChannel feedbackChannel, int appliedExtraDelayMs) {
        switch (feedbackChannel) {
            case SPELL_PROGRESS -> spellAppliedExtraDelayMs = appliedExtraDelayMs;
            case ITEM_COOLDOWN -> itemCooldownAppliedExtraDelayMs = appliedExtraDelayMs;
        }
    }

    private int countPendingInputs(FeedbackChannel feedbackChannel) {
        int count = 0;
        for (PendingInput pendingInput : pendingInputs) {
            if (pendingInput.feedbackChannel() == feedbackChannel) {
                count++;
            }
        }

        return count;
    }

    private static void addSample(Deque<Long> samples, long value) {
        if (value < 0L) return;

        samples.addLast(value);
        while (samples.size() > MAX_SAMPLE_COUNT) {
            samples.removeFirst();
        }
    }

    private static double average(Deque<Long> samples) {
        if (samples.isEmpty()) return 0.0d;

        long total = 0L;
        for (long sample : samples) {
            total += sample;
        }

        return (double) total / samples.size();
    }

    private static long spread(Deque<Long> samples) {
        if (samples.isEmpty()) return 0L;

        long min = Long.MAX_VALUE;
        long max = Long.MIN_VALUE;
        for (long sample : samples) {
            min = Math.min(min, sample);
            max = Math.max(max, sample);
        }

        return max - min;
    }

    private enum FeedbackChannel {
        SPELL_PROGRESS,
        ITEM_COOLDOWN
    }
    private record PendingInput(FeedbackChannel feedbackChannel, long sentAtMs) {}
}
