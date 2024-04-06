/*
 * Copyright © Wynntils 2023.
 * This file is released under LGPLv3. See LICENSE for full license details.
 */
package com.wynntils.models.territories;

import com.wynntils.core.components.Model;
import com.wynntils.core.text.StyledText;
import com.wynntils.mc.event.SubtitleSetTextEvent;
import java.util.List;
import net.minecraftforge.eventbus.api.SubscribeEvent;

public class TowerAuraTimerModel extends Model {
    private static final int AURA_PROC_MS = 3200;
    private static final StyledText AURA_TITLE = StyledText.fromString("§4§n/!\\§7 Tower §6Aura");

    public TowerAuraTimerModel() {
        super(List.of());
    }

    private long lastAuraProc = 0;

    @SubscribeEvent
    public void onSubtitle(SubtitleSetTextEvent event) {
        if (!StyledText.fromComponent(event.getComponent()).equals(AURA_TITLE)) return;

        lastAuraProc = System.currentTimeMillis();
    }

    public long getLastAuraProc() {
        return lastAuraProc;
    }

    public int getAuraLength() {
        return AURA_PROC_MS;
    }

    public long getRemainingTimeUntilAura() {
        return Math.max(-1, getAuraLength() - (System.currentTimeMillis() - getLastAuraProc()));
    }
}
