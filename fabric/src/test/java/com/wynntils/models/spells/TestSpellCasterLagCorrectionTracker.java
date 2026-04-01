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
        tracker.onInputSent(CombatClickType.PRIMARY, 0L);
        tracker.onSpellProgressObserved(20L);
        tracker.onInputSent(CombatClickType.PRIMARY, 50L);
        tracker.onSpellProgressObserved(90L);

        Assertions.assertEquals(0, tracker.computeExtraDelayMs(CombatClickType.PRIMARY, 30));
    }

    @Test
    public void returnsZeroWhenObservedLagDoesNotExceedBaseDelay() {
        SpellCasterLagCorrectionTracker tracker = new SpellCasterLagCorrectionTracker();

        tracker.beginAdaptiveWindow(0L);
        tracker.onInputSent(CombatClickType.PRIMARY, 0L);
        tracker.onSpellProgressObserved(20L);
        tracker.onInputSent(CombatClickType.PRIMARY, 60L);
        tracker.onSpellProgressObserved(80L);
        tracker.onInputSent(CombatClickType.PRIMARY, 120L);
        tracker.onSpellProgressObserved(140L);
        tracker.onInputSent(CombatClickType.PRIMARY, 180L);
        tracker.onSpellProgressObserved(200L);

        Assertions.assertEquals(0, tracker.computeExtraDelayMs(CombatClickType.PRIMARY, 25));
    }

    @Test
    public void keepsStableHighLagAtZeroWhenThereIsNoSpreadOrBacklog() {
        SpellCasterLagCorrectionTracker tracker = new SpellCasterLagCorrectionTracker();

        populateStableHighLagSpellSamples(tracker);

        Assertions.assertEquals(0, tracker.computeExtraDelayMs(CombatClickType.PRIMARY, 70));
        Assertions.assertEquals(0, tracker.getAppliedExtraDelayMs(CombatClickType.PRIMARY));
    }

    @Test
    public void adjustsWhenLagSpreadGrowsBeyondThreshold() {
        SpellCasterLagCorrectionTracker tracker = new SpellCasterLagCorrectionTracker();

        populateIncreasingSpellSamples(tracker);

        Assertions.assertEquals(4, tracker.computeExtraDelayMs(CombatClickType.PRIMARY, 70));
        Assertions.assertEquals(4, tracker.getAppliedExtraDelayMs(CombatClickType.PRIMARY));
    }

    @Test
    public void adjustsWhenPendingBacklogAccumulates() {
        SpellCasterLagCorrectionTracker tracker = new SpellCasterLagCorrectionTracker();

        populateIncreasingSpellSamples(tracker);
        tracker.onInputSent(CombatClickType.PRIMARY, 360L);
        tracker.onInputSent(CombatClickType.PRIMARY, 420L);
        tracker.onInputSent(CombatClickType.PRIMARY, 480L);

        Assertions.assertEquals(4, tracker.computeExtraDelayMs(CombatClickType.PRIMARY, 70));
        Assertions.assertEquals(3, tracker.getPendingSendCount());
    }

    @Test
    public void doesNotAdjustForNormalTwoInputBacklogByItself() {
        SpellCasterLagCorrectionTracker tracker = new SpellCasterLagCorrectionTracker();

        populateStableHighLagSpellSamples(tracker);
        tracker.onInputSent(CombatClickType.PRIMARY, 360L);
        tracker.onInputSent(CombatClickType.PRIMARY, 420L);

        Assertions.assertEquals(0, tracker.computeExtraDelayMs(CombatClickType.PRIMARY, 70));
        Assertions.assertEquals(0, tracker.getAppliedExtraDelayMs(CombatClickType.PRIMARY));
    }

    @Test
    public void escalatesImmediatelyForEmergencyLag() {
        SpellCasterLagCorrectionTracker tracker = new SpellCasterLagCorrectionTracker();

        populateEmergencySpellSamples(tracker);

        Assertions.assertEquals(10, tracker.computeExtraDelayMs(CombatClickType.PRIMARY, 70));
        Assertions.assertEquals(10, tracker.getAppliedExtraDelayMs(CombatClickType.PRIMARY));
    }

    @Test
    public void decaysAppliedDelayAfterLagStabilizes() {
        SpellCasterLagCorrectionTracker tracker = new SpellCasterLagCorrectionTracker();

        populateIncreasingSpellSamples(tracker);
        Assertions.assertEquals(4, tracker.computeExtraDelayMs(CombatClickType.PRIMARY, 70));

        appendStableSpellSamples(tracker, 360L, 126L, 4);

        Assertions.assertEquals(0, tracker.computeExtraDelayMs(CombatClickType.PRIMARY, 70));
        Assertions.assertEquals(0, tracker.getAppliedExtraDelayMs(CombatClickType.PRIMARY));
    }

    @Test
    public void keepsAdaptiveWindowOpenWhilePendingInputsRemain() {
        SpellCasterLagCorrectionTracker tracker = new SpellCasterLagCorrectionTracker();

        tracker.beginAdaptiveWindow(0L);
        tracker.onInputSent(CombatClickType.PRIMARY, 0L);

        tracker.onTick(500L, false);

        Assertions.assertTrue(tracker.isAdaptiveWindowActive());
        Assertions.assertEquals(1, tracker.getPendingSendCount());

        tracker.onSpellProgressObserved(600L);
        tracker.onTick(900L, false);

        Assertions.assertFalse(tracker.isAdaptiveWindowActive());
        Assertions.assertEquals(0, tracker.getPendingSendCount());
    }

    @Test
    public void preservesObservedSamplesAcrossIdleExpiry() {
        SpellCasterLagCorrectionTracker tracker = new SpellCasterLagCorrectionTracker();

        populateStableHighLagSpellSamples(tracker);
        tracker.onTick(700L, false);

        Assertions.assertFalse(tracker.isAdaptiveWindowActive());
        Assertions.assertEquals(3, tracker.getObservedSampleCount(CombatClickType.PRIMARY));
        Assertions.assertEquals(0, tracker.getAppliedExtraDelayMs(CombatClickType.PRIMARY));

        tracker.beginAdaptiveWindow(900L);
        tracker.onInputSent(CombatClickType.PRIMARY, 900L);
        tracker.onSpellProgressObserved(1_000L);

        Assertions.assertEquals(4, tracker.getObservedSampleCount(CombatClickType.PRIMARY));
        Assertions.assertEquals(0, tracker.computeExtraDelayMs(CombatClickType.PRIMARY, 70));
    }

    @Test
    public void tracksMeleeAndSpellFeedbackIndependently() {
        SpellCasterLagCorrectionTracker tracker = new SpellCasterLagCorrectionTracker();

        tracker.beginAdaptiveWindow(0L);
        tracker.onInputSent(CombatClickType.MELEE, 0L);
        tracker.onInputSent(CombatClickType.PRIMARY, 30L);

        tracker.onSpellProgressObserved(70L);
        tracker.onItemCooldownObserved(100L);

        Assertions.assertEquals(1, tracker.getObservedSampleCount(CombatClickType.PRIMARY));
        Assertions.assertEquals(1, tracker.getObservedSampleCount(CombatClickType.MELEE));
        Assertions.assertEquals(0, tracker.getPendingSendCount());
    }

    @Test
    public void ignoresDuplicateCompletionSignalsForOneInput() {
        SpellCasterLagCorrectionTracker tracker = new SpellCasterLagCorrectionTracker();

        tracker.beginAdaptiveWindow(0L);
        tracker.onInputSent(CombatClickType.PRIMARY, 0L);
        tracker.onSpellProgressObserved(40L);
        tracker.onSpellProgressObserved(42L);
        tracker.onItemCooldownObserved(44L);

        Assertions.assertEquals(1, tracker.getObservedSampleCount(CombatClickType.PRIMARY));
        Assertions.assertEquals(0, tracker.getObservedSampleCount(CombatClickType.MELEE));
        Assertions.assertEquals(0, tracker.getPendingSendCount());

        tracker.onInputSent(CombatClickType.MELEE, 100L);
        tracker.onItemCooldownObserved(160L);
        tracker.onSpellProgressObserved(162L);

        Assertions.assertEquals(1, tracker.getObservedSampleCount(CombatClickType.PRIMARY));
        Assertions.assertEquals(1, tracker.getObservedSampleCount(CombatClickType.MELEE));
        Assertions.assertEquals(0, tracker.getPendingSendCount());
    }

    @Test
    public void keepsSpellAndMeleeLagHistoriesSeparate() {
        SpellCasterLagCorrectionTracker tracker = new SpellCasterLagCorrectionTracker();

        tracker.beginAdaptiveWindow(0L);
        tracker.onInputSent(CombatClickType.MELEE, 0L);
        tracker.onItemCooldownObserved(60L);
        tracker.onInputSent(CombatClickType.MELEE, 100L);
        tracker.onItemCooldownObserved(170L);
        tracker.onInputSent(CombatClickType.MELEE, 200L);
        tracker.onItemCooldownObserved(280L);

        tracker.onInputSent(CombatClickType.PRIMARY, 300L);
        tracker.onSpellProgressObserved(400L);
        tracker.onInputSent(CombatClickType.PRIMARY, 360L);
        tracker.onSpellProgressObserved(460L);
        tracker.onInputSent(CombatClickType.PRIMARY, 420L);
        tracker.onSpellProgressObserved(520L);

        Assertions.assertEquals(0, tracker.computeExtraDelayMs(CombatClickType.PRIMARY, 70));
        Assertions.assertEquals(4, tracker.computeExtraDelayMs(CombatClickType.MELEE, 25));
    }

    private static void populateStableHighLagSpellSamples(SpellCasterLagCorrectionTracker tracker) {
        tracker.beginAdaptiveWindow(0L);
        tracker.onInputSent(CombatClickType.PRIMARY, 0L);
        tracker.onSpellProgressObserved(100L);
        tracker.onInputSent(CombatClickType.PRIMARY, 120L);
        tracker.onSpellProgressObserved(220L);
        tracker.onInputSent(CombatClickType.PRIMARY, 240L);
        tracker.onSpellProgressObserved(340L);
    }

    private static void populateIncreasingSpellSamples(SpellCasterLagCorrectionTracker tracker) {
        tracker.beginAdaptiveWindow(0L);
        tracker.onInputSent(CombatClickType.PRIMARY, 0L);
        tracker.onSpellProgressObserved(100L);
        tracker.onInputSent(CombatClickType.PRIMARY, 120L);
        tracker.onSpellProgressObserved(230L);
        tracker.onInputSent(CombatClickType.PRIMARY, 240L);
        tracker.onSpellProgressObserved(365L);
    }

    private static void populateEmergencySpellSamples(SpellCasterLagCorrectionTracker tracker) {
        tracker.beginAdaptiveWindow(0L);
        tracker.onInputSent(CombatClickType.PRIMARY, 0L);
        tracker.onSpellProgressObserved(500L);
        tracker.onInputSent(CombatClickType.PRIMARY, 600L);
        tracker.onSpellProgressObserved(1_300L);
        tracker.onInputSent(CombatClickType.PRIMARY, 1_400L);
        tracker.onSpellProgressObserved(2_300L);
    }

    private static void appendStableSpellSamples(
            SpellCasterLagCorrectionTracker tracker, long firstSentAtMs, long lagMs, int sampleCount) {
        long sentAtMs = firstSentAtMs;
        for (int i = 0; i < sampleCount; i++) {
            tracker.onInputSent(CombatClickType.PRIMARY, sentAtMs);
            tracker.onSpellProgressObserved(sentAtMs + lagMs);
            sentAtMs += 120L;
        }
    }
}
