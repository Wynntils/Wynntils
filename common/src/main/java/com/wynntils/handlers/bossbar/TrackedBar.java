/*
 * Copyright © Wynntils 2022-2024.
 * This file is released under LGPLv3. See LICENSE for full license details.
 */
package com.wynntils.handlers.bossbar;

import com.wynntils.handlers.bossbar.type.BossBarProgress;
import com.wynntils.mc.mixin.accessors.LerpingBossEventAccessor;
import com.wynntils.utils.type.CappedValue;
import java.util.Collections;
import java.util.List;
import java.util.regex.Matcher;
import java.util.regex.Pattern;
import net.minecraft.client.gui.components.LerpingBossEvent;

public class TrackedBar {
    public final List<Pattern> patterns;

    private boolean rendered = true;
    private LerpingBossEvent event = null;
    private CappedValue value = CappedValue.EMPTY;

    public TrackedBar(Pattern pattern) {
        this.patterns = List.of(pattern);
    }

    public TrackedBar(List<Pattern> patterns) {
        this.patterns = Collections.unmodifiableList(patterns);
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

    protected void reset() {
        value = CappedValue.EMPTY;
        event = null;
        rendered = true;
    }

    public boolean isActive() {
        return event != null;
    }

    public BossBarProgress getBarProgress() {
        return isActive() ? new BossBarProgress(value, event.getProgress()) : null;
    }

    protected void updateValue(int current, int max) {
        value = new CappedValue(current, max);
    }
}
