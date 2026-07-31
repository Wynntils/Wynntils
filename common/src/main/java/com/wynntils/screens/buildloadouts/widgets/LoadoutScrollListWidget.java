/*
 * Copyright © Wynntils 2026.
 * This file is released under LGPLv3. See LICENSE for full license details.
 */
package com.wynntils.screens.buildloadouts.widgets;

import com.wynntils.core.components.Models;
import com.wynntils.core.components.Services;
import com.wynntils.core.text.StyledText;
import com.wynntils.models.character.type.ClassType;
import com.wynntils.screens.buildloadouts.BuildLoadoutsScreen;
import com.wynntils.services.loadout.type.Loadout;
import com.wynntils.utils.StringUtils;
import com.wynntils.utils.colors.CommonColors;
import com.wynntils.utils.render.FontRenderer;
import com.wynntils.utils.render.type.HorizontalAlignment;
import com.wynntils.utils.render.type.TextShadow;
import com.wynntils.utils.render.type.VerticalAlignment;
import java.util.ArrayList;
import java.util.Collections;
import java.util.Comparator;
import java.util.List;
import java.util.Map;
import java.util.TreeMap;
import net.minecraft.client.gui.GuiGraphics;
import net.minecraft.client.gui.components.AbstractWidget;
import net.minecraft.network.chat.Component;

public class LoadoutScrollListWidget extends ScrollListWidget {
    private static final int MAX_WIDGETS_PER_PAGE = 7;
    private static final int WIDTH = 133 - 10;
    private static final int HEIGHT = 251 - 5;
    private static final int WIDGET_HEIGHT = 32;
    private static final int WIDGET_HEIGHT_PADDING = 2;
    private static final int WIDGET_HEIGHT_EDGE_PADDING = 5;
    private final int x;
    private final int y;
    private final BuildLoadoutsScreen parent;
    private static final int TEXT_WIDTH_PADDING = 4;
    private static final int TEXT_HEIGHT_PADDING = 10;
    private Boolean searchEmpty = false;
    private boolean loadoutListEmpty = false;

    public LoadoutScrollListWidget(int x, int y, BuildLoadoutsScreen parent) {
        super(
                x,
                y,
                WIDTH,
                HEIGHT,
                WIDGET_HEIGHT,
                WIDGET_HEIGHT_PADDING,
                WIDGET_HEIGHT_EDGE_PADDING,
                MAX_WIDGETS_PER_PAGE);
        this.x = x;
        this.y = y;
        this.parent = parent;
    }

    @Override
    protected void renderWidget(GuiGraphics guiGraphics, int mouseX, int mouseY, float partialTick) {
        super.renderWidget(guiGraphics, mouseX, mouseY, partialTick);

        StyledText text = null;
        if (searchEmpty) {
            text = StyledText.fromComponent(
                    Component.translatable("screens.wynntils.buildLoadouts.loadoutScrollList.searchEmpty"));
        }
        if (loadoutListEmpty) {
            text = StyledText.fromComponent(
                    Component.translatable("screens.wynntils.buildLoadouts.loadoutScrollList.loadoutListEmpty"));
        }

        if (text != null) {
            FontRenderer.getInstance()
                    .renderAlignedTextInBox(
                            guiGraphics,
                            text,
                            this.x + this.width / 2f - 5,
                            this.y + TEXT_HEIGHT_PADDING,
                            this.y + this.height - TEXT_HEIGHT_PADDING,
                            this.width - TEXT_WIDTH_PADDING * 2 - 10,
                            CommonColors.WHITE,
                            HorizontalAlignment.CENTER,
                            VerticalAlignment.TOP,
                            TextShadow.NORMAL);
        }
    }

    @Override
    protected List<AbstractWidget> getWidgets() {
        return Collections.unmodifiableList(parent.loadoutWidgets);
    }

    public void populateLoadouts() {
        parent.loadoutWidgets = new ArrayList<>();

        Map<String, Loadout> savedLoadouts = new TreeMap<>(Services.loadout.getLoadouts());
        ClassType currentClass = Models.Character.getClassType();

        List<String> categoryLoadouts = savedLoadouts.keySet().stream()
                .filter(name -> savedLoadouts.get(name).getMenuCategory() == parent.getCurrentCategory())
                .toList();

        loadoutListEmpty = categoryLoadouts.isEmpty();
        if (loadoutListEmpty) {
            searchEmpty = false;
            return;
        }

        List<String> filteredSorted = categoryLoadouts.stream()
                .filter(this::searchMatches)
                .sorted(Comparator.comparingInt((String name) -> getSortRank(savedLoadouts.get(name), currentClass))
                        .thenComparing(Comparator.naturalOrder()))
                .toList();

        searchEmpty = filteredSorted.isEmpty();
        if (searchEmpty) return;

        for (String name : filteredSorted) {
            Loadout loadout = savedLoadouts.get(name);

            parent.loadoutWidgets.add(new LoadoutWidget(
                    StyledText.fromString(loadout.name()),
                    this.x + 5,
                    this.y + 5 + parent.loadoutWidgets.size() * (WIDGET_HEIGHT + WIDGET_HEIGHT_PADDING),
                    133 - 25 - 4,
                    WIDGET_HEIGHT,
                    loadout,
                    parent));
        }
    }

    /**
     * Lower rank sorts first:
     * 0 - current class, favorited
     * 1 - current class, not favorited
     * 2 - other class, favorited
     * 3 - other class, not favorited
     * 4 - no class (e.g. skill point loadouts), favorited
     * 5 - no class, not favorited
     */
    private int getSortRank(Loadout loadout, ClassType currentClass) {
        boolean favorited = loadout.favorited();

        if (!loadout.hasClassType()) {
            return favorited ? 4 : 5;
        }

        boolean isCurrentClass = loadout.getClassType() == currentClass;

        if (isCurrentClass) {
            return favorited ? 0 : 1;
        } else {
            return favorited ? 2 : 3;
        }
    }

    private boolean searchMatches(String name) {
        return StringUtils.partialMatch(name, parent.searchWidget.getTextBoxInput());
    }
}
