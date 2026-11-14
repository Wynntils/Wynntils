/*
 * Copyright © Wynntils 2026.
 * This file is released under LGPLv3. See LICENSE for full license details.
 */
package com.wynntils.services.mapdata;

import com.wynntils.core.components.Service;
import com.wynntils.core.components.Services;
import com.wynntils.core.mod.event.WynntilsInitEvent;
import com.wynntils.services.mapdata.paths.PathCategoriesProvider;
import java.util.List;
import net.neoforged.bus.api.SubscribeEvent;

public final class MapPathsService extends Service {
    private static final PathCategoriesProvider PATH_CATEGORIES_PROVIDER = new PathCategoriesProvider();

    public MapPathsService() {
        super(List.of());
    }

    @SubscribeEvent
    public void onModInitFinished(WynntilsInitEvent.ModInitFinished event) {
        Services.MapData.registerBuiltInProvider(PATH_CATEGORIES_PROVIDER, this, false);
    }
}
