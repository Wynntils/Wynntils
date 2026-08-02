/*
 * Copyright © Wynntils 2026.
 * This file is released under LGPLv3. See LICENSE for full license details.
 */
package com.wynntils.screens.guides.material;

import com.wynntils.screens.guides.widgets.GuideButton;
import com.wynntils.utils.colors.CustomColor;
import java.util.List;
import net.minecraft.core.component.DataComponents;
import net.minecraft.resources.Identifier;
import net.minecraft.world.item.component.CustomModelData;

public class MaterialGuideButton extends GuideButton {
    private static final CustomColor MATERIAL_HIGHLIGHT_COLOR = CustomColor.fromInt(0x48E7FF);
    private static final Identifier TOOLTIP_STYLE = Identifier.withDefaultNamespace("profession_material");
    private static final String PROFESSION_STAR_KEY = "profession_tier_";

    public MaterialGuideButton(int x, int y, GuideMaterialItemStack itemStack) {
        super(x, y, itemStack);

        List<String> modelDataString = List.of(PROFESSION_STAR_KEY + itemStack.getTier());
        CustomModelData oldCustomModelData = itemStack.get(DataComponents.CUSTOM_MODEL_DATA);
        CustomModelData newCustomModelData = new CustomModelData(
                oldCustomModelData.floats(), oldCustomModelData.flags(), modelDataString, oldCustomModelData.colors());

        itemStack.set(DataComponents.CUSTOM_MODEL_DATA, newCustomModelData);
        itemStack.set(DataComponents.TOOLTIP_STYLE, TOOLTIP_STYLE);
    }

    // FIXME: This should be painted by ItemHighlightFeature instead...
    @Override
    protected CustomColor getColor() {
        return MATERIAL_HIGHLIGHT_COLOR;
    }
}
