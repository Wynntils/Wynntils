/*
 * Copyright © Wynntils 2022-2023.
 * This file is released under LGPLv3. See LICENSE for full license details.
 */
package com.wynntils.handlers.chat;

import com.wynntils.core.WynntilsMod;
import com.wynntils.core.components.Handler;
import com.wynntils.core.components.Managers;
import com.wynntils.core.consumers.features.Feature;
import com.wynntils.core.text.PartStyle;
import com.wynntils.core.text.StyledText;
import com.wynntils.handlers.chat.event.ChatMessageReceivedEvent;
import com.wynntils.handlers.chat.event.NpcDialogEvent;
import com.wynntils.handlers.chat.type.MessageType;
import com.wynntils.handlers.chat.type.NpcDialogueType;
import com.wynntils.handlers.chat.type.RecipientType;
import com.wynntils.mc.event.ChatPacketReceivedEvent;
import com.wynntils.mc.event.MobEffectEvent;
import com.wynntils.utils.mc.ComponentUtils;
import com.wynntils.utils.mc.McUtils;
import java.util.Collections;
import java.util.HashSet;
import java.util.LinkedList;
import java.util.List;
import java.util.Set;
import java.util.regex.Pattern;
import net.minecraft.network.chat.Component;
import net.minecraft.world.effect.MobEffects;
import net.minecraftforge.eventbus.api.EventPriority;
import net.minecraftforge.eventbus.api.SubscribeEvent;

/**
 * The responsibility of this class is to act as the first gateway for incoming
 * chat messages from Wynncraft. Chat messages in vanilla comes in three types,
 * CHAT, SYSTEM and GAME_INFO. The latter is the "action bar", and is handled
 * elsewhere. However, starting with Minecraft 1.19, Wynncraft will send all chat
 * messages as SYSTEM, so we will ignore the CHAT type.
 *
 * Using the regexp patterns in RecipientType, we classify the incoming messages
 * according to if they are sent to the guild, party, global chat, etc. Messages
 * that do not match any of these categories are called "info" messages, and are
 * typically automated responses or announcements. Messages that do match any other
 * category, are sent by other users (what could really be termed "chat"). The one
 * exception is guild messages, which can also be e.g. WAR announcements.
 * (Unfortunately, there is no way to distinguish these from chat sent by a build
 * member named "WAR", or "INFO", or..., so if these need to be separated, it has
 * to happen in a later stage).
 * <p>
 * The final problem this class needs to resolve is how Wynncraft handles NPC
 * dialogs. When you enter a NPC dialog, Wynncraft start sending "screens" once a
 * second or so, which is multi-line messages that repeat the chat history, and add
 * the NPC dialog at the end. This way, the vanilla client will always show the NPC
 * dialog, so it is a clever hack in that respect. But it makes our life harder. We
 * solve this by detecting when a multiline "screen" happens, look for the last
 * real chat message we received, and splits of the rest as the "newLines". These
 * are in turn examined, since they can contain the actual NPC dialog, or they can
 * contain new chat messages sent while the user is in the NPC dialog.
 * <p>
 * These new chat messages are the real problematic thing here. They are
 * differently formatted to be gray and tuned-down, which makes the normal regexp
 * matching fail. They are also sent as pure strings with formatting codes, instead
 * of Components as normal one-line chats are. This mean things like hover and
 * onClick information is lost. (There is nothing we can do about this, it is a
 * Wynncraft limitation.) We send out these chat messages one by one, as they would
 * have appeared if we were not in a NPC dialog, but we tag them as BACKGROUND to
 * signal that formatting is different.
 * <p>
 * In a normal vanilla setting, the last "screen" that Wynncraft sends out, the
 * messages are re-colored to have their normal colors restored (hover and onClick
 * as still missing, though). Currently, we do not handle this, since it would mean
 * sending out information that already sent chat lines would need to be updated to
 * a different formatting. This could be done, but requires extra logic, and most
 * importantly, a way to update already printed chat lines.
 */
