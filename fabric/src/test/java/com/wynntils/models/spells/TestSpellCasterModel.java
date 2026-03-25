/*
 * Copyright © Wynntils 2026.
 * This file is released under LGPLv3. See LICENSE for full license details.
 */
package com.wynntils.models.spells;

import com.wynntils.core.WynntilsMod;
import com.wynntils.models.spells.type.CombatClickType;
import java.util.ArrayList;
import java.util.Collections;
import java.util.List;
import java.util.concurrent.CopyOnWriteArrayList;
import java.util.concurrent.CountDownLatch;
import java.util.concurrent.TimeUnit;
import java.util.concurrent.atomic.AtomicLong;
import java.util.function.BooleanSupplier;
import org.junit.jupiter.api.Assertions;
import org.junit.jupiter.api.BeforeAll;
import org.junit.jupiter.api.Test;

public class TestSpellCasterModel {
    @BeforeAll
    public static void setup() {
        WynntilsMod.setupTestEnv();
    }

    @Test
    public void rejectsAdditionalSpellWhileBusy() throws Exception {
        CountDownLatch sleeping = new CountDownLatch(1);
        SpellCasterModel model = new SpellCasterModel((click, usesRightClick, isArcher) -> {}, delayMs -> {
            sleeping.countDown();
            Thread.sleep(TimeUnit.SECONDS.toMillis(5));
        });

        try {
            Assertions.assertTrue(model.queueClicks(List.of(CombatClickType.PRIMARY), false, 0, 1, 0));
            waitFor(() -> sleeping.getCount() == 0, "Timed out waiting for the active spell to enter its delay");
            Assertions.assertTrue(model.isSendingInputs());

            Assertions.assertFalse(model.queueClicks(List.of(CombatClickType.SECONDARY), false, 0, 0, 0));
        } finally {
            model.shutdown();
        }
    }

    @Test
    public void clearDropsQueuedSequencesBeforeTheyExecute() throws Exception {
        List<String> events = Collections.synchronizedList(new ArrayList<>());
        CountDownLatch sleeping = new CountDownLatch(1);
        SpellCasterModel model =
                new SpellCasterModel((click, usesRightClick, isArcher) -> events.add(click.name()), delayMs -> {
                    sleeping.countDown();
                    Thread.sleep(TimeUnit.SECONDS.toMillis(5));
                });

        try {
            Assertions.assertTrue(
                    model.queueClicks(List.of(CombatClickType.PRIMARY, CombatClickType.PRIMARY), false, 1, 1, 0));
            waitFor(() -> sleeping.getCount() == 0, "Timed out waiting for the active spell to enter its delay");

            Assertions.assertFalse(model.queueClicks(List.of(CombatClickType.SECONDARY), false, 0, 0, 0));
            Assertions.assertTrue(model.isBusy());
            Assertions.assertTrue(model.isSendingInputs());

            model.clear();

            waitFor(() -> !model.isBusy(), "Timed out waiting for clear() to empty the spellcaster");
            Thread.sleep(50);

            Assertions.assertEquals(List.of("PRIMARY"), snapshot(events));
            Assertions.assertFalse(model.isSendingInputs());
        } finally {
            model.shutdown();
        }
    }

    @Test
    public void clearDoesNotInvokeIdleListener() throws Exception {
        CountDownLatch sleeping = new CountDownLatch(1);
        CountDownLatch idleCallbackInvoked = new CountDownLatch(1);
        SpellCasterModel model = new SpellCasterModel((click, usesRightClick, isArcher) -> {}, delayMs -> {
            sleeping.countDown();
            Thread.sleep(TimeUnit.SECONDS.toMillis(5));
        });
        model.setIdleListener(idleCallbackInvoked::countDown);

        try {
            Assertions.assertTrue(
                    model.queueClicks(List.of(CombatClickType.PRIMARY, CombatClickType.SECONDARY), false, 1, 1, 0));
            waitFor(() -> sleeping.getCount() == 0, "Timed out waiting for the active spell to enter its delay");

            model.clear();

            waitFor(() -> !model.isBusy(), "Timed out waiting for clear() to empty the spellcaster");
            Thread.sleep(50);

            Assertions.assertEquals(1L, idleCallbackInvoked.getCount());
        } finally {
            model.shutdown();
        }
    }

