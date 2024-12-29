/*
 * Copyright © Wynntils 2024.
 * This file is released under LGPLv3. See LICENSE for full license details.
 */
package com.wynntils.models.combat.label;

import com.wynntils.core.text.StyledText;
import com.wynntils.handlers.labels.type.LabelInfo;
import com.wynntils.models.combat.type.KillCreditType;
import com.wynntils.utils.mc.type.Location;
import net.minecraft.world.entity.Entity;

public class KillLabelInfo extends LabelInfo {
    private final int combatXp;
    private final int guildXp;
    private final KillCreditType killCredit;

    public KillLabelInfo(
            StyledText label, Location location, Entity entity, int combatXp, int guildXp, KillCreditType killCredit) {
        super(label, location, entity);

        this.combatXp = combatXp;
        this.guildXp = guildXp;
        this.killCredit = killCredit;
    }

    public int getCombatXp() {
        return combatXp;
    }

    public int getGuildXp() {
        return guildXp;
    }

    public KillCreditType getKillCredit() {
        return killCredit;
    }
}
