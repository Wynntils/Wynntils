/*
 * Copyright © Wynntils 2022-2024.
 * This file is released under LGPLv3. See LICENSE for full license details.
 */
package com.wynntils.features.overlays;

import com.wynntils.core.components.Models;
import com.wynntils.core.consumers.features.Feature;
import com.wynntils.core.consumers.features.properties.RegisterKeyBind;
import com.wynntils.core.consumers.overlays.annotations.OverlayInfo;
import com.wynntils.core.keybinds.KeyBind;
import com.wynntils.core.persisted.Persisted;
import com.wynntils.core.persisted.config.Category;
import com.wynntils.core.persisted.config.Config;
import com.wynntils.core.persisted.config.ConfigCategory;
import com.wynntils.core.text.StyledText;
import com.wynntils.handlers.chat.event.NpcDialogEvent;
import com.wynntils.handlers.chat.type.NpcDialogueType;
import com.wynntils.mc.event.RenderEvent;
import com.wynntils.models.worlds.event.WorldStateEvent;
import com.wynntils.overlays.NpcDialogueOverlay;
import com.wynntils.utils.mc.McUtils;
import java.util.List;
import java.util.concurrent.Executors;
import java.util.concurrent.ScheduledExecutorService;
import java.util.concurrent.ScheduledFuture;
import java.util.concurrent.TimeUnit;
import net.minecraft.network.protocol.game.ServerboundPlayerCommandPacket;
import net.minecraftforge.eventbus.api.SubscribeEvent;
import org.lwjgl.glfw.GLFW;

// TODO: Write a config upfixer for this class
/**
 * Feature for handling NPC dialogues.
 * It is responsible for handling the dialogue auto progressing.
 *
 * There are two ways this feature works:
 * <ol>
 * <li>If the NPC Dialogue overlay is enabled, all dialogues will be displayed in the overlay.</li>
 * <li>If the NPC Dialogue overlay is disabled, the dialogues will be displayed in text chat.</li>
 * </ol>
 * If the feature is disabled, we stop all special processing for chat screens in ChatHandler.
 */
@ConfigCategory(Category.OVERLAYS)
public class NpcDialogueFeature extends Feature {
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

    public NpcDialogueFeature() {
        super();

        // Add this feature as a dependent of the NpcDialogueModel
        Models.NpcDialogue.addNpcDialogExtractionDependent(this);
    }

    @SubscribeEvent
    public void onNpcDialogue(NpcDialogEvent e) {
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
            }
        }
    }

    @SubscribeEvent
    public void onWorldStateChange(WorldStateEvent e) {
        cancelAutoProgress();
    }

    public void cancelAutoProgress() {
        if (scheduledAutoProgressKeyPress == null) return;

        scheduledAutoProgressKeyPress.cancel(true);
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
}
