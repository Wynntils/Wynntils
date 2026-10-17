/*
 * Copyright © Wynntils 2026.
 * This file is released under LGPLv3. See LICENSE for full license details.
 */
package com.wynntils.screens.maps.managers.widgets.providermanager;

import com.wynntils.screens.maps.managers.type.ProviderType;
import java.util.ArrayList;
import java.util.List;
import net.minecraft.client.gui.components.AbstractWidget;
import net.minecraft.network.chat.Component;

public abstract class AbstractProviderWidget extends AbstractWidget {
    protected final ProviderType providerType;
    protected List<Component> generatedTooltip = new ArrayList<>();
    public boolean display = true;

    protected AbstractProviderWidget(Component label, int height, ProviderType providerType) {
        super(0, 0, 0, height, label);
        this.providerType = providerType;
    }

    public ProviderType getProviderType() {
        return providerType;
    }
}
