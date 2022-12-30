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
import com.wynntils.gui.render.RenderUtils;
import com.wynntils.gui.render.Texture;
import com.wynntils.handlers.item.ItemAnnotation;
import com.wynntils.handlers.item.ItemHandler;
import com.wynntils.mc.event.HotbarSlotRenderEvent;
import com.wynntils.mc.event.SlotRenderEvent;
import com.wynntils.mc.objects.CustomColor;
import com.wynntils.wynn.handleditems.WynnItem;
import com.wynntils.wynn.handleditems.items.game.IngredientItem;
import com.wynntils.wynn.handleditems.items.game.MaterialItem;
import com.wynntils.wynn.handleditems.items.game.PowderItem;
import com.wynntils.wynn.handleditems.items.gui.CosmeticItem;
import com.wynntils.wynn.handleditems.properties.GearTierItemProperty;
import com.wynntils.wynn.objects.profiles.item.ItemTier;
import java.util.Optional;
import net.minecraft.world.item.ItemStack;
import net.minecraftforge.eventbus.api.EventPriority;
import net.minecraftforge.eventbus.api.SubscribeEvent;

@FeatureInfo(stability = Stability.STABLE, category = FeatureCategory.INVENTORY)
public class ItemHighlightFeature extends UserFeature {
    private static final HighlightInfo NO_HIGHLIGHT = new HighlightInfo() {
        @Override
        public CustomColor getHighlightColor() {
            return null;
        }

        @Override
        public boolean isHighlightEnabled() {
            return false;
        }
    };

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
        Optional<ItemAnnotation> annotationOpt = ItemHandler.getItemStackAnnotation(item);
        if (annotationOpt.isEmpty()) return CustomColor.NONE;
        if (!(annotationOpt.get() instanceof WynnItem wynnItem)) return CustomColor.NONE;
        HighlightInfo highlight = wynnItem.getCached(HighlightInfo.class);
        if (highlight == NO_HIGHLIGHT) return CustomColor.NONE;
        if (highlight == null) {
            highlight = calculateHighlightInfo(wynnItem);
            if (highlight == null) {
                wynnItem.storeInCache(NO_HIGHLIGHT);
                return CustomColor.NONE;
            }
            wynnItem.storeInCache(highlight);
        }

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

    public interface HighlightInfo {

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

    public static class IngredientHighlight implements HighlightInfo {
        private final IngredientItem item;

        public IngredientHighlight(IngredientItem item) {
            this.item = item;
        }

        @Override
        public CustomColor getHighlightColor() {
            return switch (item.getQualityTier()) {
                case 0 -> ItemHighlightFeature.INSTANCE.zeroStarIngredientHighlightColor;
                case 1 -> ItemHighlightFeature.INSTANCE.oneStarIngredientHighlightColor;
                case 2 -> ItemHighlightFeature.INSTANCE.twoStarIngredientHighlightColor;
                case 3 -> ItemHighlightFeature.INSTANCE.threeStarIngredientHighlightColor;
                default -> CustomColor.NONE;
            };
        }

        @Override
        public boolean isHighlightEnabled() {
            return switch (item.getQualityTier()) {
                case 0 -> ItemHighlightFeature.INSTANCE.zeroStarIngredientHighlightEnabled;
                case 1 -> ItemHighlightFeature.INSTANCE.oneStarIngredientHighlightEnabled;
                case 2 -> ItemHighlightFeature.INSTANCE.twoStarIngredientHighlightEnabled;
                case 3 -> ItemHighlightFeature.INSTANCE.threeStarIngredientHighlightEnabled;
                default -> false;
            };
        }
    }

    public static class MaterialHighlight implements HighlightInfo {
        private final MaterialItem item;

        public MaterialHighlight(MaterialItem item) {
            this.item = item;
        }

