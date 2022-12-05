/*
 * Copyright © Wynntils 2022.
 * This file is released under AGPLv3. See LICENSE for full license details.
 */
package com.wynntils.wynn.model.discoveries;

import com.google.common.reflect.TypeToken;
import com.google.gson.Gson;
import com.wynntils.core.WynntilsMod;
import com.wynntils.core.managers.CoreManager;
import com.wynntils.core.net.Reference;
import com.wynntils.core.net.api.ApiRequester;
import com.wynntils.core.net.api.RequestResponse;
import com.wynntils.core.net.downloader.DownloadableResource;
import com.wynntils.core.net.downloader.Downloader;
import com.wynntils.gui.screens.maps.MainMapScreen;
import com.wynntils.mc.MinecraftSchedulerManager;
import com.wynntils.mc.objects.Location;
import com.wynntils.mc.utils.McUtils;
import com.wynntils.utils.Utils;
import com.wynntils.utils.WebUtils;
import com.wynntils.wynn.event.DiscoveriesUpdatedEvent;
import com.wynntils.wynn.event.WorldStateEvent;
import com.wynntils.wynn.model.CompassModel;
import com.wynntils.wynn.model.discoveries.objects.DiscoveryInfo;
import com.wynntils.wynn.model.discoveries.objects.DiscoveryType;
import com.wynntils.wynn.netresources.profiles.DiscoveryProfile;
import com.wynntils.wynn.netresources.profiles.TerritoryProfile;
import java.lang.reflect.Type;
import java.util.ArrayList;
import java.util.List;
import java.util.stream.Stream;
import net.minecraft.ChatFormatting;
import net.minecraft.network.chat.Component;
import net.minecraft.network.chat.TextComponent;
import net.minecraftforge.eventbus.api.EventPriority;
import net.minecraftforge.eventbus.api.SubscribeEvent;

public class DiscoveryManager extends CoreManager {
    private static final DiscoveryContainerQueries CONTAINER_QUERIES = new DiscoveryContainerQueries();
    private static final Gson GSON = new Gson();

    private static List<DiscoveryInfo> discoveries = List.of();
    private static List<DiscoveryInfo> secretDiscoveries = List.of();
    private static List<DiscoveryInfo> discoveryInfoList = new ArrayList<>();

    private static List<Component> discoveriesTooltip = List.of();
    private static List<Component> secretDiscoveriesTooltip = List.of();

    public static void init() {}

    @SubscribeEvent(priority = EventPriority.HIGH)
    public static void onWorldStateChanged(WorldStateEvent e) {
        discoveries = List.of();
        secretDiscoveries = List.of();
    }

    public static void openDiscoveryOnMap(DiscoveryInfo discoveryInfo) {
        if (discoveryInfo.getType() == DiscoveryType.SECRET) {
            locateSecretDiscovery(discoveryInfo.getName(), DiscoveryOpenAction.MAP);
            return;
        }

        if (discoveryInfo.getGuildTerritory() != null) {
            TerritoryProfile guildTerritory = discoveryInfo.getGuildTerritory();

            int centerX = (guildTerritory.getEndX() + guildTerritory.getStartX()) / 2;
            int centerZ = (guildTerritory.getEndZ() + guildTerritory.getStartZ()) / 2;

            McUtils.mc().setScreen(new MainMapScreen(centerX, centerZ));
        }
    }

    public static void setDiscoveryCompass(DiscoveryInfo discoveryInfo) {
        if (discoveryInfo.getType() == DiscoveryType.SECRET) {
            locateSecretDiscovery(discoveryInfo.getName(), DiscoveryOpenAction.COMPASS);
            return;
        }

        if (discoveryInfo.getGuildTerritory() != null) {
            TerritoryProfile guildTerritory = discoveryInfo.getGuildTerritory();

            int centerX = (guildTerritory.getEndX() + guildTerritory.getStartX()) / 2;
            int centerZ = (guildTerritory.getEndZ() + guildTerritory.getStartZ()) / 2;

            CompassModel.setCompassLocation(new Location(centerX, 0, centerZ));
        }
    }

    public static void openSecretDiscoveryWiki(DiscoveryInfo discoveryInfo) {
        String wikiUrl = "https://wynncraft.fandom.com/wiki/" + WebUtils.encodeForWikiTitle(discoveryInfo.getName());
        Utils.openUrl(wikiUrl);
    }

    public static void queryDiscoveries() {
        CONTAINER_QUERIES.queryDiscoveries();
    }

    public static void setDiscoveries(List<DiscoveryInfo> newDiscoveries) {
        discoveries = newDiscoveries;
        WynntilsMod.postEvent(new DiscoveriesUpdatedEvent.Normal());
    }

    public static void setSecretDiscoveries(List<DiscoveryInfo> newDiscoveries) {
        secretDiscoveries = newDiscoveries;
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

    public static List<DiscoveryInfo> getDiscoveryInfoList() {
        return discoveryInfoList;
    }

    private static void locateSecretDiscovery(String name, DiscoveryOpenAction action) {
        String queryUrl = Reference.URLs.getWikiDiscoveryQuery();
        String url = queryUrl + WebUtils.encodeForWikiTitle(name);
        RequestResponse response = ApiRequester.get(url, "SecretWikiQuery");
        response.handleJsonObject(json -> {
            if (json.has("error")) { // Returns error if page does not exist
                McUtils.sendMessageToClient(new TextComponent(
                        ChatFormatting.RED + "Unable to find discovery coordinates. (Wiki page not found)"));
                return true;
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

            int xEnd = Math.min(xLocation.indexOf("|"), xLocation.indexOf("}}"));
            int zEnd = Math.min(zLocation.indexOf("|"), zLocation.indexOf("}}"));

            int x;
            int z;

            try {
                x = Integer.parseInt(xLocation.substring(12, xEnd));
                z = Integer.parseInt(zLocation.substring(12, zEnd));
            } catch (NumberFormatException e) {
                McUtils.sendMessageToClient(new TextComponent(
                        ChatFormatting.RED + "Unable to find discovery coordinates. (Wiki template not located)"));
                return true;
            }

            if (x == 0 && z == 0) {
                McUtils.sendMessageToClient(new TextComponent(
                        ChatFormatting.RED + "Unable to find discovery coordinates. (Wiki coordinates not located)"));
                return true;
            }

            switch (action) {
                case MAP -> {
                    // We can't run this is on request thread
                    MinecraftSchedulerManager.queueRunnable(() -> McUtils.mc().setScreen(new MainMapScreen(x, z)));
                }
                case COMPASS -> {
                    CompassModel.setCompassLocation(new Location(x, 0, z));
                }
            }

            return true;
        });
    }

    private static void updateDiscoveriesResource() {
        String url = Reference.URLs.getDiscoveries();
        DownloadableResource dl = Downloader.download(url, "discoveries.json", "discoveries");
        dl.handleJsonObject(json -> {
            Type type = new TypeToken<ArrayList<DiscoveryProfile>>() {}.getType();

            List<DiscoveryProfile> discoveries = GSON.fromJson(json, type);
            discoveryInfoList = discoveries.stream().map(DiscoveryInfo::new).toList();
            return true;
        });
    }

    public static void reloadDiscoveries() {
        updateDiscoveriesResource();
        queryDiscoveries();
    }

    public enum DiscoveryOpenAction {
        MAP,
        COMPASS
    }
}
