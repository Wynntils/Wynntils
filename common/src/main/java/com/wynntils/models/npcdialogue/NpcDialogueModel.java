/*
 * Copyright © Wynntils 2024.
 * This file is released under LGPLv3. See LICENSE for full license details.
 */
package com.wynntils.models.npcdialogue;

import com.wynntils.core.WynntilsMod;
import com.wynntils.core.components.Managers;
import com.wynntils.core.components.Model;
import com.wynntils.core.consumers.features.Feature;
import com.wynntils.core.text.StyledText;
import com.wynntils.features.overlays.NpcDialogueFeature;
import com.wynntils.handlers.chat.event.NpcDialogEvent;
import com.wynntils.handlers.chat.type.NpcDialogueType;
import com.wynntils.mc.event.TickEvent;
import com.wynntils.models.npcdialogue.type.ConfirmationlessDialogue;
import com.wynntils.models.npcdialogue.type.NpcDialogue;
import com.wynntils.models.worlds.event.WorldStateEvent;
import java.util.ArrayList;
import java.util.HashSet;
import java.util.List;
import java.util.Set;
import java.util.regex.Pattern;
import net.minecraft.network.chat.Component;
import net.minecraftforge.eventbus.api.EventPriority;
import net.minecraftforge.eventbus.api.SubscribeEvent;

public class NpcDialogueModel extends Model {
    private static final Pattern NEW_QUEST_STARTED = Pattern.compile("^§6§lNew Quest Started: §e§l(.*)$");

    private final Set<Feature> dialogExtractionDependents = new HashSet<>();

    private NpcDialogue currentDialogue = NpcDialogue.EMPTY;
    private final List<ConfirmationlessDialogue> confirmationlessDialogues = new ArrayList<>();

    public NpcDialogueModel() {
        super(List.of());
    }

    @SubscribeEvent(priority = EventPriority.HIGHEST)
    public void onNpcDialogue(NpcDialogEvent e) {
        handleDialogue(e.getChatMessage(), e.isProtected(), e.getType());
    }

    @SubscribeEvent
    public void onWorldStateChange(WorldStateEvent e) {
        currentDialogue = NpcDialogue.EMPTY;
        confirmationlessDialogues.clear();
    }

    @SubscribeEvent
    public void onTick(TickEvent event) {
        long now = System.currentTimeMillis();
        confirmationlessDialogues.removeIf(dialogue -> now >= dialogue.removeTime());
    }

    public void addNpcDialogExtractionDependent(Feature feature) {
        dialogExtractionDependents.add(feature);
    }

    public boolean isNpcDialogExtractionRequired() {
        return !dialogExtractionDependents.isEmpty()
                && dialogExtractionDependents.stream().allMatch(Feature::isEnabled);
    }

    public long calculateMessageReadTime(List<StyledText> msg) {
        // FIXME: Remove this feature-model dependency when we have model configs
        NpcDialogueFeature feature = Managers.Feature.getFeatureInstance(NpcDialogueFeature.class);

        int words = StyledText.join(" ", msg).split(" ").length;
        long delay = feature.dialogAutoProgressDefaultTime.get()
                + ((long) words * feature.dialogAutoProgressAdditionalTimePerWord.get());
        return delay;
    }

    public NpcDialogue getCurrentDialogue() {
        return currentDialogue;
    }

    public List<ConfirmationlessDialogue> getConfirmationlessDialogues() {
        return List.copyOf(confirmationlessDialogues);
    }

    private void handleDialogue(List<Component> chatMessage, boolean protectedDialogue, NpcDialogueType type) {
        List<StyledText> msg =
                chatMessage.stream().map(StyledText::fromComponent).toList();

        // Print dialogue to the system log
        WynntilsMod.info(
                "[NPC] Type: " + (msg.isEmpty() ? "<empty> " : "") + (protectedDialogue ? "<protected> " : "") + type);
        msg.forEach(s -> WynntilsMod.info("[NPC] " + (s.isEmpty() ? "<empty>" : s)));

        // The same message can be repeating before we have finished removing the old
        // Just remove the old and add the new with an updated remove time
        // It can also happen that a confirmationless dialogue turn into a normal
        // dialogue after a while (the "Press SHIFT..." text do not appear immediately)
        confirmationlessDialogues.removeIf(d -> d.text().equals(msg));

        if (type == NpcDialogueType.CONFIRMATIONLESS) {
            ConfirmationlessDialogue dialogue = new ConfirmationlessDialogue(
                    msg, System.currentTimeMillis(), System.currentTimeMillis() + calculateMessageReadTime(msg));
            confirmationlessDialogues.add(dialogue);
            return;
        }

        // If the message is the same as the current one, and the mode is "normal", don't update it
        // (ChatHandler already filters duplicates, but there are rare cases where the same dialogue is refreshed after
        // a while)
        if (type == NpcDialogueType.NORMAL && currentDialogue.currentDialogue().equals(msg)) return;

        // If the message is "NONE", set an empty dialogue
        if (type == NpcDialogueType.NONE) {
            currentDialogue = NpcDialogue.EMPTY;
            return;
        }

        currentDialogue = new NpcDialogue(msg, type, protectedDialogue, System.currentTimeMillis());

        if (!msg.isEmpty() && msg.get(0).getMatcher(NEW_QUEST_STARTED).find()) {
            // TODO: Show nice banner notification instead
            // but then we'd also need to confirm it with a sneak
            Managers.Notification.queueMessage(msg.get(0));
        }
    }
}
