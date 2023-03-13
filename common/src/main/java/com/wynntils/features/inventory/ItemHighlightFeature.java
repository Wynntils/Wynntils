/*
 * Copyright © Wynntils 2022.
 * This file is released under AGPLv3. See LICENSE for full license details.
 */
package com.wynntils.features.inventory;

import com.wynntils.core.components.Models;
import com.wynntils.core.config.Category;
import com.wynntils.core.config.ConfigCategory;
import com.wynntils.core.config.ConfigInfo;
import com.wynntils.core.features.UserFeature;
import com.wynntils.mc.event.HotbarSlotRenderEvent;
import com.wynntils.mc.event.SlotRenderEvent;
import com.wynntils.models.items.WynnItem;
import com.wynntils.models.items.WynnItemCache;
import com.wynntils.models.items.items.game.EmeraldPouchItem;
import com.wynntils.models.items.items.game.IngredientItem;
import com.wynntils.models.items.items.game.MaterialItem;
import com.wynntils.models.items.items.game.PowderItem;
import com.wynntils.models.items.items.gui.CosmeticItem;
import com.wynntils.models.items.properties.GearTierItemProperty;
import com.wynntils.utils.colors.CustomColor;
import com.wynntils.utils.render.RenderUtils;
import com.wynntils.utils.render.Texture;
import java.util.Optional;
import net.minecraft.ChatFormatting;
import net.minecraft.world.item.ItemStack;
import net.minecraftforge.eventbus.api.EventPriority;
import net.minecraftforge.eventbus.api.SubscribeEvent;

@ConfigCategory(Category.INVENTORY)
public class ItemHighlightFeature extends UserFeature {
    @ConfigInfo
    public boolean normalHighlightEnabled = true;

    @ConfigInfo
    public CustomColor normalHighlightColor = new CustomColor(255, 255, 255);

    @ConfigInfo
    public boolean uniqueHighlightEnabled = true;

    @ConfigInfo
    public CustomColor uniqueHighlightColor = new CustomColor(255, 255, 0);

    @ConfigInfo
    public boolean rareHighlightEnabled = true;

    @ConfigInfo
    public CustomColor rareHighlightColor = new CustomColor(255, 0, 255);

    @ConfigInfo
    public boolean setHighlightEnabled = true;

    @ConfigInfo
    public CustomColor setHighlightColor = new CustomColor(0, 255, 0);

    @ConfigInfo
    public boolean legendaryHighlightEnabled = true;

    @ConfigInfo
    public CustomColor legendaryHighlightColor = new CustomColor(0, 255, 255);

    @ConfigInfo
    public boolean fabledHighlightEnabled = true;

    @ConfigInfo
    public CustomColor fabledHighlightColor = new CustomColor(255, 85, 85);

    @ConfigInfo
    public boolean mythicHighlightEnabled = true;

    @ConfigInfo
    public CustomColor mythicHighlightColor = new CustomColor(76, 0, 76);

    @ConfigInfo
    public boolean craftedHighlightEnabled = true;

    @ConfigInfo
    public CustomColor craftedHighlightColor = new CustomColor(0, 138, 138);

    @ConfigInfo
    public boolean zeroStarIngredientHighlightEnabled = true;

    @ConfigInfo
    public CustomColor zeroStarIngredientHighlightColor = new CustomColor(102, 102, 102);

    @ConfigInfo
    public boolean oneStarIngredientHighlightEnabled = true;

    @ConfigInfo
    public CustomColor oneStarIngredientHighlightColor = new CustomColor(255, 247, 153);

    @ConfigInfo
    public boolean twoStarIngredientHighlightEnabled = true;

    @ConfigInfo
    public CustomColor twoStarIngredientHighlightColor = new CustomColor(255, 255, 0);

    @ConfigInfo
    public boolean threeStarIngredientHighlightEnabled = true;

    @ConfigInfo
    public CustomColor threeStarIngredientHighlightColor = new CustomColor(230, 77, 0);

    @ConfigInfo
    public boolean oneStarMaterialHighlightEnabled = true;

    @ConfigInfo
    public CustomColor oneStarMaterialHighlightColor = new CustomColor(255, 247, 153);

    @ConfigInfo
    public boolean twoStarMaterialHighlightEnabled = true;

