/*
 * Copyright © Wynntils 2022.
 * This file is released under AGPLv3. See LICENSE for full license details.
 */
package com.wynntils.wynn.model.item;

import com.wynntils.core.components.Managers;
import com.wynntils.core.components.Model;
import com.wynntils.wynn.item.WynnItemStack;
import com.wynntils.wynn.model.item.ItemStackTransformManager.ItemPropertyWriter;
import java.util.function.Consumer;
import java.util.function.Predicate;
import net.minecraft.world.item.ItemStack;

public abstract class PropertyModel extends Model {
    private final ItemPropertyWriter writer;

    protected PropertyModel(Predicate<ItemStack> pred, Consumer<WynnItemStack> cons) {
        this.writer = new ItemPropertyWriter(pred, cons);
    }

    @Override
    public void init() {
        Managers.ItemStackTransform.registerProperty(writer);
    }

    @Override
    public void disable() {
        Managers.ItemStackTransform.unregisterProperty(writer);
    }
}
