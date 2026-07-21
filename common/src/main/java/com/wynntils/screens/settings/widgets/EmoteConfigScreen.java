/*
 * Copyright © Wynntils 2022-2026.
 * This file is released under LGPLv3. See LICENSE for full license details.
 */
package com.wynntils.screens.settings.widgets;

import static com.wynntils.features.ui.EmoteWheelFeature.MAX_EMOTES;

import com.wynntils.core.persisted.config.Config;
import com.wynntils.screens.emotewheel.EmoteWheelConfigScreen;
import java.util.Arrays;
import java.util.List;
import java.util.regex.Matcher;
import java.util.regex.Pattern;
import net.minecraft.client.gui.components.AbstractWidget;
import net.minecraft.client.gui.screens.Screen;
import net.minecraft.network.chat.Component;

public class EmoteConfigScreen extends CustomConfigScreen {
    private static final Pattern EMOTES_PATTERN =
            Pattern.compile("\\{favoritedEmotes=\\[(?:(\\w+),?)(?:(\\w+),?)(?:(\\w+),?)(?:(\\w+),?)(?:(\\w+),?)"
                    + "(?:(\\w+),?)(?:(\\w+),?)(?:(\\w+),?)(?:(\\w+),?)(?:(\\w+),?)]}");

    private final List<String> favoritedEmotes;

    public EmoteConfigScreen() {
        super();
        this.favoritedEmotes = Arrays.asList(new String[MAX_EMOTES]);
    }

    public EmoteConfigScreen(String string) {
        this.favoritedEmotes = Arrays.asList(new String[MAX_EMOTES]);
        Matcher matcher = EMOTES_PATTERN.matcher(string.replace(" ", ""));

        if (!matcher.matches()) {
            throw new RuntimeException("Failed to parse FavoritedEmotes");
        }

        try {
            for (int i = 0; i < MAX_EMOTES; i++) {
                if (matcher.group(i + 1).equals("null")) this.favoritedEmotes.set(i, null);
                else this.favoritedEmotes.set(i, matcher.group(i + 1));
            }
        } catch (IllegalArgumentException exception) {
            throw new RuntimeException("Failed to parse FavoritedEmotes", exception);
        }
    }

    public List<String> getFavoritedEmotes() {
        return this.favoritedEmotes;
    }

    @Override
    public AbstractWidget customScreenWidget(
            int renderX, int renderY, Config<?> configOption, Screen screen, int maskTopY, int maskBottomY) {
        return new EmoteWheelSettingsWidget(renderX, renderY, configOption, screen, maskTopY, maskBottomY);
    }

    public static class EmoteWheelSettingsWidget extends ScreenSettingsButton {
        public EmoteWheelSettingsWidget(
                int x, int y, Config<?> config, Screen returnScreen, int maskTopY, int maskBottomY) {
            super(x, y, config, EmoteWheelConfigScreen.create(returnScreen), maskTopY, maskBottomY);
        }

        @Override
        public Component getMessage() {
            return Component.translatable("screens.wynntils.overlaySelection.edit");
        }
    }
}
