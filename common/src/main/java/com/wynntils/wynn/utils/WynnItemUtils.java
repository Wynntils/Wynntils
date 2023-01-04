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
