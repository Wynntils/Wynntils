/*
 * Copyright © Wynntils 2024-2025.
 * This file is released under LGPLv3. See LICENSE for full license details.
 */
package com.wynntils.screens.guides.widgets.sorts;

import com.google.common.collect.Lists;
import com.wynntils.core.components.Services;
import com.wynntils.core.text.StyledText;
import com.wynntils.screens.guides.WynntilsGuideScreen;
import com.wynntils.services.itemfilter.type.ItemSearchQuery;
import com.wynntils.services.itemfilter.type.ItemStatProvider;
import com.wynntils.services.itemfilter.type.SortDirection;
import com.wynntils.services.itemfilter.type.SortInfo;
import com.wynntils.utils.colors.CommonColors;
import com.wynntils.utils.mc.ComponentUtils;
import com.wynntils.utils.mc.McUtils;
import com.wynntils.utils.render.FontRenderer;
import com.wynntils.utils.render.RenderUtils;
import com.wynntils.utils.render.type.HorizontalAlignment;
import com.wynntils.utils.render.type.TextShadow;
import com.wynntils.utils.render.type.VerticalAlignment;
import java.util.List;
import java.util.Optional;
import net.minecraft.client.gui.GuiGraphics;
import net.minecraft.client.gui.components.AbstractWidget;
import net.minecraft.client.gui.narration.NarrationElementOutput;
import net.minecraft.network.chat.Component;
import org.lwjgl.glfw.GLFW;

public class GuideSortButton extends AbstractWidget {
    private final WynntilsGuideScreen guideScreen;
    private final ItemStatProvider<?> provider;

    private SortDirection sortDirection = null;

    public GuideSortButton(ItemSearchQuery searchQuery, WynntilsGuideScreen guideScreen, Class<?> clazz) {
        super(0, 0, 64, 16, Component.empty());
        this.guideScreen = guideScreen;

        this.provider = Services.ItemFilter.getItemStatProviders().stream()
                .filter(clazz::isInstance)
                .findFirst()
                .orElseThrow(() -> new IllegalStateException("No provider of type " + clazz.getSimpleName()));

        updateFromQuery(searchQuery);
    }

    @Override
    protected void renderWidget(GuiGraphics guiGraphics, int mouseX, int mouseY, float partialTick) {
        RenderUtils.drawRect(
                guiGraphics.pose(),
                CommonColors.BLACK.withAlpha(isHovered ? 0.7f : 0.5f),
                getX(),
                getY(),
                0,
                getWidth(),
                getHeight());

        FontRenderer.getInstance()
                .renderText(
                        guiGraphics.pose(),
                        StyledText.fromString(getSortName()),
                        getX() + getWidth() / 2f,
                        getY() + getHeight() / 2f,
                        sortDirection == null ? CommonColors.WHITE : CommonColors.ORANGE,
                        HorizontalAlignment.CENTER,
                        VerticalAlignment.MIDDLE,
                        TextShadow.NORMAL);

        if (sortDirection != null) {
            FontRenderer.getInstance()
                    .renderText(
                            guiGraphics.pose(),
                            StyledText.fromString(sortDirection == SortDirection.ASCENDING ? "▲" : "▼"),
                            getX() + getWidth(),
                            getY() - 2,
                            CommonColors.YELLOW,
                            HorizontalAlignment.CENTER,
                            VerticalAlignment.TOP,
                            TextShadow.OUTLINE);
        }

        if (isHovered) {
            McUtils.screen()
                    .setTooltipForNextRenderPass(Lists.transform(
                            ComponentUtils.wrapTooltips(
                                    List.of(Component.translatable(
                                            "screens.wynntils.wynntilsGuides.sortWidget.tooltip", getSortName())),
                                    200),
                            Component::getVisualOrderText));
        }
    }

    @Override
    public boolean mouseClicked(double mouseX, double mouseY, int button) {
        if (button == GLFW.GLFW_MOUSE_BUTTON_LEFT || button == GLFW.GLFW_MOUSE_BUTTON_RIGHT) {
            if (sortDirection == SortDirection.DESCENDING) {
                sortDirection = SortDirection.ASCENDING;
            } else if (sortDirection != SortDirection.DESCENDING) {
                sortDirection = SortDirection.DESCENDING;
            }
        } else if (button == GLFW.GLFW_MOUSE_BUTTON_MIDDLE) {
            sortDirection = null;
        }

        guideScreen.updateSearchFromQuickFilters();

        return super.mouseClicked(mouseX, mouseY, button);
    }

    public final void updateFromQuery(ItemSearchQuery searchQuery) {
        Optional<SortInfo> sortInfoOptional = searchQuery.sorts().stream()
                .filter(sortInfo -> sortInfo.provider() == provider)
                .findFirst();

        sortDirection = sortInfoOptional.map(SortInfo::direction).orElse(null);
    }

    protected final SortInfo getSortInfo() {
        if (sortDirection == null) return null;

        return new SortInfo(sortDirection, provider);
    }

    private String getSortName() {
        return provider.getDisplayName();
    }

    @Override
    protected void updateWidgetNarration(NarrationElementOutput narrationElementOutput) {}
}
