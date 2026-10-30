/*
 * Copyright © Wynntils 2026.
 * This file is released under LGPLv3. See LICENSE for full license details.
 */
package com.wynntils.screens.guides.material;

import com.wynntils.core.components.Models;
import com.wynntils.handlers.tooltip.builders.MaterialTooltipBuilder;
import com.wynntils.handlers.tooltip.type.ItemSourceType;
import com.wynntils.models.items.items.game.MaterialItem;
import com.wynntils.models.profession.type.MaterialInfo;
import com.wynntils.screens.guides.GuideItemStack;
import java.util.ArrayList;
import java.util.List;
import net.minecraft.network.chat.Component;
import net.minecraft.world.entity.player.Player;
import net.minecraft.world.item.Item;
import net.minecraft.world.item.TooltipFlag;

public final class GuideMaterialItemStack extends GuideItemStack {
    private final MaterialInfo materialInfo;
    private final int tier;
    private List<Component> generatedTooltip;

    public GuideMaterialItemStack(MaterialInfo materialInfo, int tier) {
        super(materialInfo.material().itemStack(), new MaterialItem(materialInfo, tier), materialInfo.name());

        this.materialInfo = materialInfo;
        this.tier = tier;
    }

    @Override
    public List<Component> getTooltipLines(Item.TooltipContext context, Player player, TooltipFlag isAdvanced) {
        if (generatedTooltip == null) {
            generatedTooltip =
                    new ArrayList<>(MaterialTooltipBuilder.buildNewItem(materialInfo, tier, ItemSourceType.GUIDE, "")
                            .getTooltipLines(Models.Character.getClassType()));
        }

        return generatedTooltip;
    }

    public int getTier() {
        return tier;
    }

    public MaterialInfo getMaterialInfo() {
        return materialInfo;
    }
}
