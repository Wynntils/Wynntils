/*
 * Copyright © Wynntils 2022-2026.
 * This file is released under LGPLv3. See LICENSE for full license details.
 */
package com.wynntils.services.map;

import com.google.common.reflect.TypeToken;
import com.mojang.blaze3d.platform.NativeImage;
import com.wynntils.core.WynntilsMod;
import com.wynntils.core.components.Managers;
import com.wynntils.core.components.Service;
import com.wynntils.core.net.Download;
import com.wynntils.core.net.DownloadRegistry;
import com.wynntils.core.net.UrlId;
import com.wynntils.utils.mc.McUtils;
import com.wynntils.utils.type.BoundingBox;
import com.wynntils.utils.type.BoundingCircle;
import com.wynntils.utils.type.BoundingShape;
import java.io.IOException;
import java.io.Reader;
import java.lang.reflect.Type;
import java.net.URI;
import java.util.List;
import java.util.concurrent.CopyOnWriteArrayList;
import net.minecraft.core.Position;

public final class MapService extends Service {
    private List<MapTexture> maps = new CopyOnWriteArrayList<>();
    private volatile List<BoundingBox> mapBounds = List.of();

    public MapService() {
        super(List.of());
    }

    @Override
    public void registerDownloads(DownloadRegistry registry) {
        registry.registerDownload(UrlId.DATA_STATIC_MAPS).handleReader(this::handleMaps);
    }

    public List<MapTexture> getMapsForBoundingBox(BoundingBox box) {
        return maps.stream()
                .filter(map -> BoundingShape.intersects(box, map.getBox()))
                .toList();
    }

    public List<MapTexture> getMapsForBoundingCircle(BoundingCircle circle) {
        return maps.stream()
                .filter(map -> BoundingShape.intersects(map.getBox(), circle))
                .toList();
    }

    public boolean hasMapData() {
        return !mapBounds.isEmpty();
    }

    /** Uses the same map bounds as the minimap, without depending on its zoom or visibility settings. */
    public boolean isInMappedArea(Position position) {
        return mapBounds.stream().anyMatch(box -> box.contains((float) position.x(), (float) position.z()));
    }

    public boolean isPlayerInMappedArea(float width, float height, float scale) {
        BoundingCircle textureBoundingCircle = BoundingCircle.enclosingCircle(BoundingBox.centered(
                (float) McUtils.player().getX(), (float) McUtils.player().getZ(), width * scale, height * scale));

        return !getMapsForBoundingCircle(textureBoundingCircle).isEmpty();
    }

    private void handleMaps(Reader reader) {
        Type type = new TypeToken<List<MapPartProfile>>() {}.getType();

        List<MapPartProfile> mapPartList = WynntilsMod.GSON.fromJson(reader, type);
        // Publish all bounds together before asynchronous texture downloads begin. Location checks
        // must not mistake a map part whose image is still downloading for an unmapped area.
        mapBounds = mapPartList.stream()
                .map(part -> new BoundingBox(part.x1, part.z1, part.x2, part.z2))
                .toList();
        List<MapTexture> newMaps = new CopyOnWriteArrayList<>();
        for (MapPartProfile mapPart : mapPartList) {
            String fileName = mapPart.md5 + ".png";

            loadMapPart(mapPart, fileName, newMaps);
        }

        maps = newMaps;
    }

    private void loadMapPart(MapPartProfile mapPart, String fileName, List<MapTexture> newMaps) {
        Download dl = Managers.Net.download(
                URI.create(Managers.Url.getDownloadSourceUrl() + mapPart.path), "maps/" + fileName, mapPart.md5);
        dl.handleInputStream(
                inputStream -> {
                    try {
                        NativeImage nativeImage = NativeImage.read(inputStream);
                        MapTexture mapPartImage =
                                new MapTexture(fileName, nativeImage, mapPart.x1, mapPart.z1, mapPart.x2, mapPart.z2);
                        newMaps.add(mapPartImage);
                    } catch (IOException e) {
                        WynntilsMod.warn("IOException occurred while loading map image of " + mapPart.name, e);
                    }
                },
                onError -> WynntilsMod.warn("Error occurred while downloading map image of " + mapPart.name, onError));
    }

    private record MapPartProfile(String name, String url, String path, int x1, int z1, int x2, int z2, String md5) {}
}
