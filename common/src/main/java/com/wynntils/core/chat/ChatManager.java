/*
 * Copyright © Wynntils 2022.
 * This file is released under AGPLv3. See LICENSE for full license details.
 */
package com.wynntils.core.chat;

import com.wynntils.core.WynntilsMod;
import com.wynntils.mc.event.ChatPacketReceivedEvent;
import com.wynntils.mc.utils.ComponentUtils;
import com.wynntils.mc.utils.McUtils;
import com.wynntils.wc.event.ChatMessageReceivedEvent;
import com.wynntils.wc.event.NpcDialogEvent;
import com.wynntils.wc.utils.WynnUtils;
import java.util.Arrays;
import java.util.Collections;
import java.util.LinkedList;
import java.util.List;
import java.util.regex.Matcher;
import java.util.regex.Pattern;
import net.minecraft.network.chat.ChatType;
import net.minecraft.network.chat.Component;
import net.minecraft.network.chat.TextComponent;
import net.minecraftforge.eventbus.api.EventPriority;
import net.minecraftforge.eventbus.api.SubscribeEvent;

public class ChatManager {
    private static final Pattern NPC_FINAL_PATTERN =
            Pattern.compile(" +§[47]Press §r§[cf](SNEAK|SHIFT) §r§[47]to continue§r$");

    private static boolean extractDialog = false;
    private static String lastRealChat = null;
    private static List<String> lastNpcDialog = List.of();

    public static void init() {
        WynntilsMod.getEventBus().register(ChatManager.class);
    }

    @SubscribeEvent(priority = EventPriority.HIGHEST)
    public static void onChatReceived(ChatPacketReceivedEvent e) {
        if (!WynnUtils.onServer()) return;
        if (e.getType() == ChatType.GAME_INFO) return;

        Component message = e.getMessage();
        String codedMessage = ComponentUtils.getCoded(message);
        if (!codedMessage.contains("\n")) {
            saveLastChat(ComponentUtils.getCoded(message));
            MessageType messageType = e.getType() == ChatType.SYSTEM ? MessageType.SYSTEM : MessageType.NORMAL;
            Component updatedMessage = handleChatLine(message, codedMessage, messageType);
            if (updatedMessage == null) {
                e.setCanceled(true);
            } else if (!updatedMessage.equals(message)) {
                e.setMessage(updatedMessage);
            }
            return;
        }

        if (extractDialog) {
            handleMultilineMessage(codedMessage);
            e.setCanceled(true);
        }
    }

    private static void handleMultilineMessage(String msg) {
        LinkedList<String> lines = new LinkedList<>(Arrays.asList(msg.split("\\n")));
        // From now on, we'll work on reversed lists
        Collections.reverse(lines);
        LinkedList<String> newLines = new LinkedList<>();
        if (lastRealChat == null) {
            // If we have no history, all lines are to be considered new
            lines.forEach(newLines::addLast);
        } else {
            // Figure out what's new since last chat message
            for (String line : lines) {
                String noCodes = ComponentUtils.stripFormattingCodes(line);
                if (noCodes.equals(lastRealChat)) break;
                newLines.addLast(line);
            }
        }

        if (newLines.isEmpty()) {
            // No new lines has appeared since last registered chat line.
            // We could just have a dialog that disappeared, so we must signal this
            handleNpcDialog(List.of());
            return;
        }

        if (newLines.getLast().isEmpty()) {
            // Wynntils add an empty line before the NPC dialog; remove it
            newLines.removeLast();
        }

        LinkedList<String> newChatLines = new LinkedList<>();
        LinkedList<String> dialog = new LinkedList<>();

        String trailingLine = newLines.getFirst();
        Matcher m = NPC_FINAL_PATTERN.matcher(trailingLine);
        if (m.find()) {
            // This is an NPC dialog screen.
            // First remove the "Press SHIFT to continue" trailer.
            newLines.removeFirst();
            if (newLines.getFirst().isEmpty()) {
                newLines.removeFirst();
            } else {
                WynntilsMod.warn("Malformed dialog [#1]: " + newLines.getFirst());
            }

            // Separate the dialog part from any potential new "real" chat lines
            boolean dialogDone = false;
            for (String line : newLines) {
                if (!dialogDone) {
                    if (line.equals("§r")) {
                        dialogDone = true;
                        // Intentionally throw away this line
                    } else {
                        dialog.push(line);
                    }
                } else {
                    newChatLines.push(line);
                }
            }
        } else {
            // After a NPC dialog screen, Wynncraft sends a "clear screen" with line of ÀÀÀ...
            // We just ignore that part
            if (trailingLine.matches("À+")) {
                newLines.removeFirst();
                if (newLines.getFirst().equals("§r")) {
                    newLines.removeFirst();
                } else {
                    WynntilsMod.warn("Malformed dialog [#2]: " + newLines.getFirst());
                }
            }

            // What remains, if any, are new chat lines
            newLines.forEach(newChatLines::push);
        }

        // Register all new chat lines
        newChatLines.forEach(line -> handleFakeChatLine(line));

        // Update the new dialog
        handleNpcDialog(dialog);
    }

