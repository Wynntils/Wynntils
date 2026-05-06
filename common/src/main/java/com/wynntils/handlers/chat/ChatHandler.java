/*
 * Copyright © Wynntils 2022-2026.
 * This file is released under LGPLv3. See LICENSE for full license details.
 */
package com.wynntils.handlers.chat;

import com.wynntils.core.WynntilsMod;
import com.wynntils.core.components.Handler;
import com.wynntils.core.components.Handlers;
import com.wynntils.core.components.Models;
import com.wynntils.core.text.StyledText;
import com.wynntils.core.text.type.StyleType;
import com.wynntils.handlers.chat.event.ChatMessageEvent;
import com.wynntils.handlers.chat.type.RecipientType;
import com.wynntils.mc.event.SystemMessageEvent;
import com.wynntils.mc.event.TickEvent;
import com.wynntils.utils.mc.McUtils;
import java.util.Iterator;
import java.util.LinkedHashSet;
import net.neoforged.bus.api.EventPriority;
import net.neoforged.bus.api.SubscribeEvent;

/**
 * The responsibility of this class is to act as the first gateway for incoming
 * chat messages from Wynncraft.
 *
 * We need to classify the incoming chat messages according to their
 * recipient type. The typical use case for this is to separate chat messages
 * in different tabs. We do this using the regexp patterns in RecipientType,
 * and we classify the incoming messages according to if they are sent to the
 * guild, party, global chat, etc. Messages that do not match any of these
 * categories are called "info" messages, and are typically automated responses
 * or announcements. Messages that do match any other category, are sent by
 * other users (what could really be termed "chat"). The one exception is guild
 * messages, which can also be e.g. WAR announcements. (Unfortunately, there is
 * no way to distinguish these from chat sent by a build member named "WAR", or
 * "INFO", or..., so if these need to be separated, it has to happen in a later
 * stage).
 */
public final class ChatHandler extends Handler {
    private static final int TICKS_PER_EXECUTE = 100;

    private final LinkedHashSet<QueuedMessage> chatQueue = new LinkedHashSet<>();
    private int chatQueueTicks = 0;

    @SubscribeEvent(priority = EventPriority.HIGHEST)
    public void onSystemChatReceived(SystemMessageEvent.ChatReceivedEvent event) {
        handleIncomingChatMessage(event);
    }

    private void handleIncomingChatMessage(SystemMessageEvent.ChatReceivedEvent event) {
        StyledText message = StyledText.fromComponent(event.getMessage());
        StyledText newMessage = processChatMessage(message);

        if (newMessage == null) {
            event.setCanceled(true);
            return;
        }
        if (!newMessage.equals(message)) {
            event.setMessage(newMessage.getComponent());
        }
    }

    private StyledText processChatMessage(StyledText message) {
        // All chat messages will pass through this method, one way or another
        RecipientType recipientType = getRecipientType(message);

        // Normally § codes are stripped from the log; need this to be able to debug chat formatting
        WynntilsMod.info("[CHAT/" + recipientType + "] "
                + message.getString(StyleType.COMPLETE).replace("§", "&"));

        ChatMessageEvent.Match receivedEvent = new ChatMessageEvent.Match(message, recipientType);
        WynntilsMod.postEvent(receivedEvent);
        if (receivedEvent.isChatCanceled()) return null;

        ChatMessageEvent.Edit rewriteEvent = new ChatMessageEvent.Edit(message, recipientType);
        WynntilsMod.postEvent(rewriteEvent);
        return rewriteEvent.getMessage();
    }

    public RecipientType getRecipientType(StyledText codedMessage) {
        // Check if message match a recipient category
        for (RecipientType recipientType : RecipientType.values()) {
            if (recipientType.matchPattern(codedMessage)) {
                return recipientType;
            }
        }

        // If no specific recipient matched, it is an "info" message
        return RecipientType.INFO;
    }

    @SubscribeEvent
    public void onTick(TickEvent e) {
        if (!Models.WorldState.onWorld()) return;

        chatQueueTicks--;

        if (chatQueueTicks <= 0 && !chatQueue.isEmpty()) {
            Iterator<QueuedMessage> it = chatQueue.iterator();
            QueuedMessage queued = it.next();
            it.remove();

            WynntilsMod.info("Executing queued chat message: " + queued.content());
            queued.send();
            chatQueueTicks = TICKS_PER_EXECUTE;
        }
    }

    public void queueChat(String message) {
        enqueue(new QueuedMessage(message, false));
    }

    public void queueChatCommand(String command) {
        enqueue(new QueuedMessage(command, true));
    }

    private void enqueue(QueuedMessage queued) {
        if (chatQueueTicks <= 0) {
            WynntilsMod.info("Executing queued chat message immediately: " + queued.content());
            queued.send();
            chatQueueTicks = TICKS_PER_EXECUTE;
        } else {
            chatQueue.add(queued);
        }
    }

    private record QueuedMessage(String content, boolean isCommand) {
        void send() {
            if (isCommand) {
                Handlers.Command.queueCommand(content);
            } else {
                McUtils.sendChat(content);
            }
        }
    }
}
