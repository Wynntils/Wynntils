/*
 * Copyright © Wynntils 2022.
 * This file is released under AGPLv3. See LICENSE for full license details.
 */
package com.wynntils.features.debug;

import com.wynntils.core.features.DebugFeature;
import com.wynntils.mc.event.ItemTooltipRenderEvent;
import com.wynntils.utils.KeyboardUtils;
import com.wynntils.utils.StringUtils;
import com.wynntils.wynn.handleditems.ItemModel;
import com.wynntils.wynn.handleditems.WynnItem;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.List;
import java.util.Optional;
import net.minecraft.ChatFormatting;
import net.minecraft.network.chat.Component;
import net.minecraftforge.eventbus.api.EventPriority;
import net.minecraftforge.eventbus.api.SubscribeEvent;
import org.lwjgl.glfw.GLFW;

public class ItemDebugTooltipsFeature extends DebugFeature {
    @SubscribeEvent(priority = EventPriority.LOWEST)
    public void onTooltipPre(ItemTooltipRenderEvent.Pre event) {
        Optional<WynnItem> wynnItemOpt = ItemModel.getWynnItem(event.getItemStack());
        if (wynnItemOpt.isEmpty()) return;
        WynnItem wynnItem = wynnItemOpt.get();

        List<Component> tooltips = new ArrayList<>(event.getTooltips());
        tooltips.addAll(getTooltipAddon(wynnItem));
        event.setTooltips(tooltips);
    }

    private List<Component> getTooltipAddon(WynnItem wynnItem) {
        List<Component> addon = new ArrayList<>();

        addon.add(Component.literal("Wynn Item Type:").withStyle(ChatFormatting.GREEN));

        List<String> wrappedDescription = Arrays.stream(StringUtils.wrapTextBySize(wynnItem.toString(), 150))
                .toList();
        if (!KeyboardUtils.isKeyDown(GLFW.GLFW_KEY_RIGHT_SHIFT) && wrappedDescription.size() > 4) {
            wrappedDescription = new ArrayList<>(wrappedDescription.subList(0, 3));
            wrappedDescription.add("...");
            wrappedDescription.add("Press Right Shift for all");
        }

        for (String line : wrappedDescription) {
            addon.add(Component.literal(line).withStyle(ChatFormatting.DARK_GREEN));
        }

        return addon;
    }
}
