/*
 * Copyright © Wynntils 2022.
 * This file is released under AGPLv3. See LICENSE for full license details.
 */
package com.wynntils.gui.screens.settings;

import com.mojang.blaze3d.systems.RenderSystem;
import com.mojang.blaze3d.vertex.PoseStack;
import com.wynntils.core.config.ConfigManager;
import com.wynntils.core.features.Feature;
import com.wynntils.core.features.FeatureRegistry;
import com.wynntils.core.features.properties.FeatureCategory;
import com.wynntils.gui.render.RenderUtils;
import com.wynntils.gui.render.Texture;
import com.wynntils.gui.screens.settings.widgets.CategoryButton;
import com.wynntils.gui.screens.settings.widgets.FeatureButton;
import com.wynntils.gui.screens.settings.widgets.GeneralSettingsButton;
import com.wynntils.gui.screens.settings.widgets.ScrollButton;
import com.wynntils.utils.MathUtils;
import java.util.ArrayList;
import java.util.List;
import net.minecraft.ChatFormatting;
import net.minecraft.client.gui.components.AbstractButton;
import net.minecraft.client.gui.components.Widget;
import net.minecraft.client.gui.components.events.GuiEventListener;
import net.minecraft.client.gui.screens.Screen;
import net.minecraft.network.chat.TranslatableComponent;

public class WynntilsBookSettingsScreen extends Screen {
    private final int FEATURES_PER_PAGE = 13;
    private final List<AbstractButton> features = new ArrayList<>();

    private ScrollButton featureListScrollButton;
    private Feature selected = null;
    private int scrollOffset = 0;

    public WynntilsBookSettingsScreen() {
        super(new TranslatableComponent("screens.wynntils.settingsScreen.name"));

        reloadFeatureButtons();
    }

    @Override
    protected void init() {
        this.addRenderableWidget(new GeneralSettingsButton(
                55,
                Texture.SETTING_BACKGROUND.height() - 30,
                35,
                14,
                new TranslatableComponent("screens.wynntils.settingsScreen.apply"),
                () -> {
                    ConfigManager.saveConfig();
                    this.onClose();
                },
                List.of(new TranslatableComponent("screens.wynntils.settingsScreen.apply.description")
                        .withStyle(ChatFormatting.GREEN))));

        this.addRenderableWidget(new GeneralSettingsButton(
                15,
                Texture.SETTING_BACKGROUND.height() - 30,
                35,
                14,
                new TranslatableComponent("screens.wynntils.settingsScreen.close"),
                this::onClose,
                List.of(new TranslatableComponent("screens.wynntils.settingsScreen.close.description")
                        .withStyle(ChatFormatting.DARK_RED))));
    }

    // region render

    @Override
    public void render(PoseStack poseStack, int mouseX, int mouseY, float partialTick) {
        float backgroundRenderX = getTranslationX();
        float backgroundRenderY = getTranslationY();

        poseStack.pushPose();
        poseStack.translate(backgroundRenderX, backgroundRenderY, 0);

        renderBg(poseStack);

        renderScrollArea(poseStack);

        renderButtons(poseStack, mouseX, mouseY, partialTick);

        poseStack.popPose();
    }

    private void renderButtons(PoseStack poseStack, int mouseX, int mouseY, float partialTick) {
        mouseX -= getTranslationX();
        mouseY -= getTranslationY();

        for (Widget renderable : renderables) {
            renderable.render(poseStack, mouseX, mouseY, partialTick);
        }

        for (int i = scrollOffset; i < Math.min(features.size(), scrollOffset + FEATURES_PER_PAGE); i++) {
            AbstractButton featureButton = features.get(i);
            featureButton.render(poseStack, mouseX, mouseY, partialTick);
        }

        featureListScrollButton.renderButton(poseStack, mouseX, mouseY, partialTick);
    }

    private static void renderScrollArea(PoseStack poseStack) {
        RenderSystem.enableBlend();
        RenderUtils.drawTexturedRect(
                poseStack,
                Texture.SETTING_SCROLL_AREA,
                (Texture.SETTING_BACKGROUND.width() / 2f - Texture.SETTING_SCROLL_AREA.width()) / 2f,
                10);
        RenderSystem.disableBlend();
    }

