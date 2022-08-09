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
import com.wynntils.mc.event.SlotRenderEvent;
import com.wynntils.mc.render.RenderUtils;
import com.wynntils.mc.utils.McUtils;
import com.wynntils.utils.objects.CommonColors;
import com.wynntils.wc.custom.item.GearItemStack;
import java.util.List;
import java.util.Optional;
import net.minecraft.client.gui.screens.Screen;
import net.minecraft.client.gui.screens.inventory.AbstractContainerScreen;
import net.minecraft.world.inventory.Slot;
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

    private static GearItemStack COMPARED_ITEM = null;
    private static boolean COMPARE_MODE_ON = false;

    @SubscribeEvent
    public void onRenderSlot(SlotRenderEvent.Pre event) {
        Slot slot = event.getSlot();
        if (slot.getItem() == COMPARED_ITEM) {
            RenderUtils.drawArc(CommonColors.LIGHT_BLUE, slot.x, slot.y, 200, 1, 6, 8);
        }
    }

    @SubscribeEvent
    public void onItemTooltipRenderEvent(ItemTooltipRenderEvent.Post event) {
        if (!(McUtils.mc().screen instanceof AbstractContainerScreen<?> abstractContainerScreen)) return;
        if (!COMPARE_MODE_ON) return;

        if (abstractContainerScreen.hoveredSlot != null) {
            ItemStack hoveredItemStack = abstractContainerScreen.hoveredSlot.getItem();

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

                matchingArmorItemStack.ifPresent(gearItemStack -> handleCompareTo(
                        new PoseStack(),
                        event.getMouseX(),
                        event.getMouseY(),
                        abstractContainerScreen,
                        hoveredGearItemStack,
                        gearItemStack));
            } else {
                handleCompareTo(
                        new PoseStack(),
                        event.getMouseX(),
                        event.getMouseY(),
                        abstractContainerScreen,
                        hoveredGearItemStack,
                        COMPARED_ITEM);
            }
        }
    }

    private static void handleCompareTo(
            PoseStack poseStack,
            int mouseX,
            int mouseY,
            AbstractContainerScreen<?> inventoryScreen,
            GearItemStack hoveredGearItemStack,
            GearItemStack matchingArmorItemStack) {
        if (hoveredGearItemStack == matchingArmorItemStack) return;

        Screen screen = McUtils.mc().screen;

        if (screen == null) return;

        poseStack.pushPose();

        poseStack.translate(0, 0, 300);

        int toBeRenderedWidth = screen.getTooltipFromItem(matchingArmorItemStack).stream()
                .map(component -> McUtils.mc().font.width(component))
                .max(Integer::compareTo)
                .orElse(0);

        int hoveredWidth = screen.getTooltipFromItem(hoveredGearItemStack).stream()
                .map(component -> McUtils.mc().font.width(component))
                .max(Integer::compareTo)
                .orElse(0);

        if (mouseX + toBeRenderedWidth + hoveredWidth > screen.width) {
            inventoryScreen.renderTooltip(poseStack, matchingArmorItemStack, mouseX - toBeRenderedWidth - 10, mouseY);
        } else {
            inventoryScreen.renderTooltip(poseStack, matchingArmorItemStack, mouseX + hoveredWidth + 10, mouseY);
        }

        poseStack.popPose();
    }

    @SubscribeEvent
    public void onScreenClose(ScreenClosedEvent event) {
        COMPARE_MODE_ON = false;
    }

    @SubscribeEvent
    public void onInventoryKeyPress(InventoryKeyPressEvent event) {
        if (toggleCompareModeKeybind.getKeybind().matches(event.getKeyCode(), event.getScanCode())) {
            if (COMPARED_ITEM != null) {
                COMPARED_ITEM = null;
                COMPARE_MODE_ON = true;
            } else {
                COMPARE_MODE_ON = !COMPARE_MODE_ON;
            }
        } else if (compareSelectKeybind.getKeybind().matches(event.getKeyCode(), event.getScanCode())) {
            if (event.getHoveredSlot() == null) {
                return;
            }

            if (event.getHoveredSlot().getItem() instanceof GearItemStack gearItemStack) {
                if (COMPARED_ITEM == gearItemStack) {
                    COMPARED_ITEM = null;
                    COMPARE_MODE_ON = false;
                } else {
                    COMPARED_ITEM = gearItemStack;
                    COMPARE_MODE_ON = true;
                }
            }
        }
    }
}
