/*
 * Copyright © Wynntils 2022-2023.
 * This file is released under LGPLv3. See LICENSE for full license details.
 */
package com.wynntils.models.activities.discoveries;

import com.wynntils.core.WynntilsMod;
import com.wynntils.core.components.Managers;
import com.wynntils.core.components.Model;
import com.wynntils.core.components.Models;
import com.wynntils.core.net.ApiResponse;
import com.wynntils.core.net.UrlId;
import com.wynntils.core.text.StyledText;
import com.wynntils.models.activities.event.ActivityUpdatedEvent;
import com.wynntils.models.activities.type.ActivityInfo;
import com.wynntils.models.activities.type.ActivitySortOrder;
import com.wynntils.models.activities.type.ActivityType;
import com.wynntils.models.activities.type.DiscoveryType;
import com.wynntils.models.territories.profile.TerritoryProfile;
import com.wynntils.models.worlds.event.WorldStateEvent;
import com.wynntils.screens.maps.MainMapScreen;
import com.wynntils.utils.mc.McUtils;
import com.wynntils.utils.mc.type.Location;
import java.util.ArrayList;
import java.util.Comparator;
import java.util.List;
import java.util.Map;
import java.util.stream.Collectors;
import java.util.stream.Stream;
import net.minecraft.network.chat.Component;
import net.minecraftforge.eventbus.api.EventPriority;
import net.minecraftforge.eventbus.api.SubscribeEvent;

public final class DiscoveryModel extends Model {
    // From container query updates
    private List<DiscoveryInfo> territoryDiscoveries = List.of();
    private List<DiscoveryInfo> worldDiscoveries = List.of();
    private List<DiscoveryInfo> secretDiscoveries = List.of();
    private List<StyledText> territoryDiscoveriesTooltip = List.of();
    private List<StyledText> worldDiscoveriesTooltip = List.of();
    private List<StyledText> secretDiscoveriesTooltip = List.of();

    public DiscoveryModel() {
        super(List.of());
    }

    @SubscribeEvent(priority = EventPriority.HIGH)
    public void onWorldStateChanged(WorldStateEvent e) {
        territoryDiscoveries = List.of();
        worldDiscoveries = List.of();
        secretDiscoveries = List.of();
    }

    public void openDiscoveryOnMap(DiscoveryInfo discoveryInfo) {
        if (discoveryInfo.getType() == DiscoveryType.SECRET) {
            locateSecretDiscovery(discoveryInfo.getName(), DiscoveryOpenAction.MAP);
            return;
        }

        TerritoryProfile guildTerritory = Models.Territory.getTerritoryProfile(discoveryInfo.getName());
        if (guildTerritory != null) {
            int centerX = (guildTerritory.getEndX() + guildTerritory.getStartX()) / 2;
            int centerZ = (guildTerritory.getEndZ() + guildTerritory.getStartZ()) / 2;

            McUtils.mc().setScreen(MainMapScreen.create(centerX, centerZ));
        }
    }

    public void setDiscoveryCompass(DiscoveryInfo discoveryInfo) {
        if (discoveryInfo.getType() == DiscoveryType.SECRET) {
            locateSecretDiscovery(discoveryInfo.getName(), DiscoveryOpenAction.COMPASS);
            return;
        }

        TerritoryProfile guildTerritory = Models.Territory.getTerritoryProfile(discoveryInfo.getName());
        if (guildTerritory != null) {
            int centerX = (guildTerritory.getEndX() + guildTerritory.getStartX()) / 2;
            int centerZ = (guildTerritory.getEndZ() + guildTerritory.getStartZ()) / 2;

            Models.Marker.USER_WAYPOINTS_PROVIDER.addLocation(new Location(centerX, 0, centerZ));
        }
    }

    public void openSecretDiscoveryWiki(DiscoveryInfo discoveryInfo) {
        Managers.Net.openLink(UrlId.LINK_WIKI_LOOKUP, Map.of("title", discoveryInfo.getName()));
    }

    private void queryDiscoveries() {
        WynntilsMod.info("Requesting rescan of discoveries in Content Book");

        // This order is a bit arbitrary, but it's the order they appear in the Content Book,
        // so we can use this as a workaround to parse them faster.
        Models.Activity.scanContentBook(ActivityType.SECRET_DISCOVERY, this::updateSecretDiscoveriesFromQuery);
        Models.Activity.scanContentBook(ActivityType.WORLD_DISCOVERY, this::updateWorldDiscoveriesFromQuery);
        Models.Activity.scanContentBook(ActivityType.TERRITORIAL_DISCOVERY, this::updateTerritoryDiscoveriesFromQuery);
    }

    private void updateTerritoryDiscoveriesFromQuery(List<ActivityInfo> newActivities, List<StyledText> progress) {
        List<DiscoveryInfo> newDiscoveries = new ArrayList<>();
        for (ActivityInfo activity : newActivities) {
            if (activity.type() != ActivityType.TERRITORIAL_DISCOVERY) {
                WynntilsMod.warn("Incorrect territory discovery activity type recieved: " + activity);
                continue;
            }
            DiscoveryInfo discoveryInfo = getDiscoveryInfoFromActivity(activity);
            newDiscoveries.add(discoveryInfo);
        }

        territoryDiscoveries = newDiscoveries;
        territoryDiscoveriesTooltip = progress;
        WynntilsMod.postEvent(new ActivityUpdatedEvent(ActivityType.TERRITORIAL_DISCOVERY));
    }

