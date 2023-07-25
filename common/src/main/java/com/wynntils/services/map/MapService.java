/*
 * Copyright © Wynntils 2022.
 * This file is released under AGPLv3. See LICENSE for full license details.
 */
package com.wynntils.services.map;

import com.google.common.reflect.TypeToken;
import com.mojang.blaze3d.platform.NativeImage;
import com.wynntils.core.WynntilsMod;
import com.wynntils.core.components.Managers;
import com.wynntils.core.components.Service;
import com.wynntils.core.net.Download;
import com.wynntils.core.net.UrlId;
import com.wynntils.utils.type.BoundingBox;
import java.io.IOException;
import java.lang.reflect.Type;
import java.net.URI;
import java.util.List;
import java.util.concurrent.CopyOnWriteArrayList;

public final class MapService extends Service {
    private final List<MapTexture> maps = new CopyOnWriteArrayList<>();

    public MapService() {
        super(List.of());

        loadData();
    }

    @Override
    public void reloadData() {
        loadData();
    }

    private void loadData() {
        loadMaps();
    }

    public List<MapTexture> getMapsForBoundingBox(BoundingBox box) {
        return maps.stream().filter(map -> box.intersects(map.getBox())).toList();
    }

    private void loadMaps() {
        maps.clear();

        Download dl = Managers.Net.download(UrlId.DATA_STATIC_MAPS);
        dl.handleReader(reader -> {
            Type type = new TypeToken<List<MapPartProfile>>() {}.getType();

            List<MapPartProfile> mapPartList = WynntilsMod.GSON.fromJson(reader, type);
            for (MapPartProfile mapPart : mapPartList) {
                String fileName = mapPart.md5 + ".png";

                loadMapPart(mapPart, fileName);
            }
        });
    }

    private void loadMapPart(MapPartProfile mapPart, String fileName) {
        Download dl = Managers.Net.download(URI.create(mapPart.url), "maps/" + fileName, mapPart.md5);
        dl.handleInputStream(
                inputStream -> {
                    try {
                        NativeImage nativeImage = NativeImage.read(inputStream);
                        MapTexture mapPartImage =
                                new MapTexture(fileName, nativeImage, mapPart.x1, mapPart.z1, mapPart.x2, mapPart.z2);
                        maps.add(mapPartImage);
                    } catch (IOException e) {
                        WynntilsMod.warn("IOException occurred while loading map image of " + mapPart.name, e);
                    }
                },
                onError -> WynntilsMod.warn("Error occurred while download map image of " + mapPart.name, onError));
    }

    private static final class MapPartProfile {
        final String name;
        final String url;
        final int x1;
        final int z1;
        final int x2;
        final int z2;
        final String md5;

        private MapPartProfile(String name, String url, int x1, int z1, int x2, int z2, String md5) {
            this.name = name;
            this.url = url;
            this.x1 = x1;
            this.z1 = z1;
            this.x2 = x2;
            this.z2 = z2;
            this.md5 = md5;
        }
    }
}
