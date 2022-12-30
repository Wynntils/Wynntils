/*
 * Copyright © Wynntils 2022.
 * This file is released under AGPLv3. See LICENSE for full license details.
 */
package com.wynntils.features.user.inventory;

import com.mojang.blaze3d.vertex.PoseStack;
import com.wynntils.core.WynntilsMod;
import com.wynntils.core.components.Model;
import com.wynntils.core.components.Models;
import com.wynntils.core.config.Config;
import com.wynntils.core.features.UserFeature;
import com.wynntils.core.features.properties.FeatureCategory;
import com.wynntils.core.features.properties.FeatureInfo;
import com.wynntils.gui.render.FontRenderer;
import com.wynntils.gui.render.TextRenderTask;
import com.wynntils.handlers.item.ItemAnnotation;
import com.wynntils.handlers.item.ItemHandler;
import com.wynntils.mc.event.HotbarSlotRenderEvent;
import com.wynntils.mc.event.SlotRenderEvent;
import com.wynntils.model.item.game.GameItem;
import java.util.List;
import java.util.Optional;
import net.minecraft.world.item.ItemStack;
import net.minecraftforge.eventbus.api.SubscribeEvent;

@FeatureInfo(category = FeatureCategory.INVENTORY)
public class ExtendedItemCountFeature extends UserFeature {
    public static final List<Model> TEXT_OVERLAY_PROPERTIES = List.of(
            Models.DailyRewardMultiplierProperty,
            Models.ServerCountProperty,
            Models.SkillIconProperty,
            Models.SkillPointProperty);

    private static final TextOverlayInfo NO_OVERLAY = new TextOverlayInfo() {
        @Override
        public TextOverlay getTextOverlay() {
            return null;
        }

        @Override
        public boolean isTextOverlayEnabled() {
            return false;
        }
    };

    public static ExtendedItemCountFeature INSTANCE;

    @Config
    public boolean consumableChargeEnabled = true;

    @Config
    public FontRenderer.TextShadow consumableChargeShadow = FontRenderer.TextShadow.NORMAL;

    @Config
    public boolean inventoryTextOverlayEnabled = true;

    @Config
    public boolean hotbarTextOverlayEnabled = true;

    @Override
    public List<Model> getModelDependencies() {
        return TEXT_OVERLAY_PROPERTIES;
    }

    @SubscribeEvent
    public void onRenderSlot(SlotRenderEvent.Post e) {
        if (!inventoryTextOverlayEnabled) return;

        drawTextOverlay(e.getSlot().getItem(), e.getSlot().x, e.getSlot().y, false);
    }

    @SubscribeEvent
    public void onRenderHotbarSlot(HotbarSlotRenderEvent.Post e) {
        if (!hotbarTextOverlayEnabled) return;

        drawTextOverlay(e.getStack(), e.getX(), e.getY(), true);
    }

    private void drawTextOverlay(ItemStack item, int slotX, int slotY, boolean hotbar) {
        Optional<ItemAnnotation> annotationOpt = ItemHandler.getItemStackAnnotation(item);
        if (annotationOpt.isEmpty()) return;
        if (!(annotationOpt.get() instanceof GameItem wynnItem)) return;
        TextOverlayInfo overlayProperty = wynnItem.getCached(TextOverlayInfo.class);
        if (overlayProperty == NO_OVERLAY) return;
        if (overlayProperty == null) {
            overlayProperty = calculateOverlay(wynnItem);
            if (overlayProperty == null) {
                wynnItem.storeInCache(NO_OVERLAY);
                return;
            }
            wynnItem.storeInCache(overlayProperty);
        }

        boolean contextEnabled = hotbar ? overlayProperty.isHotbarText() : overlayProperty.isInventoryText();
        if (!overlayProperty.isTextOverlayEnabled() || !contextEnabled) return; // not enabled or wrong context

        TextOverlay textOverlay = overlayProperty.getTextOverlay();

        if (textOverlay == null) {
            WynntilsMod.error(overlayProperty + "'s textOverlay was null.");
            return;
        }

        PoseStack poseStack = new PoseStack();
        poseStack.translate(0, 0, 300); // items are drawn at z300, so text has to be as well
        poseStack.scale(textOverlay.scale(), textOverlay.scale(), 1f);
        float x = (slotX + textOverlay.xOffset()) / textOverlay.scale();
        float y = (slotY + textOverlay.yOffset()) / textOverlay.scale();
        FontRenderer.getInstance().renderText(poseStack, x, y, textOverlay.task());
    }

    private TextOverlayInfo calculateOverlay(GameItem wynnItem) {

        return null;
    }

    public interface TextOverlayInfo {
        TextOverlay getTextOverlay();

        boolean isTextOverlayEnabled();

        /**
         * Whether this overlay is allowed to be rendered in inventories.
         */
        default boolean isInventoryText() {
            return true;
        }

        /**
         * Whether this overlay is allowed to be rendered in the hotbar.
         */
        default boolean isHotbarText() {
            return true;
        }
    }


    /**
     * Describes an item's text overlay, with its color, position relative to the item's slot, and text scale.
     */
    public static final record TextOverlay(TextRenderTask task, int xOffset, int yOffset, float scale) {}
}
