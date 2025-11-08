/*
 * Copyright © Wynntils 2025.
 * This file is released under LGPLv3. See LICENSE for full license details.
 */
package com.wynntils.models.housing;

import com.wynntils.core.components.Model;
import com.wynntils.core.text.StyledText;
import com.wynntils.handlers.chat.event.ChatMessageEvent;
import com.wynntils.models.worlds.event.WorldStateEvent;
import com.wynntils.models.worlds.type.WorldState;
import com.wynntils.utils.mc.StyledTextUtils;
import java.util.List;
import java.util.regex.Matcher;
import java.util.regex.Pattern;
import net.neoforged.bus.api.SubscribeEvent;

public class HousingModel extends Model {
    private static final Pattern HOUSING_EDIT_PATTERN = Pattern.compile(
            "§e(?:\uE008\uE002|\uE001) You (?<state>are now in|have left) housing edit mode. Type §b/housing edit§e to switch back.");

    private boolean onHousing = false;
    private boolean inEditMode = false;
    private String currentHousingName = "";

    public HousingModel() {
        super(List.of());
    }

    @SubscribeEvent
    public void onWorldStateChange(WorldStateEvent event) {
        if (event.getNewState() != WorldState.WORLD) return;

        updateHousingState(false, "");
    }

    @SubscribeEvent
    public void onChatMessage(ChatMessageEvent.Match event) {
        if (!onHousing) return;

        StyledText message = StyledTextUtils.unwrap(event.getMessage()).stripAlignment();

        Matcher matcher = message.getMatcher(HOUSING_EDIT_PATTERN);
        if (matcher.matches()) {
            inEditMode = matcher.group("state").equals("are now in");
        }
    }

    public void updateHousingState(boolean onHousing, String housingName) {
        this.onHousing = onHousing;
        currentHousingName = housingName;

        if (!onHousing) {
            inEditMode = false;
        }
    }

    public boolean isOnHousing() {
        return onHousing;
    }

    public String getCurrentHousingName() {
        return currentHousingName;
    }

    public boolean isInEditMode() {
        return inEditMode;
    }
}
