/*
 * Copyright © Wynntils 2023-2024.
 * This file is released under LGPLv3. See LICENSE for full license details.
 */
package com.wynntils.models.war;

import com.wynntils.core.WynntilsMod;
import com.wynntils.core.components.Model;
import com.wynntils.core.text.StyledText;
import com.wynntils.mc.event.SubtitleSetTextEvent;
import com.wynntils.models.war.event.GuildWarTowerEffectEvent;
import java.util.List;
import net.minecraftforge.eventbus.api.SubscribeEvent;

public class GuildWarTowerModel extends Model {
    private static final int EFFECT_PROC_MS = 3200;
    private static final StyledText AURA_TITLE = StyledText.fromString("§4§n/!\\§7 Tower §6Aura");
    private static final StyledText VOLLEY_TITLE = StyledText.fromString("§4§n/!\\§7 Tower §dVolley");

    public GuildWarTowerModel() {
        super(List.of());
    }

    private long lastAuraProc = 0;
    private long lastVolleyProc = 0;

    @SubscribeEvent
    public void onSubtitle(SubtitleSetTextEvent event) {
        StyledText styledText = StyledText.fromComponent(event.getComponent());

        if (styledText.equals(AURA_TITLE)) {
            lastAuraProc = System.currentTimeMillis();
            WynntilsMod.postEvent(new GuildWarTowerEffectEvent.AuraSpawned());
            return;
        }

        if (styledText.equals(VOLLEY_TITLE)) {
            lastVolleyProc = System.currentTimeMillis();
            WynntilsMod.postEvent(new GuildWarTowerEffectEvent.VolleySpawned());
        }
    }

    public long getLastAuraProc() {
        return lastAuraProc;
    }

    public long getLastVolleyProc() {
        return lastVolleyProc;
    }

    public int getEffectLength() {
        return EFFECT_PROC_MS;
    }

    public long getRemainingTimeUntilAura() {
        return Math.max(-1, getEffectLength() - (System.currentTimeMillis() - getLastAuraProc()));
    }

    public long getRemainingTimeUntilVolley() {
        return Math.max(-1, getEffectLength() - (System.currentTimeMillis() - getLastVolleyProc()));
    }
}
