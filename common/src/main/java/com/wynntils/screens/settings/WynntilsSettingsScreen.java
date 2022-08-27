/*
 * Copyright © Wynntils 2022.
 * This file is released under AGPLv3. See LICENSE for full license details.
 */
package com.wynntils.screens.settings;

import com.mojang.blaze3d.vertex.PoseStack;
import com.wynntils.core.config.ConfigManager;
import com.wynntils.core.features.Feature;
import com.wynntils.mc.objects.CommonColors;
import com.wynntils.mc.objects.CustomColor;
import com.wynntils.mc.render.FontRenderer;
import com.wynntils.mc.render.RenderUtils;
import com.wynntils.mc.utils.McUtils;
import com.wynntils.screens.settings.entries.FeatureSettingWidget;
import com.wynntils.screens.settings.lists.FeatureList;
import com.wynntils.screens.settings.lists.entries.FeatureEntry;
import com.wynntils.screens.widgets.SearchWidget;
import net.minecraft.client.Minecraft;
import net.minecraft.client.gui.components.Button;
import net.minecraft.client.gui.screens.Screen;
import net.minecraft.client.resources.language.I18n;
import net.minecraft.network.chat.TranslatableComponent;
import org.lwjgl.glfw.GLFW;

public class WynntilsSettingsScreen extends Screen {
    private static final int BUTTON_WIDTH = 160;
    private static final int BUTTON_HEIGHT = 20;
    private static final int SEARCH_BAR_HEIGHT = 20;
    private static final float BAR_HEIGHT = 30f;

    private static final CustomColor BACKGROUND_COLOR = new CustomColor(56, 42, 27, 255);
    private static final CustomColor FOREGROUND_COLOR = new CustomColor(126, 111, 83, 255);

    private final Screen lastScreen;

    private FeatureEntry selectedFeatureEntry;

    private FeatureList featureList;

    private SearchWidget searchWidget;
    private FeatureSettingWidget featureSettingWidget;

    public WynntilsSettingsScreen() {
        super(new TranslatableComponent("screens.wynntils.settingsScreen.name"));
        lastScreen = McUtils.mc().screen;
    }

    @Override
    protected void init() {
        featureList = new FeatureList(this);

        this.addRenderableWidget(new Button(
                this.width / 2 - BUTTON_WIDTH - 5,
                this.height - BUTTON_HEIGHT - 5,
                BUTTON_WIDTH,
                BUTTON_HEIGHT,
                new TranslatableComponent("screens.wynntils.settingsScreen.close"),
                this::exitWithoutSaving));

        this.addRenderableWidget(new Button(
                this.width / 2 + 5,
                this.height - BUTTON_HEIGHT - 5,
                BUTTON_WIDTH,
                BUTTON_HEIGHT,
                new TranslatableComponent("screens.wynntils.settingsScreen.apply"),
                this::saveAndExit));

        this.searchWidget = new SearchWidget(
                this.width / 90,
                (int) (BAR_HEIGHT + 25),
                (int) (this.width / 6f),
                SEARCH_BAR_HEIGHT,
                newSearchText -> featureList.reAddEntriesWithSearchFilter(newSearchText));

        this.featureSettingWidget = new FeatureSettingWidget(
                this.width / 5,
                (int) this.BAR_HEIGHT,
                this.width / 5 * 4,
                (int) (this.height - this.BAR_HEIGHT * 2),
                this);

        this.addRenderableWidget(this.searchWidget);
        this.addRenderableWidget(this.featureSettingWidget);

        this.setFocused(searchWidget);
    }

    @Override
    public void render(PoseStack poseStack, int mouseX, int mouseY, float partialTick) {
        this.renderBackground(poseStack);

        FontRenderer.getInstance()
                .renderText(
                        poseStack,
                        I18n.get("screens.wynntils.settingsScreen.title"),
                        this.width / 2f,
                        10,
                        CommonColors.WHITE,
                        FontRenderer.TextAlignment.CENTER_ALIGNED,
                        FontRenderer.TextShadow.OUTLINE);

        featureList.render(poseStack, mouseX, mouseY, partialTick);

        // Re-render the bottom bar again, so overlaying buttons are not displayed
        RenderUtils.drawRect(poseStack, FOREGROUND_COLOR, 0, this.height - BAR_HEIGHT, 0, this.width, BAR_HEIGHT);

        poseStack.pushPose();

        poseStack.translate(this.width / 90f, 0, 0);

        FontRenderer.getInstance()
                .renderAlignedTextInBox(
                        poseStack,
                        I18n.get("screens.wynntils.settingsScreen.featureList"),
                        0,
                        this.width / 6f,
                        BAR_HEIGHT + 7.5f,
                        0,
                        CommonColors.WHITE,
                        FontRenderer.TextAlignment.CENTER_ALIGNED,
                        FontRenderer.TextShadow.OUTLINE);

        poseStack.popPose();

        super.render(poseStack, mouseX, mouseY, partialTick);
    }

    @Override
    public void renderBackground(PoseStack poseStack) {
        RenderUtils.drawRect(poseStack, BACKGROUND_COLOR, 0, 0, 0, this.width, this.height);

        RenderUtils.drawRect(poseStack, FOREGROUND_COLOR, 0, 0, 0, this.width, BAR_HEIGHT);
        RenderUtils.drawRect(poseStack, FOREGROUND_COLOR, 0, this.height - BAR_HEIGHT, 0, this.width, BAR_HEIGHT);
    }

    @Override
    public void resize(Minecraft minecraft, int width, int height) {
        this.init();
        super.resize(minecraft, width, height);
    }

    @Override
    public boolean mouseScrolled(double mouseX, double mouseY, double delta) {
        return featureList.mouseScrolled(mouseX, mouseY, delta);
    }

    @Override
    public boolean mouseDragged(double mouseX, double mouseY, int button, double dragX, double dragY) {
        return featureList.mouseDragged(mouseX, mouseY, button, dragX, dragY);
    }

    @Override
    public boolean mouseClicked(double mouseX, double mouseY, int button) {
        super.mouseClicked(mouseX, mouseY, button);
        return featureList.mouseClicked(mouseX, mouseY, button);
    }

    @Override
    public boolean mouseReleased(double mouseX, double mouseY, int button) {
        return featureList.mouseReleased(mouseX, mouseY, button);
    }

    @Override
    public boolean keyPressed(int keyCode, int scanCode, int modifiers) {
        if (keyCode == GLFW.GLFW_KEY_ESCAPE && this.shouldCloseOnEsc()) {
            this.onClose();
            return true;
        }

        super.keyPressed(keyCode, scanCode, modifiers);

        return featureList.keyPressed(keyCode, scanCode, modifiers);
    }

    private void saveAndExit(Button button) {
        ConfigManager.saveConfig();

        McUtils.mc().setScreen(this.lastScreen);
    }

    private void exitWithoutSaving(Button button) {
        ConfigManager.loadConfigFile();
        ConfigManager.loadConfigOptions(ConfigManager.getConfigHolders(), true);

        McUtils.mc().setScreen(this.lastScreen);
    }

    public float getBarHeight() {
        return BAR_HEIGHT;
    }

    public FeatureEntry getSelectedFeatureEntry() {
        return selectedFeatureEntry;
    }

    public Feature getSelectedFeature() {
        return selectedFeatureEntry == null ? null : selectedFeatureEntry.getFeature();
    }

    public void setSelectedFeature(FeatureEntry selectedFeatureEntry) {
        this.selectedFeatureEntry = selectedFeatureEntry;
    }
}
