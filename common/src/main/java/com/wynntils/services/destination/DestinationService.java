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
     * @return Abbreviation of the location (eg. "Ra"). Reads from destination json first, if not found returns first + next non-conflicting letter of the location name.
     */
    public String getAbbreviation(String location) {
        destinations.computeIfAbsent(location, this::findNextAbbreviation);
        return destinations.get(location);
    }

    /**
     * Finds the next non-conflicting abbreviation for a location, excluding spaces
     * @param location Full location name of the destination (eg. "Ragni")
     */
    private String findNextAbbreviation(String location) {
        String abbreviation = location.substring(0, 2);
        int i = 2;
        while (destinations.containsValue(abbreviation)) {
            if (location.charAt(i) == ' ') i++;
            abbreviation = location.charAt(0) + "" + location.charAt(i);
            i++;
        }
        WynntilsMod.warn("DestinationService: No destination found for " + location
                + ", computed fallback abbreviation " + abbreviation);
        return abbreviation;
    }

    private void loadDestinations() {
        Map<String, String> newDestinations = new HashMap<>();

        Download dl = Managers.Net.download(UrlId.DATA_STATIC_DESTINATIONS);
        dl.handleReader(reader -> {
            Destination result = WynntilsMod.GSON.fromJson(reader, Destination.class);
            for (Destination.DestinationDetail destination : result.destinations) {
                newDestinations.put(destination.location, destination.abbreviation);
            }
        });

        destinations = newDestinations;
    }

    private static class Destination {
        List<DestinationDetail> destinations;

        static class DestinationDetail {
            String location;
            String abbreviation;
        }
    }
}
