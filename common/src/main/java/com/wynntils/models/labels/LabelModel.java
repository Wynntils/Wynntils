/*
 * Copyright © Wynntils 2023.
 * This file is released under AGPLv3. See LICENSE for full license details.
 */
package com.wynntils.models.labels;

import com.wynntils.core.WynntilsMod;
import com.wynntils.core.components.Model;
import com.wynntils.mc.event.SetEntityDataEvent;
import com.wynntils.utils.mc.McUtils;
import java.util.List;
import java.util.Optional;
import net.minecraft.network.chat.Component;
import net.minecraft.network.syncher.SynchedEntityData;
import net.minecraft.world.entity.Entity;
import net.minecraftforge.eventbus.api.EventPriority;
import net.minecraftforge.eventbus.api.SubscribeEvent;

public class LabelModel extends Model {
    public LabelModel() {
        super(List.of());
    }

    @SubscribeEvent(priority = EventPriority.HIGHEST)
    public void onEntitySetData(SetEntityDataEvent event) {
        Entity entity = McUtils.mc().level.getEntity(event.getId());

        if (entity == null) return;

        for (SynchedEntityData.DataValue<?> packedItem : event.getPackedItems()) {
            if (Entity.DATA_CUSTOM_NAME.getId() == packedItem.id()) {
                Optional<Component> value = (Optional<Component>) packedItem.value();
                if (value.isEmpty()) continue;

                WynntilsMod.postEvent(new LabelEvent(value.get()));
                return;
            }
        }
    }
}
