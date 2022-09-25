/*
 * Copyright © Wynntils 2022.
 * This file is released under AGPLv3. See LICENSE for full license details.
 */
package com.wynntils.wynn.model.bossbar;

import java.util.UUID;
import java.util.regex.Matcher;
import java.util.regex.Pattern;

import com.wynntils.wynn.objects.ClassType;
import net.minecraft.Util;
import net.minecraft.util.Mth;

public abstract class TrackedBar {
    public final Pattern pattern;
    public final BarType type;
    public final ClassType classType;

    private boolean rendered = true;

    protected float progress = 0;
    protected float targetProgress = 0;
    protected long setTime = 0;

    protected int current = 0;
    protected int max = 0;

    protected UUID uuid = null;

    TrackedBar(Pattern pattern, BarType type, ClassType classType) {
        this.pattern = pattern;
        this.type = type;
        this.classType = classType;
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
        this.targetProgress = progress;
        this.setTime = Util.getMillis();
    }

    public float getProgress() {
        long l = Util.getMillis() - this.setTime;
        float f = Mth.clamp((float) l / 100.0F, 0.0F, 1.0F);
        return Mth.lerp(f, this.progress, this.targetProgress);
    }

    public void setUuid(UUID uuid) {
        this.uuid = uuid;
    }

    public UUID getUuid() {
        return uuid;
    }

    void reset() {
        progress = 0;
        targetProgress = 0;
        setTime = 0;
        current = 0;
        max = 0;
        rendered = true;
        uuid = null;
    }

    public boolean isActive() {
        return uuid != null;
    }

    public BossBarModel.BarProgress getBarProgress() {
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
