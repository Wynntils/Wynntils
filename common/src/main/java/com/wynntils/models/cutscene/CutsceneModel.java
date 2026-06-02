/*
 * Copyright © Wynntils 2026.
 * This file is released under LGPLv3. See LICENSE for full license details.
 */
package com.wynntils.models.cutscene;

import com.wynntils.core.components.Model;
import com.wynntils.core.components.Models;
import com.wynntils.mc.event.SetCameraEntityEvent;
import com.wynntils.models.character.type.VehicleType;
import java.util.List;
import java.util.Optional;
import net.minecraft.world.entity.Display;
import net.minecraft.world.entity.Entity;
import net.neoforged.bus.api.SubscribeEvent;

public class CutsceneModel extends Model {
    private Optional<Entity> cameraEntity = Optional.empty();

    public CutsceneModel() {
        super(List.of());
    }

    @SubscribeEvent
    public void setCameraEntity(SetCameraEntityEvent event) {
        cameraEntity = Optional.ofNullable(event.getViewingEntity());
    }

    public Optional<Entity> getCameraEntity() {
        return cameraEntity;
    }

    public boolean isCutsceneActive() {
        return Models.Character.getVehicle() == VehicleType.DISPLAY
                || cameraEntity.filter(entity -> entity instanceof Display).isPresent();
    }
}
