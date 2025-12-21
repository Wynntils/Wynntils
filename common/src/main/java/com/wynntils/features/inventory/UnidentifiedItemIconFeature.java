/*
 * Copyright © Wynntils 2022-2025.
 * This file is released under LGPLv3. See LICENSE for full license details.
 */
package com.wynntils.features.inventory;

import com.mojang.blaze3d.vertex.PoseStack;
import com.wynntils.core.components.Models;
import com.wynntils.core.consumers.features.Feature;
import com.wynntils.core.consumers.features.ProfileDefault;
import com.wynntils.core.persisted.Persisted;
import com.wynntils.core.persisted.config.Category;
import com.wynntils.core.persisted.config.Config;
import com.wynntils.core.persisted.config.ConfigCategory;
import com.wynntils.core.persisted.config.ConfigProfile;
import com.wynntils.core.text.StyledText;
import com.wynntils.mc.event.HotbarSlotRenderEvent;
import com.wynntils.mc.event.SlotRenderEvent;
import com.wynntils.models.gear.type.GearType;
import com.wynntils.models.items.WynnItem;
import com.wynntils.models.items.items.game.GearBoxItem;
import com.wynntils.models.items.items.game.UnknownGearItem;
import com.wynntils.models.items.properties.GearTypeItemProperty;
import com.wynntils.models.items.properties.IdentifiableItemProperty;
import com.wynntils.utils.colors.CommonColors;
import com.wynntils.utils.colors.CustomColor;
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
import net.minecraft.network.chat.Component;
import net.minecraft.world.item.ItemStack;
import net.neoforged.bus.api.SubscribeEvent;

@ConfigCategory(Category.INVENTORY)
public class UnidentifiedItemIconFeature extends Feature {
    private static final Map<GearType, Pair<Integer, Integer>> TEXTURE_COORDS = new EnumMap<>(Map.ofEntries(
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
            Map.entry(GearType.CHARM, Pair.of(16 * 1, 16 * 3))));

    @Persisted
    private final Config<UnidentifiedItemTextures> texture = new Config<>(UnidentifiedItemTextures.WYNN);

    @Persisted
    private final Config<Boolean> markRevealedItems = new Config<>(true);

    @Persisted
    private final Config<UnidentifiedItemIconLocation> markRevealedItemsLocation =
            new Config<>(UnidentifiedItemIconLocation.CENTER);

    private static final CustomColor DEFAULT_UNID_ICON_COLOR = CommonColors.WHITE.withAlpha(0.67f);

    @Persisted
    private final Config<CustomColor> markRevealedItemsIconColor = new Config<>(DEFAULT_UNID_ICON_COLOR);

    private static final StyledText QUESTION_MARK_TEXT = StyledText.fromComponent(Component.literal("?"));

    public UnidentifiedItemIconFeature() {
        super(new ProfileDefault.Builder()
                .disableFor(ConfigProfile.LITE, ConfigProfile.MINIMAL, ConfigProfile.BLANK_SLATE)
                .build());
    }

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
            texture.get().getIconRenderer().renderIcon(poseStack, slotX, slotY, z, box.getGearType(), Optional.empty());
            return;
        }

        if (markRevealedItems.get()
                && (wynnItem instanceof IdentifiableItemProperty identifiableItem
                                && identifiableItem.getItemInstance().isEmpty()
                        || wynnItem instanceof UnknownGearItem unknownGearItem && unknownGearItem.isUnidentified())
                && wynnItem instanceof GearTypeItemProperty gearType) {
            markRevealedItemsLocation
                    .get()
                    .getIconRenderer()
                    .renderIcon(
                            poseStack,
                            slotX,
                            slotY,
                            z,
                            gearType.getGearType(),
                            Optional.of(markRevealedItemsIconColor.get()));
        }
    }

    @FunctionalInterface
    private interface IconRenderer {
        void renderIcon(PoseStack poseStack, int x, int y, int z, GearType gearType, Optional<CustomColor> textColor);

        static IconRenderer forSpriteSheet(Texture texture, int yOffset, int padding) {
            int paddedDims = 16 - padding - padding;
            return (poseStack, x, y, z, gearType, textColor) -> {
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

        static IconRenderer forText(
                Function<GearType, StyledText> textMap,
                HorizontalAlignment horizontalAlignment,
                VerticalAlignment verticalAlignment) {
            int padding = 0;
            int paddedDims = 16 - padding - padding;
            return (poseStack, x, y, z, gearType, textColor) -> {
                poseStack.pushPose();
                poseStack.translate(0, 0, z);
                StyledText text = textMap.apply(gearType);
                FontRenderer.getInstance()
                        .renderAlignedTextInBox(
                                poseStack,
                                text,
                                x + padding + 1,
                                x + paddedDims + 1,
                                y + padding + 1,
                                y + paddedDims + 1,
                                paddedDims,
                                textColor.orElse(DEFAULT_UNID_ICON_COLOR),
                                horizontalAlignment,
                                verticalAlignment,
                                TextShadow.OUTLINE);
                poseStack.popPose();
            };
        }
    }

    public enum UnidentifiedItemTextures {
        WYNN(IconRenderer.forSpriteSheet(Texture.GEAR_ICONS, 0, 2)),
        OUTLINE(IconRenderer.forSpriteSheet(Texture.GEAR_ICONS, 64, 2)),
        LEGACY(IconRenderer.forSpriteSheet(Texture.GEAR_ICONS, 128, 2));

        private final IconRenderer iconRenderer;

        UnidentifiedItemTextures(IconRenderer iconRenderer) {
            this.iconRenderer = iconRenderer;
        }

        private IconRenderer getIconRenderer() {
            return iconRenderer;
        }
    }

    public enum UnidentifiedItemIconLocation {
        TOP_LEFT(HorizontalAlignment.LEFT, VerticalAlignment.TOP),
        TOP_CENTER(HorizontalAlignment.CENTER, VerticalAlignment.TOP),
        TOP_RIGHT(HorizontalAlignment.RIGHT, VerticalAlignment.TOP),
        CENTER_LEFT(HorizontalAlignment.LEFT, VerticalAlignment.MIDDLE),
        CENTER(HorizontalAlignment.CENTER, VerticalAlignment.MIDDLE),
        CENTER_RIGHT(HorizontalAlignment.RIGHT, VerticalAlignment.MIDDLE),
        BOTTOM_LEFT(HorizontalAlignment.LEFT, VerticalAlignment.BOTTOM),
        BOTTOM_CENTER(HorizontalAlignment.CENTER, VerticalAlignment.BOTTOM),
        BOTTOM_RIGHT(HorizontalAlignment.RIGHT, VerticalAlignment.BOTTOM);

        private final IconRenderer iconRenderer;

        UnidentifiedItemIconLocation(HorizontalAlignment horizontalAlignment, VerticalAlignment verticalAlignment) {
            this.iconRenderer =
                    IconRenderer.forText(gearType -> QUESTION_MARK_TEXT, horizontalAlignment, verticalAlignment);
        }

        private IconRenderer getIconRenderer() {
            return iconRenderer;
        }
    }
}
