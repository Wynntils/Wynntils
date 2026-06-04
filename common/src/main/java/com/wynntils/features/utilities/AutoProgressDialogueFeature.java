/*
 * Copyright © Wynntils 2026.
 * This file is released under LGPLv3. See LICENSE for full license details.
 */
package com.wynntils.features.utilities;

import com.wynntils.core.components.Managers;
import com.wynntils.core.components.Models;
import com.wynntils.core.components.Services;
import com.wynntils.core.consumers.features.Feature;
import com.wynntils.core.consumers.features.ProfileDefault;
import com.wynntils.core.consumers.features.properties.RegisterKeyBind;
import com.wynntils.core.consumers.overlays.Overlay;
import com.wynntils.core.consumers.overlays.annotations.RegisterOverlay;
import com.wynntils.core.keybinds.KeyBind;
import com.wynntils.core.keybinds.KeyBindDefinition;
import com.wynntils.core.mod.TickSchedulerManager;
import com.wynntils.core.persisted.Persisted;
import com.wynntils.core.persisted.config.Category;
import com.wynntils.core.persisted.config.Config;
import com.wynntils.core.persisted.config.ConfigCategory;
import com.wynntils.mc.event.TickEvent;
import com.wynntils.models.dialogue.event.NpcDialogueEvent;
import com.wynntils.models.worlds.event.WorldStateEvent;
import com.wynntils.overlays.AutoProgressDialogueOverlay;
import com.wynntils.utils.mc.McUtils;
import com.wynntils.utils.type.RenderElementType;
import net.minecraft.client.player.LocalPlayer;
import net.minecraft.network.protocol.game.ServerboundPlayerInputPacket;
import net.minecraft.sounds.SoundEvents;
import net.minecraft.world.entity.player.Input;
import net.neoforged.bus.api.SubscribeEvent;

/**
 * Automatically sends short shift pulses for Wynncraft NPC dialogue.
 *
 * <p>Normal mode waits until the dialogue HUD exposes its shift-to-continue control. Skip Directly mode starts from the
 * first dialogue update so it can interrupt the text animation after the base delay. When the direct base delay is
 * smaller than one client tick, it holds shift instead of sending repeated pulses.
 */
@ConfigCategory(Category.UTILITIES)
public class AutoProgressDialogueFeature extends Feature {
    private static final int SHIFT_RELEASE_DELAY_MS = 200;
    private static final int MIN_DELAY_FOR_SHIFT_PULSE = 100;

    @RegisterKeyBind
    private final KeyBind toggleAutoProgress =
            KeyBindDefinition.TOGGLE_DIALOGUE_AUTO_PROGRESS.create(this::toggleAutoProgress);

    @RegisterOverlay(renderType = RenderElementType.CHAT)
    private final Overlay autoProgressDialogueOverlay =
            new AutoProgressDialogueOverlay(this::shouldShowAutoProgressOverlay);

    @Persisted
    private final Config<Boolean> autoProgressToggle = new Config<>(true);

    @Persisted
    private final Config<Boolean> skipDirectly = new Config<>(false);

    @Persisted
    private final Config<Integer> baseDelayMs = new Config<>(500);

    @Persisted
    private final Config<Integer> delayPerWordMs = new Config<>(50);

    private long progressAtMs = 0L;
    private long releaseShiftAtMs = 0L;
    private boolean syntheticShiftDown = false;
    private boolean skipAttempted = false;
    private TickSchedulerManager.ScheduledTask scheduledSkipTask = null;

    public AutoProgressDialogueFeature() {
        super(ProfileDefault.DISABLED);
    }

    @SubscribeEvent
    public void onNpcDialogueStarted(NpcDialogueEvent.Started event) {
        if (!autoProgressToggle.get() || event.hasChoices()) {
            clearProgressState();
            return;
        }

        if (isDirectHoldActive()) return;

        clearProgressState();

        if (skipDirectly.get()) {
            scheduleProgress(getBaseDelayMs());
        }
    }

    @SubscribeEvent
    public void onNpcDialogueUpdate(NpcDialogueEvent.Updated event) {
        if (!autoProgressToggle.get() || event.hasChoices()) {
            clearProgressState();
            return;
        }

        scheduleRetryIfSkipDidNotProgress(event.getDialogueText());
    }

    @SubscribeEvent
    public void onNpcDialogueFinished(NpcDialogueEvent.Finished event) {
        if (!autoProgressToggle.get() || event.hasChoices()) {
            clearProgressState();
            return;
        }

        skipDialogue(event.getDialogueText());
    }

    private void skipDialogue(String dialogueText) {
        cancelScheduledSkipRetry();
        if (isDirectHoldActive()) return;

        if (skipDirectly.get()) {
            scheduleProgress(getBaseDelayMs());
        } else {
            scheduleProgress(getProgressDelayMs(dialogueText));
        }
    }

    @SubscribeEvent
    public void onNpcDialogueEnded(NpcDialogueEvent.Ended event) {
        if (isDirectHoldActive() && !event.hasChoices()) {
            // Dialogue line transitions post Ended then Started in the same tick; keep the hold through that handoff.
            Managers.TickScheduler.scheduleNextTick(() -> {
                if (isDirectHoldActive() && !Models.Dialogue.isDialoguePresent()) {
                    clearProgressState();
                }
            });
            return;
        }

        clearProgressState();
    }

