/*
 * Copyright © Wynntils 2024.
 * This file is released under LGPLv3. See LICENSE for full license details.
 */
package com.wynntils.features.utilities;

import com.mojang.blaze3d.systems.RenderSystem;
import com.wynntils.core.components.Managers;
import com.wynntils.core.components.Models;
import com.wynntils.core.consumers.features.Feature;
import com.wynntils.core.persisted.Persisted;
import com.wynntils.core.persisted.config.Category;
import com.wynntils.core.persisted.config.Config;
import com.wynntils.core.persisted.config.ConfigCategory;
import com.wynntils.core.text.StyledText;
import com.wynntils.mc.event.ContainerClickEvent;
import com.wynntils.mc.event.ContainerCloseEvent;
import com.wynntils.mc.event.SetSlotEvent;
import com.wynntils.mc.event.SlotRenderEvent;
import com.wynntils.mc.event.TickEvent;
import com.wynntils.models.gear.type.GearTier;
import com.wynntils.models.items.WynnItem;
import com.wynntils.models.items.items.game.TomeItem;
import com.wynntils.models.items.properties.GearTierItemProperty;
import com.wynntils.utils.colors.CommonColors;
import com.wynntils.utils.colors.CustomColor;
import com.wynntils.utils.mc.KeyboardUtils;
import com.wynntils.utils.mc.LoreUtils;
import com.wynntils.utils.mc.McUtils;
import com.wynntils.utils.render.FontRenderer;
import com.wynntils.utils.render.RenderUtils;
import com.wynntils.utils.render.type.HorizontalAlignment;
import com.wynntils.utils.render.type.TextShadow;
import com.wynntils.utils.render.type.VerticalAlignment;
import java.util.ArrayList;
import java.util.List;
import java.util.Optional;
import java.util.regex.Matcher;
import java.util.regex.Pattern;
import net.minecraft.ChatFormatting;
import net.minecraft.client.gui.GuiGraphics;
import net.minecraft.client.gui.components.AbstractWidget;
import net.minecraft.client.gui.narration.NarrationElementOutput;
import net.minecraft.client.gui.screens.inventory.ContainerScreen;
import net.minecraft.client.resources.language.I18n;
import net.minecraft.network.chat.Component;
import net.minecraft.resources.ResourceLocation;
import net.minecraft.world.item.ItemStack;
import net.neoforged.bus.api.EventPriority;
import net.neoforged.bus.api.SubscribeEvent;

@ConfigCategory(Category.UTILITIES)
public class MythicSellWarningFeature extends Feature {
    @Persisted
    public final Config<Boolean> tomesWarning = new Config<>(false);

    @Persisted
    public final Config<Float> tradeMarketPriceThreshold = new Config<>(0.9f);

    private static final ResourceLocation CIRCLE_TEXTURE =
            ResourceLocation.withDefaultNamespace("textures/wynn/gui/tutorial.png");

    private static final String BLACKSMITH_TITLE = "\uDAFF\uDFF8\uE016";
    private static final int BLACKSMITH_CONFIRM_BUTTON_SLOT = 17;

    private static final String TM_SELL_TITLE = "\uDAFF\uDFE8\uE014\uDAFF\uDF951";
    private static final int TM_ITEM_SLOT = 22;
    private static final int TM_SELL_PRICE_SLOT = 28;
    private static final Pattern TM_SELL_PRICE_PATTERN = Pattern.compile("§a- §7Total:§f (\\d{1,3}(?:,\\d{3})*)");
    private static final int TM_PRICE_CHECK_SLOT = 51;
    private static final Pattern TM_PRICE_CHECK_PATTERN =
            Pattern.compile("§7Cheapest Sell Offer: §f(\\d{1,3}(?:,\\d{3})*)");
    private static final int TM_CONFIRM_BUTTON_SLOT = 34;

    private HintTextWidget ctrlHintTextWidget;
    private final List<HintTextWidget> hintTextWidgets = new ArrayList<>();
    private int emphasizeAnimationFrame = 0; // 0-indexed 4 frames of animation
    private int emphasizeAnimationDelay = 0;
    private int emphasizeDirection = 1; // 1 for forward, -1 for reverse
    private boolean drawTradeMarketWarning = false;

