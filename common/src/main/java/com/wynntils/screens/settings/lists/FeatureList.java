/*
 * Copyright © Wynntils 2022.
 * This file is released under AGPLv3. See LICENSE for full license details.
 */
package com.wynntils.screens.settings.lists;

import com.mojang.blaze3d.vertex.PoseStack;
import com.wynntils.core.features.Feature;
import com.wynntils.core.features.FeatureRegistry;
import com.wynntils.mc.render.RenderUtils;
import com.wynntils.mc.render.Texture;
import com.wynntils.mc.utils.McUtils;
import com.wynntils.screens.settings.WynntilsSettingsScreen;
import com.wynntils.screens.settings.lists.entries.FeatureCategoryEntry;
import com.wynntils.screens.settings.lists.entries.FeatureEntry;
import com.wynntils.screens.settings.lists.entries.FeatureListEntryBase;
import java.util.Objects;
import net.minecraft.client.gui.components.ContainerObjectSelectionList;
import net.minecraft.client.resources.language.I18n;
import org.apache.commons.lang3.StringUtils;

public class FeatureList extends ContainerObjectSelectionList<FeatureListEntryBase> {
    private final WynntilsSettingsScreen settingsScreen;
    private static final int PADDING = 5;

    private float cachedRenderHeight = 0;

    public FeatureList(WynntilsSettingsScreen screen) {
        super(
                McUtils.mc(),
                screen.width,
                screen.height,
                screen.height / 10 + 15,
                screen.height / 10 + Texture.OVERLAY_SELECTION_GUI.height() - 15,
                25);

        this.settingsScreen = screen;

        this.reAddEntriesWithSearchFilter("");

        this.setRenderBackground(false);
        this.setRenderTopAndBottom(false);
    }

    @Override
    public void render(PoseStack poseStack, int mouseX, int mouseY, float partialTick) {
        this.renderBackground(poseStack);

        int x = this.getRowLeft();
        int y = this.y0 + 4 - (int) this.getScrollAmount();

        this.renderList(poseStack, x, y, mouseX, mouseY, partialTick);
    }

    @Override
    protected void renderList(PoseStack poseStack, int x, int y, int mouseX, int mouseY, float partialTick) {
        int itemCount = this.getItemCount();

        int heightOffset = 0;
        int renderedCount = 0;

        for (int i = 0; i < itemCount; i++) {
            FeatureListEntryBase entry = this.getEntry(i);

            int top = this.y0 + 1 + heightOffset + (renderedCount * PADDING);
            int bottom = top + this.itemHeight;

            if (getRowTop(i) < this.y0 || bottom > settingsScreen.height - settingsScreen.getBarHeight() - 10) continue;

            int renderHeight = entry.getRenderHeight();

            entry.render(
                    poseStack,
                    i,
                    top + 1,
                    this.getRowLeft(),
                    this.getRowWidth(),
                    renderHeight,
                    mouseX,
                    mouseY,
                    Objects.equals(this.getHovered(), entry),
                    partialTick);

            heightOffset += renderHeight;
            renderedCount++;
        }
    }

    @Override
    protected void renderBackground(PoseStack poseStack) {
        float width = settingsScreen.width / 5f;
        float height = settingsScreen.height - settingsScreen.getBarHeight() * 2;
        RenderUtils.drawTexturedRect(
                poseStack,
                Texture.FEATURE_LIST_BACKGROUND.resource(),
                0,
                settingsScreen.getBarHeight(),
                0,
                width,
                height,
                0,
                0,
                Texture.FEATURE_LIST_BACKGROUND.width(),
                Texture.FEATURE_LIST_BACKGROUND.height(),
                Texture.FEATURE_LIST_BACKGROUND.width(),
                Texture.FEATURE_LIST_BACKGROUND.height());
    }

    @Override
    protected int getRowTop(int index) {
        int itemCount = this.getItemCount();

        int height = 0;

        for (int i = 0; i < Math.min(index, itemCount); i++) {
            FeatureListEntryBase entry = this.getEntry(i);

            height += entry.getRenderHeight();
        }

        return this.y0 - (int) this.getScrollAmount() + height + this.headerHeight + 1;
    }

    @Override
    protected int getMaxPosition() {
        return (int) cachedRenderHeight;
    }

    @Override
    public int getMaxScroll() {
        float maxScroll = this.getMaxPosition() - (settingsScreen.height - settingsScreen.getBarHeight() * 2) + 170;
        return (int) Math.max(0, maxScroll);
    }

    @Override
    public int getRowWidth() {
        return settingsScreen.width / 6;
    }

    @Override
    public int getRowLeft() {
        return this.x0 + settingsScreen.width / 90;
    }

    public void reAddEntriesWithSearchFilter(String searchText) {
        this.clearEntries();
        this.setScrollAmount(0);

        float renderHeight = 0;

        String lastCategory = "";

        for (Feature feature : FeatureRegistry.getFeatures().stream()
                .filter(feature -> StringUtils.containsIgnoreCase(feature.getTranslatedName(), searchText))
                .sorted(Feature::compareTo)
                .toList()) {
            if (!Objects.equals(lastCategory, feature.getCategory())) {
                lastCategory = feature.getCategory();

                FeatureCategoryEntry entry;

                if (lastCategory.isEmpty()) {
                    entry = new FeatureCategoryEntry(I18n.get("screens.wynntils.settingsScreen.uncategorized"));
                } else {
                    entry = new FeatureCategoryEntry(lastCategory);
                }

                this.addEntry(entry);

                renderHeight += entry.getRenderHeight();
            }

            FeatureEntry entry = new FeatureEntry(feature, this);
            this.addEntry(entry);
            renderHeight += entry.getRenderHeight();
        }

        cachedRenderHeight = renderHeight;
    }
}