    private void updateWorldDiscoveriesFromQuery(List<ActivityInfo> newActivities, List<StyledText> progress) {
        List<DiscoveryInfo> newDiscoveries = new ArrayList<>();
        for (ActivityInfo activity : newActivities) {
            if (activity.type() != ActivityType.WORLD_DISCOVERY) {
                WynntilsMod.warn("Incorrect discovery activity type recieved: " + activity);
                continue;
            }
            DiscoveryInfo discoveryInfo = getDiscoveryInfoFromActivity(activity);
            newDiscoveries.add(discoveryInfo);
        }

        worldDiscoveries = newDiscoveries;
        worldDiscoveriesTooltip = progress;
        WynntilsMod.postEvent(new ActivityUpdatedEvent(ActivityType.WORLD_DISCOVERY));
    }

    private void updateSecretDiscoveriesFromQuery(List<ActivityInfo> newActivities, List<StyledText> progress) {
        List<DiscoveryInfo> newDiscoveries = new ArrayList<>();
        for (ActivityInfo activity : newActivities) {
            if (activity.type() != ActivityType.SECRET_DISCOVERY) {
                WynntilsMod.warn("Incorrect secret discovery activity type recieved: " + activity);
                continue;
            }
            DiscoveryInfo discoveryInfo = getDiscoveryInfoFromActivity(activity);
            newDiscoveries.add(discoveryInfo);
        }

        secretDiscoveries = newDiscoveries;
        secretDiscoveriesTooltip = progress;
        WynntilsMod.postEvent(new ActivityUpdatedEvent(ActivityType.SECRET_DISCOVERY));
    }

    private DiscoveryInfo getDiscoveryInfoFromActivity(ActivityInfo activity) {
        return DiscoveryInfo.fromActivityInfo(activity);
    }

    public List<Component> getDiscoveriesTooltip() {
        return Stream.concat(
                        territoryDiscoveriesTooltip.stream().map(StyledText::getComponent),
                        worldDiscoveriesTooltip.stream().map(StyledText::getComponent))
                .collect(Collectors.toList());
    }

    public List<Component> getSecretDiscoveriesTooltip() {
        return secretDiscoveriesTooltip.stream().map(StyledText::getComponent).collect(Collectors.toList());
    }

    public Stream<DiscoveryInfo> getAllDiscoveries(ActivitySortOrder sortOrder) {
        if (sortOrder == ActivitySortOrder.DISTANCE) {
            throw new IllegalArgumentException("Cannot sort discoveries by distance");
        }

        // All discoveries are always sorted by status (available then unavailable), and then
        // the given sort order, and finally a third way if the given sort order is equal.
        Stream<DiscoveryInfo> baseStream = Stream.concat(
                Stream.concat(territoryDiscoveries.stream(), worldDiscoveries.stream()), secretDiscoveries.stream());

        return switch (sortOrder) {
            case LEVEL -> baseStream.sorted(Comparator.comparing(DiscoveryInfo::isDiscovered)
                    .thenComparing(DiscoveryInfo::getMinLevel)
                    .thenComparing(DiscoveryInfo::getName));
            case ALPHABETIC -> baseStream.sorted(Comparator.comparing(DiscoveryInfo::isDiscovered)
                    .thenComparing(DiscoveryInfo::getName)
                    .thenComparing(DiscoveryInfo::getMinLevel));
            case DISTANCE -> null;
        };
    }

    public Stream<DiscoveryInfo> getAllCompletedDiscoveries(ActivitySortOrder sortOrder) {
        return getAllDiscoveries(sortOrder).filter(DiscoveryInfo::isDiscovered);
    }

    private void locateSecretDiscovery(String name, DiscoveryOpenAction action) {
        ApiResponse apiResponse = Managers.Net.callApi(UrlId.API_WIKI_DISCOVERY_QUERY, Map.of("name", name));
        apiResponse.handleJsonObject(json -> {
            if (json.has("error")) { // Returns error if page does not exist
                McUtils.sendErrorToClient("Unable to find discovery coordinates. (Wiki page not found)");
                return;
            }

            String wikiText = json.get("parse")
                    .getAsJsonObject()
                    .get("wikitext")
                    .getAsJsonObject()
                    .get("*")
                    .getAsString()
                    .replace(" ", "")
                    .replace("\n", "");

            String xLocation = wikiText.substring(wikiText.indexOf("xcoordinate="));
            String zLocation = wikiText.substring(wikiText.indexOf("zcoordinate="));

            int xEnd = Math.min(xLocation.indexOf('|'), xLocation.indexOf("}}"));
            int zEnd = Math.min(zLocation.indexOf('|'), zLocation.indexOf("}}"));

            int x;
            int z;

            try {
                x = Integer.parseInt(xLocation.substring(12, xEnd));
                z = Integer.parseInt(zLocation.substring(12, zEnd));
            } catch (NumberFormatException e) {
                McUtils.sendErrorToClient("Unable to find discovery coordinates. (Wiki template not located)");
                return;
            }

            if (x == 0 && z == 0) {
                McUtils.sendErrorToClient("Unable to find discovery coordinates. (Wiki coordinates not located)");
                return;
            }

            switch (action) {
                    // We can't run this is on request thread
                case MAP -> Managers.TickScheduler.scheduleNextTick(
                        () -> McUtils.mc().setScreen(MainMapScreen.create(x, z)));
                case COMPASS -> Models.Marker.USER_WAYPOINTS_PROVIDER.addLocation(new Location(x, 0, z));
            }
        });
    }

    public void reloadDiscoveries() {
        queryDiscoveries();
    }

    public enum DiscoveryOpenAction {
        MAP,
        COMPASS
    }
}
