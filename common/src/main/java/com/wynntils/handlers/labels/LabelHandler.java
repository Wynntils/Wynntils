/*
 * Copyright © Wynntils 2023-2024.
 * This file is released under LGPLv3. See LICENSE for full license details.
 */
package com.wynntils.handlers.labels;

import com.wynntils.core.WynntilsMod;
import com.wynntils.core.components.Handler;
import com.wynntils.core.text.StyledText;
import com.wynntils.handlers.labels.event.EntityLabelChangedEvent;
import com.wynntils.handlers.labels.event.EntityLabelVisibilityEvent;
import com.wynntils.handlers.labels.event.LabelIdentifiedEvent;
import com.wynntils.handlers.labels.event.LabelsRemovedEvent;
import com.wynntils.handlers.labels.type.LabelInfo;
import com.wynntils.handlers.labels.type.LabelParser;
import com.wynntils.mc.event.RemoveEntitiesEvent;
import com.wynntils.mc.event.SetEntityDataEvent;
import com.wynntils.models.worlds.event.WorldStateEvent;
import com.wynntils.utils.mc.McUtils;
import com.wynntils.utils.mc.type.Location;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.Optional;
import net.minecraft.network.chat.Component;
import net.minecraft.network.syncher.EntityDataSerializer;
import net.minecraft.network.syncher.SynchedEntityData;
import net.minecraft.world.entity.Entity;
import net.minecraftforge.eventbus.api.EventPriority;
import net.minecraftforge.eventbus.api.SubscribeEvent;

public class LabelHandler extends Handler {
    private final List<LabelParser> parsers = new ArrayList<>();

    private final Map<Integer, LabelInfo> liveLabels = new HashMap<>();

    @SubscribeEvent(priority = EventPriority.HIGHEST)
    public void onEntitySetData(SetEntityDataEvent event) {
        Entity entity = McUtils.mc().level.getEntity(event.getId());
        if (entity == null) return;

        SynchedEntityData.DataValue<?> oldNameData = null;
        SynchedEntityData.DataValue<Optional<Component>> newCustomNameData = null;

        for (SynchedEntityData.DataValue<?> packedItem : event.getPackedItems()) {
            if (packedItem.id() == Entity.DATA_CUSTOM_NAME_VISIBLE.getId()) {
                WynntilsMod.postEvent(new EntityLabelVisibilityEvent(entity, (Boolean) packedItem.value()));
                continue;
            }

            if (packedItem.id() == Entity.DATA_CUSTOM_NAME.getId()) {
                Optional<Component> value = (Optional<Component>) packedItem.value();
                if (value.isEmpty()) continue;

                Component oldNameComponent = entity.getCustomName();
                StyledText oldName =
                        oldNameComponent != null ? StyledText.fromComponent(oldNameComponent) : StyledText.EMPTY;
                StyledText newName = StyledText.fromComponent(value.get());

                // Sometimes there is no actual change; ignore it then
                if (newName.equals(oldName)) continue;

                LabelInfo labelInfo = tryIdentifyLabel(newName, entity);
                if (labelInfo != null) {
                    liveLabels.put(entity.getId(), labelInfo);
                }

                EntityLabelChangedEvent labelChangedEvent =
                        new EntityLabelChangedEvent(entity, newName, oldName, labelInfo);
                WynntilsMod.postEvent(labelChangedEvent);

                // If the event was cancelled, remove the name change data
                if (labelChangedEvent.isCanceled()) {
                    oldNameData = packedItem;
                    continue;
                }

                // If the event changed the name, update the data
                if (!labelChangedEvent.getName().equals(newName)) {
                    oldNameData = packedItem;
                    newCustomNameData = new SynchedEntityData.DataValue<>(
                            Entity.DATA_CUSTOM_NAME.getId(),
                            (EntityDataSerializer<Optional<Component>>) packedItem.serializer(),
                            Optional.of(labelChangedEvent.getName().getComponent()));
                }
            }
        }

        // If the name was removed, remove the old data
        if (oldNameData != null) {
            event.removePackedItem(oldNameData);
        }

        // If the name was changed, add the new data
        if (newCustomNameData != null) {
            event.addPackedItem(newCustomNameData);
        }
    }

    @SubscribeEvent(priority = EventPriority.HIGHEST)
    public void onEntitiesRemoved(RemoveEntitiesEvent event) {
        List<LabelInfo> removedLabels = liveLabels.values().stream()
                .filter(label -> event.getEntityIds().contains(label.getEntity().getId()))
                .toList();

        removedLabels.forEach(label -> liveLabels.remove(label.getEntity().getId()));
        WynntilsMod.postEvent(new LabelsRemovedEvent(removedLabels));
    }

    @SubscribeEvent
    public void onWorldStateChange(WorldStateEvent event) {
        List<LabelInfo> oldLabels = new ArrayList<>(liveLabels.values());
        liveLabels.clear();
        WynntilsMod.postEvent(new LabelsRemovedEvent(oldLabels));
    }

    public void registerParser(LabelParser labelParser) {
        parsers.add(labelParser);
    }

    private LabelInfo tryIdentifyLabel(StyledText name, Entity entity) {
        for (LabelParser parser : parsers) {
            LabelInfo info = parser.getInfo(name, Location.containing(entity.position()), entity);

            if (info == null) continue;

            WynntilsMod.postEvent(new LabelIdentifiedEvent(info));
            return info;
        }

        return null;
    }
}
