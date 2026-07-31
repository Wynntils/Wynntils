/*
 * Copyright © Wynntils 2026.
 * This file is released under LGPLv3. See LICENSE for full license details.
 */
package com.wynntils.models.spells;

import com.wynntils.core.WynntilsMod;
import com.wynntils.core.components.Model;
import com.wynntils.mc.event.ChangeCarriedItemEvent;
import com.wynntils.mc.event.CooldownUpdateEvent;
import com.wynntils.mc.event.TickEvent;
import com.wynntils.models.spells.event.SpellEvent;
import com.wynntils.models.spells.type.CombatClickType;
import com.wynntils.models.spells.type.SpellDirection;
import com.wynntils.models.worlds.event.WorldStateEvent;
import com.wynntils.utils.mc.McUtils;
import com.wynntils.utils.mc.MouseUtils;
import java.util.List;
import java.util.concurrent.ArrayBlockingQueue;
import java.util.concurrent.BlockingQueue;
import java.util.function.LongSupplier;
import net.minecraft.world.InteractionHand;
import net.neoforged.bus.api.SubscribeEvent;

public final class SpellCasterModel extends Model {
    private static final DirectClickTransport DEFAULT_DIRECT_CLICK_TRANSPORT = new MouseDirectClickTransport();

    private final BlockingQueue<QueuedSequence> queuedSequences;
    private final Object stateLock = new Object();
    private final ClickSender clickSender;
    private final DelayStrategy delayStrategy;
    private final BeforeDispatchHook beforeDispatchHook;
    private final SpellCasterLagCorrectionTracker lagCorrectionTracker;
    private final LongSupplier currentTimeMsSupplier;
    private final Thread workerThread;

    private long generation = 0L;
    private volatile boolean processing = false;
    private volatile boolean sendingInputs = false;
    private volatile boolean shuttingDown = false;
    private Runnable idleListener = () -> {};

    public SpellCasterModel() {
        this(
                DEFAULT_DIRECT_CLICK_TRANSPORT,
                Thread::sleep,
                click -> {},
                new SpellCasterLagCorrectionTracker(),
                System::currentTimeMillis);
    }

    // Package-private for unit tests that need to verify the production click transport.
    SpellCasterModel(DirectClickTransport directClickTransport, DelayStrategy delayStrategy) {
        this(
                directClickTransport,
                delayStrategy,
                click -> {},
                new SpellCasterLagCorrectionTracker(),
                System::currentTimeMillis);
    }

    // Package-private for unit tests that need to verify the production transport with controlled timing.
    SpellCasterModel(
            DirectClickTransport directClickTransport,
            DelayStrategy delayStrategy,
            SpellCasterLagCorrectionTracker lagCorrectionTracker,
            LongSupplier currentTimeMsSupplier) {
        this(directClickTransport, delayStrategy, click -> {}, lagCorrectionTracker, currentTimeMsSupplier);
    }

    // Package-private for unit tests that need to pause precisely before a production click is dispatched.
    SpellCasterModel(
            DirectClickTransport directClickTransport,
            DelayStrategy delayStrategy,
            BeforeDispatchHook beforeDispatchHook,
            SpellCasterLagCorrectionTracker lagCorrectionTracker,
            LongSupplier currentTimeMsSupplier) {
        this(
                (click, usesRightClick, isArcher) -> dispatchClick(directClickTransport, usesRightClick),
                delayStrategy,
                beforeDispatchHook,
                lagCorrectionTracker,
                currentTimeMsSupplier);
    }

    // Package-private for unit tests that need to stub packet sending and timing.
    SpellCasterModel(ClickSender clickSender, DelayStrategy delayStrategy) {
        this(clickSender, delayStrategy, click -> {}, new SpellCasterLagCorrectionTracker(), System::currentTimeMillis);
    }

    // Package-private for unit tests that need to control lag-correction state and timing.
    SpellCasterModel(
            ClickSender clickSender,
            DelayStrategy delayStrategy,
            SpellCasterLagCorrectionTracker lagCorrectionTracker,
            LongSupplier currentTimeMsSupplier) {
        this(clickSender, delayStrategy, click -> {}, lagCorrectionTracker, currentTimeMsSupplier);
    }

    // Package-private for unit tests that need to pause precisely before a click is dispatched.
    SpellCasterModel(
            ClickSender clickSender,
            DelayStrategy delayStrategy,
            BeforeDispatchHook beforeDispatchHook,
            SpellCasterLagCorrectionTracker lagCorrectionTracker,
            LongSupplier currentTimeMsSupplier) {
        super(List.of());

        this.queuedSequences = new ArrayBlockingQueue<>(1);
        this.clickSender = clickSender;
        this.delayStrategy = delayStrategy;
        this.beforeDispatchHook = beforeDispatchHook;
        this.lagCorrectionTracker = lagCorrectionTracker;
        this.currentTimeMsSupplier = currentTimeMsSupplier;
        workerThread = new Thread(this::runWorker, "Wynntils-SpellCaster");
        workerThread.setDaemon(true);
        workerThread.start();
    }

