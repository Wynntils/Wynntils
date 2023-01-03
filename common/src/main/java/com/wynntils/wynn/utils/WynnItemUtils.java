/*
 * Copyright © Wynntils 2022.
 * This file is released under AGPLv3. See LICENSE for full license details.
 */
package com.wynntils.wynn.utils;

import com.wynntils.core.components.Managers;
import com.wynntils.features.user.tooltips.ItemStatInfoFeature;
import com.wynntils.mc.utils.ComponentUtils;
import com.wynntils.utils.KeyboardUtils;
import com.wynntils.wynn.objects.ItemIdentificationContainer;
import com.wynntils.wynn.objects.profiles.item.IdentificationModifier;
import com.wynntils.wynn.objects.profiles.item.IdentificationProfile;
import com.wynntils.wynn.objects.profiles.item.ItemProfile;
import java.util.ArrayList;
import java.util.List;
import java.util.Map;
import net.minecraft.ChatFormatting;
import net.minecraft.network.chat.Component;
import net.minecraft.network.chat.MutableComponent;
import net.minecraft.network.chat.Style;
import net.minecraft.world.item.ItemStack;
import org.lwjgl.glfw.GLFW;

public final class WynnItemUtils {
    /**
     * Create a list of ItemIdentificationContainer corresponding to the given ItemProfile, formatted for item guide items
     *
     * @param item the profile of the item
     * @return a list of appropriately formatted ItemIdentificationContainer
     */
    public static List<ItemIdentificationContainer> identificationsFromProfile(ItemProfile item) {
        List<ItemIdentificationContainer> ids = new ArrayList<>();

        for (Map.Entry<String, IdentificationProfile> entry : item.getStatuses().entrySet()) {
            IdentificationProfile idProfile = entry.getValue();
            IdentificationModifier type = idProfile.getType();
            String idName = entry.getKey();
            MutableComponent line;

            boolean inverted = idProfile.isInverted();
            if (idProfile.hasConstantValue()) {
                int value = idProfile.getBaseValue();
                line = Component.literal((value > 0 ? "+" : "") + value + type.getInGame(idName));
                line.setStyle(
                        Style.EMPTY.withColor(inverted ^ (value > 0) ? ChatFormatting.GREEN : ChatFormatting.RED));
            } else {
                int min = idProfile.getMin();
                int max = idProfile.getMax();
                ChatFormatting mainColor = inverted ^ (min > 0) ? ChatFormatting.GREEN : ChatFormatting.RED;
                ChatFormatting textColor = inverted ^ (min > 0) ? ChatFormatting.DARK_GREEN : ChatFormatting.DARK_RED;
                line = Component.literal((min > 0 ? "+" : "") + min).withStyle(mainColor);
                line.append(Component.literal(" to ").withStyle(textColor));
                line.append(Component.literal((max > 0 ? "+" : "") + max + type.getInGame(idName))
                        .withStyle(mainColor));
            }

            line.append(Component.literal(" " + IdentificationProfile.getAsLongName(idName))
                    .withStyle(ChatFormatting.GRAY));

            ItemIdentificationContainer id =
                    new ItemIdentificationContainer(item, idProfile, type, idName, 0, 0, -1, line, line, line, line);
            ids.add(id);
        }

        return ids;
    }

    public static void removeLoreTooltipLines(List<Component> tooltip) {
        List<Component> toRemove = new ArrayList<>();
        boolean lore = false;
        for (Component c : tooltip) {
            // only remove text after the item type indicator
            if (!lore && WynnItemMatchers.rarityLineMatcher(c).find()) {
                lore = true;
                continue;
            }

            if (lore) toRemove.add(c);
        }
        tooltip.removeAll(toRemove);
    }

    public static String getTranslatedName(ItemStack itemStack) {
        String unformattedItemName = ComponentUtils.getUnformatted(itemStack.getHoverName());
        return Managers.ItemProfiles.getTranslatedReference(unformattedItemName).replace("֎", "");
    }

    public static GearTooltipBuilder.IdentificationPresentationStyle getCurrentIdentificationStyle() {
        GearTooltipBuilder.IdentificationDecorations decorations;
        if (KeyboardUtils.isKeyDown(GLFW.GLFW_KEY_LEFT_SHIFT)) {
            decorations = GearTooltipBuilder.IdentificationDecorations.RANGE;
        } else if (KeyboardUtils.isKeyDown(GLFW.GLFW_KEY_LEFT_CONTROL)) {
            decorations = GearTooltipBuilder.IdentificationDecorations.REROLL_CHANCE;
        } else {
            decorations = GearTooltipBuilder.IdentificationDecorations.PERCENT;
        }

        return new GearTooltipBuilder.IdentificationPresentationStyle(
                decorations,
                ItemStatInfoFeature.INSTANCE.reorderIdentifications,
                ItemStatInfoFeature.INSTANCE.groupIdentifications);
    }
}
