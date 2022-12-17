/*
 * Copyright © Wynntils 2022.
 * This file is released under AGPLv3. See LICENSE for full license details.
 */
package com.wynntils.gui.screens.settings;

import com.mojang.blaze3d.systems.RenderSystem;
import com.mojang.blaze3d.vertex.PoseStack;
import com.wynntils.core.config.ConfigHolder;
import com.wynntils.core.features.Feature;
import com.wynntils.core.features.FeatureRegistry;
import com.wynntils.core.features.Translatable;
import com.wynntils.core.features.overlays.Overlay;
import com.wynntils.core.features.properties.FeatureCategory;
import com.wynntils.core.managers.Managers;
import com.wynntils.gui.render.FontRenderer;
import com.wynntils.gui.render.HorizontalAlignment;
import com.wynntils.gui.render.RenderUtils;
import com.wynntils.gui.render.Texture;
import com.wynntils.gui.render.VerticalAlignment;
import com.wynntils.gui.screens.TextboxScreen;
import com.wynntils.gui.screens.WynntilsScreenWrapper;
import com.wynntils.gui.screens.settings.widgets.CategoryButton;
import com.wynntils.gui.screens.settings.widgets.ConfigButton;
import com.wynntils.gui.screens.settings.widgets.ConfigurableButton;
import com.wynntils.gui.screens.settings.widgets.GeneralSettingsButton;
import com.wynntils.gui.screens.settings.widgets.ScrollButton;
import com.wynntils.gui.widgets.SearchWidget;
import com.wynntils.gui.widgets.TextInputBoxWidget;
import com.wynntils.mc.objects.CommonColors;
import com.wynntils.mc.objects.CustomColor;
import com.wynntils.mc.utils.McUtils;
import com.wynntils.utils.MathUtils;
import com.wynntils.utils.StringUtils;
import java.util.ArrayList;
import java.util.Comparator;
import java.util.List;
import java.util.Objects;
import net.minecraft.ChatFormatting;
import net.minecraft.client.gui.components.AbstractButton;
import net.minecraft.client.gui.components.Widget;
import net.minecraft.client.gui.components.events.GuiEventListener;
import net.minecraft.client.gui.screens.Screen;
import net.minecraft.network.chat.TranslatableComponent;
import org.lwjgl.glfw.GLFW;

public class WynntilsBookSettingsScreen extends Screen implements TextboxScreen {
    private final int CONFIGURABLES_PER_PAGE = 13;
    private final int CONFIGS_PER_PAGE = 4;
    private final List<AbstractButton> configurables = new ArrayList<>();
    private final List<AbstractButton> configs = new ArrayList<>();

    private TextInputBoxWidget focusedTextInput;
    private final SearchWidget searchWidget;
    private ScrollButton configurableListScrollButton;
    private ScrollButton configListScrollButton;

    // FIXME: Suboptimal to have both selected variables, but we need big changes to fix that
    private Feature selectedFeature = null;
    private Overlay selectedOverlay = null;

    private int configurableScrollOffset = 0;
    private int configScrollOffset = 0;

    private WynntilsBookSettingsScreen() {
        super(new TranslatableComponent("screens.wynntils.settingsScreen.name"));

        McUtils.mc().keyboardHandler.setSendRepeatsToGui(true);

        searchWidget = new SearchWidget(
                95,
                Texture.SETTING_BACKGROUND.height() - 32,
                100,
                20,
                s -> {
                    reloadConfigurableButtons();
                },
                this);
        reloadConfigurableButtons();
    }

    public static Screen create() {
        return WynntilsScreenWrapper.create(new WynntilsBookSettingsScreen());
    }

