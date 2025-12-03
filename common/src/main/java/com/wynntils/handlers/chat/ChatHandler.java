/*
 * Copyright © Wynntils 2022-2025.
 * This file is released under LGPLv3. See LICENSE for full license details.
 */
package com.wynntils.handlers.chat;

import com.wynntils.core.WynntilsMod;
import com.wynntils.core.components.Handler;
import com.wynntils.core.components.Models;
import com.wynntils.core.mod.event.WynncraftConnectionEvent;
import com.wynntils.core.text.StyledText;
import com.wynntils.core.text.type.StyleType;
import com.wynntils.handlers.chat.event.ChatMessageEvent;
import com.wynntils.handlers.chat.type.MessageType;
import com.wynntils.handlers.chat.type.RecipientType;
import com.wynntils.mc.event.MobEffectEvent;
import com.wynntils.mc.event.SystemMessageEvent;
import com.wynntils.mc.event.TickEvent;
import com.wynntils.models.worlds.event.WorldStateEvent;
import com.wynntils.models.worlds.type.WorldState;
import com.wynntils.utils.mc.McUtils;
import java.util.List;
import net.minecraft.network.chat.Component;
import net.neoforged.bus.api.EventPriority;
import net.neoforged.bus.api.SubscribeEvent;

/**
 * The responsibility of this class is to act as the first gateway for incoming
 * chat messages from Wynncraft. We need to solve two problems here. First,
 * Wynncraft will resend the entire chat history with new lines added at the end
 * at certain situations, most notably when the user is in a NPC dialog. These
 * are called "pages" in this context. We need to detect these pages, and split
 * out the actual content, so it can be sent out as a NPC dialogue event.
 *
 * A complication is that new chat messages can be sent while the user is in
 * page mode. We call these "background" messages, and they are are formatted
 * differently to be gray and tuned-down, which makes the normal regexp
 * matching fail. They are also sent as pure strings with formatting codes,
 * instead of Components as normal one-line chats are. This mean things like
 * hover and onClick information is lost. (There is nothing we can do about
 * this, it is a Wynncraft limitation.) We send out these chat messages one by
 * one, as they would have appeared if we were not in a NPC dialog, but we tag
 * them as BACKGROUND to signal that formatting is different.
 *
 * In a normal vanilla setting, the last "screen" that Wynncraft sends out, the
 * messages are re-colored to have their normal colors restored (hover and
 * onClick as still missing, though). Currently, we do not handle this, since
 * it would mean sending out information that already sent chat lines would
 * need to be updated to a different formatting. This could be done, but
 * requires extra logic, and most importantly, a way to update already printed
 * chat lines.
 *
 * Second, we need to classify the incoming chat messages according to their
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
    private final ChatPageDetector pageDetector = new ChatPageDetector();
    private final ChatPageProcessor pageProcessor = new ChatPageProcessor();
    private boolean isLocal;

    @SubscribeEvent
    public void onConnectionChange(WynncraftConnectionEvent.Connected event) {
        pageDetector.reset();
        pageProcessor.reset();
    }

    @SubscribeEvent
    public void onWorldStateChange(WorldStateEvent event) {
        // We don't want to reset on a new world join, only after leaving a world
        if (event.getNewState() == WorldState.WORLD) return;

        pageDetector.reset();
        pageProcessor.reset();
    }

    @SubscribeEvent
    public void onTick(TickEvent event) {
        pageDetector.onTick();
    }

    @SubscribeEvent(priority = EventPriority.HIGHEST)
    public void onSystemChatReceived(SystemMessageEvent.ChatReceivedEvent event) {
        if (needPageDetector()) {
            boolean handled = pageDetector.processIncomingChatMessage(event);
            if (handled) return;
        }

        handleIncomingChatMessage(event);
    }

    @SubscribeEvent
    public void onStatusEffectUpdate(MobEffectEvent.Update event) {
        pageProcessor.onStatusEffectUpdate(event);
    }

    @SubscribeEvent
    public void onStatusEffectRemove(MobEffectEvent.Remove event) {
        pageProcessor.onStatusEffectRemove(event);
    }

    /**
     * Callback from ChatPageDetector when a page is detected.
     */
    void handlePage(List<StyledText> pageContent) {
        pageProcessor.handlePage(pageContent);
    }

    /**
     * Callback from ChatPageDetector when chat message needs to be sent with a delay due to analysis reasons
     */
    void sendDelayedChat(Component msg) {
        StyledText message = StyledText.fromComponent(msg);
        StyledText newMessage = processChatMessage(message, MessageType.FOREGROUND);

        if (newMessage == null) return;

        // Send it without triggering any new events
        McUtils.mc().gui.getChat().addMessage(newMessage.getComponent());
    }

    /**
     * Callback from ChatPageDetector when background chat message is detected
     */
    void handleBackgroundLine(StyledText message) {
        StyledText newMessage = processChatMessage(message, MessageType.BACKGROUND);

        if (newMessage == null) return;

        // Send it without triggering any new events
        McUtils.mc().gui.getChat().addMessage(newMessage.getComponent());
    }

    private void handleIncomingChatMessage(SystemMessageEvent.ChatReceivedEvent event) {
        StyledText message = StyledText.fromComponent(event.getMessage());
        StyledText newMessage = processChatMessage(message, MessageType.FOREGROUND);

        if (newMessage == null) {
            event.setCanceled(true);
            return;
        }
        if (!newMessage.equals(message)) {
            event.setMessage(newMessage.getComponent());
        }
    }

    private StyledText processChatMessage(StyledText message, MessageType messageType) {
        // All chat messages will pass through this method, one way or another
        RecipientType recipientType = getRecipientType(message, messageType);

        // Normally § codes are stripped from the log; need this to be able to debug chat formatting
        WynntilsMod.info("[CHAT/" + recipientType + (messageType == MessageType.BACKGROUND ? "/bg" : "") + "] "
                + message.getString(StyleType.COMPLETE).replace("§", "&"));

        ChatMessageEvent.Match receivedEvent = new ChatMessageEvent.Match(message, messageType, recipientType);
        WynntilsMod.postEvent(receivedEvent);
        if (receivedEvent.isChatCanceled()) return null;

        ChatMessageEvent.Edit rewriteEvent = new ChatMessageEvent.Edit(message, messageType, recipientType);
        WynntilsMod.postEvent(rewriteEvent);
        return rewriteEvent.getMessage();
    }

    public RecipientType getRecipientType(StyledText codedMessage, MessageType messageType) {
        // Check if message match a recipient category
        for (RecipientType recipientType : RecipientType.values()) {
            if (recipientType.matchPattern(codedMessage, messageType)) {
                return recipientType;
            }
        }

        // If no specific recipient matched, it is an "info" message
        return RecipientType.INFO;
    }

    private boolean needPageDetector() {
        // This is still a bit wonky...
        return Models.NpcDialogue.isNpcDialogExtractionRequired();
    }

    public void setLocalMessage(boolean isLocal) {
        this.isLocal = isLocal;
    }

    public boolean isLocalMessage() {
        return isLocal;
    }
}
