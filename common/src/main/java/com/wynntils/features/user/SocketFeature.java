/*
 * Copyright © Wynntils 2022.
 * This file is released under AGPLv3. See LICENSE for full license details.
 */
package com.wynntils.features.user;

import com.wynntils.core.config.Config;
import com.wynntils.core.config.ConfigHolder;
import com.wynntils.core.features.UserFeature;
import com.wynntils.core.managers.Model;
import com.wynntils.hades.protocol.enums.SocialType;
import com.wynntils.sockets.model.HadesUserModel;
import com.wynntils.sockets.model.SocketModel;
import com.wynntils.wynn.model.ActionBarModel;
import com.wynntils.wynn.model.PlayerRelationsModel;
import java.util.List;

public class SocketFeature extends UserFeature {
    public static SocketFeature INSTANCE;

    @Config
    public boolean getOtherPlayerInfo = true;

    @Config
    public boolean shareWithParty = true;

    @Config
    public boolean shareWithFriends = true;

    @Config
    public boolean shareWithGuild = true;

    @Override
    public List<Class<? extends Model>> getModelDependencies() {
        // SocketModel needs ActionBarModel for updating player info and HadesUserModel
        return List.of(PlayerRelationsModel.class, SocketModel.class, HadesUserModel.class, ActionBarModel.class);
    }

    @Override
    protected void onConfigUpdate(ConfigHolder configHolder) {
        switch (configHolder.getFieldName()) {
            case "getOtherPlayerInfo" -> HadesUserModel.getHadesUserMap().clear();
            case "shareWithParty" -> {
                if (shareWithParty) {
                    PlayerRelationsModel.requestPartyListUpdate();
                } else {
                    SocketModel.resetSocialType(SocialType.PARTY);
                }
            }
            case "shareWithFriends" -> {
                if (shareWithFriends) {
                    PlayerRelationsModel.requestFriendListUpdate();
                } else {
                    SocketModel.resetSocialType(SocialType.FRIEND);
                }
            }
            case "shareWithGuild" -> {
                // TODO
            }
        }
    }
}
