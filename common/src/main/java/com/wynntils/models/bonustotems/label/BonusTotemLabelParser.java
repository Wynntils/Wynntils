/*
 * Copyright © Wynntils 2025.
 * This file is released under LGPLv3. See LICENSE for full license details.
 */
package com.wynntils.models.bonustotems.label;

import com.wynntils.core.WynntilsMod;
import com.wynntils.core.text.StyledText;
import com.wynntils.handlers.labels.type.LabelParser;
import com.wynntils.models.bonustotems.type.BonusTotemType;
import com.wynntils.utils.mc.type.Location;
import java.util.Locale;
import java.util.regex.Matcher;
import java.util.regex.Pattern;
import net.minecraft.world.entity.Entity;

public class BonusTotemLabelParser implements LabelParser<BonusTotemLabelInfo> {
    // Test in BonusTotemLabelParser_BONUS_TOTEM_PATTERN
    private static final Pattern BONUS_TOTEM_PATTERN = Pattern.compile(
            "§#ffd750ff(§o)?(?<user>.*?)(§r§#ffd750ff)?'s?§#[a-z0-9]{8} (?<type>Mob|Gathering) Totem\n§d\uE01F §7(?<timer>([0-9]+m )?[0-9]+s)");

    @Override
    public BonusTotemLabelInfo getInfo(StyledText label, Location location, Entity entity) {
        Matcher matcher = label.getMatcher(BONUS_TOTEM_PATTERN);
        if (!matcher.matches()) return null;

        String user = matcher.group("user");
        String typeStr = matcher.group("type");
        String timerString = matcher.group("timer");

        BonusTotemType type;
        try {
            type = BonusTotemType.valueOf(typeStr.toUpperCase(Locale.ROOT));
        } catch (IllegalArgumentException e) {
            WynntilsMod.warn("Failed to parse bonus totem type: " + typeStr);
            return null;
        }

        return new BonusTotemLabelInfo(label, location, entity, type, user, timerString);
    }
}
