/*
 * Copyright © Wynntils 2022.
 * This file is released under AGPLv3. See LICENSE for full license details.
 */
package com.wynntils.features.debug;

import com.wynntils.core.components.Models;
import com.wynntils.core.config.Category;
import com.wynntils.core.config.ConfigCategory;
import com.wynntils.core.features.Feature;
import com.wynntils.core.features.properties.StartDisabled;
import com.wynntils.core.text.CodedString;
import com.wynntils.mc.event.ItemTooltipRenderEvent;
import com.wynntils.models.items.WynnItem;
import com.wynntils.utils.mc.KeyboardUtils;
import com.wynntils.utils.mc.LoreUtils;
import com.wynntils.utils.mc.RenderedStringUtils;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.List;
import java.util.Optional;
import net.minecraft.ChatFormatting;
import net.minecraft.network.chat.Component;
import net.minecraftforge.eventbus.api.EventPriority;
import net.minecraftforge.eventbus.api.SubscribeEvent;
import org.lwjgl.glfw.GLFW;

@StartDisabled
@ConfigCategory(Category.DEBUG)
public class ItemDebugTooltipsFeature extends Feature {
    @SubscribeEvent(priority = EventPriority.LOWEST)
    public void onTooltipPre(ItemTooltipRenderEvent.Pre event) {
        Optional<WynnItem> wynnItemOpt = Models.Item.getWynnItem(event.getItemStack());
        if (wynnItemOpt.isEmpty()) return;
        WynnItem wynnItem = wynnItemOpt.get();

        List<Component> tooltips =
                LoreUtils.appendTooltip(event.getItemStack(), event.getTooltips(), getTooltipAddon(wynnItem));
        event.setTooltips(tooltips);
    }

    private List<Component> getTooltipAddon(WynnItem wynnItem) {
        List<Component> addon = new ArrayList<>();

        List<CodedString> wrappedDescription = Arrays.stream(
                        RenderedStringUtils.wrapTextBySize(CodedString.fromString(wynnItem.toString()), 150))
                .toList();
        if (!KeyboardUtils.isKeyDown(GLFW.GLFW_KEY_RIGHT_SHIFT) && wrappedDescription.size() > 4) {
            wrappedDescription = new ArrayList<>(wrappedDescription.subList(0, 3));
            wrappedDescription.add(CodedString.fromString("..."));
            wrappedDescription.add(CodedString.fromString("Press Right Shift for all"));
        }

        for (CodedString line : wrappedDescription) {
            addon.add(line.asSingleLiteralComponentWithCodedString().withStyle(ChatFormatting.DARK_GREEN));
        }

        return addon;
    }
}
