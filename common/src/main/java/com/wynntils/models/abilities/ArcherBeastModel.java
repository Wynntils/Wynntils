/*
 * Copyright © Wynntils 2026.
 * This file is released under LGPLv3. See LICENSE for full license details.
 */
package com.wynntils.models.abilities;

import com.wynntils.core.components.Handlers;
import com.wynntils.core.components.Model;
import com.wynntils.core.components.Models;
import com.wynntils.handlers.labels.event.LabelIdentifiedEvent;
import com.wynntils.handlers.labels.type.LabelInfo;
import com.wynntils.mc.event.RemoveEntitiesEvent;
import com.wynntils.models.abilities.label.ArcherCrowInfo;
import com.wynntils.models.abilities.label.ArcherCrowParser;
import com.wynntils.models.abilities.label.ArcherHoundInfo;
import com.wynntils.models.abilities.label.ArcherHoundParser;
import com.wynntils.models.abilities.label.ArcherSnakeInfo;
import com.wynntils.models.abilities.label.ArcherSnakeParser;
import com.wynntils.models.character.event.CharacterUpdateEvent;
import com.wynntils.models.character.type.ClassType;
import com.wynntils.models.worlds.event.WorldStateEvent;
import com.wynntils.utils.mc.McUtils;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import net.neoforged.bus.api.SubscribeEvent;

public class ArcherBeastModel extends Model {
    private static final int NO_HOUND_ID = -1;

    private final Map<Integer, ArcherCrowInfo> activeCrowMap = new HashMap<>();
    private final Map<Integer, ArcherHoundInfo> activeHoundsMap = new HashMap<>();
    private final Map<Integer, ArcherSnakeInfo> activeSnakeMap = new HashMap<>();

    private int lastHoundId = NO_HOUND_ID;

    public ArcherBeastModel() {
        super(List.of());

        Handlers.Label.registerParser(new ArcherCrowParser());
        Handlers.Label.registerParser(new ArcherHoundParser());
        Handlers.Label.registerParser(new ArcherSnakeParser());
    }

    @SubscribeEvent
    public void onWorldChange(WorldStateEvent event) {
        resetModel();
    }

    @SubscribeEvent
    public void onClassChange(CharacterUpdateEvent event) {
        resetModel();
    }

    private void resetModel() {
        activeCrowMap.clear();
        activeHoundsMap.clear();
        lastHoundId = NO_HOUND_ID;
        activeSnakeMap.clear();
    }

    @SubscribeEvent
    public void onBeastIdentified(LabelIdentifiedEvent event) {
        LabelInfo labelInfo = event.getLabelInfo();

        if (labelInfo instanceof ArcherCrowInfo crowLabelInfo
                && crowLabelInfo.getPlayerName().equals(McUtils.playerName())) {
            activeCrowMap.put(labelInfo.getEntity().getId(), crowLabelInfo);
        } else if (labelInfo instanceof ArcherHoundInfo houndLabelInfo
                && houndLabelInfo.getPlayerName().equals(McUtils.playerName())) {
            activeHoundsMap.put(labelInfo.getEntity().getId(), houndLabelInfo);
            lastHoundId = houndLabelInfo.getEntity().getId();
        } else if (labelInfo instanceof ArcherSnakeInfo snakeLabelInfo
                && snakeLabelInfo.getPlayerName().equals(McUtils.playerName())) {
            activeSnakeMap.put(labelInfo.getEntity().getId(), snakeLabelInfo);
        }
    }

    @SubscribeEvent
    public void onBeastRemoved(RemoveEntitiesEvent event) {
        if (!Models.WorldState.onWorld()) return;
        if (Models.Character.getClassType() != ClassType.ARCHER) return;

        List<Integer> entityIds = event.getEntityIds();
        entityIds.forEach(activeCrowMap::remove);
        entityIds.forEach(activeHoundsMap::remove);
        entityIds.forEach(activeSnakeMap::remove);
    }

    public int getHoundsTimeLeft() {
        if (lastHoundId == NO_HOUND_ID) return 0;
        if (!activeHoundsMap.containsKey(lastHoundId)) return 0;
        return activeHoundsMap.get(lastHoundId).getSecondsLeft();
    }

    public int getActiveCrowCount() {
        return activeCrowMap.size();
    }

    public int getActiveSnakeCount() {
        return activeSnakeMap.size();
    }
}
