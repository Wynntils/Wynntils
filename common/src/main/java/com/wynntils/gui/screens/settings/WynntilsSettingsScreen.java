/*
 * Copyright © Wynntils 2022.
 * This file is released under AGPLv3. See LICENSE for full license details.
 */
package com.wynntils.gui.screens.settings;

import com.mojang.blaze3d.vertex.PoseStack;
import com.wynntils.core.config.ConfigManager;
import com.wynntils.core.features.Feature;
import com.wynntils.gui.render.FontRenderer;
import com.wynntils.gui.render.HorizontalAlignment;
import com.wynntils.gui.render.RenderUtils;
import com.wynntils.gui.render.VerticalAlignment;
import com.wynntils.gui.screens.SearchableScreen;
import com.wynntils.gui.screens.WynntilsMenuScreen;
import com.wynntils.gui.screens.settings.lists.FeatureList;
import com.wynntils.gui.screens.settings.lists.entries.FeatureEntry;
import com.wynntils.gui.screens.settings.widgets.FeatureSettingWidget;
import com.wynntils.gui.widgets.SearchWidget;
import com.wynntils.gui.widgets.TextInputBoxWidget;
import com.wynntils.mc.objects.CommonColors;
import com.wynntils.mc.objects.CustomColor;
import com.wynntils.mc.utils.McUtils;
import net.minecraft.client.Minecraft;
import net.minecraft.client.gui.components.Button;
import net.minecraft.client.gui.screens.Screen;
import net.minecraft.client.resources.language.I18n;
import net.minecraft.network.chat.TranslatableComponent;
import org.lwjgl.glfw.GLFW;

public class WynntilsSettingsScreen extends Screen implements SearchableScreen {
    private static final int BUTTON_WIDTH = 160;
    private static final int BUTTON_HEIGHT = 20;
    private static final int SEARCH_BAR_HEIGHT = 20;
    private static final float BAR_HEIGHT = 30f;

    private static final CustomColor BACKGROUND_COLOR = new CustomColor(56, 42, 27, 255);
    private static final CustomColor FOREGROUND_COLOR = new CustomColor(126, 111, 83, 255);

    private FeatureEntry selectedFeatureEntry;

    private FeatureList featureList;

    private SearchWidget searchWidget;
    private FeatureSettingWidget featureSettingWidget;

    private Button exitButton;
    private Button saveButton;

    private TextInputBoxWidget focusedTextInput = null;

    public WynntilsSettingsScreen() {
        super(new TranslatableComponent("screens.wynntils.settingsScreen.name"));
    }

    @Override
    protected void init() {
        McUtils.mc().keyboardHandler.setSendRepeatsToGui(true);

        featureList = new FeatureList(this);

        this.exitButton = new Button(
                this.width / 2 - BUTTON_WIDTH - 5,
                this.height - BUTTON_HEIGHT - 5,
                BUTTON_WIDTH,
                BUTTON_HEIGHT,
                new TranslatableComponent("screens.wynntils.settingsScreen.close"),
                this::exitWithoutSaving);
        this.addRenderableWidget(this.exitButton);

        this.saveButton = new Button(
                this.width / 2 + 5,
                this.height - BUTTON_HEIGHT - 5,
                BUTTON_WIDTH,
                BUTTON_HEIGHT,
                new TranslatableComponent("screens.wynntils.settingsScreen.apply"),
                this::saveAndExit);
        this.addRenderableWidget(this.saveButton);

        this.searchWidget = new SearchWidget(
                this.width / 90,
                (int) (BAR_HEIGHT + 25),
                (int) (this.width / 6f),
                SEARCH_BAR_HEIGHT,
                newSearchText -> featureList.reAddEntriesWithSearchFilter(newSearchText),
                this);

        this.featureSettingWidget = new FeatureSettingWidget(
                this.width / 5, (int) BAR_HEIGHT, this.width / 5 * 4, (int) (this.height - BAR_HEIGHT * 2), this);

        this.addRenderableWidget(this.searchWidget);
        this.addRenderableWidget(this.featureSettingWidget);

        this.setFocused(searchWidget);
    }

    @Override
    public void onClose() {
        McUtils.mc().keyboardHandler.setSendRepeatsToGui(false);
        super.onClose();
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
                        HorizontalAlignment.Center,
                        VerticalAlignment.Top,
                        FontRenderer.TextShadow.OUTLINE);

        featureList.render(poseStack, mouseX, mouseY, partialTick);

        // Re-render the bottom bar again, so overlaying buttons are not displayed
        RenderUtils.drawRect(poseStack, FOREGROUND_COLOR, 0, this.height - BAR_HEIGHT, 10, this.width, BAR_HEIGHT);

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
                        HorizontalAlignment.Center,
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
        if (featureList.isMouseOver(mouseX, mouseY)) {
            return featureList.mouseScrolled(mouseX, mouseY, delta);
        } else if (featureSettingWidget.isMouseOver(mouseX, mouseY)) {
            featureSettingWidget.mouseScrolled(mouseX, mouseY, delta);
        }

        return false;
    }

    @Override
    public boolean mouseDragged(double mouseX, double mouseY, int button, double dragX, double dragY) {
        return featureList.mouseDragged(mouseX, mouseY, button, dragX, dragY);
    }

    @Override
    public boolean mouseClicked(double mouseX, double mouseY, int button) {
        return featureList.mouseClicked(mouseX, mouseY, button)
                || searchWidget.mouseClicked(mouseX, mouseY, button)
                || super.mouseClicked(mouseX, mouseY, button);
    }

    @Override
    public boolean mouseReleased(double mouseX, double mouseY, int button) {
        return featureList.mouseReleased(mouseX, mouseY, button);
    }

    @Override
    public boolean keyPressed(int keyCode, int scanCode, int modifiers) {
        if (focusedTextInput != null) {
            return focusedTextInput.keyPressed(keyCode, scanCode, modifiers);
        }

        if (keyCode == GLFW.GLFW_KEY_ESCAPE && this.shouldCloseOnEsc()) {
            exitWithoutSaving(this.exitButton);
            return true;
        }

        return super.keyPressed(keyCode, scanCode, modifiers) || featureList.keyPressed(keyCode, scanCode, modifiers);
    }

    @Override
    public boolean charTyped(char codePoint, int modifiers) {
        if (focusedTextInput != null) {
            return focusedTextInput.charTyped(codePoint, modifiers);
        }

        return super.charTyped(codePoint, modifiers);
    }

    private void saveAndExit(Button button) {
        ConfigManager.saveConfig();

        McUtils.mc().setScreen(new WynntilsMenuScreen());
    }

    private void exitWithoutSaving(Button button) {
        ConfigManager.loadConfigFile();
        ConfigManager.loadAllConfigOptions(true);

        McUtils.mc().setScreen(new WynntilsMenuScreen());
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

    @Override
    public TextInputBoxWidget getFocusedTextInput() {
        return focusedTextInput;
    }

    @Override
    public void setFocusedTextInput(TextInputBoxWidget focusedTextInput) {
        this.focusedTextInput = focusedTextInput;
    }

    public SearchWidget getSearchWidget() {
        return searchWidget;
    }
}
