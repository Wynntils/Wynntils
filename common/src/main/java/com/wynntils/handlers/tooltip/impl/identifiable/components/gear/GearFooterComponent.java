/*
 * Copyright © Wynntils 2026.
 * This file is released under LGPLv3. See LICENSE for full license details.
 */
package com.wynntils.handlers.tooltip.impl.identifiable.components.gear;

import com.wynntils.models.gear.type.GearInfo;
import com.wynntils.models.gear.type.GearInstance;
import com.wynntils.models.gear.type.GearRestrictions;
import com.wynntils.utils.StringUtils;
import java.util.ArrayList;
import java.util.List;
import net.minecraft.ChatFormatting;
import net.minecraft.network.chat.Component;

public final class GearFooterComponent {
    public List<Component> buildFooterTooltip(GearInfo gearInfo, GearInstance gearInstance, boolean showItemType) {
        List<Component> footer = new ArrayList<>();
        if (gearInfo.metaInfo().restrictions() == GearRestrictions.NONE) {
            return footer;
        }

        footer.add(Component.empty());

        if (gearInfo.metaInfo().restrictions() != GearRestrictions.NONE) {
            footer.add(Component.literal(StringUtils.capitalizeFirst(
                            gearInfo.metaInfo().restrictions().getDescription()))
                    .withStyle(ChatFormatting.RED));
        }

        return footer;
    }
}
