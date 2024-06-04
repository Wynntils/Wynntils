/*
 * Copyright © Wynntils 2024.
 * This file is released under LGPLv3. See LICENSE for full license details.
 */
package com.wynntils.services.mapdata;

import com.google.gson.reflect.TypeToken;
import com.wynntils.core.components.Managers;
import com.wynntils.core.components.Service;
import com.wynntils.core.components.Services;
import com.wynntils.core.net.Download;
import com.wynntils.core.net.UrlId;
import com.wynntils.services.map.PoiService;
import com.wynntils.services.mapdata.providers.builtin.CombatListProvider;
import com.wynntils.services.mapdata.providers.builtin.PlaceListProvider;
import com.wynntils.services.mapdata.providers.builtin.ServiceListProvider;
import java.util.List;

public class MapFeatureService extends Service {
    public MapFeatureService(MapDataService mapData) {
        super(List.of(mapData));

        loadPlaces();
        loadServices();
        loadCombatLocations();
    }

    private void loadPlaces() {
        Download dl = Managers.Net.download(UrlId.DATA_STATIC_MAPDATA_PLACES);
        dl.handleReader(reader -> {
            TypeToken<List<PlaceListProvider.PlaceLocation>> type = new TypeToken<>() {};
            List<PlaceListProvider.PlaceLocation> places = PoiService.GSON.fromJson(reader, type.getType());
            Services.MapData.PLACE_LIST_PROVIDER.updatePlaces(places);
        });
    }

    private void loadServices() {
        Download dl = Managers.Net.download(UrlId.DATA_STATIC_MAPDATA_SERVICES);
        dl.handleReader(reader -> {
            TypeToken<List<ServiceListProvider.ServiceLocation>> type = new TypeToken<>() {};
            List<ServiceListProvider.ServiceLocation> services = PoiService.GSON.fromJson(reader, type.getType());
            Services.MapData.SERVICE_LIST_PROVIDER.updateServices(services);
        });
    }

    private void loadCombatLocations() {
        Download dl = Managers.Net.download(UrlId.DATA_STATIC_MAPDATA_COMBAT_LOCATIONS);
        dl.handleReader(reader -> {
            TypeToken<List<CombatListProvider.CombatLocation>> type = new TypeToken<>() {};
            List<CombatListProvider.CombatLocation> combatLocations = PoiService.GSON.fromJson(reader, type.getType());
            Services.MapData.COMBAT_LIST_PROVIDER.updateCombats(combatLocations);
        });
    }
}