    @Test
    public void acceptsNextSpellAfterCurrentSequenceFinishes() throws Exception {
        List<String> events = Collections.synchronizedList(new ArrayList<>());
        SpellCasterModel model = new SpellCasterModel(
                (click, usesRightClick, isArcher) -> events.add(click.name()),
                delayMs -> events.add("sleep:" + delayMs));

        try {
            Assertions.assertTrue(model.queueClicks(List.of(CombatClickType.PRIMARY), false, 0, 25, 0));
            waitFor(() -> snapshot(events).size() == 2, "Timed out waiting for the first spell to finish");
            Assertions.assertTrue(model.queueClicks(List.of(CombatClickType.PRIMARY), false, 0, 25, 0));
            waitFor(() -> snapshot(events).size() == 4, "Timed out waiting for the second spell to execute");

            Assertions.assertEquals(List.of("PRIMARY", "sleep:25", "PRIMARY", "sleep:25"), snapshot(events));
        } finally {
            model.shutdown();
        }
    }

    @Test
    public void excludesCooldownFromSendingInputs() throws Exception {
        CountDownLatch clickDelayStarted = new CountDownLatch(1);
        CountDownLatch releaseClickDelay = new CountDownLatch(1);
        CountDownLatch cooldownStarted = new CountDownLatch(1);
        CountDownLatch releaseCooldown = new CountDownLatch(1);
        SpellCasterModel model = new SpellCasterModel((click, usesRightClick, isArcher) -> {}, delayMs -> {
            if (delayMs == 25) {
                clickDelayStarted.countDown();
                releaseClickDelay.await();
                return;
            }

            if (delayMs == 50) {
                cooldownStarted.countDown();
                releaseCooldown.await();
            }
        });

        try {
            Assertions.assertTrue(model.queueClicks(List.of(CombatClickType.PRIMARY), false, 0, 25, 50));

            waitFor(() -> clickDelayStarted.getCount() == 0, "Timed out waiting for spell input sending to start");
            Assertions.assertTrue(model.isBusy());
            Assertions.assertTrue(model.isSendingInputs());

            releaseClickDelay.countDown();

            waitFor(() -> cooldownStarted.getCount() == 0, "Timed out waiting for spell cooldown to start");
            Assertions.assertTrue(model.isBusy());
            Assertions.assertFalse(model.isSendingInputs());

            releaseCooldown.countDown();

            waitFor(() -> !model.isBusy(), "Timed out waiting for spell cooldown to finish");
            Assertions.assertFalse(model.isSendingInputs());
        } finally {
            releaseClickDelay.countDown();
            releaseCooldown.countDown();
            model.shutdown();
        }
    }

    @Test
    public void ignoresCooldownClearPacketsForMeleeSampling() {
        Assertions.assertFalse(SpellCasterModel.shouldObserveItemCooldown(true, true, 0));
        Assertions.assertFalse(SpellCasterModel.shouldObserveItemCooldown(true, true, -1));
        Assertions.assertFalse(SpellCasterModel.shouldObserveItemCooldown(false, true, 5));
        Assertions.assertFalse(SpellCasterModel.shouldObserveItemCooldown(true, false, 5));
        Assertions.assertTrue(SpellCasterModel.shouldObserveItemCooldown(true, true, 5));
    }

    @Test
    public void sleepsAfterFinalCombatInput() throws Exception {
        List<String> events = Collections.synchronizedList(new ArrayList<>());
        SpellCasterModel model = new SpellCasterModel(
                (click, usesRightClick, isArcher) -> events.add(click.name() + ":" + usesRightClick),
                delayMs -> events.add("sleep:" + delayMs));

        try {
            Assertions.assertTrue(model.queueClicks(List.of(CombatClickType.MELEE), false, 5, 7, 0));
            waitFor(() -> snapshot(events).size() == 2, "Timed out waiting for the standalone melee to execute");

            Assertions.assertEquals(List.of("MELEE:false", "sleep:5"), snapshot(events));
        } finally {
            model.shutdown();
        }
    }

