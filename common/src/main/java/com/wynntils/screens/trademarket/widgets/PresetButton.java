/*
 * Copyright © Wynntils 2023-2025.
 * This file is released under LGPLv3. See LICENSE for full license details.
 */
package com.wynntils.screens.trademarket.widgets;

import com.wynntils.core.components.Models;
import com.wynntils.screens.base.TooltipProvider;
import com.wynntils.screens.base.widgets.WynntilsButton;
import com.wynntils.screens.trademarket.TradeMarketSearchResultScreen;
import com.wynntils.utils.mc.RenderedStringUtils;
import com.wynntils.utils.render.FontRenderer;
import com.wynntils.utils.render.RenderUtils;
import com.wynntils.utils.render.Texture;
import java.util.ArrayList;
import java.util.List;
import java.util.Optional;
import net.minecraft.ChatFormatting;
import net.minecraft.client.gui.GuiGraphics;
import net.minecraft.network.chat.Component;
import org.lwjgl.glfw.GLFW;

public class PresetButton extends WynntilsButton implements TooltipProvider {
    private final int presetId;
    private final TradeMarketSearchResultScreen tradeMarketSearchResultScreen;

    public PresetButton(int x, int y, int width, int height, int presetId, TradeMarketSearchResultScreen screen) {
        super(x, y, width, height, Component.literal("Preset Button"));
        this.presetId = presetId;
        this.tradeMarketSearchResultScreen = screen;
    }

    @Override
    protected void renderWidget(GuiGraphics guiGraphics, int mouseX, int mouseY, float partialTick) {
        Texture itemTexture = isSavedPreset() ? Texture.SAVED_PRESET : Texture.PRESET;

        RenderUtils.drawTexturedRect(guiGraphics.pose(), itemTexture, this.getX(), this.getY());
    }

    @Override
    public boolean mouseClicked(double mouseX, double mouseY, int button) {
        if (!isMouseOver(mouseX, mouseY)) return false;

        if (button == GLFW.GLFW_MOUSE_BUTTON_LEFT) {
            String lastSearchFilter =
                    tradeMarketSearchResultScreen.getSearchQuery().queryString();
            if (lastSearchFilter.isEmpty()) return true;

            Models.TradeMarket.setPresetFilter(presetId, lastSearchFilter);
            return true;
        } else if (button == GLFW.GLFW_MOUSE_BUTTON_RIGHT) {
            Optional<String> presetFilterOpt = Models.TradeMarket.getPresetFilter(presetId);
            if (presetFilterOpt.isEmpty()) return true;

            String filter = presetFilterOpt.get();
            tradeMarketSearchResultScreen.setSearchQuery(filter);
            return true;
        }

        return false;
    }

    @Override
    public void onPress() {}

    @Override
    public List<Component> getTooltipLines() {
        List<Component> list = new ArrayList<>();

        if (isSavedPreset()) {
            list.add(Component.translatable("screens.wynntils.tradeMarketSearchResult.button.savedPreset", presetId + 1)
                    .withStyle(ChatFormatting.BOLD)
                    .withStyle(ChatFormatting.YELLOW));
        } else {
            list.add(Component.translatable("screens.wynntils.tradeMarketSearchResult.button.preset", presetId + 1)
                    .withStyle(ChatFormatting.BOLD));
        }

        list.add(Component.empty());

        if (isSavedPreset()) {
            Models.TradeMarket.getPresetFilter(presetId).ifPresent(presetString -> {
                list.add(Component.literal(RenderedStringUtils.getMaxFittingText(
                                presetString, 150, FontRenderer.getInstance().getFont()))
                        .withStyle(ChatFormatting.GRAY));
                list.add(Component.empty());
            });
        }

        list.add(Component.translatable("screens.wynntils.tradeMarketSearchResult.button.clickToSave")
                .withStyle(ChatFormatting.GREEN));
        list.add(Component.translatable("screens.wynntils.tradeMarketSearchResult.button.clickToLoad")
                .withStyle(ChatFormatting.YELLOW));

        return list;
    }

    private boolean isSavedPreset() {
        return Models.TradeMarket.getPresetFilter(presetId).isPresent();
    }
}
