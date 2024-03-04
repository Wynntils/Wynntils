/*
 * Copyright © Wynntils 2022-2024.
 * This file is released under LGPLv3. See LICENSE for full license details.
 */
package com.wynntils.features.overlays;

import com.wynntils.core.components.Managers;
import com.wynntils.core.components.Models;
import com.wynntils.core.consumers.features.Feature;
import com.wynntils.core.consumers.features.properties.RegisterKeyBind;
import com.wynntils.core.consumers.overlays.annotations.OverlayInfo;
import com.wynntils.core.keybinds.KeyBind;
import com.wynntils.core.notifications.MessageContainer;
import com.wynntils.core.persisted.Persisted;
import com.wynntils.core.persisted.config.Category;
import com.wynntils.core.persisted.config.Config;
import com.wynntils.core.persisted.config.ConfigCategory;
import com.wynntils.core.text.StyledText;
import com.wynntils.handlers.chat.event.NpcDialogEvent;
import com.wynntils.handlers.chat.type.NpcDialogueType;
import com.wynntils.mc.event.RenderEvent;
import com.wynntils.mc.event.TickEvent;
import com.wynntils.models.worlds.event.WorldStateEvent;
import com.wynntils.overlays.NpcDialogueOverlay;
import com.wynntils.utils.mc.McUtils;
import java.util.ArrayList;
import java.util.List;
import java.util.concurrent.Executors;
import java.util.concurrent.ScheduledExecutorService;
import java.util.concurrent.ScheduledFuture;
import java.util.concurrent.TimeUnit;
import net.minecraft.ChatFormatting;
import net.minecraft.network.chat.Component;
import net.minecraft.network.protocol.game.ServerboundPlayerCommandPacket;
import net.minecraftforge.eventbus.api.SubscribeEvent;
import org.lwjgl.glfw.GLFW;

/**
 * Feature for handling NPC dialogues.
 * It is responsible for handling the dialogue auto progressing.
 * <p>
 * There are two ways this feature works:
 * <ol>
 * <li>If the NPC Dialogue overlay is enabled, all dialogues will be displayed in the overlay.</li>
 * <li>If the NPC Dialogue overlay is disabled, the dialogues will be displayed in text chat.</li>
 * </ol>
 * If the feature is disabled, we stop all special processing for chat screens in ChatHandler.
 */
@ConfigCategory(Category.OVERLAYS)
public class NpcDialogueFeature extends Feature {
    // This is deliberately a styled text, so we construct new components every time
    private static final StyledText PRESS_SHIFT_TO_CONTINUE =
            StyledText.fromString("                   §7Press §fSHIFT §7to continue");

    @OverlayInfo(renderType = RenderEvent.ElementType.GUI)
    private final NpcDialogueOverlay npcDialogueOverlay = new NpcDialogueOverlay();

    @RegisterKeyBind
    public final KeyBind cancelAutoProgressKeybind =
            new KeyBind("Cancel Dialog Auto Progress", GLFW.GLFW_KEY_Y, false, this::cancelAutoProgress);

    @Persisted
    public final Config<Boolean> autoProgress = new Config<>(false);

    @Persisted
    public final Config<Integer> dialogAutoProgressDefaultTime = new Config<>(1600); // Milliseconds

    @Persisted
    public final Config<Integer> dialogAutoProgressAdditionalTimePerWord = new Config<>(300); // Milliseconds

    private final ScheduledExecutorService autoProgressExecutor = Executors.newSingleThreadScheduledExecutor();
    private ScheduledFuture<?> scheduledAutoProgressKeyPress = null;

    private List<Component> lastDialogue = null;
    private MessageContainer autoProgressContainer = null;

    public NpcDialogueFeature() {
        super();

        // Add this feature as a dependent of the NpcDialogueModel
        Models.NpcDialogue.addNpcDialogExtractionDependent(this);
    }

    @SubscribeEvent
    public void onNpcDialogue(NpcDialogEvent e) {
        // If the overlay is not enabled, print the dialogue in chat, like Wynn would
        if (!Managers.Overlay.isEnabled(npcDialogueOverlay)) {
            printDialogueInChat(e.getChatMessage(), e.getType(), e.isProtected());
        }

        NpcDialogueType dialogueType = e.getType();

        if (dialogueType == NpcDialogueType.CONFIRMATIONLESS) return;

        if (scheduledAutoProgressKeyPress != null) {
            scheduledAutoProgressKeyPress.cancel(true);

            // Release sneak key if currently pressed
            McUtils.sendPacket(new ServerboundPlayerCommandPacket(
                    McUtils.player(), ServerboundPlayerCommandPacket.Action.RELEASE_SHIFT_KEY));

            scheduledAutoProgressKeyPress = null;
        }

        if (autoProgress.get() && dialogueType == NpcDialogueType.NORMAL) {
            List<StyledText> msg =
                    e.getChatMessage().stream().map(StyledText::fromComponent).toList();

            // Schedule a new sneak key press if this is not the end of the dialogue
            if (!msg.isEmpty()) {
                scheduledAutoProgressKeyPress = scheduledSneakPress(msg);

                // Display the auto progress notification
                updateAutoProgressNotification();
            }
        }
    }

