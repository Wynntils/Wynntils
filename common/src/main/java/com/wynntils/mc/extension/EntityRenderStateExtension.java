/*
 * Copyright © Wynntils 2024-2025.
 * This file is released under LGPLv3. See LICENSE for full license details.
 */
package com.wynntils.mc.extension;

import net.minecraft.world.entity.Entity;

public interface EntityRenderStateExtension {
    void setEntity(Entity entity);

    Entity getEntity();
}
