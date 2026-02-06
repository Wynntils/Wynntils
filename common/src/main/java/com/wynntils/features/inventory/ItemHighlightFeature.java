/*
 * Copyright © Wynntils 2022-2025.
 * This file is released under LGPLv3. See LICENSE for full license details.
 */
package com.wynntils.features.inventory;

import com.mojang.blaze3d.systems.RenderSystem;
import com.wynntils.core.components.Models;
import com.wynntils.core.consumers.features.Feature;
import com.wynntils.core.consumers.features.ProfileDefault;
import com.wynntils.core.persisted.Persisted;
import com.wynntils.core.persisted.config.Category;
import com.wynntils.core.persisted.config.Config;
import com.wynntils.core.persisted.config.ConfigCategory;
import com.wynntils.core.persisted.config.ConfigProfile;
import com.wynntils.mc.event.DataComponentGetEvent;
import com.wynntils.mc.event.HotbarSlotRenderEvent;
import com.wynntils.mc.event.SlotRenderEvent;
import com.wynntils.models.items.WynnItem;
import com.wynntils.models.items.WynnItemData;
import com.wynntils.models.items.items.game.EmeraldPouchItem;
import com.wynntils.models.items.items.game.IngredientItem;
import com.wynntils.models.items.items.game.MaterialItem;
import com.wynntils.models.items.items.game.PowderItem;
import com.wynntils.models.items.items.gui.StoreItem;
import com.wynntils.models.items.properties.GearTierItemProperty;
import com.wynntils.utils.colors.CustomColor;
import com.wynntils.utils.mc.McUtils;
import com.wynntils.utils.render.RenderUtils;
import com.wynntils.utils.render.Texture;
import com.wynntils.utils.render.buffered.BufferedRenderUtils;
import java.util.List;
import java.util.Optional;
import net.minecraft.ChatFormatting;
import net.minecraft.world.item.ItemStack;
import net.minecraft.world.item.component.CustomModelData;
import net.neoforged.bus.api.EventPriority;
import net.neoforged.bus.api.SubscribeEvent;

@ConfigCategory(Category.INVENTORY)
public class ItemHighlightFeature extends Feature {
    private static final List<String> DEFAULT_HIGHLIGHT_KEYS =
            List.of("item_tier", "ingredient_tier", "material_tier", "store_tier");

    // TODO: Set default to WYNN when porting to 1.21.6+
    @Persisted
    private final Config<HighlightTexture> highlightTexture = new Config<>(HighlightTexture.TAG);

    @Persisted
    private final Config<Boolean> normalHighlightEnabled = new Config<>(true);

    @Persisted
    private final Config<CustomColor> normalHighlightColor = new Config<>(new CustomColor(255, 255, 255));

    @Persisted
    private final Config<Boolean> uniqueHighlightEnabled = new Config<>(true);

    @Persisted
    private final Config<CustomColor> uniqueHighlightColor = new Config<>(new CustomColor(255, 255, 0));

    @Persisted
    private final Config<Boolean> rareHighlightEnabled = new Config<>(true);

    @Persisted
    private final Config<CustomColor> rareHighlightColor = new Config<>(new CustomColor(255, 0, 255));

    @Persisted
    private final Config<Boolean> setHighlightEnabled = new Config<>(true);

    @Persisted
    private final Config<CustomColor> setHighlightColor = new Config<>(new CustomColor(0, 255, 0));

    @Persisted
    private final Config<Boolean> legendaryHighlightEnabled = new Config<>(true);

    @Persisted
    private final Config<CustomColor> legendaryHighlightColor = new Config<>(new CustomColor(0, 255, 255));

    @Persisted
    private final Config<Boolean> fabledHighlightEnabled = new Config<>(true);

    @Persisted
    private final Config<CustomColor> fabledHighlightColor = new Config<>(new CustomColor(255, 85, 85));

    @Persisted
    private final Config<Boolean> mythicHighlightEnabled = new Config<>(true);

    @Persisted
    private final Config<CustomColor> mythicHighlightColor = new Config<>(new CustomColor(76, 0, 76));

    @Persisted
    private final Config<Boolean> craftedHighlightEnabled = new Config<>(true);

    @Persisted
    private final Config<CustomColor> craftedHighlightColor = new Config<>(new CustomColor(0, 138, 138));

    @Persisted
    private final Config<Boolean> zeroStarIngredientHighlightEnabled = new Config<>(true);

    @Persisted
    private final Config<CustomColor> zeroStarIngredientHighlightColor = new Config<>(new CustomColor(102, 102, 102));

    @Persisted
    private final Config<Boolean> oneStarIngredientHighlightEnabled = new Config<>(true);

    @Persisted
    private final Config<CustomColor> oneStarIngredientHighlightColor = new Config<>(new CustomColor(255, 247, 153));

    @Persisted
    private final Config<Boolean> twoStarIngredientHighlightEnabled = new Config<>(true);

    @Persisted
    private final Config<CustomColor> twoStarIngredientHighlightColor = new Config<>(new CustomColor(255, 255, 0));

