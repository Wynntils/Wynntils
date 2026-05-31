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
import com.wynntils.core.text.StyledText;
import com.wynntils.handlers.chat.event.ChatMessageEvent;
import com.wynntils.models.items.items.gui.EmoteItem;
import com.wynntils.screens.emotewheel.EmoteWheelScreen;
import com.wynntils.utils.colors.CommonColors;
import com.wynntils.utils.colors.CustomColor;
import com.wynntils.utils.mc.McUtils;
import com.wynntils.utils.mc.StyledTextUtils;
import com.wynntils.utils.render.type.EmoteWheelButton;
import com.wynntils.utils.render.type.TextShadow;
import java.util.Arrays;
import java.util.List;
import java.util.Optional;
import java.util.regex.Matcher;
import java.util.regex.Pattern;
import net.minecraft.world.inventory.Slot;
import net.neoforged.bus.api.SubscribeEvent;

@ConfigCategory(Category.UI)
public class EmoteWheelFeature extends Feature {
    public static final int MAX_EMOTES = 10;
    private static final Pattern INACCESSIBLE_EMOTE_PATTERN =
            Pattern.compile("§4(?:\uE008\uE002|\uE001) You do not have access to this emote\\.");
    private static final Pattern NONEXISTENT_EMOTE_PATTERN =
            Pattern.compile("§4(?:\uE008\uE002|\uE001) No emotes were found by the given name\\.");

    @RegisterKeyBind
    public final KeyBind openEmoteWheelKeybind =
            KeyBindDefinition.OPEN_EMOTE_WHEEL.create(this::openEmoteWheel, this::tryFavoritingEmoteOnHoveredSlot);

    @Persisted
    private final Config<Integer> numberOfButtons = new Config<>(8);

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

    @Persisted
    public final HiddenConfig<List<String>> favoritedEmotes;

    private int lastEmoteNum = -1;

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

    @SubscribeEvent
    public void onChatMessage(ChatMessageEvent.Match event) {
        if (lastEmoteNum == -1) return;

        StyledText message = StyledTextUtils.unwrap(event.getMessage()).stripAlignment();
        Matcher matcher = message.getMatcher(INACCESSIBLE_EMOTE_PATTERN);
        Matcher matcher2 = message.getMatcher(NONEXISTENT_EMOTE_PATTERN);
        if (matcher.matches() || matcher2.matches()) {
            favoritedEmotes.get().set(lastEmoteNum, null);
            favoritedEmotes.touched();
        }

        lastEmoteNum = -1;
    }

    public void setlastEmoteNum(int lastEmoteNum) {
        this.lastEmoteNum = lastEmoteNum;
    }
}