    @Test
    public void appliesDelayBasedOnTheSentCombatInput() throws Exception {
        List<String> events = Collections.synchronizedList(new ArrayList<>());
        SpellCasterModel model = new SpellCasterModel(
                (click, usesRightClick, isArcher) -> events.add(click.name() + ":" + usesRightClick),
                delayMs -> events.add("sleep:" + delayMs));

        try {
            Assertions.assertTrue(model.queueClicks(
                    List.of(CombatClickType.PRIMARY, CombatClickType.SECONDARY, CombatClickType.PRIMARY),
                    false,
                    5,
                    7,
                    0));

            waitFor(() -> snapshot(events).size() == 6, "Timed out waiting for the asymmetric sequence to execute");

            Assertions.assertEquals(
                    List.of("PRIMARY:true", "sleep:7", "SECONDARY:false", "sleep:5", "PRIMARY:true", "sleep:7"),
                    snapshot(events));
        } finally {
            model.shutdown();
        }
    }

    @Test
    public void preservesMixedSequenceTimingForRepeatedLeftClicks() throws Exception {
        List<String> events = Collections.synchronizedList(new ArrayList<>());
        SpellCasterModel model = new SpellCasterModel(
                (click, usesRightClick, isArcher) -> events.add(click.name() + ":" + usesRightClick),
                delayMs -> events.add("sleep:" + delayMs));

        try {
            Assertions.assertTrue(model.queueClicks(
                    List.of(CombatClickType.PRIMARY, CombatClickType.SECONDARY, CombatClickType.SECONDARY),
                    false,
                    5,
                    7,
                    0));

            waitFor(() -> snapshot(events).size() == 6, "Timed out waiting for the mixed sequence to execute");

            Assertions.assertEquals(
                    List.of("PRIMARY:true", "sleep:7", "SECONDARY:false", "sleep:5", "SECONDARY:false", "sleep:5"),
                    snapshot(events));
        } finally {
            model.shutdown();
        }
    }

    @Test
    public void executesCombinedMeleeAndSpellSequenceInOrder() throws Exception {
        List<String> events = Collections.synchronizedList(new ArrayList<>());
        SpellCasterModel model = new SpellCasterModel(
                (click, usesRightClick, isArcher) -> events.add(click.name() + ":" + usesRightClick),
                delayMs -> events.add("sleep:" + delayMs));

        try {
            Assertions.assertTrue(model.queueClicks(
                    List.of(
                            CombatClickType.MELEE,
                            CombatClickType.PRIMARY,
                            CombatClickType.SECONDARY,
                            CombatClickType.PRIMARY),
                    false,
                    5,
                    7,
                    11));

            waitFor(() -> snapshot(events).size() == 9, "Timed out waiting for the combined sequence to execute");

            Assertions.assertEquals(
                    List.of(
                            "MELEE:false",
                            "sleep:5",
                            "PRIMARY:true",
                            "sleep:7",
                            "SECONDARY:false",
                            "sleep:5",
                            "PRIMARY:true",
                            "sleep:7",
                            "sleep:11"),
                    snapshot(events));
        } finally {
            model.shutdown();
        }
    }

    @Test
    public void adaptiveLagCorrectionAddsExtraDelayOnlyWhenEnabled() throws Exception {
        AtomicLong clock = new AtomicLong(200L);
        SpellCasterLagCorrectionTracker tracker = new SpellCasterLagCorrectionTracker();
        populateIncreasingSpellSamples(tracker);
        List<Integer> adaptiveSleeps = new CopyOnWriteArrayList<>();
        SpellCasterModel adaptiveModel = new SpellCasterModel(
                (click, usesRightClick, isArcher) -> {}, delayMs -> adaptiveSleeps.add(delayMs), tracker, clock::get);

        try {
            Assertions.assertTrue(adaptiveModel.queueClicks(List.of(CombatClickType.PRIMARY), false, 5, 7, 0, true));
            waitFor(() -> adaptiveSleeps.size() == 1, "Timed out waiting for the adaptive spell to execute");

            Assertions.assertEquals(List.of(11), List.copyOf(adaptiveSleeps));
        } finally {
            adaptiveModel.shutdown();
        }

        clock.set(200L);
        SpellCasterLagCorrectionTracker fixedTracker = new SpellCasterLagCorrectionTracker();
        populateIncreasingSpellSamples(fixedTracker);
        List<Integer> fixedSleeps = new CopyOnWriteArrayList<>();
        SpellCasterModel fixedModel = new SpellCasterModel(
                (click, usesRightClick, isArcher) -> {}, delayMs -> fixedSleeps.add(delayMs), fixedTracker, clock::get);

        try {
            Assertions.assertTrue(fixedModel.queueClicks(List.of(CombatClickType.PRIMARY), false, 5, 7, 0));
            waitFor(() -> fixedSleeps.size() == 1, "Timed out waiting for the fixed spell to execute");

            Assertions.assertEquals(List.of(7), List.copyOf(fixedSleeps));
        } finally {
            fixedModel.shutdown();
        }
    }

