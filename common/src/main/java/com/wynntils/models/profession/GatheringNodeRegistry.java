/*
 * Copyright © Wynntils 2026.
 * This file is released under LGPLv3. See LICENSE for full license details.
 */
package com.wynntils.models.profession;

import com.google.gson.reflect.TypeToken;
import com.wynntils.core.components.Managers;
import com.wynntils.core.components.Models;
import com.wynntils.core.net.Dependency;
import com.wynntils.core.net.DownloadRegistry;
import com.wynntils.core.net.UrlId;
import com.wynntils.models.profession.providers.GatheringNodeProvider;
import com.wynntils.models.profession.providers.GatheringNodeProvider.GatheringNodeLocation;
import java.io.Reader;
import java.util.List;

public class GatheringNodeRegistry {
    private final GatheringNodeProvider gatheringNodeProvider;

    public GatheringNodeRegistry(GatheringNodeProvider gatheringNodeProvider) {
        this.gatheringNodeProvider = gatheringNodeProvider;
    }

    public void registerDownloads(DownloadRegistry registry) {
        // The nodes are grouped into categories per source material, which can only be resolved
        // once the material registry is downloaded.
        registry.registerDownload(
                        UrlId.DATA_STATIC_GATHERING_NODE_MAPFEATURES,
                        Dependency.simple(Models.Profession, UrlId.DATA_STATIC_MATERIALS))
                .handleReader(this::handleGatheringNodes);
    }

    private void handleGatheringNodes(Reader reader) {
        TypeToken<List<GatheringNodeLocation>> type = new TypeToken<>() {};
        List<GatheringNodeLocation> nodes = Managers.Json.GSON.fromJson(reader, type.getType());

        gatheringNodeProvider.updateNodes(nodes);
    }
}
