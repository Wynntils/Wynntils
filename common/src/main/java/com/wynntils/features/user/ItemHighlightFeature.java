/*
 * Copyright © Wynntils 2022.
 * This file is released under AGPLv3. See LICENSE for full license details.
 */
package com.wynntils.features.user;

import com.wynntils.core.config.properties.ConfigOption;
import com.wynntils.core.config.properties.Configurable;
import com.wynntils.core.features.UserFeature;
import com.wynntils.core.features.properties.EventListener;
import com.wynntils.core.features.properties.FeatureInfo;
import com.wynntils.core.features.properties.FeatureInfo.Stability;
import com.wynntils.mc.event.HotbarSlotRenderEvent;
import com.wynntils.mc.event.SlotRenderEvent;
import com.wynntils.mc.utils.RenderUtils;
import com.wynntils.utils.objects.CustomColor;
import com.wynntils.wc.custom.item.render.HighlightedItem;
import com.wynntils.wc.custom.item.render.HotbarHighlightedItem;
import net.minecraft.world.item.ItemStack;
import net.minecraftforge.eventbus.api.SubscribeEvent;

@EventListener
@FeatureInfo(stability = Stability.STABLE)
@Configurable(category = "Inventory")
public class ItemHighlightFeature extends UserFeature {

    @ConfigOption(displayName = "Normal Item Highlight")
    public static boolean normalHighlightEnabled = true;

    @ConfigOption(displayName = "Normal Highlight Color")
    public static CustomColor normalHighlightColor = new CustomColor(255, 255, 255);

    @ConfigOption(displayName = "Unique Item Highlight")
    public static boolean uniqueHighlightEnabled = true;

    @ConfigOption(displayName = "Unique Highlight Color")
    public static CustomColor uniqueHighlightColor = new CustomColor(255, 255, 0);

    @ConfigOption(displayName = "Rare Item Highlight")
    public static boolean rareHighlightEnabled = true;

    @ConfigOption(displayName = "Rare Highlight Color")
    public static CustomColor rareHighlightColor = new CustomColor(255, 0, 255);

    @ConfigOption(displayName = "Set Item Highlight")
    public static boolean setHighlightEnabled = true;

    @ConfigOption(displayName = "Set Highlight Color")
    public static CustomColor setHighlightColor = new CustomColor(0, 255, 0);

    @ConfigOption(displayName = "Legendary Item Highlight")
    public static boolean legendaryHighlightEnabled = true;

    @ConfigOption(displayName = "Legendary Highlight Color")
    public static CustomColor legendaryHighlightColor = new CustomColor(0, 255, 255);

    @ConfigOption(displayName = "Fabled Item Highlight")
    public static boolean fabledHighlightEnabled = true;

    @ConfigOption(displayName = "Fabled Highlight Color")
    public static CustomColor fabledHighlightColor = new CustomColor(255, 85, 85);

    @ConfigOption(displayName = "Mythic Item Highlight")
    public static boolean mythicHighlightEnabled = true;

    @ConfigOption(displayName = "Mythic Highlight Color")
    public static CustomColor mythicHighlightColor = new CustomColor(76, 0, 76);

    @ConfigOption(displayName = "Crafted Item Highlight")
    public static boolean craftedHighlightEnabled = true;

    @ConfigOption(displayName = "Crafted Highlight Color")
    public static CustomColor craftedHighlightColor = new CustomColor(0, 138, 138);

    @ConfigOption(displayName = "Inventory Item Highlights")
    public static boolean inventoryHighlightEnabled = true;

    @ConfigOption(displayName = "Inventory Highlight Opacity")
    public static float inventoryOpacity = 1f;

    @ConfigOption(displayName = "Hotbar Item Highlights")
    public static boolean hotbarHighlightEnabled = true;

    @ConfigOption(displayName = "Hotbar Highlight Opacity")
    public static float hotbarOpacity = .5f;

    @SubscribeEvent
    public void onRenderSlotPre(SlotRenderEvent.Pre e) {
        if (!inventoryHighlightEnabled) return;

        ItemStack item = e.getSlot().getItem();
        if (!(item instanceof HighlightedItem highlightedItem)) return;

        CustomColor color = highlightedItem.getHighlightColor(e.getScreen(), e.getSlot());
        if (color == CustomColor.NONE) return;
        RenderUtils.drawTexturedRectWithColor(
                RenderUtils.highlight,
                color.withAlpha(inventoryOpacity),
                e.getSlot().x - 1,
                e.getSlot().y - 1,
                200,
                18,
                18,
                256,
                256);
    }

    @SubscribeEvent
    public void onRenderHotbarSlot(HotbarSlotRenderEvent.Pre e) {
        if (!hotbarHighlightEnabled) return;

        ItemStack item = e.getStack();
        if (!(item instanceof HotbarHighlightedItem highlightedItem)) return;

        CustomColor color = highlightedItem.getHotbarColor();
        if (color == CustomColor.NONE) return;
        RenderUtils.drawRect(color.withAlpha(hotbarOpacity), e.getX(), e.getY(), 0, 16, 16);
    }
}
