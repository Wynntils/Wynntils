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
import com.wynntils.core.text.StyledText;
import com.wynntils.mc.event.HotbarSlotRenderEvent;
import com.wynntils.mc.event.SlotRenderEvent;
import com.wynntils.models.gear.type.GearType;
import com.wynntils.models.items.WynnItem;
import com.wynntils.models.items.items.game.GearBoxItem;
import com.wynntils.models.items.items.game.GearItem;
import com.wynntils.utils.colors.CommonColors;
import com.wynntils.utils.render.FontRenderer;
import com.wynntils.utils.render.RenderUtils;
import com.wynntils.utils.render.Texture;
import com.wynntils.utils.render.type.HorizontalAlignment;
import com.wynntils.utils.render.type.TextShadow;
import com.wynntils.utils.render.type.VerticalAlignment;
import com.wynntils.utils.type.Pair;
import java.util.EnumMap;
import java.util.Map;
import java.util.Optional;
import java.util.function.Function;
import net.minecraft.ChatFormatting;
import net.minecraft.network.chat.Component;
import net.minecraft.world.item.ItemStack;
import net.neoforged.bus.api.SubscribeEvent;

@ConfigCategory(Category.INVENTORY)
public class UnidentifiedItemIconFeature extends Feature {
    private static final StyledText QUESTION_MARK_TEXT =
            StyledText.fromComponent(Component.literal("?").withStyle(ChatFormatting.BOLD));
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

    @Persisted
    public final Config<UnidentifiedItemTextures> unboxedTexture = new Config<>(UnidentifiedItemTextures.QUESTION_MARK);

    @SubscribeEvent
    public void onSlotRender(SlotRenderEvent.CountPre e) {
        drawIcon(e.getPoseStack(), e.getSlot().getItem(), e.getSlot().x, e.getSlot().y, 200);
    }

    @SubscribeEvent
    public void onHotbarSlotRender(HotbarSlotRenderEvent.CountPre e) {
        drawIcon(e.getPoseStack(), e.getItemStack(), e.getX(), e.getY(), 200);
    }

    private void drawIcon(PoseStack poseStack, ItemStack itemStack, int slotX, int slotY, int z) {
        Optional<WynnItem> wynnItemOpt = Models.Item.getWynnItem(itemStack);
        if (wynnItemOpt.isEmpty()) return;

        WynnItem wynnItem = wynnItemOpt.get();
        if (wynnItem instanceof GearBoxItem box) {
            texture.get().getIconRenderer().renderIcon(poseStack, slotX, slotY, z, box.getGearType());
        } else if (showOnUnboxed.get() && wynnItem instanceof GearItem gear && gear.isUnidentified()) {
            unboxedTexture.get().getIconRenderer().renderIcon(poseStack, slotX, slotY, z, gear.getGearType());
        }
    }

    @FunctionalInterface
    private interface IconRenderer {
        void renderIcon(PoseStack poseStack, int x, int y, int z, GearType gearType);

        static IconRenderer forSpriteSheet(Texture texture, int yOffset, int padding) {
            int paddedDims = 16 - padding - padding;
            return (poseStack, x, y, z, gearType) -> {
                Pair<Integer, Integer> textureCoords = TEXTURE_COORDS.get(gearType);
                RenderUtils.drawTexturedRect(
                        poseStack,
                        texture.resource(),
                        x + padding,
                        y + padding,
                        z,
                        paddedDims,
                        paddedDims,
                        textureCoords.a(),
                        textureCoords.b() + yOffset,
                        16,
                        16,
                        texture.width(),
                        texture.height());
            };
        }

        static IconRenderer forText(Function<GearType, StyledText> textMap) {
            return (poseStack, x, y, z, gearType) -> {
                poseStack.pushPose();
                poseStack.translate(0, 0, z);
                StyledText text = textMap.apply(gearType);
                FontRenderer.getInstance()
                        .renderText(
                                poseStack,
                                text,
                                x + 8,
                                y + 9,
                                0,
                                CommonColors.WHITE,
                                HorizontalAlignment.CENTER,
                                VerticalAlignment.MIDDLE,
                                TextShadow.OUTLINE);
                poseStack.popPose();
            };
        }
    }

    public enum UnidentifiedItemTextures {
        WYNN(IconRenderer.forSpriteSheet(Texture.GEAR_ICONS, 0, 2)),
        OUTLINE(IconRenderer.forSpriteSheet(Texture.GEAR_ICONS, 64, 2)),
        QUESTION_MARK(IconRenderer.forText(gearType -> QUESTION_MARK_TEXT));

        private final IconRenderer iconRenderer;

        UnidentifiedItemTextures(IconRenderer iconRenderer) {
            this.iconRenderer = iconRenderer;
        }

        private IconRenderer getIconRenderer() {
            return iconRenderer;
        }
    }
}
