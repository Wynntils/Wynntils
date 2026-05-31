/*
 * Copyright © Wynntils 2026.
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
import com.wynntils.utils.colors.CommonColors;
import com.wynntils.utils.colors.CustomColor;
import com.wynntils.utils.mc.McUtils;
import com.wynntils.utils.render.type.EmoteWheelButton;
import com.wynntils.utils.render.type.TextShadow;
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
    public final Config<TextShadow> textShadow = new Config<>(TextShadow.NORMAL);

    @Persisted
    public final Config<CustomColor> backgroundColor = new Config<>(CustomColor.fromHexString("#000000BB"));

    @Persisted
    public final Config<CustomColor> backgroundColorHovered = new Config<>(CustomColor.fromHexString("#4C8D2CBB"));

    @Persisted
    public final Config<Integer> buttonRadius = new Config<>(5);

    @Persisted
    public final HiddenConfig<List<String>> favoritedEmotes;

    public EmoteWheelFeature() {
        super(ProfileDefault.onlyDefault());
        favoritedEmotes = new HiddenConfig<>(Arrays.asList(new String[MAX_EMOTES]));
    }

    private void openEmoteWheel() {
        if (openEmoteWheelKeybind.isPressed()) return;
        if (McUtils.screen() != null && !(McUtils.screen() instanceof EmoteWheelScreen)) return;

        McUtils.setScreen(EmoteWheelScreen.create(numberOfButtons.get(), scale.get()));
    }

    private void tryFavoritingEmoteOnHoveredSlot(Slot hoveredSlot) {
        if (hoveredSlot == null || !hoveredSlot.hasItem()) return;

        Optional<EmoteItem> possibleEmoteItem = Models.Item.asWynnItem(hoveredSlot.getItem(), EmoteItem.class);
        if (possibleEmoteItem.isEmpty() || possibleEmoteItem.get() == null) return;

        Services.FavoritedEmotes.toggleFavorite(possibleEmoteItem.get());
    }
}