    private static void handleFakeChatLine(String codedString) {
        // This is a normal, single line chat but coded with format codes
        saveLastChat(codedString);
        TextComponent message = new TextComponent(codedString);
        Component updatedMessage = handleChatLine(message, codedString, MessageType.BACKGROUND);
        // If the message is canceled, we do not need to cancel any packets,
        // just don't send out the chat message
        if (updatedMessage == null) return;

        McUtils.sendMessageToClient(updatedMessage);
    }

    private static void saveLastChat(String codedString) {
        String msg = ComponentUtils.stripFormattingCodes(codedString);
        if (!msg.isBlank()) {
            lastRealChat = msg;
        }
    }

    private static RecipientType getRecipientType(Component message, MessageType messageType) {
        String msg = ComponentUtils.getCoded(message);
        if (messageType == MessageType.SYSTEM) {
            // System type messages can only be shouts or "info" messages
            // We call this MessageType.NORMAL anyway...
            if (RecipientType.SHOUT.matchPattern(msg, MessageType.NORMAL)) {
                return RecipientType.SHOUT;
            }
            return RecipientType.INFO;
        } else {
            for (RecipientType recipientType : RecipientType.values()) {
                if (recipientType.matchPattern(msg, messageType)) {
                    return recipientType;
                }
            }

            // If no specific recipient matched, it is an "info" message
            return RecipientType.INFO;
        }
    }

    /**
     * Return a "massaged" version of the message, or null if we should cancel the
     * message entirely.
     */
    private static Component handleChatLine(Component message, String codedMessage, MessageType messageType) {
        RecipientType recipientType = getRecipientType(message, messageType);

        System.out.println("Handling chat: " + ComponentUtils.getCoded(message) + ", type:" + messageType
                + ", recipient: " + recipientType);

        ChatMessageReceivedEvent event =
                new ChatMessageReceivedEvent(message, codedMessage, messageType, recipientType);
        WynntilsMod.getEventBus().post(event);
        if (event.isCanceled()) return null;
        return event.getMessage();
    }

    private static void handleNpcDialog(List<String> dialog) {
        // dialog could be the empty list, this means the last dialog is removed
        if (!dialog.equals(lastNpcDialog)) {
            lastNpcDialog = dialog;
            if (dialog.size() > 1) {
                WynntilsMod.warn("Malformed dialog [#3]: " + dialog);
            } else {
                NpcDialogEvent event = new NpcDialogEvent(dialog.isEmpty() ? null : dialog.get(0));
                WynntilsMod.getEventBus().post(event);
            }
        }
    }

    public static void enableNpcDialogExtraction() {
        extractDialog = true;
    }

    public static void disableNpcDialogExtraction() {
        extractDialog = false;
    }
}
