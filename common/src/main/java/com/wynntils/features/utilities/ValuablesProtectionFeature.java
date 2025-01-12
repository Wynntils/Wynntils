/*
 * Copyright © Wynntils 2024-2025.
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
import com.wynntils.models.containers.containers.ItemIdentifierContainer;
import com.wynntils.models.containers.containers.TradeMarketSellContainer;
import com.wynntils.models.containers.type.BoundedContainerProperty;
import com.wynntils.models.gear.type.GearTier;
import com.wynntils.models.items.WynnItem;
import com.wynntils.models.items.items.game.TomeItem;
import com.wynntils.models.items.properties.GearTierItemProperty;
import com.wynntils.models.items.properties.IdentifiableItemProperty;
import com.wynntils.models.items.properties.LeveledItemProperty;
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
import net.minecraft.client.gui.Font;
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
    private final Config<Float> tradeMarketPriceThreshold = new Config<>(90.0f);

    @Persisted
    private final Config<ProtectableNPCs> mythicWarningNPCs = new Config<>(ProtectableNPCs.BLACKSMITH_AND_TRADE_MARKET);

    @Persisted
    private final Config<ProtectableNPCs> highRollWarningNPCs = new Config<>(ProtectableNPCs.ALL);

    @Persisted
    private final Config<Float> highRollThreshold = new Config<>(80.0f);

    @Persisted
    private final Config<Boolean> tomesWarning = new Config<>(false);

    @Persisted
    private final Config<Integer> craftedBlacksmithLevel = new Config<>(0);

    private static final ResourceLocation CIRCLE_TEXTURE =
            ResourceLocation.withDefaultNamespace("textures/wynn/gui/tutorial.png");

    private static final int BLACKSMITH_IDENTIFIER_CONFIRM_BUTTON_SLOT = 17;
    private static final int TM_ITEM_SLOT = 22;
    private static final int TM_PRICE_SLOT = 28;
    private static final int TM_CONFIRM_BUTTON_SLOT = 34;

    private Class<? extends BoundedContainerProperty> currentContainerType;
    /** Represents the slots to draw red circles on where necessary.*/
    private List<Integer> slotsToWarn = new ArrayList<>();

    private HintTextWidget ctrlHintTextWidget;
    private final List<HintTextWidget> tmHintTextWidgets = new ArrayList<>();
    private int emphasizeAnimationFrame = 0; // 0-indexed 4 frames of animation
    private int emphasizeAnimationDelay = 0;
    private int emphasizeDirection = 1; // 1 for forward, -1 for reverse

    @SubscribeEvent(priority = EventPriority.HIGHEST)
    public void onRenderSlot(SlotRenderEvent.Pre e) {
        Container currentContainer = Models.Container.getCurrentContainer();
        if (currentContainerType != null && !currentContainerType.isInstance(currentContainer)) return;
        if (!slotsToWarn.contains(e.getSlot().index)) return;

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

    @SubscribeEvent
    public void onSetSlot(SetSlotEvent.Post e) {
        if (!(McUtils.mc().screen instanceof ContainerScreen cs)) return;

        Container currentContainer = Models.Container.getCurrentContainer();
        if (currentContainer == null) return;

        resetAll();
        doBlacksmithIdentifierChecks(currentContainer, cs);
        doTradeMarketChecks(currentContainer, cs);
    }

    private void doBlacksmithIdentifierChecks(Container currentContainer, ContainerScreen cs) {
        for (Class<? extends BoundedContainerProperty> container :
                ProtectableNPCs.BLACKSMITH_AND_IDENTIFIER.getContainers()) {
            if (!currentContainer.getClass().equals(container)) continue;
            currentContainerType = container;

            for (int i :
                    ((BoundedContainerProperty) currentContainer).getBounds().getSlots()) {
                Optional<WynnItem> itemOpt =
                        Models.Item.getWynnItem(cs.getMenu().getItems().get(i));
                if (itemOpt.isEmpty()) continue;
                WynnItem item = itemOpt.get();

                // check tomes since we can return early
                if (item instanceof TomeItem && !tomesWarning.get()) continue;

                // set a single flag for all the checks, first do high roll
                boolean shouldWarn = highRollWarningNPCs.get().getContainers().contains(container)
                        && item instanceof IdentifiableItemProperty<?, ?> identifiableItemProperty
                        && identifiableItemProperty.getOverallPercentage() >= highRollThreshold.get();

                if (item instanceof GearTierItemProperty gtip) {
                    if (mythicWarningNPCs.get().getContainers().contains(container)
                            && gtip.getGearTier() == GearTier.MYTHIC) {
                        shouldWarn = true;
                    }
                    if (craftedBlacksmithLevel.get() > 0
                            && gtip.getGearTier() == GearTier.CRAFTED
                            && item instanceof LeveledItemProperty lip
                            && lip.getLevel() >= craftedBlacksmithLevel.get()) {
                        shouldWarn = true;
                    }
                }

                if (shouldWarn) {
                    slotsToWarn.add(i);
                }
            }
            break;
        }

        if (!slotsToWarn.isEmpty()) {
            ctrlHintTextWidget = new HintTextWidget(
                    cs.width / 2,
                    cs.topPos - 6,
                    cs.width,
                    11,
                    I18n.get(
                            "feature.wynntils.valuablesProtection.ctrlClick",
                            I18n.get("feature.wynntils.valuablesProtection."
                                    + (currentContainerType == ItemIdentifierContainer.class
                                            ? "identifying"
                                            : "selling"))),
                    HorizontalAlignment.CENTER,
                    CommonColors.WHITE);
            cs.addRenderableOnly(ctrlHintTextWidget);
        }
    }

    private void doTradeMarketChecks(Container currentContainer, ContainerScreen cs) {
        if (currentContainer instanceof TradeMarketSellContainer) {
            Optional<WynnItem> optItem =
                    Models.Item.getWynnItem(cs.getMenu().getItems().get(TM_ITEM_SLOT));
            if (optItem.isEmpty()) return;
            WynnItem item = optItem.get();

            // set a single flag for all the checks, first do high roll
            boolean warnableItem = highRollWarningNPCs.get().getContainers().contains(TradeMarketSellContainer.class)
                    && item instanceof IdentifiableItemProperty<?, ?> identifiableItemProperty
                    && identifiableItemProperty.getOverallPercentage() >= highRollThreshold.get();

            if (item instanceof GearTierItemProperty gtip) {
                if (mythicWarningNPCs.get().getContainers().contains(TradeMarketSellContainer.class)
                        && gtip.getGearTier() == GearTier.MYTHIC) {
                    warnableItem = true;
                }
            }

            if (!warnableItem) return;

            int salePrice = Models.TradeMarket.getUnitPrice();
            int lowestPrice = Models.TradeMarket.getLowestPrice();

            if (salePrice == -1 || lowestPrice == -1) return;
            slotsToWarn.add(TM_PRICE_SLOT);

            if (salePrice < lowestPrice * (tradeMarketPriceThreshold.get() / 100d)) {
                ctrlHintTextWidget = new HintTextWidget(
                        cs.width - cs.leftPos + 2,
                        cs.height / 2,
                        cs.leftPos,
                        11,
                        I18n.get(
                                "feature.wynntils.valuablesProtection.ctrlClick",
                                I18n.get("feature.wynntils.valuablesProtection.selling")),
                        HorizontalAlignment.LEFT,
                        CommonColors.WHITE);
                cs.addRenderableOnly(ctrlHintTextWidget);

                tmHintTextWidgets.add(new HintTextWidget(
                        cs.width - cs.leftPos + 2,
                        cs.height / 2 + 20,
                        cs.leftPos,
                        11,
                        I18n.get(
                                "feature.wynntils.valuablesProtection.tmWarning",
                                salePrice + " " + ChatFormatting.DARK_GRAY + "("
                                        + Models.Emerald.getFormattedString(salePrice, false) + ")"
                                        + ChatFormatting.RESET,
                                tradeMarketPriceThreshold.get(),
                                lowestPrice + " " + ChatFormatting.DARK_GRAY + "("
                                        + Models.Emerald.getFormattedString(lowestPrice, false) + ")"
                                        + ChatFormatting.RESET),
                        HorizontalAlignment.LEFT,
                        CommonColors.LIGHT_GRAY));
                tmHintTextWidgets.add(new HintTextWidget(
                        cs.width - cs.leftPos + 2,
                        cs.height / 2 + 56,
                        cs.leftPos,
                        11,
                        I18n.get("feature.wynntils.valuablesProtection.settingsHint"),
                        HorizontalAlignment.LEFT,
                        CommonColors.GRAY));

                tmHintTextWidgets.forEach(cs::addRenderableOnly);
            } else {
                resetAll();
            }
        }
    }

    @SubscribeEvent
    public void onSlotClicked(ContainerClickEvent e) {
        if (slotsToWarn.isEmpty() || KeyboardUtils.isControlDown()) return;
        if (e.getSlotNum() != BLACKSMITH_IDENTIFIER_CONFIRM_BUTTON_SLOT && e.getSlotNum() != TM_CONFIRM_BUTTON_SLOT)
            return;

        e.setCanceled(true);
        for (int i = 0; i < 12; i += 6) {
            Managers.TickScheduler.scheduleLater(
                    () -> {
                        if (ctrlHintTextWidget != null) {
                            ctrlHintTextWidget.setTextColor(CommonColors.RED);
                        }
                    },
                    i);
            Managers.TickScheduler.scheduleLater(
                    () -> {
                        if (ctrlHintTextWidget != null) {
                            ctrlHintTextWidget.setTextColor(CommonColors.WHITE);
                        }
                    },
                    i + 3);
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
        resetAll();
    }

    private void resetAll() {
        if (McUtils.mc().screen instanceof ContainerScreen cs) {
            cs.removeWidget(ctrlHintTextWidget);
            tmHintTextWidgets.forEach(cs::removeWidget);
        }
        ctrlHintTextWidget = null;
        tmHintTextWidgets.clear();
        slotsToWarn = new ArrayList<>();
        currentContainerType = null;
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
                            getWidth(),
                            textColor,
                            horizontalAlignment,
                            VerticalAlignment.BOTTOM,
                            TextShadow.NORMAL,
                            Font.DisplayMode.NORMAL);
        }

        @Override
        protected void updateWidgetNarration(NarrationElementOutput narrationElementOutput) {}

        public void setTextColor(CustomColor textColor) {
            this.textColor = textColor;
        }
    }

    private enum ProtectableNPCs {
        NONE(List.of()),
        BLACKSMITH(List.of(BlacksmithContainer.class)),
        TRADE_MARKET(List.of(TradeMarketSellContainer.class)),
        IDENTIFIER(List.of(ItemIdentifierContainer.class)),
        BLACKSMITH_AND_TRADE_MARKET(List.of(BlacksmithContainer.class, TradeMarketSellContainer.class)),
        BLACKSMITH_AND_IDENTIFIER(List.of(BlacksmithContainer.class, ItemIdentifierContainer.class)),
        TRADE_MARKET_AND_IDENTIFIER(List.of(TradeMarketSellContainer.class, ItemIdentifierContainer.class)),
        ALL(List.of(BlacksmithContainer.class, TradeMarketSellContainer.class, ItemIdentifierContainer.class));

        private final List<Class<? extends BoundedContainerProperty>> containers;

        ProtectableNPCs(List<Class<? extends BoundedContainerProperty>> containers) {
            this.containers = containers;
        }

        public List<Class<? extends BoundedContainerProperty>> getContainers() {
            return containers;
        }
    }
}
