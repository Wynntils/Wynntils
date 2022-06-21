/*
 * Copyright © Wynntils 2022.
 * This file is released under AGPLv3. See LICENSE for full license details.
 */
package com.wynntils.features.internal;

import com.google.common.collect.ImmutableList;
import com.wynntils.core.features.InternalFeature;
import com.wynntils.core.features.properties.EventListener;
import com.wynntils.core.webapi.WebManager;
import com.wynntils.mc.event.ChatReceivedEvent;
import com.wynntils.mc.event.KeyInputEvent;
import com.wynntils.mc.mixin.accessors.ChatScreenAccessor;
import com.wynntils.mc.utils.McUtils;
import com.wynntils.wc.custom.item.GearItemStack;
import com.wynntils.wc.utils.ChatItemUtils;
import com.wynntils.wc.utils.WynnUtils;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.regex.Matcher;
import java.util.stream.Collectors;
import net.minecraft.client.gui.components.EditBox;
import net.minecraft.client.gui.screens.ChatScreen;
import net.minecraft.network.chat.Component;
import net.minecraft.network.chat.MutableComponent;
import net.minecraft.network.chat.Style;
import net.minecraft.network.chat.TextComponent;
import net.minecraftforge.eventbus.api.SubscribeEvent;
import org.lwjgl.glfw.GLFW;

@EventListener
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
        Matcher m = ChatItemUtils.ENCODED_PATTERN.matcher(chatInput.getValue());
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
        List<MutableComponent> components =
                message.getSiblings().stream().map(Component::copy).collect(Collectors.toList());
        components.add(0, message.plainCopy().withStyle(message.getStyle()));

        // chat item tooltips
        if (ChatItemUtils.ENCODED_PATTERN.matcher(message.getString()).find()) {
            MutableComponent temp = new TextComponent("");
            for (Component comp : components) {
                Matcher m = ChatItemUtils.ENCODED_PATTERN.matcher(comp.getString());
                if (!m.find()) {
                    Component newComponent = comp.copy();
                    temp.append(newComponent);
                    continue;
                }

                do {
                    String text = comp.getString();
                    Style style = comp.getStyle();

                    GearItemStack item = ChatItemUtils.decodeItem(m.group());
                    if (item == null) { // couldn't decode, skip
                        comp = comp.copy();
                        continue;
                    }

                    MutableComponent preText = new TextComponent(text.substring(0, m.start()));
                    preText.withStyle(style);
                    temp.append(preText);

                    // create hover-able text component for the item
                    Component itemComponent = ChatItemUtils.createItemComponent(item);
                    temp.append(itemComponent);

                    comp = new TextComponent(text.substring(m.end())).withStyle(style);
                    m = ChatItemUtils.ENCODED_PATTERN.matcher(comp.getString()); // recreate matcher for new substring
                } while (m.find()); // search for multiple items in the same message
                temp.append(comp); // leftover text after item(s)
            }

            e.setMessage(temp);
        }
    }
}
