/*
 * Copyright © Wynntils 2023-2026.
 * This file is released under LGPLv3. See LICENSE for full license details.
 */
package com.wynntils.services.emotes.favorites;

import static com.wynntils.features.ui.EmoteWheelFeature.MAX_EMOTES;

import com.wynntils.core.components.Managers;
import com.wynntils.core.components.Service;
import com.wynntils.features.ui.EmoteWheelFeature;
import com.wynntils.models.items.items.gui.EmoteItem;
import java.util.List;

public final class EmotesService extends Service {
    public EmotesService() {
        super(List.of());
    }

    public boolean isFavorited(String emoteName) {
        return emoteName != null && getFavoritedEmotes().contains(emoteName);
    }

    public boolean isFavorited(EmoteItem emoteItem) {
        return isFavorited(emoteItem.getEmoteCommand());
    }

    private void addFavorite(String emoteName) {
        int firstNull = getFavoritedEmotes().indexOf(null);
        if (firstNull == -1 || firstNull > MAX_EMOTES - 1) return;

        getFavoritedEmotes().set(firstNull, emoteName);
        Managers.Feature.getFeatureInstance(EmoteWheelFeature.class)
                .favoritedEmotes
                .touched();
    }

    private void removeFavorite(String emoteName) {
        int index = getFavoritedEmotes().indexOf(emoteName);
        getFavoritedEmotes().set(index, null);
        Managers.Feature.getFeatureInstance(EmoteWheelFeature.class)
                .favoritedEmotes
                .touched();
    }

    public void toggleFavorite(String emoteName) {
        if (isFavorited(emoteName)) {
            removeFavorite(emoteName);
        } else {
            addFavorite(emoteName);
        }
    }

    public List<String> getFavoritedEmotes() {
        // This is a hack to allow saving of favorites in the config
        return Managers.Feature.getFeatureInstance(EmoteWheelFeature.class)
                .favoritedEmotes
                .get();
    }
}
