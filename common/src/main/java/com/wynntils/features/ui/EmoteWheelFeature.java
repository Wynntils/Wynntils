/*
 * Copyright © Wynntils 2026.
 * This file is released under LGPLv3. See LICENSE for full license details.
 */
package com.wynntils.features.ui;

import com.wynntils.core.components.Models;
import com.wynntils.core.consumers.features.Feature;
import com.wynntils.core.consumers.features.ProfileDefault;
import com.wynntils.core.consumers.features.properties.RegisterKeyBind;
import com.wynntils.core.keybinds.KeyBind;
import com.wynntils.core.keybinds.KeyBindDefinition;
import com.wynntils.core.persisted.Persisted;
import com.wynntils.core.persisted.config.Category;
import com.wynntils.core.persisted.config.Config;
import com.wynntils.core.persisted.config.ConfigCategory;
import com.wynntils.mc.event.CommandSuggestionsEvent;
import com.wynntils.models.worlds.event.WorldStateEvent;
import com.wynntils.screens.emotewheel.EmoteWheelScreen;
import com.wynntils.screens.settings.widgets.EmoteConfigScreen;
import com.wynntils.utils.StringUtils;
import com.wynntils.utils.colors.CommonColors;
import com.wynntils.utils.colors.CustomColor;
import com.wynntils.utils.mc.McUtils;
import com.wynntils.utils.render.type.EmoteWheelButton;
import com.wynntils.utils.render.type.TextShadow;
import java.util.List;
import net.minecraft.network.protocol.game.ServerboundCommandSuggestionPacket;
import net.neoforged.bus.api.SubscribeEvent;

@ConfigCategory(Category.UI)
public class EmoteWheelFeature extends Feature {
    public static final int MAX_EMOTES = 10;
    private static final int EMOTE_COMMAND_PACKET_ID = 227;

    @RegisterKeyBind
    public final KeyBind openEmoteWheelKeybind = KeyBindDefinition.OPEN_EMOTE_WHEEL.create(this::openEmoteWheel);

    @Persisted
    public final Config<EmoteConfigScreen> configureEmotes = new Config<>(new EmoteConfigScreen());

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

    public List<String> availableEmotes;

    private boolean refreshedRecently = false;

    public EmoteWheelFeature() {
        super(ProfileDefault.onlyDefault());
    }

    private void openEmoteWheel() {
        if (openEmoteWheelKeybind.isPressed()) return;
        if (McUtils.screen() != null && !(McUtils.screen() instanceof EmoteWheelScreen)) return;

        McUtils.setScreen(EmoteWheelScreen.create());
    }

    public void refreshAvailableEmotes() {
        // The command does not exist outside of the world
        if (Models.WorldState.onWorld())
            McUtils.sendPacket(new ServerboundCommandSuggestionPacket(EMOTE_COMMAND_PACKET_ID, "/emote "));
    }

    @SubscribeEvent
    public void onConnect(WorldStateEvent e) {
        if (!e.isFirstJoinWorld()) {
            return;
        }
        refreshAvailableEmotes();
    }

    @SubscribeEvent
    public void onRecieve(CommandSuggestionsEvent e) {
        if (e.getId() == EMOTE_COMMAND_PACKET_ID) {
            availableEmotes = e.getSuggestions().getList().stream()
                    .map(suggestion -> StringUtils.capitalizeFirst(suggestion.getText()))
                    .toList();
            refreshedRecently = true;
        }
    }

    public boolean isRefreshedRecently() {
        return refreshedRecently;
    }

    public void setRefreshedRecently(boolean refreshedRecently) {
        this.refreshedRecently = refreshedRecently;
    }
}
