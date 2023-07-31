/*
 * Copyright © Wynntils 2023.
 * This file is released under AGPLv3. See LICENSE for full license details.
 */
package com.wynntils.features.debug;

import com.google.common.collect.ComparisonChain;
import com.wynntils.core.components.Models;
import com.wynntils.core.config.Category;
import com.wynntils.core.config.ConfigCategory;
import com.wynntils.core.consumers.features.Feature;
import com.wynntils.core.consumers.features.properties.StartDisabled;
import com.wynntils.core.storage.RegisterStorage;
import com.wynntils.core.storage.Storage;
import com.wynntils.models.beacons.type.VerifiedBeacon;
import com.wynntils.models.lootrun.event.LootrunBeaconSelectedEvent;
import com.wynntils.models.lootrun.type.LootrunLocation;
import com.wynntils.models.lootrun.type.LootrunTaskType;
import com.wynntils.utils.mc.type.Location;
import java.util.Map;
import java.util.Optional;
import java.util.Set;
import java.util.TreeMap;
import java.util.TreeSet;
import net.minecraft.core.Position;
import net.minecraftforge.eventbus.api.SubscribeEvent;

@StartDisabled
@ConfigCategory(Category.DEBUG)
public class LootrunBeaconLocationCollectorFeature extends Feature {
    // Dumping to a storage is a bit weird,
    // but it's the easiest way to get the data out of the game for people to share.
    @RegisterStorage
    private final Storage<Map<LootrunLocation, Set<TaskLocation>>> tasks = new Storage<>(new TreeMap<>());

    @SubscribeEvent
    public void onLootrunBeaconSelected(LootrunBeaconSelectedEvent event) {
        VerifiedBeacon beacon = event.getBeacon();

        if (!beacon.getColor().getContentType().showsUpInLootruns()) return;

        Optional<LootrunTaskType> currentTaskTypeOpt = Models.Lootrun.getCurrentTaskType();
        if (currentTaskTypeOpt.isEmpty()) return;

        Optional<LootrunLocation> currentLocationOpt = Models.Lootrun.getCurrentLocation();
        if (currentLocationOpt.isEmpty()) return;
        Position position = beacon.getPosition();

        tasks.get().putIfAbsent(currentLocationOpt.get(), new TreeSet<>());
        tasks.get()
                .get(currentLocationOpt.get())
                .add(new TaskLocation(Location.containing(position), currentTaskTypeOpt.get()));
        tasks.touched();
    }

    // This location has to be a Location because Position doesn't have proper mapping, so GSON can't serialize it.
    private record TaskLocation(Location location, LootrunTaskType taskType) implements Comparable<TaskLocation> {
        @Override
        public int compareTo(LootrunBeaconLocationCollectorFeature.TaskLocation taskLocation) {
            return ComparisonChain.start()
                    .compare(location.x(), taskLocation.location.x())
                    .compare(location.y(), taskLocation.location.y())
                    .compare(location.z(), taskLocation.location.z())
                    .compare(taskType, taskLocation.taskType)
                    .result();
        }
    }
}
