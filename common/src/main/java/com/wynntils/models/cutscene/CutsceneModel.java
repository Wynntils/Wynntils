/*
 * Copyright © Wynntils 2026.
 * This file is released under LGPLv3. See LICENSE for full license details.
 */
package com.wynntils.models.cutscene;

import com.wynntils.core.WynntilsMod;
import com.wynntils.core.components.Model;
import com.wynntils.core.components.Models;
import com.wynntils.mc.event.SetCameraEntityEvent;
import com.wynntils.models.character.type.VehicleType;
import com.wynntils.models.cutscene.type.CutsceneState;
import com.wynntils.models.worlds.event.CutsceneStartedEvent;
import java.util.List;
import java.util.Optional;
import net.minecraft.world.entity.Display;
import net.minecraft.world.entity.Entity;
import net.neoforged.bus.api.SubscribeEvent;

public class CutsceneModel extends Model {
    private Optional<Entity> cameraEntity = Optional.empty();
    private CutsceneState cutsceneState = CutsceneState.NOT_IN_CUTSCENE;

    public CutsceneModel() {
        super(List.of());
    }

    @SubscribeEvent
    public void setCameraEntity(SetCameraEntityEvent event) {
        cameraEntity = Optional.ofNullable(event.getViewingEntity());
    }

    public void cutsceneStarted(boolean groupCutscene) {
        if (cutsceneState == CutsceneState.NOT_IN_CUTSCENE) {
            cutsceneState = CutsceneState.IN_CUTSCENE;

            CutsceneStartedEvent event = new CutsceneStartedEvent(groupCutscene);
            WynntilsMod.postEvent(event);

            if (event.isCanceled()) {
                cutsceneState = CutsceneState.SKIPPED_CUTSCENE;
            }
        }
    }

    public Optional<Entity> getCameraEntity() {
        return cameraEntity;
    }

    public boolean isCutsceneActive() {
        return Models.Character.getVehicle() == VehicleType.DISPLAY
                || cameraEntity.filter(entity -> entity instanceof Display).isPresent()
                || cutsceneState == CutsceneState.IN_CUTSCENE;
    }

    public void cutsceneEnded() {
        cutsceneState = CutsceneState.NOT_IN_CUTSCENE;
    }
}
