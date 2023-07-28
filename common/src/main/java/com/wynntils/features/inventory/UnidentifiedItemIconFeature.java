/*
 * Copyright © Wynntils 2022-2023.
 * This file is released under AGPLv3. See LICENSE for full license details.
 */
package com.wynntils.features.inventory;

import com.mojang.blaze3d.vertex.PoseStack;
import com.wynntils.core.components.Models;
import com.wynntils.core.config.Category;
import com.wynntils.core.config.Config;
import com.wynntils.core.config.ConfigCategory;
import com.wynntils.core.config.RegisterConfig;
import com.wynntils.core.consumers.features.Feature;
import com.wynntils.mc.event.HotbarSlotRenderEvent;
import com.wynntils.mc.event.SlotRenderEvent;
import com.wynntils.models.gear.type.GearType;
import com.wynntils.models.items.items.game.GearBoxItem;
import com.wynntils.utils.render.RenderUtils;
import com.wynntils.utils.render.Texture;
import com.wynntils.utils.type.Pair;
import java.util.Map;
import java.util.Optional;
import net.minecraft.world.item.ItemStack;
import net.minecraftforge.eventbus.api.SubscribeEvent;

@ConfigCategory(Category.INVENTORY)
public class UnidentifiedItemIconFeature extends Feature {
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

    @RegisterConfig
    public final Config<UnidentifiedItemTextures> texture = new Config<>(UnidentifiedItemTextures.WYNN);

    @SubscribeEvent
    public void onSlotRender(SlotRenderEvent.CountPre e) {
        drawIcon(e.getPoseStack(), e.getSlot().getItem(), e.getSlot().x, e.getSlot().y, 300);
    }

    @SubscribeEvent
    public void onHotbarSlotRender(HotbarSlotRenderEvent.CountPre e) {
        drawIcon(e.getPoseStack(), e.getItemStack(), e.getX(), e.getY(), 200);
    }

    private void drawIcon(PoseStack poseStack, ItemStack itemStack, int slotX, int slotY, int z) {
        Optional<GearBoxItem> gearBoxItemOpt = Models.Item.asWynnItem(itemStack, GearBoxItem.class);
        if (gearBoxItemOpt.isEmpty()) return;

        GearType gearType = gearBoxItemOpt.get().getGearType();

        int textureX = TEXTURE_COORDS.get(gearType).a();
        int textureY = TEXTURE_COORDS.get(gearType).b();
        RenderUtils.drawTexturedRect(
                poseStack,
                Texture.GEAR_ICONS.resource(),
                slotX + 2,
                slotY + 2,
                z,
                12,
                12,
                textureX,
                textureY + texture.get().getTextureYOffset(),
                16,
                16,
                Texture.GEAR_ICONS.width(),
                Texture.GEAR_ICONS.height());
    }

    public enum UnidentifiedItemTextures {
        WYNN(0),
        OUTLINE(64);

        private final int yOffset;

        UnidentifiedItemTextures(int yOffset) {
            this.yOffset = yOffset;
        }

        private int getTextureYOffset() {
            return yOffset;
        }
    }
}
