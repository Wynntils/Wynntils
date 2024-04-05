/*
 * Copyright © Wynntils 2023-2024.
 * This file is released under LGPLv3. See LICENSE for full license details.
 */
package com.wynntils.services.itemfilter.statproviders;

import com.wynntils.models.items.WynnItem;
import com.wynntils.models.items.items.game.AmplifierItem;
import com.wynntils.models.items.items.game.CharmItem;
import com.wynntils.models.items.items.game.CraftedConsumableItem;
import com.wynntils.models.items.items.game.CraftedGearItem;
import com.wynntils.models.items.items.game.DungeonKeyItem;
import com.wynntils.models.items.items.game.EmeraldItem;
import com.wynntils.models.items.items.game.EmeraldPouchItem;
import com.wynntils.models.items.items.game.GatheringToolItem;
import com.wynntils.models.items.items.game.GearBoxItem;
import com.wynntils.models.items.items.game.GearItem;
import com.wynntils.models.items.items.game.HorseItem;
import com.wynntils.models.items.items.game.IngredientItem;
import com.wynntils.models.items.items.game.InsulatorItem;
import com.wynntils.models.items.items.game.MaterialItem;
import com.wynntils.models.items.items.game.MultiHealthPotionItem;
import com.wynntils.models.items.items.game.PotionItem;
import com.wynntils.models.items.items.game.PowderItem;
import com.wynntils.models.items.items.game.RuneItem;
import com.wynntils.models.items.items.game.SimulatorItem;
import com.wynntils.models.items.items.game.TeleportScrollItem;
import com.wynntils.models.items.items.game.TomeItem;
import com.wynntils.services.itemfilter.type.ItemStatProvider;
import java.util.List;

public class ItemTypeStatProvider extends ItemStatProvider<String> {
    @Override
    public List<String> getValue(WynnItem wynnItem) {
        return List.of(wynnItem.getClass().getSimpleName().replace("Item", ""));
    }

    @Override
    public List<String> getValidInputs() {
        // These are the only WynnItem's that can appear in places where
        // searching is supported
        return List.of(
                AmplifierItem.class.getSimpleName().replace("Item", ""),
                CharmItem.class.getSimpleName().replace("Item", ""),
                CraftedConsumableItem.class.getSimpleName().replace("Item", ""),
                CraftedGearItem.class.getSimpleName().replace("Item", ""),
                DungeonKeyItem.class.getSimpleName().replace("Item", ""),
                EmeraldItem.class.getSimpleName().replace("Item", ""),
                EmeraldPouchItem.class.getSimpleName().replace("Item", ""),
                GatheringToolItem.class.getSimpleName().replace("Item", ""),
                GearBoxItem.class.getSimpleName().replace("Item", ""),
                GearItem.class.getSimpleName().replace("Item", ""),
                HorseItem.class.getSimpleName().replace("Item", ""),
                IngredientItem.class.getSimpleName().replace("Item", ""),
                InsulatorItem.class.getSimpleName().replace("Item", ""),
                MaterialItem.class.getSimpleName().replace("Item", ""),
                MultiHealthPotionItem.class.getSimpleName().replace("Item", ""),
                PotionItem.class.getSimpleName().replace("Item", ""),
                PowderItem.class.getSimpleName().replace("Item", ""),
                RuneItem.class.getSimpleName().replace("Item", ""),
                SimulatorItem.class.getSimpleName().replace("Item", ""),
                TeleportScrollItem.class.getSimpleName().replace("Item", ""),
                TomeItem.class.getSimpleName().replace("Item", ""));
    }
}
