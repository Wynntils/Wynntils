/*
 * Copyright © Wynntils 2023-2026.
 * This file is released under LGPLv3. See LICENSE for full license details.
 */
package com.wynntils.models.bonustotems;

import com.wynntils.core.components.Handlers;
import com.wynntils.core.components.Model;
import com.wynntils.core.components.Models;
import com.wynntils.handlers.labels.event.LabelIdentifiedEvent;
import com.wynntils.mc.event.RemoveEntitiesEvent;
import com.wynntils.models.bonustotems.label.BonusTotemLabelInfo;
import com.wynntils.models.bonustotems.label.BonusTotemLabelParser;
import com.wynntils.models.bonustotems.type.BonusTotemType;
import com.wynntils.models.worlds.event.WorldStateEvent;
import com.wynntils.utils.mc.PosUtils;
import java.util.Comparator;
import java.util.LinkedHashMap;
import java.util.List;
import java.util.Map;
import java.util.TreeMap;
import net.neoforged.bus.api.SubscribeEvent;

public final class BonusTotemModel extends Model {
    private final Map<BonusTotemType, Map<Integer, BonusTotem>> bonusTotems = new TreeMap<>();

    public BonusTotemModel() {
        super(List.of());

        Handlers.Label.registerParser(new BonusTotemLabelParser());
    }

    @SubscribeEvent
    public void onLabelIdentified(LabelIdentifiedEvent event) {
        if (!Models.WorldState.onWorld()) return;
        if (!(event.getLabelInfo() instanceof BonusTotemLabelInfo bonusTotemLabelInfo)) return;

        Map<Integer, BonusTotem> bonusTotemMap =
                bonusTotems.getOrDefault(bonusTotemLabelInfo.getBonusTotemType(), new LinkedHashMap<>());

        // If the totem is already in the list, don't add it again
        bonusTotemMap.putIfAbsent(
                event.getLabelInfo().getEntity().getId(),
                new BonusTotem(
                        bonusTotemLabelInfo.getBonusTotemType(),
                        PosUtils.newPosition(event.getLabelInfo().getEntity()),
                        bonusTotemLabelInfo.getUser()));
        bonusTotemMap
                .get(event.getLabelInfo().getEntity().getId())
                .setSecondsLeft(bonusTotemLabelInfo.getSecondsLeft());

        bonusTotems.put(bonusTotemLabelInfo.getBonusTotemType(), bonusTotemMap);
    }

    @SubscribeEvent
    public void onTotemDestroy(RemoveEntitiesEvent e) {
        if (!Models.WorldState.onWorld()) return;

        e.getEntityIds().forEach(entity -> {
            bonusTotems.values().forEach(map -> map.remove(entity));
        });
    }

    @SubscribeEvent
    public void onWorldChange(WorldStateEvent e) {
        bonusTotems.clear();
    }

    public List<BonusTotem> getBonusTotemsByType(BonusTotemType type) {
        return bonusTotems.getOrDefault(type, new LinkedHashMap<>()).values().stream()
                .sorted(Comparator.comparing(BonusTotem::getOwner))
                .toList();
    }

    /** Returns the one-based owner-sorted number of the closest totem, or zero if none exist. */
    public int getClosestBonusTotemNumber(BonusTotemType type) {
        List<BonusTotem> totems = getBonusTotemsByType(type);
        int closestNumber = 0;
        double closestDistance = Double.POSITIVE_INFINITY;
        for (int i = 0; i < totems.size(); i++) {
            double distance = totems.get(i).getDistanceToPlayer();
            // Keep the first totem in owner order when distances are equal.
            if (distance < closestDistance) {
                closestNumber = i + 1;
                closestDistance = distance;
            }
        }
        return closestNumber;
    }

    public BonusTotem getBonusTotem(BonusTotemType type, int index) {
        if (index < 0
                || index
                        >= bonusTotems.getOrDefault(type, new LinkedHashMap<>()).size()) {
            return null;
        }

        return getBonusTotemsByType(type).get(index);
    }
}
