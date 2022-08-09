/*
 * Copyright © Wynntils 2022.
 * This file is released under AGPLv3. See LICENSE for full license details.
 */
package com.wynntils.features.user.tooltips;

import com.mojang.blaze3d.vertex.PoseStack;
import com.wynntils.core.features.UserFeature;
import com.wynntils.core.features.properties.FeatureInfo;
import com.wynntils.core.features.properties.RegisterKeyBind;
import com.wynntils.core.keybinds.KeyHolder;
import com.wynntils.mc.event.InventoryKeyPressEvent;
import com.wynntils.mc.event.ItemTooltipRenderEvent;
import com.wynntils.mc.event.ScreenClosedEvent;
import com.wynntils.mc.utils.McUtils;
import com.wynntils.wc.custom.item.GearItemStack;
import java.util.List;
import java.util.Optional;
import net.minecraft.client.gui.screens.inventory.InventoryScreen;
import net.minecraft.world.item.ItemStack;
import net.minecraftforge.eventbus.api.SubscribeEvent;
import org.lwjgl.glfw.GLFW;

@FeatureInfo(stability = FeatureInfo.Stability.STABLE, category = "Item Tooltips")
public class ItemCompareFeature extends UserFeature {
    @RegisterKeyBind
    private final KeyHolder toggleCompareModeKeybind =
            new KeyHolder("Compare mode", GLFW.GLFW_KEY_SPACE, "Wynntils", true, () -> {});

    @RegisterKeyBind
    private final KeyHolder compareSelectKeybind =
            new KeyHolder("Select for comparing", GLFW.GLFW_KEY_C, "Wynntils", true, () -> {});

    private static ItemStack COMPARED_ITEM = null;
    private static boolean COMPARE_MODE_ON = false;

    @SubscribeEvent
    public void onInventoryRenderEvent(ItemTooltipRenderEvent.Post event) {
        if (!(McUtils.mc().screen instanceof InventoryScreen inventoryScreen)) return;
        if (!COMPARE_MODE_ON) return;

        if (inventoryScreen.hoveredSlot != null) {
            ItemStack hoveredItemStack = inventoryScreen.hoveredSlot.getItem();

            if (event.getItemStack() != hoveredItemStack) {
                return;
            }

            if (!(hoveredItemStack instanceof GearItemStack hoveredGearItemStack)) {
                return;
            }

            // No compared item selected, try compare to equipped armor
            if (COMPARED_ITEM == null) {
                List<ItemStack> armorSlots = McUtils.player().getInventory().armor;

                Optional<GearItemStack> matchingArmorItemStack = armorSlots.stream()
                        .filter(itemStack -> itemStack instanceof GearItemStack gItemStack
                                && gItemStack.getItemProfile().getItemInfo().getType()
                                        == hoveredGearItemStack
                                                .getItemProfile()
                                                .getItemInfo()
                                                .getType())
                        .map(itemStack -> (GearItemStack) itemStack)
                        .findFirst();

                if (matchingArmorItemStack.isPresent() && matchingArmorItemStack.get() != hoveredGearItemStack) {
                    GearItemStack gearItemStack = matchingArmorItemStack.get();
                    PoseStack poseStack = event.getPoseStack();
                    poseStack.pushPose();

                    poseStack.translate(0, 0, 300);
                    boolean secondHalf = event.getMouseX() > McUtils.mc().screen.width / 2;

                    if (secondHalf) {
                        int toBeRenderedWidth = McUtils.mc().screen.getTooltipFromItem(gearItemStack).stream()
                                .map(component -> McUtils.mc().font.width(component))
                                .max(Integer::compareTo)
                                .orElse(0);
                        inventoryScreen.renderTooltip(
                                poseStack,
                                gearItemStack,
                                event.getMouseX() - toBeRenderedWidth - 10,
                                event.getMouseY());
                    } else {
                        int hoveredWidth = McUtils.mc().screen.getTooltipFromItem(hoveredGearItemStack).stream()
                                .map(component -> McUtils.mc().font.width(component))
                                .max(Integer::compareTo)
                                .orElse(0);
                        inventoryScreen.renderTooltip(
                                poseStack, gearItemStack, event.getMouseX() + hoveredWidth + 10, event.getMouseY());
                    }

                    poseStack.popPose();
                }
            }
        }
    }

    @SubscribeEvent
    public void onScreenClose(ScreenClosedEvent event) {
        COMPARE_MODE_ON = false;
    }

    @SubscribeEvent
    public void onInventoryKeyPress(InventoryKeyPressEvent event) {
        if (toggleCompareModeKeybind.getKeybind().matches(event.getKeyCode(), event.getScanCode())) {
            COMPARE_MODE_ON = !COMPARE_MODE_ON;
        } else if (compareSelectKeybind.getKeybind().matches(event.getKeyCode(), event.getScanCode())) {
            if (event.getHoveredSlot() == null) {
                return;
            }

            COMPARED_ITEM = event.getHoveredSlot().getItem();
        }
    }
}
