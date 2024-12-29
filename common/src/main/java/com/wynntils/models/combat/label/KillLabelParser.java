/*
 * Copyright © Wynntils 2024.
 * This file is released under LGPLv3. See LICENSE for full license details.
 */
package com.wynntils.models.combat.label;

import com.wynntils.core.text.StyledText;
import com.wynntils.handlers.labels.type.LabelParser;
import com.wynntils.models.combat.type.KillCreditType;
import com.wynntils.utils.mc.type.Location;
import java.util.regex.Matcher;
import java.util.regex.Pattern;
import net.minecraft.world.entity.Entity;

public class KillLabelParser implements LabelParser<KillLabelInfo> {
    // Test in KillLabelParser_KILL_LABEL_PATTERN
    private static final Pattern KILL_LABEL_PATTERN = Pattern.compile(
            "(?:§dx\\d )?§7\\[§f\\+(?:§d)?(?<combatXp>\\d+)(?:§f)? Combat XP§7\\](\n(?:§dx\\d )?(?:§bx\\d\\.\\d )?(?:§7)?\\[§f\\+(?:§(?:b|d))?(?<guildXp>\\d+)(?:§f)? Guild XP§7\\])?\n\\[(?<killCredit>.*)\\]");

    @Override
    public KillLabelInfo getInfo(StyledText label, Location location, Entity entity) {
        Matcher matcher = label.getMatcher(KILL_LABEL_PATTERN);
        if (!matcher.matches()) return null;

        // The xp numbers already have the multiplier applied, so during dxp we don't need to multiply combatXp by 2
        // or the 10% guild xp boost blessings
        // When a kill is shared, the total xp is the same for everyone, but it is split based on damage dealt.
        // For example, 2 players dealing 50% damage to a mob will each get 50% of the xp from the kill
        // If one player does 80% of the damage and the other does 20% then the xp split is 80% and 20%
        // We currently have no way to split the xp so it is unused but parse it anyway
        int combatXp = Integer.parseInt(matcher.group("combatXp"));
        String guildXpStr = matcher.group("guildXp");
        int guildXp = guildXpStr == null ? 0 : Integer.parseInt(guildXpStr);
        String killCreditStr = matcher.group("killCredit");
        KillCreditType killCredit = killCreditStr.equals("Shared") ? KillCreditType.SHARED : KillCreditType.SELF;

        return new KillLabelInfo(label, location, entity, combatXp, guildXp, killCredit);
    }
}