    public boolean queueDirections(
            List<SpellDirection> directions, boolean isArcher, int leftDelayMs, int rightDelayMs, int cooldownMs) {
        return queueDirections(directions, isArcher, leftDelayMs, rightDelayMs, cooldownMs, false);
    }

    public boolean queueDirections(
            List<SpellDirection> directions,
            boolean isArcher,
            int leftDelayMs,
            int rightDelayMs,
            int cooldownMs,
            boolean adaptiveLagCorrectionEnabled) {
        List<CombatClickType> clicks = directions.stream()
                .map(direction ->
                        direction == SpellDirection.RIGHT ? CombatClickType.PRIMARY : CombatClickType.SECONDARY)
                .toList();
        return queueClicks(clicks, isArcher, leftDelayMs, rightDelayMs, cooldownMs, adaptiveLagCorrectionEnabled);
    }

    public boolean queueClicks(
            List<CombatClickType> clicks, boolean isArcher, int leftDelayMs, int rightDelayMs, int cooldownMs) {
        return queueClicks(clicks, isArcher, leftDelayMs, rightDelayMs, cooldownMs, false);
    }

    public boolean queueClicks(
            List<CombatClickType> clicks,
            boolean isArcher,
            int leftDelayMs,
            int rightDelayMs,
            int cooldownMs,
            boolean adaptiveLagCorrectionEnabled) {
        if (clicks.isEmpty()) return false;

        List<CombatClickType> copiedClicks = List.copyOf(clicks);
        int normalizedLeftDelayMs = Math.max(leftDelayMs, 0);
        int normalizedRightDelayMs = Math.max(rightDelayMs, 0);
        int normalizedCooldownMs = Math.max(cooldownMs, 0);

        synchronized (stateLock) {
            if (shuttingDown || processing || !queuedSequences.isEmpty()) return false;

            return queuedSequences.offer(new QueuedSequence(
                    copiedClicks,
                    isArcher,
                    normalizedLeftDelayMs,
                    normalizedRightDelayMs,
                    normalizedCooldownMs,
                    adaptiveLagCorrectionEnabled,
                    generation));
        }
    }

    public boolean isBusy() {
        return processing || !queuedSequences.isEmpty();
    }

    public boolean isSendingInputs() {
        return sendingInputs;
    }

    public void clear() {
        synchronized (stateLock) {
            generation++;
            processing = false;
            sendingInputs = false;
            queuedSequences.clear();
        }

        lagCorrectionTracker.reset();
        workerThread.interrupt();
    }

    public void setIdleListener(Runnable idleListener) {
        synchronized (stateLock) {
            this.idleListener = idleListener != null ? idleListener : () -> {};
        }
    }

    void shutdown() {
        synchronized (stateLock) {
            shuttingDown = true;
            generation++;
            processing = false;
            sendingInputs = false;
            queuedSequences.clear();
        }

        workerThread.interrupt();
    }

    @SubscribeEvent
    public void onHeldItemChange(ChangeCarriedItemEvent event) {
        clear();
    }

    @SubscribeEvent
    public void onWorldStateChange(WorldStateEvent event) {
        clear();
    }

    @SubscribeEvent
    public void onSpellPartial(SpellEvent.Partial event) {
        lagCorrectionTracker.onSpellProgressObserved(currentTimeMs());
    }

    @SubscribeEvent
    public void onCooldownUpdate(CooldownUpdateEvent event) {
        if (!shouldObserveItemCooldown(
                McUtils.player() != null,
                McUtils.player() != null
                        && event.getCooldownGroup()
                                .equals(McUtils.player()
                                        .getCooldowns()
                                        .getCooldownGroup(McUtils.player().getItemInHand(InteractionHand.MAIN_HAND))),
                event.getDuration())) {
            return;
        }

        lagCorrectionTracker.onItemCooldownObserved(currentTimeMs());
    }

    static boolean shouldObserveItemCooldown(boolean playerPresent, boolean sameCooldownGroup, int duration) {
        return playerPresent && sameCooldownGroup && duration > 0;
    }

    @SubscribeEvent
    public void onTick(TickEvent event) {
        lagCorrectionTracker.onTick(currentTimeMs(), isSendingInputs());
    }

    private void runWorker() {
        while (!isShuttingDown()) {
            try {
                QueuedSequence sequence = queuedSequences.take();
                if (!beginProcessing(sequence)) continue;

                try {
                    execute(sequence);
                } finally {
                    finishProcessing(sequence);
                }
            } catch (InterruptedException ignored) {
                // Interrupts are used to abort active sequences or wake the worker after a reset.
                if (isShuttingDown()) {
                    return;
                }
            } catch (Throwable throwable) {
                WynntilsMod.error("Spell caster worker crashed", throwable);
            }
        }
    }

