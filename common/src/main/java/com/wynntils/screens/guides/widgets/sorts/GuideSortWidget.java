/*
 * Copyright © Wynntils 2025.
 * This file is released under LGPLv3. See LICENSE for full license details.
 */
package com.wynntils.screens.guides.widgets.sorts;

import com.wynntils.core.components.Services;
import com.wynntils.services.itemfilter.type.ItemSearchQuery;
import com.wynntils.services.itemfilter.type.SortInfo;
import java.util.ArrayList;
import java.util.List;
import java.util.Map;
import net.minecraft.client.gui.GuiGraphics;
import net.minecraft.client.gui.components.AbstractWidget;
import net.minecraft.client.gui.narration.NarrationElementOutput;
import net.minecraft.network.chat.Component;

public class GuideSortWidget extends AbstractWidget {
    private GuideSortButton guideSortButtonPrimary;
    private GuideSortButton guideSortButtonSecondary;

    public GuideSortWidget(int x, int y) {
        super(x, y, 144, 16, Component.empty());
    }

    @Override
    protected void renderWidget(GuiGraphics guiGraphics, int mouseX, int mouseY, float partialTick) {
        guideSortButtonPrimary.render(guiGraphics, mouseX, mouseY, partialTick);
        guideSortButtonSecondary.render(guiGraphics, mouseX, mouseY, partialTick);
    }

    @Override
    public boolean mouseClicked(double mouseX, double mouseY, int button) {
        if (guideSortButtonPrimary.isMouseOver(mouseX, mouseY)) {
            return guideSortButtonPrimary.mouseClicked(mouseX, mouseY, button);
        }

        if (guideSortButtonSecondary.isMouseOver(mouseX, mouseY)) {
            return guideSortButtonSecondary.mouseClicked(mouseX, mouseY, button);
        }

        return super.mouseClicked(mouseX, mouseY, button);
    }

    public void updateFromQuery(ItemSearchQuery searchQuery) {
        if (guideSortButtonPrimary == null || guideSortButtonSecondary == null) return;

        guideSortButtonPrimary.updateFromQuery(searchQuery);
        guideSortButtonSecondary.updateFromQuery(searchQuery);
    }

    public String getSortQuery() {
        return Services.ItemFilter.getItemFilterString(Map.of(), getSortInfos(), List.of());
    }

    public void setPrimarySortButton(GuideSortButton guideSortButton) {
        guideSortButton.setX(getX());
        guideSortButton.setY(getY());
        guideSortButtonPrimary = guideSortButton;
    }

    public void setSecondarySortButton(GuideSortButton guideSortButton) {
        guideSortButton.setX(getX() + 80);
        guideSortButton.setY(getY());
        guideSortButtonSecondary = guideSortButton;
    }

    private List<SortInfo> getSortInfos() {
        List<SortInfo> sorts = new ArrayList<>();

        SortInfo primarySortInfo = guideSortButtonPrimary.getSortInfo();
        SortInfo secondarySortInfo = guideSortButtonSecondary.getSortInfo();

        if (primarySortInfo != null) {
            sorts.add(primarySortInfo);
        }

        if (secondarySortInfo != null) {
            sorts.add(secondarySortInfo);
        }

        return sorts;
    }

    @Override
    protected void updateWidgetNarration(NarrationElementOutput narrationElementOutput) {}
}
