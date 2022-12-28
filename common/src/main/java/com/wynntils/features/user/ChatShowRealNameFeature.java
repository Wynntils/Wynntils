/*
 * Copyright © Wynntils 2022.
 * This file is released under AGPLv3. See LICENSE for full license details.
 */
package com.wynntils.features.user;

import com.wynntils.core.features.UserFeature;
import com.wynntils.handlers.chat.event.ChatMessageReceivedEvent;
import java.util.List;
import net.minecraft.ChatFormatting;
import net.minecraft.network.chat.Component;
import net.minecraft.network.chat.HoverEvent;
import net.minecraftforge.eventbus.api.SubscribeEvent;

public class ChatShowRealNameFeature extends UserFeature {
    // credits to avomod for part of the code
    private static Component eventOriginalMessage;
    private static String eventOriginalColorMessage;

    @SubscribeEvent
    public void onChatMessage(ChatMessageReceivedEvent event) {
        eventOriginalMessage = event.getOriginalMessage();
        eventOriginalColorMessage = event.getOriginalCodedMessage();
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
                List<Component> siblings = parentMessage.getSiblings();
                // Champion: §e, Hero: §5, VIP+: §b, VIP: §2, None: §7
                System.out.println("Parent message: " + parentMessage + " | Siblings: " + parentMessage.getSiblings());
                String colourCode = getColourCodeByRank(eventOriginalColorMessage);
                Component fullMessage = Component.literal("§r§" + colourCode + realName)
                        .withStyle()
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

                System.out.println(parentMessage.getSiblings());
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

    private static String getColourCodeByRank(String message) {
        if (message.contains("[§r§b§k|§r§6CHAMPION§r§b§k|§r§e]")) {
            return "e";
        } else if (message.contains("[§r§dHERO§r§5]")) {
            return "5";
        } else if (message.contains("[§r§3VIP+§r§b]")) {
            return "b";
        } else if (message.contains("[§r§aVIP§r§2]")) {
            return "2";
        } else {
            return "e";
        }
    }
}
