/*
 * Copyright © Wynntils 2024-2026.
 * This file is released under LGPLv3. See LICENSE for full license details.
 */
package com.wynntils.services.destination;

import com.wynntils.core.WynntilsMod;
import com.wynntils.core.components.Service;
import com.wynntils.core.net.DownloadRegistry;
import com.wynntils.core.net.UrlId;
import java.io.Reader;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

public class DestinationService extends Service {
    private static final int DESTINATION_ABBREVIATION_LENGTH = 3;

    // Map<location, abbreviation>
    private Map<String, String> destinations = new HashMap<>();

    public DestinationService() {
        super(List.of());
    }

    @Override
    public void registerDownloads(DownloadRegistry registry) {
        registry.registerDownload(UrlId.DATA_STATIC_DESTINATIONS).handleReader(this::handleDestinations);
    }

    /**
     * Get the abbreviation of a location
     * @param location Full location name of the destination (eg. "Ragni")
     * @return Abbreviation of the location (eg. "Rag"). Reads from destination json first, if not found computes
     * a non-conflicting abbreviation from the location name.
     */
    public String getAbbreviation(String location) {
        destinations.computeIfAbsent(location, this::findNextAbbreviation);
        return destinations.get(location);
    }

    /**
     * Finds the next non-conflicting abbreviation for a location, excluding spaces.
     * @param location Full location name of the destination (eg. "Ragni")
     */
    private String findNextAbbreviation(String location) {
        String compactLocation = location.replace(" ", "");

        String fallbackAbbreviation = compactLocation.substring(0, DESTINATION_ABBREVIATION_LENGTH);

        for (int secondIndex = 1; secondIndex < compactLocation.length() - 1; secondIndex++) {
            for (int thirdIndex = secondIndex + 1; thirdIndex < compactLocation.length(); thirdIndex++) {
                String abbreviation = "" + compactLocation.charAt(0)
                        + compactLocation.charAt(secondIndex)
                        + compactLocation.charAt(thirdIndex);
                if (!destinations.containsValue(abbreviation)) {
                    WynntilsMod.warn("DestinationService: No destination found for " + location
                            + ", computed fallback abbreviation " + abbreviation);
                    return abbreviation;
                }
                fallbackAbbreviation = abbreviation;
            }
        }

        WynntilsMod.warn("DestinationService: No unique destination found for " + location
                + ", computed fallback abbreviation " + fallbackAbbreviation);
        return fallbackAbbreviation;
    }

    private void handleDestinations(Reader reader) {
        Map<String, String> newDestinations = new HashMap<>();

        Destination result = WynntilsMod.GSON.fromJson(reader, Destination.class);
        for (Destination.DestinationDetail destination : result.destinations) {
            newDestinations.put(destination.location, destination.abbreviation);
        }

        destinations = newDestinations;
    }

    private static class Destination {
        List<DestinationDetail> destinations;

        private static class DestinationDetail {
            String location;
            String abbreviation;
        }
    }
}
