/*
 * Copyright © Wynntils 2022.
 * This file is released under AGPLv3. See LICENSE for full license details.
 */
package com.wynntils.handlers.chat;

import com.wynntils.core.WynntilsMod;
import com.wynntils.core.components.Handler;
import com.wynntils.core.features.Feature;
import com.wynntils.handlers.chat.event.ChatMessageReceivedEvent;
import com.wynntils.handlers.chat.event.NpcDialogEvent;
import com.wynntils.mc.event.ChatPacketReceivedEvent;
import com.wynntils.mc.objects.ChatType;
import com.wynntils.mc.utils.ComponentUtils;
import com.wynntils.mc.utils.McUtils;
import java.util.Collections;
import java.util.HashSet;
import java.util.LinkedList;
import java.util.List;
import java.util.Set;
import java.util.regex.Pattern;
import net.minecraft.network.chat.Component;
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
    private static final Pattern NPC_FINAL_PATTERN =
            Pattern.compile("^ +§[47]Press §r§[cf](SNEAK|SHIFT) §r§[47]to continue§r$");
    private static final Pattern NPC_SELECT_PATTERN =
            Pattern.compile("^ +§[c47](Select|CLICK) §r§[4cf]an option (§r§[47])?to continue§r$");

    private static final Pattern EMPTY_LINE_PATTERN = Pattern.compile("^\\s*(§r|À+)?\\s*$");

    private final Set<Feature> dialogExtractionDependents = new HashSet<>();
    private String lastRealChat = null;
    private List<Component> lastNpcDialog = List.of();

    @SubscribeEvent(priority = EventPriority.HIGHEST)
    public void onChatReceived(ChatPacketReceivedEvent e) {
        if (e.getType() == ChatType.GAME_INFO) return;

        Component message = e.getMessage();
        String codedMessage = ComponentUtils.getCoded(message);

        // Sometimes there is just a trailing newline; that does not
        // make it a multiline message
        if (!codedMessage.contains("\n") || codedMessage.indexOf('\n') == (codedMessage.length() - 1)) {
            saveLastChat(message);
            RecipientType recipientType = getRecipientType(message, MessageType.FOREGROUND);

            boolean separateNPC = (dialogExtractionDependents.stream().anyMatch(Feature::isEnabled));
            if (separateNPC && recipientType == RecipientType.NPC) {
                handleNpcDialog(List.of(message), false, false);
                return;
            }

            Component updatedMessage = handleChatLine(message, codedMessage, recipientType, MessageType.FOREGROUND);
            if (updatedMessage == null) {
                e.setCanceled(true);
            } else if (!updatedMessage.equals(message)) {
                e.setMessage(updatedMessage);
            }
            return;
        }

        if (dialogExtractionDependents.stream().anyMatch(Feature::isEnabled)) {
            handleMultilineMessage(message);
            e.setCanceled(true);
        }
    }

    private void handleMultilineMessage(Component message) {
        List<Component> lines = ComponentUtils.splitComponentInLines(message);
        // From now on, we'll work on reversed lists
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
            handleNpcDialog(List.of(), true, false);
            return;
        }

        if (newLines.getLast().getString().isEmpty()) {
            // Wynntils add an empty line before the NPC dialog; remove it
            newLines.removeLast();
        }

        LinkedList<Component> newChatLines = new LinkedList<>();
        LinkedList<Component> dialog = new LinkedList<>();
        boolean isSelectionDialog;

        String firstLineCoded = ComponentUtils.getCoded(newLines.getFirst());
        if (NPC_FINAL_PATTERN.matcher(firstLineCoded).find()) {
            // This is an NPC dialog screen.
            // First remove the "Press SHIFT to continue" trailer.
            newLines.removeFirst();
            if (newLines.getFirst().getString().isEmpty()) {
                newLines.removeFirst();
            } else {
                WynntilsMod.warn("Malformed dialog [#1]: " + newLines.getFirst());
            }

            isSelectionDialog = false;
            // Separate the dialog part from any potential new "real" chat lines
            boolean dialogDone = false;
            for (Component line : newLines) {
                String codedLine = ComponentUtils.getCoded(line);
                if (!dialogDone) {
                    if (EMPTY_LINE_PATTERN.matcher(codedLine).find()) {
                        dialogDone = true;
                        // Intentionally throw away this line
                    } else {
                        dialog.push(line);
                    }
                } else {
                    if (!EMPTY_LINE_PATTERN.matcher(codedLine).find()) {
                        newChatLines.push(line);
                    }
                }
            }
        } else if (NPC_SELECT_PATTERN.matcher(firstLineCoded).find()) {
            // This is an NPC dialog screen.
            // First remove the "Press SHIFT to continue" trailer.
            newLines.removeFirst();
            if (newLines.getFirst().getString().isEmpty()) {
                newLines.removeFirst();
            } else {
                WynntilsMod.warn("Malformed dialog [#1]: " + newLines.getFirst());
            }

            isSelectionDialog = true;
            // Separate the dialog part from any potential new "real" chat lines
            boolean dialogDone = false;
            for (Component line : newLines) {
                String codedLine = ComponentUtils.getCoded(line);
                if (!dialogDone) {
                    if (codedLine.matches("^ *§[78]")) {
                        dialogDone = true;
                        // This is the first background line
                        newChatLines.push(line);
                    } else {
                        dialog.push(line);
                    }
                } else {
                    if (!EMPTY_LINE_PATTERN.matcher(codedLine).find()) {
                        newChatLines.push(line);
                    }
                }
            }
        } else {
            isSelectionDialog = false;
            // After a NPC dialog screen, Wynncraft sends a "clear screen" with line of ÀÀÀ...
            // We just ignore that part. Also, remove empty lines or lines with just the §r code
            while (!newLines.isEmpty()
                    && EMPTY_LINE_PATTERN
                            .matcher(ComponentUtils.getCoded(newLines.getFirst()))
                            .find()) {
                newLines.removeFirst();
            }

            // What remains, if any, are new chat lines
            newLines.forEach(newChatLines::push);
        }

        // Register all new chat lines
        LinkedList<Component> noConfirmationDialog = new LinkedList<>();

        newChatLines.forEach((line) -> {
            handleFakeChatLine(line, noConfirmationDialog);
        });
        if (!noConfirmationDialog.isEmpty()) {
            if (!dialog.isEmpty()) {
                WynntilsMod.warn("Malformed dialog [#4]: " + ComponentUtils.getCoded(dialog.get(0)) + ", "
                        + ComponentUtils.getCoded(noConfirmationDialog.get(0)));
            }
            if (noConfirmationDialog.size() > 1) {
                WynntilsMod.warn("Malformed dialog [#5]: " + noConfirmationDialog);
                // Keep going anyway and post the first line of the dialog
            }
            lastNpcDialog = noConfirmationDialog;
            NpcDialogEvent event = new NpcDialogEvent(noConfirmationDialog, false);
            WynntilsMod.postEvent(event);
        } else {
            // Update the new confirmation requred dialog
            System.out.println(isSelectionDialog);
            handleNpcDialog(dialog, true, isSelectionDialog);
        }
    }

    private void handleFakeChatLine(Component chatMsg, LinkedList<Component> noConfirmationDialog) {
        // This is a normal, single line chat
        saveLastChat(chatMsg);
        String coded = ComponentUtils.getCoded(chatMsg);

        RecipientType recipientType = getRecipientType(chatMsg, MessageType.BACKGROUND);
        boolean separateNPC = (dialogExtractionDependents.stream().anyMatch(Feature::isEnabled));

        if (separateNPC && recipientType == RecipientType.NPC) {
            noConfirmationDialog.add(chatMsg);
            return;
        }

        Component updatedMessage = handleChatLine(chatMsg, coded, recipientType, MessageType.BACKGROUND);
        // If the message is canceled, we do not need to cancel any packets,
        // just don't send out the chat message
        if (updatedMessage == null) return;

        McUtils.sendMessageToClient(updatedMessage);
    }

    private void saveLastChat(Component chatMsg) {
        String plainText = chatMsg.getString();
        if (!plainText.isBlank()) {
            // We store the unformatted string version to be able to compare between
            // normal and background versions
            lastRealChat = plainText;
        }
    }

    private RecipientType getRecipientType(Component message, MessageType messageType) {
        String msg = ComponentUtils.getCoded(message);

        // Check if message match a recipient category
        for (RecipientType recipientType : RecipientType.values()) {
            if (recipientType.matchPattern(msg, messageType)) {
                return recipientType;
            }
        }

        // If no specific recipient matched, it is an "info" message
        return RecipientType.INFO;
    }

    /**
     * Return a "massaged" version of the message, or null if we should cancel the
     * message entirely.
     */
    private Component handleChatLine(
            Component message, String codedMessage, RecipientType recipientType, MessageType messageType) {
        ChatMessageReceivedEvent event =
                new ChatMessageReceivedEvent(message, codedMessage, messageType, recipientType);
        WynntilsMod.postEvent(event);
        if (event.isCanceled()) return null;
        return event.getMessage();
    }

    private void handleNpcDialog(List<Component> dialog, boolean needsConfirmation, boolean isSelectionDialog) {
        // dialog could be the empty list, this means the last dialog is removed
        if (!dialog.equals(lastNpcDialog)) {
            lastNpcDialog = dialog;
            if (dialog.size() > 1) {
                WynntilsMod.warn("Malformed dialog [#3]: " + dialog);
                // Keep going anyway and post the first line of the dialog
            }
            NpcDialogEvent event = new NpcDialogEvent(dialog, needsConfirmation);
            WynntilsMod.postEvent(event);
        }
    }

    public void addNpcDialogExtractionDependent(Feature feature) {
        dialogExtractionDependents.add(feature);
    }

    public void removeNpcDialogExtractionDependent(Feature feature) {
        dialogExtractionDependents.remove(feature);
    }
}
