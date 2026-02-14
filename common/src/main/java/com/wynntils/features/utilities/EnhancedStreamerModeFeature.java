/*
 * Copyright © Wynntils 2026.
 * This file is released under LGPLv3. See LICENSE for full license details.
 */
package com.wynntils.features.utilities;

import com.wynntils.core.components.Models;
import com.wynntils.core.consumers.features.Feature;
import com.wynntils.core.consumers.features.ProfileDefault;
import com.wynntils.core.persisted.Persisted;
import com.wynntils.core.persisted.config.Category;
import com.wynntils.core.persisted.config.Config;
import com.wynntils.core.persisted.config.ConfigCategory;
import com.wynntils.core.text.StyledText;
import com.wynntils.mc.event.HotbarSlotRenderEvent;
import com.wynntils.mc.event.ItemTooltipRenderEvent;
import com.wynntils.mc.event.SlotRenderEvent;
import com.wynntils.models.items.properties.GearTypeItemProperty;
import com.wynntils.utils.colors.CommonColors;
import com.wynntils.utils.mc.KeyboardUtils;
import com.wynntils.utils.mc.McUtils;
import com.wynntils.utils.render.FontRenderer;
import com.wynntils.utils.render.type.HorizontalAlignment;
import com.wynntils.utils.render.type.TextShadow;
import com.wynntils.utils.render.type.VerticalAlignment;
import java.util.List;
import java.util.Optional;
import net.minecraft.client.gui.GuiGraphics;
import net.minecraft.client.gui.screens.inventory.InventoryScreen;
import net.minecraft.client.renderer.RenderPipelines;
import net.minecraft.network.chat.Component;
import net.minecraft.resources.Identifier;
import net.minecraft.world.item.ItemStack;
import net.neoforged.bus.api.EventPriority;
import net.neoforged.bus.api.ICancellableEvent;
import net.neoforged.bus.api.SubscribeEvent;
import org.lwjgl.glfw.GLFW;

@ConfigCategory(Category.UTILITIES)
public class EnhancedStreamerModeFeature extends Feature {
    private static final Integer INVENTORY_FIRST_HOTBAR_SLOT = 36;
    private static final Integer LAST_GEAR_SLOT = 12;

    @Persisted
    private final Config<Boolean> hideHotbarWeapons = new Config<>(false);

    @Persisted
    private final Config<Boolean> hideEquippedGear = new Config<>(false);

    @Persisted
    private final Config<Boolean> hideGearTooltips = new Config<>(false);

    public EnhancedStreamerModeFeature() {
        super(ProfileDefault.DISABLED);
    }

    // Needs to run before item highlights
    @SubscribeEvent(priority = EventPriority.HIGH)
    public void onRenderHotbarSlot(HotbarSlotRenderEvent.Pre event) {
        if (!Models.StreamerMode.isInStream()) return;
        if (!hideHotbarWeapons.get()) return;

        handleWeapon(event, event.getItemStack(), event.getGuiGraphics(), event.getX(), event.getY());
    }

    @SubscribeEvent(priority = EventPriority.HIGHEST)
    public void onRenderSlot(SlotRenderEvent.Pre event) {
        if (!Models.StreamerMode.isInStream()) return;

        ItemStack itemStack = event.getSlot().getItem();
        if (!McUtils.inventory().contains(itemStack)) return;

        // When in a container that shows inventory
        if (!(McUtils.screen() instanceof InventoryScreen) && hideEquippedGear.get()) {
            if (event.getSlot().index >= 54 && event.getSlot().index <= 57) {
                Optional<GearTypeItemProperty> gearItemOpt =
                        Models.Item.asWynnItemProperty(itemStack, GearTypeItemProperty.class);
                if (gearItemOpt.isEmpty()) return;
                if (gearItemOpt.get().getGearType().isAccessory()) {
                    event.setCanceled(true);
                    return;
                }
            }
        }

        if (event.getSlot().index >= INVENTORY_FIRST_HOTBAR_SLOT && hideHotbarWeapons.get()) {
            handleWeapon(event, itemStack, event.getGuiGraphics(), event.getSlot().x, event.getSlot().y);
        } else if (event.getSlot().index <= LAST_GEAR_SLOT && hideEquippedGear.get()) {
            event.setCanceled(true);

            // For armor we can render the no item texture just so it doesn't look off
            Identifier noItemIcon = event.getSlot().getNoItemIcon();
            if (noItemIcon != null) {
                event.getGuiGraphics()
                        .blitSprite(
                                RenderPipelines.GUI_TEXTURED, noItemIcon, event.getSlot().x, event.getSlot().y, 16, 16);
            }
        }
    }

    @SubscribeEvent(priority = EventPriority.LOW)
    public void onRenderTooltip(ItemTooltipRenderEvent.Pre event) {
        if (!Models.StreamerMode.isInStream()) return;
        if (!hideGearTooltips.get()) return;
        if (KeyboardUtils.isKeyDown(GLFW.GLFW_KEY_SPACE)) return;
        if (!McUtils.inventory().contains(event.getItemStack())) return;

        Optional<GearTypeItemProperty> gearItemOpt =
                Models.Item.asWynnItemProperty(event.getItemStack(), GearTypeItemProperty.class);
        if (gearItemOpt.isEmpty()) return;

        event.setTooltips(List.of(Component.translatable("feature.wynntils.enhancedStreamerMode.tooltipHidden")));
    }

    private void handleWeapon(ICancellableEvent event, ItemStack itemStack, GuiGraphics guiGraphics, int x, int y) {
        Optional<GearTypeItemProperty> gearItemOpt =
                Models.Item.asWynnItemProperty(itemStack, GearTypeItemProperty.class);
        if (gearItemOpt.isEmpty()) return;
        if (!gearItemOpt.get().getGearType().isWeapon()) return;

        event.setCanceled(true);

        FontRenderer.getInstance()
                .renderAlignedTextInBox(
                        guiGraphics,
                        StyledText.fromString(
                                gearItemOpt.get().getGearType().name().substring(0, 1)),
                        x,
                        x + 16,
                        y,
                        y + 16,
                        16,
                        CommonColors.WHITE,
                        HorizontalAlignment.CENTER,
                        VerticalAlignment.MIDDLE,
                        TextShadow.OUTLINE);
    }
}