    @Persisted
    private final Config<Boolean> threeStarIngredientHighlightEnabled = new Config<>(true);

    @Persisted
    private final Config<CustomColor> threeStarIngredientHighlightColor = new Config<>(new CustomColor(230, 77, 0));

    @Persisted
    private final Config<Boolean> oneStarMaterialHighlightEnabled = new Config<>(true);

    @Persisted
    private final Config<CustomColor> oneStarMaterialHighlightColor = new Config<>(new CustomColor(255, 247, 153));

    @Persisted
    private final Config<Boolean> twoStarMaterialHighlightEnabled = new Config<>(true);

    @Persisted
    private final Config<CustomColor> twoStarMaterialHighlightColor = new Config<>(new CustomColor(255, 255, 0));

    @Persisted
    private final Config<Boolean> threeStarMaterialHighlightEnabled = new Config<>(true);

    @Persisted
    private final Config<CustomColor> threeStarMaterialHighlightColor = new Config<>(new CustomColor(230, 77, 0));

    @Persisted
    private final Config<Boolean> storeHighlightEnabled = new Config<>(true);

    @Persisted
    private final Config<Boolean> powderHighlightEnabled = new Config<>(true);

    @Persisted
    private final Config<Boolean> emeraldPouchHighlightEnabled = new Config<>(true);

    @Persisted
    private final Config<Boolean> inventoryHighlightEnabled = new Config<>(true);

    @Persisted
    private final Config<Float> inventoryOpacity = new Config<>(1f);

    @Persisted
    private final Config<Boolean> hotbarHighlightEnabled = new Config<>(true);

    @Persisted
    private final Config<Float> hotbarOpacity = new Config<>(1f);

    @Persisted
    private final Config<Boolean> selectedItemHighlight = new Config<>(true);

    public ItemHighlightFeature() {
        super(new ProfileDefault.Builder()
                .disableFor(ConfigProfile.MINIMAL, ConfigProfile.BLANK_SLATE)
                .build());
    }

    @SubscribeEvent(priority = EventPriority.HIGH)
    public void onRenderSlot(SlotRenderEvent.CountPre e) {
        if (!inventoryHighlightEnabled.get()) return;

        CustomColor color = getHighlightColor(e.getSlot().getItem(), false);
        if (color == CustomColor.NONE) return;

        if (selectedItemHighlight.get()
                && McUtils.inventory().getSelected().equals(e.getSlot().getItem())) {
            RenderSystem.enableDepthTest();
            RenderUtils.drawTexturedRectWithColor(
                    e.getPoseStack(),
                    Texture.HOTBAR_SELECTED_HIGHLIGHT.resource(),
                    color,
                    e.getSlot().x,
                    e.getSlot().y,
                    100,
                    16,
                    16,
                    16,
                    16);
            RenderSystem.disableDepthTest();
            return;
        }

        RenderSystem.enableDepthTest();
        RenderUtils.drawTexturedRectWithColor(
                e.getPoseStack(),
                Texture.HIGHLIGHT.resource(),
                color,
                e.getSlot().x - 1,
                e.getSlot().y - 1,
                100,
                18,
                18,
                // TODO: Remove +18 when porting to 1.21.6+
                (highlightTexture.get().ordinal() * 18) + 18,
                0,
                18,
                18,
                Texture.HIGHLIGHT.width(),
                Texture.HIGHLIGHT.height());
        RenderSystem.disableDepthTest();
    }

    @SubscribeEvent
    public void onRenderHotbarSlot(HotbarSlotRenderEvent.Pre e) {
        if (!hotbarHighlightEnabled.get()) return;

        CustomColor color = getHighlightColor(e.getItemStack(), true);
        if (color == CustomColor.NONE) return;

        if (selectedItemHighlight.get() && McUtils.inventory().getSelected().equals(e.getItemStack())) {
            BufferedRenderUtils.drawTexturedRectWithColor(
                    e.getPoseStack(),
                    e.getGuiGraphics().bufferSource,
                    Texture.HOTBAR_SELECTED_HIGHLIGHT,
                    color,
                    e.getX(),
                    e.getY());
            return;
        }

        BufferedRenderUtils.drawTexturedRectWithColor(
                e.getPoseStack(),
                e.getGuiGraphics().bufferSource,
                Texture.HIGHLIGHT.resource(),
                color,
                e.getX() - 1,
                e.getY() - 1,
                0,
                18,
                18,
                // TODO: Remove +18 when porting to 1.21.6+
                (highlightTexture.get().ordinal() * 18) + 18,
                0,
                18,
                18,
                Texture.HIGHLIGHT.width(),
                Texture.HIGHLIGHT.height());
    }

    @SubscribeEvent
    public void onGetModelData(DataComponentGetEvent.CustomModelData event) {
        CustomModelData itemStackModelData = event.getOriginalValue();

        // The index of model data matters, so instead of removing the tier string, just replace it with an empty string
        List<String> newStrings = itemStackModelData.strings().stream()
                .map(s -> DEFAULT_HIGHLIGHT_KEYS.stream().anyMatch(s::startsWith) ? "" : s)
                .toList();

        if (!newStrings.equals(itemStackModelData.strings())) {
            event.setValue(new CustomModelData(
                    itemStackModelData.floats(), itemStackModelData.flags(), newStrings, itemStackModelData.colors()));
        }
    }

