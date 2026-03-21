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
import java.util.concurrent.CountDownLatch;
import java.util.concurrent.TimeUnit;
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
            Assertions.assertTrue(model.queueClicks(List.of(CombatClickType.PRIMARY), false, 1, 1, 0));
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
    public void acceptsNextSpellAfterCurrentSequenceFinishes() throws Exception {
        List<String> events = Collections.synchronizedList(new ArrayList<>());
        SpellCasterModel model = new SpellCasterModel(
                (click, usesRightClick, isArcher) -> events.add(click.name()),
                delayMs -> events.add("sleep:" + delayMs));

        try {
            Assertions.assertTrue(model.queueClicks(List.of(CombatClickType.PRIMARY), false, 0, 0, 25));
            waitFor(() -> snapshot(events).size() == 2, "Timed out waiting for the first spell to finish");
            Assertions.assertTrue(model.queueClicks(List.of(CombatClickType.PRIMARY), false, 0, 0, 0));
            waitFor(() -> snapshot(events).size() == 3, "Timed out waiting for the second spell to execute");

            Assertions.assertEquals(List.of("PRIMARY", "sleep:25", "PRIMARY"), snapshot(events));
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
            Assertions.assertTrue(model.queueClicks(List.of(CombatClickType.PRIMARY), false, 25, 0, 50));

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
