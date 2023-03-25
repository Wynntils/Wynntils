/*
 * Copyright © Wynntils 2022.
 * This file is released under AGPLv3. See LICENSE for full license details.
 */
package com.wynntils.screens.settings;

import com.mojang.blaze3d.systems.RenderSystem;
import com.mojang.blaze3d.vertex.PoseStack;
import com.wynntils.core.components.Managers;
import com.wynntils.core.config.Category;
import com.wynntils.core.config.ConfigHolder;
import com.wynntils.core.features.Configurable;
import com.wynntils.core.features.Feature;
import com.wynntils.core.features.Translatable;
import com.wynntils.core.features.overlays.Overlay;
import com.wynntils.screens.base.TextboxScreen;
import com.wynntils.screens.base.WynntilsScreen;
import com.wynntils.screens.base.widgets.SearchWidget;
import com.wynntils.screens.base.widgets.TextInputBoxWidget;
import com.wynntils.screens.base.widgets.WynntilsButton;
import com.wynntils.screens.settings.widgets.CategoryButton;
import com.wynntils.screens.settings.widgets.ConfigButton;
import com.wynntils.screens.settings.widgets.ConfigurableButton;
import com.wynntils.screens.settings.widgets.GeneralSettingsButton;
import com.wynntils.screens.settings.widgets.ScrollButton;
import com.wynntils.utils.MathUtils;
import com.wynntils.utils.StringUtils;
import com.wynntils.utils.colors.CommonColors;
import com.wynntils.utils.colors.CustomColor;
import com.wynntils.utils.render.FontRenderer;
import com.wynntils.utils.render.RenderUtils;
import com.wynntils.utils.render.Texture;
import com.wynntils.utils.render.type.HorizontalAlignment;
import com.wynntils.utils.render.type.TextShadow;
import com.wynntils.utils.render.type.VerticalAlignment;
import java.util.ArrayList;
import java.util.Collection;
import java.util.Comparator;
import java.util.List;
import java.util.Objects;
import java.util.stream.Stream;
import net.minecraft.ChatFormatting;
import net.minecraft.client.gui.components.Renderable;
import net.minecraft.client.gui.components.events.GuiEventListener;
import net.minecraft.client.gui.screens.Screen;
import net.minecraft.network.chat.Component;
import org.lwjgl.glfw.GLFW;

public final class WynntilsBookSettingsScreen extends WynntilsScreen implements TextboxScreen {
    private static final int CONFIGURABLES_PER_PAGE = 13;
    private static final int CONFIGS_PER_PAGE = 4;
    private final List<WynntilsButton> configurables = new ArrayList<>();
    private final List<WynntilsButton> configs = new ArrayList<>();

    private TextInputBoxWidget focusedTextInput;
    private final SearchWidget searchWidget;
    private ScrollButton configurableListScrollButton;
    private ScrollButton configListScrollButton;

    private Configurable selected = null;

    private int configurableScrollOffset = 0;
    private int configScrollOffset = 0;

    private WynntilsBookSettingsScreen() {
        super(Component.translatable("screens.wynntils.settingsScreen.name"));

        searchWidget = new SearchWidget(
                95, Texture.SETTING_BACKGROUND.height() - 32, 100, 20, s -> reloadConfigurableButtons(), this);
        reloadConfigurableButtons();
    }

    public static Screen create() {
        return new WynntilsBookSettingsScreen();
    }

    @Override
    protected void doInit() {
        reloadConfigurableButtons();

        this.addRenderableWidget(searchWidget);

        this.addRenderableWidget(new GeneralSettingsButton(
                55,
                Texture.SETTING_BACKGROUND.height() - 30,
                35,
                14,
                Component.translatable("screens.wynntils.settingsScreen.apply"),
                () -> {
                    Managers.Config.saveConfig();
                    this.onClose();
                },
                List.of(Component.translatable("screens.wynntils.settingsScreen.apply.description")
                        .withStyle(ChatFormatting.GREEN))));

        this.addRenderableWidget(new GeneralSettingsButton(
                15,
                Texture.SETTING_BACKGROUND.height() - 30,
                35,
                14,
                Component.translatable("screens.wynntils.settingsScreen.close"),
                this::onClose,
                List.of(Component.translatable("screens.wynntils.settingsScreen.close.description")
                        .withStyle(ChatFormatting.DARK_RED))));
    }

    // region Render

