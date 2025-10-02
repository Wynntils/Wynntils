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
import com.wynntils.models.gambits.type.Gambit;
import com.wynntils.utils.mc.McUtils;
import java.util.ArrayList;
import java.util.List;
import net.minecraft.network.chat.Component;
import net.minecraft.world.item.Item;
import net.minecraft.world.item.ItemStack;
import net.minecraft.world.item.TooltipFlag;
import net.neoforged.bus.api.SubscribeEvent;

public final class GambitModel extends Model {
    private final List<Gambit> ActiveGambits = new ArrayList<>();

    private static final String GAMBIT_ENABLED="Click to Disable";
    private static final String PLAYER_READY="Un-ready to change";

    public GambitModel() {
        super(List.of());
    }

    public List<Gambit> getActiveGambits() {
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

                //we only need the last line of the tooltip
                Component tooltipLast =
                        itemStack.getTooltipLines(Item.TooltipContext.of(McUtils.mc().level), null, TooltipFlag.NORMAL).getLast();

                Gambit gambit = Gambit.fromItemName(StyledText.fromComponent(itemStack.getHoverName()));
                if (gambit == null) continue;

                // only clear the gambits before adding the first item, skip if player is readied up
                if (isFirst && !tooltipLast.toString().contains(PLAYER_READY)) {
                    isFirst = false;
                    ActiveGambits.clear();
                }

                if (tooltipLast.toString().contains(GAMBIT_ENABLED) && gambit!= null) {
                    ActiveGambits.add(gambit);
                }
            }
        }
    }
}
