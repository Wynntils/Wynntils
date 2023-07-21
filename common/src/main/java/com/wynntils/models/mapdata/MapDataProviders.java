/*
 * Copyright © Wynntils 2023.
 * This file is released under AGPLv3. See LICENSE for full license details.
 */
package com.wynntils.models.mapdata;

import com.wynntils.models.mapdata.providers.MapDataProvider;
import com.wynntils.models.mapdata.providers.builtin.CategoriesProvider;
import com.wynntils.models.mapdata.providers.builtin.CharacterProvider;
import com.wynntils.models.mapdata.providers.builtin.MapIconsProvider;
import com.wynntils.models.mapdata.providers.json.JsonProvider;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.stream.Stream;

public class MapDataProviders {
    private static final List<MapDataProvider> BUILT_IN_PROVIDERS =
            List.of(new CategoriesProvider(), new CharacterProvider(), new MapIconsProvider());

    private final List<MapDataProvider> providers = new ArrayList<>();
    private final Map<String, MapDataProvider> onlineProviders = new HashMap<>();

    public MapDataProviders() {
        providers.addAll(BUILT_IN_PROVIDERS);
        //   providers.add(createLocalProvider("mapdata.json"));
        createOnlineProvider(
                "online-1",
                "https://raw.githubusercontent.com/magicus/Artemis/map-data-rewrite/common/src/main/resources/assets/wynntils/mapdata.json");
    }

    Stream<MapDataProvider> getProviders() {
        return providers.stream();
    }

    // per-account, per-character or shared
    // can be added just from disk, or downloaded from an url
    MapDataProvider createLocalProvider(String filename) {
        return JsonProvider.loadLocalResource(filename);
    }

    void createOnlineProvider(String id, String url) {
        JsonProvider.loadOnlineResource(id, url, this::registerOnlineProvider);
    }

    private void registerOnlineProvider(String id, MapDataProvider provider) {
        System.out.println("REGISTERING provider for " + id);
        onlineProviders.put(id, provider);
        providers.add(provider);
    }

    //
}
