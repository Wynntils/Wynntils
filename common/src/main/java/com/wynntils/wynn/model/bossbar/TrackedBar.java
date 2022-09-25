/*
 * Copyright © Wynntils 2022.
 * This file is released under AGPLv3. See LICENSE for full license details.
 */
package com.wynntils.wynn.model.bossbar;

import java.util.UUID;
import java.util.regex.Matcher;
import java.util.regex.Pattern;
import net.minecraft.Util;
import net.minecraft.util.Mth;

public abstract class TrackedBar {
    public final Pattern pattern;
    public final BarType type;

    protected float progress = 0;
    private float targetPercent = 0;
    private long setTime = 0;

    private boolean rendered = true;

    protected int current = 0;
    protected int max = 0;

    protected UUID uuid = null;

    TrackedBar(Pattern pattern, BarType type) {
        this.pattern = pattern;
        this.type = type;
    }

    public void onAdd() {}

    public abstract void onUpdateName(Matcher match);

    public boolean isRendered() {
        return rendered;
    }

    public void setRendered(boolean rendered) {
        this.rendered = rendered;
    }

    public void setProgress(float progress) {
        this.progress = this.getProgress();
        this.targetPercent = progress;
        this.setTime = Util.getMillis();
    }

    public float getProgress() {
        long l = Util.getMillis() - this.setTime;
        float f = Mth.clamp((float) l / 100.0F, 0.0F, 1.0F);
        return Mth.lerp(f, this.progress, this.targetPercent);
    }

    public void setUuid(UUID uuid) {
        this.uuid = uuid;
    }

    public UUID getUuid() {
        return uuid;
    }

    void reset() {
        progress = 0;
        targetPercent = 0;
        setTime = 0;
        rendered = true;
        uuid = null;
    }

    public boolean isActive() {
        return uuid != null;
    }

    public BossBarModel.BarProgress getBar() {
        return isActive() ? new BossBarModel.BarProgress(current, max, getProgress()) : null;
    }

    public enum BarType {
        BLOODPOOL,
        MANABANK,
        AWAKENED,
        FOCUS,
        CORRUPTED
    }
}
