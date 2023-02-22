package com.wynntils.features.user.inventory;

import com.wynntils.core.config.Config;
import com.wynntils.core.features.UserFeature;
import com.wynntils.core.features.properties.FeatureCategory;
import com.wynntils.core.features.properties.FeatureInfo;
import com.wynntils.mc.event.ContainerClickEvent;
import com.wynntils.utils.mc.KeyboardUtils;
import com.wynntils.utils.mc.LoreUtils;
import com.wynntils.utils.mc.McUtils;
import com.wynntils.utils.wynn.ContainerUtils;
import net.minecraft.ChatFormatting;
import net.minecraft.world.inventory.AbstractContainerMenu;
import net.minecraft.world.inventory.ClickType;
import net.minecraft.world.item.ItemStack;
import net.minecraftforge.eventbus.api.SubscribeEvent;

@FeatureInfo(category = FeatureCategory.INVENTORY)
public class BulkBuyFeature extends UserFeature {
    @Config
    public int bulkBuyAmount = 4;

    @SubscribeEvent
    public void onSlotClicked(ContainerClickEvent e) {
        if (!KeyboardUtils.isShiftDown()) return;

        if (e.getContainerId() != McUtils.player().containerMenu.containerId) return;
        AbstractContainerMenu container = McUtils.player().containerMenu;

        if (!isBulkBuyable(container, e.getItemStack())) return;

        if (e.getClickType() == ClickType.QUICK_MOVE) { // Shift + Left Click
            for (int i = 1; i < bulkBuyAmount; i++) {
                ContainerUtils.clickOnSlot(e.getSlotNum(), container.containerId, 10, container.getItems());
            }
        }
    }

    private boolean isBulkBuyable(AbstractContainerMenu menu, ItemStack toBuy) {
        String title = menu.getSlot(4).getItem().getHoverName().getString();

        return title.startsWith(ChatFormatting.GREEN.toString())
                && title.endsWith(" Shop")
                && LoreUtils.getStringLore(toBuy).contains("§6Price:");
    }
}
