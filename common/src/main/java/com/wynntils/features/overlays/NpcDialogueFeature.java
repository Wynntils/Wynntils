/*
 * Copyright © Wynntils 2022-2025.
 * This file is released under LGPLv3. See LICENSE for full license details.
 */
package com.wynntils.features.overlays;

import com.wynntils.core.components.Managers;
import com.wynntils.core.components.Models;
import com.wynntils.core.consumers.features.Feature;
import com.wynntils.core.consumers.features.ProfileDefault;
import com.wynntils.core.consumers.features.properties.RegisterKeyBind;
import com.wynntils.core.consumers.overlays.annotations.OverlayInfo;
import com.wynntils.core.keybinds.KeyBind;
import com.wynntils.core.notifications.MessageContainer;
import com.wynntils.core.persisted.Persisted;
import com.wynntils.core.persisted.config.Category;
import com.wynntils.core.persisted.config.Config;
import com.wynntils.core.persisted.config.ConfigCategory;
import com.wynntils.core.persisted.config.ConfigProfile;
import com.wynntils.core.text.StyledText;
import com.wynntils.handlers.chat.type.NpcDialogueType;
import com.wynntils.mc.event.KeyInputEvent;
import com.wynntils.mc.event.PacketEvent;
import com.wynntils.mc.event.RenderEvent;
import com.wynntils.mc.event.TickEvent;
import com.wynntils.models.npcdialogue.event.NpcDialogueProcessingEvent;
import com.wynntils.models.npcdialogue.type.NpcDialogue;
import com.wynntils.models.worlds.event.WorldStateEvent;
import com.wynntils.overlays.NpcDialogueOverlay;
import com.wynntils.utils.mc.McUtils;
import java.util.ArrayList;
import java.util.List;
import java.util.Objects;
import java.util.concurrent.Executors;
import java.util.concurrent.ScheduledExecutorService;
import java.util.concurrent.ScheduledFuture;
import java.util.concurrent.TimeUnit;
import net.minecraft.ChatFormatting;
import net.minecraft.network.chat.Component;
import net.minecraft.network.protocol.game.ServerboundPlayerCommandPacket;
import net.neoforged.bus.api.SubscribeEvent;
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
    private static final StyledText PRESS_SHIFT_TO_CONTINUE =
            StyledText.fromComponent(Component.translatable("feature.wynntils.npcDialogue.pressShiftToContinue"));

    @OverlayInfo(renderType = RenderEvent.ElementType.GUI)
    private final NpcDialogueOverlay npcDialogueOverlay = new NpcDialogueOverlay();

    @RegisterKeyBind
    public final KeyBind cancelAutoProgressKeybind =
            new KeyBind("Cancel Dialog Auto Progress", GLFW.GLFW_KEY_Y, false, this::cancelAutoProgress);

    @RegisterKeyBind
    public final KeyBind npcDialogKeyOverrideKeybind =
            new KeyBind("Progress NPC Dialogue", GLFW.GLFW_KEY_UNKNOWN, true, this::progressNPCDialogue);

    @Persisted
    private final Config<NpcDialogueChatDisplayType> chatDisplayType = new Config<>(NpcDialogueChatDisplayType.NORMAL);

    @Persisted
    private final Config<Boolean> autoProgress = new Config<>(false);

    @Persisted
    public final Config<Integer> dialogAutoProgressDefaultTime = new Config<>(1600); // Milliseconds

    @Persisted
    public final Config<Integer> dialogAutoProgressAdditionalTimePerWord = new Config<>(300); // Milliseconds

    @Persisted
    public final Config<Boolean> overrideSneakKey = new Config<>(true);

    private final ScheduledExecutorService autoProgressExecutor = Executors.newSingleThreadScheduledExecutor();
    private ScheduledFuture<?> scheduledAutoProgressKeyPress = null;

    private final ScheduledExecutorService npcDialogKeyOverrideExecutor = Executors.newSingleThreadScheduledExecutor();
    private boolean isReleaseShiftScheduled;

    // Normal mode
    // This list holds a constructed dialogue screen,
    // with all the currently displayed dialogues
    private List<Component> currentlyDisplayedDialogue = null;
    private NpcDialogue currentDialogue = null;
    private List<NpcDialogue> confirmationlessDialogues = new ArrayList<>();
    private MessageContainer autoProgressContainer = null;

    // Legacy mode
    // Used to track when confirmationless dialogues are turned into normal dialogues,
    // so we only display the helper message, and not duplicate the dialogues
    private List<Component> lastDialogue = null;
    private MessageContainer displayedHelperContainer = null;
    private StyledText displayedHelperMessage = null;

    public NpcDialogueFeature() {
        super(new ProfileDefault.Builder().disableFor(ConfigProfile.BLANK_SLATE).build());

        // Add this feature as a dependent of the NpcDialogueModel
        Models.NpcDialogue.addNpcDialogExtractionDependent(this);
    }

    @SubscribeEvent
    public void onNpcDialogue(NpcDialogueProcessingEvent.Pre event) {
        NpcDialogue dialogue = event.getDialogue();
        NpcDialogueType dialogueType = dialogue.dialogueType();

        if (dialogueType == NpcDialogueType.CONFIRMATIONLESS) return;

        if (autoProgress.get() && dialogueType == NpcDialogueType.NORMAL) {
            // Schedule a new sneak key press if this is not the end of the dialogue
            if (!dialogue.isEmpty()) {
                scheduledAutoProgressKeyPress = scheduledSneakPress(dialogue.currentDialogue());

                // Display the auto progress notification
                updateAutoProgressNotification();
            }
        }
    }

    @SubscribeEvent
    public void onNpcDialoguePost(NpcDialogueProcessingEvent.Post event) {
        NpcDialogue dialogue = event.getDialogue();

        // If the overlay is not enabled, print the dialogue in chat, like Wynn would
        if (!Managers.Overlay.isEnabled(npcDialogueOverlay)
                && chatDisplayType.get() == NpcDialogueChatDisplayType.LEGACY) {
            printLegacyDialogueInChat(
                    event.getPostProcessedDialogueComponent(), dialogue.dialogueType(), dialogue.isProtected());
        }

        // NpcDialogueChatDisplayType.NORMAL mode updates in the onTick method
    }

    @SubscribeEvent
    public void onDialogueSneakPress(KeyInputEvent e) {
        if (npcDialogKeyOverrideKeybind.getKeyMapping().isUnbound()) return;
        if (!overrideSneakKey.get()) return;
        if (e.getKey() != McUtils.options().keyShift.key.getValue()) return;

        if (Models.NpcDialogue.getCurrentDialogue().dialogueType() == NpcDialogueType.NORMAL
                && Models.NpcDialogue.getCurrentDialogue().isProtected()) {
            e.setCanceled(true);
        }
    }

    @SubscribeEvent
    public void onTick(TickEvent event) {
        if (Managers.Overlay.isEnabled(npcDialogueOverlay)) return;

        // Both modes
        updateAutoProgressNotification();

        // Normal mode
        if (chatDisplayType.get() == NpcDialogueChatDisplayType.NORMAL) {
            updateDialogueScreen();
        }

        // Legacy mode
        if (chatDisplayType.get() == NpcDialogueChatDisplayType.LEGACY) {
            if (!Models.NpcDialogue.isInDialogue()) {
                lastDialogue = null;
                removeHelperMessage();
                resetAutoProgressContainer();
                return;
            }

            displayHelperMessage();
        }
    }

    @SubscribeEvent
    public void onPacketSent(PacketEvent.PacketSentEvent<?> e) {
        if (!(e.getPacket() instanceof ServerboundPlayerCommandPacket packet)) return;
        if (packet.getAction() != ServerboundPlayerCommandPacket.Action.PRESS_SHIFT_KEY) return;

        if (scheduledAutoProgressKeyPress != null) {
            scheduledAutoProgressKeyPress.cancel(true);

            // Must be scheduled, can't be sent immediately
            autoProgressExecutor.schedule(
                    () -> McUtils.sendPacket(new ServerboundPlayerCommandPacket(
                            McUtils.player(), ServerboundPlayerCommandPacket.Action.RELEASE_SHIFT_KEY)),
                    100,
                    TimeUnit.MILLISECONDS);

            scheduledAutoProgressKeyPress = null;
        }

        if (isReleaseShiftScheduled) {
            npcDialogKeyOverrideExecutor.schedule(
                    () -> McUtils.sendPacket(new ServerboundPlayerCommandPacket(
                            McUtils.player(), ServerboundPlayerCommandPacket.Action.RELEASE_SHIFT_KEY)),
                    100,
                    TimeUnit.MILLISECONDS);

            isReleaseShiftScheduled = false;
        }
    }

    @SubscribeEvent
    public void onWorldStateChange(WorldStateEvent e) {
        cancelAutoProgress();

        // Reset the state
        currentlyDisplayedDialogue = null;
        currentDialogue = null;
        confirmationlessDialogues = new ArrayList<>();
        autoProgressContainer = null;
        lastDialogue = null;
        displayedHelperMessage = null;
        displayedHelperContainer = null;
    }

    private void cancelAutoProgress() {
        if (scheduledAutoProgressKeyPress == null) return;

        scheduledAutoProgressKeyPress.cancel(true);

        // Also reset the auto progress container
        resetAutoProgressContainer();
    }

    public ScheduledFuture<?> getScheduledAutoProgressKeyPress() {
        return scheduledAutoProgressKeyPress;
    }

    private void progressNPCDialogue() {
        if (Models.NpcDialogue.getCurrentDialogue().dialogueType() == NpcDialogueType.NORMAL) {
            isReleaseShiftScheduled = true;
            McUtils.sendPacket(new ServerboundPlayerCommandPacket(
                    McUtils.player(), ServerboundPlayerCommandPacket.Action.PRESS_SHIFT_KEY));
        }
    }

    private ScheduledFuture<?> scheduledSneakPress(List<StyledText> dialogue) {
        long delay = Models.NpcDialogue.calculateMessageReadTime(dialogue);

        return autoProgressExecutor.schedule(
                () -> McUtils.sendPacket(new ServerboundPlayerCommandPacket(
                        McUtils.player(), ServerboundPlayerCommandPacket.Action.PRESS_SHIFT_KEY)),
                delay,
                TimeUnit.MILLISECONDS);
    }

    private void printLegacyDialogueInChat(List<Component> dialogues, NpcDialogueType type, boolean isProtected) {
        // If the dialogues are not the same as the last dialogues, print them in chat
        if (!Objects.equals(dialogues, this.lastDialogue)) {
            this.lastDialogue = dialogues;

            // In legacy mode, just print the dialogues in chat
            dialogues.forEach(McUtils::sendMessageToClient);
        }

        // Either ways, display the helper message
        if (type == NpcDialogueType.SELECTION) {
            displayedHelperMessage =
                    StyledText.fromComponent(Component.translatable("feature.wynntils.npcDialogue.selectAnOption")
                            .withStyle(ChatFormatting.RED));
            displayHelperMessage();
        } else if (type == NpcDialogueType.NORMAL) {
            displayedHelperMessage = getNormalDisplayedHelperMessage();
            displayHelperMessage();
        }
    }

    private StyledText getNormalDisplayedHelperMessage() {
        if (!npcDialogKeyOverrideKeybind.getKeyMapping().isUnbound()) {
            String keyName = npcDialogKeyOverrideKeybind
                    .getKeyMapping()
                    .getTranslatedKeyMessage()
                    .getString();

            if (overrideSneakKey.get()) {
                return StyledText.fromComponent(
                        Component.translatable("feature.wynntils.npcDialogue.keyToProgress", keyName)
                                .withStyle(ChatFormatting.GREEN));
            }
            return StyledText.fromComponent(
                    Component.translatable("feature.wynntils.npcDialogue.shiftOrKeyToProgress", keyName)
                            .withStyle(ChatFormatting.GREEN));
        }
        return StyledText.fromComponent(Component.translatable("feature.wynntils.npcDialogue.shiftToProgress")
                .withStyle(ChatFormatting.GREEN));
    }

    private void updateDialogueScreen() {
        List<NpcDialogue> confirmationlessDialogues = Models.NpcDialogue.getConfirmationlessDialogues();
        NpcDialogue currentDialogue = Models.NpcDialogue.getCurrentDialogue();

        // If there is no dialogue, clear the last dialogue
        if (currentDialogue == null || currentDialogue.isEmpty() && confirmationlessDialogues.isEmpty()) {
            clearLastDialogue();
            return;
        }

        // If there is no change in the dialogues, return
        if (Objects.equals(currentDialogue, this.currentDialogue)
                && Objects.equals(confirmationlessDialogues, this.confirmationlessDialogues)) {
            return;
        }

        this.currentDialogue = currentDialogue;
        this.confirmationlessDialogues = confirmationlessDialogues;

        // Construct the dialogue screen
        List<Component> screenLines = new ArrayList<>();

        for (NpcDialogue confirmationlessDialogue : confirmationlessDialogues) {
            screenLines.add(Component.empty());
            screenLines.addAll(confirmationlessDialogue.dialogueComponent());
        }

        if (!currentDialogue.isEmpty()) {
            screenLines.add(Component.empty());
            screenLines.addAll(currentDialogue.currentDialogue().stream()
                    .map(StyledText::getComponent)
                    .toList());
            screenLines.add(Component.empty());
            StyledText PRESS_SHIFT_OR_KEY_TO_CONTINUE = getPressShiftOrKeyToContinue();
            screenLines.add(PRESS_SHIFT_OR_KEY_TO_CONTINUE.getComponent());
            screenLines.add(Component.empty());
        } else {
            // Add an empty line after the last confirmationless dialogue
            screenLines.add(Component.empty());
        }

        // If the last dialogue is not null, clear it
        clearLastDialogue();

        // Send the dialogue to the client
        screenLines.forEach(McUtils::sendMessageToClient);
        currentlyDisplayedDialogue = screenLines;
    }

    private StyledText getPressShiftOrKeyToContinue() {
        if (!npcDialogKeyOverrideKeybind.getKeyMapping().isUnbound()) {
            String keyName = npcDialogKeyOverrideKeybind
                    .getKeyMapping()
                    .getTranslatedKeyMessage()
                    .getString();

            if (overrideSneakKey.get()) {
                return StyledText.fromComponent(
                        Component.translatable("feature.wynntils.npcDialogue.pressKeyToContinue", keyName));
            }
            return StyledText.fromComponent(
                    Component.translatable("feature.wynntils.npcDialogue.pressShiftOrKeyToContinue", keyName));
        }
        return PRESS_SHIFT_TO_CONTINUE;
    }

    private void updateAutoProgressNotification() {
        if (!autoProgress.get()) return;
        if (getScheduledAutoProgressKeyPress() == null) return;
        if (getScheduledAutoProgressKeyPress().isCancelled()) return;

        long timeUntilProgress = getScheduledAutoProgressKeyPress().getDelay(TimeUnit.MILLISECONDS);

        StyledText autoProgressStyledText = StyledText.fromComponent(Component.translatable(
                        "feature.wynntils.npcDialogue.autoProgressMessage",
                        Math.max(0, Math.round(timeUntilProgress / 1000f)),
                        cancelAutoProgressKeybind
                                .getKeyMapping()
                                .getTranslatedKeyMessage()
                                .getString())
                .withStyle(ChatFormatting.GREEN));

        if (autoProgressContainer != null) {
            autoProgressContainer = Managers.Notification.editMessage(autoProgressContainer, autoProgressStyledText);
        } else {
            autoProgressContainer = Managers.Notification.queueMessage(autoProgressStyledText);
        }
    }

    private void clearLastDialogue() {
        if (currentlyDisplayedDialogue != null) {
            currentlyDisplayedDialogue.forEach(McUtils::removeMessageFromChat);
            currentlyDisplayedDialogue = null;
        }

        // Also reset the auto progress container
        resetAutoProgressContainer();
    }

    private void resetAutoProgressContainer() {
        if (autoProgressContainer != null) {
            Managers.Notification.removeMessage(autoProgressContainer);
            autoProgressContainer = null;
        }
    }

    private void displayHelperMessage() {
        // If the helper message is null, return
        if (displayedHelperMessage == null) return;

        if (displayedHelperContainer == null) {
            displayedHelperContainer = Managers.Notification.queueMessage(displayedHelperMessage);
            return;
        }

        displayedHelperContainer = Managers.Notification.editMessage(displayedHelperContainer, displayedHelperMessage);
    }

    private void removeHelperMessage() {
        displayedHelperMessage = null;

        // If the helper container is not null, remove it
        if (displayedHelperContainer != null) {
            Managers.Notification.removeMessage(displayedHelperContainer);
            displayedHelperContainer = null;
        }
    }

    public enum NpcDialogueChatDisplayType {
        NORMAL,
        LEGACY
    }
}