    @Test
    public void adaptiveLagCorrectionUsesChannelSpecificHistoryForMixedCombat() throws Exception {
        AtomicLong clock = new AtomicLong(500L);
        SpellCasterLagCorrectionTracker tracker = new SpellCasterLagCorrectionTracker();
        populateMixedChannelSamples(tracker);
        List<Integer> adaptiveSleeps = new CopyOnWriteArrayList<>();
        SpellCasterModel model = new SpellCasterModel(
                (click, usesRightClick, isArcher) -> {}, delayMs -> adaptiveSleeps.add(delayMs), tracker, clock::get);

        try {
            Assertions.assertTrue(
                    model.queueClicks(List.of(CombatClickType.MELEE, CombatClickType.PRIMARY), false, 5, 7, 0, true));
            waitFor(() -> adaptiveSleeps.size() == 2, "Timed out waiting for the mixed adaptive sequence to execute");

            Assertions.assertEquals(List.of(9, 7), List.copyOf(adaptiveSleeps));
        } finally {
            model.shutdown();
        }
    }

    @Test
    public void adaptiveLagCorrectionDecaysAfterLagStabilizes() throws Exception {
        AtomicLong clock = new AtomicLong(200L);
        SpellCasterLagCorrectionTracker tracker = new SpellCasterLagCorrectionTracker();
        populateIncreasingSpellSamples(tracker);
        populateStableSpellSamples(tracker, 360L, 126L, 4);
        List<Integer> sleeps = new CopyOnWriteArrayList<>();
        SpellCasterModel model = new SpellCasterModel(
                (click, usesRightClick, isArcher) -> {}, delayMs -> sleeps.add(delayMs), tracker, clock::get);

        try {
            Assertions.assertTrue(model.queueClicks(List.of(CombatClickType.PRIMARY), false, 5, 7, 0, true));
            waitFor(() -> sleeps.size() == 1, "Timed out waiting for the stabilized adaptive spell to execute");
            Assertions.assertEquals(List.of(7), List.copyOf(sleeps));
        } finally {
            model.shutdown();
        }
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

    private static void populateMixedChannelSamples(SpellCasterLagCorrectionTracker tracker) {
        tracker.beginAdaptiveWindow(0L);
        tracker.onInputSent(CombatClickType.MELEE, 0L);
        tracker.onItemCooldownObserved(60L);
        tracker.onInputSent(CombatClickType.MELEE, 80L);
        tracker.onItemCooldownObserved(150L);
        tracker.onInputSent(CombatClickType.MELEE, 160L);
        tracker.onItemCooldownObserved(240L);

        tracker.onInputSent(CombatClickType.PRIMARY, 200L);
        tracker.onSpellProgressObserved(300L);
        tracker.onInputSent(CombatClickType.PRIMARY, 320L);
        tracker.onSpellProgressObserved(420L);
        tracker.onInputSent(CombatClickType.PRIMARY, 440L);
        tracker.onSpellProgressObserved(540L);
    }

    private static void populateStableSpellSamples(
            SpellCasterLagCorrectionTracker tracker, long firstSentAtMs, long lagMs, int sampleCount) {
        long sentAtMs = firstSentAtMs;
        for (int i = 0; i < sampleCount; i++) {
            tracker.onInputSent(CombatClickType.PRIMARY, sentAtMs);
            tracker.onSpellProgressObserved(sentAtMs + lagMs);
            sentAtMs += 120L;
        }
    }

    private static List<String> snapshot(List<String> events) {
        synchronized (events) {
            return List.copyOf(events);
        }
    }

    private static void waitFor(BooleanSupplier condition, String failureMessage) throws InterruptedException {
        long deadline = System.nanoTime() + TimeUnit.SECONDS.toNanos(2);
        while (System.nanoTime() < deadline) {
            if (condition.getAsBoolean()) {
                return;
            }

            Thread.sleep(10);
        }

        Assertions.fail(failureMessage);
    }
}
