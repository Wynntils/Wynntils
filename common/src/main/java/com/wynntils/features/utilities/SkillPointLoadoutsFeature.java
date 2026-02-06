/*
 * Copyright © Wynntils 2023-2025.
 * This file is released under LGPLv3. See LICENSE for full license details.
 */
package com.wynntils.features.utilities;

import com.wynntils.core.components.Models;
import com.wynntils.core.consumers.features.Feature;
import com.wynntils.core.consumers.features.ProfileDefault;
import com.wynntils.core.persisted.config.Category;
import com.wynntils.core.persisted.config.ConfigCategory;
import com.wynntils.core.persisted.config.ConfigProfile;
import com.wynntils.mc.event.ScreenOpenedEvent;
import com.wynntils.models.containers.containers.CharacterInfoContainer;
import com.wynntils.screens.base.widgets.WynntilsButton;
import com.wynntils.screens.skillpointloadouts.SkillPointLoadoutsScreen;
import com.wynntils.utils.mc.McUtils;
import net.minecraft.client.gui.screens.inventory.ContainerScreen;
import net.minecraft.network.chat.Component;
import net.neoforged.bus.api.SubscribeEvent;

@ConfigCategory(Category.UTILITIES)
public class SkillPointLoadoutsFeature extends Feature {
    public SkillPointLoadoutsFeature() {
        super(new ProfileDefault.Builder()
                .disableFor(ConfigProfile.NEW_PLAYER, ConfigProfile.BLANK_SLATE)
                .build());
    }

    @SubscribeEvent
    public void onCharacterInfoScreenOpened(ScreenOpenedEvent.Post e) {
        if (!(e.getScreen() instanceof ContainerScreen screen)) return;
        if (!(Models.Container.getCurrentContainer() instanceof CharacterInfoContainer)) return;

        screen.addRenderableWidget(
                new LoadoutScreenButton(screen.width / 2 - LoadoutScreenButton.BUTTON_WIDTH / 2, screen.topPos - 24));
    }

    private static final class LoadoutScreenButton extends WynntilsButton {
        private static final int BUTTON_WIDTH = 150;
        private static final int BUTTON_HEIGHT = 20;

        private LoadoutScreenButton(int x, int y) {
            super(
                    x,
                    y,
                    BUTTON_WIDTH,
                    BUTTON_HEIGHT,
                    Component.translatable("feature.wynntils.skillPointLoadouts.button"));
        }

        @Override
        public void onPress() {
            McUtils.setScreen(SkillPointLoadoutsScreen.create());
        }
    }
}
