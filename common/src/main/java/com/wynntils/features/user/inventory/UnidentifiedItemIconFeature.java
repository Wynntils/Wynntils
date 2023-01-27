/*
 * Copyright © Wynntils 2022.
 * This file is released under AGPLv3. See LICENSE for full license details.
 */
package com.wynntils.features.user.inventory;

import com.mojang.blaze3d.vertex.PoseStack;
import com.wynntils.core.components.Models;
import com.wynntils.core.config.Config;
import com.wynntils.core.features.UserFeature;
import com.wynntils.core.features.properties.FeatureCategory;
import com.wynntils.core.features.properties.FeatureInfo;
import com.wynntils.mc.event.HotbarSlotRenderEvent;
import com.wynntils.mc.event.SlotRenderEvent;
import com.wynntils.models.gearinfo.type.GearType;
import com.wynntils.models.items.items.game.GearBoxItem;
import com.wynntils.utils.render.RenderUtils;
import com.wynntils.utils.render.Texture;
import com.wynntils.utils.type.Pair;
import java.util.Map;
import java.util.Optional;
import net.minecraft.world.item.ItemStack;
import net.minecraftforge.eventbus.api.SubscribeEvent;

@FeatureInfo(category = FeatureCategory.INVENTORY)
public class UnidentifiedItemIconFeature extends UserFeature {
    private static final Map<GearType, Pair<Integer, Integer>> TEXTURE_COORDS = Map.ofEntries(
            Map.entry(GearType.SPEAR, Pair.of(16 * 1, 16 * 1)),
            Map.entry(GearType.WAND, Pair.of(16 * 0, 16 * 1)),
            Map.entry(GearType.DAGGER, Pair.of(16 * 2, 16 * 1)),
            Map.entry(GearType.BOW, Pair.of(16 * 3, 16 * 1)),
            Map.entry(GearType.RELIK, Pair.of(16 * 0, 16 * 2)),
            Map.entry(GearType.RING, Pair.of(16 * 1, 16 * 2)),
            Map.entry(GearType.BRACELET, Pair.of(16 * 2, 16 * 2)),
            Map.entry(GearType.NECKLACE, Pair.of(16 * 3, 16 * 2)),
            Map.entry(GearType.HELMET, Pair.of(16 * 0, 16 * 0)),
            Map.entry(GearType.CHESTPLATE, Pair.of(16 * 1, 16 * 0)),
            Map.entry(GearType.LEGGINGS, Pair.of(16 * 2, 16 * 0)),
            Map.entry(GearType.BOOTS, Pair.of(16 * 3, 16 * 0)),
            Map.entry(GearType.MASTERY_TOME, Pair.of(16 * 0, 16 * 3)),
            Map.entry(GearType.CHARM, Pair.of(16 * 1, 16 * 3)));

    @Config
    public UnidentifiedItemTextures texture = UnidentifiedItemTextures.Wynn;

    @SubscribeEvent
    public void onSlotRender(SlotRenderEvent.Post e) {
        drawIcon(e.getSlot().getItem(), e.getSlot().x, e.getSlot().y);
    }

    @SubscribeEvent
    public void onHotbarSlotRender(HotbarSlotRenderEvent.Post e) {
        drawIcon(e.getStack(), e.getX(), e.getY());
    }

    private void drawIcon(ItemStack item, int slotX, int slotY) {
        Optional<GearBoxItem> gearBoxItemOpt = Models.Item.asWynnItem(item, GearBoxItem.class);
        if (gearBoxItemOpt.isEmpty()) return;

        GearType gearType = gearBoxItemOpt.get().getGearType();

        int textureX = TEXTURE_COORDS.get(gearType).a();
        int textureY = TEXTURE_COORDS.get(gearType).b();
        RenderUtils.drawTexturedRect(
                new PoseStack(),
                Texture.GEAR_ICONS.resource(),
                slotX + 2,
                slotY + 2,
                400,
                12,
                12,
                textureX,
                textureY + texture.getTextureYOffset(),
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
