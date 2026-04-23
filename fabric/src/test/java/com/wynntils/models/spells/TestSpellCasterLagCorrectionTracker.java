/*
 * Copyright © Wynntils 2026.
 * This file is released under LGPLv3. See LICENSE for full license details.
 */
package com.wynntils.models.spells;

import com.wynntils.models.spells.type.CombatClickType;
import org.junit.jupiter.api.Assertions;
import org.junit.jupiter.api.Test;

public class TestSpellCasterLagCorrectionTracker {
    @Test
    public void returnsZeroWithoutEnoughObservedSamples() {
        SpellCasterLagCorrectionTracker tracker = new SpellCasterLagCorrectionTracker();

        tracker.beginAdaptiveWindow(0L);
        recordCadence(tracker, CombatClickType.PRIMARY, 0L, 100L, 10L, 2);

        Assertions.assertEquals(0, tracker.computeExtraDelayMs(CombatClickType.PRIMARY, 30));
    }

    @Test
    public void returnsZeroWhenFeedbackCadenceMatchesSendCadence() {
        SpellCasterLagCorrectionTracker tracker = new SpellCasterLagCorrectionTracker();

        tracker.beginAdaptiveWindow(0L);
        recordCadence(tracker, CombatClickType.PRIMARY, 0L, 100L, 20L, 4);

        Assertions.assertEquals(0, tracker.computeExtraDelayMs(CombatClickType.PRIMARY, 30));
        Assertions.assertEquals(0, tracker.getAppliedExtraDelayMs(CombatClickType.PRIMARY));
    }

    @Test
    public void appliesDelayWhenFeedbackCadenceFallsBehindSendCadence() {
        SpellCasterLagCorrectionTracker tracker = new SpellCasterLagCorrectionTracker();

        tracker.beginAdaptiveWindow(0L);
        recordSpellCadenceWithIndependentFeedback(tracker, 0L, 100L, 20L, 126L, 4);

        Assertions.assertEquals(3, tracker.computeExtraDelayMs(CombatClickType.PRIMARY, 70));
        Assertions.assertEquals(3, tracker.getAppliedExtraDelayMs(CombatClickType.PRIMARY));
    }

    @Test
    public void resetsDelayAfterCadenceStabilizes() {
        SpellCasterLagCorrectionTracker tracker = new SpellCasterLagCorrectionTracker();

        tracker.beginAdaptiveWindow(0L);
        recordSpellCadenceWithIndependentFeedback(tracker, 0L, 100L, 20L, 126L, 4);
        Assertions.assertEquals(3, tracker.computeExtraDelayMs(CombatClickType.PRIMARY, 70));

        recordSpellCadenceWithIndependentFeedback(tracker, 500L, 100L, 520L, 100L, 4);

        Assertions.assertEquals(0, tracker.computeExtraDelayMs(CombatClickType.PRIMARY, 70));
        Assertions.assertEquals(0, tracker.getAppliedExtraDelayMs(CombatClickType.PRIMARY));
    }

    @Test
    public void addsBonusWhenFeedbackCadenceBecomesUnstable() {
        SpellCasterLagCorrectionTracker tracker = new SpellCasterLagCorrectionTracker();

        tracker.beginAdaptiveWindow(0L);
        recordSpellCadenceWithFeedbackIntervals(tracker, 0L, 100L, 20L, new long[] {118L, 130L, 142L});

        Assertions.assertEquals(9, tracker.computeExtraDelayMs(CombatClickType.PRIMARY, 70));
        Assertions.assertEquals(9, tracker.getAppliedExtraDelayMs(CombatClickType.PRIMARY));
    }

    @Test
    public void ignoresDuplicateSpellFeedbackWithoutAdditionalPendingInput() {
        SpellCasterLagCorrectionTracker tracker = new SpellCasterLagCorrectionTracker();

        tracker.beginAdaptiveWindow(0L);
        tracker.onInputSent(CombatClickType.PRIMARY, 90L);
        tracker.onSpellProgressObserved(100L);
        tracker.onSpellProgressObserved(101L);
        tracker.onInputSent(CombatClickType.PRIMARY, 190L);
        tracker.onSpellProgressObserved(200L);
        tracker.onSpellProgressObserved(201L);

        Assertions.assertEquals(1, tracker.getObservedSampleCount(CombatClickType.PRIMARY));
    }

    @Test
    public void appliesSpellDelayAfterSingleObservedInterval() {
        SpellCasterLagCorrectionTracker tracker = new SpellCasterLagCorrectionTracker();

        tracker.beginAdaptiveWindow(0L);
        recordSpellCadenceWithIndependentFeedback(tracker, 0L, 100L, 20L, 126L, 3);

        Assertions.assertEquals(3, tracker.computeExtraDelayMs(CombatClickType.PRIMARY, 70));
    }

    @Test
    public void tracksMeleeAndSpellCadenceIndependently() {
        SpellCasterLagCorrectionTracker tracker = new SpellCasterLagCorrectionTracker();

        tracker.beginAdaptiveWindow(0L);
        recordCadence(tracker, CombatClickType.MELEE, 0L, 60L, 20L, 4);
        recordSpellCadenceWithIndependentFeedback(tracker, 400L, 100L, 420L, 126L, 4);

        Assertions.assertEquals(0, tracker.computeExtraDelayMs(CombatClickType.MELEE, 25));
        Assertions.assertEquals(3, tracker.computeExtraDelayMs(CombatClickType.PRIMARY, 70));
        Assertions.assertEquals(3, tracker.getObservedSampleCount(CombatClickType.MELEE));
        Assertions.assertEquals(3, tracker.getObservedSampleCount(CombatClickType.PRIMARY));
    }