public final class ChatHandler extends Handler {
    // Test suite: https://regexr.com/7esj7
    private static final Pattern NPC_CONFIRM_PATTERN =
            Pattern.compile("^ *§[47]Press §[cf](SNEAK|SHIFT) §[47]to continue$");

    // Test suite: https://regexr.com/7esjd
    private static final Pattern NPC_SELECT_PATTERN =
            Pattern.compile("^ *§[47cf](Select|CLICK) §[47cf]an option (§[47])?to continue$");

    private static final Pattern EMPTY_LINE_PATTERN = Pattern.compile("^\\s*(§r|À+)?\\s*$");
    private static final long SLOWDOWN_PACKET_DIFF_MS = 500;

    private final Set<Feature> dialogExtractionDependents = new HashSet<>();
    private String lastRealChat = null;
    private long lastSlowdownApplied = 0;
    private List<Component> lastScreenNpcDialog = List.of();
    private List<Component> delayedDialogue;
    private NpcDialogueType delayedType;

    public void addNpcDialogExtractionDependent(Feature feature) {
        dialogExtractionDependents.add(feature);
    }

    public void removeNpcDialogExtractionDependent(Feature feature) {
        dialogExtractionDependents.remove(feature);
    }

    @SubscribeEvent(priority = EventPriority.HIGHEST)
    public void onChatReceived(ChatPacketReceivedEvent e) {
        if (e instanceof ChatPacketReceivedEvent.GameInfo) return;

        Component message = e.getMessage();
        StyledText styledText = StyledText.fromComponent(message);
        String plainMessage = styledText.getString(PartStyle.StyleType.NONE);

        // Sometimes there is just a trailing newline; that does not
        // make it a multiline message
        if (styledText.contains("\n") && plainMessage.indexOf('\n') != plainMessage.length() - 1) {
            // This is a "chat screen"
            if (shouldSeparateNPC()) {
                handleIncomingChatScreen(message);
                e.setCanceled(true);
            }
            // If we are not separating NPCs, just pass the chat screen right on. We can do
            // nothing about it.
            // FIXME: This is a temporary solution. We don't classify NPC messages correctly, they are marked as INFO.
            //        Even if we don't separate NPC messages, we should still classify them correctly.
            //        However, this hack allows us to disable the NPC dialog extraction feature (/overlay), and still
            // get
            //        the dialog in the chat window.
            Component updatedMessage = postChatLine(message, styledText, MessageType.FOREGROUND);

            if (updatedMessage == null) {
                e.setCanceled(true);
            } else if (!updatedMessage.equals(message)) {
                e.setMessage(updatedMessage);
            }
        } else {
            // No, it's a normal one line chat
            Component updatedMessage = postChatLine(message, styledText, MessageType.FOREGROUND);

            if (updatedMessage == null) {
                e.setCanceled(true);
            } else if (!updatedMessage.equals(message)) {
                e.setMessage(updatedMessage);
            }
        }
    }

    @SubscribeEvent
    public void onStatusEffectUpdate(MobEffectEvent.Update event) {
        if (event.getEntity() != McUtils.player()) return;

        if (event.getEffect() == MobEffects.MOVEMENT_SLOWDOWN
                && event.getEffectAmplifier() == 3
                && event.getEffectDurationTicks() == 32767) {
            if (delayedDialogue != null) {
                List<Component> dialogue = delayedDialogue;
                delayedDialogue = null;

                postNpcDialogue(dialogue, delayedType, true);
            } else {
                lastSlowdownApplied = System.currentTimeMillis();
            }
        }
    }

    @SubscribeEvent
    public void onStatusEffectRemove(MobEffectEvent.Remove event) {
        if (event.getEntity() != McUtils.player()) return;

        if (event.getEffect() == MobEffects.MOVEMENT_SLOWDOWN) {
            lastSlowdownApplied = 0;
        }
    }

