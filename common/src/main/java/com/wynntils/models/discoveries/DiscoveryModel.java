/*
 * Copyright © Wynntils 2022.
 * This file is released under AGPLv3. See LICENSE for full license details.
 */
package com.wynntils.models.discoveries;

import com.google.common.reflect.TypeToken;
import com.wynntils.core.WynntilsMod;
import com.wynntils.core.components.Managers;
import com.wynntils.core.components.Model;
import com.wynntils.core.components.Models;
import com.wynntils.core.net.ApiResponse;
import com.wynntils.core.net.Download;
import com.wynntils.core.net.UrlId;
import com.wynntils.models.characterstats.CombatXpModel;
import com.wynntils.models.content.type.ContentInfo;
import com.wynntils.models.content.type.ContentType;
import com.wynntils.models.discoveries.event.DiscoveriesUpdatedEvent;
import com.wynntils.models.discoveries.profile.DiscoveryProfile;
import com.wynntils.models.discoveries.type.DiscoveryType;
import com.wynntils.models.map.CompassModel;
import com.wynntils.models.quests.QuestModel;
import com.wynntils.models.territories.TerritoryModel;
import com.wynntils.models.territories.profile.TerritoryProfile;
import com.wynntils.models.worlds.event.WorldStateEvent;
import com.wynntils.screens.maps.MainMapScreen;
import com.wynntils.utils.mc.McUtils;
import com.wynntils.utils.mc.type.Location;
import java.lang.reflect.Type;
import java.util.ArrayList;
import java.util.List;
import java.util.Map;
import java.util.stream.Stream;
import net.minecraft.ChatFormatting;
import net.minecraft.network.chat.Component;
import net.minecraftforge.eventbus.api.EventPriority;
import net.minecraftforge.eventbus.api.SubscribeEvent;

public final class DiscoveryModel extends Model {
    private List<DiscoveryInfo> discoveries = List.of();
    private List<DiscoveryInfo> secretDiscoveries = List.of();
    private List<DiscoveryInfo> discoveryInfoList = new ArrayList<>();

    private List<Component> discoveriesTooltip = List.of();
    private List<Component> secretDiscoveriesTooltip = List.of();

    public DiscoveryModel(
            CombatXpModel combatXpModel,
            CompassModel compassModel,
            QuestModel questModel,
            TerritoryModel territoryModel) {
        super(List.of(combatXpModel, compassModel, questModel, territoryModel));
    }

    @Override
    public void reloadData() {
        updateDiscoveriesResource();
    }

    @SubscribeEvent(priority = EventPriority.HIGH)
    public void onWorldStateChanged(WorldStateEvent e) {
        discoveries = List.of();
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

            Models.Compass.setCompassLocation(new Location(centerX, 0, centerZ));
        }
    }

    public void openSecretDiscoveryWiki(DiscoveryInfo discoveryInfo) {
        Managers.Net.openLink(UrlId.LINK_WIKI_LOOKUP, Map.of("title", discoveryInfo.getName()));
    }

    private void queryDiscoveries() {
        WynntilsMod.info("Requesting rescan of discoveries in Content Book");
        Models.Content.scanContentBook("World Discoveries", this::updateDiscoveriesFromQuery);
        Models.Content.scanContentBook("Secret Discoveries", this::updateSecretDiscoveriesFromQuery);
    }

    private void updateDiscoveriesFromQuery(List<ContentInfo> newContent) {
        List<DiscoveryInfo> newDiscoveries = new ArrayList<>();
        for (ContentInfo content : newContent) {
            System.out.println("New discovery: " + content);
            if (content.type() != ContentType.WORLD_DISCOVERY) {
                WynntilsMod.warn("Incorrect discovery content type recieved: " + content);
                continue;
            }
            DiscoveryInfo discoveryInfo = getDiscoveryInfoFromContent(content);
            newDiscoveries.add(discoveryInfo);
        }

        discoveries = newDiscoveries;
        WynntilsMod.postEvent(new DiscoveriesUpdatedEvent.Normal());
    }

    private void updateSecretDiscoveriesFromQuery(List<ContentInfo> newContent) {
        List<DiscoveryInfo> newDiscoveries = new ArrayList<>();
        for (ContentInfo content : newContent) {
            System.out.println("New secret discovery: " + content);
            if (content.type() != ContentType.SECRET_DISCOVERY) {
                WynntilsMod.warn("Incorrect secret discovery content type recieved: " + content);
                continue;
            }
            DiscoveryInfo discoveryInfo = getDiscoveryInfoFromContent(content);
            newDiscoveries.add(discoveryInfo);
        }

        secretDiscoveries = newDiscoveries;
        WynntilsMod.postEvent(new DiscoveriesUpdatedEvent.Secret());
    }

    private DiscoveryInfo getDiscoveryInfoFromContent(ContentInfo content) {
        // FIXME
        return null;
    }

    public void setDiscoveriesTooltip(List<Component> newTooltip) {
        discoveriesTooltip = newTooltip;
    }

    public void setSecretDiscoveriesTooltip(List<Component> newTooltip) {
        secretDiscoveriesTooltip = newTooltip;
    }

    public List<Component> getDiscoveriesTooltip() {
        return discoveriesTooltip;
    }

    public List<Component> getSecretDiscoveriesTooltip() {
        return secretDiscoveriesTooltip;
    }

    public Stream<DiscoveryInfo> getAllDiscoveries() {
        return Stream.concat(discoveries.stream(), secretDiscoveries.stream());
    }

    public List<DiscoveryInfo> getDiscoveryInfoList() {
        return discoveryInfoList;
    }

    private void locateSecretDiscovery(String name, DiscoveryOpenAction action) {
        ApiResponse apiResponse = Managers.Net.callApi(UrlId.API_WIKI_DISCOVERY_QUERY, Map.of("name", name));
        apiResponse.handleJsonObject(json -> {
            if (json.has("error")) { // Returns error if page does not exist
                McUtils.sendMessageToClient(Component.literal(
                        ChatFormatting.RED + "Unable to find discovery coordinates. (Wiki page not found)"));
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
                McUtils.sendMessageToClient(Component.literal(
                        ChatFormatting.RED + "Unable to find discovery coordinates. (Wiki template not located)"));
                return;
            }

            if (x == 0 && z == 0) {
                McUtils.sendMessageToClient(Component.literal(
                        ChatFormatting.RED + "Unable to find discovery coordinates. (Wiki coordinates not located)"));
                return;
            }

            switch (action) {
                    // We can't run this is on request thread
                case MAP -> Managers.TickScheduler.scheduleNextTick(
                        () -> McUtils.mc().setScreen(MainMapScreen.create(x, z)));
                case COMPASS -> Models.Compass.setCompassLocation(new Location(x, 0, z));
            }
        });
    }

    private void updateDiscoveriesResource() {
        Download dl = Managers.Net.download(UrlId.DATA_STATIC_DISCOVERIES);
        dl.handleReader(reader -> {
            Type type = new TypeToken<ArrayList<DiscoveryProfile>>() {}.getType();
            List<DiscoveryProfile> discoveries = WynntilsMod.GSON.fromJson(reader, type);
            discoveryInfoList = discoveries.stream().map(DiscoveryInfo::new).toList();
        });
    }

    public void reloadDiscoveries() {
        updateDiscoveriesResource();
        queryDiscoveries();
    }

    public enum DiscoveryOpenAction {
        MAP,
        COMPASS
    }
}
