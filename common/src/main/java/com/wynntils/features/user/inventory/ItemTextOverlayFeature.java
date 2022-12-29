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
import com.wynntils.gui.render.TextRenderSetting;
import com.wynntils.gui.render.TextRenderTask;
import com.wynntils.handlers.item.ItemAnnotation;
import com.wynntils.handlers.item.ItemHandler;
import com.wynntils.mc.event.HotbarSlotRenderEvent;
import com.wynntils.mc.event.SlotRenderEvent;
import com.wynntils.mc.objects.CustomColor;
import com.wynntils.model.item.game.AmplifierItem;
import com.wynntils.model.item.game.DungeonKeyItem;
import com.wynntils.model.item.game.EmeraldPouchItem;
import com.wynntils.model.item.game.GameItem;
import com.wynntils.model.item.game.GatheringToolItem;
import com.wynntils.model.item.game.PowderItem;
import com.wynntils.model.item.game.SkillPotionItem;
import com.wynntils.model.item.game.TeleportScrollItem;
import com.wynntils.utils.MathUtils;
import com.wynntils.wynn.item.properties.type.PropertyType;
import com.wynntils.wynn.item.properties.type.TextOverlayProperty;
import com.wynntils.wynn.objects.profiles.item.ItemTier;
import java.util.List;
import java.util.Optional;
import net.minecraft.ChatFormatting;
import net.minecraft.world.item.ItemStack;
import net.minecraftforge.eventbus.api.SubscribeEvent;

@FeatureInfo(category = FeatureCategory.INVENTORY)
public class ItemTextOverlayFeature extends UserFeature {
    public static final List<Model> TEXT_OVERLAY_PROPERTIES = List.of(
            Models.ConsumableChargeProperty,
            Models.DailyRewardMultiplierProperty,
            Models.ServerCountProperty,
            Models.SkillIconProperty,
            Models.SkillPointProperty);

    private static final TextOverlayInfo NO_OVERLAY = new TextOverlayInfo() {
        @Override
        public TextOverlayProperty.TextOverlay getTextOverlay() {
            return null;
        }

        @Override
        public boolean isTextOverlayEnabled() {
            return false;
        }
    };

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

        TextOverlayProperty.TextOverlay textOverlay = overlayProperty.getTextOverlay();

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
        if (wynnItem instanceof DungeonKeyItem dungeonKeyItem) {
            return new DungeonKeyOverlay(dungeonKeyItem);
        }
        if (wynnItem instanceof AmplifierItem amplifierItem) {
            return new AmplifierOverlay(amplifierItem);
        }
        if (wynnItem instanceof TeleportScrollItem teleportScrollItem) {
            return new TeleportScrollOverlay(teleportScrollItem);
        }
        if (wynnItem instanceof EmeraldPouchItem emeraldPouchItem) {
            return new EmeraldPouchOverlay(emeraldPouchItem);
        }
        if (wynnItem instanceof GatheringToolItem gatheringToolItem) {
            return new GatheringToolOverlay(gatheringToolItem);
        }
        if (wynnItem instanceof PowderItem powderItem) {
            return new PowderOverlay(powderItem);
        }
        if (wynnItem instanceof SkillPotionItem skillPotionItem) {
            return new SkillPotionOverlay(skillPotionItem);
        }

