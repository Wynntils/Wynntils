/*
 * Copyright © Wynntils 2022.
 * This file is released under AGPLv3. See LICENSE for full license details.
 */
package com.wynntils.sockets.model;

import com.wynntils.core.managers.Model;
import com.wynntils.sockets.objects.HadesUser;
import java.util.HashMap;
import java.util.Map;
import java.util.Optional;
import java.util.UUID;
import java.util.concurrent.ConcurrentHashMap;

public final class HadesUserModel extends Model {
    private Map<UUID, HadesUser> hadesUserMap = new ConcurrentHashMap<>();

    public void init() {
        hadesUserMap = new HashMap<>();
    }

    public void disable() {
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
