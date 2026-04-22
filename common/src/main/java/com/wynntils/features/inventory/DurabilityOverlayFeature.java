/*
 * Copyright © Wynntils 2022-2026.
 * This file is released under LGPLv3. See LICENSE for full license details.
 */
package com.wynntils.features.inventory;

import com.wynntils.core.components.Models;
import com.wynntils.core.consumers.features.Feature;
import com.wynntils.core.consumers.features.ProfileDefault;
import com.wynntils.core.persisted.Persisted;
import com.wynntils.core.persisted.config.Category;
import com.wynntils.core.persisted.config.Config;
import com.wynntils.core.persisted.config.ConfigCategory;
import com.wynntils.core.persisted.config.ConfigProfile;
import com.wynntils.core.text.StyledText;
import com.wynntils.mc.event.DataComponentGetEvent;
import com.wynntils.mc.event.HotbarSlotRenderEvent;
import com.wynntils.mc.event.SlotRenderEvent;
import com.wynntils.models.items.properties.DurableItemProperty;
import com.wynntils.utils.colors.CommonColors;
import com.wynntils.utils.colors.CustomColor;
import com.wynntils.utils.render.FontRenderer;
import com.wynntils.utils.render.RenderUtils;
import com.wynntils.utils.render.type.HorizontalAlignment;
import com.wynntils.utils.render.type.TextShadow;
import com.wynntils.utils.render.type.VerticalAlignment;
import com.wynntils.utils.type.CappedValue;
import java.util.Optional;
import net.minecraft.client.gui.GuiGraphics;
import net.minecraft.util.Mth;
import net.minecraft.world.inventory.Slot;
import net.minecraft.world.item.ItemStack;
import net.minecraft.world.item.component.CustomModelData;
import net.neoforged.bus.api.SubscribeEvent;

@ConfigCategory(Category.INVENTORY)
public class DurabilityOverlayFeature extends Feature {
    @Persisted
    private final Config<Boolean> renderDurabilityOverlayInventories = new Config<>(true);

    @Persisted
    private final Config<Boolean> renderDurabilityOverlayHotbar = new Config<>(true);

    @Persisted
    private final Config<DurabilityRenderMode> durabilityRenderMode = new Config<>(DurabilityRenderMode.BAR);

    @Persisted
    private final Config<ColorScheme> colorScheme = new Config<>(ColorScheme.VANILLA);

    @Persisted
    private final Config<CustomColor> fullDurabilityColor = new Config<>(CommonColors.GREEN);

    @Persisted
    private final Config<CustomColor> noDurabilityColor = new Config<>(CommonColors.RED);

    private static final CustomColor VANILLA_DURABILITY_COLOR = CustomColor.fromHexString("00C8FF");

    public DurabilityOverlayFeature() {
        super(new ProfileDefault.Builder()
                .enabledFor(ConfigProfile.DEFAULT, ConfigProfile.NEW_PLAYER, ConfigProfile.LITE)
                .build());
    }

    @SubscribeEvent
    public void onGetModelData(DataComponentGetEvent.CustomModelData event) {
        Optional<DurableItemProperty> durableItemOpt =
                Models.Item.asWynnItemProperty(event.getItemStack(), DurableItemProperty.class);
        if (durableItemOpt.isEmpty()) return;

        CustomModelData itemStackModelData = event.getOriginalValue();

        if (itemStackModelData.floats().size() < 2) return;

        // Remove vanilla durability bar
        itemStackModelData.floats().remove(1);
    }

    @SubscribeEvent
    public void onRenderHotbarSlot(HotbarSlotRenderEvent.Post event) {
        if (!renderDurabilityOverlayHotbar.get()) return;
        drawDurability(event.getGuiGraphics(), event.getItemStack(), event.getX(), event.getY());
    }

    @SubscribeEvent
    public void onRenderSlot(SlotRenderEvent.Post event) {
        if (!renderDurabilityOverlayInventories.get()) return;
        Slot slot = event.getSlot();
        drawDurability(event.getGuiGraphics(), slot.getItem(), slot.x, slot.y);
    }

