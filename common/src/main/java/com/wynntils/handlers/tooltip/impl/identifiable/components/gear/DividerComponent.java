/*
 * Copyright © Wynntils 2023-2026.
 * This file is released under LGPLv3. See LICENSE for full license details.
 */
package com.wynntils.handlers.tooltip.impl.identifiable.components.gear;

import com.wynntils.handlers.tooltip.impl.identifiable.IdentifiableTooltipComponent;
import com.wynntils.models.gear.type.GearTier;
import net.minecraft.network.chat.Component;

public final class DividerComponent {
    public Component buildSectionDivider(GearTier gearTier) {
        return buildDivider(gearTier);
    }

    public Component buildIdentificationDivider(GearTier gearTier) {
        return buildDivider(gearTier);
    }

    private static Component buildDivider(GearTier gearTier) {
        return GearTooltipSupport.withWhiteShadow(IdentifiableTooltipComponent.DIVIDER
                .copy()
                .withStyle(style -> style.withColor(
                        GearTooltipSupport.getDividerColor(gearTier).asInt())));
    }
}
