/*
 * Copyright © Wynntils 2022.
 * This file is released under AGPLv3. See LICENSE for full license details.
 */
package com.wynntils.core.chat;

import com.wynntils.core.WynntilsMod;
import com.wynntils.mc.event.ChatReceivedEvent;
import com.wynntils.mc.utils.ComponentUtils;
import com.wynntils.mc.utils.McUtils;
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
    private static final boolean EXTRACT_DIALOG = true;
    private static final Pattern NPC_FINAL_PATTERN =
            Pattern.compile(" +§[47]Press §r§[cf](SNEAK|SHIFT) §r§[47]to continue§r$");

    public static void init() {
        WynntilsMod.getEventBus().register(ChatManager.class);
    }

    private static String lastRealChat = null;
    private static List<String> lastNpcDialog = List.of();

    private static boolean handleChatLine(Component message) {
        // If we want to cancel a chat line, return false here
        return true;
    }

    @SubscribeEvent(priority = EventPriority.HIGHEST)
    public static void onChatReceived(ChatReceivedEvent e) {
        if (!WynnUtils.onServer()) return;
        if (e.getType() != ChatType.CHAT) return;

        Component message = e.getMessage();
        String msg = ComponentUtils.getFormatted(message);
        if (!msg.contains("\n")) {
            saveLastChat(ComponentUtils.getFormatted(message));
            if (!handleChatLine(message)) {
                e.setCanceled(true);
            }
            return;
        }

        if (EXTRACT_DIALOG) {
            handleMultilineMessage(msg);
            e.setCanceled(true);
        }
    }

    private static String stripFormattingCodes(String msg) {
        return msg.replaceAll("§[0-9a-fklmnor]", "");
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
                String noCodes = stripFormattingCodes(line);
                if (noCodes.equals(lastRealChat)) break;
                newLines.addLast(line);
            }
        }

        if (newLines.isEmpty()) {
            // No new lines has appeared since last registered chat line.
            // We could just have a dialog that disappeared, so we must signal this
            handleNewNpcDialog(List.of());
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
        newChatLines.forEach(ChatManager::handleFakeChatLine);

        // Update the new dialog
        handleNewNpcDialog(dialog);
    }

    private static void handleNewNpcDialog(List<String> dialog) {
        // dialog could be the empty list, this means the last dialog is removed
        if (!dialog.equals(lastNpcDialog)) {
            lastNpcDialog = dialog;
            for (String dialogLine : dialog) {
                McUtils.sendMessageToClient(new TextComponent("NPC: " + dialogLine));
            }
        }
    }

    private static void handleFakeChatLine(String codedString) {
        // This is a normal, single line chat but coded with format codes
        saveLastChat(codedString);
        TextComponent message = new TextComponent(codedString);
        if (handleChatLine(message)) {
            McUtils.sendMessageToClient(message);
        }
    }

    private static void saveLastChat(String codedString) {
        String msg = stripFormattingCodes(codedString);
        if (!msg.isBlank()) {
            lastRealChat = msg;
        }
    }
}
