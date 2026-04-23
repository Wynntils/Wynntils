/*
 * Copyright © Wynntils 2026.
 * This file is released under LGPLv3. See LICENSE for full license details.
 */
package com.wynntils.models.abilities;

import com.wynntils.core.components.Handlers;
import com.wynntils.core.components.Model;
import com.wynntils.core.components.Models;
import com.wynntils.handlers.labels.event.LabelIdentifiedEvent;
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
        activeHoundsMap.clear();
        activeCrowMap.clear();
        activeSnakeMap.clear();
        lastHoundId = NO_HOUND_ID;
    }

    @SubscribeEvent
    public void onCrowIdentified(LabelIdentifiedEvent event) {
        if (!(event.getLabelInfo() instanceof ArcherCrowInfo labelInfo)) return;
        if (!labelInfo.getPlayerName().equals(McUtils.playerName())) return;
        activeCrowMap.put(labelInfo.getEntity().getId(), labelInfo);
    }

    @SubscribeEvent
    public void onCrowRemoved(RemoveEntitiesEvent event) {
        if (!Models.WorldState.onWorld()) return;
        if (Models.Character.getClassType() != ClassType.ARCHER) return;
        event.getEntityIds().forEach(activeCrowMap::remove);
    }

    @SubscribeEvent
    public void onHoundIdentified(LabelIdentifiedEvent event) {
        if (!(event.getLabelInfo() instanceof ArcherHoundInfo labelInfo)) return;
        if (!labelInfo.getPlayerName().equals(McUtils.playerName())) return;
        activeHoundsMap.put(labelInfo.getEntity().getId(), labelInfo);
        lastHoundId = labelInfo.getEntity().getId();
    }

    @SubscribeEvent
    public void onHoundRemoved(RemoveEntitiesEvent event) {
        if (!Models.WorldState.onWorld()) return;
        if (Models.Character.getClassType() != ClassType.ARCHER) return;
        event.getEntityIds().forEach(activeHoundsMap::remove);
    }

    @SubscribeEvent
    public void onSnakeIdentified(LabelIdentifiedEvent event) {
        if (!(event.getLabelInfo() instanceof ArcherSnakeInfo labelInfo)) return;
        if (!labelInfo.getPlayerName().equals(McUtils.playerName())) return;
        activeSnakeMap.put(labelInfo.getEntity().getId(), labelInfo);
    }

    @SubscribeEvent
    public void onSnakeRemoved(RemoveEntitiesEvent event) {
        if (!Models.WorldState.onWorld()) return;
        if (Models.Character.getClassType() != ClassType.ARCHER) return;
        event.getEntityIds().forEach(activeSnakeMap::remove);
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
