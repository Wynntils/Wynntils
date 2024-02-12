/*
 * Copyright © Wynntils 2024.
 * This file is released under LGPLv3. See LICENSE for full license details.
 */
package com.wynntils.services.destination;

import com.wynntils.core.WynntilsMod;
import com.wynntils.core.components.Managers;
import com.wynntils.core.components.Service;
import com.wynntils.core.net.Download;
import com.wynntils.core.net.UrlId;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

public class DestinationService extends Service {
    // Map<location, abbreviation>
    private Map<String, String> destinations;

    public DestinationService() {
        super(List.of());

        loadDestinations();
    }

    @Override
    public void reloadData() {
        loadDestinations();
    }

    /**
     * Get the abbreviation of a location
     * @param location Full location name of the destination (eg. "Ragni")
     * @return Abbreviation of the location (eg. "Ra") or null if not found
     */
    public String getAbbreviation(String location) {
        if (!destinations.containsKey(location)) {
            return null;
        }
        return destinations.get(location);
    }

    private void loadDestinations() {
        destinations = new HashMap<>();
        Download dl = Managers.Net.download(UrlId.DATA_STATIC_DESTINATIONS);
        dl.handleReader(reader -> {
            Destination result = WynntilsMod.GSON.fromJson(reader, Destination.class);
            for (Destination.DestinationDetail destination : result.destinations) {
                destinations.put(destination.location, destination.abbreviation);
            }
        });
    }

    private static class Destination {
        List<DestinationDetail> destinations;

        static class DestinationDetail {
            String location;
            String abbreviation;
        }
    }
}
