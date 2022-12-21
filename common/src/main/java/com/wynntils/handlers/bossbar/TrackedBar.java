/*
 * Copyright © Wynntils 2022.
 * This file is released under AGPLv3. See LICENSE for full license details.
 */
package com.wynntils.handlers.bossbar;

import com.wynntils.mc.mixin.LerpingBossEventAccessor;
import java.util.UUID;
import java.util.regex.Matcher;
import java.util.regex.Pattern;
import net.minecraft.client.gui.components.LerpingBossEvent;

public class TrackedBar {
    public final Pattern pattern;
    private final String barType;

    private boolean rendered = true;

    private LerpingBossEvent event = null;

    protected int current = 0;
    protected int max = 0;

    protected UUID uuid = null;

    public TrackedBar(Pattern pattern, String barType) {
        this.pattern = pattern;
        this.barType = barType;
    }

    public void onUpdateName(Matcher match) {}

    public void onUpdateProgress(float progress) {}

    public boolean isRendered() {
        return rendered;
    }

    public void setEvent(LerpingBossEvent event) {
        this.event = event;
    }

    public void setRendered(boolean rendered) {
        this.rendered = rendered;
    }

    public float getTargetProgress() {
        return ((LerpingBossEventAccessor) event).getTargetPercent();
    }

    public LerpingBossEvent getEvent() {
        return event;
    }

    public UUID getUuid() {
        return event.getId();
    }

    void reset() {
        current = 0;
        max = 0;
        event = null;
        rendered = true;
    }

    public boolean isActive() {
        return event != null;
    }

    public BossBarProgress getBarProgress() {
        return isActive() ? new BossBarProgress(current, max, event.getProgress()) : null;
    }

    public String getBarType() {
        return barType;
    }
}