    private boolean beginProcessing(QueuedSequence sequence) {
        synchronized (stateLock) {
            if (sequence.generation != generation || shuttingDown) return false;

            processing = true;
            sendingInputs = true;
            if (sequence.adaptiveLagCorrectionEnabled) {
                lagCorrectionTracker.beginAdaptiveWindow(currentTimeMs());
            }
            return true;
        }
    }

    private void finishProcessing(QueuedSequence sequence) {
        Runnable idleCallback = null;
        long callbackGeneration = -1L;

        synchronized (stateLock) {
            sendingInputs = false;
            if (sequence.generation == generation) {
                processing = false;
                if (!shuttingDown && queuedSequences.isEmpty()) {
                    idleCallback = idleListener;
                    callbackGeneration = generation;
                }
            }
        }

        if (idleCallback != null) {
            scheduleIdleCallback(callbackGeneration, idleCallback);
        }
    }

    private void execute(QueuedSequence sequence) throws InterruptedException {
        for (int i = 0; i < sequence.clicks.size(); i++) {
            CombatClickType click = sequence.clicks.get(i);
            if (!isCurrent(sequence)) return;

            boolean usesRightClick = click.usesRightClick(sequence.isArcher);
            beforeDispatchHook.beforeDispatch(click);
            if (!sendClick(sequence, click, usesRightClick, sequence.isArcher)) {
                return;
            }
            if (sequence.adaptiveLagCorrectionEnabled) {
                lagCorrectionTracker.onInputSent(click, currentTimeMs());
            }
            // Quick Cast delays are configured as "wait after sending this click type",
            // including the final click before the sequence is considered settled.
            int delayMs = usesRightClick ? sequence.rightDelayMs : sequence.leftDelayMs;
            if (sequence.adaptiveLagCorrectionEnabled) {
                delayMs += lagCorrectionTracker.computeExtraDelayMs(click, delayMs);
            }
            if (delayMs > 0) {
                delayStrategy.sleep(delayMs);
            }
        }

        finishSendingInputs(sequence);
        if (!isCurrent(sequence) || sequence.cooldownMs <= 0) return;

        delayStrategy.sleep(sequence.cooldownMs);
    }

    private boolean sendClick(
            QueuedSequence sequence, CombatClickType click, boolean usesRightClick, boolean isArcher) {
        synchronized (stateLock) {
            if (sequence.generation != generation || shuttingDown) {
                return false;
            }

            clickSender.send(click, usesRightClick, isArcher);
            return true;
        }
    }

    private boolean isCurrent(QueuedSequence sequence) {
        synchronized (stateLock) {
            return sequence.generation == generation;
        }
    }

    private boolean isShuttingDown() {
        return shuttingDown;
    }

    private void finishSendingInputs(QueuedSequence sequence) {
        synchronized (stateLock) {
            if (sequence.generation != generation || !processing) return;

            sendingInputs = false;
        }
    }

    private void scheduleIdleCallback(long callbackGeneration, Runnable idleCallback) {
        if (McUtils.mc() == null) return;

        Runnable guardedCallback = () -> {
            synchronized (stateLock) {
                if (callbackGeneration != generation || processing || !queuedSequences.isEmpty() || shuttingDown) {
                    return;
                }
            }

            idleCallback.run();
        };

        if (McUtils.mc().isSameThread()) {
            guardedCallback.run();
        } else {
            McUtils.mc().execute(guardedCallback);
        }
    }

    private static void dispatchClick(DirectClickTransport directClickTransport, boolean usesRightClick) {
        if (usesRightClick) {
            directClickTransport.sendDirectRightClickInput();
        } else {
            directClickTransport.sendLeftClickInput();
        }
    }

    private long currentTimeMs() {
        return currentTimeMsSupplier.getAsLong();
    }

    private record QueuedSequence(
            List<CombatClickType> clicks,
            boolean isArcher,
            int leftDelayMs,
            int rightDelayMs,
            int cooldownMs,
            boolean adaptiveLagCorrectionEnabled,
            long generation) {}

    interface DirectClickTransport {
        void sendDirectRightClickInput();

        void sendLeftClickInput();
    }

    private static final class MouseDirectClickTransport implements DirectClickTransport {
        @Override
        public void sendDirectRightClickInput() {
            MouseUtils.sendDirectRightClickInput();
        }

        @Override
        public void sendLeftClickInput() {
            MouseUtils.sendLeftClickInput();
        }
    }

    @FunctionalInterface
    interface ClickSender {
        void send(CombatClickType click, boolean usesRightClick, boolean isArcher);
    }

    @FunctionalInterface
    interface DelayStrategy {
        void sleep(int delayMs) throws InterruptedException;
    }

    @FunctionalInterface
    interface BeforeDispatchHook {
        void beforeDispatch(CombatClickType click) throws InterruptedException;
    }
}
