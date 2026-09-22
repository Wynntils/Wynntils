/*
 * Copyright © Wynntils 2022-2026.
 * This file is released under LGPLv3. See LICENSE for full license details.
 */
package com.wynntils.models.abilities;

import com.wynntils.core.WynntilsMod;
import com.wynntils.core.components.Model;
import com.wynntils.core.text.StyledText;
import com.wynntils.mc.event.SubtitleSetTextEvent;
import com.wynntils.models.abilities.event.ShamanMaskTitlePacketEvent;
import com.wynntils.models.abilities.type.ShamanMaskType;
import com.wynntils.models.character.event.CharacterDeathEvent;
import com.wynntils.models.worlds.event.WorldStateEvent;
import java.util.List;
import net.neoforged.bus.api.SubscribeEvent;

public final class ShamanMaskModel extends Model {
    private ShamanMaskType currentMaskType = ShamanMaskType.NONE;

    public ShamanMaskModel() {
        super(List.of());
    }

    @SubscribeEvent
    public void onSubtitle(SubtitleSetTextEvent event) {
        StyledText title = StyledText.fromComponent(event.getComponent());

        if (title.contains("➤")) {
            parseMask(title);

            if (currentMaskType == null) return;

            ShamanMaskTitlePacketEvent maskEvent = new ShamanMaskTitlePacketEvent();
            WynntilsMod.postEvent(maskEvent);

            if (maskEvent.isCanceled()) {
                event.setCanceled(true);
            }
        }
    }

    @SubscribeEvent
    public void onCharacterDeath(CharacterDeathEvent event) {
        resetMask();
    }

    @SubscribeEvent
    public void onWorldStateChange(WorldStateEvent event) {
        resetMask();
    }

    private void resetMask() {
        currentMaskType = ShamanMaskType.NONE;
    }

    private void parseMask(StyledText title) {
        ShamanMaskType parsedMask = ShamanMaskType.NONE;

        for (ShamanMaskType type : ShamanMaskType.values()) {
            if (type.getParseStyledText() == null) continue;

            if (title.contains(type.getParseStyledText())) {
                parsedMask = type;
                break;
            }
        }

        currentMaskType = parsedMask;
    }

    public ShamanMaskType getCurrentMaskType() {
        return currentMaskType;
    }
}
