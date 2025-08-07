/*
 * Copyright © Wynntils 2025.
 * This file is released under LGPLv3. See LICENSE for full license details.
 */
package com.wynntils.models.bonustotems.label;

import com.wynntils.core.text.StyledText;
import com.wynntils.handlers.labels.type.LabelInfo;
import com.wynntils.models.bonustotems.type.BonusTotemType;
import com.wynntils.utils.mc.type.Location;
import net.minecraft.world.entity.Entity;

public class BonusTotemLabelInfo extends LabelInfo {
    private final BonusTotemType bonusTotemType;
    private final String user;
    private final String timerString;

    public BonusTotemLabelInfo(
            StyledText label,
            Location location,
            Entity entity,
            BonusTotemType bonusTotemType,
            String user,
            String timerString) {
        super(label, location, entity);

        this.bonusTotemType = bonusTotemType;
        this.user = user;
        this.timerString = timerString;
    }

    public BonusTotemType getBonusTotemType() {
        return bonusTotemType;
    }

    public String getUser() {
        return user;
    }

    public String getTimerString() {
        return timerString;
    }
}
