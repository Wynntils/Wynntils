/*
 * Copyright © Wynntils 2026.
 * This file is released under LGPLv3. See LICENSE for full license details.
 */
package com.wynntils.features.utilities;

import com.wynntils.core.components.Models;
import com.wynntils.core.consumers.features.Feature;
import com.wynntils.core.consumers.features.ProfileDefault;
import com.wynntils.core.consumers.features.properties.RegisterKeyBind;
import com.wynntils.core.keybinds.KeyBind;
import com.wynntils.core.keybinds.KeyBindDefinition;
import com.wynntils.core.persisted.Persisted;
import com.wynntils.core.persisted.config.Category;
import com.wynntils.core.persisted.config.Config;
import com.wynntils.core.persisted.config.ConfigCategory;
import com.wynntils.handlers.actionbar.event.ActionBarUpdatedEvent;
import com.wynntils.handlers.actionbar.segments.DialogueSegment;
import com.wynntils.mc.event.TickEvent;
import com.wynntils.models.worlds.event.WorldStateEvent;
import com.wynntils.utils.mc.McUtils;
import net.minecraft.network.protocol.game.ServerboundPlayerInputPacket;
import net.minecraft.world.entity.player.Input;
import net.neoforged.bus.api.SubscribeEvent;

/**
 * Automatically sends short shift pulses for Wynncraft NPC dialogue.
 *
 * <p>Normal mode waits until the dialogue HUD exposes its shift-to-continue control. Skip Directly mode starts from the
 * first dialogue update so it can interrupt the text animation after the base delay.
 */
@ConfigCategory(Category.UTILITIES)
public class AutoProgressDialogueFeature extends Feature {
    private static final int SHIFT_RELEASE_DELAY_MS = 200;

    @RegisterKeyBind
    private final KeyBind cancelAutoProgress =
            KeyBindDefinition.CANCEL_DIALOGUE_AUTO_PROGRESS.create(this::cancelPendingProgress);

    @Persisted
    private final Config<Boolean> skipDirectly = new Config<>(false);

    @Persisted
    private final Config<Integer> baseDelayMs = new Config<>(500);

    @Persisted
    private final Config<Integer> delayPerWordMs = new Config<>(50);

    private String lastDialogueText = "";
    private String lastDirectSkipPressedText = "";
    private String scheduledDialogueText = "";
    private int directSkipPressesForDialogue = 0;
    private long progressAtMs = 0L;
    private long releaseShiftAtMs = 0L;
    private boolean waitingToReleaseShift = false;

    public AutoProgressDialogueFeature() {
        super(ProfileDefault.DISABLED);
    }

    @SubscribeEvent
    public void onActionBarUpdate(ActionBarUpdatedEvent event) {
        event.runIfPresentOrElse(DialogueSegment.class, this::onDialogueUpdate, this::onDialogueGone);
    }

    private void onDialogueUpdate(DialogueSegment dialogueSegment) {
        if (skipDirectly.get()) {
            onDirectDialogueUpdate(dialogueSegment);
            return;
        }

        String dialogueText = dialogueSegment.getDialogueText();
        if (!dialogueText.equals(lastDialogueText)) {
            cancelScheduledProgress();
            lastDialogueText = dialogueText;
        }

        if (dialogueSegment.hasChoices()) {
            cancelScheduledProgress();
            return;
        }

        if (!dialogueSegment.requiresShift()) {
            cancelScheduledProgress();
            return;
        }

        if (dialogueText.equals(scheduledDialogueText)) return;

        scheduledDialogueText = dialogueText;
        progressAtMs = System.currentTimeMillis() + getProgressDelayMs(dialogueText);
    }

    private void onDirectDialogueUpdate(DialogueSegment dialogueSegment) {
        String dialogueText = dialogueSegment.getDialogueText();
        if (dialogueSegment.hasChoices()) {
            cancelScheduledProgress();
            lastDialogueText = dialogueText;
            return;
        }

        if (directSkipPressesForDialogue > 0 && isNewDirectDialogue(dialogueText)) {
            directSkipPressesForDialogue = 0;
            lastDirectSkipPressedText = "";
        }

        lastDialogueText = dialogueText;

        if (directSkipPressesForDialogue >= 2) return;
        // Text can type, reflow, or gain spacer glyphs; direct mode must not restart its first timer for those updates.
        if (progressAtMs > 0L) return;

        scheduledDialogueText = dialogueText;
        progressAtMs = System.currentTimeMillis() + getProgressDelayMs(dialogueText);
    }

    private void onDialogueGone() {
        clearDialogueState();
    }

    @SubscribeEvent
    public void onTick(TickEvent event) {
        if (!Models.WorldState.onWorld()) {
            cancelPendingProgress();
            return;
        }

        long now = System.currentTimeMillis();
        if (progressAtMs > 0L && now >= progressAtMs) {
            String progressedDialogueText = scheduledDialogueText;
            progressAtMs = 0L;
            scheduledDialogueText = "";

            sendShiftPulse();
            // Direct skip may need one press to reveal the full line and one more to advance it.
            if (skipDirectly.get()) {
                lastDirectSkipPressedText = lastDialogueText.isBlank() ? progressedDialogueText : lastDialogueText;
                directSkipPressesForDialogue++;
            }

            releaseShiftAtMs = now + SHIFT_RELEASE_DELAY_MS;
            waitingToReleaseShift = true;
        }

        if (waitingToReleaseShift && now >= releaseShiftAtMs) {
            waitingToReleaseShift = false;
            releaseShiftAtMs = 0L;
            sendShift(false);
        }
    }

    @SubscribeEvent
    public void onWorldStateChange(WorldStateEvent event) {
        cancelPendingProgress();
    }

    @Override
    public void onDisable() {
        cancelPendingProgress();
    }

    private long getProgressDelayMs(String dialogueText) {
        int baseDelay = Math.max(0, baseDelayMs.get());
        if (skipDirectly.get()) return baseDelay;

        int words = dialogueText.isBlank() ? 0 : dialogueText.trim().split("\\s+").length;
        int wordDelay = Math.max(0, delayPerWordMs.get());

        return baseDelay + ((long) words * wordDelay);
    }

    private boolean isNewDirectDialogue(String dialogueText) {
        if (lastDialogueText.isBlank() || dialogueText.equals(lastDialogueText)) return false;

        // If the text only grows while typing or after the reveal press, keep treating it as the same dialogue.
        return !dialogueText.startsWith(lastDialogueText)
                && (lastDirectSkipPressedText.isBlank() || !dialogueText.startsWith(lastDirectSkipPressedText));
    }

    private void sendShiftPulse() {
        sendShift(false);
        sendShift(true);
    }

    private void sendShift(boolean shift) {
        if (McUtils.player() == null) return;

        Input input = McUtils.player().getLastSentInput();
        if (input == null) {
            input = Input.EMPTY;
        }

        McUtils.sendPacket(new ServerboundPlayerInputPacket(new Input(
                input.forward(), input.backward(), input.left(), input.right(), input.jump(), shift, input.sprint())));
    }

    private void cancelPendingProgress() {
        clearDialogueState();
    }

    private void clearDialogueState() {
        cancelScheduledProgress();
        lastDialogueText = "";
        lastDirectSkipPressedText = "";
        directSkipPressesForDialogue = 0;
    }

    private void cancelScheduledProgress() {
        progressAtMs = 0L;
        scheduledDialogueText = "";

        if (waitingToReleaseShift) {
            waitingToReleaseShift = false;
            releaseShiftAtMs = 0L;
            sendShift(false);
        }
    }
}
