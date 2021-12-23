package com.wynntils.features;

import com.wynntils.framework.Subscriber;
import com.wynntils.framework.feature.Feature;
import com.wynntils.framework.feature.GameplayImpact;
import com.wynntils.framework.feature.PerformanceImpact;
import com.wynntils.mc.event.InventoryRenderEvent;
import com.wynntils.utils.ItemUtils;
import net.minecraft.ChatFormatting;
import net.minecraft.world.inventory.Slot;
import net.minecraft.world.item.ItemStack;
import net.minecraft.world.item.Items;

import java.util.List;

public class SoulPointTimerFeature extends Feature {
    @Subscriber
    public void onInventoryRender(InventoryRenderEvent e) {
        Slot hoveredSlot = e.getHoveredSlot();
        if (hoveredSlot == null || !hoveredSlot.hasItem()) return;

        ItemStack stack = hoveredSlot.getItem();

        if (stack.getItem() != Items.NETHER_STAR && stack.getItem() != Items.SNOW) return;

        if (!stack.getDisplayName().getString().contains("Soul Point")) return;

        List<String> lore = ItemUtils.getLore(stack);
        if (!lore.isEmpty()) {
            if (lore.get(lore.size() - 1).contains("Time until next soul point: ")) {
                lore.remove(lore.size() - 1);
                lore.remove(lore.size() - 1);
            }
        }

        lore.add("");
        int secondsUntilSoulPoint =
                900; // FIXME: PlayerInfo.get(InventoryData.class).getTicksToNextSoulPoint() /
        // 20;
        int minutesUntilSoulPoint = secondsUntilSoulPoint / 60;
        secondsUntilSoulPoint %= 60;
        lore.add(
                ChatFormatting.AQUA
                        + "Time until next soul point: "
                        + ChatFormatting.WHITE
                        + minutesUntilSoulPoint
                        + ":"
                        + String.format("%02d", secondsUntilSoulPoint));
        ItemUtils.replaceLore(stack, lore);
    }

    @Override
    public PerformanceImpact getPerformanceImpact() {
        return PerformanceImpact.Medium;
    }

    @Override
    public GameplayImpact getGameplayImpactImpact() {
        return GameplayImpact.Medium;
    }

    @Override
    public int getCreationPriority() {
        return 1;
    }
}
