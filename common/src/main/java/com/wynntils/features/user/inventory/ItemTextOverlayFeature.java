/*
 * Copyright © Wynntils 2022.
 * This file is released under AGPLv3. See LICENSE for full license details.
 */
package com.wynntils.features.user.inventory;

import com.mojang.blaze3d.vertex.PoseStack;
import com.wynntils.core.WynntilsMod;
import com.wynntils.core.components.Models;
import com.wynntils.core.config.Config;
import com.wynntils.core.features.UserFeature;
import com.wynntils.core.features.properties.FeatureCategory;
import com.wynntils.core.features.properties.FeatureInfo;
import com.wynntils.mc.event.HotbarSlotRenderEvent;
import com.wynntils.mc.event.SlotRenderEvent;
import com.wynntils.models.elements.type.Skill;
import com.wynntils.models.items.WynnItem;
import com.wynntils.models.items.WynnItemCache;
import com.wynntils.models.items.items.game.AmplifierItem;
import com.wynntils.models.items.items.game.DungeonKeyItem;
import com.wynntils.models.items.items.game.EmeraldPouchItem;
import com.wynntils.models.items.items.game.GatheringToolItem;
import com.wynntils.models.items.items.game.PotionItem;
import com.wynntils.models.items.items.game.PowderItem;
import com.wynntils.models.items.items.game.TeleportScrollItem;
import com.wynntils.models.items.items.gui.SkillPointItem;
import com.wynntils.utils.MathUtils;
import com.wynntils.utils.colors.CustomColor;
import com.wynntils.utils.render.FontRenderer;
import com.wynntils.utils.render.TextRenderSetting;
import com.wynntils.utils.render.TextRenderTask;
import com.wynntils.utils.render.type.TextShadow;
import java.util.Optional;
import net.minecraft.ChatFormatting;
import net.minecraft.world.item.ItemStack;
import net.minecraftforge.eventbus.api.SubscribeEvent;

@FeatureInfo(category = FeatureCategory.INVENTORY)
public class ItemTextOverlayFeature extends UserFeature {
    @Config
    public boolean powderTierEnabled = true;

    @Config
    public boolean powderTierRomanNumerals = true;

    @Config
    public TextShadow powderTierShadow = TextShadow.OUTLINE;

    @Config
    public boolean emeraldPouchTierEnabled = true;

    @Config
    public boolean emeraldPouchTierRomanNumerals = true;

    @Config
    public TextShadow emeraldPouchTierShadow = TextShadow.OUTLINE;

    @Config
    public boolean gatheringToolTierEnabled = true;

    @Config
    public boolean gatheringToolTierRomanNumerals = true;

    @Config
    public TextShadow gatheringToolTierShadow = TextShadow.OUTLINE;

    @Config
    public boolean teleportScrollEnabled = true;

    @Config
    public TextShadow teleportScrollShadow = TextShadow.OUTLINE;

    @Config
    public boolean dungeonKeyEnabled = true;

    @Config
    public TextShadow dungeonKeyShadow = TextShadow.OUTLINE;

    @Config
    public boolean amplifierTierEnabled = true;

    @Config
    public boolean amplifierTierRomanNumerals = true;

    @Config
    public TextShadow amplifierTierShadow = TextShadow.OUTLINE;

    @Config
    public boolean skillIconEnabled = true;

    @Config
    public TextShadow skillIconShadow = TextShadow.OUTLINE;

    @Config
    public boolean inventoryTextOverlayEnabled = true;

    @Config
    public boolean hotbarTextOverlayEnabled = true;

    @SubscribeEvent
    public void onRenderSlot(SlotRenderEvent.Post e) {
        if (!inventoryTextOverlayEnabled) return;

        drawTextOverlay(e.getSlot().getItem(), e.getSlot().x, e.getSlot().y, false);
    }

    @SubscribeEvent
    public void onRenderHotbarSlot(HotbarSlotRenderEvent.Post e) {
        if (!hotbarTextOverlayEnabled) return;

        drawTextOverlay(e.getItemStack(), e.getX(), e.getY(), true);
    }