    private CustomColor getHighlightColor(ItemStack itemStack, boolean hotbarHighlight) {
        Optional<WynnItem> wynnItemOpt = Models.Item.getWynnItem(itemStack);
        if (wynnItemOpt.isEmpty()) return CustomColor.NONE;

        WynnItem wynnItem = wynnItemOpt.get();
        HighlightInfo highlight = wynnItem.getData()
                .getOrCalculate(WynnItemData.HIGHLIGHT_KEY, () -> calculateHighlightInfo(wynnItemOpt.get()));
        if (highlight == null) return CustomColor.NONE;

        if (!highlight.isHighlightEnabled()) return CustomColor.NONE;

        return highlight.getHighlightColor().withAlpha(hotbarHighlight ? hotbarOpacity.get() : inventoryOpacity.get());
    }

    private HighlightInfo calculateHighlightInfo(WynnItem wynnItem) {
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
        if (wynnItem instanceof StoreItem storeItem) {
            return new StoreHighlight(storeItem);
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

    private final class StoreHighlight implements HighlightInfo {
        private final StoreItem item;

        private StoreHighlight(StoreItem item) {
            this.item = item;
        }

        @Override
        public boolean isHighlightEnabled() {
            return storeHighlightEnabled.get();
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
            return item.getGearTier() != null
                    && switch (item.getGearTier()) {
                        case NORMAL -> normalHighlightEnabled.get();
                        case UNIQUE -> uniqueHighlightEnabled.get();
                        case RARE -> rareHighlightEnabled.get();
                        case SET -> setHighlightEnabled.get();
                        case LEGENDARY -> legendaryHighlightEnabled.get();
                        case FABLED -> fabledHighlightEnabled.get();
                        case MYTHIC -> mythicHighlightEnabled.get();
                        case CRAFTED -> craftedHighlightEnabled.get();
                    };
        }

        @Override
        public CustomColor getHighlightColor() {
            return item.getGearTier() == null
                    ? CustomColor.NONE
                    : switch (item.getGearTier()) {
                        case NORMAL -> normalHighlightColor.get();
                        case UNIQUE -> uniqueHighlightColor.get();
                        case RARE -> rareHighlightColor.get();
                        case SET -> setHighlightColor.get();
                        case LEGENDARY -> legendaryHighlightColor.get();
                        case FABLED -> fabledHighlightColor.get();
                        case MYTHIC -> mythicHighlightColor.get();
                        case CRAFTED -> craftedHighlightColor.get();
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
                case 0 -> zeroStarIngredientHighlightColor.get();
                case 1 -> oneStarIngredientHighlightColor.get();
                case 2 -> twoStarIngredientHighlightColor.get();
                case 3 -> threeStarIngredientHighlightColor.get();
                default -> CustomColor.NONE;
            };
        }

        @Override
        public boolean isHighlightEnabled() {
            return switch (item.getQualityTier()) {
                case 0 -> zeroStarIngredientHighlightEnabled.get();
                case 1 -> oneStarIngredientHighlightEnabled.get();
                case 2 -> twoStarIngredientHighlightEnabled.get();
                case 3 -> threeStarIngredientHighlightEnabled.get();
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
                case 1 -> oneStarMaterialHighlightColor.get();
                case 2 -> twoStarMaterialHighlightColor.get();
                case 3 -> threeStarMaterialHighlightColor.get();
                default -> CustomColor.NONE;
            };
        }

        @Override
        public boolean isHighlightEnabled() {
            return switch (item.getQualityTier()) {
                case 1 -> oneStarMaterialHighlightEnabled.get();
                case 2 -> twoStarMaterialHighlightEnabled.get();
                case 3 -> threeStarMaterialHighlightEnabled.get();
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
            return powderHighlightEnabled.get();
        }

        @Override
        public CustomColor getHighlightColor() {
            return item.getPowderProfile().element().getColor();
        }
    }

    private final class EmeraldPouchHighlight implements HighlightInfo {
        private EmeraldPouchHighlight(EmeraldPouchItem item) {}

        @Override
        public boolean isHighlightEnabled() {
            return emeraldPouchHighlightEnabled.get();
        }

        @Override
        public CustomColor getHighlightColor() {
            return CustomColor.fromChatFormatting(ChatFormatting.GREEN);
        }
    }

    public enum HighlightTexture {
        // TODO: Add WYNN back when porting to 1.21.6+
        // WYNN,
        TAG,
        CIRCLE_TRANSPARENT,
        CIRCLE_OPAQUE,
        CIRCLE_OUTLINE_LARGE,
        CIRCLE_OUTLINE_SMALL,
        BOX_TRANSPARENT,
        BOX_OPAQUE,
        BOX_GRADIENT_1,
        BOX_GRADIENT_2
    }
}