    @Test
    public void idleResetClearsCadenceHistory() {
        SpellCasterLagCorrectionTracker tracker = new SpellCasterLagCorrectionTracker();

        tracker.beginAdaptiveWindow(0L);
        recordCadence(tracker, CombatClickType.PRIMARY, 0L, 100L, 20L, 4);

        tracker.onTick(700L, false);

        Assertions.assertFalse(tracker.isAdaptiveWindowActive());
        Assertions.assertEquals(0, tracker.getObservedSampleCount(CombatClickType.PRIMARY));
        Assertions.assertEquals(0, tracker.getSentSampleCount(CombatClickType.PRIMARY));
    }

    @Test
    public void keepsAdaptiveWindowOpenWhileFeedbackIsStillPending() {
        SpellCasterLagCorrectionTracker tracker = new SpellCasterLagCorrectionTracker();

        tracker.beginAdaptiveWindow(0L);
        tracker.onInputSent(CombatClickType.PRIMARY, 0L);
        tracker.onSpellProgressObserved(20L);
        tracker.onInputSent(CombatClickType.PRIMARY, 100L);

        tracker.onTick(500L, false);

        Assertions.assertTrue(tracker.isAdaptiveWindowActive());
        Assertions.assertEquals(1, tracker.getPendingSendCount(CombatClickType.PRIMARY));

        tracker.onSpellProgressObserved(600L);

        Assertions.assertEquals(1, tracker.getObservedSampleCount(CombatClickType.PRIMARY));
        Assertions.assertEquals(0, tracker.getPendingSendCount(CombatClickType.PRIMARY));
    }

    @Test
    public void expiresPendingFeedbackAfterStaleTimeout() {
        SpellCasterLagCorrectionTracker tracker = new SpellCasterLagCorrectionTracker();

        tracker.beginAdaptiveWindow(0L);
        tracker.onInputSent(CombatClickType.PRIMARY, 0L);

        tracker.onTick(2_001L, false);

        Assertions.assertFalse(tracker.isAdaptiveWindowActive());
        Assertions.assertEquals(0, tracker.getPendingSendCount(CombatClickType.PRIMARY));
        Assertions.assertEquals(0, tracker.getObservedSampleCount(CombatClickType.PRIMARY));
    }

    @Test
    public void ignoresFeedbackWithoutPendingInput() {
        SpellCasterLagCorrectionTracker tracker = new SpellCasterLagCorrectionTracker();

        tracker.beginAdaptiveWindow(0L);
        tracker.onSpellProgressObserved(100L);
        tracker.onInputSent(CombatClickType.PRIMARY, 200L);
        tracker.onSpellProgressObserved(220L);

        Assertions.assertEquals(0, tracker.getObservedSampleCount(CombatClickType.PRIMARY));
        Assertions.assertEquals(0, tracker.getPendingSendCount(CombatClickType.PRIMARY));
    }

    private static void recordCadence(
            SpellCasterLagCorrectionTracker tracker,
            CombatClickType clickType,
            long firstSentAtMs,
            long intervalMs,
            long feedbackOffsetMs,
            int observationCount) {
        long sentAtMs = firstSentAtMs;
        for (int i = 0; i < observationCount; i++) {
            tracker.onInputSent(clickType, sentAtMs);
            if (clickType == CombatClickType.MELEE) {
                tracker.onItemCooldownObserved(sentAtMs + feedbackOffsetMs);
            } else {
                tracker.onSpellProgressObserved(sentAtMs + feedbackOffsetMs);
            }
            sentAtMs += intervalMs;
        }
    }

    private static void recordSpellCadenceWithIndependentFeedback(
            SpellCasterLagCorrectionTracker tracker,
            long firstSentAtMs,
            long sendIntervalMs,
            long firstFeedbackAtMs,
            long feedbackIntervalMs,
            int observationCount) {
        long sentAtMs = firstSentAtMs;
        long feedbackAtMs = firstFeedbackAtMs;
        for (int i = 0; i < observationCount; i++) {
            tracker.onInputSent(CombatClickType.PRIMARY, sentAtMs);
            tracker.onSpellProgressObserved(feedbackAtMs);
            sentAtMs += sendIntervalMs;
            feedbackAtMs += feedbackIntervalMs;
        }
    }

    private static void recordSpellCadenceWithFeedbackIntervals(
            SpellCasterLagCorrectionTracker tracker,
            long firstSentAtMs,
            long sendIntervalMs,
            long firstFeedbackAtMs,
            long[] feedbackIntervalsMs) {
        long sentAtMs = firstSentAtMs;
        long feedbackAtMs = firstFeedbackAtMs;

        tracker.onInputSent(CombatClickType.PRIMARY, sentAtMs);
        tracker.onSpellProgressObserved(feedbackAtMs);
        sentAtMs += sendIntervalMs;

        for (long feedbackIntervalMs : feedbackIntervalsMs) {
            feedbackAtMs += feedbackIntervalMs;
            tracker.onInputSent(CombatClickType.PRIMARY, sentAtMs);
            tracker.onSpellProgressObserved(feedbackAtMs);
            sentAtMs += sendIntervalMs;
        }
    }
}
