package com.wynntils.features.user;

import com.wynntils.core.features.UserFeature;
import com.wynntils.handlers.chat.event.ChatMessageReceivedEvent;
import net.minecraft.network.chat.HoverEvent;
import net.minecraft.network.chat.Component;
import net.minecraftforge.eventbus.api.SubscribeEvent;

import java.util.List;


public class ChatShowRealNameFeature extends UserFeature {
    // credits to avomod for part of the code

    @SubscribeEvent
    public void onChatMessage(ChatMessageReceivedEvent event) {
        // if module is enabled

        addRealNameToMessage(event.getOriginalMessage());
    }

    private static void addRealNameToMessage(Component message) {
        if (message.getSiblings().size() > 0) {
            for (Component siblingMessage : message.getSiblings()) {
                addRealNameToMessage(siblingMessage);
            }
        }
        if (messageHasNickHover(message)) {
            HoverEvent hover = message.getStyle().getHoverEvent();
            if (hover == null) return;
            if (hover.getValue(hover.getAction()) instanceof Component hoverText) {
                String realName = hoverText.getString().split(" ")[hoverText.getString().split(" ").length - 1];
                // Save all sibling of the message
                List<Component> siblings = message.getSiblings();
                // Make a TextElement with the real name
                //Component fullMessage = Component.literal("§c(" + realName + ")§f");
                Component fullMessage = Component.literal(realName);
                // Add all old siblings to the real name
                fullMessage.getSiblings().addAll(siblings);
                // Clears everything except for the nickname (and [***] stuff if in guild chat)
                message.getSiblings().clear();
                // Adds the real name + the original message after the nickname
                message.getSiblings().add(fullMessage);
                System.out.println(message);
            }
//            message.getSiblings().addAll(TextElement.of("§c(" + realName + ")§f").getWithStyle(message.getStyle())); // This is not used due to it appearing after the message in guild chat
        }
    }

    public static boolean messageHasNickHover(Component message) {
        HoverEvent hover = message.getStyle().getHoverEvent();
        if (hover != null && hover.getValue(hover.getAction()) instanceof Component hoverText) {
            return hoverText.getString().contains("real username");
        }
        return false;
    }
}
