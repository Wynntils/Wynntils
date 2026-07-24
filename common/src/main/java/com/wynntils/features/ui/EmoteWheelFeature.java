/*
 * Copyright © Wynntils 2026.
 * This file is released under LGPLv3. See LICENSE for full license details.
 */
package com.wynntils.features.ui;

import com.wynntils.core.consumers.features.ExternalConfigurationScreen;
import com.wynntils.core.consumers.features.Feature;
import com.wynntils.core.consumers.features.ProfileDefault;
import com.wynntils.core.consumers.features.properties.RegisterKeyBind;
import com.wynntils.core.keybinds.KeyBind;
import com.wynntils.core.keybinds.KeyBindDefinition;
import com.wynntils.core.persisted.Persisted;
import com.wynntils.core.persisted.config.Category;
import com.wynntils.core.persisted.config.Config;
import com.wynntils.core.persisted.config.ConfigCategory;
import com.wynntils.screens.emotewheel.EmoteWheelConfigScreen;
import com.wynntils.screens.emotewheel.EmoteWheelScreen;
import com.wynntils.utils.colors.CommonColors;
import com.wynntils.utils.colors.CustomColor;
import com.wynntils.utils.mc.McUtils;
import com.wynntils.utils.render.type.EmoteWheelButton;
import com.wynntils.utils.render.type.TextShadow;
import net.minecraft.client.gui.screens.Screen;

@ConfigCategory(Category.UI)
public class EmoteWheelFeature extends Feature implements ExternalConfigurationScreen {
    public static final int MAX_EMOTES = 10;

    @RegisterKeyBind
    public final KeyBind openEmoteWheelKeybind = KeyBindDefinition.OPEN_EMOTE_WHEEL.create(this::openEmoteWheel);

    @Persisted
    public final Config<Integer> numberOfButtons = new Config<>(8);

    @Persisted
    public final Config<Boolean> showNumbers = new Config<>(true);

    @Persisted
    public final Config<Double> scale = new Config<>(1.0);

    @Persisted
    public final Config<EmoteWheelButton> buttonStyle = new Config<>(EmoteWheelButton.BUTTON);

    @Persisted
    public final Config<CustomColor> textColor = new Config<>(CommonColors.WHITE);

    @Persisted
    public final Config<CustomColor> textColorHovered = new Config<>(CommonColors.WHITE);

    @Persisted
    public final Config<TextShadow> textShadow = new Config<>(TextShadow.OUTLINE);

    @Persisted
    public final Config<CustomColor> backgroundColor = new Config<>(CustomColor.fromHexString("#2D2D2DEE"));

    @Persisted
    public final Config<CustomColor> backgroundColorHovered = new Config<>(CustomColor.fromHexString("#4C8D2CEE"));

    @Persisted
    public final Config<Integer> buttonRadius = new Config<>(0);

    public EmoteWheelFeature() {
        super(ProfileDefault.onlyDefault());
    }

    private void openEmoteWheel() {
        if (McUtils.screen() == null) McUtils.setScreen(EmoteWheelScreen.create(this));
    }

    @Override
    public Screen getExternalConfigurationScreen(Screen previousScreen) {
        return EmoteWheelConfigScreen.create(previousScreen, this);
    }
}
