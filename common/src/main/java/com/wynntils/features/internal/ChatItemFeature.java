/*
 * Copyright © Wynntils 2022.
 * This file is released under AGPLv3. See LICENSE for full license details.
 */
package com.wynntils.features.internal;

import com.google.common.collect.ImmutableList;
import com.wynntils.core.features.InternalFeature;
import com.wynntils.core.webapi.WebManager;
import com.wynntils.mc.event.ChatReceivedEvent;
import com.wynntils.mc.event.KeyInputEvent;
import com.wynntils.mc.mixin.accessors.ChatScreenAccessor;
import com.wynntils.mc.utils.McUtils;
import com.wynntils.wc.utils.ChatItemUtils;
import com.wynntils.wc.utils.WynnUtils;
import java.util.HashMap;
import java.util.Map;
import java.util.regex.Matcher;
import net.minecraft.client.gui.components.EditBox;
import net.minecraft.client.gui.screens.ChatScreen;
import net.minecraft.network.chat.Component;
import net.minecraftforge.eventbus.api.SubscribeEvent;
import org.lwjgl.glfw.GLFW;

public class ChatItemFeature extends InternalFeature {

    private final Map<String, String> chatItems = new HashMap<>();

    @Override
    protected void onInit(ImmutableList.Builder<Condition> conditions) {
        conditions.add(new WebLoadedCondition());
    }

    @Override
    protected boolean onEnable() {
        return WebManager.isItemListLoaded() || WebManager.tryLoadItemList();
    }

    @SubscribeEvent
    public void onKeyTyped(KeyInputEvent e) {
        if (!WynnUtils.onWorld()) return;
        if (!(McUtils.mc().screen instanceof ChatScreen chatScreen)) return;

        EditBox chatInput = ((ChatScreenAccessor) chatScreen).getChatInput();

        if (!chatItems.isEmpty() && (e.getKey() == GLFW.GLFW_KEY_ENTER || e.getKey() == GLFW.GLFW_KEY_KP_ENTER)) {
            // replace the placeholder strings with the actual encoded strings
            for (Map.Entry<String, String> item : chatItems.entrySet()) {
                chatInput.setValue(chatInput.getValue().replace("<" + item.getKey() + ">", item.getValue()));
            }
            chatItems.clear();
            return;
        }

        // replace encoded strings with placeholders for less confusion
        Matcher m = ChatItemUtils.chatItemMatcher(chatInput.getValue());
        while (m.find()) {
            String encodedItem = m.group();
            String name = m.group("Name");
            while (chatItems.containsKey(name)) { // avoid overwriting entries
                name += "_";
            }

            chatInput.setValue(chatInput.getValue().replace(encodedItem, "<" + name + ">"));
            chatItems.put(name, encodedItem);
        }
    }

    @SubscribeEvent
    public void onChatReceived(ChatReceivedEvent e) {
        if (!WynnUtils.onWorld()) return;

        Component message = e.getMessage();
        if (!ChatItemUtils.chatItemMatcher(message.getString()).find()) return; // no chat items to replace

        e.setMessage(ChatItemUtils.insertItemComponents(message));
    }
}
