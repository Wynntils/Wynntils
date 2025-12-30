/*
 * Copyright © Wynntils 2022-2025.
 * This file is released under LGPLv3. See LICENSE for full license details.
 */
package com.wynntils.features.players;

import com.wynntils.core.components.Models;
import com.wynntils.core.components.Services;
import com.wynntils.core.consumers.features.Feature;
import com.wynntils.core.consumers.features.ProfileDefault;
import com.wynntils.core.persisted.Persisted;
import com.wynntils.core.persisted.config.Category;
import com.wynntils.core.persisted.config.Config;
import com.wynntils.core.persisted.config.ConfigCategory;
import com.wynntils.core.persisted.config.ConfigProfile;
import com.wynntils.hades.protocol.enums.SocialType;

@ConfigCategory(Category.PLAYERS)
public class HadesFeature extends Feature {
    @Persisted
    public final Config<Boolean> getOtherPlayerInfo = new Config<>(true);

    @Persisted
    public final Config<Boolean> shareWithParty = new Config<>(true);

    @Persisted
    public final Config<Boolean> shareWithFriends = new Config<>(true);

    @Persisted
    public final Config<Boolean> shareWithGuild = new Config<>(true);

    public HadesFeature() {
        super(new ProfileDefault.Builder()
                .disableFor(ConfigProfile.MINIMAL, ConfigProfile.BLANK_SLATE)
                .build());
    }

    @Override
    protected void onConfigUpdate(Config<?> config) {
        switch (config.getFieldName()) {
            case "getOtherPlayerInfo" -> {
                if (getOtherPlayerInfo.get()) {
                    Services.Hades.tryResendWorldData();
                } else {
                    Services.Hades.resetHadesUsers();
                }
            }
            case "shareWithParty" -> {
                if (shareWithParty.get()) {
                    Models.Party.requestData();
                } else {
                    Services.Hades.resetSocialType(SocialType.PARTY);
                }
            }
            case "shareWithFriends" -> {
                if (shareWithFriends.get()) {
                    Models.Friends.requestData();
                } else {
                    Services.Hades.resetSocialType(SocialType.FRIEND);
                }
            }
            case "shareWithGuild" -> {
                if (shareWithGuild.get()) {
                    Models.Guild.requestGuildMembers();
                } else {
                    Services.Hades.resetSocialType(SocialType.GUILD);
                }
            }
        }
    }
}