    private void drawDurability(GuiGraphics guiGraphics, ItemStack itemStack, int slotX, int slotY) {
        switch (durabilityRenderMode.get()) {
            case ARC -> drawDurabilityArc(guiGraphics, itemStack, slotX, slotY);
            case BAR -> drawDurabilityBar(guiGraphics, itemStack, slotX, slotY);
            case PERCENTAGE -> drawDurabilityPercentage(guiGraphics, itemStack, slotX, slotY);
        }
    }

    private void drawDurabilityArc(GuiGraphics guiGraphics, ItemStack itemStack, int slotX, int slotY) {
        Optional<DurableItemProperty> durableItemOpt =
                Models.Item.asWynnItemProperty(itemStack, DurableItemProperty.class);
        if (durableItemOpt.isEmpty()) return;

        CappedValue durability = durableItemOpt.get().getDurability();

        // draw
        RenderUtils.drawArc(
                guiGraphics, getColor(durability).withAlpha(160), slotX, slotY, (float) durability.getProgress(), 6, 8);
    }

    private void drawDurabilityBar(GuiGraphics guiGraphics, ItemStack itemStack, int slotX, int slotY) {
        Optional<DurableItemProperty> durableItemProperty =
                Models.Item.asWynnItemProperty(itemStack, DurableItemProperty.class);
        if (durableItemProperty.isEmpty()) return;

        CappedValue durability = durableItemProperty.get().getDurability();

        if (durability.isAtCap()) return;

        // draw
        int x = slotX + 2;
        int y = slotY + 13;
        RenderUtils.drawRect(guiGraphics, CommonColors.BLACK, x, y, 13, 2);
        RenderUtils.drawRect(
                guiGraphics,
                getColor(durability),
                x,
                y,
                Mth.clamp(Math.round(13 * (float) durability.getProgress()), 0, 13),
                1);
    }

    // Inspiration taken from https://github.com/GTNewHorizons/DuraDisplay
    private void drawDurabilityPercentage(GuiGraphics guiGraphics, ItemStack itemStack, int slotX, int slotY) {
        Optional<DurableItemProperty> durableItemOpt =
                Models.Item.asWynnItemProperty(itemStack, DurableItemProperty.class);
        if (durableItemOpt.isEmpty()) return;

        CappedValue durability = durableItemOpt.get().getDurability();

        FontRenderer.getInstance()
                .renderText(
                        guiGraphics,
                        StyledText.fromString(Math.round((float) durability.getProgress() * 100) + "%"),
                        (float) slotX + 8,
                        (float) slotY + 16,
                        getColor(durability),
                        HorizontalAlignment.CENTER,
                        VerticalAlignment.BOTTOM,
                        TextShadow.NORMAL,
                        0.5f);
    }

    private CustomColor getColor(CappedValue durability) {
        return switch (colorScheme.get()) {
            case ColorScheme.VANILLA -> VANILLA_DURABILITY_COLOR;
            case ColorScheme.WYNNTILS ->
                CustomColor.fromHSV(Math.max(0f, (float) durability.getProgress()) / 3f, 1f, 1f, 1f);
            case ColorScheme.CUSTOM -> {
                final float progress = (float) durability.getProgress();
                final CustomColor full = fullDurabilityColor.get();
                final CustomColor no = noDurabilityColor.get();
                yield new CustomColor(
                        Mth.lerpInt(progress, no.r(), full.r()),
                        Mth.lerpInt(progress, no.g(), full.g()),
                        Mth.lerpInt(progress, no.b(), full.b()),
                        Mth.lerpInt(progress, no.a(), full.a()));
            }
        };
    }

    private enum ColorScheme {
        VANILLA,
        WYNNTILS,
        CUSTOM
    }

    private enum DurabilityRenderMode {
        ARC,
        BAR,
        PERCENTAGE
    }
}
