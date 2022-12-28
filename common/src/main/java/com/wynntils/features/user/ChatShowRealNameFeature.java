/*
 * Copyright © Wynntils 2022.
 * This file is released under AGPLv3. See LICENSE for full license details.
 */
package com.wynntils.features.user;

import com.wynntils.core.features.UserFeature;
import com.wynntils.handlers.chat.event.ChatMessageReceivedEvent;
import java.util.List;
import java.util.regex.Matcher;
import java.util.regex.Pattern;
import net.minecraft.ChatFormatting;
import net.minecraft.network.chat.Component;
import net.minecraft.network.chat.HoverEvent;
import net.minecraftforge.eventbus.api.SubscribeEvent;

public class ChatShowRealNameFeature extends UserFeature {
    // credits to avomod for part of the code
    private static final Pattern CHAT_PATTERN = Pattern.compile("§([e5b273])§o([A-Z][a-zA-Z_\\s]+)");
    private static Component eventOriginalMessage;

    @SubscribeEvent
    public void onChatMessage(ChatMessageReceivedEvent event) {
        // if module is enabled
        // if message matches
        eventOriginalMessage = event.getOriginalMessage();
        System.out.println("Original Coloured message: " + event.getOriginalCodedMessage());
        if (event.getOriginalCodedMessage().contains("§3[")) {
            for (Component siblingMessage : event.getOriginalMessage().getSiblings()) {
                System.out.println("Sibling message: " + siblingMessage);
                System.out.println("Siblings: " + siblingMessage.getSiblings());
            }
        }
        addRealNameToMessage(event.getOriginalMessage(), event.getOriginalMessage());
    }

    private static void addRealNameToMessage(Component message, Component parentMessage) {
        char colourCode = 'f';
        if (message.getSiblings().size() > 0) {
            for (Component siblingMessage : message.getSiblings()) {
                addRealNameToMessage(siblingMessage, message);
            }
        }
        if (messageHasNickHover(message)) {
            HoverEvent hover = message.getStyle().getHoverEvent();
            if (hover == null) return;
            if (hover.getValue(hover.getAction()) instanceof Component hoverText) {
                String realName =
                        hoverText.getString().split(" ")[hoverText.getString().split(" ").length - 1];
                // Save all sibling of the message
                List<Component> siblings = parentMessage.getSiblings();
                // Make a TextElement with the real name
                // Component fullMessage = Component.literal("§c(" + realName + ")§f");
                // Champion: §e, Hero: §5, VIP+: §b, VIP: §2, None: §7
                Matcher matcher = CHAT_PATTERN.matcher(message.getString());
                if (matcher.lookingAt()) {
                    colourCode = matcher.group(1).charAt(0);
                    System.out.println("Colour code: " + colourCode);
                }
                char finalColourCode = colourCode;
                Component fullMessage = Component.literal("§r§" + finalColourCode + realName)
                        .withStyle(style -> style.withHoverEvent(new HoverEvent(
                                HoverEvent.Action.SHOW_TEXT,
                                Component.literal("§r§f" + realName + "§r§7" + "'s nickname is " + "§r§f"
                                        + ChatFormatting.stripFormatting(message.getString())))));
                System.out.println("Message in siblings: " + siblings.contains(message));
                System.out.println("Message: " + message.getString());

                if (siblings.contains(message)) {
                    parentMessage.getSiblings().set(siblings.indexOf(message), fullMessage);
                } else if (eventOriginalMessage.getSiblings().contains(message)) {
                    eventOriginalMessage.getSiblings().set(siblings.indexOf(message), fullMessage);
                } else {
                    System.out.println("Message not found in siblings");
                }
            }
        }
    }

    private static boolean messageHasNickHover(Component message) {
        HoverEvent hover = message.getStyle().getHoverEvent();
        if (hover != null && hover.getValue(hover.getAction()) instanceof Component hoverText) {
            return hoverText.getString().contains("real username");
        }
        return false;
    }
}
