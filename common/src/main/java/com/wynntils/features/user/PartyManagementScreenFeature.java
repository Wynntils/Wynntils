package com.wynntils.features.user;

import com.wynntils.core.config.ConfigHolder;
import com.wynntils.core.features.UserFeature;
import com.wynntils.core.features.properties.FeatureInfo;
import com.wynntils.core.features.properties.RegisterKeyBind;
import com.wynntils.core.keybinds.KeyBind;
import com.wynntils.gui.screens.ChatTabEditingScreen;
import com.wynntils.gui.screens.PartyManagementScreen;
import com.wynntils.mc.utils.McUtils;
import org.lwjgl.glfw.GLFW;

@FeatureInfo(stability = FeatureInfo.Stability.STABLE)
public class PartyManagementScreenFeature extends UserFeature {
    @RegisterKeyBind
    private final KeyBind openPartyManagementScreen = new KeyBind("Open Party Management Screen", GLFW.GLFW_KEY_RIGHT_BRACKET, true, () -> McUtils.mc().setScreen(PartyManagementScreen.create()));


}
