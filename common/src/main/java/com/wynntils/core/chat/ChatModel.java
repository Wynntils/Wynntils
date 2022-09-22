/*
 * Copyright © Wynntils 2022.
 * This file is released under AGPLv3. See LICENSE for full license details.
 */
package com.wynntils.core.chat;

import com.wynntils.core.WynntilsMod;
import com.wynntils.core.features.Feature;
import com.wynntils.core.managers.Model;
import com.wynntils.mc.event.ChatPacketReceivedEvent;
import com.wynntils.mc.utils.ComponentUtils;
import com.wynntils.mc.utils.McUtils;
import com.wynntils.wynn.event.ChatMessageReceivedEvent;
import com.wynntils.wynn.event.NpcDialogEvent;
import java.util.Collections;
import java.util.HashSet;
import java.util.LinkedList;
import java.util.List;
import java.util.Set;
import java.util.regex.Pattern;
import net.minecraft.network.chat.ChatType;
import net.minecraft.network.chat.Component;
import net.minecraftforge.eventbus.api.EventPriority;
import net.minecraftforge.eventbus.api.SubscribeEvent;

/**
 * The responsibility of this class is to act as the first gateway for incoming
 * chat messages from Wynncraft. Chat messages in vanilla comes in three types,
 * CHAT, SYSTEM and GAME_INFO. The latter is the "action bar", and is handled
 * elsewhere. The difference between CHAT and SYSTEM is almost academic; it looks
 * the same to users, but Wynntils put different messages in different categories.
 * Most are CHAT, but a few are SYSTEM. When we pass on the messages, we use the
 * term "NORMAL" instead of "CHAT".
 * <p>
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
public final class ChatModel extends Model {
    private static final Pattern NPC_FINAL_PATTERN =
            Pattern.compile(" +§[47]Press §r§[cf](SNEAK|SHIFT) §r§[47]to continue(§r)?$");
    private static final Pattern NPC_DIALOGUE_PATTERN = Pattern.compile("§r§7\\[\\d+/\\d+\\] §r§2.+: §r§a.*");

    private static final Set<Feature> dialogExtractionDependents = new HashSet<>();
    private static String lastRealChat = null;
    private static List<String> lastNpcDialog = List.of();

    /** Needed for all Models */
    public static void init() {}

    @SubscribeEvent(priority = EventPriority.HIGHEST)
    public static void onChatReceived(ChatPacketReceivedEvent e) {
        if (e.getType() == ChatType.GAME_INFO) return;

        Component message = e.getMessage();
        String codedMessage = ComponentUtils.getCoded(message);

        // Sometimes there is just a trailing newline; that does not
        // make it a multiline message
        if (!codedMessage.contains("\n") || codedMessage.indexOf("\n") == (codedMessage.length() - 1)) {
            saveLastChat(codedMessage);
            MessageType messageType = e.getType() == ChatType.SYSTEM ? MessageType.SYSTEM : MessageType.NORMAL;
            Component updatedMessage = handleChatLine(message, codedMessage, messageType);
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

    private static void handleMultilineMessage(Component message) {
        List<Component> lines = new LinkedList<>(message.getSiblings());
        // From now on, we'll work on reversed lists
        Collections.reverse(lines);
        LinkedList<Component> newLines = new LinkedList<>();
        if (lastRealChat == null) {
            // If we have no history, all lines are to be considered new
            lines.forEach(newLines::addLast);
        } else {
            // Figure out what's new since last chat message
            for (Component line : lines) {
                String noCodes = ComponentUtils.getUnformatted(line);
                // Remove all empty lines
                if (noCodes.isBlank()) continue;

                if (noCodes.equals(lastRealChat)) break;
                newLines.addLast(line);
            }
        }

        if (newLines.isEmpty()) {
            // No new lines has appeared since last registered chat line.
            // We could just have a dialog that disappeared, so we must signal this
            handleNpcDialog(List.of(), true);
            return;
        }

        if (newLines.getLast().getString().isEmpty()) {
            // Wynntils add an empty line before the NPC dialog; remove it
            newLines.removeLast();
        }

        LinkedList<Component> newChatLines = new LinkedList<>();
        LinkedList<String> dialog = new LinkedList<>();

        String firstNewLineCoded = ComponentUtils.getCoded(newLines.getFirst());

        boolean nonBlockingDialogue = false;

        if (NPC_DIALOGUE_PATTERN.matcher(firstNewLineCoded).matches()) {
            nonBlockingDialogue = true;
            handleNpcDialog(List.of(firstNewLineCoded), false);
        } else if (NPC_FINAL_PATTERN.matcher(firstNewLineCoded).find()) {
            // This is an NPC dialog screen.
            // First remove the "Press SHIFT to continue" trailer.
            newLines.removeFirst();

            // Separate the dialog part from any potential new "real" chat lines
            for (Component lineComponent : newLines) {
                String line = ComponentUtils.getCoded(lineComponent);

                if (NPC_DIALOGUE_PATTERN.matcher(line).find()) {
                    dialog.push(line);
                } else {
                    newChatLines.push(lineComponent);
                }
            }
        } else {
            // What remains, if any, are new chat lines
            newLines.forEach(newChatLines::push);
        }

        // Register all new chat lines
        newChatLines.forEach(ChatModel::handleFakeChatLine);

        if (!nonBlockingDialogue) {
            // Update the new dialog
            handleNpcDialog(dialog, true);
        }
    }

    private static void handleFakeChatLine(Component lineComponent) {
        // This is a normal, single line chat but coded with format codes
        saveLastChat(ComponentUtils.getUnformatted(lineComponent));
        Component updatedMessage =
                handleChatLine(lineComponent, ComponentUtils.getCoded(lineComponent), MessageType.BACKGROUND);
        // If the message is canceled, we do not need to cancel any packets,
        // just don't send out the chat message
        if (updatedMessage == null) return;

        McUtils.sendMessageToClient(updatedMessage);
    }

    private static void saveLastChat(String codedString) {
        String msg = ComponentUtils.stripFormatting(codedString);
        if (!msg.isBlank()) {
            lastRealChat = msg;
        }
    }

    private static RecipientType getRecipientType(Component message, MessageType messageType) {
        String msg = ComponentUtils.getCoded(message);

        // Check if message match a recipient category
        if (messageType == MessageType.SYSTEM) {
            // System type messages can only be shouts or "info" messages
            // We call this MessageType.NORMAL anyway...
            if (RecipientType.SHOUT.matchPattern(msg, MessageType.NORMAL)) {
                return RecipientType.SHOUT;
            }
        } else {
            for (RecipientType recipientType : RecipientType.values()) {
                if (recipientType.matchPattern(msg, messageType)) {
                    return recipientType;
                }
            }
        }

        // If no specific recipient matched, it is an "info" message
        return RecipientType.INFO;
    }

    /**
     * Return a "massaged" version of the message, or null if we should cancel the
     * message entirely.
     */
    private static Component handleChatLine(Component message, String codedMessage, MessageType messageType) {
        RecipientType recipientType = getRecipientType(message, messageType);

        ChatMessageReceivedEvent event =
                new ChatMessageReceivedEvent(message, codedMessage, messageType, recipientType);
        WynntilsMod.postEvent(event);
        if (event.isCanceled()) return null;
        return event.getMessage();
    }

    private static void handleNpcDialog(List<String> dialog, boolean nonBlocking) {
        // dialog could be the empty list, this means the last dialog is removed
        if (!dialog.equals(lastNpcDialog)) {
            lastNpcDialog = dialog;
            if (dialog.size() > 1) {
                WynntilsMod.warn("Malformed dialog [#1]: " + dialog);
                // Keep going anyway and post the first line of the dialog
            }
            NpcDialogEvent event = new NpcDialogEvent(dialog.isEmpty() ? null : dialog.get(0), !nonBlocking);
            WynntilsMod.postEvent(event);
        }
    }

    public static void addNpcDialogExtractionDependent(Feature feature) {
        dialogExtractionDependents.add(feature);
    }

    public static void removeNpcDialogExtractionDependent(Feature feature) {
        dialogExtractionDependents.remove(feature);
    }
}
