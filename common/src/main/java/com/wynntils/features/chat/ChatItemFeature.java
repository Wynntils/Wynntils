/*
 * Copyright © Wynntils 2022-2023.
 * This file is released under AGPLv3. See LICENSE for full license details.
 */
package com.wynntils.features.chat;

import com.wynntils.core.components.Models;
import com.wynntils.core.consumers.features.Feature;
import com.wynntils.core.persisted.config.Category;
import com.wynntils.core.persisted.config.ConfigCategory;
import com.wynntils.core.text.PartStyle;
import com.wynntils.core.text.StyledText;
import com.wynntils.core.text.StyledTextPart;
import com.wynntils.handlers.chat.event.ChatMessageReceivedEvent;
import com.wynntils.mc.event.KeyInputEvent;
import com.wynntils.mc.mixin.accessors.ChatScreenAccessor;
import com.wynntils.mc.mixin.accessors.ItemStackInfoAccessor;
import com.wynntils.models.items.FakeItemStack;
import com.wynntils.models.items.items.game.GearItem;
import com.wynntils.utils.mc.McUtils;
import com.wynntils.utils.type.IterationDecision;
import java.util.HashMap;
import java.util.Map;
import java.util.regex.Matcher;
import net.minecraft.ChatFormatting;
import net.minecraft.client.gui.components.EditBox;
import net.minecraft.client.gui.screens.ChatScreen;
import net.minecraft.network.chat.HoverEvent;
import net.minecraft.network.chat.Style;
import net.minecraft.world.item.ItemStack;
import net.minecraftforge.eventbus.api.EventPriority;
import net.minecraftforge.eventbus.api.SubscribeEvent;
import org.lwjgl.glfw.GLFW;

@ConfigCategory(Category.CHAT)
public class ChatItemFeature extends Feature {
    private final Map<String, String> chatItems = new HashMap<>();

    @SubscribeEvent
    public void onKeyTyped(KeyInputEvent e) {
        if (!Models.WorldState.onWorld()) return;
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
        Matcher m = Models.Gear.gearChatEncodingMatcher(chatInput.getValue());
        while (m.find()) {
            String encodedItem = m.group();
            StringBuilder name = new StringBuilder(m.group("Name"));
            while (chatItems.containsKey(name.toString())) { // avoid overwriting entries
                name.append("_");
            }

            chatInput.setValue(chatInput.getValue().replace(encodedItem, "<" + name + ">"));
            chatItems.put(name.toString(), encodedItem);
        }
    }

    @SubscribeEvent(priority = EventPriority.HIGHEST)
    public void onChatReceived(ChatMessageReceivedEvent e) {
        if (!Models.WorldState.onWorld()) return;

        StyledText styledText = e.getStyledText();

        StyledText modified = styledText.iterate((part, changes) -> {
            StyledTextPart partToReplace = part;
            Matcher matcher =
                    Models.Gear.gearChatEncodingMatcher(partToReplace.getString(null, PartStyle.StyleType.NONE));

            while (matcher.find()) {
                GearItem decodedItem = Models.Gear.fromEncodedString(matcher.group());
                if (decodedItem == null) continue;

                String unformattedString = partToReplace.getString(null, PartStyle.StyleType.NONE);

                String firstPart = unformattedString.substring(0, matcher.start());
                String lastPart = unformattedString.substring(matcher.end());

                PartStyle partStyle = partToReplace.getPartStyle();

                StyledTextPart first = new StyledTextPart(firstPart, partStyle.getStyle(), null, Style.EMPTY);
                StyledTextPart item = createItemPart(decodedItem);
                StyledTextPart last = new StyledTextPart(lastPart, partStyle.getStyle(), null, Style.EMPTY);

                changes.remove(partToReplace);
                changes.add(first);
                changes.add(item);
                changes.add(last);

                partToReplace = last;
                matcher = Models.Gear.gearChatEncodingMatcher(lastPart);
            }

            return IterationDecision.CONTINUE;
        });

        if (modified.equals(styledText)) return;

        e.setMessage(modified.getComponent());
    }

    private StyledTextPart createItemPart(GearItem gearItem) {
        Style style = Style.EMPTY
                .applyFormat(ChatFormatting.UNDERLINE)
                .withColor(gearItem.getGearInfo().tier().getChatFormatting());

        ItemStack itemStack = new FakeItemStack(gearItem, "From chat");
        HoverEvent.ItemStackInfo itemHoverEvent = new HoverEvent.ItemStackInfo(itemStack);
        ((ItemStackInfoAccessor) itemHoverEvent).setItemStack(itemStack);
        style = style.withHoverEvent(new HoverEvent(HoverEvent.Action.SHOW_ITEM, itemHoverEvent));

        return new StyledTextPart(gearItem.getGearInfo().name(), style, null, Style.EMPTY);
    }
}
