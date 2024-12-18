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
import com.wynntils.models.containers.Container;
import com.wynntils.models.containers.containers.BlacksmithContainer;
import com.wynntils.models.containers.containers.TradeMarketSellContainer;
import com.wynntils.models.gear.type.GearTier;
import com.wynntils.models.items.WynnItem;
import com.wynntils.models.items.items.game.TomeItem;
import com.wynntils.models.items.properties.GearTierItemProperty;
import com.wynntils.models.trademarket.TradeMarketModel;
import com.wynntils.utils.colors.CommonColors;
import com.wynntils.utils.colors.CustomColor;
import com.wynntils.utils.mc.KeyboardUtils;
import com.wynntils.utils.mc.McUtils;
import com.wynntils.utils.render.FontRenderer;
import com.wynntils.utils.render.RenderUtils;
import com.wynntils.utils.render.type.HorizontalAlignment;
import com.wynntils.utils.render.type.TextShadow;
import com.wynntils.utils.render.type.VerticalAlignment;
import java.util.ArrayList;
import java.util.List;
import java.util.Optional;
import net.minecraft.ChatFormatting;
import net.minecraft.client.gui.GuiGraphics;
import net.minecraft.client.gui.components.AbstractWidget;
import net.minecraft.client.gui.narration.NarrationElementOutput;
import net.minecraft.client.gui.screens.inventory.ContainerScreen;
import net.minecraft.client.resources.language.I18n;
import net.minecraft.network.chat.Component;
import net.minecraft.resources.ResourceLocation;
import net.neoforged.bus.api.EventPriority;
import net.neoforged.bus.api.SubscribeEvent;

@ConfigCategory(Category.UTILITIES)
public class ValuablesProtectionFeature extends Feature {
    @Persisted
    public final Config<Float> tradeMarketPriceThreshold = new Config<>(0.9f);

    @Persisted
    public final Config<ProtectableNPCs> mythicWarningNPCs = new Config<>(ProtectableNPCs.BLACKSMITH_AND_TRADE_MARKET);

    @Persisted
    public final Config<ProtectableNPCs> craftedWarningNPCs = new Config<>(ProtectableNPCs.BLACKSMITH);

    @Persisted
    public final Config<Integer> craftedLevelThreshold = new Config<>(100);

    @Persisted
    public final Config<ProtectableNPCs> highRollWarningNPCs = new Config<>(ProtectableNPCs.ALL);

    @Persisted
    public final Config<Float> highRollThreshold = new Config<>(80.0f);

    @Persisted
    public final Config<Boolean> tomesWarning = new Config<>(false);

    private static final ResourceLocation CIRCLE_TEXTURE =
            ResourceLocation.withDefaultNamespace("textures/wynn/gui/tutorial.png");

    private static final int BLACKSMITH_CONFIRM_BUTTON_SLOT = 17;
    private static final int TM_CONFIRM_BUTTON_SLOT = 34;
    private static final int TM_ITEM_SLOT = 22;

    private HintTextWidget ctrlHintTextWidget;
    private final List<HintTextWidget> tmHintTextWidgets = new ArrayList<>();
    private int emphasizeAnimationFrame = 0; // 0-indexed 4 frames of animation
    private int emphasizeAnimationDelay = 0;
    private int emphasizeDirection = 1; // 1 for forward, -1 for reverse
    private boolean drawTradeMarketWarning = false;

    @SubscribeEvent(priority = EventPriority.HIGHEST)
    public void onRenderSlot(SlotRenderEvent.Pre e) {
        if (!(e.getScreen() instanceof ContainerScreen cs)) return;

        Container currentContainer = Models.Container.getCurrentContainer();
        if (currentContainer instanceof BlacksmithContainer blacksmithContainer) {
            int itemIndex = cs.getMenu().getItems().indexOf(e.getSlot().getItem());
            if (!blacksmithContainer.getBounds().slots().contains(itemIndex)) return;

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
        } else if (currentContainer instanceof TradeMarketSellContainer
                && drawTradeMarketWarning
                && e.getSlot().index == TradeMarketModel.TM_SELL_PRICE_SLOT) {
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

        Container currentContainer = Models.Container.getCurrentContainer();
        if (currentContainer instanceof BlacksmithContainer blacksmithContainer) {
            boolean widgetRequired = false;
            for (int i : blacksmithContainer.getBounds().getSlots()) {
                Optional<WynnItem> optItem =
                        Models.Item.getWynnItem(cs.getMenu().getItems().get(i));
                if (optItem.isEmpty() || !(optItem.get() instanceof GearTierItemProperty gtip)) continue;

                if (gtip.getGearTier() == GearTier.MYTHIC
                        && !(optItem.get() instanceof TomeItem && !tomesWarning.get())) {
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
        } else if (currentContainer instanceof TradeMarketSellContainer) {
            resetTradeMarketWarning();
            Optional<GearTierItemProperty> optGearTier = Models.Item.asWynnItemProperty(
                    cs.getMenu().getItems().get(TM_ITEM_SLOT), GearTierItemProperty.class);
            if (optGearTier.isEmpty() || optGearTier.get().getGearTier() != GearTier.MYTHIC) return;

            int salePrice = Models.TradeMarket.getSalePrice();
            int lowestPrice = Models.TradeMarket.getLowestPrice();

            if (salePrice == -1 || lowestPrice == -1) return;

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

                tmHintTextWidgets.add(new HintTextWidget(
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
                tmHintTextWidgets.add(new HintTextWidget(
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
                tmHintTextWidgets.add(new HintTextWidget(
                        cs.width - cs.leftPos + 2,
                        cs.height / 2 - 4,
                        200,
                        11,
                        I18n.get("feature.wynntils.mythicSellWarning.tmWarning3"),
                        HorizontalAlignment.LEFT,
                        CommonColors.GRAY));

                tmHintTextWidgets.forEach(cs::addRenderableOnly);
            } else {
                resetTradeMarketWarning();
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
        resetTradeMarketWarning();
    }

    private void resetTradeMarketWarning() {
        if (McUtils.mc().screen instanceof ContainerScreen cs) {
            cs.removeWidget(ctrlHintTextWidget);
            tmHintTextWidgets.forEach(cs::removeWidget);
        }
        drawTradeMarketWarning = false;
        ctrlHintTextWidget = null;
        tmHintTextWidgets.clear();
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

    private enum ProtectableNPCs {
        BLACKSMITH,
        TRADE_MARKET,
        IDENTIFIER,
        BLACKSMITH_AND_TRADE_MARKET,
        BLACKSMITH_AND_IDENTIFIER,
        TRADE_MARKET_AND_IDENTIFIER,
        ALL
    }
}
