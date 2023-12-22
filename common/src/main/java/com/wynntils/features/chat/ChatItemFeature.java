/*
 * Copyright © Wynntils 2022-2023.
 * This file is released under LGPLv3. See LICENSE for full license details.
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
import com.wynntils.models.items.WynnItem;
import com.wynntils.models.items.items.game.GearItem;
import com.wynntils.models.items.properties.GearTierItemProperty;
import com.wynntils.models.items.properties.IdentifiableItemProperty;
import com.wynntils.utils.EncodedByteBuffer;
import com.wynntils.utils.mc.McUtils;
import com.wynntils.utils.type.ErrorOr;
import com.wynntils.utils.type.IterationDecision;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.regex.Matcher;
import net.minecraft.ChatFormatting;
import net.minecraft.client.gui.components.EditBox;
import net.minecraft.client.gui.screens.ChatScreen;
import net.minecraft.network.chat.Component;
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

        // check for old chat item encoding
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

        // check for new chat item encoding
        m = Models.ItemEncoding.getEncodedDataPattern().matcher(chatInput.getValue());
        while (m.find()) {
            EncodedByteBuffer encodedByteBuffer = EncodedByteBuffer.fromUtf16String(m.group());
            ErrorOr<WynnItem> errorOrDecodedItem = Models.ItemEncoding.decodeItem(encodedByteBuffer);

            String name;
            if (errorOrDecodedItem.hasError()) {
                name = "encoding_error";
            } else {
                WynnItem decodedItem = errorOrDecodedItem.getValue();

                name = decodedItem.getClass().getSimpleName();

                if (decodedItem instanceof IdentifiableItemProperty identifiableItemProperty) {
                    name = identifiableItemProperty.getName();
                }
            }

            while (chatItems.containsKey(name)) { // avoid overwriting entries
                name += "_";
            }

            chatInput.setValue(chatInput.getValue().replace(m.group(), "<" + name + ">"));
            chatItems.put(name, m.group());
        }
    }

    @SubscribeEvent(priority = EventPriority.HIGHEST)
    public void onChatReceived(ChatMessageReceivedEvent e) {
        if (!Models.WorldState.onWorld()) return;

        StyledText styledText = e.getStyledText();

        // Decode old chat item encoding
        StyledText modified = styledText.iterate((part, changes) -> {
            StyledTextPart partToReplace = part;

            decoodeDeprecatedChatEncoding(changes, partToReplace);

            return IterationDecision.CONTINUE;
        });

        // Decode new chat item encoding
        modified = modified.iterate((part, changes) -> {
            StyledTextPart partToReplace = part;

            decoodeChatEncoding(changes, partToReplace);

            return IterationDecision.CONTINUE;
        });

        if (modified.equals(styledText)) return;

        e.setMessage(modified.getComponent());
    }

    private void decoodeChatEncoding(List<StyledTextPart> changes, StyledTextPart partToReplace) {
        Matcher matcher = Models.ItemEncoding.getEncodedDataPattern()
                .matcher(partToReplace.getString(null, PartStyle.StyleType.NONE));

        while (matcher.find()) {
            EncodedByteBuffer encodedByteBuffer = EncodedByteBuffer.fromUtf16String(matcher.group());
            ErrorOr<WynnItem> errorOrDecodedItem = Models.ItemEncoding.decodeItem(encodedByteBuffer);

            String unformattedString = partToReplace.getString(null, PartStyle.StyleType.NONE);

            String firstPart = unformattedString.substring(0, matcher.start());
            String lastPart = unformattedString.substring(matcher.end());

            PartStyle partStyle = partToReplace.getPartStyle();

            StyledTextPart first = new StyledTextPart(firstPart, partStyle.getStyle(), null, Style.EMPTY);
            StyledTextPart replacedPart = errorOrDecodedItem.hasError()
                    ? createErrorPart(matcher.group(), errorOrDecodedItem.getError())
                    : createItemPart(errorOrDecodedItem.getValue());
            StyledTextPart last = new StyledTextPart(lastPart, partStyle.getStyle(), null, Style.EMPTY);

            changes.remove(partToReplace);
            changes.add(first);
            changes.add(replacedPart);
            changes.add(last);

            partToReplace = last;
            matcher = Models.Gear.gearChatEncodingMatcher(lastPart);
        }
    }

    private void decoodeDeprecatedChatEncoding(List<StyledTextPart> changes, StyledTextPart partToReplace) {
        Matcher matcher = Models.Gear.gearChatEncodingMatcher(partToReplace.getString(null, PartStyle.StyleType.NONE));

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
    }

    private StyledTextPart createErrorPart(String originalString, String error) {
        // Display the original string with a red underline and a tooltip with the error message
        Style style = Style.EMPTY.applyFormat(ChatFormatting.UNDERLINE).withColor(ChatFormatting.RED);

        HoverEvent hoverEvent = new HoverEvent(
                HoverEvent.Action.SHOW_TEXT, Component.literal(error).withStyle(ChatFormatting.RED));
        style = style.withHoverEvent(hoverEvent);

        return new StyledTextPart(originalString, style, null, Style.EMPTY);
    }

    private StyledTextPart createItemPart(WynnItem wynnItem) {
        String name = wynnItem.getClass().getSimpleName();

        if (wynnItem instanceof IdentifiableItemProperty identifiableItemProperty) {
            name = identifiableItemProperty.getName();
        }

        Style style = Style.EMPTY.applyFormat(ChatFormatting.UNDERLINE).withColor(ChatFormatting.GOLD);

        if (wynnItem instanceof GearTierItemProperty tierItemProperty) {
            style = style.withColor(tierItemProperty.getGearTier().getChatFormatting());
        }

        ItemStack itemStack = new FakeItemStack(wynnItem, "From chat");
        HoverEvent.ItemStackInfo itemHoverEvent = new HoverEvent.ItemStackInfo(itemStack);
        ((ItemStackInfoAccessor) itemHoverEvent).setItemStack(itemStack);
        style = style.withHoverEvent(new HoverEvent(HoverEvent.Action.SHOW_ITEM, itemHoverEvent));

        return new StyledTextPart(name, style, null, Style.EMPTY);
    }
}
