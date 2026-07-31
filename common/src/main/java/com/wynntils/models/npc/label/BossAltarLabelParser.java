/*
 * Copyright © Wynntils 2026.
 * This file is released under LGPLv3. See LICENSE for full license details.
 */
package com.wynntils.models.npc.label;

import com.wynntils.core.text.StyledText;
import com.wynntils.handlers.labels.type.LabelParser;
import com.wynntils.utils.mc.type.Location;
import java.util.regex.Matcher;
import java.util.regex.Pattern;
import net.minecraft.world.entity.Entity;

public class BossAltarLabelParser implements LabelParser<BossAltarLabelInfo> {
    // Test in BossAltarLabelParser_BOSS_ALTAR_LABEL_PATTERN
    private static final Pattern BOSS_ALTAR_LABEL_PATTERN = Pattern.compile(
            "§#d9822bff\uE060\uDAFF\uDFFF\uE031\uDAFF\uDFFF\uE03E\uDAFF\uDFFF\uE042\uDAFF\uDFFF\uE042\uDAFF\uDFFF\uE061\uDAFF\uDFFF\uE030\uDAFF\uDFFF\uE03B\uDAFF\uDFFF\uE043\uDAFF\uDFFF\uE030\uDAFF\uDFFF\uE041\uDAFF\uDFFF\uE062\uDAFF\uDFC4§0\uE001\uE00E\uE012\uE012 \uE000\uE00B\uE013\uE000\uE011\uDB00\uDC02§#d9822bff\n§#f2d349ff(§k)?(?<name>[A-Za-z' ]+)§#d9822bff\n\n§7Recommended Level: §f(?<level>\\d+)\n\n§7tribute \\[(?<tributeAmount>\\d+)\\]",
            Pattern.DOTALL);

    public BossAltarLabelInfo getInfo(StyledText label, Location location, Entity entity) {
        Matcher bossAltarMatcher = BOSS_ALTAR_LABEL_PATTERN.matcher(label.getString());
        if (bossAltarMatcher.matches()) {
            String name = bossAltarMatcher.group("name");

            int level = Integer.parseInt(bossAltarMatcher.group("level"));
            int tributeAmount = Integer.parseInt(bossAltarMatcher.group("tributeAmount"));

            return new BossAltarLabelInfo(label, name, location.offset(0, 4, 0), entity, level, tributeAmount);
        }

        return null;
    }
}
