/*
 * Copyright © Wynntils 2024-2025.
 * This file is released under LGPLv3. See LICENSE for full license details.
 */
package com.wynntils.mc.mixin;

import com.wynntils.mc.extension.EntityRenderStateExtension;
import net.minecraft.client.renderer.entity.state.EntityRenderState;
import net.minecraft.world.entity.Entity;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.Unique;

@Mixin(EntityRenderState.class)
public class EntityRenderStateMixin implements EntityRenderStateExtension {
    @Unique
    private Entity entity;

    @Unique
    @Override
    public void setEntity(Entity entity) {
        this.entity = entity;
    }

    @Unique
    @Override
    public Entity getEntity() {
        return this.entity;
    }
}
