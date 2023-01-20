/*
 * Copyright © Wynntils 2022.
 * This file is released under AGPLv3. See LICENSE for full license details.
 */
package com.wynntils.wynn.utils;

import com.wynntils.core.components.Managers;
import com.wynntils.features.user.tooltips.ItemStatInfoFeature;
import com.wynntils.mc.utils.ComponentUtils;
import com.wynntils.models.gear.GearIdentificationContainer;
import com.wynntils.models.gear.profile.GearProfile;
import com.wynntils.models.gear.profile.IdentificationProfile;
import com.wynntils.models.gear.type.IdentificationModifier;
import com.wynntils.utils.KeyboardUtils;
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
     * Create a list of ItemIdentificationContainer corresponding to the given GearProfile, formatted for item guide items
     *
     * @param item the profile of the item
     * @return a list of appropriately formatted ItemIdentificationContainer
     */
    public static List<GearIdentificationContainer> identificationsFromProfile(GearProfile item) {
        List<GearIdentificationContainer> ids = new ArrayList<>();

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

            GearIdentificationContainer id =
                    new GearIdentificationContainer(item, idProfile, type, idName, 0, 0, -1, line, line, line, line);
            ids.add(id);
        }

        return ids;
    }

    public static void removeLoreTooltipLines(List<Component> tooltip) {
        int loreStart = -1;
        for (int i = 0; i < tooltip.size(); i++) {
            // only remove text after the item type indicator
            if (WynnItemMatchers.rarityLineMatcher(tooltip.get(i)).find()) {
                loreStart = i + 1;
                break;
            }
        }

        // type indicator was found
        if (loreStart != -1) {
            tooltip.subList(loreStart, tooltip.size()).clear();
        }
    }

    public static String getTranslatedName(ItemStack itemStack) {
        String unformattedItemName = ComponentUtils.getUnformatted(itemStack.getHoverName());
        return Managers.GearProfiles.getTranslatedReference(unformattedItemName).replace("֎", "");
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