    @Override
    public void doRender(PoseStack poseStack, int mouseX, int mouseY, float partialTick) {
        float backgroundRenderX = getTranslationX();
        float backgroundRenderY = getTranslationY();

        poseStack.pushPose();
        poseStack.translate(backgroundRenderX, backgroundRenderY, 0);

        renderBg(poseStack);

        renderScrollArea(poseStack);

        renderWidgets(poseStack, mouseX, mouseY, partialTick);

        if (selected != null) {
            renderConfigTitle(poseStack);
        }

        poseStack.popPose();
    }

    private void renderConfigTitle(PoseStack poseStack) {
        String name = "";
        boolean enabled = false;
        if (selected instanceof Overlay selectedOverlay) {
            enabled = selectedOverlay.shouldBeEnabled();
            name = selectedOverlay.getTranslatedName();
        } else if (selected instanceof Feature selectedFeature) {
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
                        TextShadow.NONE);
        poseStack.popPose();
    }

    private void renderWidgets(PoseStack poseStack, int mouseX, int mouseY, float partialTick) {
        int adjustedMouseX = mouseX - (int) getTranslationX();
        int adjustedMouseY = mouseY - (int) getTranslationY();

        for (Renderable renderable : renderables) {
            renderable.render(poseStack, adjustedMouseX, adjustedMouseY, partialTick);
        }

        configurableListScrollButton.renderWidget(poseStack, adjustedMouseX, adjustedMouseY, partialTick);

        for (int i = configurableScrollOffset * CONFIGURABLES_PER_PAGE;
                i < Math.min(configurables.size(), (configurableScrollOffset + 1) * CONFIGURABLES_PER_PAGE);
                i++) {
            WynntilsButton featureButton = configurables.get(i);
            featureButton.render(poseStack, adjustedMouseX, adjustedMouseY, partialTick);
        }

        if (configListScrollButton != null) {
            configListScrollButton.renderWidget(poseStack, adjustedMouseX, adjustedMouseY, partialTick);
        }

        // Reverse iteration for so tooltip Z levels are correct when rendering
        for (int i = Math.min(configs.size(), configScrollOffset + CONFIGS_PER_PAGE) - 1;
                i >= configScrollOffset;
                i--) {
            WynntilsButton configButton = configs.get(i);
            configButton.render(poseStack, adjustedMouseX, adjustedMouseY, partialTick);
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
        double adjustedMouseX = mouseX - getTranslationX();
        double adjustedMouseY = mouseY - getTranslationY();

        for (GuiEventListener child : children()) {
            if (child.isMouseOver(adjustedMouseX, adjustedMouseY)) {
                child.mouseClicked(adjustedMouseX, adjustedMouseY, button);
            }
        }

        for (int i = configurableScrollOffset * CONFIGURABLES_PER_PAGE;
                i < Math.min(configurables.size(), (configurableScrollOffset + 1) * CONFIGURABLES_PER_PAGE);
                i++) {
            WynntilsButton featureButton = configurables.get(i);
            if (featureButton.isMouseOver(adjustedMouseX, adjustedMouseY)) {
                featureButton.mouseClicked(adjustedMouseX, adjustedMouseY, button);
            }
        }

        for (int i = configScrollOffset; i < Math.min(configs.size(), configScrollOffset + CONFIGS_PER_PAGE); i++) {
            WynntilsButton configButton = configs.get(i);
            if (configButton.isMouseOver(adjustedMouseX, adjustedMouseY)) {
                configButton.mouseClicked(adjustedMouseX, adjustedMouseY, button);
            }
        }

        if (configurableListScrollButton.isMouseOver(adjustedMouseX, adjustedMouseY)) {
            configurableListScrollButton.mouseClicked(adjustedMouseX, adjustedMouseY, button);
        }

        if (configListScrollButton != null && configListScrollButton.isMouseOver(adjustedMouseX, adjustedMouseY)) {
            configListScrollButton.mouseClicked(adjustedMouseX, adjustedMouseY, button);
        }

        return true;
    }

    @Override
    public boolean mouseScrolled(double mouseX, double mouseY, double delta) {
        double adjustedMouseX = mouseX - getTranslationX();
        double adjustedMouseY = mouseY - getTranslationY();

        if (adjustedMouseX <= Texture.SETTING_BACKGROUND.width() / 2f) {
            configurableListScrollButton.mouseScrolled(adjustedMouseX, adjustedMouseY, delta);
        } else if (configListScrollButton != null) {
            configListScrollButton.mouseScrolled(adjustedMouseX, adjustedMouseY, delta);
        }

        return true;
    }

    @Override
    public boolean mouseReleased(double mouseX, double mouseY, int button) {
        double adjustedMouseX = mouseX - getTranslationX();
        double adjustedMouseY = mouseY - getTranslationY();

        for (GuiEventListener child : children()) {
            if (child.isMouseOver(adjustedMouseX, adjustedMouseY)) {
                child.mouseReleased(adjustedMouseX, adjustedMouseY, button);
            }
        }

        for (int i = configurableScrollOffset * CONFIGURABLES_PER_PAGE;
             i < Math.min(configurables.size(), (configurableScrollOffset + 1) * CONFIGURABLES_PER_PAGE);
             i++) {
            WynntilsButton featureButton = configurables.get(i);
            featureButton.mouseReleased(adjustedMouseX, adjustedMouseY, button);
        }

        for (int i = configScrollOffset; i < Math.min(configs.size(), configScrollOffset + CONFIGS_PER_PAGE); i++) {
            WynntilsButton configButton = configs.get(i);
            configButton.mouseReleased(adjustedMouseX, adjustedMouseY, button);
        }

        configurableListScrollButton.mouseReleased(adjustedMouseX, adjustedMouseY, button);

        if (configListScrollButton != null) {
            configListScrollButton.mouseReleased(adjustedMouseX, adjustedMouseY, button);
        }

        return true;
    }

    @Override
    public boolean mouseDragged(double mouseX, double mouseY, int button, double dragX, double dragY) {
        double adjustedMouseX = mouseX - getTranslationX();
        double adjustedMouseY = mouseY - getTranslationY();

        for (GuiEventListener child : children()) {
            if (child.isMouseOver(adjustedMouseX, adjustedMouseY)) {
                child.mouseDragged(adjustedMouseX, adjustedMouseY, button, dragX, dragY);
            }
        }

        for (int i = configurableScrollOffset * CONFIGURABLES_PER_PAGE;
             i < Math.min(configurables.size(), (configurableScrollOffset + 1) * CONFIGURABLES_PER_PAGE);
             i++) {
            WynntilsButton featureButton = configurables.get(i);
            featureButton.mouseDragged(adjustedMouseX, adjustedMouseY, button, dragX, dragY);
        }

        for (int i = configScrollOffset; i < Math.min(configs.size(), configScrollOffset + CONFIGS_PER_PAGE); i++) {
            WynntilsButton configButton = configs.get(i);
            configButton.mouseDragged(adjustedMouseX, adjustedMouseY, button, dragX, dragY);
        }

        configurableListScrollButton.mouseDragged(adjustedMouseX, adjustedMouseY, button, dragX, dragY);
        if (configListScrollButton != null) {
            configListScrollButton.mouseDragged(adjustedMouseX, adjustedMouseY, button, dragX, dragY);
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

        configurableScrollOffset = MathUtils.clamp((int) (configurableScrollOffset + delta), 0, roundedUpPageNeed);
    }

    private void scrollConfigList(double delta) {
        int roundedUpPageNeed = configs.size() / CONFIGS_PER_PAGE + (configs.size() % CONFIGS_PER_PAGE == 0 ? 0 : 1);
        configScrollOffset = MathUtils.clamp(
                (int) (configScrollOffset + delta),
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
        Managers.Config.reloadConfiguration();
        super.onClose();
    }

    private void reloadConfigurableButtons() {
        configurables.clear();
        configurableScrollOffset = 0;

        Category oldCategory = null;

        List<Feature> featureList = Managers.Feature.getFeatures().stream()
                .filter(feature -> searchMatches(feature)
                        || feature.getVisibleConfigOptions().stream().anyMatch(this::configOptionContains)
                        || Managers.Overlay.getFeatureOverlays(feature).stream()
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

            for (Overlay overlay : Managers.Overlay.getFeatureOverlays(feature)) {
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

        if (selected != null) {
            Stream<Configurable> configurablesList = Stream.concat(
                    Managers.Feature.getFeatures().stream(),
                    Managers.Feature.getFeatures().stream()
                            .map(Managers.Overlay::getFeatureOverlays)
                            .flatMap(Collection::stream)
                            .map(overlay -> (Configurable) overlay));

            Configurable newSelected = configurablesList
                    .filter(configurable -> configurable.getConfigJsonName().equals(selected.getConfigJsonName()))
                    .findFirst()
                    .orElse(null);

            setSelected(newSelected);
        }
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

        if (selected == null) {
            configListScrollButton = null;
            return;
        }

        List<ConfigHolder> configsOptions = selected.getVisibleConfigOptions().stream()
                .sorted(Comparator.comparing(
                        configHolder -> !Objects.equals(configHolder.getFieldName(), "userEnabled")))
                .toList();

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

    public Configurable getSelected() {
        return selected;
    }

    public void setSelected(Configurable selected) {
        this.selected = selected;
        reloadConfigButtons();
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
