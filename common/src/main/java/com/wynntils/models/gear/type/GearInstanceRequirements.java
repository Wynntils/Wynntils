/*
 * Copyright © Wynntils 2026.
 * This file is released under LGPLv3. See LICENSE for full license details.
 */
package com.wynntils.models.gear.type;

import com.wynntils.models.elements.type.Skill;
import java.util.Map;

public record GearInstanceRequirements(
        boolean levelReqMet, boolean classReqMet, Map<Skill, Boolean> skillReqsMet, boolean questReqMet) {
    public static final GearInstanceRequirements UNKNOWN = new GearInstanceRequirements(false, false, Map.of(), false);

    public boolean meetsAllRequirements() {
        return levelReqMet
                && classReqMet
                && skillReqsMet.values().stream().allMatch(Boolean::booleanValue)
                && questReqMet;
    }
}