    @SubscribeEvent(priority = EventPriority.HIGHEST)
    public void onRenderSlot(SlotRenderEvent.Pre e) {
        if (!(e.getScreen() instanceof ContainerScreen cs)) return;

        if (cs.getTitle().getString().equals(BLACKSMITH_TITLE)) {
            int itemIndex = cs.getMenu().getItems().indexOf(e.getSlot().getItem());
            if (itemIndex > 24 || itemIndex < 11) return; // Not in the sell slots

            Optional<WynnItem> item = Models.Item.getWynnItem(e.getSlot().getItem());
            if (item.isEmpty() || !(item.get() instanceof GearTierItemProperty gtip)) return;

            if (gtip.getGearTier() != GearTier.MYTHIC) return;

            if (item.get() instanceof TomeItem && !tomesWarning.get()) return;

            RenderSystem.enableDepthTest();
            RenderUtils.drawTexturedRectWithColor(
                    e.getPoseStack(),
                    CIRCLE_TEXTURE,
                    CommonColors.RED,
                    e.getSlot().x - 16,
                    e.getSlot().y - 16,
                    200,
                    48,
                    48,
                    0,
                    emphasizeAnimationFrame * 48,
                    48,
                    48,
                    48,
                    192);
            RenderSystem.disableDepthTest();
        } else if (cs.getTitle().getString().equals(TM_SELL_TITLE)
                && drawTradeMarketWarning
                && e.getSlot().index == TM_SELL_PRICE_SLOT) {
            RenderSystem.enableDepthTest();
            RenderUtils.drawTexturedRectWithColor(
                    e.getPoseStack(),
                    CIRCLE_TEXTURE,
                    CommonColors.RED,
                    e.getSlot().x - 16,
                    e.getSlot().y - 16,
                    200,
                    48,
                    48,
                    0,
                    emphasizeAnimationFrame * 48,
                    48,
                    48,
                    48,
                    192);
            RenderSystem.disableDepthTest();
        }
    }

    @SubscribeEvent
    public void onSetSlot(SetSlotEvent.Post e) {
        if (!(McUtils.mc().screen instanceof ContainerScreen cs)) return;

        if (cs.getTitle().getString().equals(BLACKSMITH_TITLE)) {
            boolean widgetRequired = false;
            for (int i = 11; i <= 24; i++) {
                Optional<GearTierItemProperty> optGearTier =
                        Models.Item.asWynnItemProperty(cs.getMenu().getItems().get(i), GearTierItemProperty.class);

                if (optGearTier.isPresent() && optGearTier.get().getGearTier() == GearTier.MYTHIC) {
                    widgetRequired = true;
                    break;
                }
            }

            if (widgetRequired && ctrlHintTextWidget == null) {
                ctrlHintTextWidget = new HintTextWidget(
                        cs.width / 2,
                        cs.topPos - 6,
                        cs.width - 2 * cs.leftPos,
                        11,
                        I18n.get("feature.wynntils.mythicSellWarning.ctrlClick"),
                        HorizontalAlignment.CENTER,
                        CommonColors.WHITE);
                cs.addRenderableOnly(ctrlHintTextWidget);
            } else if (!widgetRequired && ctrlHintTextWidget != null) {
                cs.removeWidget(ctrlHintTextWidget);
                ctrlHintTextWidget = null;
            }
        } else if (cs.getTitle().getString().equals(TM_SELL_TITLE)) {
            Optional<GearTierItemProperty> optGearTier = Models.Item.asWynnItemProperty(
                    cs.getMenu().getItems().get(TM_ITEM_SLOT), GearTierItemProperty.class);
            if (optGearTier.isEmpty() || optGearTier.get().getGearTier() != GearTier.MYTHIC) return;

            int salePrice = getSalePrice();
            int lowestPrice = getLowestPrice();

            if (salePrice == -1 || lowestPrice == -1) {
                drawTradeMarketWarning = false;
                return;
            }

            if (salePrice < lowestPrice * tradeMarketPriceThreshold.get()) {
                drawTradeMarketWarning = true;

                ctrlHintTextWidget = new HintTextWidget(
                        cs.width - cs.leftPos + 2,
                        cs.height / 2 - 46,
                        200,
                        11,
                        I18n.get("feature.wynntils.mythicSellWarning.ctrlClick"),
                        HorizontalAlignment.LEFT,
                        CommonColors.WHITE);
                cs.addRenderableOnly(ctrlHintTextWidget);

                hintTextWidgets.clear();
                hintTextWidgets.add(new HintTextWidget(
                        cs.width - cs.leftPos + 2,
                        cs.height / 2 - 34,
                        200,
                        11,
                        I18n.get(
                                "feature.wynntils.mythicSellWarning.tmWarning1",
                                salePrice + " " + ChatFormatting.DARK_GRAY + "("
                                        + Models.Emerald.getFormattedString(salePrice, false) + ")"
                                        + ChatFormatting.RESET,
                                tradeMarketPriceThreshold.get() * 100),
                        HorizontalAlignment.LEFT,
                        CommonColors.LIGHT_GRAY));
                hintTextWidgets.add(new HintTextWidget(
                        cs.width - cs.leftPos + 2,
                        cs.height / 2 - 22,
                        200,
                        11,
                        I18n.get(
                                "feature.wynntils.mythicSellWarning.tmWarning2",
                                lowestPrice + " " + ChatFormatting.DARK_GRAY + "("
                                        + Models.Emerald.getFormattedString(lowestPrice, false) + ")"
                                        + ChatFormatting.RESET),
                        HorizontalAlignment.LEFT,
                        CommonColors.LIGHT_GRAY));
                hintTextWidgets.add(new HintTextWidget(
                        cs.width - cs.leftPos + 2,
                        cs.height / 2 - 4,
                        200,
                        11,
                        I18n.get("feature.wynntils.mythicSellWarning.tmWarning3"),
                        HorizontalAlignment.LEFT,
                        CommonColors.GRAY));

                hintTextWidgets.forEach(cs::addRenderableOnly);
            } else {
                drawTradeMarketWarning = false;
                cs.removeWidget(ctrlHintTextWidget);
                ctrlHintTextWidget = null;
                hintTextWidgets.forEach(cs::removeWidget);
                hintTextWidgets.clear();
            }
        }
    }