    private static void renderBg(PoseStack poseStack) {
        RenderUtils.drawTexturedRect(poseStack, Texture.SETTING_BACKGROUND, 0, 0);
    }

    // endregion

    // region mouse events

    @Override
    public boolean mouseClicked(double mouseX, double mouseY, int button) {
        mouseX -= getTranslationX();
        mouseY -= getTranslationY();

        for (GuiEventListener child : children()) {
            if (child.isMouseOver(mouseX, mouseY)) {
                child.mouseClicked(mouseX, mouseY, button);
            }
        }

        for (int i = scrollOffset; i < Math.min(features.size(), scrollOffset + FEATURES_PER_PAGE); i++) {
            AbstractButton featureButton = features.get(i);
            if (featureButton.isMouseOver(mouseX, mouseY)) {
                featureButton.mouseClicked(mouseX, mouseY, button);
            }
        }

        if (featureListScrollButton.isMouseOver(mouseX, mouseY)) {
            featureListScrollButton.mouseClicked(mouseX, mouseY, button);
        }

        return true;
    }

    @Override
    public boolean mouseScrolled(double mouseX, double mouseY, double delta) {
        mouseX -= getTranslationX();
        mouseY -= getTranslationY();

        if (mouseX <= Texture.SETTING_BACKGROUND.width() / 2f) {
            featureListScrollButton.mouseScrolled(mouseX, mouseY, delta);
        }

        return true;
    }

    @Override
    public boolean mouseReleased(double mouseX, double mouseY, int button) {
        if (featureListScrollButton.isMouseOver(mouseX, mouseY)) {
            featureListScrollButton.mouseReleased(mouseX, mouseY, button);
        }
        return true;
    }

    @Override
    public boolean mouseDragged(double mouseX, double mouseY, int button, double dragX, double dragY) {
        featureListScrollButton.mouseDragged(mouseX, mouseY, button, dragX, dragY);

        return true;
    }

    private void scroll(double delta) {
        scrollOffset =
                MathUtils.clamp((int) (scrollOffset - delta), 0, Math.max(0, features.size() - FEATURES_PER_PAGE));
    }

    // endregion

    @Override
    public void onClose() {
        ConfigManager.loadConfigFile();
        ConfigManager.loadConfigOptions(ConfigManager.getConfigHolders(), true);
        super.onClose();
    }

    private void reloadFeatureButtons() {
        features.clear();
        scrollOffset = 0;

        FeatureCategory oldCategory = null;

        List<Feature> featureList =
                FeatureRegistry.getFeatures().stream().sorted().toList();
        int offset = 0;
        for (int i = 0; i < featureList.size(); i++) {
            Feature feature = featureList.get(i);

            int renderIndex = (i + offset) % FEATURES_PER_PAGE;

            if (feature.getCategory() != oldCategory) {
                features.add(new CategoryButton(37, 21 + renderIndex * 12, 140, 10, feature.getCategory()));
                oldCategory = feature.getCategory();
                offset++;
                renderIndex = (i + offset) % FEATURES_PER_PAGE;
            }

            features.add(new FeatureButton(37, 21 + renderIndex * 12, 140, 10, feature));
        }

        featureListScrollButton = new ScrollButton(
                23,
                17,
                Texture.SETTING_BACKGROUND.height() - 50,
                Texture.SETTING_SCROLL_BUTTON.width(),
                Texture.SETTING_SCROLL_BUTTON.height() / 2,
                features.size() - FEATURES_PER_PAGE,
                this::scroll);
    }

    private float getTranslationY() {
        return (this.height - Texture.SETTING_BACKGROUND.height()) / 2f;
    }

    private float getTranslationX() {
        return (this.width - Texture.SETTING_BACKGROUND.width()) / 2f;
    }

    public Feature getSelected() {
        return selected;
    }

    public void setSelected(Feature selected) {
        this.selected = selected;
    }
}