    private void handleIncomingChatScreen(Component message) {
        List<Component> lines = ComponentUtils.splitComponentInLines(message);
        // From now on, we'll work on reversed lists, so the message that should
        // have been closest to the bottom is now on top.
        Collections.reverse(lines);

        LinkedList<Component> newLines = new LinkedList<>();
        if (lastRealChat == null) {
            // If we have no history, all lines are to be considered new
            lines.forEach(newLines::addLast);
        } else {
            // Figure out what's new since last chat message
            for (Component line : lines) {
                String plainText = line.getString();
                if (plainText.equals(lastRealChat)) break;
                newLines.addLast(line);
            }
        }

        if (newLines.isEmpty()) {
            // No new lines has appeared since last registered chat line.
            // We could just have a dialog that disappeared, so we must signal this
            postNpcDialogue(List.of(), NpcDialogueType.NONE, false);
            return;
        }

        if (newLines.getLast().getString().isEmpty()) {
            // Wynntils add an empty line before the NPC dialog; remove it
            newLines.removeLast();
        }

        // Now what to do with the new lines we found?
        processNewLines(newLines);
    }

    private void processNewLines(LinkedList<Component> newLines) {
        // We have new lines added to the bottom of the chat screen. They are either a dialogue,
        // or new background chat messages. Separate them in two parts
        LinkedList<Component> newChatLines = new LinkedList<>();
        LinkedList<Component> dialogue = new LinkedList<>();

        StyledText firstLineCoded = StyledText.fromComponent(newLines.getFirst());
        boolean isNpcConfirm = firstLineCoded.find(NPC_CONFIRM_PATTERN);
        boolean isNpcSelect = firstLineCoded.find(NPC_SELECT_PATTERN);

        if (isNpcConfirm || isNpcSelect) {
            // This is an NPC dialogue screen.
            // First remove the "Press SHIFT/Select an option to continue" trailer.
            newLines.removeFirst();
            if (newLines.getFirst().getString().isEmpty()) {
                // After this we assume a blank line
                newLines.removeFirst();
            } else {
                WynntilsMod.warn("Malformed dialog [#1]: " + newLines.getFirst());
            }

            boolean dialogDone = false;
            // This need to be false if we are to look for options
            boolean optionsFound = !isNpcSelect;

            // Separate the dialog part from any potential new "real" chat lines
            for (Component line : newLines) {
                StyledText codedLine = StyledText.fromComponent(line);
                if (!dialogDone) {
                    if (codedLine.find(EMPTY_LINE_PATTERN)) {
                        if (!optionsFound) {
                            // First part of the dialogue found
                            optionsFound = true;
                            dialogue.push(line);
                        } else {
                            dialogDone = true;
                        }
                        // Intentionally throw away this line
                    } else {
                        dialogue.push(line);
                    }
                } else {
                    // If there is anything after the dialogue, it is new chat lines
                    if (!codedLine.find(EMPTY_LINE_PATTERN)) {
                        newChatLines.push(line);
                    }
                }
            }
        } else {
            // After a NPC dialog screen, Wynncraft sends a "clear screen" with line of ÀÀÀ...
            // We just ignore that part. Also, remove empty lines or lines with just the §r code
            while (!newLines.isEmpty()
                    && StyledText.fromComponent(newLines.getFirst()).find(EMPTY_LINE_PATTERN)) {
                newLines.removeFirst();
            }

            // What remains, if any, are new chat lines
            newLines.forEach(newChatLines::push);
        }

        // Register all new chat lines
        newChatLines.forEach(this::handleFakeChatLine);

        // Handle the dialogue, if any
        handleScreenNpcDialog(dialogue, isNpcSelect);
    }

