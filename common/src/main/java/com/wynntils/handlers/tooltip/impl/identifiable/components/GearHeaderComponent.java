/*
 * Copyright © Wynntils 2023-2026.
 * This file is released under LGPLv3. See LICENSE for full license details.
 */
package com.wynntils.handlers.tooltip.impl.identifiable.components;

import com.wynntils.handlers.tooltip.impl.identifiable.components.gear.GearOverviewComponent;
import com.wynntils.handlers.tooltip.impl.identifiable.components.gear.GearRequirementsComponent;
import com.wynntils.handlers.tooltip.impl.identifiable.components.gear.GearTitleComponent;
import com.wynntils.models.gear.type.GearInfo;
import com.wynntils.models.gear.type.GearInstance;
import java.util.ArrayList;
import java.util.List;
import net.minecraft.network.chat.Component;

public final class GearHeaderComponent {
    private final GearTitleComponent titleComponent = new GearTitleComponent();
    private final GearOverviewComponent overviewComponent = new GearOverviewComponent();
    private final GearRequirementsComponent requirementsComponent = new GearRequirementsComponent();

    public List<Component> buildHeaderTooltip(GearInfo gearInfo, GearInstance gearInstance, boolean hideUnidentified) {
        List<Component> header = new ArrayList<>();
        header.addAll(titleComponent.buildHeaderLines(gearInfo, gearInstance, hideUnidentified));
        header.addAll(overviewComponent.buildHeaderLines(gearInfo));
        header.addAll(requirementsComponent.buildHeaderLines(gearInfo, gearInstance));
        return header;
    }
}
