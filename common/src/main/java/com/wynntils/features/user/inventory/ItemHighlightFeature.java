/*
 * Copyright © Wynntils 2022.
 * This file is released under AGPLv3. See LICENSE for full license details.
 */
package com.wynntils.features.user.inventory;

import com.wynntils.core.components.Models;
import com.wynntils.core.config.Config;
import com.wynntils.core.features.UserFeature;
import com.wynntils.core.features.properties.FeatureCategory;
import com.wynntils.core.features.properties.FeatureInfo;
import com.wynntils.core.features.properties.FeatureInfo.Stability;
import com.wynntils.gui.render.RenderUtils;
import com.wynntils.gui.render.Texture;
import com.wynntils.mc.event.HotbarSlotRenderEvent;
import com.wynntils.mc.event.SlotRenderEvent;
import com.wynntils.mc.objects.CustomColor;
import com.wynntils.wynn.handleditems.WynnItem;
import com.wynntils.wynn.handleditems.WynnItemCache;
import com.wynntils.wynn.handleditems.items.game.IngredientItem;
import com.wynntils.wynn.handleditems.items.game.MaterialItem;
import com.wynntils.wynn.handleditems.items.game.PowderItem;
import com.wynntils.wynn.handleditems.items.gui.CosmeticItem;
import com.wynntils.wynn.handleditems.properties.GearTierItemProperty;
import java.util.Optional;
import net.minecraft.world.item.ItemStack;
import net.minecraftforge.eventbus.api.EventPriority;
import net.minecraftforge.eventbus.api.SubscribeEvent;

@FeatureInfo(stability = Stability.STABLE, category = FeatureCategory.INVENTORY)
public class ItemHighlightFeature extends UserFeature {
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
        Optional<WynnItem> wynnItemOpt = Models.Item.asWynnItem(item, WynnItem.class);
        if (wynnItemOpt.isEmpty()) return CustomColor.NONE;
        WynnItem wynnItem = wynnItemOpt.get();

        HighlightInfo highlight =
                wynnItem.getCache().getOrCalculate(WynnItemCache.HIGHLIGHT_KEY, () -> calculateHighlightInfo(wynnItem));
        if (highlight == null) return CustomColor.NONE;

        boolean contextEnabled = hotbarHighlight ? highlight.isHotbarHighlight() : highlight.isInventoryHighlight();
        if (!highlight.isHighlightEnabled() || !contextEnabled) return CustomColor.NONE;

        return highlight.getHighlightColor();
    }

    private HighlightInfo calculateHighlightInfo(WynnItem wynnItem) {
        if (wynnItem instanceof IngredientItem ingredientItem) {
            return new IngredientHighlight(ingredientItem);
        }
        if (wynnItem instanceof MaterialItem materialItem) {
            return new MaterialHighlight(materialItem);
        }
        if (wynnItem instanceof PowderItem powderItem) {
            return new PowderHighlight(powderItem);
        }
        if (wynnItem instanceof CosmeticItem cosmeticItem) {
            return new CosmeticHighlight(cosmeticItem);
        }
        if (wynnItem instanceof GearTierItemProperty gearItem) {
            return new GearHighlight(gearItem);
        }

        return null;
    }

    private interface HighlightInfo {
        CustomColor getHighlightColor();

        boolean isHighlightEnabled();

        /** Whether this highlight should be shown in inventories */
        default boolean isInventoryHighlight() {
            return true;
        }

        /** Whether this highlight should be shown in the hotbar */
        default boolean isHotbarHighlight() {
            return true;
        }
    }

    private class IngredientHighlight implements HighlightInfo {
        private final IngredientItem item;

        private IngredientHighlight(IngredientItem item) {
            this.item = item;
        }

        @Override
        public CustomColor getHighlightColor() {
            return switch (item.getQualityTier()) {
                case 0 -> zeroStarIngredientHighlightColor;
                case 1 -> oneStarIngredientHighlightColor;
                case 2 -> twoStarIngredientHighlightColor;
                case 3 -> threeStarIngredientHighlightColor;
                default -> CustomColor.NONE;
            };
        }

        @Override
        public boolean isHighlightEnabled() {
            return switch (item.getQualityTier()) {
                case 0 -> zeroStarIngredientHighlightEnabled;
                case 1 -> oneStarIngredientHighlightEnabled;
                case 2 -> twoStarIngredientHighlightEnabled;
                case 3 -> threeStarIngredientHighlightEnabled;
                default -> false;
            };
        }
    }

    private class MaterialHighlight implements HighlightInfo {
        private final MaterialItem item;

        private MaterialHighlight(MaterialItem item) {
            this.item = item;
        }

        @Override
        public CustomColor getHighlightColor() {
            return switch (item.getQualityTier()) {
                case 1 -> oneStarMaterialHighlightColor;
                case 2 -> twoStarMaterialHighlightColor;
                case 3 -> threeStarMaterialHighlightColor;
                default -> CustomColor.NONE;
            };
        }

        @Override
        public boolean isHighlightEnabled() {
            return switch (item.getQualityTier()) {
                case 1 -> oneStarMaterialHighlightEnabled;
                case 2 -> twoStarMaterialHighlightEnabled;
                case 3 -> threeStarMaterialHighlightEnabled;
                default -> false; // should not happen
            };
        }
    }

    private class PowderHighlight implements HighlightInfo {
        private final PowderItem item;

        private PowderHighlight(PowderItem item) {
            this.item = item;
        }

        @Override
        public boolean isHighlightEnabled() {
            return powderHighlightEnabled;
        }

        @Override
        public CustomColor getHighlightColor() {
            return item.getPowderProfile().element().getColor();
        }
    }

    private class CosmeticHighlight implements HighlightInfo {
        private final CosmeticItem item;

        private CosmeticHighlight(CosmeticItem item) {
            this.item = item;
        }

        @Override
        public boolean isHighlightEnabled() {
            return cosmeticHighlightEnabled;
        }

        @Override
        public CustomColor getHighlightColor() {
            return item.getHighlightColor();
        }
    }

    private class GearHighlight implements HighlightInfo {
        private final GearTierItemProperty item;

        private GearHighlight(GearTierItemProperty item) {
            this.item = item;
        }

        @Override
        public boolean isHighlightEnabled() {
            return switch (item.getGearTier()) {
                case NORMAL -> normalHighlightEnabled;
                case UNIQUE -> uniqueHighlightEnabled;
                case RARE -> rareHighlightEnabled;
                case SET -> setHighlightEnabled;
                case LEGENDARY -> legendaryHighlightEnabled;
                case FABLED -> fabledHighlightEnabled;
                case MYTHIC -> mythicHighlightEnabled;
                default -> false;
            };
        }

        @Override
        public CustomColor getHighlightColor() {
            return switch (item.getGearTier()) {
                case NORMAL -> normalHighlightColor;
                case UNIQUE -> uniqueHighlightColor;
                case RARE -> rareHighlightColor;
                case SET -> setHighlightColor;
                case LEGENDARY -> legendaryHighlightColor;
                case FABLED -> fabledHighlightColor;
                case MYTHIC -> mythicHighlightColor;
                default -> CustomColor.NONE;
            };
        }
    }
}