    @ConfigInfo
    public CustomColor twoStarMaterialHighlightColor = new CustomColor(255, 255, 0);

    @ConfigInfo
    public boolean threeStarMaterialHighlightEnabled = true;

    @ConfigInfo
    public CustomColor threeStarMaterialHighlightColor = new CustomColor(230, 77, 0);

    @ConfigInfo
    public boolean cosmeticHighlightEnabled = true;

    @ConfigInfo
    public boolean powderHighlightEnabled = true;

    @ConfigInfo
    public boolean emeraldPouchHighlightEnabled = true;

    @ConfigInfo
    public boolean inventoryHighlightEnabled = true;

    @ConfigInfo
    public float inventoryOpacity = 1f;

    @ConfigInfo
    public boolean hotbarHighlightEnabled = true;

    @ConfigInfo
    public float hotbarOpacity = .5f;

    @SubscribeEvent(priority = EventPriority.HIGHEST)
    public void onRenderSlot(SlotRenderEvent.Pre e) {
        if (!inventoryHighlightEnabled) return;

        CustomColor color = getHighlightColor(e.getSlot().getItem(), false);
        if (color == CustomColor.NONE) return;

        RenderUtils.drawTexturedRectWithColor(
                e.getPoseStack(),
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

        CustomColor color = getHighlightColor(e.getItemStack(), true);
        if (color == CustomColor.NONE) return;

        RenderUtils.drawRect(e.getPoseStack(), color.withAlpha(hotbarOpacity), e.getX(), e.getY(), 0, 16, 16);
    }

    private CustomColor getHighlightColor(ItemStack itemStack, boolean hotbarHighlight) {
        Optional<WynnItem> wynnItemOpt = Models.Item.getWynnItem(itemStack);
        if (wynnItemOpt.isEmpty()) return CustomColor.NONE;

        WynnItem wynnItem = wynnItemOpt.get();
        HighlightInfo highlight = wynnItem.getCache()
                .getOrCalculate(WynnItemCache.HIGHLIGHT_KEY, () -> calculateHighlightInfo(wynnItemOpt.get()));
        if (highlight == null) return CustomColor.NONE;

        if (!highlight.isHighlightEnabled()) return CustomColor.NONE;

        return highlight.getHighlightColor();
    }

    private HighlightInfo calculateHighlightInfo(WynnItem wynnItem) {
        if (wynnItem instanceof CosmeticItem cosmeticItem) {
            return new CosmeticHighlight(cosmeticItem);
        }
        if (wynnItem instanceof GearTierItemProperty gearItem) {
            return new GearHighlight(gearItem);
        }
        if (wynnItem instanceof IngredientItem ingredientItem) {
            return new IngredientHighlight(ingredientItem);
        }
        if (wynnItem instanceof MaterialItem materialItem) {
            return new MaterialHighlight(materialItem);
        }
        if (wynnItem instanceof PowderItem powderItem) {
            return new PowderHighlight(powderItem);
        }
        if (wynnItem instanceof EmeraldPouchItem emeraldPouchItem) {
            return new EmeraldPouchHighlight(emeraldPouchItem);
        }

        return null;
    }

    private interface HighlightInfo {
        CustomColor getHighlightColor();

        boolean isHighlightEnabled();
    }

    private final class CosmeticHighlight implements HighlightInfo {
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

    private final class GearHighlight implements HighlightInfo {
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
                case CRAFTED -> craftedHighlightEnabled;
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
                case CRAFTED -> craftedHighlightColor;
            };
        }
    }

    private final class IngredientHighlight implements HighlightInfo {
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

    private final class MaterialHighlight implements HighlightInfo {
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

    private final class PowderHighlight implements HighlightInfo {
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

    private final class EmeraldPouchHighlight implements HighlightInfo {
        private final EmeraldPouchItem item;

        private EmeraldPouchHighlight(EmeraldPouchItem item) {
            this.item = item;
        }

        @Override
        public boolean isHighlightEnabled() {
            return emeraldPouchHighlightEnabled;
        }

        @Override
        public CustomColor getHighlightColor() {
            return CustomColor.fromChatFormatting(ChatFormatting.GREEN);
        }
    }
}