        @Override
        public CustomColor getHighlightColor() {
            return switch (item.getQualityTier()) {
                case 1 -> ItemHighlightFeature.INSTANCE.oneStarMaterialHighlightColor;
                case 2 -> ItemHighlightFeature.INSTANCE.twoStarMaterialHighlightColor;
                case 3 -> ItemHighlightFeature.INSTANCE.threeStarMaterialHighlightColor;
                default -> CustomColor.NONE;
            };
        }

        @Override
        public boolean isHighlightEnabled() {
            return switch (item.getQualityTier()) {
                case 1 -> ItemHighlightFeature.INSTANCE.oneStarMaterialHighlightEnabled;
                case 2 -> ItemHighlightFeature.INSTANCE.twoStarMaterialHighlightEnabled;
                case 3 -> ItemHighlightFeature.INSTANCE.threeStarMaterialHighlightEnabled;
                default -> false; // should not happen
            };
        }
    }

    public static class PowderHighlight implements HighlightInfo {
        private final PowderItem item;

        public PowderHighlight(PowderItem item) {
            this.item = item;
        }

        @Override
        public boolean isHighlightEnabled() {
            return ItemHighlightFeature.INSTANCE.powderHighlightEnabled;
        }

        @Override
        public CustomColor getHighlightColor() {
            return item.getPowderProfile().element().getColor();
        }
    }

    public static class CosmeticHighlight implements HighlightInfo {
        private final CosmeticItem item;

        public CosmeticHighlight(CosmeticItem item) {
            this.item = item;
        }

        @Override
        public boolean isHighlightEnabled() {
            return ItemHighlightFeature.INSTANCE.cosmeticHighlightEnabled;
        }

        @Override
        public CustomColor getHighlightColor() {
            return item.getHighlightColor();
        }
    }

    public static class GearHighlight implements HighlightInfo {
        private final GearTierItemProperty item;

        public GearHighlight(GearTierItemProperty item) {
            this.item = item;
        }

        @Override
        public boolean isHighlightEnabled() {
            return ItemHighlightFeature.isHighlightEnabled(item.getGearTier());
        }

        @Override
        public CustomColor getHighlightColor() {
            return ItemHighlightFeature.getHighlightColor(item.getGearTier());
        }
    }

    // This is a bit ugly, but it is used in GuideGearItemStack...
    public static CustomColor getHighlightColor(ItemTier itemTier) {
        return switch (itemTier) {
            case NORMAL -> ItemHighlightFeature.INSTANCE.normalHighlightColor;
            case UNIQUE -> ItemHighlightFeature.INSTANCE.uniqueHighlightColor;
            case RARE -> ItemHighlightFeature.INSTANCE.rareHighlightColor;
            case SET -> ItemHighlightFeature.INSTANCE.setHighlightColor;
            case LEGENDARY -> ItemHighlightFeature.INSTANCE.legendaryHighlightColor;
            case FABLED -> ItemHighlightFeature.INSTANCE.fabledHighlightColor;
            case MYTHIC -> ItemHighlightFeature.INSTANCE.mythicHighlightColor;
            default -> CustomColor.NONE;
        };
    }

    public static boolean isHighlightEnabled(ItemTier itemTier) {
        return switch (itemTier) {
            case NORMAL -> ItemHighlightFeature.INSTANCE.normalHighlightEnabled;
            case UNIQUE -> ItemHighlightFeature.INSTANCE.uniqueHighlightEnabled;
            case RARE -> ItemHighlightFeature.INSTANCE.rareHighlightEnabled;
            case SET -> ItemHighlightFeature.INSTANCE.setHighlightEnabled;
            case LEGENDARY -> ItemHighlightFeature.INSTANCE.legendaryHighlightEnabled;
            case FABLED -> ItemHighlightFeature.INSTANCE.fabledHighlightEnabled;
            case MYTHIC -> ItemHighlightFeature.INSTANCE.mythicHighlightEnabled;
            default -> false;
        };
    }
}
