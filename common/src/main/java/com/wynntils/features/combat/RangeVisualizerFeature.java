package com.wynntils.features.combat;

import com.wynntils.core.components.Models;
import com.wynntils.core.config.Category;
import com.wynntils.core.config.ConfigCategory;
import com.wynntils.core.features.Feature;
import com.wynntils.mc.event.PlayerRenderEvent;
import com.wynntils.models.gear.type.GearType;
import com.wynntils.models.items.items.game.GearItem;
import net.minecraft.world.InteractionHand;
import net.minecraftforge.eventbus.api.SubscribeEvent;

import java.util.Optional;

@ConfigCategory(Category.COMBAT)
public class RangeVisualizerFeature extends Feature {

    @SubscribeEvent
    public void onPlayerRender(PlayerRenderEvent e) {
        System.out.println(e.getPlayer().getName());
        Optional<GearItem> item = Models.Item.asWynnItem(e.getPlayer().getItemInHand(InteractionHand.MAIN_HAND), GearItem.class);
        if (item.isEmpty()) return;
        GearItem gearItem = item.get();

        System.out.println(gearItem.getGearInfo().name());

        // Major IDs that we can visualize:
        // Taunt (12 blocks)
        // Magnet (8 blocks)
        // Saviour's Sacrifice (8 blocks)?
        // Heart of the pack (8 blocks)?
        // Guardian (8 blocks)?
        // Marked with a ? needs additional confirmation

        GearType gearType = item.get().getGearType();
    }
}
