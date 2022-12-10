/*
 * Copyright © Wynntils 2022.
 * This file is released under AGPLv3. See LICENSE for full license details.
 */
package com.wynntils.features.user;

import com.wynntils.core.features.UserFeature;
import com.wynntils.core.managers.Model;
import com.wynntils.wynn.model.ServerListModel;
import com.wynntils.wynn.model.item.ServerItemStackModel;
import java.util.List;

public class LobbyUptimeFeature extends UserFeature {
    @Override
    public List<Class<? extends Model>> getModelDependencies() {
        return List.of(ServerListModel.class, ServerItemStackModel.class);
    }
}
