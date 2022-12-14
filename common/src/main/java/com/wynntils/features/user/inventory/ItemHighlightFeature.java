/*
 * Copyright © Wynntils 2022.
 * This file is released under AGPLv3. See LICENSE for full license details.
 */
package com.wynntils.features.user.inventory;

import com.wynntils.core.config.Config;
import com.wynntils.core.features.UserFeature;
import com.wynntils.core.features.properties.FeatureCategory;
import com.wynntils.core.features.properties.FeatureInfo;
import com.wynntils.core.features.properties.FeatureInfo.Stability;
import com.wynntils.core.managers.Managers;
import com.wynntils.core.managers.Model;
import com.wynntils.gui.render.RenderUtils;
import com.wynntils.gui.render.Texture;
import com.wynntils.mc.event.HotbarSlotRenderEvent;
import com.wynntils.mc.event.SlotRenderEvent;
import com.wynntils.mc.objects.CustomColor;
import com.wynntils.wynn.item.WynnItemStack;
import com.wynntils.wynn.item.properties.ItemProperty;
import com.wynntils.wynn.item.properties.type.HighlightProperty;
import java.util.List;
import net.minecraft.world.item.ItemStack;
import net.minecraftforge.eventbus.api.EventPriority;
import net.minecraftforge.eventbus.api.SubscribeEvent;

@FeatureInfo(stability = Stability.STABLE, category = FeatureCategory.INVENTORY)
public class ItemHighlightFeature extends UserFeature {
    public static ItemHighlightFeature INSTANCE;

    @Config
    public boolean normalHighlightEnabled = true;

    @Config
    public CustomColor normalHighlightColor = new CustomColor(255, 255, 255);

    @Config
    public boolean uniqueHighlightEnabled = true;

    @Config
    public CustomColor uniqueHighlightColor = new CustomColor(255, 255, 0);

    @Config
    public boolean rareHighlightEnabled = true;

    @Config
    public CustomColor rareHighlightColor = new CustomColor(255, 0, 255);

    @Config
    public boolean setHighlightEnabled = true;

    @Config
    public CustomColor setHighlightColor = new CustomColor(0, 255, 0);

    @Config
    public boolean legendaryHighlightEnabled = true;

    @Config
    public CustomColor legendaryHighlightColor = new CustomColor(0, 255, 255);

    @Config
    public boolean fabledHighlightEnabled = true;

    @Config
    public CustomColor fabledHighlightColor = new CustomColor(255, 85, 85);

    @Config
    public boolean mythicHighlightEnabled = true;

    @Config
    public CustomColor mythicHighlightColor = new CustomColor(76, 0, 76);

    @Config
    public boolean craftedHighlightEnabled = true;

    @Config
    public CustomColor craftedHighlightColor = new CustomColor(0, 138, 138);

    @Config
    public boolean zeroStarIngredientHighlightEnabled = true;

    @Config
    public CustomColor zeroStarIngredientHighlightColor = new CustomColor(102, 102, 102);

    @Config
    public boolean oneStarIngredientHighlightEnabled = true;

    @Config
    public CustomColor oneStarIngredientHighlightColor = new CustomColor(255, 247, 153);

    @Config
    public boolean twoStarIngredientHighlightEnabled = true;

    @Config
    public CustomColor twoStarIngredientHighlightColor = new CustomColor(255, 255, 0);

    @Config
    public boolean threeStarIngredientHighlightEnabled = true;

    @Config
    public CustomColor threeStarIngredientHighlightColor = new CustomColor(230, 77, 0);

    @Config
    public boolean oneStarMaterialHighlightEnabled = true;

    @Config
    public CustomColor oneStarMaterialHighlightColor = new CustomColor(255, 247, 153);

    @Config
    public boolean twoStarMaterialHighlightEnabled = true;

    @Config
    public CustomColor twoStarMaterialHighlightColor = new CustomColor(255, 255, 0);

    @Config
    public boolean threeStarMaterialHighlightEnabled = true;

    @Config
    public CustomColor threeStarMaterialHighlightColor = new CustomColor(230, 77, 0);

    @Config
    public boolean cosmeticHighlightEnabled = true;

    @Config
    public boolean powderHighlightEnabled = true;

    @Config
    public boolean emeraldPouchHighlightEnabled = true;

    @Config
    public boolean inventoryHighlightEnabled = true;

    @Config
    public float inventoryOpacity = 1f;

    @Config
    public boolean hotbarHighlightEnabled = true;

    @Config
    public float hotbarOpacity = .5f;

    @Override
    public List<Class<? extends Model>> getModelDependencies() {
        return Managers.ITEM_STACK_TRANSFORM.HIGHLIGHT_PROPERTIES;
    }

    @SubscribeEvent(priority = EventPriority.HIGHEST)
    public void onRenderSlot(SlotRenderEvent.Pre e) {
        if (!inventoryHighlightEnabled) return;

        CustomColor color = getHighlightColor(e.getSlot().getItem(), false);
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

        CustomColor color = getHighlightColor(e.getStack(), true);
        if (color == CustomColor.NONE) return;

        RenderUtils.drawRect(color.withAlpha(hotbarOpacity), e.getX(), e.getY(), 0, 16, 16);
    }

    private CustomColor getHighlightColor(ItemStack item, boolean hotbarHighlight) {
        if (!(item instanceof WynnItemStack wynnItem)) return CustomColor.NONE;

        if (!wynnItem.hasProperty(ItemProperty.HIGHLIGHT)) return CustomColor.NONE;
        HighlightProperty highlight = wynnItem.getProperty(ItemProperty.HIGHLIGHT);

        boolean contextEnabled = hotbarHighlight ? highlight.isHotbarHighlight() : highlight.isInventoryHighlight();
        if (!highlight.isHighlightEnabled() || !contextEnabled) return CustomColor.NONE;

        return highlight.getHighlightColor();
    }
}