    private void drawTextOverlay(ItemStack itemStack, int slotX, int slotY, boolean hotbar) {
        Optional<WynnItem> wynnItemOpt = Models.Item.getWynnItem(itemStack);
        if (wynnItemOpt.isEmpty()) return;

        WynnItem wynnItem = wynnItemOpt.get();
        TextOverlayInfo overlayProperty =
                wynnItem.getCache().getOrCalculate(WynnItemCache.OVERLAY_KEY, () -> calculateOverlay(wynnItem));
        if (overlayProperty == null) return;

        if (!overlayProperty.isTextOverlayEnabled()) return;

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

    private TextOverlayInfo calculateOverlay(WynnItem wynnItem) {
        if (wynnItem instanceof AmplifierItem amplifierItem) {
            return new AmplifierOverlay(amplifierItem);
        }
        if (wynnItem instanceof DungeonKeyItem dungeonKeyItem) {
            return new DungeonKeyOverlay(dungeonKeyItem);
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
        if (wynnItem instanceof SkillPointItem skillPointItem) {
            return new SkillPointOverlay(skillPointItem);
        }
        if (wynnItem instanceof PotionItem potionItem && potionItem.getType().getSkill() != null) {
            return new SkillPotionOverlay(potionItem);
        }
        if (wynnItem instanceof TeleportScrollItem teleportScrollItem) {
            return new TeleportScrollOverlay(teleportScrollItem);
        }

        return null;
    }

    private String valueToString(int value, boolean useRomanNumerals) {
        return useRomanNumerals ? MathUtils.toRoman(value) : String.valueOf(value);
    }

    private interface TextOverlayInfo {
        TextOverlay getTextOverlay();

        boolean isTextOverlayEnabled();
    }

    private final class AmplifierOverlay implements TextOverlayInfo {
        private static final CustomColor HIGHLIGHT_COLOR = new CustomColor(0, 255, 255);

        private final AmplifierItem item;

        private AmplifierOverlay(AmplifierItem item) {
            this.item = item;
        }

        @Override
        public TextOverlay getTextOverlay() {
            String text = valueToString(item.getTier(), amplifierTierRomanNumerals);
            TextRenderSetting style =
                    TextRenderSetting.DEFAULT.withCustomColor(HIGHLIGHT_COLOR).withTextShadow(amplifierTierShadow);

            return new TextOverlay(new TextRenderTask(text, style), -1, 1, 0.75f);
        }

        @Override
        public boolean isTextOverlayEnabled() {
            return amplifierTierEnabled;
        }
    }

    private final class DungeonKeyOverlay implements TextOverlayInfo {
        private static final CustomColor STANDARD_COLOR = CustomColor.fromChatFormatting(ChatFormatting.GOLD);
        private static final CustomColor CORRUPTED_COLOR = CustomColor.fromChatFormatting(ChatFormatting.DARK_RED);

        private final DungeonKeyItem item;

        private DungeonKeyOverlay(DungeonKeyItem item) {
            this.item = item;
        }

        @Override
        public TextOverlay getTextOverlay() {
            CustomColor textColor = item.isCorrupted() ? CORRUPTED_COLOR : STANDARD_COLOR;

            String text = item.getDungeon();
            TextRenderSetting style =
                    TextRenderSetting.DEFAULT.withCustomColor(textColor).withTextShadow(dungeonKeyShadow);

            return new TextOverlay(new TextRenderTask(text, style), -1, 1, 1f);
        }

        @Override
        public boolean isTextOverlayEnabled() {
            return dungeonKeyEnabled;
        }
    }

    private final class EmeraldPouchOverlay implements TextOverlayInfo {
        private static final CustomColor HIGHLIGHT_COLOR = CustomColor.fromChatFormatting(ChatFormatting.GREEN);

        private final EmeraldPouchItem item;

        private EmeraldPouchOverlay(EmeraldPouchItem item) {
            this.item = item;
        }

        @Override
        public boolean isTextOverlayEnabled() {
            return emeraldPouchTierEnabled;
        }

        @Override
        public TextOverlay getTextOverlay() {
            String text = valueToString(item.getTier(), emeraldPouchTierRomanNumerals);
            TextRenderSetting style =
                    TextRenderSetting.DEFAULT.withCustomColor(HIGHLIGHT_COLOR).withTextShadow(emeraldPouchTierShadow);

            return new TextOverlay(new TextRenderTask(text, style), -1, 1, 0.9f);
        }
    }

    private final class GatheringToolOverlay implements TextOverlayInfo {
        private final GatheringToolItem item;

        private GatheringToolOverlay(GatheringToolItem item) {
            this.item = item;
        }

        @Override
        public boolean isTextOverlayEnabled() {
            return gatheringToolTierEnabled;
        }

        @Override
        public TextOverlay getTextOverlay() {
            String text = valueToString(item.getTier(), gatheringToolTierRomanNumerals);
            TextRenderSetting style = TextRenderSetting.DEFAULT
                    .withCustomColor(CustomColor.fromChatFormatting(ChatFormatting.DARK_AQUA))
                    .withTextShadow(gatheringToolTierShadow);

            return new TextOverlay(new TextRenderTask(text, style), -1, 1, 0.9f);
        }
    }

    private final class PowderOverlay implements TextOverlayInfo {
        private final PowderItem item;

        private PowderOverlay(PowderItem item) {
            this.item = item;
        }

        @Override
        public boolean isTextOverlayEnabled() {
            return powderTierEnabled;
        }

        @Override
        public TextOverlay getTextOverlay() {
            CustomColor highlightColor = item.getPowderProfile().element().getColor();

            String text = valueToString(item.getTier(), powderTierRomanNumerals);
            TextRenderSetting style =
                    TextRenderSetting.DEFAULT.withCustomColor(highlightColor).withTextShadow(powderTierShadow);

            return new TextOverlay(new TextRenderTask(text, style), -1, 1, 0.75f);
        }
    }

    private final class SkillPointOverlay implements TextOverlayInfo {
        private final SkillPointItem item;

        private SkillPointOverlay(SkillPointItem item) {
            this.item = item;
        }

        @Override
        public ItemTextOverlayFeature.TextOverlay getTextOverlay() {
            Skill skill = item.getSkill();

            String text = skill.getSymbol();
            TextRenderSetting style = TextRenderSetting.DEFAULT
                    .withCustomColor(CustomColor.fromChatFormatting(skill.getColorCode()))
                    .withTextShadow(skillIconShadow);

            return new TextOverlay(new TextRenderTask(text, style), -1, 1, 0.9f);
        }

        @Override
        public boolean isTextOverlayEnabled() {
            return skillIconEnabled;
        }
    }

    private final class SkillPotionOverlay implements TextOverlayInfo {
        private final PotionItem item;

        private SkillPotionOverlay(PotionItem item) {
            this.item = item;
        }

        @Override
        public TextOverlay getTextOverlay() {
            Skill skill = item.getType().getSkill();

            String text = skill.getSymbol();
            TextRenderSetting style = TextRenderSetting.DEFAULT
                    .withCustomColor(CustomColor.fromChatFormatting(skill.getColorCode()))
                    .withTextShadow(skillIconShadow);

            return new TextOverlay(new TextRenderTask(text, style), -1, 1, 0.9f);
        }

        @Override
        public boolean isTextOverlayEnabled() {
            return skillIconEnabled;
        }
    }

    private final class TeleportScrollOverlay implements TextOverlayInfo {
        private static final CustomColor CITY_COLOR = CustomColor.fromChatFormatting(ChatFormatting.AQUA);
        private static final CustomColor DUNGEON_COLOR = CustomColor.fromChatFormatting(ChatFormatting.GOLD);

        private final TeleportScrollItem item;

        private TeleportScrollOverlay(TeleportScrollItem item) {
            this.item = item;
        }

        @Override
        public boolean isTextOverlayEnabled() {
            return teleportScrollEnabled;
        }

        @Override
        public TextOverlay getTextOverlay() {
            CustomColor textColor = item.isDungeon() ? DUNGEON_COLOR : CITY_COLOR;

            String text = item.getDestination();
            TextRenderSetting style =
                    TextRenderSetting.DEFAULT.withCustomColor(textColor).withTextShadow(teleportScrollShadow);

            return new TextOverlay(new TextRenderTask(text, style), 0, 0, 1f);
        }
    }

    /**
     * Describes an item's text overlay, with its color, position relative to the item's slot, and text scale.
     */
    private record TextOverlay(TextRenderTask task, int xOffset, int yOffset, float scale) {}
}
