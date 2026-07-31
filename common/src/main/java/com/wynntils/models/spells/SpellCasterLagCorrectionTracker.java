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
    static final int MIN_MELEE_OBSERVED_SAMPLE_COUNT = 3;
    static final int MIN_SPELL_OBSERVED_SAMPLE_COUNT = 1;
    static final int MAX_EXTRA_DELAY_MS = 30;
    static final long IDLE_RESET_MS = 250L;
    static final long STALE_PENDING_SAMPLE_MS = 2_000L;

    private static final int FEEDBACK_SLACK_MS = 8;
    private static final int MIN_EXCESS_DELAY_MS = 13;
    private static final int RECENT_GAP_MARGIN_MS = 10;
    private static final int INSTABILITY_THRESHOLD_MS = 14;
    private static final double BASE_DELAY_SCALE = 0.65d;
    private static final double RECENT_GAP_BONUS_SCALE = 0.22d;
    private static final double INSTABILITY_BONUS_SCALE = 0.10d;
    private static final double DELAY_CREDIT_SCALE = 0.40d;

    private final ChannelState spellState = new ChannelState(MIN_SPELL_OBSERVED_SAMPLE_COUNT);
    private final ChannelState itemCooldownState = new ChannelState(MIN_MELEE_OBSERVED_SAMPLE_COUNT);

    private boolean adaptiveWindowActive = false;
    private long lastActivityAtMs = -1L;

    synchronized void beginAdaptiveWindow(long nowMs) {
        adaptiveWindowActive = true;
        lastActivityAtMs = nowMs;
    }

    synchronized void onInputSent(CombatClickType click, long nowMs) {
        if (!adaptiveWindowActive) {
            return;
        }

        feedbackChannel(click).recordSend(nowMs);
        lastActivityAtMs = nowMs;
    }

    synchronized void onSpellProgressObserved(long nowMs) {
        onFeedbackObserved(spellState, nowMs);
    }

    synchronized void onItemCooldownObserved(long nowMs) {
        onFeedbackObserved(itemCooldownState, nowMs);
    }

    synchronized int computeExtraDelayMs(CombatClickType click, int baseDelayMs) {
        ChannelState channelState = feedbackChannel(click);
        if (!adaptiveWindowActive || channelState.feedbackIntervalsMs.size() < channelState.minObservedSampleCount) {
            channelState.lastDelayCreditMs = 0.0d;
            return 0;
        }

        double feedbackAverageMs = average(channelState.feedbackIntervalsMs);
        double sendAverageMs = average(channelState.sendIntervalsMs);
        double excessDelayMs = feedbackAverageMs - sendAverageMs - channelState.lastDelayCreditMs - FEEDBACK_SLACK_MS;
        if (excessDelayMs <= MIN_EXCESS_DELAY_MS || baseDelayMs < 0) {
            channelState.lastDelayCreditMs = 0.0d;
            return 0;
        }

        double latestGapMs = latest(channelState.feedbackIntervalsMs) - latest(channelState.sendIntervalsMs);
        long instabilityMs = spread(channelState.feedbackIntervalsMs) - spread(channelState.sendIntervalsMs);

        double baseDelayAdjustmentMs = (excessDelayMs - MIN_EXCESS_DELAY_MS) * BASE_DELAY_SCALE;
        double recentGapBonusMs =
                Math.max(0.0d, latestGapMs - excessDelayMs - RECENT_GAP_MARGIN_MS) * RECENT_GAP_BONUS_SCALE;
        double instabilityBonusMs = Math.max(0.0d, instabilityMs - INSTABILITY_THRESHOLD_MS) * INSTABILITY_BONUS_SCALE;
        int appliedDelayMs = (int)
                Math.min(MAX_EXTRA_DELAY_MS, Math.round(baseDelayAdjustmentMs + recentGapBonusMs + instabilityBonusMs));
        channelState.lastDelayCreditMs = appliedDelayMs > 0 ? appliedDelayMs * DELAY_CREDIT_SCALE : 0.0d;
        return appliedDelayMs;
    }

    synchronized void onTick(long nowMs, boolean sendingInputs) {
        if (!adaptiveWindowActive || sendingInputs) {
            return;
        }

        pruneStalePendingInputs(nowMs);
        if (hasPendingInputs()) {
            return;
        }

        if (lastActivityAtMs == -1L || nowMs - lastActivityAtMs > IDLE_RESET_MS) {
            clearWindowState();
        }
    }

    synchronized void reset() {
        clearWindowState();
    }

    synchronized int getObservedSampleCount(CombatClickType click) {
        return feedbackChannel(click).feedbackIntervalsMs.size();
    }

    synchronized int getSentSampleCount(CombatClickType click) {
        return feedbackChannel(click).sendIntervalsMs.size();
    }

    synchronized boolean isAdaptiveWindowActive() {
        return adaptiveWindowActive;
    }

    synchronized int getAppliedExtraDelayMs(CombatClickType click) {
        return (int) Math.round(feedbackChannel(click).lastDelayCreditMs / DELAY_CREDIT_SCALE);
    }

    synchronized int getPendingSendCount(CombatClickType click) {
        return feedbackChannel(click).pendingSentAtMs.size();
    }

    private void onFeedbackObserved(ChannelState channelState, long nowMs) {
        if (!adaptiveWindowActive) {
            return;
        }

        if (channelState.pollPendingSend() == null) {
            return;
        }

        channelState.recordFeedback(nowMs);
        lastActivityAtMs = nowMs;
    }

    private void clearWindowState() {
        adaptiveWindowActive = false;
        lastActivityAtMs = -1L;
        spellState.clear();
        itemCooldownState.clear();
    }

    private static double average(Deque<Long> samples) {
        if (samples.isEmpty()) {
            return 0.0d;
        }

        long total = 0L;
        for (long sample : samples) {
            total += sample;
        }

        return (double) total / samples.size();
    }

    private static long latest(Deque<Long> samples) {
        return samples.isEmpty() ? 0L : samples.peekLast();
    }

    private static long spread(Deque<Long> samples) {
        if (samples.isEmpty()) {
            return 0L;
        }

        long min = Long.MAX_VALUE;
        long max = Long.MIN_VALUE;
        for (long sample : samples) {
            min = Math.min(min, sample);
            max = Math.max(max, sample);
        }

        return max - min;
    }

    private ChannelState feedbackChannel(CombatClickType click) {
        return click == CombatClickType.MELEE ? itemCooldownState : spellState;
    }

    private void pruneStalePendingInputs(long nowMs) {
        spellState.pruneStalePendingSends(nowMs);
        itemCooldownState.pruneStalePendingSends(nowMs);
    }

    private boolean hasPendingInputs() {
        return !spellState.pendingSentAtMs.isEmpty() || !itemCooldownState.pendingSentAtMs.isEmpty();
    }

    private static void addInterval(Deque<Long> samples, long intervalMs) {
        if (intervalMs <= 1L) {
            return;
        }

        samples.addLast(intervalMs);
        while (samples.size() > MAX_SAMPLE_COUNT) {
            samples.removeFirst();
        }
    }

    private static final class ChannelState {
        private final Deque<Long> feedbackIntervalsMs = new ArrayDeque<>();
        private final Deque<Long> pendingSentAtMs = new ArrayDeque<>();
        private final Deque<Long> sendIntervalsMs = new ArrayDeque<>();
        private final int minObservedSampleCount;

        private long lastAcceptedFeedbackAtMs = -1L;
        private long lastSentAtMs = -1L;
        private double lastDelayCreditMs = 0.0d;

        private ChannelState(int minObservedSampleCount) {
            this.minObservedSampleCount = minObservedSampleCount;
        }

        private void recordSend(long nowMs) {
            if (lastSentAtMs != -1L) {
                addInterval(sendIntervalsMs, nowMs - lastSentAtMs);
            }

            pendingSentAtMs.addLast(nowMs);
            lastSentAtMs = nowMs;
        }

        private Long pollPendingSend() {
            return pendingSentAtMs.pollFirst();
        }

        private void pruneStalePendingSends(long nowMs) {
            while (!pendingSentAtMs.isEmpty() && nowMs - pendingSentAtMs.peekFirst() > STALE_PENDING_SAMPLE_MS) {
                pendingSentAtMs.removeFirst();
            }
        }

        private void recordFeedback(long nowMs) {
            if (lastAcceptedFeedbackAtMs != -1L) {
                addInterval(feedbackIntervalsMs, nowMs - lastAcceptedFeedbackAtMs);
            }

            lastAcceptedFeedbackAtMs = nowMs;
        }

        private void clear() {
            feedbackIntervalsMs.clear();
            pendingSentAtMs.clear();
            sendIntervalsMs.clear();
            lastAcceptedFeedbackAtMs = -1L;
            lastSentAtMs = -1L;
            lastDelayCreditMs = 0.0d;
        }
    }
}
