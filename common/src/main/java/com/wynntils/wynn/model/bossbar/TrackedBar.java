package com.wynntils.wynn.model.bossbar;

import net.minecraft.Util;
import net.minecraft.util.Mth;

import java.util.UUID;
import java.util.regex.Matcher;
import java.util.regex.Pattern;

public abstract class TrackedBar {
    public final Pattern pattern;
    public final BarType type;

    protected float progress = -1;
    private float targetPercent = -1;
    private long setTime = -1;

    private boolean rendered = true;

    protected int current = -1;
    protected int max = -1;

    protected UUID uuid = null;

    TrackedBar(Pattern pattern, BarType type) {
        this.pattern = pattern;
        this.type = type;
    }

    public void onAdd() {

    }

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
        progress = -1;
        targetPercent = -1;
        setTime = -1;
        rendered = true;
    }

    public boolean isActive() {
        return progress != -1;
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
