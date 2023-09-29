/*
 * Copyright © Wynntils 2023.
 * This file is released under LGPLv3. See LICENSE for full license details.
 */
package com.wynntils.models.characterstats;

import com.wynntils.core.components.Model;
import com.wynntils.core.components.Models;
import com.wynntils.models.gear.type.GearInstance;
import com.wynntils.models.items.ItemModel;
import com.wynntils.models.items.items.game.GearItem;
import com.wynntils.models.stats.type.ShinyStat;
import com.wynntils.utils.mc.McUtils;
import java.util.ArrayList;
import java.util.List;
import java.util.Optional;
import net.minecraft.world.item.ItemStack;

public class ShinyModel extends Model {
    public ShinyModel(ItemModel itemModel) {
        super(List.of(itemModel));
    }

    public Optional<ShinyStat> getShinyStat(ItemStack itemStack) {
        Optional<GearItem> gearItemOpt = Models.Item.asWynnItem(itemStack, GearItem.class);
        if (gearItemOpt.isEmpty()) return Optional.empty();

        Optional<GearInstance> gearInstanceOpt = gearItemOpt.get().getGearInstance();
        if (gearInstanceOpt.isEmpty()) return Optional.empty();

        return gearInstanceOpt.get().shinyStat();
    }

    public List<ShinyStat> getAllShinyStats() {
        List<ShinyStat> allShinies = new ArrayList<>();
        int size = McUtils.inventory().getContainerSize();
        for (int i = 0; i < size; i++) {
            ItemStack itemStack = McUtils.inventory().getItem(i);
            Optional<ShinyStat> shinyOpt = getShinyStat(itemStack);
            if (shinyOpt.isPresent()) {
                allShinies.add(shinyOpt.get());
            }
        }

        return allShinies;
    }
}
