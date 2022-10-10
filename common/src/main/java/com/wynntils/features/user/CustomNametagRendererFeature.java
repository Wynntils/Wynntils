/*
 * Copyright © Wynntils 2022.
 * This file is released under AGPLv3. See LICENSE for full license details.
 */
package com.wynntils.features.user;

import com.wynntils.core.config.Config;
import com.wynntils.core.features.UserFeature;
import com.wynntils.core.managers.Model;
import com.wynntils.mc.event.NametagRenderEvent;
import com.wynntils.wynn.model.UserInfoModel;
import com.wynntils.wynn.objects.account.AccountType;
import java.util.List;
import net.minecraftforge.eventbus.api.SubscribeEvent;

public class CustomNametagRendererFeature extends UserFeature {
    @Config
    public boolean hideAllNametags = false;

    @Config
    public boolean showAccountType = true;

    @SubscribeEvent
    public void onNameTagRender(NametagRenderEvent event) {
        if (hideAllNametags) {
            event.setCanceled(true);
            return;
        }

        if (showAccountType) {
            for (AccountType type : AccountType.values()) {
                if (type.getComponent() == null) continue;

                if (UserInfoModel.isAccountType(event.getEntity().getUUID(), type)) {
                    event.addInjectedLine(type.getComponent());
                }
            }
        }
    }

    @Override
    public List<Class<? extends Model>> getModelDependencies() {
        return List.of(UserInfoModel.class);
    }
}
