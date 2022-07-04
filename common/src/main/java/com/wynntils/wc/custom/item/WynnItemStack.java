/*
 * Copyright © Wynntils 2022.
 * This file is released under AGPLv3. See LICENSE for full license details.
 */
package com.wynntils.wc.custom.item;

import com.wynntils.wc.custom.item.properties.ItemProperty;
import com.wynntils.wc.utils.WynnUtils;
import java.util.ArrayList;
import java.util.List;
import net.minecraft.ChatFormatting;
import net.minecraft.network.chat.Component;
import net.minecraft.world.item.ItemStack;
import net.minecraft.world.item.TooltipFlag.Default;

public class WynnItemStack extends ItemStack {

    protected String itemName;
    protected List<ItemProperty> properties = new ArrayList<>();

    public WynnItemStack(ItemStack stack) {
        super(stack.getItem(), stack.getCount());
        if (stack.getTag() != null) setTag(stack.getTag());

        itemName = WynnUtils.normalizeBadString(
                ChatFormatting.stripFormatting(super.getHoverName().getString()));
    }

    public String getSimpleName() {
        return itemName;
    }

    public List<Component> getOriginalTooltip() {
        return super.getTooltipLines(null, Default.NORMAL);
    }

    public void addProperty(ItemProperty property) {
        if (getProperty(property.getClass()) != null) return; // don't allow duplicate properties of same type
        this.properties.add(property);
    }

    public <T> T getProperty(Class<T> propertyType) {
        for (ItemProperty property : properties) {
            if (propertyType.isAssignableFrom(property.getClass())) return (T) property;
        }
        return null; // no match
    }

    public boolean hasProperty(Class<?> propertyType) {
        // getProperty returns null if no property of the given type is present
        return (getProperty(propertyType) != null);
    }
}
