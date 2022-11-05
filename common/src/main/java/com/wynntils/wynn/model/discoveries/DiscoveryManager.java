/*
 * Copyright © Wynntils 2022.
 * This file is released under AGPLv3. See LICENSE for full license details.
 */
package com.wynntils.wynn.model.discoveries;

import com.wynntils.core.WynntilsMod;
import com.wynntils.core.managers.CoreManager;
import com.wynntils.wynn.event.DiscoveriesUpdatedEvent;
import com.wynntils.wynn.event.WorldStateEvent;
import com.wynntils.wynn.model.discoveries.objects.DiscoveryInfo;
import java.util.List;
import java.util.stream.Stream;
import net.minecraft.network.chat.Component;
import net.minecraftforge.eventbus.api.EventPriority;
import net.minecraftforge.eventbus.api.SubscribeEvent;

public class DiscoveryManager extends CoreManager {
    private static final DiscoveryContainerQueries CONTAINER_QUERIES = new DiscoveryContainerQueries();

    private static List<DiscoveryInfo> discoveries = List.of();
    private static List<DiscoveryInfo> secretDiscoveries = List.of();

    private static List<Component> discoveriesTooltip = List.of();
    private static List<Component> secretDiscoveriesTooltip = List.of();

    public static void init() {}

    @SubscribeEvent(priority = EventPriority.HIGH)
    public static void onWorldStateChanged(WorldStateEvent e) {
        discoveries = List.of();
        secretDiscoveries = List.of();
    }

    public static void queryDiscoveries() {
        CONTAINER_QUERIES.queryDiscoveries();
    }

    public static void setDiscoveries(List<DiscoveryInfo> newDiscoveries) {
        discoveries = newDiscoveries;

        WynntilsMod.info("Discovered " + discoveries.size() + " discoveries");
        WynntilsMod.postEvent(new DiscoveriesUpdatedEvent.Normal());
    }

    public static void setSecretDiscoveries(List<DiscoveryInfo> newDiscoveries) {
        secretDiscoveries = newDiscoveries;

        WynntilsMod.info("Discovered " + secretDiscoveries.size() + " secret discoveries");
        WynntilsMod.postEvent(new DiscoveriesUpdatedEvent.Secret());
    }

    public static void setDiscoveriesTooltip(List<Component> newTooltip) {
        discoveriesTooltip = newTooltip;
    }

    public static void setSecretDiscoveriesTooltip(List<Component> newTooltip) {
        secretDiscoveriesTooltip = newTooltip;
    }

    public static List<Component> getDiscoveriesTooltip() {
        return discoveriesTooltip;
    }

    public static List<Component> getSecretDiscoveriesTooltip() {
        return secretDiscoveriesTooltip;
    }

    public static Stream<DiscoveryInfo> getAllDiscoveries() {
        return Stream.concat(discoveries.stream(), secretDiscoveries.stream());
    }
}
