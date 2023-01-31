/*
 * Copyright © Wynntils 2023.
 * This file is released under AGPLv3. See LICENSE for full license details.
 */
package com.wynntils.features.user;

import com.wynntils.core.features.UserFeature;
import com.wynntils.core.features.properties.FeatureInfo;
import com.wynntils.core.features.properties.RegisterKeyBind;
import com.wynntils.core.keybinds.KeyBind;
import com.wynntils.screens.partymanagement.PartyManagementScreen;
import com.wynntils.utils.mc.McUtils;
import org.lwjgl.glfw.GLFW;

@FeatureInfo(stability = FeatureInfo.Stability.STABLE)
public class PartyManagementScreenFeature extends UserFeature {
    @RegisterKeyBind
    private final KeyBind openPartyManagementScreen =
            new KeyBind("Open Party Management Screen", GLFW.GLFW_KEY_RIGHT_BRACKET, true, () -> McUtils.mc()
                    .setScreen(PartyManagementScreen.create()));
}
