/*
 * Copyright © Wynntils 2022.
 * This file is released under AGPLv3. See LICENSE for full license details.
 */
package com.wynntils.features.user.tooltips;

import com.wynntils.core.features.UserFeature;
import com.wynntils.mc.event.ItemTooltipRenderEvent;
import com.wynntils.model.item.ItemModel;
import com.wynntils.model.item.game.GearItem;
import com.wynntils.utils.KeyboardUtils;
import com.wynntils.wynn.utils.GearTooltipBuilder;
import java.util.ArrayList;
import java.util.List;
import net.minecraft.network.chat.Component;
import net.minecraftforge.eventbus.api.SubscribeEvent;
import org.lwjgl.glfw.GLFW;

public class GearTooltipFeature extends UserFeature {
    @SubscribeEvent
    public void onTooltipPre(ItemTooltipRenderEvent.Pre event) {
        var wynnItemOpt = ItemModel.getWynnItem(event.getItemStack());
        if (wynnItemOpt.isEmpty()) return;
        if (!(wynnItemOpt.get() instanceof GearItem gearItem)) return;

        // FIXME!
        if (KeyboardUtils.isKeyDown(GLFW.GLFW_KEY_RIGHT_SHIFT)) {
            List<Component> tooltips = new ArrayList<>();
            tooltips.addAll(new GearTooltipBuilder(gearItem).getTooltipLines());
            event.setTooltips(tooltips);
        }
    }
}
