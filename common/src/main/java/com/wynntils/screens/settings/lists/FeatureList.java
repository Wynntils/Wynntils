/*
 * Copyright © Wynntils 2022.
 * This file is released under AGPLv3. See LICENSE for full license details.
 */
package com.wynntils.screens.settings.lists;

import com.mojang.blaze3d.vertex.PoseStack;
import com.wynntils.core.WynntilsMod;
import com.wynntils.core.features.DebugFeature;
import com.wynntils.core.features.Feature;
import com.wynntils.core.features.FeatureRegistry;
import com.wynntils.core.features.UserFeature;
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
import java.util.concurrent.atomic.AtomicInteger;
import java.util.concurrent.atomic.AtomicReference;
import net.minecraft.client.gui.components.ContainerObjectSelectionList;
import net.minecraft.client.resources.language.I18n;
import net.minecraft.client.resources.sounds.SimpleSoundInstance;
import net.minecraft.sounds.SoundEvents;
import org.apache.logging.log4j.util.TriConsumer;

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
        iterateOnRenderedEntries((featureListEntryBase, index, top) -> {
            featureListEntryBase.render(
                    poseStack,
                    index,
                    top + 1,
                    this.getRowLeft(),
                    this.getRowWidth(),
                    featureListEntryBase.getRenderHeight(),
                    mouseX,
                    mouseY,
                    Objects.equals(this.getHovered(), featureListEntryBase),
                    partialTick);
        });
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
        AtomicReference<FeatureEntry> returnValue = new AtomicReference<>(null);

        iterateOnRenderedEntries((entry, index, top) -> {
            if (entry instanceof FeatureEntry featureEntry
                    && top <= mouseY
                    && top + entry.getRenderHeight() >= mouseY) {
                returnValue.set(featureEntry);
            }
        });

        return returnValue.get();
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
            return true;
        }

        // Update hovered
        // (this is usually done in super.render,
        // but we do not call that this case, and this way,
        // we do not calculate it when we do not use it)
        this.hovered = this.isMouseOver(mouseX, mouseY) ? this.getEntryAtPosition(mouseX, mouseY) : null;

        FeatureListEntryBase hovered = this.getHovered();

        if (hovered instanceof FeatureEntry featureEntry) {
            McUtils.soundManager().play(SimpleSoundInstance.forUI(SoundEvents.UI_BUTTON_CLICK, 1.0F));

            float switchRenderX = featureEntry.getEnabledSwitchRenderX() + getRowLeft();
            float switchRenderY = featureEntry.getEnabledSwitchRenderY() + getEntryY(featureEntry);
            float switchSize = featureEntry.getConfigOptionElementSize();

            // Clicked on switch
            if (mouseX >= switchRenderX
                    && mouseX <= switchRenderX + switchSize * 2
                    && mouseY >= switchRenderY
                    && mouseY <= switchRenderY + switchSize) {
                if (!(featureEntry.getFeature() instanceof UserFeature userFeature)) {
                    WynntilsMod.error(featureEntry.getFeature() + " had userEnabled field, but is not a UserFeature.");
                    assert false;
                    return true;
                }

                userFeature.setUserEnabled(!userFeature.isEnabled());
                userFeature.tryUserToggle();

                return true;
            }

            settingsScreen.setSelectedFeature(featureEntry);
            return true;
        }

        return false;
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

    @Override
    public FeatureListEntryBase getSelected() {
        return settingsScreen.getSelectedFeatureEntry();
    }

    @Override
    public void setSelected(FeatureListEntryBase selected) {
        if (selected instanceof FeatureEntry featureEntry) {
            McUtils.soundManager().play(SimpleSoundInstance.forUI(SoundEvents.UI_BUTTON_CLICK, 1.0F));
            settingsScreen.setSelectedFeature(featureEntry);
        }

        super.setSelected(selected);
    }

    @Override
    protected void moveSelection(SelectionDirection ordering) {
        this.moveSelection(ordering, (entry) -> entry instanceof FeatureEntry);
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

    private int getEntryY(FeatureListEntryBase entry) {
        AtomicInteger renderHeight = new AtomicInteger();

        iterateOnRenderedEntries((featureListEntryBase, i, top) -> {
            if (featureListEntryBase == entry) {
                renderHeight.set(top);
            }
        });

        return renderHeight.get();
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
                (float) this.getScrollAmount(), 0, this.getMaxScroll(), settingsScreen.getBarHeight() + 4, height - 2);
    }

    private float getScrollButtonXPos() {
        return settingsScreen.width / 5.515f;
    }

    private void iterateOnRenderedEntries(TriConsumer<FeatureListEntryBase, Integer, Integer> consumer) {
        int itemCount = this.getItemCount();

        int heightOffset = 0;
        int renderedCount = 0;

        for (int i = 0; i < itemCount; i++) {
            FeatureListEntryBase entry = this.getEntry(i);

            int renderHeight = entry.getRenderHeight();

            int top = this.y0 + 1 + heightOffset + (renderedCount * PADDING);
            int bottom = top + renderHeight;

            if (getRowTop(i) < this.y0) continue;
            if (bottom > settingsScreen.height - settingsScreen.getBarHeight() - 10) break;

            consumer.accept(entry, i, top);

            heightOffset += renderHeight;
            renderedCount++;
        }
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
            if (!Objects.equals(lastCategory, feature.getCategory().toString())) {
                lastCategory = feature.getCategory().toString();

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