    @SubscribeEvent
    public void onTick(TickEvent event) {
        updateAutoProgressNotification();
    }

    private void printDialogueInChat(List<Component> dialogues, NpcDialogueType type, boolean isProtected) {
        switch (type) {
            case NONE -> clearLastDialogue();
            case NORMAL -> displayNormalDialogue(dialogues);
            case SELECTION -> displaySelection(dialogues);
            case CONFIRMATIONLESS -> displayConfirmationlessDialogue(dialogues);
        }
    }

    @SubscribeEvent
    public void onWorldStateChange(WorldStateEvent e) {
        cancelAutoProgress();
    }

    public void cancelAutoProgress() {
        if (scheduledAutoProgressKeyPress == null) return;

        scheduledAutoProgressKeyPress.cancel(true);

        // Also reset the auto progress container
        if (autoProgressContainer != null) {
            Managers.Notification.removeMessage(autoProgressContainer);
            autoProgressContainer = null;
        }
    }

    public ScheduledFuture<?> getScheduledAutoProgressKeyPress() {
        return scheduledAutoProgressKeyPress;
    }

    private ScheduledFuture<?> scheduledSneakPress(List<StyledText> msg) {
        long delay = Models.NpcDialogue.calculateMessageReadTime(msg);

        return autoProgressExecutor.schedule(
                () -> McUtils.sendPacket(new ServerboundPlayerCommandPacket(
                        McUtils.player(), ServerboundPlayerCommandPacket.Action.PRESS_SHIFT_KEY)),
                delay,
                TimeUnit.MILLISECONDS);
    }

    private void displayNormalDialogue(List<Component> dialogues) {
        List<Component> screenLines = new ArrayList<>();

        // Construct the dialogue screen
        screenLines.add(Component.empty());
        screenLines.addAll(dialogues);
        screenLines.add(Component.empty());
        screenLines.add(PRESS_SHIFT_TO_CONTINUE.getComponent());
        screenLines.add(Component.empty());

        // If the last dialogue is not null, clear it
        clearLastDialogue();

        // Send the dialogue to the client
        screenLines.forEach(McUtils::sendMessageToClient);
        lastDialogue = screenLines;
    }

    private void updateAutoProgressNotification() {
        if (!autoProgress.get()) return;
        if (getScheduledAutoProgressKeyPress() == null) return;
        if (getScheduledAutoProgressKeyPress().isCancelled()) return;

        long timeUntilProgress = getScheduledAutoProgressKeyPress().getDelay(TimeUnit.MILLISECONDS);

        StyledText autoProgressStyledText = StyledText.fromString(ChatFormatting.GREEN + "Auto-progress: "
                + Math.max(0, Math.round(timeUntilProgress / 1000f))
                + " seconds (Press "
                + StyledText.fromComponent(
                                cancelAutoProgressKeybind.getKeyMapping().getTranslatedKeyMessage())
                        .getStringWithoutFormatting()
                + " to cancel)");

        if (autoProgressContainer != null) {
            Managers.Notification.editMessage(autoProgressContainer, autoProgressStyledText);
        } else {
            autoProgressContainer = Managers.Notification.queueMessage(autoProgressStyledText);
        }
    }

    private void displaySelection(List<Component> dialogues) {
        // If the last dialogue is not null, clear it
        clearLastDialogue();

        // Send the dialogue to the client
        dialogues.forEach(McUtils::sendMessageToClient);
        lastDialogue = dialogues;
    }

    private void displayConfirmationlessDialogue(List<Component> dialogues) {
        // We don't want to set last dialogue here, as it's not a normal dialogue
        dialogues.forEach(McUtils::sendMessageToClient);
    }

    private void clearLastDialogue() {
        if (lastDialogue != null) {
            lastDialogue.forEach(McUtils::removeMessageFromChat);
            lastDialogue = null;
        }

        // Also reset the auto progress container
        if (autoProgressContainer != null) {
            Managers.Notification.removeMessage(autoProgressContainer);
            autoProgressContainer = null;
        }
    }
}
