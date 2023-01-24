/*
 * Copyright © Wynntils 2023.
 * This file is released under AGPLv3. See LICENSE for full license details.
 */
package com.wynntils.mc.mixin;

import com.wynntils.models.entities.WynntilsCustomGlowEntityProperty;
import com.wynntils.utils.colors.CustomColor;
import net.minecraft.world.entity.Entity;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.Unique;

@Mixin(Entity.class)
public abstract class EntityMixin implements WynntilsCustomGlowEntityProperty {
    @Unique
    private CustomColor wynntilsGlowColor = CustomColor.NONE;

    @Override
    public CustomColor getGlowColor() {
        return wynntilsGlowColor;
    }

    @Override
    public void setGlowColor(CustomColor color) {
        wynntilsGlowColor = color;
    }
}
