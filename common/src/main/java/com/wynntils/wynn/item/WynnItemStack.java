/*
 * Copyright © Wynntils 2022.
 * This file is released under AGPLv3. See LICENSE for full license details.
 */
package com.wynntils.wynn.item;

import com.wynntils.handlers.item.AnnotatedItemStack;
import com.wynntils.handlers.item.ItemAnnotation;
import com.wynntils.mc.utils.ComponentUtils;
import com.wynntils.wynn.utils.WynnUtils;
import java.util.List;
import net.minecraft.network.chat.Component;
import net.minecraft.world.item.ItemStack;
import net.minecraft.world.item.TooltipFlag;

public class WynnItemStack extends ItemStack implements ItemAnnotation {
    protected final String itemName;

    public WynnItemStack(ItemStack stack) {
        super(stack.getItem(), stack.getCount());
        if (stack.getTag() != null) setTag(stack.getTag());
        ItemAnnotation annotation = ((AnnotatedItemStack) stack).getAnnotation();
        if (annotation != null) {
            ((AnnotatedItemStack) this).setAnnotation(annotation);
        }

        itemName = WynnUtils.normalizeBadString(
                ComponentUtils.stripFormatting(super.getHoverName().getString()));
    }

    public String getSimpleName() {
        return itemName;
    }

    public List<Component> getOriginalTooltip() {
        return super.getTooltipLines(null, TooltipFlag.NORMAL);
    }

    /**
     * Called when all properties are setup on this stack
     */
    public void init() {}
}
