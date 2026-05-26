/*
 * Copyright © Wynntils 2026.
 * This file is released under LGPLv3. See LICENSE for full license details.
 */
package com.wynntils.services.remoteoverlay;

import com.wynntils.core.WynntilsMod;
import com.wynntils.core.components.Managers;
import com.wynntils.core.components.Service;
import com.wynntils.core.json.JsonManager;
import com.wynntils.core.persisted.Persisted;
import com.wynntils.core.persisted.storage.Storage;
import com.wynntils.services.remoteoverlay.type.RemoteOverlayInfo;
import com.wynntils.services.remoteoverlay.type.RemoteOverlayProvider;
import java.util.ArrayList;
import java.util.List;
import java.util.Map;
import java.util.Optional;
import java.util.concurrent.ConcurrentHashMap;

public class RemoteOverlayService extends Service {
    @Persisted
    private final Storage<List<RemoteOverlayProvider>> remoteOverlayProviders = new Storage<>(new ArrayList<>());

    public final Map<RemoteOverlayProvider, RemoteOverlayInfo> providedRemoteOverlays = new ConcurrentHashMap<>();

    public RemoteOverlayService() {
        super(List.of());
    }

    @Override
    public void onStorageLoad(Storage<?> storage) {
        loadRemoteOverlayProviders();
    }

    public void loadRemoteOverlayProviders() {
        for (RemoteOverlayProvider provider : remoteOverlayProviders.get()) {
            try {
                Managers.Net.download(provider.getUrl(), "remoteoverlay/" + provider.getName())
                        .handleJsonObject(jsonObject -> {
                            RemoteOverlayInfo overlay = JsonManager.GSON.fromJson(jsonObject, RemoteOverlayInfo.class);

                            providedRemoteOverlays.put(provider, overlay);
                        });
            } catch (IllegalArgumentException e) {
                WynntilsMod.warn("Failed to load RemoteOverlayInfo from " + provider.getUrl() + ": " + e.getMessage());
            }
        }
    }

    public void addRemoteOverlayProvider(RemoteOverlayProvider overlayProvider) {
        remoteOverlayProviders.get().add(overlayProvider);
        remoteOverlayProviders.touched();
        loadRemoteOverlayProviders();
    }

    public boolean removeRemoteOverlayProvider(String name) {
        Optional<RemoteOverlayProvider> provider = remoteOverlayProviders.get().stream()
                .filter(overlayProvider -> overlayProvider.getName().equals(name))
                .findFirst();

        if (provider.isEmpty()) return false;

        remoteOverlayProviders.get().remove(provider.get());
        remoteOverlayProviders.touched();
        providedRemoteOverlays.remove(provider.get());

        return true;
    }

    public List<RemoteOverlayProvider> getRemoteOverlayProviders() {
        return remoteOverlayProviders.get();
    }
}