    @SubscribeEvent
    public void onSlotClicked(ContainerClickEvent e) {
        if (ctrlHintTextWidget == null || KeyboardUtils.isControlDown()) return;
        if (e.getSlotNum() != BLACKSMITH_CONFIRM_BUTTON_SLOT && e.getSlotNum() != TM_CONFIRM_BUTTON_SLOT) return;

        e.setCanceled(true);
        for (int i = 0; i < 12; i += 6) {
            Managers.TickScheduler.scheduleLater(() -> ctrlHintTextWidget.setTextColor(CommonColors.RED), i);
            Managers.TickScheduler.scheduleLater(() -> ctrlHintTextWidget.setTextColor(CommonColors.WHITE), i + 3);
        }
    }

    @SubscribeEvent
    public void onTick(TickEvent e) {
        if (!(McUtils.mc().screen instanceof ContainerScreen)) return;

        if (++emphasizeAnimationDelay % 4 == 0) {
            emphasizeAnimationFrame += emphasizeDirection;
            if (emphasizeAnimationFrame == 4 || emphasizeAnimationFrame == -1) {
                emphasizeDirection *= -1;
                emphasizeAnimationFrame =
                        Math.max(0, Math.min(3, emphasizeAnimationFrame)); // Keeps the frame within bounds
            }
        }
    }

    @SubscribeEvent
    public void onContainerClose(ContainerCloseEvent.Post e) {
        drawTradeMarketWarning = false;
        ctrlHintTextWidget = null;
        hintTextWidgets.clear();
    }

    private int getSalePrice() {
        if (!(McUtils.mc().screen instanceof ContainerScreen cs)
                || !cs.getTitle().getString().equals(TM_SELL_TITLE)) return -1;

        ItemStack priceCheckItem = cs.getMenu().getItems().get(TM_SELL_PRICE_SLOT);
        if (priceCheckItem.isEmpty()) return -1;

        String lore = LoreUtils.getStringLore(priceCheckItem).getString();
        Matcher priceCheckMatcher = TM_SELL_PRICE_PATTERN.matcher(lore);
        if (priceCheckMatcher.find()) {
            String priceCheckString = priceCheckMatcher.group(1);
            return Integer.parseInt(priceCheckString.replace(",", ""));
        }

        return -1;
    }

    private int getLowestPrice() {
        if (!(McUtils.mc().screen instanceof ContainerScreen cs)
                || !cs.getTitle().getString().equals(TM_SELL_TITLE)) return -1;

        ItemStack priceCheckItem = cs.getMenu().getItems().get(TM_PRICE_CHECK_SLOT);
        if (priceCheckItem.isEmpty()) return -1;

        String lore = LoreUtils.getStringLore(priceCheckItem).getString();
        Matcher priceCheckMatcher = TM_PRICE_CHECK_PATTERN.matcher(lore);
        if (priceCheckMatcher.find()) {
            String priceCheckString = priceCheckMatcher.group(1);
            return Integer.parseInt(priceCheckString.replace(",", ""));
        }

        return -1;
    }

    private static final class HintTextWidget extends AbstractWidget {
        private final String text;
        private final HorizontalAlignment horizontalAlignment;
        private CustomColor textColor;

        private HintTextWidget(
                int x,
                int y,
                int width,
                int height,
                String text,
                HorizontalAlignment horizontalAlignment,
                CustomColor defaultColor) {
            super(x, y, width, height, Component.literal(text));
            this.text = text;
            this.horizontalAlignment = horizontalAlignment;
            this.textColor = defaultColor;
        }

        @Override
        protected void renderWidget(GuiGraphics guiGraphics, int mouseX, int mouseY, float partialTick) {
            FontRenderer.getInstance()
                    .renderText(
                            guiGraphics.pose(),
                            StyledText.fromString(text),
                            getX(),
                            getY(),
                            textColor,
                            horizontalAlignment,
                            VerticalAlignment.BOTTOM,
                            TextShadow.NORMAL);
        }

        @Override
        protected void updateWidgetNarration(NarrationElementOutput narrationElementOutput) {}

        public void setTextColor(CustomColor textColor) {
            this.textColor = textColor;
        }
    }
}
