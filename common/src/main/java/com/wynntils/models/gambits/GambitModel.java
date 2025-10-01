/*
 * Copyright © Wynntils 2025.
 * This file is released under LGPLv3. See LICENSE for full license details.
 */
package com.wynntils.models.gambits;

import com.wynntils.core.components.Model;
import com.wynntils.core.components.Models;
import com.wynntils.core.text.StyledText;
import com.wynntils.mc.event.ContainerSetContentEvent;
import com.wynntils.models.containers.Container;
import com.wynntils.models.containers.containers.RaidStartContainer;
import com.wynntils.utils.mc.McUtils;
import java.util.ArrayList;
import java.util.List;
import net.minecraft.network.chat.Component;
import net.minecraft.world.item.Item;
import net.minecraft.world.item.ItemStack;
import net.minecraft.world.item.TooltipFlag;
import net.neoforged.bus.api.SubscribeEvent;

public final class GambitModel extends Model {
    private static final List<String> GAMBITS = List.of(
            "Anemic's",
            "Arcane Incontinent's",
            "Bleeding Warrior's",
            "Burdened Pacifist's",
            "Cursed Alchemist's",
            "Dull Blade's",
            "Eroded Speedster's",
            "Farsighted's",
            "Foreseen Swordsman's",
            "Glutton's",
            "Hemophiliac's",
            "Ingenuous Mage's",
            "Leaden Fighter's",
            "Maddening Mage's",
            "Myopic's",
            "Outworn Soldier's",
            "Shattered Mortal's");

    private final List<String> ActiveGambits = new ArrayList<>();

    public GambitModel() {
        super(List.of());
    }

    public List<String> getActiveGambits() {
        return ActiveGambits;
    }

    @SubscribeEvent
    public void onContentSet(ContainerSetContentEvent.Pre event) {
        Container currentContainer = Models.Container.getCurrentContainer();

        if (currentContainer instanceof RaidStartContainer raidStartContainer) {
            List<Integer> gambitSlots = new ArrayList<>(raidStartContainer.getGambitSlots());
            boolean isFirst = true;

            for (Integer i : gambitSlots) {
                ItemStack itemStack = event.getItems().get(i);
                if (itemStack.isEmpty()) continue;

                List<Component> lore =
                        itemStack.getTooltipLines(Item.TooltipContext.of(McUtils.mc().level), null, TooltipFlag.NORMAL);
                StyledText itemName = StyledText.fromComponent(itemStack.getHoverName());

                String gambitName = null;
                for (String gambit : GAMBITS) {
                    if (itemName.contains(gambit)) {
                        gambitName = gambit;
                        break;
                    }
                }
                if (isFirst && !lore.getLast().toString().contains("Un-ready to change")) {
                    isFirst = false;
                    ActiveGambits.clear();
                }
                if (lore.getLast().toString().contains("Click to Disable") && gambitName != null) {
                    ActiveGambits.add(gambitName);
                }
            }
        }
    }
}
