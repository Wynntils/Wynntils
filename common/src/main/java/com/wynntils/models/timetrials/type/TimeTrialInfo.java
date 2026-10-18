/*
 * Copyright © Wynntils 2026.
 * This file is released under LGPLv3. See LICENSE for full license details.
 */
package com.wynntils.models.timetrials.type;

import com.wynntils.models.mount.type.MountType;
import com.wynntils.utils.type.CappedValue;

public final class TimeTrialInfo {
    private final String timeTrialName;
    private final MountType mountType;

    private long elapsedTime = -1;
    private CappedValue checkpoints = CappedValue.EMPTY;

    public TimeTrialInfo(String timeTrialName, MountType mountType) {
        this.timeTrialName = timeTrialName;
        this.mountType = mountType;
    }

    public String getTimeTrialName() {
        return timeTrialName;
    }

    public MountType getMountType() {
        return mountType;
    }

    public void updateElapsedTime(long elapsedTime) {
        this.elapsedTime = elapsedTime;
    }

    public long getElapsedTime() {
        return elapsedTime;
    }

    public void updateCheckpoints(CappedValue checkpoints) {
        this.checkpoints = checkpoints;
    }

    public CappedValue getCheckpoints() {
        return checkpoints;
    }
}