    private void handleScreenNpcDialog(List<Component> dialog, boolean isSelection) {
        if (dialog.isEmpty()) {
            // dialog could be the empty list, this means the last dialog is removed
            postNpcDialogue(dialog, NpcDialogueType.NONE, false);
            return;
        }

        NpcDialogueType type = isSelection ? NpcDialogueType.SELECTION : NpcDialogueType.NORMAL;

        if ((System.currentTimeMillis() <= lastSlowdownApplied + SLOWDOWN_PACKET_DIFF_MS)) {
            // This is a "protected" dialogue if we have gotten slowdown effect just prior to the chat message
            // This is the normal case
            postNpcDialogue(dialog, type, true);
            return;
        }

        // Maybe this should be a protected dialogue but packets came in the wrong order.
        // Wait a tick for slowdown, and then send the event
        delayedDialogue = dialog;
        delayedType = type;
        Managers.TickScheduler.scheduleNextTick(() -> {
            if (delayedDialogue != null) {
                List<Component> dialogToSend = delayedDialogue;
                delayedDialogue = null;
                // If we got here, then we did not get the slowdown effect, otherwise we would
                // have sent the dialogue already
                postNpcDialogue(dialogToSend, delayedType, false);
            }
        });
    }

    private void handleFakeChatLine(Component message) {
        // This is a normal, single line chat, sent in the background
        StyledText styledText = StyledText.fromComponent(message);

        // But it can weirdly enough actually also be a foreground NPC chat message...
        if (getRecipientType(styledText, MessageType.FOREGROUND) == RecipientType.NPC) {
            // In this case, do *not* save this as last chat, since it will soon disappear
            // from history!
            postNpcDialogue(List.of(message), NpcDialogueType.CONFIRMATIONLESS, false);
            return;
        }

        Component updatedMessage = postChatLine(message, styledText, MessageType.BACKGROUND);
        // If the message is canceled, we do not need to cancel any packets,
        // just don't send out the chat message
        if (updatedMessage == null) return;

        // Otherwise emulate a normal incoming chat message
        McUtils.sendMessageToClient(updatedMessage);
    }

    /**
     * Return a "massaged" version of the message, or null if we should cancel the
     * message entirely.
     */
    private Component postChatLine(Component message, StyledText styledText, MessageType messageType) {
        String plainText = styledText.getStringWithoutFormatting();
        if (!plainText.isBlank()) {
            // We store the unformatted string version to be able to compare between
            // foreground and background versions
            lastRealChat = plainText;
        }

        // Normally § codes are stripped from the log; need this to be able to debug chat formatting
        WynntilsMod.info("[CHAT] " + styledText.getString().replace("§", "&"));
        RecipientType recipientType = getRecipientType(styledText, messageType);

        if (recipientType == RecipientType.NPC) {
            if (shouldSeparateNPC()) {
                postNpcDialogue(List.of(message), NpcDialogueType.CONFIRMATIONLESS, false);
                // We need to cancel the original chat event, if any
                return null;
            } else {
                // Reclassify this as a INFO type for the chat
                recipientType = RecipientType.INFO;
            }
        }

        ChatMessageReceivedEvent event = new ChatMessageReceivedEvent(message, styledText, messageType, recipientType);
        WynntilsMod.postEvent(event);
        if (event.isCanceled()) return null;
        return event.getMessage();
    }

    private void postNpcDialogue(List<Component> dialogue, NpcDialogueType type, boolean isProtected) {
        // Confirmationless dialoges bypass the lastScreenNpcDialogue check
        if (type != NpcDialogueType.CONFIRMATIONLESS) {
            if (lastScreenNpcDialog.equals(dialogue)) return;

            lastScreenNpcDialog = dialogue;
        }

        if (type == NpcDialogueType.NONE) {
            // Ignore any delayed dialogues, since they are now obsolete
            delayedDialogue = null;
        }

        NpcDialogEvent event = new NpcDialogEvent(dialogue, type, isProtected);
        WynntilsMod.postEvent(event);
    }

    private RecipientType getRecipientType(StyledText codedMessage, MessageType messageType) {
        // Check if message match a recipient category
        for (RecipientType recipientType : RecipientType.values()) {
            if (recipientType.matchPattern(codedMessage, messageType)) {
                return recipientType;
            }
        }

        // If no specific recipient matched, it is an "info" message
        return RecipientType.INFO;
    }

    private boolean shouldSeparateNPC() {
        return dialogExtractionDependents.stream().anyMatch(Feature::isEnabled);
    }
}
