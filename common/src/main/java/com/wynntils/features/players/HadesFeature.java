/*
 * Copyright © Wynntils 2022-2026.
 * This file is released under LGPLv3. See LICENSE for full license details.
 */
package com.wynntils.features.players;

import com.wynntils.core.components.Models;
import com.wynntils.core.components.Services;
import com.wynntils.core.consumers.features.ExternalConfigurationScreen;
import com.wynntils.core.consumers.features.Feature;
import com.wynntils.core.consumers.features.ProfileDefault;
import com.wynntils.core.consumers.features.properties.RegisterKeyBind;
import com.wynntils.core.consumers.features.properties.RegisterSubFeature;
import com.wynntils.core.keybinds.KeyBind;
import com.wynntils.core.keybinds.KeyBindDefinition;
import com.wynntils.core.persisted.Persisted;
import com.wynntils.core.persisted.config.Category;
import com.wynntils.core.persisted.config.Config;
import com.wynntils.core.persisted.config.ConfigCategory;
import com.wynntils.core.persisted.config.ConfigProfile;
import com.wynntils.hades.protocol.enums.SocialType;
import com.wynntils.screens.hadesinteraction.HadesInteractionWheelScreen;
import com.wynntils.screens.playerviewer.GearSharingSettingsScreen;
import com.wynntils.services.hades.HadesUser;
import com.wynntils.utils.mc.McUtils;
import java.util.ArrayList;
import java.util.List;
import net.minecraft.client.gui.screens.Screen;

@ConfigCategory(Category.PLAYERS)
public class HadesFeature extends Feature implements ExternalConfigurationScreen {
    @Persisted
    public final Config<Boolean> getOtherPlayerInfo = new Config<>(true);

    @Persisted
    public final Config<Boolean> shareWithParty = new Config<>(true);

    @Persisted
    public final Config<Boolean> shareWithFriends = new Config<>(true);

    @Persisted
    public final Config<Boolean> shareWithGuild = new Config<>(true);

    @RegisterSubFeature
    private final PlayerViewerFeature playerViewer = new PlayerViewerFeature();

    @RegisterSubFeature
    private final PlayerPingFeature playerPing = new PlayerPingFeature();

    @RegisterKeyBind
    public final KeyBind openInteractionWheelKeybind =
            KeyBindDefinition.HADES_INTERACTION_WHEEL.create(this::openInteractionWheel);

    public HadesFeature() {
        super(
                new ProfileDefault.Builder()
                        .enabledFor(ConfigProfile.DEFAULT, ConfigProfile.NEW_PLAYER, ConfigProfile.LITE)
                        .build(),
                List.of(
                        ConfigDependency.functionality(Models.Friends.queryFriendsList),
                        ConfigDependency.functionality(Models.Party.queryPartyMembers),
                        ConfigDependency.functionality(Models.Guild.requestGuildMembers),
                        ConfigDependency.functionality(Services.Hades.connectToHades)));
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

    @Override
    public Screen getExternalConfigurationScreen(Screen previousScreen) {
        return GearSharingSettingsScreen.create(previousScreen);
    }

    public void tryOpenPlayerViewer() {
        playerViewer.tryOpenPlayerViewer();
    }

    private void openInteractionWheel() {
        List<HadesUser> hadesUsers = Services.Hades.getHadesUsers().toList();
        List<String> partyMembers = Models.Party.getPartyMembers();

        List<HadesUser> hadesUsingPartyMembers = new ArrayList<>(hadesUsers.stream()
                .filter(hadesUser -> partyMembers.contains(hadesUser.getName()))
                .toList());

        List<HadesUser> warUsers = Models.War.getHadesUsers();

        for (HadesUser warUser : warUsers) {
            if (hadesUsingPartyMembers.stream()
                    .noneMatch(partyUser -> partyUser.getUuid().equals(warUser.getUuid()))) {
                hadesUsingPartyMembers.add(warUser);
            }
        }

        if (!hadesUsingPartyMembers.isEmpty()) {
            if (McUtils.screen() == null) {
                McUtils.setScreen(HadesInteractionWheelScreen.create(this, playerPing));
            }
        } else {
            tryOpenPlayerViewer();
        }
    }
}
