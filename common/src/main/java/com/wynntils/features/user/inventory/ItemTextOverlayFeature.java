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
import com.wynntils.mc.event.HotbarSlotRenderEvent;
import com.wynntils.mc.event.SlotRenderEvent;
import com.wynntils.wynn.item.WynnItemStack;
import com.wynntils.wynn.item.properties.ItemProperty;
import com.wynntils.wynn.item.properties.type.TextOverlayProperty;
import java.util.List;
import net.minecraft.world.item.ItemStack;
import net.minecraftforge.eventbus.api.SubscribeEvent;

@FeatureInfo(category = FeatureCategory.INVENTORY)
public class ItemTextOverlayFeature extends UserFeature {
    public static final List<Model> TEXT_OVERLAY_PROPERTIES = List.of(
            Models.AmplifierTierProperty,
            Models.ConsumableChargeProperty,
            Models.DailyRewardMultiplierProperty,
            Models.DungeonKeyProperty,
            Models.EmeraldPouchTierProperty,
            Models.GatheringToolProperty,
            Models.PowderTierProperty,
            Models.ServerCountProperty,
            Models.SkillIconProperty,
            Models.SkillPointProperty,
            Models.TeleportScrollProperty);

    public static ItemTextOverlayFeature INSTANCE;

    @Config
    public boolean powderTierEnabled = true;

    @Config
    public boolean powderTierRomanNumerals = true;

    @Config
    public FontRenderer.TextShadow powderTierShadow = FontRenderer.TextShadow.OUTLINE;

    @Config
    public boolean emeraldPouchTierEnabled = true;

    @Config
    public boolean emeraldPouchTierRomanNumerals = true;

    @Config
    public FontRenderer.TextShadow emeraldPouchTierShadow = FontRenderer.TextShadow.OUTLINE;

    @Config
    public boolean gatheringToolTierEnabled = true;

    @Config
    public boolean gatheringToolTierRomanNumerals = true;

    @Config
    public FontRenderer.TextShadow gatheringToolTierShadow = FontRenderer.TextShadow.OUTLINE;

    @Config
    public boolean teleportScrollEnabled = true;

    @Config
    public FontRenderer.TextShadow teleportScrollShadow = FontRenderer.TextShadow.OUTLINE;

    @Config
    public boolean dungeonKeyEnabled = true;

    @Config
    public FontRenderer.TextShadow dungeonKeyShadow = FontRenderer.TextShadow.OUTLINE;

    @Config
    public boolean amplifierTierEnabled = true;

    @Config
    public boolean amplifierTierRomanNumerals = true;

    @Config
    public FontRenderer.TextShadow amplifierTierShadow = FontRenderer.TextShadow.OUTLINE;

    @Config
    public boolean consumableChargeEnabled = true;

    @Config
    public FontRenderer.TextShadow consumableChargeShadow = FontRenderer.TextShadow.NORMAL;

    @Config
    public boolean skillIconEnabled = true;

    @Config
    public FontRenderer.TextShadow skillIconShadow = FontRenderer.TextShadow.OUTLINE;

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
        if (!(item instanceof WynnItemStack wynnItem)) return;
        if (!wynnItem.hasProperty(ItemProperty.TEXT_OVERLAY)) return;

        for (TextOverlayProperty overlayProperty : wynnItem.getProperties(ItemProperty.TEXT_OVERLAY)) {
            boolean contextEnabled = hotbar ? overlayProperty.isHotbarText() : overlayProperty.isInventoryText();
            if (!overlayProperty.isTextOverlayEnabled() || !contextEnabled) continue; // not enabled or wrong context

            TextOverlayProperty.TextOverlay textOverlay = overlayProperty.getTextOverlay();

            if (textOverlay == null) {
                WynntilsMod.error(overlayProperty + "'s textOverlay was null.");
                continue;
            }

            PoseStack poseStack = new PoseStack();
            poseStack.translate(0, 0, 300); // items are drawn at z300, so text has to be as well
            poseStack.scale(textOverlay.scale(), textOverlay.scale(), 1f);
            float x = (slotX + textOverlay.xOffset()) / textOverlay.scale();
            float y = (slotY + textOverlay.yOffset()) / textOverlay.scale();
            FontRenderer.getInstance().renderText(poseStack, x, y, textOverlay.task());
        }
    }
}
