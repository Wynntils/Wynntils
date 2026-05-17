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
import com.wynntils.mc.event.TickEvent;
import com.wynntils.models.npcdialogue.event.NpcDialogueUpdatedEvent;
import com.wynntils.models.worlds.event.WorldStateEvent;
import com.wynntils.utils.mc.McUtils;
import net.minecraft.client.player.LocalPlayer;
import net.minecraft.network.protocol.game.ServerboundPlayerInputPacket;
import net.minecraft.world.entity.player.Input;
import net.neoforged.bus.api.SubscribeEvent;

/**
 * Automatically sends short shift pulses for Wynncraft NPC dialogue.
 *
 * <p>Normal mode waits until the dialogue HUD exposes its shift-to-continue control. Skip Directly mode starts from the
 * first dialogue update so it can interrupt the text animation after the base delay. When the direct base delay is
 * smaller and less than MIN_DELAY_FOR_SHIFT_PULSE, it holds shift instead of sending repeated pulses.
 */
@ConfigCategory(Category.UTILITIES)
public class AutoProgressDialogueFeature extends Feature {
    private static final int SHIFT_RELEASE_DELAY_MS = 200;
    private static final int MIN_DELAY_FOR_SHIFT_PULSE = 50;

    @RegisterKeyBind
    private final KeyBind cancelAutoProgress =
            KeyBindDefinition.CANCEL_DIALOGUE_AUTO_PROGRESS.create(this::onKeyBindPressed);

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
    private boolean holdingDirectSkipShift = false;

    public AutoProgressDialogueFeature() {
        super(ProfileDefault.DISABLED);
    }

    @SubscribeEvent
    public void onNpcDialogueUpdate(NpcDialogueUpdatedEvent event) {
        if (!event.isDialoguePresent()) {
            onDialogueGone();
            return;
        }

        onDialogueUpdate(event);
    }

    private void onDialogueUpdate(NpcDialogueUpdatedEvent dialogueSegment) {
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

    private void onDirectDialogueUpdate(NpcDialogueUpdatedEvent dialogueSegment) {
        if (isDirectHoldMode()) {
            onDirectHoldDialogueUpdate(dialogueSegment);
            return;
        }

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

    private void onDirectHoldDialogueUpdate(NpcDialogueUpdatedEvent dialogueSegment) {
        String dialogueText = dialogueSegment.getDialogueText();
        lastDialogueText = dialogueText;

        if (dialogueSegment.hasChoices() || !dialogueSegment.requiresShift()) {
            cancelScheduledProgress();
            return;
        }

        if (holdingDirectSkipShift || progressAtMs > 0L) return;

        scheduledDialogueText = dialogueText;
        progressAtMs = System.currentTimeMillis() + getBaseDelayMs();
    }

    private void onDialogueGone() {
        clearDialogueState();
    }

    @SubscribeEvent
    public void onTick(TickEvent event) {
        if (!Models.WorldState.onWorld()) {
            clearDialogueState();
            return;
        }

        long now = System.currentTimeMillis();
        if (holdingDirectSkipShift && !isDirectHoldMode()) {
            stopDirectSkipHold();
        }

        if (progressAtMs > 0L && now >= progressAtMs) {
            String progressedDialogueText = scheduledDialogueText;
            progressAtMs = 0L;
            scheduledDialogueText = "";

            if (isDirectHoldMode()) {
                holdingDirectSkipShift = holdShiftForDirectSkip();
            } else {
                boolean shiftPulseSent = sendShiftPulse();
                if (shiftPulseSent) {
                    // Direct skip may need one press to reveal the full line and one more to advance it.
                    if (skipDirectly.get()) {
                        lastDirectSkipPressedText =
                                lastDialogueText.isBlank() ? progressedDialogueText : lastDialogueText;
                        directSkipPressesForDialogue++;
                    }

                    releaseShiftAtMs = now + SHIFT_RELEASE_DELAY_MS;
                    waitingToReleaseShift = true;
                }
            }
        }

        if (waitingToReleaseShift && now >= releaseShiftAtMs) {
            waitingToReleaseShift = false;
            releaseShiftAtMs = 0L;
            restoreShiftState();
        }
    }

    @SubscribeEvent
    public void onWorldStateChange(WorldStateEvent event) {
        clearDialogueState();
    }

    @Override
    public void onDisable() {
        clearDialogueState();
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

    private boolean isNewDirectDialogue(String dialogueText) {
        if (lastDialogueText.isBlank() || dialogueText.equals(lastDialogueText)) return false;

        // If the text only grows while typing or after the reveal press, keep treating it as the same dialogue.
        return !dialogueText.startsWith(lastDialogueText)
                && (lastDirectSkipPressedText.isBlank() || !dialogueText.startsWith(lastDirectSkipPressedText));
    }

    private boolean sendShiftPulse() {
        LocalPlayer player = McUtils.player();
        if (player == null || isPlayerSneaking(player)) return false;

        sendShift(player, true);
        return true;
    }

    private boolean holdShiftForDirectSkip() {
        LocalPlayer player = McUtils.player();
        if (player == null) return false;

        if (!isPlayerSneaking(player)) {
            sendShift(player, true);
        }

        return true;
    }

    private void stopDirectSkipHold() {
        if (!holdingDirectSkipShift) return;

        holdingDirectSkipShift = false;
        restoreShiftState();
    }

    private void restoreShiftState() {
        LocalPlayer player = McUtils.player();
        // If the player is already sneaking, don't send a release packet as that would interfere with their intended
        // input.
        if (player == null || isPlayerSneaking(player)) return;

        sendShift(player, isPlayerSneaking(player));
    }

    private boolean isPlayerSneaking(LocalPlayer player) {
        Input input = getLastSentInput(player);

        // Respect both the current key/toggle state and Minecraft's last sent input so auto progress never releases a
        // real player sneak.
        return player.isShiftKeyDown() || McUtils.options().keyShift.isDown() || input.shift();
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
            restoreShiftState();
        }

        stopDirectSkipHold();
    }

    private void onKeyBindPressed() {
        clearDialogueState();
    }
}
