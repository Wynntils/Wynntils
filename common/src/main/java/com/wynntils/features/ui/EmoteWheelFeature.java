/*
 * Copyright © Wynntils 2023-2026.
 * This file is released under LGPLv3. See LICENSE for full license details.
 */
package com.wynntils.features.ui;

import com.wynntils.core.components.Models;
import com.wynntils.core.components.Services;
import com.wynntils.core.consumers.features.Feature;
import com.wynntils.core.consumers.features.ProfileDefault;
import com.wynntils.core.consumers.features.properties.RegisterKeyBind;
import com.wynntils.core.keybinds.KeyBind;
import com.wynntils.core.keybinds.KeyBindDefinition;
import com.wynntils.core.persisted.Persisted;
import com.wynntils.core.persisted.config.Category;
import com.wynntils.core.persisted.config.Config;
import com.wynntils.core.persisted.config.ConfigCategory;
import com.wynntils.core.persisted.config.HiddenConfig;
import com.wynntils.models.items.items.gui.EmoteItem;
import com.wynntils.screens.emotewheel.EmoteWheelScreen;
import com.wynntils.utils.colors.CustomColor;
import com.wynntils.utils.mc.McUtils;
import java.util.Arrays;
import java.util.List;
import java.util.Optional;
import net.minecraft.world.inventory.Slot;

@ConfigCategory(Category.UI)
public class EmoteWheelFeature extends Feature {
    public static final int MAX_EMOTES = 10;

    @RegisterKeyBind
    public final KeyBind openEmoteWheelKeybind =
            KeyBindDefinition.OPEN_EMOTE_WHEEL.create(this::openEmoteWheel, this::tryFavoritingEmoteOnHoveredSlot);

    @Persisted
    private final Config<Integer> numberOfButtons = new Config<>(8);

    @Persisted
    public final Config<Boolean> showNumbers = new Config<>(true);

    @Persisted
    private final Config<Double> scale = new Config<>(1.0);

    @Persisted
    private final Config<CustomColor> backgroundColor = new Config<>(CustomColor.fromHexString("#000000BB"));

    @Persisted
    private final Config<CustomColor> backgroundColorHovered = new Config<>(CustomColor.fromHexString("#666666BB"));

    @Persisted
    private final Config<Integer> buttonRadius = new Config<>(5);

    @Persisted
    public final HiddenConfig<List<String>> favoritedEmotes;

    public EmoteWheelFeature() {
        super(ProfileDefault.onlyDefault());
        favoritedEmotes = new HiddenConfig<>(Arrays.asList(new String[MAX_EMOTES]));
    }

    private void openEmoteWheel() {
        if (openEmoteWheelKeybind.isPressed()) return;
        if (McUtils.screen() != null && !(McUtils.screen() instanceof EmoteWheelScreen)) return;

        McUtils.setScreen(EmoteWheelScreen.create(
                backgroundColor.get(),
                backgroundColorHovered.get(),
                buttonRadius.get(),
                numberOfButtons.get(),
                scale.get()));
    }

    private void tryFavoritingEmoteOnHoveredSlot(Slot hoveredSlot) {
        if (hoveredSlot == null || !hoveredSlot.hasItem()) return;

        Optional<EmoteItem> possibleEmoteItem = Models.Item.asWynnItem(hoveredSlot.getItem(), EmoteItem.class);
        if (possibleEmoteItem.isEmpty() || possibleEmoteItem.get() == null) return;

        Services.FavoritedEmotes.toggleFavorite(possibleEmoteItem.get());
    }
}
