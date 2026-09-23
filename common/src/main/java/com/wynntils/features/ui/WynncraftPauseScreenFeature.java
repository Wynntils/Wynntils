/*
 * Copyright © Wynntils 2022-2026.
 * This file is released under LGPLv3. See LICENSE for full license details.
 */
package com.wynntils.features.ui;

import com.wynntils.core.components.Handlers;
import com.wynntils.core.components.Models;
import com.wynntils.core.consumers.features.Feature;
import com.wynntils.core.consumers.features.ProfileDefault;
import com.wynntils.core.persisted.config.Category;
import com.wynntils.core.persisted.config.ConfigCategory;
import com.wynntils.core.persisted.config.ConfigProfile;
import com.wynntils.mc.event.PauseMenuButtonReplaceEvent;
import com.wynntils.models.worlds.type.WorldState;
import com.wynntils.screens.base.WynntilsMenuScreenBase;
import com.wynntils.screens.maps.GuildMapScreen;
import com.wynntils.screens.wynntilsmenu.WynntilsMenuScreen;
import com.wynntils.utils.mc.McUtils;
import net.minecraft.ChatFormatting;
import net.minecraft.client.gui.components.Button;
import net.minecraft.client.gui.components.SpriteIconButton;
import net.minecraft.network.chat.Component;
import net.minecraft.resources.Identifier;
import net.neoforged.bus.api.SubscribeEvent;

@ConfigCategory(Category.UI)
public class WynncraftPauseScreenFeature extends Feature {
    public WynncraftPauseScreenFeature() {
        // We don't use ProfileDefault.ENABLED for this as it is one method of accessing the Wynntils menu that
        // has minimal impact on gameplay so we want it enabled for BLANK_SLATE
        super(new ProfileDefault.Builder()
                .enabledFor(
                        ConfigProfile.DEFAULT,
                        ConfigProfile.NEW_PLAYER,
                        ConfigProfile.LITE,
                        ConfigProfile.MINIMAL,
                        ConfigProfile.BLANK_SLATE)
                .build());
    }

    @SubscribeEvent
    public void onReplaceTextButton(PauseMenuButtonReplaceEvent.Text event) {
        if (!(Models.WorldState.getCurrentState() == WorldState.CHARACTER_SELECTION
                || Models.WorldState.getCurrentState() == WorldState.WORLD)) return;

        switch (event.getButtonType()) {
            case ADVANCEMENTS -> event.setButton(createTerritoryButton(event.getButton()));
            case STATS -> event.setButton(createWynntilsButton(event.getButton()));
        }
    }

    @SubscribeEvent
    public void onReplaceSpriteIconButton(PauseMenuButtonReplaceEvent.SpriteIcon event) {
        if (!(Models.WorldState.getCurrentState() == WorldState.CHARACTER_SELECTION
                || Models.WorldState.getCurrentState() == WorldState.WORLD)) return;

        switch (event.getButtonType()) {
            case REPORT_BUGS -> event.setButton(createCharacterSelectionButton());
            case GIVE_FEEDBACK -> event.setButton(createHubButton());
        }
    }

    private Button createTerritoryButton(Button oldButton) {
        return new Button.Builder(
                        Component.translatable("feature.wynntils.wynncraftPauseScreen.territoryMap.name")
                                .withStyle(ChatFormatting.DARK_AQUA),
                        (button) -> McUtils.setScreen(GuildMapScreen.create()))
                .pos(oldButton.getX(), oldButton.getY())
                .size(oldButton.getWidth(), oldButton.getHeight())
                .build();
    }

    private Button createWynntilsButton(Button oldButton) {
        return new Button.Builder(
                        Component.translatable("feature.wynntils.wynncraftPauseScreen.wynntilsMenuButton.name"),
                        (button) -> WynntilsMenuScreenBase.openBook(WynntilsMenuScreen.create()))
                .pos(oldButton.getX(), oldButton.getY())
                .size(oldButton.getWidth(), oldButton.getHeight())
                .build();
    }

    private SpriteIconButton createCharacterSelectionButton() {
        return SpriteIconButton.builder(
                        Component.translatable("feature.wynntils.wynncraftPauseScreen.characterSelectionButton.name"),
                        (button) -> {
                            McUtils.setScreen(null);
                            McUtils.mc().mouseHandler.grabMouse();
                            Handlers.Command.sendCommandImmediately("characters");
                        },
                        true)
                .width(20)
                .sprite(Identifier.fromNamespaceAndPath("wynntils", "player"), 16, 16)
                .withTootip()
                .build();
    }

    private SpriteIconButton createHubButton() {
        return SpriteIconButton.builder(
                        Component.translatable("feature.wynntils.wynncraftPauseScreen.hubButton.name"),
                        (button) -> {
                            McUtils.setScreen(null);
                            McUtils.mc().mouseHandler.grabMouse();
                            Handlers.Command.sendCommandImmediately("hub");
                        },
                        true)
                .width(20)
                .sprite(Identifier.fromNamespaceAndPath("wynntils", "hub"), 16, 16)
                .withTootip()
                .build();
    }
}
