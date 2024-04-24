/*
 * Copyright © Wynntils 2023-2024.
 * This file is released under LGPLv3. See LICENSE for full license details.
 */
package com.wynntils.models.war;

import com.wynntils.core.WynntilsMod;
import com.wynntils.core.components.Handlers;
import com.wynntils.core.components.Model;
import com.wynntils.core.text.StyledText;
import com.wynntils.handlers.bossbar.TrackedBar;
import com.wynntils.mc.event.SubtitleSetTextEvent;
import com.wynntils.models.war.bossbar.WarTowerBar;
import com.wynntils.models.war.event.GuildWarEvent;
import com.wynntils.models.war.event.GuildWarTowerEffectEvent;
import com.wynntils.models.war.type.WarTowerState;
import com.wynntils.models.worlds.event.WorldStateEvent;
import java.util.List;
import java.util.Optional;
import net.minecraftforge.eventbus.api.SubscribeEvent;

public class GuildWarTowerModel extends Model {
    private static final int EFFECT_PROC_MS = 3200;
    private static final StyledText AURA_TITLE = StyledText.fromString("§4§n/!\\§7 Tower §6Aura");
    private static final StyledText VOLLEY_TITLE = StyledText.fromString("§4§n/!\\§7 Tower §dVolley");

    public final TrackedBar WarTowerBar = new WarTowerBar();

    public GuildWarTowerModel() {
        super(List.of());

        Handlers.BossBar.registerBar(WarTowerBar);
    }

    // The state of the tower on the start of the war
    private WarTowerState initialTowerState;

    // Updated on every tower damage event
    private WarTowerState currentTowerState;

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

    @SubscribeEvent
    public void onWorldStateChange(WorldStateEvent evente) {
        resetTowerState();
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

    public Optional<WarTowerState> getInitialTowerState() {
        return Optional.ofNullable(initialTowerState);
    }

    public Optional<WarTowerState> getCurrentTowerState() {
        return Optional.ofNullable(currentTowerState);
    }

    public void onTowerDamaged(WarTowerState towerState) {
        if (initialTowerState == null) {
            initialTowerState = towerState;
            WynntilsMod.postEvent(new GuildWarEvent.Started());
        }

        currentTowerState = towerState;
    }

    public void resetTowerState() {
        initialTowerState = null;
        currentTowerState = null;
        WynntilsMod.postEvent(new GuildWarEvent.Ended());
    }
}
