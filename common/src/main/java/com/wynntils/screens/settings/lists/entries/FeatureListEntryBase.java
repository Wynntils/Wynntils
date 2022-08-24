/*
 * Copyright © Wynntils 2022.
 * This file is released under AGPLv3. See LICENSE for full license details.
 */
package com.wynntils.screens.settings.lists.entries;

import net.minecraft.client.gui.components.ContainerObjectSelectionList;

public abstract class FeatureListEntryBase extends ContainerObjectSelectionList.Entry<FeatureListEntryBase> {
    public FeatureListEntryBase() {}

    public abstract int getRenderHeight();
}
