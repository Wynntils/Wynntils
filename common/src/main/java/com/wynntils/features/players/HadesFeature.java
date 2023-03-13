/*
 * Copyright © Wynntils 2022.
 * This file is released under AGPLv3. See LICENSE for full license details.
 */
package com.wynntils.features.players;

import com.wynntils.core.components.Models;
import com.wynntils.core.config.Category;
import com.wynntils.core.config.ConfigCategory;
import com.wynntils.core.config.ConfigHolder;
import com.wynntils.core.config.ConfigInfo;
import com.wynntils.core.features.UserFeature;
import com.wynntils.hades.protocol.enums.SocialType;

@ConfigCategory(Category.PLAYERS)
public class HadesFeature extends UserFeature {
    public static HadesFeature INSTANCE;

    @ConfigInfo
    public boolean getOtherPlayerInfo = true;

    @ConfigInfo
    public boolean shareWithParty = true;

    @ConfigInfo
    public boolean shareWithFriends = true;

    @ConfigInfo
    public boolean shareWithGuild = true;

    @Override
    protected void onConfigUpdate(ConfigHolder configHolder) {
        switch (configHolder.getFieldName()) {
            case "getOtherPlayerInfo" -> {
                if (getOtherPlayerInfo) {
                    Models.Hades.tryResendWorldData();
                } else {
                    Models.Hades.resetHadesUsers();
                }
            }
            case "shareWithParty" -> {
                if (shareWithParty) {
                    Models.Party.requestData();
                } else {
                    Models.Hades.resetSocialType(SocialType.PARTY);
                }
            }
            case "shareWithFriends" -> {
                if (shareWithFriends) {
                    Models.Friends.requestData();
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
