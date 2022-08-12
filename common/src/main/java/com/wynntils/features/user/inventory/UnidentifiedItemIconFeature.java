/*
 * Copyright © Wynntils 2022.
 * This file is released under AGPLv3. See LICENSE for full license details.
 */
package com.wynntils.features.user.inventory;

import com.google.common.collect.ImmutableList;
import com.mojang.blaze3d.vertex.PoseStack;
import com.wynntils.core.config.Config;
import com.wynntils.core.features.UserFeature;
import com.wynntils.core.features.properties.FeatureInfo;
import com.wynntils.core.managers.Model;
import com.wynntils.mc.event.HotbarSlotRenderEvent;
import com.wynntils.mc.event.SlotRenderEvent;
import com.wynntils.mc.render.RenderUtils;
import com.wynntils.mc.render.Texture;
import com.wynntils.wc.custom.item.ItemStackTransformModel;
import com.wynntils.wc.custom.item.UnidentifiedItemStack;
import net.minecraft.world.item.ItemStack;
import net.minecraftforge.eventbus.api.SubscribeEvent;

@FeatureInfo(category = "Inventory")
public class UnidentifiedItemIconFeature extends UserFeature {
    @Config
    public UnidentifiedItemTextures texture = UnidentifiedItemTextures.Wynn;

    @Override
    protected void onInit(
            ImmutableList.Builder<Condition> conditions, ImmutableList.Builder<Class<? extends Model>> dependencies) {
        dependencies.add(ItemStackTransformModel.class);
    }

    @SubscribeEvent
    public void onSlotRender(SlotRenderEvent.Post e) {
        drawIcon(e.getSlot().getItem(), e.getSlot().x, e.getSlot().y);
    }

    @SubscribeEvent
    public void onHotbarSlotRender(HotbarSlotRenderEvent.Post e) {
        drawIcon(e.getStack(), e.getX(), e.getY());
    }

    private void drawIcon(ItemStack item, int slotX, int slotY) {
        if (!(item instanceof UnidentifiedItemStack unidentifiedItem)) return;

        RenderUtils.drawTexturedRect(
                new PoseStack(),
                Texture.GEAR_ICONS.resource(),
                slotX + 2,
                slotY + 2,
                400,
                12,
                12,
                unidentifiedItem.getItemType().getIconTextureX(),
                unidentifiedItem.getItemType().getIconTextureY() + texture.getTextureYOffset(),
                16,
                16,
                Texture.GEAR_ICONS.width(),
                Texture.GEAR_ICONS.height());
    }

    public enum UnidentifiedItemTextures {
        Wynn(0),
        Outline(64);

        private final int yOffset;

        UnidentifiedItemTextures(int yOffset) {
            this.yOffset = yOffset;
        }

        public int getTextureYOffset() {
            return yOffset;
        }
    }
}
