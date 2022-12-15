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
    private static Map<UUID, HadesUser> hadesUserMap = new ConcurrentHashMap<>();

    public static void init() {
        hadesUserMap = new HashMap<>();
    }

    public static void disable() {
        hadesUserMap.clear();
    }

    public static Map<UUID, HadesUser> getHadesUserMap() {
        return hadesUserMap;
    }

    public static Optional<HadesUser> getUser(UUID uuid) {
        return Optional.ofNullable(hadesUserMap.get(uuid));
    }

    public static void putUser(UUID uuid, HadesUser hadesUser) {
        hadesUserMap.put(uuid, hadesUser);
    }

    public static void removeUser(UUID uuid) {
        hadesUserMap.remove(uuid);
    }
}
