/*
 * Copyright © Wynntils 2026.
 * This file is released under LGPLv3. See LICENSE for full license details.
 */
package com.wynntils.models.emote;

import static com.wynntils.features.ui.EmoteWheelFeature.MAX_EMOTES;

import com.wynntils.core.components.Managers;
import com.wynntils.core.components.Model;
import com.wynntils.core.components.Models;
import com.wynntils.features.ui.EmoteWheelFeature;
import com.wynntils.mc.event.CommandSuggestionsEvent;
import com.wynntils.models.worlds.event.WorldStateEvent;
import com.wynntils.utils.StringUtils;
import com.wynntils.utils.mc.McUtils;
import java.util.List;
import net.minecraft.network.protocol.game.ServerboundCommandSuggestionPacket;
import net.neoforged.bus.api.SubscribeEvent;

public class EmoteModel extends Model {
    private static final int EMOTE_COMMAND_PACKET_ID = 227;

    private boolean refreshedRecently = false;

    public EmoteModel() {
        super(List.of());
    }

    public List<String> getAvailableEmotes() {
        return Managers.Feature.getFeatureInstance(EmoteWheelFeature.class).getAvailableEmotes();
    }

    public List<String> getFavoritedEmotes() {
        return Managers.Feature.getFeatureInstance(EmoteWheelFeature.class).getFavoritedEmotes();
    }

    public void refreshAvailableEmotes() {
        // The command does not exist outside of the world
        if (!Models.WorldState.onWorld())
            return;

        McUtils.sendPacket(new ServerboundCommandSuggestionPacket(EMOTE_COMMAND_PACKET_ID, "/emote "));
    }

    @SubscribeEvent
    public void onConnect(WorldStateEvent e) {
        if (!e.isFirstJoinWorld())
            return;

        refreshAvailableEmotes();
    }

    @SubscribeEvent
    public void onRecieve(CommandSuggestionsEvent e) {
        if (e.getId() == EMOTE_COMMAND_PACKET_ID) {
            List<String> emotes = e.getSuggestions().getList().stream()
                    .map(suggestion -> StringUtils.capitalizeFirst(suggestion.getText()))
                    .toList();
            getAvailableEmotes().clear();
            getAvailableEmotes().addAll(emotes);

            Managers.Feature.getFeatureInstance(EmoteWheelFeature.class).updateAvailableEmotes();
            refreshedRecently = true;
        }
    }

    public boolean isRefreshedRecently() {
        return refreshedRecently;
    }

    public void setRefreshedRecently(boolean refreshedRecently) {
        this.refreshedRecently = refreshedRecently;
    }

    public boolean isFavorited(String emote) {
        return emote != null && getFavoritedEmotes().contains(emote);
    }

    private void addFavorite(String emote) {
        int firstNull = getFavoritedEmotes().indexOf(null);
        if (firstNull == -1 || firstNull > MAX_EMOTES - 1) return;

        getFavoritedEmotes().set(firstNull, emote);
        Managers.Feature.getFeatureInstance(EmoteWheelFeature.class).updateFavoritedEmotes();
    }

    private void removeFavorite(String emote) {
        int index = getFavoritedEmotes().indexOf(emote);
        getFavoritedEmotes().set(index, null);
        Managers.Feature.getFeatureInstance(EmoteWheelFeature.class).updateFavoritedEmotes();
    }

    public void toggleFavorite(String emote) {
        if (isFavorited(emote)) {
            removeFavorite(emote);
        } else {
            addFavorite(emote);
        }
    }

    public int getEmoteIndex(String emote) {
        return getFavoritedEmotes().indexOf(emote);
    }
}
