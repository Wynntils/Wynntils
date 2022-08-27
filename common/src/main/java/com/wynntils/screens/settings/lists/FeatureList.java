/*
 * Copyright © Wynntils 2022.
 * This file is released under AGPLv3. See LICENSE for full license details.
 */
package com.wynntils.screens.settings.lists;

import com.mojang.blaze3d.vertex.PoseStack;
import com.wynntils.core.features.DebugFeature;
import com.wynntils.core.features.Feature;
import com.wynntils.core.features.FeatureRegistry;
import com.wynntils.mc.render.RenderUtils;
import com.wynntils.mc.render.Texture;
import com.wynntils.mc.utils.McUtils;
import com.wynntils.screens.settings.WynntilsSettingsScreen;
import com.wynntils.screens.settings.lists.entries.FeatureCategoryEntry;
import com.wynntils.screens.settings.lists.entries.FeatureEntry;
import com.wynntils.screens.settings.lists.entries.FeatureListEntryBase;
import com.wynntils.utils.MathUtils;
import com.wynntils.utils.StringUtils;
import java.util.Objects;
import net.minecraft.client.gui.components.ContainerObjectSelectionList;
import net.minecraft.client.resources.language.I18n;
import net.minecraft.sounds.SoundEvents;

public class FeatureList extends ContainerObjectSelectionList<FeatureListEntryBase> {
    private final WynntilsSettingsScreen settingsScreen;
    private static final int PADDING = 5;

    private float cachedRenderHeight = 0;

    private boolean draggingScrollButton = false;

    public FeatureList(WynntilsSettingsScreen screen) {
        super(
                McUtils.mc(),
                screen.width,
                screen.height,
                (int) (screen.getBarHeight() + 50),
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

        renderScrollButton(poseStack);
    }

    @Override
    protected void renderList(PoseStack poseStack, int x, int y, int mouseX, int mouseY, float partialTick) {
        int itemCount = this.getItemCount();

        int heightOffset = 0;
        int renderedCount = 0;

        for (int i = 0; i < itemCount; i++) {
            FeatureListEntryBase entry = this.getEntry(i);

            int renderHeight = entry.getRenderHeight();

            int top = this.y0 + 1 + heightOffset + (renderedCount * PADDING);
            int bottom = top + renderHeight;

            if (getRowTop(i) < this.y0 || bottom > settingsScreen.height - settingsScreen.getBarHeight() - 10) continue;

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
        float width = getRenderWidth();
        float height = getRenderHeight();
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
    public boolean isMouseOver(double mouseX, double mouseY) {
        return mouseX <= getRenderWidth()
                && mouseY >= this.settingsScreen.getBarHeight()
                && mouseY <= this.settingsScreen.getBarHeight() + this.height;
    }

    @Override
    protected FeatureListEntryBase getEntryAtPosition(double mouseX, double mouseY) {
        if (mouseX > getRenderWidth()) return null;

        int itemCount = this.getItemCount();

        int heightOffset = 0;
        int renderedCount = 0;

        for (int i = 0; i < itemCount; i++) {
            FeatureListEntryBase entry = this.getEntry(i);

            int renderHeight = entry.getRenderHeight();

            int top = this.y0 + 1 + heightOffset + (renderedCount * PADDING);
            int bottom = top + renderHeight;

            if (getRowTop(i) < this.y0 || bottom > settingsScreen.height - settingsScreen.getBarHeight() - 10) continue;

            if (entry instanceof FeatureEntry && top <= mouseY && bottom >= mouseY) {
                return entry;
            }

            heightOffset += renderHeight;
            renderedCount++;
        }

        return null;
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

    @Override
    public boolean mouseClicked(double mouseX, double mouseY, int button) {
        float scrollButtonXPos = getScrollButtonXPos();
        float scrollButtonYPos = getScrollButtonYPos();

        int size = (int) (settingsScreen.width / 65f);

        // Check if we clicked on the scroll button
        if (mouseX >= scrollButtonXPos
                && mouseX <= scrollButtonXPos + size
                && mouseY >= scrollButtonYPos
                && mouseY <= scrollButtonYPos + size) {
            draggingScrollButton = true;
            return false;
        }

        // Update hovered
        // (this is usually done in super.render,
        // but we do not call that this case, and this way,
        // we do not calculate it when we do not use it)
        this.hovered = this.isMouseOver(mouseX, mouseY) ? this.getEntryAtPosition(mouseX, mouseY) : null;

        FeatureListEntryBase hovered = this.getHovered();

        if (hovered instanceof FeatureEntry featureEntry) {
            McUtils.player().playSound(SoundEvents.UI_BUTTON_CLICK, 1f, 1f);
            settingsScreen.setSelectedFeature(featureEntry.getFeature());
            return false;
        }

        return super.mouseClicked(mouseX, mouseY, button);
    }

    @Override
    public boolean mouseDragged(double mouseX, double mouseY, int button, double dragX, double dragY) {
        if (draggingScrollButton) {
            float height = getRenderHeight();

            this.setScrollAmount(
                    this.getScrollAmount() + MathUtils.map((float) dragY, 0, height, 0, this.getMaxScroll()));
        }

        return false;
    }

    @Override
    public boolean mouseReleased(double mouseX, double mouseY, int button) {
        draggingScrollButton = false;

        return super.mouseReleased(mouseX, mouseY, button);
    }

    private void renderScrollButton(PoseStack poseStack) {
        float xPos = getScrollButtonXPos();
        float yPos = getScrollButtonYPos();

        int size = (int) (settingsScreen.width / 65f);

        RenderUtils.drawTexturedRect(
                poseStack,
                Texture.SCROLL_BUTTON.resource(),
                xPos,
                yPos,
                0,
                size,
                size,
                0,
                0,
                Texture.SCROLL_BUTTON.width(),
                Texture.SCROLL_BUTTON.height(),
                Texture.SCROLL_BUTTON.width(),
                Texture.SCROLL_BUTTON.height());
    }

    private float getRenderHeight() {
        return settingsScreen.height - settingsScreen.getBarHeight() * 2;
    }

    private float getRenderWidth() {
        return settingsScreen.width / 5f;
    }

    private float getScrollButtonYPos() {
        float height = getRenderHeight();

        return MathUtils.map(
                (float) this.getScrollAmount(), 0, this.getMaxScroll(), settingsScreen.getBarHeight() + 4, height - 4);
    }

    private float getScrollButtonXPos() {
        return settingsScreen.width / 5.515f;
    }

    public void reAddEntriesWithSearchFilter(String searchText) {
        this.clearEntries();
        this.setScrollAmount(0);

        float renderHeight = 0;

        String lastCategory = "";

        for (Feature feature : FeatureRegistry.getFeatures().stream()
                .filter(feature -> !(feature instanceof DebugFeature))
                .filter(feature -> StringUtils.partialMatch(feature.getTranslatedName(), searchText))
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

            FeatureEntry entry = new FeatureEntry(feature, this, this.settingsScreen);
            this.addEntry(entry);
            renderHeight += entry.getRenderHeight();
        }

        cachedRenderHeight = renderHeight;
    }
}
