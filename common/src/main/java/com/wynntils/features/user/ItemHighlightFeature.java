/*
 * Copyright © Wynntils 2022.
 * This file is released under AGPLv3. See LICENSE for full license details.
 */
package com.wynntils.features.user;

import com.wynntils.core.config.Config;
import com.wynntils.core.features.UserFeature;
import com.wynntils.core.features.properties.EventListener;
import com.wynntils.core.features.properties.FeatureInfo;
import com.wynntils.core.features.properties.FeatureInfo.Stability;
import com.wynntils.mc.event.HotbarSlotRenderEvent;
import com.wynntils.mc.event.SlotRenderEvent;
import com.wynntils.mc.render.RenderUtils;
import com.wynntils.mc.render.Texture;
import com.wynntils.utils.objects.CustomColor;
import com.wynntils.wc.custom.item.render.HighlightedItem;
import com.wynntils.wc.custom.item.render.HotbarHighlightedItem;
import net.minecraft.world.item.ItemStack;
import net.minecraftforge.eventbus.api.EventPriority;
import net.minecraftforge.eventbus.api.SubscribeEvent;

@EventListener
@FeatureInfo(stability = Stability.STABLE, category = "Inventory")
public class ItemHighlightFeature extends UserFeature {

    @Config(displayName = "Normal Item Highlight")
    public static boolean normalHighlightEnabled = true;

    @Config(displayName = "Normal Highlight Color")
    public static CustomColor normalHighlightColor = new CustomColor(255, 255, 255);

    @Config(displayName = "Unique Item Highlight")
    public static boolean uniqueHighlightEnabled = true;

    @Config(displayName = "Unique Highlight Color")
    public static CustomColor uniqueHighlightColor = new CustomColor(255, 255, 0);

    @Config(displayName = "Rare Item Highlight")
    public static boolean rareHighlightEnabled = true;

    @Config(displayName = "Rare Highlight Color")
    public static CustomColor rareHighlightColor = new CustomColor(255, 0, 255);

    @Config(displayName = "Set Item Highlight")
    public static boolean setHighlightEnabled = true;

    @Config(displayName = "Set Highlight Color")
    public static CustomColor setHighlightColor = new CustomColor(0, 255, 0);

    @Config(displayName = "Legendary Item Highlight")
    public static boolean legendaryHighlightEnabled = true;

    @Config(displayName = "Legendary Highlight Color")
    public static CustomColor legendaryHighlightColor = new CustomColor(0, 255, 255);

    @Config(displayName = "Fabled Item Highlight")
    public static boolean fabledHighlightEnabled = true;

    @Config(displayName = "Fabled Highlight Color")
    public static CustomColor fabledHighlightColor = new CustomColor(255, 85, 85);

    @Config(displayName = "Mythic Item Highlight")
    public static boolean mythicHighlightEnabled = true;

    @Config(displayName = "Mythic Highlight Color")
    public static CustomColor mythicHighlightColor = new CustomColor(76, 0, 76);

    @Config(displayName = "Crafted Item Highlight")
    public static boolean craftedHighlightEnabled = true;

    @Config(displayName = "Crafted Highlight Color")
    public static CustomColor craftedHighlightColor = new CustomColor(0, 138, 138);

    @Config(displayName = "Inventory Item Highlights")
    public static boolean inventoryHighlightEnabled = true;

    @Config(displayName = "Inventory Highlight Opacity")
    public static float inventoryOpacity = 1f;

    @Config(displayName = "Hotbar Item Highlights")
    public static boolean hotbarHighlightEnabled = true;

    @Config(displayName = "Hotbar Highlight Opacity")
    public static float hotbarOpacity = .5f;

    @SubscribeEvent(priority = EventPriority.HIGHEST)
    public void onRenderSlot(SlotRenderEvent.Pre e) {
        if (!inventoryHighlightEnabled) return;

        ItemStack item = e.getSlot().getItem();
        if (!(item instanceof HighlightedItem highlightedItem)) return;

        CustomColor color = highlightedItem.getHighlightColor(e.getScreen(), e.getSlot());
        if (color == CustomColor.NONE) return;
        RenderUtils.drawTexturedRectWithColor(
                Texture.HIGHLIGHT.resource(),
                color.withAlpha(inventoryOpacity),
                e.getSlot().x - 1,
                e.getSlot().y - 1,
                200,
                18,
                18,
                Texture.HIGHLIGHT.width(),
                Texture.HIGHLIGHT.height());
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
