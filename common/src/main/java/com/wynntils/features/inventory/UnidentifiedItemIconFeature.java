/*
 * Copyright © Wynntils 2022-2024.
 * This file is released under LGPLv3. See LICENSE for full license details.
 */
package com.wynntils.features.inventory;

import com.mojang.blaze3d.vertex.PoseStack;
import com.wynntils.core.components.Models;
import com.wynntils.core.consumers.features.Feature;
import com.wynntils.core.persisted.Persisted;
import com.wynntils.core.persisted.config.Category;
import com.wynntils.core.persisted.config.Config;
import com.wynntils.core.persisted.config.ConfigCategory;
import com.wynntils.mc.event.HotbarSlotRenderEvent;
import com.wynntils.mc.event.SlotRenderEvent;
import com.wynntils.models.gear.type.GearType;
import com.wynntils.models.items.WynnItem;
import com.wynntils.models.items.items.game.GearBoxItem;
import com.wynntils.models.items.items.game.GearItem;
import com.wynntils.utils.render.RenderUtils;
import com.wynntils.utils.render.Texture;
import com.wynntils.utils.type.Pair;
import java.util.EnumMap;
import java.util.Map;
import java.util.Optional;
import net.minecraft.world.item.ItemStack;
import net.neoforged.bus.api.SubscribeEvent;

@ConfigCategory(Category.INVENTORY)
public class UnidentifiedItemIconFeature extends Feature {
    private static final Map<GearType, Pair<Integer, Integer>> TEXTURE_COORDS = new EnumMap<>(GearType.class);

    static {
        TEXTURE_COORDS.put(GearType.SPEAR, Pair.of(16 * 1, 16 * 1));
        TEXTURE_COORDS.put(GearType.WAND, Pair.of(16 * 0, 16 * 1));
        TEXTURE_COORDS.put(GearType.DAGGER, Pair.of(16 * 2, 16 * 1));
        TEXTURE_COORDS.put(GearType.BOW, Pair.of(16 * 3, 16 * 1));
        TEXTURE_COORDS.put(GearType.RELIK, Pair.of(16 * 0, 16 * 2));
        TEXTURE_COORDS.put(GearType.RING, Pair.of(16 * 1, 16 * 2));
        TEXTURE_COORDS.put(GearType.BRACELET, Pair.of(16 * 2, 16 * 2));
        TEXTURE_COORDS.put(GearType.NECKLACE, Pair.of(16 * 3, 16 * 2));
        TEXTURE_COORDS.put(GearType.HELMET, Pair.of(16 * 0, 16 * 0));
        TEXTURE_COORDS.put(GearType.CHESTPLATE, Pair.of(16 * 1, 16 * 0));
        TEXTURE_COORDS.put(GearType.LEGGINGS, Pair.of(16 * 2, 16 * 0));
        TEXTURE_COORDS.put(GearType.BOOTS, Pair.of(16 * 3, 16 * 0));
        TEXTURE_COORDS.put(GearType.MASTERY_TOME, Pair.of(16 * 0, 16 * 3));
        TEXTURE_COORDS.put(GearType.CHARM, Pair.of(16 * 1, 16 * 3));
    }

    @Persisted
    public final Config<UnidentifiedItemTextures> texture = new Config<>(UnidentifiedItemTextures.WYNN);

    @Persisted
    public final Config<Boolean> showOnUnboxed = new Config<>(true);

    @SubscribeEvent
    public void onSlotRender(SlotRenderEvent.CountPre e) {
        drawIcon(e.getPoseStack(), e.getSlot().getItem(), e.getSlot().x, e.getSlot().y, 200);
    }

    @SubscribeEvent
    public void onHotbarSlotRender(HotbarSlotRenderEvent.CountPre e) {
        drawIcon(e.getPoseStack(), e.getItemStack(), e.getX(), e.getY(), 200);
    }

    private void drawIcon(PoseStack poseStack, ItemStack itemStack, int slotX, int slotY, int z) {
        GearType gearType = getUnidentifiedGearType(itemStack);
        if (gearType == null) return;

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

    private GearType getUnidentifiedGearType(ItemStack itemStack) {
        Optional<WynnItem> wynnItemOpt = Models.Item.getWynnItem(itemStack);
        if (wynnItemOpt.isEmpty()) return null;
        WynnItem wynnItem = wynnItemOpt.get();

        if (wynnItem instanceof GearBoxItem box) {
            return box.getGearType();
        }

        if (showOnUnboxed.get() && wynnItem instanceof GearItem gear && gear.isUnidentified()) {
            return gear.getGearType();
        }

        return null;
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
