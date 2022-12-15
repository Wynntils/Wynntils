/*
 * Copyright © Wynntils 2022.
 * This file is released under AGPLv3. See LICENSE for full license details.
 */
package com.wynntils.features.user;

import com.wynntils.core.features.UserFeature;
import com.wynntils.core.managers.Model;
import com.wynntils.core.managers.Models;
import java.util.List;

public class LobbyUptimeFeature extends UserFeature {
    @Override
    public List<Model> getModelDependencies() {
        return List.of(Models.ServerList, Models.ServerItemStack);
    }
}
