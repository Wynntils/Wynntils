/*
 * Copyright © Wynntils 2023.
 * This file is released under AGPLv3. See LICENSE for full license details.
 */
package com.wynntils.features.debug;

import com.wynntils.core.components.Models;
import com.wynntils.core.consumers.features.Feature;
import com.wynntils.core.consumers.features.properties.StartDisabled;
import com.wynntils.core.persisted.Persisted;
import com.wynntils.core.persisted.config.Category;
import com.wynntils.core.persisted.config.ConfigCategory;
import com.wynntils.core.persisted.storage.Storage;
import com.wynntils.models.beacons.type.Beacon;
import com.wynntils.models.lootrun.event.LootrunBeaconSelectedEvent;
import com.wynntils.models.lootrun.type.LootrunLocation;
import com.wynntils.models.lootrun.type.LootrunTaskType;
import com.wynntils.models.lootrun.type.TaskLocation;
import com.wynntils.utils.mc.type.Location;
import java.util.Map;
import java.util.Optional;
import java.util.Set;
import java.util.TreeMap;
import java.util.TreeSet;
import net.minecraftforge.eventbus.api.SubscribeEvent;

@StartDisabled
@ConfigCategory(Category.DEBUG)
public class LootrunBeaconLocationCollectorFeature extends Feature {
    // Dumping to a storage is a bit weird,
    // but it's the easiest way to get the data out of the game for people to share.
    @Persisted
    private final Storage<Map<LootrunLocation, Set<TaskLocation>>> tasks = new Storage<>(new TreeMap<>());

    @SubscribeEvent
    public void onLootrunBeaconSelected(LootrunBeaconSelectedEvent event) {
        Beacon beacon = event.getBeacon();

        if (!beacon.color().isUsedInLootruns()) return;

        Optional<LootrunTaskType> currentTaskTypeOpt = Models.Lootrun.getTaskType();
        if (currentTaskTypeOpt.isEmpty()) return;

        Optional<LootrunLocation> currentLocationOpt = Models.Lootrun.getLocation();
        if (currentLocationOpt.isEmpty()) return;

        tasks.get().putIfAbsent(currentLocationOpt.get(), new TreeSet<>());
        tasks.get()
                .get(currentLocationOpt.get())
                .add(new TaskLocation("", Location.containing(beacon.position()), currentTaskTypeOpt.get()));
        tasks.touched();
    }
}
