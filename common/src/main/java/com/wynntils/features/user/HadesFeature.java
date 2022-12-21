/*
 * Copyright © Wynntils 2022.
 * This file is released under AGPLv3. See LICENSE for full license details.
 */
package com.wynntils.features.user;

import com.wynntils.core.config.Config;
import com.wynntils.core.config.ConfigHolder;
import com.wynntils.core.features.UserFeature;
import com.wynntils.core.managers.Model;
import com.wynntils.core.managers.Models;
import com.wynntils.hades.protocol.enums.SocialType;
import java.util.List;

public class HadesFeature extends UserFeature {
    public static HadesFeature INSTANCE;

    @Config
    public boolean getOtherPlayerInfo = true;

    @Config
    public boolean shareWithParty = true;

    @Config
    public boolean shareWithFriends = true;

    @Config
    public boolean shareWithGuild = true;

    @Override
    public List<Model> getModelDependencies() {
        // We need:
        //      HadesModel to communicate with Hades server
        //      HadesUserModel for storing remote HadesUser info
        //      PlayerRelationsModel to parse player relations
        // Inter-model dependencies, that cannot be tracked otherwise:
        //      HadesModel needs ActionBarModel for updating player info
        return List.of(Models.Hades, Models.PlayerRelations, Models.HadesUser, Models.ActionBar);
    }

    @Override
    protected void onConfigUpdate(ConfigHolder configHolder) {
        switch (configHolder.getFieldName()) {
            case "getOtherPlayerInfo" -> {
                if (getOtherPlayerInfo) {
                    Models.Hades.tryResendWorldData();
                } else {
                    Models.HadesUser.getHadesUserMap().clear();
                }
            }
            case "shareWithParty" -> {
                if (shareWithParty) {
                    Models.PlayerRelations.requestPartyListUpdate();
                } else {
                    Models.Hades.resetSocialType(SocialType.PARTY);
                }
            }
            case "shareWithFriends" -> {
                if (shareWithFriends) {
                    Models.PlayerRelations.requestFriendListUpdate();
                } else {
                    Models.Hades.resetSocialType(SocialType.FRIEND);
                }
            }
            case "shareWithGuild" -> {
                // TODO
            }
        }
    }
}