        return null;
    }

    public interface TextOverlayInfo extends PropertyType {
        TextOverlayProperty.TextOverlay getTextOverlay();

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

    public static class DungeonKeyOverlay implements TextOverlayInfo {
        private static final CustomColor STANDARD_COLOR = CustomColor.fromChatFormatting(ChatFormatting.GOLD);
        private static final CustomColor CORRUPTED_COLOR = CustomColor.fromChatFormatting(ChatFormatting.DARK_RED);

        private final DungeonKeyItem item;

        public DungeonKeyOverlay(DungeonKeyItem item) {
            this.item = item;
        }

        @Override
        public TextOverlayProperty.TextOverlay getTextOverlay() {
            CustomColor textColor = item.isCorrupted() ? CORRUPTED_COLOR : STANDARD_COLOR;
            String dungeon = item.getDungeon();

            return new TextOverlayProperty.TextOverlay(
                    new TextRenderTask(
                            dungeon,
                            TextRenderSetting.DEFAULT
                                    .withCustomColor(textColor)
                                    .withTextShadow(ItemTextOverlayFeature.INSTANCE.dungeonKeyShadow)),
                    -1,
                    1,
                    1f);
        }

        @Override
        public boolean isTextOverlayEnabled() {
            return ItemTextOverlayFeature.INSTANCE.dungeonKeyEnabled;
        }
    }

    public static class AmplifierOverlay implements TextOverlayInfo {
        private final AmplifierItem item;

        public AmplifierOverlay(AmplifierItem item) {
            this.item = item;
        }

        @Override
        public TextOverlayProperty.TextOverlay getTextOverlay() {
            String text = ItemTextOverlayFeature.INSTANCE.amplifierTierRomanNumerals
                    ? MathUtils.toRoman(item.getTier())
                    : String.valueOf(item.getTier());

            return new TextOverlayProperty.TextOverlay(
                    new TextRenderTask(
                            text,
                            TextRenderSetting.DEFAULT
                                    .withCustomColor(ItemTier.LEGENDARY.getHighlightColor())
                                    .withTextShadow(ItemTextOverlayFeature.INSTANCE.amplifierTierShadow)),
                    -1,
                    1,
                    0.75f);
        }

        @Override
        public boolean isTextOverlayEnabled() {
            return ItemTextOverlayFeature.INSTANCE.amplifierTierEnabled;
        }
    }

    public static class TeleportScrollOverlay implements TextOverlayInfo {
        private static final CustomColor CITY_COLOR = CustomColor.fromChatFormatting(ChatFormatting.AQUA);
        private static final CustomColor DUNGEON_COLOR = CustomColor.fromChatFormatting(ChatFormatting.GOLD);

        private final TeleportScrollItem item;

        public TeleportScrollOverlay(TeleportScrollItem item) {
            this.item = item;
        }

        @Override
        public boolean isTextOverlayEnabled() {
            return ItemTextOverlayFeature.INSTANCE.teleportScrollEnabled;
        }

        @Override
        public TextOverlayProperty.TextOverlay getTextOverlay() {
            CustomColor textColor = item.isDungeon() ? DUNGEON_COLOR : CITY_COLOR;
            String location = item.getDestination();

            return new TextOverlayProperty.TextOverlay(
                    new TextRenderTask(
                            location,
                            TextRenderSetting.DEFAULT
                                    .withCustomColor(textColor)
                                    .withTextShadow(ItemTextOverlayFeature.INSTANCE.teleportScrollShadow)),
                    0,
                    0,
                    1f);
        }
    }

    public static class EmeraldPouchOverlay implements TextOverlayInfo {
        private static final CustomColor HIGHLIGHT_COLOR = CustomColor.fromChatFormatting(ChatFormatting.GREEN);

        private final EmeraldPouchItem item;

        public EmeraldPouchOverlay(EmeraldPouchItem item) {
            this.item = item;
        }

        @Override
        public boolean isTextOverlayEnabled() {
            return ItemTextOverlayFeature.INSTANCE.emeraldPouchTierEnabled;
        }

        @Override
        public TextOverlayProperty.TextOverlay getTextOverlay() {
            // convert from roman to arabic if necessary
            String text = ItemTextOverlayFeature.INSTANCE.emeraldPouchTierRomanNumerals
                    ? MathUtils.toRoman(item.getTier())
                    : String.valueOf(item.getTier());

            TextRenderSetting style = TextRenderSetting.DEFAULT
                    .withCustomColor(HIGHLIGHT_COLOR)
                    .withTextShadow(ItemTextOverlayFeature.INSTANCE.emeraldPouchTierShadow);

            return new TextOverlayProperty.TextOverlay(new TextRenderTask(text, style), -1, 1, 0.9f);
        }
    }

    public static class GatheringToolOverlay implements TextOverlayInfo {
        private final GatheringToolItem item;

        public GatheringToolOverlay(GatheringToolItem item) {
            this.item = item;
        }

        @Override
        public boolean isTextOverlayEnabled() {
            return ItemTextOverlayFeature.INSTANCE.gatheringToolTierEnabled;
        }

        @Override
        public TextOverlayProperty.TextOverlay getTextOverlay() {
            // convert from roman to arabic if necessary
            String text = ItemTextOverlayFeature.INSTANCE.gatheringToolTierRomanNumerals
                    ? MathUtils.toRoman(item.getTier())
                    : String.valueOf(item.getTier());

            TextRenderSetting style = TextRenderSetting.DEFAULT
                    .withCustomColor(CustomColor.fromChatFormatting(ChatFormatting.DARK_AQUA))
                    .withTextShadow(ItemTextOverlayFeature.INSTANCE.gatheringToolTierShadow);

            return new TextOverlayProperty.TextOverlay(new TextRenderTask(text, style), -1, 1, 0.9f);
        }
    }

    public static class PowderOverlay implements TextOverlayInfo {
        private final PowderItem item;

        public PowderOverlay(PowderItem item) {
            this.item = item;
        }

        @Override
        public boolean isTextOverlayEnabled() {
            return ItemTextOverlayFeature.INSTANCE.powderTierEnabled;
        }

        @Override
        public TextOverlayProperty.TextOverlay getTextOverlay() {
            CustomColor highlightColor = item.getPowderProfile().element().getColor();

            // convert from roman to arabic if necessary
            String text = ItemTextOverlayFeature.INSTANCE.powderTierRomanNumerals
                    ? MathUtils.toRoman(item.getTier())
                    : String.valueOf(item.getTier());

            return new TextOverlayProperty.TextOverlay(
                    new TextRenderTask(
                            text,
                            TextRenderSetting.DEFAULT
                                    .withCustomColor(highlightColor)
                                    .withTextShadow(ItemTextOverlayFeature.INSTANCE.powderTierShadow)),
                    -1,
                    1,
                    0.75f);
        }
    }

    public static class SkillPotionOverlay implements TextOverlayInfo {
        private final SkillPotionItem item;

        public SkillPotionOverlay(SkillPotionItem item) {
            this.item = item;
        }

        @Override
        public TextOverlayProperty.TextOverlay getTextOverlay() {
            String icon = item.getSkill().getSymbol();
            CustomColor color = CustomColor.fromChatFormatting(item.getSkill().getColor());

            return new TextOverlayProperty.TextOverlay(
                    new TextRenderTask(
                            icon,
                            TextRenderSetting.DEFAULT
                                    .withCustomColor(color)
                                    .withTextShadow(ItemTextOverlayFeature.INSTANCE.skillIconShadow)),
                    -1,
                    1,
                    0.9f);
        }

        @Override
        public boolean isTextOverlayEnabled() {
            return ItemTextOverlayFeature.INSTANCE.skillIconEnabled;
        }
    }
}
