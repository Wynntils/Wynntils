/*
 * Copyright © Wynntils 2026.
 * This file is released under LGPLv3. See LICENSE for full license details.
 */
package com.wynntils.screens.guides.widgets.filters;

import com.wynntils.core.text.fonts.CommonFonts;
import com.wynntils.screens.base.widgets.WynntilsCheckbox;
import com.wynntils.services.itemfilter.type.ItemSearchQuery;
import com.wynntils.services.itemfilter.type.ItemStatProvider;
import com.wynntils.services.itemfilter.type.StatProviderAndFilterPair;
import net.minecraft.client.gui.narration.NarrationElementOutput;
import net.minecraft.network.chat.Component;
import net.minecraft.network.chat.Style;

public abstract class GuideFilterCheckbox<T extends ItemStatProvider<?>> extends WynntilsCheckbox {
    protected GuideFilterCheckbox(String label) {
        super(
                0,
                0,
                16,
                Component.literal(label).withStyle(Style.EMPTY.withFont(CommonFonts.LANGUAGE_WYNNCRAFT_FONT)),
                false,
                100);
    }

    protected abstract void updateStateFromQuery(ItemSearchQuery searchQuery);

    protected abstract StatProviderAndFilterPair getFilterPair(T provider);

    @Override
    protected void updateWidgetNarration(NarrationElementOutput narrationElementOutput) {}
}
