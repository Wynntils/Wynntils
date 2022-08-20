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
import com.wynntils.screens.settings.lists.entries.Entry;
import com.wynntils.screens.settings.lists.entries.FeatureCategoryEntry;
import com.wynntils.screens.settings.lists.entries.FeatureEntry;
import java.util.Objects;
import net.minecraft.client.gui.components.ContainerObjectSelectionList;
import net.minecraft.client.resources.language.I18n;

public class FeatureList extends ContainerObjectSelectionList<Entry> {
    private final WynntilsSettingsScreen settingsScreen;
    private static final int ITEM_HEIGHT = 25;

    public FeatureList(WynntilsSettingsScreen screen) {
        super(
                McUtils.mc(),
                screen.width,
                screen.height,
                screen.height / 10 + 15,
                screen.height / 10 + Texture.OVERLAY_SELECTION_GUI.height() - 15,
                ITEM_HEIGHT);

        this.settingsScreen = screen;

        String lastCategory = "";

        for (Feature feature : FeatureRegistry.getFeatures().stream()
                .sorted(Feature::compareTo)
                .toList()) {
            if (!Objects.equals(lastCategory, feature.getCategory())) {
                lastCategory = feature.getCategory();

                if (lastCategory == null) {
                    this.addEntry(new FeatureCategoryEntry(I18n.get("screens.wynntils.settingsScreen.uncategorized")));
                } else {
                    this.addEntry(new FeatureCategoryEntry(lastCategory));
                }
            }

            this.addEntry(new FeatureEntry(feature));
        }

        this.setRenderBackground(false);
        this.setRenderTopAndBottom(false);
    }

    @Override
    public void render(PoseStack poseStack, int mouseX, int mouseY, float partialTick) {
        this.renderBackground(poseStack);

        super.render(poseStack, mouseX, mouseY, partialTick);
    }

    @Override
    protected void renderBackground(PoseStack poseStack) {
        float width = settingsScreen.width / 6.5f;
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
    protected int getScrollbarPosition() {
        return (int) (settingsScreen.width / 6.5f) - 15;
    }
}
