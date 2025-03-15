/*
 * Copyright © Wynntils 2023-2025.
 * This file is released under LGPLv3. See LICENSE for full license details.
 */
package com.wynntils.models.npc.label;

import com.wynntils.core.text.StyledText;
import com.wynntils.handlers.labels.type.LabelParser;
import com.wynntils.utils.mc.type.Location;
import java.util.regex.Matcher;
import java.util.regex.Pattern;
import net.minecraft.world.entity.Entity;

public class NpcLabelParser implements LabelParser<NpcLabelInfo> {
    // Test in NpcLabelParser_NPC_LABEL_PATTERN
    private static final Pattern NPC_LABEL_PATTERN =
            Pattern.compile("^§f(?<icon>.)\n§(?:c|d)(?<name>[a-zA-Z ]*)(?:§f)?\n§7(?<description>.*)$", Pattern.DOTALL);

    // Special cases
    private static final Pattern HOUSING_LABEL_PATTERN = Pattern.compile("^§fClick §7to go to your housing plot$");
    private static final Pattern BOOTH_SHOP_LABEL_PATTERN =
            Pattern.compile("^(§b.*'(s)?§7 Shop.*|§f\uE000 Click §7to set up booth)$", Pattern.DOTALL);
    private static final Pattern SEASKIPPER_LABEL_PATTERN =
            Pattern.compile("^§6V.S.S. Seaskipper\n§7Right-click to Sail\n§0À$");
    private static final Pattern LOOTRUN_MASTER_LABEL_PATTERN = Pattern.compile("§dLootrun Master\n§7Start a Lootrun");

    public NpcLabelInfo getInfo(StyledText label, Location location, Entity entity) {
        Matcher matcher = NPC_LABEL_PATTERN.matcher(label.getString());
        if (matcher.matches()) {
            return new NpcLabelInfo(
                    label,
                    matcher.group("name"),
                    location.offset(0, -1, 0),
                    entity,
                    matcher.group("icon"),
                    matcher.group("description"));
        }

        if (label.matches(HOUSING_LABEL_PATTERN)) {
            return new NpcLabelInfo(label, "Housing Balloon", location, entity);
        }

        if (label.matches(BOOTH_SHOP_LABEL_PATTERN)) {
            return new NpcLabelInfo(label, "Booth Shop", location.offset(0, -1, 0), entity);
        }

        if (label.matches(SEASKIPPER_LABEL_PATTERN)) {
            return new NpcLabelInfo(label, "Seaskipper", location.offset(0, -1, 0), entity);
        }

        if (label.matches(LOOTRUN_MASTER_LABEL_PATTERN)) {
            return new NpcLabelInfo(label, "Lootrun Master", location.offset(0, -1, 0), entity);
        }

        return null;
    }
}
