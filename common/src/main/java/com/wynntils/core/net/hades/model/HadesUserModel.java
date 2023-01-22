/*
 * Copyright © Wynntils 2022.
 * This file is released under AGPLv3. See LICENSE for full license details.
 */
package com.wynntils.core.net.hades.model;

import com.wynntils.core.components.Model;
import com.wynntils.core.net.hades.objects.HadesUser;
import com.wynntils.models.worlds.event.WorldStateEvent;
import java.util.HashMap;
import java.util.Map;
import java.util.Optional;
import java.util.UUID;
import java.util.concurrent.ConcurrentHashMap;
import net.minecraftforge.eventbus.api.SubscribeEvent;

public final class HadesUserModel extends Model {
    private Map<UUID, HadesUser> hadesUserMap = new ConcurrentHashMap<>();

    public HadesUserModel() {
        hadesUserMap = new HashMap<>();
    }

    @SubscribeEvent
    public void onWorldStateChange(WorldStateEvent event) {
        hadesUserMap.clear();
    }

    public Map<UUID, HadesUser> getHadesUserMap() {
        return hadesUserMap;
    }

    public Optional<HadesUser> getUser(UUID uuid) {
        return Optional.ofNullable(hadesUserMap.get(uuid));
    }

    public void putUser(UUID uuid, HadesUser hadesUser) {
        hadesUserMap.put(uuid, hadesUser);
    }

    public void removeUser(UUID uuid) {
        hadesUserMap.remove(uuid);
    }
}