    @Override
    protected void init() {
        this.addRenderableWidget(searchWidget);

        this.addRenderableWidget(new GeneralSettingsButton(
                55,
                Texture.SETTING_BACKGROUND.height() - 30,
                35,
                14,
                new TranslatableComponent("screens.wynntils.settingsScreen.apply"),
                () -> {
                    Managers.Config.saveConfig();
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

    // region Render

    @Override
    public void render(PoseStack poseStack, int mouseX, int mouseY, float partialTick) {
        float backgroundRenderX = getTranslationX();
        float backgroundRenderY = getTranslationY();

        poseStack.pushPose();
        poseStack.translate(backgroundRenderX, backgroundRenderY, 0);

        renderBg(poseStack);

        renderScrollArea(poseStack);

        renderButtons(poseStack, mouseX, mouseY, partialTick);

        if (selectedFeature != null || selectedOverlay != null) {
            renderConfigTitle(poseStack);
        }

        poseStack.popPose();
    }

    private void renderConfigTitle(PoseStack poseStack) {
        String name = "";
        boolean enabled = false;
        if (selectedOverlay != null) {
            enabled = selectedOverlay.isEnabled();
            name = selectedOverlay.getTranslatedName();
        } else if (selectedFeature != null) {
            enabled = selectedFeature.isEnabled();
            name = selectedFeature.getTranslatedName();
        }

        poseStack.pushPose();
        poseStack.scale(0.8f, 0.8f, 0);
        FontRenderer.getInstance()
                .renderText(
                        poseStack,
                        name + ": "
                                + (enabled
                                        ? ChatFormatting.DARK_GREEN + "Enabled"
                                        : ChatFormatting.DARK_RED + "Disabled"),
                        Texture.SETTING_BACKGROUND.width() / 2f / 0.8f + 10,
                        12,
                        CommonColors.BLACK,
                        HorizontalAlignment.Left,
                        VerticalAlignment.Top,
                        FontRenderer.TextShadow.NONE);
        poseStack.popPose();
    }

    private void renderButtons(PoseStack poseStack, int mouseX, int mouseY, float partialTick) {
        mouseX -= getTranslationX();
        mouseY -= getTranslationY();

        for (Widget renderable : renderables) {
            renderable.render(poseStack, mouseX, mouseY, partialTick);
        }

        configurableListScrollButton.renderButton(poseStack, mouseX, mouseY, partialTick);

        for (int i = configurableScrollOffset * CONFIGURABLES_PER_PAGE;
                i < Math.min(configurables.size(), (configurableScrollOffset + 1) * CONFIGURABLES_PER_PAGE);
                i++) {
            AbstractButton featureButton = configurables.get(i);
            featureButton.render(poseStack, mouseX, mouseY, partialTick);
        }

        if (configListScrollButton != null) {
            configListScrollButton.renderButton(poseStack, mouseX, mouseY, partialTick);
        }

        // Reverse iteration for so tooltip Z levels are correct when rendering
        for (int i = Math.min(configs.size(), configScrollOffset + CONFIGS_PER_PAGE) - 1;
                i >= configScrollOffset;
                i--) {
            AbstractButton configButton = configs.get(i);
            configButton.render(poseStack, mouseX, mouseY, partialTick);
        }
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

    // region Mouse events

    @Override
    public boolean mouseClicked(double mouseX, double mouseY, int button) {
        mouseX -= getTranslationX();
        mouseY -= getTranslationY();

        for (GuiEventListener child : children()) {
            if (child.isMouseOver(mouseX, mouseY)) {
                child.mouseClicked(mouseX, mouseY, button);
            }
        }

        for (int i = configurableScrollOffset * CONFIGURABLES_PER_PAGE;
                i < Math.min(configurables.size(), (configurableScrollOffset + 1) * CONFIGURABLES_PER_PAGE);
                i++) {
            AbstractButton featureButton = configurables.get(i);
            if (featureButton.isMouseOver(mouseX, mouseY)) {
                featureButton.mouseClicked(mouseX, mouseY, button);
            }
        }

        for (int i = configScrollOffset; i < Math.min(configs.size(), configScrollOffset + CONFIGS_PER_PAGE); i++) {
            AbstractButton configButton = configs.get(i);
            if (configButton.isMouseOver(mouseX, mouseY)) {
                configButton.mouseClicked(mouseX, mouseY, button);
            }
        }

        if (configurableListScrollButton.isMouseOver(mouseX, mouseY)) {
            configurableListScrollButton.mouseClicked(mouseX, mouseY, button);
        }

        if (configListScrollButton != null && configListScrollButton.isMouseOver(mouseX, mouseY)) {
            configListScrollButton.mouseClicked(mouseX, mouseY, button);
        }

        return true;
    }

    @Override
    public boolean mouseScrolled(double mouseX, double mouseY, double delta) {
        mouseX -= getTranslationX();
        mouseY -= getTranslationY();

        if (mouseX <= Texture.SETTING_BACKGROUND.width() / 2f) {
            configurableListScrollButton.mouseScrolled(mouseX, mouseY, delta);
        } else if (configListScrollButton != null) {
            configListScrollButton.mouseScrolled(mouseX, mouseY, delta);
        }

        return true;
    }

    @Override
    public boolean mouseReleased(double mouseX, double mouseY, int button) {
        mouseX -= getTranslationX();
        mouseY -= getTranslationY();

        configurableListScrollButton.mouseReleased(mouseX, mouseY, button);

        if (configListScrollButton != null) {
            configListScrollButton.mouseReleased(mouseX, mouseY, button);
        }

        return true;
    }

    @Override
    public boolean mouseDragged(double mouseX, double mouseY, int button, double dragX, double dragY) {
        mouseX -= getTranslationX();
        mouseY -= getTranslationY();

        configurableListScrollButton.mouseDragged(mouseX, mouseY, button, dragX, dragY);
        if (configListScrollButton != null) {
            configListScrollButton.mouseDragged(mouseX, mouseY, button, dragX, dragY);
        }

        return true;
    }

    private void scrollConfigurableList(double delta) {
        int roundedUpPageNeed = Math.max(
                0,
                configurables.size() / CONFIGURABLES_PER_PAGE
                        + (configurables.size() > CONFIGURABLES_PER_PAGE
                                        && configurables.size() % CONFIGURABLES_PER_PAGE != 0
                                ? 1
                                : 0)
                        - 1);

        configurableScrollOffset = MathUtils.clamp((int) (configurableScrollOffset - delta), 0, roundedUpPageNeed);
    }

    private void scrollConfigList(double delta) {
        int roundedUpPageNeed = configs.size() / CONFIGS_PER_PAGE + (configs.size() % CONFIGS_PER_PAGE == 0 ? 0 : 1);
        configScrollOffset = MathUtils.clamp(
                (int) (configScrollOffset - delta),
                0,
                configs.size() <= CONFIGS_PER_PAGE ? 0 : (roundedUpPageNeed - 1) * CONFIGS_PER_PAGE);
    }

    // endregion

    // region Keyboard events

    @Override
    public boolean charTyped(char codePoint, int modifiers) {
        return focusedTextInput != null && focusedTextInput.charTyped(codePoint, modifiers);
    }

    @Override
    public boolean keyPressed(int keyCode, int scanCode, int modifiers) {
        if (keyCode == GLFW.GLFW_KEY_ESCAPE) {
            this.onClose();
            return true;
        }

        return focusedTextInput != null && focusedTextInput.keyPressed(keyCode, scanCode, modifiers);
    }

    // endregion

    @Override
    public void onClose() {
        McUtils.mc().keyboardHandler.setSendRepeatsToGui(false);
        Managers.Config.loadConfigFile();
        Managers.Config.loadConfigOptions(Managers.Config.getConfigHolders(), true);
        super.onClose();
    }

    private void reloadConfigurableButtons() {
        configurables.clear();
        configurableScrollOffset = 0;

        FeatureCategory oldCategory = null;

        List<Feature> featureList = FeatureRegistry.getFeatures().stream()
                .filter(feature -> searchMatches(feature)
                        || feature.getVisibleConfigOptions().stream().anyMatch(this::configOptionContains)
                        || feature.getOverlays().stream()
                                .anyMatch(overlay -> searchMatches(feature)
                                        || overlay.getVisibleConfigOptions().stream()
                                                .anyMatch(this::configOptionContains)))
                .sorted()
                .toList();

        int offset = 0;
        for (int i = 0; i < featureList.size(); i++) {
            Feature feature = featureList.get(i);

            int renderIndex = (i + offset) % CONFIGURABLES_PER_PAGE;

            if (feature.getCategory() != oldCategory) {
                configurables.add(new CategoryButton(37, 21 + renderIndex * 12, 140, 10, feature.getCategory()));
                oldCategory = feature.getCategory();
                offset++;
                renderIndex = (i + offset) % CONFIGURABLES_PER_PAGE;
            }

            configurables.add(new ConfigurableButton(37, 21 + renderIndex * 12, 140, 10, feature));

            for (Overlay overlay : feature.getOverlays()) {
                offset++;
                renderIndex = (i + offset) % CONFIGURABLES_PER_PAGE;
                configurables.add(new ConfigurableButton(37, 21 + renderIndex * 12, 140, 10, overlay));
            }
        }

        int roundedUpPageNeed = Math.max(
                0,
                configurables.size() / CONFIGURABLES_PER_PAGE
                        + (configurables.size() > CONFIGURABLES_PER_PAGE
                                        && configurables.size() % CONFIGURABLES_PER_PAGE != 0
                                ? 1
                                : 0)
                        - 1);
        configurableListScrollButton = new ScrollButton(
                23,
                17,
                Texture.SETTING_BACKGROUND.height() - 50,
                Texture.SETTING_SCROLL_BUTTON.width(),
                Texture.SETTING_SCROLL_BUTTON.height() / 2,
                roundedUpPageNeed,
                1,
                this::scrollConfigurableList,
                CustomColor.NONE);
    }

    public boolean configOptionContains(ConfigHolder configHolder) {
        return !searchWidget.getTextBoxInput().isEmpty()
                && StringUtils.containsIgnoreCase(configHolder.getDisplayName(), searchWidget.getTextBoxInput());
    }

    private boolean searchMatches(Translatable translatable) {
        return StringUtils.partialMatch(translatable.getTranslatedName(), searchWidget.getTextBoxInput());
    }

    private void reloadConfigButtons() {
        configs.clear();
        configScrollOffset = 0;

        if (selectedFeature == null && selectedOverlay == null) {
            configListScrollButton = null;
            return;
        }

        List<ConfigHolder> configsOptions;

        if (selectedFeature != null) {
            configsOptions = selectedFeature.getVisibleConfigOptions().stream()
                    .sorted(Comparator.comparing(
                            configHolder -> !Objects.equals(configHolder.getFieldName(), "userEnabled")))
                    .toList();
        } else {
            configsOptions = selectedOverlay.getVisibleConfigOptions().stream()
                    .sorted(Comparator.comparing(
                            configHolder -> !Objects.equals(configHolder.getFieldName(), "userEnabled")))
                    .toList();
        }

        for (int i = 0; i < configsOptions.size(); i++) {
            ConfigHolder config = configsOptions.get(i);

            int renderIndex = i % CONFIGS_PER_PAGE;

            configs.add(new ConfigButton(
                    Texture.SETTING_BACKGROUND.width() / 2 + 10, 21 + renderIndex * 46, 160, 45, this, config));
        }

        int roundedUpPageNeed = configs.size() / CONFIGS_PER_PAGE + (configs.size() % CONFIGS_PER_PAGE == 0 ? 0 : 1);
        configListScrollButton = new ScrollButton(
                Texture.SETTING_BACKGROUND.width() - 23,
                17,
                Texture.SETTING_BACKGROUND.height() - 25,
                Texture.SETTING_SCROLL_BUTTON.width(),
                Texture.SETTING_SCROLL_BUTTON.height() / 2,
                configs.size() <= CONFIGS_PER_PAGE ? 0 : (roundedUpPageNeed - 1) * CONFIGS_PER_PAGE,
                CONFIGS_PER_PAGE,
                this::scrollConfigList,
                CommonColors.GRAY);
    }

    private float getTranslationY() {
        return (this.height - Texture.SETTING_BACKGROUND.height()) / 2f;
    }

    private float getTranslationX() {
        return (this.width - Texture.SETTING_BACKGROUND.width()) / 2f;
    }

    public Feature getSelectedFeature() {
        return selectedFeature;
    }

    public void setSelectedFeature(Feature selectedFeature) {
        this.selectedFeature = selectedFeature;
        this.selectedOverlay = null;
        reloadConfigButtons();
    }

    public void setSelectedOverlay(Overlay selectedOverlay) {
        this.selectedOverlay = selectedOverlay;
        this.selectedFeature = null;
        reloadConfigButtons();
    }

    public Overlay getSelectedOverlay() {
        return selectedOverlay;
    }

    @Override
    public TextInputBoxWidget getFocusedTextInput() {
        return focusedTextInput;
    }

    @Override
    public void setFocusedTextInput(TextInputBoxWidget focusedTextInput) {
        this.focusedTextInput = focusedTextInput;
    }
}