    @SubscribeEvent
    public void onTick(TickEvent event) {
        if (!Models.WorldState.onWorld()) {
            clearProgressState();
            return;
        }

        if (!autoProgressToggle.get()) {
            clearProgressState();
            return;
        }

        long now = System.currentTimeMillis();

        if (progressAtMs > 0L && now >= progressAtMs) {
            progressAtMs = 0L;

            if (isDirectHoldMode()) {
                if (!holdShiftForDirectSkip()) {
                    scheduleProgress(getBaseDelayMs());
                }
            } else {
                boolean shiftPulseSent = sendShiftPulse();
                if (shiftPulseSent) {
                    releaseShiftAtMs = now + SHIFT_RELEASE_DELAY_MS;
                } else {
                    scheduleProgress(getBaseDelayMs());
                }
            }
        }

        if (releaseShiftAtMs > 0L && now >= releaseShiftAtMs) {
            releaseShiftAtMs = 0L;
            restoreShiftState();
        }
    }

    @SubscribeEvent
    public void onWorldStateChange(WorldStateEvent event) {
        clearProgressState();
    }

    @Override
    public void onDisable() {
        clearProgressState();
    }

    private long getProgressDelayMs(String dialogueText) {
        int baseDelay = getBaseDelayMs();
        if (skipDirectly.get()) return baseDelay;

        int words = dialogueText.isBlank() ? 0 : dialogueText.trim().split("\\s+").length;
        int wordDelay = Math.max(0, delayPerWordMs.get());

        return baseDelay + ((long) words * wordDelay);
    }

    private int getBaseDelayMs() {
        return Math.max(0, baseDelayMs.get());
    }

    private boolean isDirectHoldMode() {
        return skipDirectly.get() && getBaseDelayMs() <= MIN_DELAY_FOR_SHIFT_PULSE;
    }

    private boolean sendShiftPulse() {
        LocalPlayer player = McUtils.player();
        if (player == null || isPlayerPressingShift(player)) return false;

        // If the server still sees a released user shift as held, release first and retry on the next scheduled pulse.
        if (isServerShiftDown(player) && !syntheticShiftDown) {
            sendShift(player, false);
            return false;
        }

        sendShift(player, true);
        syntheticShiftDown = true;
        return true;
    }

    private boolean holdShiftForDirectSkip() {
        LocalPlayer player = McUtils.player();
        if (player == null) return false;

        if (syntheticShiftDown) return true;

        if (isPlayerPressingShift(player)) return true;

        if (isServerShiftDown(player) && !syntheticShiftDown) {
            sendShift(player, false);
            return false;
        }

        if (!isPlayerPressingShift(player) && !isServerShiftDown(player)) {
            sendShift(player, true);
            syntheticShiftDown = true;
        }

        return true;
    }

    private void restoreShiftState() {
        LocalPlayer player = McUtils.player();
        if (player == null) {
            syntheticShiftDown = false;
            return;
        }

        // Release only auto-owned shift. A real held shift keybind remains untouched.
        if (syntheticShiftDown && !isPlayerPressingShift(player)) {
            sendShift(player, false);
        }

        syntheticShiftDown = false;
    }

    private boolean isPlayerPressingShift(LocalPlayer player) {
        return player.isShiftKeyDown() || McUtils.options().keyShift.isDown();
    }

    private boolean isServerShiftDown(LocalPlayer player) {
        return getLastSentInput(player).shift();
    }

    private void sendShift(LocalPlayer player, boolean shift) {
        Input input = getLastSentInput(player);

        McUtils.sendPacket(new ServerboundPlayerInputPacket(new Input(
                input.forward(), input.backward(), input.left(), input.right(), input.jump(), shift, input.sprint())));
    }

    private Input getLastSentInput(LocalPlayer player) {
        Input input = player.getLastSentInput();
        if (input == null) {
            input = Input.EMPTY;
        }

        return input;
    }

    private void clearProgressState() {
        progressAtMs = 0L;
        skipAttempted = false;
        releaseShiftAtMs = 0L;

        cancelScheduledSkipRetry();

        restoreShiftState();
    }

    private void scheduleProgress(long delayMs) {
        if (progressAtMs > 0L || releaseShiftAtMs > 0L || isDirectHoldActive()) return;

        skipAttempted = true;
        progressAtMs = System.currentTimeMillis() + delayMs;
    }

    private void scheduleRetryIfSkipDidNotProgress(String dialogueText) {
        if (!skipAttempted
                || releaseShiftAtMs > 0L
                || progressAtMs > 0L
                || scheduledSkipTask != null
                || isDirectHoldActive()) {
            return;
        }

        LocalPlayer player = McUtils.player();
        if (player == null || isPlayerPressingShift(player) || isServerShiftDown(player)) return;

        scheduledSkipTask = Managers.TickScheduler.scheduleLater(
                () -> {
                    scheduledSkipTask = null;

                    if (!autoProgressToggle.get()
                            || !Models.WorldState.onWorld()
                            || !Models.Dialogue.isDialoguePresent()) {
                        return;
                    }

                    skipDialogue(dialogueText);
                },
                getPingDelayTicks());
    }

    private int getPingDelayTicks() {
        return Math.max(2, Services.Ping.getPing() / 50 + 2);
    }

    private boolean isDirectHoldActive() {
        return isDirectHoldMode() && syntheticShiftDown;
    }

    private void cancelScheduledSkipRetry() {
        if (scheduledSkipTask == null) return;

        Managers.TickScheduler.cancel(scheduledSkipTask);
        scheduledSkipTask = null;
    }

    private void toggleAutoProgress() {
        autoProgressToggle.store(!autoProgressToggle.get());
        autoProgressToggle.touched();
        McUtils.playSoundMaster(SoundEvents.UI_BUTTON_CLICK.value());

        if (!autoProgressToggle.get()) {
            clearProgressState();
        }
    }

    private boolean shouldShowAutoProgressOverlay() {
        return autoProgressToggle.get() && Models.Dialogue.isDialoguePresent();
    }
}
