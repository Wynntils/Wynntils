/*
 * Copyright © Wynntils 2022-2023.
 * This file is released under LGPLv3. See LICENSE for full license details.
 */
package com.wynntils.screens.settings;

import com.mojang.blaze3d.systems.RenderSystem;
import com.mojang.blaze3d.vertex.PoseStack;
import com.wynntils.core.components.Managers;
import com.wynntils.core.consumers.features.Configurable;
import com.wynntils.core.consumers.features.Feature;
import com.wynntils.core.consumers.overlays.Overlay;
import com.wynntils.core.consumers.screens.WynntilsScreen;
import com.wynntils.core.persisted.Translatable;
import com.wynntils.core.persisted.config.Category;
import com.wynntils.core.persisted.config.Config;
import com.wynntils.core.text.StyledText;
import com.wynntils.screens.base.TextboxScreen;
import com.wynntils.screens.base.widgets.SearchWidget;
import com.wynntils.screens.base.widgets.TextInputBoxWidget;
import com.wynntils.screens.base.widgets.WynntilsButton;
import com.wynntils.screens.settings.widgets.ApplyButton;
import com.wynntils.screens.settings.widgets.CategoryButton;
import com.wynntils.screens.settings.widgets.CloseButton;
import com.wynntils.screens.settings.widgets.ConfigTile;
import com.wynntils.screens.settings.widgets.ConfigurableButton;
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
import java.util.stream.Collectors;
import java.util.stream.Stream;
import net.minecraft.ChatFormatting;
import net.minecraft.client.gui.GuiGraphics;
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
                95, Texture.CONFIG_BOOK_BACKGROUND.height() - 32, 100, 20, s -> reloadConfigurableButtons(), this);
        setFocusedTextInput(searchWidget);
        reloadConfigurableButtons();
    }

    public static Screen create() {
        return new WynntilsBookSettingsScreen();
    }

    @Override
    protected void doInit() {
        reloadConfigurableButtons();

        this.addRenderableWidget(searchWidget);

        this.addRenderableWidget(new ApplyButton(this));

        this.addRenderableWidget(new CloseButton(this));
    }

    // region Render

    @Override
    public void doRender(GuiGraphics guiGraphics, int mouseX, int mouseY, float partialTick) {
        float backgroundRenderX = getTranslationX();
        float backgroundRenderY = getTranslationY();

        PoseStack poseStack = guiGraphics.pose();

        poseStack.pushPose();
        poseStack.translate(backgroundRenderX, backgroundRenderY, 0);

        renderBg(poseStack);

        renderScrollArea(poseStack);

        renderWidgets(guiGraphics, mouseX, mouseY, partialTick);

        if (selected != null) {
            renderConfigTitle(poseStack);
        }

        poseStack.popPose();
    }

    private void renderConfigTitle(PoseStack poseStack) {
        String name = "";
        boolean enabled = false;
        if (selected instanceof Overlay selectedOverlay) {
            enabled = Managers.Overlay.isEnabled(selectedOverlay);
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
                        StyledText.fromString(name + ": "
                                + (enabled
                                        ? ChatFormatting.DARK_GREEN + "Enabled"
                                        : ChatFormatting.DARK_RED + "Disabled")),
                        Texture.CONFIG_BOOK_BACKGROUND.width() / 2f / 0.8f + 10,
                        12,
                        CommonColors.BLACK,
                        HorizontalAlignment.LEFT,
                        VerticalAlignment.TOP,
                        TextShadow.NONE);
        poseStack.popPose();
    }

    private void renderWidgets(GuiGraphics guiGraphics, int mouseX, int mouseY, float partialTick) {
        int adjustedMouseX = mouseX - (int) getTranslationX();
        int adjustedMouseY = mouseY - (int) getTranslationY();

        for (Renderable renderable : renderables) {
            renderable.render(guiGraphics, adjustedMouseX, adjustedMouseY, partialTick);
        }

        configurableListScrollButton.renderWidget(guiGraphics, adjustedMouseX, adjustedMouseY, partialTick);

        if (configListScrollButton != null) {
            configListScrollButton.renderWidget(guiGraphics, adjustedMouseX, adjustedMouseY, partialTick);
        }

        // Reverse iteration for so tooltip Z levels are correct when rendering
        for (int i = Math.min(configs.size(), configScrollOffset + CONFIGS_PER_PAGE) - 1;
                i >= configScrollOffset;
                i--) {
            WynntilsButton configButton = configs.get(i);
            configButton.render(guiGraphics, adjustedMouseX, adjustedMouseY, partialTick);
        }

        // Render configurable's after configs so tooltip Z levels are correct
        // Reverse iteration for so tooltip Z levels are correct when rendering
        for (int i = Math.min(configurables.size(), (configurableScrollOffset + 1) * CONFIGURABLES_PER_PAGE) - 1;
                i >= configurableScrollOffset * CONFIGURABLES_PER_PAGE;
                i--) {
            WynntilsButton featureButton = configurables.get(i);
            featureButton.render(guiGraphics, adjustedMouseX, adjustedMouseY, partialTick);
        }
    }

    private static void renderScrollArea(PoseStack poseStack) {
        RenderSystem.enableBlend();
        RenderUtils.drawTexturedRect(
                poseStack,
                Texture.CONFIG_BOOK_SCROLL_AREA,
                (Texture.CONFIG_BOOK_BACKGROUND.width() / 2f - Texture.CONFIG_BOOK_SCROLL_AREA.width()) / 2f,
                10);
        RenderSystem.disableBlend();
    }

    private static void renderBg(PoseStack poseStack) {
        RenderUtils.drawTexturedRect(poseStack, Texture.CONFIG_BOOK_BACKGROUND, 0, 0);
    }

    // endregion

    // region Mouse events

    @Override
    public boolean doMouseClicked(double mouseX, double mouseY, int button) {
        double adjustedMouseX = mouseX - getTranslationX();
        double adjustedMouseY = mouseY - getTranslationY();

        for (GuiEventListener listener : getWidgetsForIteration().toList()) {
            if (listener.isMouseOver(adjustedMouseX, adjustedMouseY)) {
                return listener.mouseClicked(adjustedMouseX, adjustedMouseY, button);
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
    public boolean mouseDragged(double mouseX, double mouseY, int button, double dragX, double dragY) {
        double adjustedMouseX = mouseX - getTranslationX();
        double adjustedMouseY = mouseY - getTranslationY();

        for (GuiEventListener listener : getWidgetsForIteration().toList()) {
            if (listener.isMouseOver(adjustedMouseX, adjustedMouseY)) {
                return listener.mouseDragged(adjustedMouseX, adjustedMouseY, button, dragX, dragY);
            }
        }

        configurableListScrollButton.mouseDragged(adjustedMouseX, adjustedMouseY, button, dragX, dragY);
        if (configListScrollButton != null) {
            configListScrollButton.mouseDragged(adjustedMouseX, adjustedMouseY, button, dragX, dragY);
        }

        return true;
    }

    @Override
    public boolean mouseReleased(double mouseX, double mouseY, int button) {
        double adjustedMouseX = mouseX - getTranslationX();
        double adjustedMouseY = mouseY - getTranslationY();

        for (GuiEventListener listener : getWidgetsForIteration().toList()) {
            listener.mouseReleased(adjustedMouseX, adjustedMouseY, button);
        }

        configurableListScrollButton.mouseReleased(adjustedMouseX, adjustedMouseY, button);

        if (configListScrollButton != null) {
            configListScrollButton.mouseReleased(adjustedMouseX, adjustedMouseY, button);
        }

        return true;
    }

    @Override
    public boolean mouseScrolled(double mouseX, double mouseY, double deltaX, double deltaY) {
        double adjustedMouseX = mouseX - getTranslationX();
        double adjustedMouseY = mouseY - getTranslationY();

        if (adjustedMouseX <= Texture.CONFIG_BOOK_BACKGROUND.width() / 2f) {
            configurableListScrollButton.mouseScrolled(adjustedMouseX, adjustedMouseY, deltaX, deltaY);
        } else if (configListScrollButton != null) {
            configListScrollButton.mouseScrolled(adjustedMouseX, adjustedMouseY, deltaX, deltaY);
        }

        return true;
    }

    private Stream<GuiEventListener> getWidgetsForIteration() {
        return Stream.concat(
                children().stream(),
                Stream.concat(
                        configurables
                                .subList(
                                        configurableScrollOffset * CONFIGURABLES_PER_PAGE,
                                        Math.min(
                                                configurables.size(),
                                                (configurableScrollOffset + 1) * CONFIGURABLES_PER_PAGE))
                                .stream(),
                        configs
                                .subList(
                                        configScrollOffset,
                                        Math.min(configs.size(), configScrollOffset + CONFIGS_PER_PAGE))
                                .stream()));
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
    public void added() {
        searchWidget.opened();
        super.added();
    }

    @Override
    public void onClose() {
        Managers.Config.reloadConfiguration();
        super.onClose();
    }

    private void reloadConfigurableButtons() {
        configurables.clear();
        configurableScrollOffset = 0;

        Category oldCategory = null;

        List<Configurable> configurableList = Managers.Feature.getFeatures().stream()
                .filter(feature -> searchMatches(feature)
                        || feature.getVisibleConfigOptions().stream().anyMatch(this::configOptionContains))
                .map(feature -> (Configurable) feature)
                .sorted()
                .collect(Collectors.toList());

        configurableList.addAll(Managers.Overlay.getOverlays().stream()
                .filter(overlay -> !configurableList.contains(Managers.Overlay.getOverlayParent(overlay)))
                .filter(overlay -> searchMatches(overlay)
                        || overlay.getVisibleConfigOptions().stream().anyMatch(this::configOptionContains))
                .sorted()
                .toList());

        int offset = 0;
        for (int i = 0; i < configurableList.size(); i++) {
            Configurable configurable = configurableList.get(i);

            int renderIndex = (i + offset) % CONFIGURABLES_PER_PAGE;

            Category category;

            if (configurable instanceof Feature feature) {
                category = feature.getCategory();
            } else if (configurable instanceof Overlay overlay) {
                category = Managers.Overlay.getOverlayParent(overlay).getCategory();
            } else {
                throw new IllegalStateException("Unknown configurable type: " + configurable.getClass());
            }

            if (category != oldCategory) {
                configurables.add(new CategoryButton(37, 21 + renderIndex * 12, 140, 10, category));
                oldCategory = category;
                offset++;
                renderIndex = (i + offset) % CONFIGURABLES_PER_PAGE;
            }

            configurables.add(new ConfigurableButton(37, 21 + renderIndex * 12, 140, 10, configurable));

            if (configurable instanceof Feature feature) {
                for (Overlay overlay : Managers.Overlay.getFeatureOverlays(feature).stream()
                        .sorted()
                        .toList()) {
                    offset++;
                    renderIndex = (i + offset) % CONFIGURABLES_PER_PAGE;
                    configurables.add(new ConfigurableButton(37, 21 + renderIndex * 12, 140, 10, overlay));
                }
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
                Texture.CONFIG_BOOK_BACKGROUND.height() - 50,
                Texture.CONFIG_BOOK_SCROLL_BUTTON.width(),
                Texture.CONFIG_BOOK_SCROLL_BUTTON.height() / 2,
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
                    .filter(configurable -> configurable.getJsonName().equals(selected.getJsonName()))
                    .findFirst()
                    .orElse(null);

            setSelected(newSelected);
        }
    }

    public boolean configOptionContains(Config<?> config) {
        return !searchWidget.getTextBoxInput().isEmpty()
                && StringUtils.containsIgnoreCase(config.getDisplayName(), searchWidget.getTextBoxInput());
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

        List<Config<?>> configsOptions = selected.getVisibleConfigOptions().stream()
                .sorted(Comparator.comparing(config -> !Objects.equals(config.getFieldName(), "userEnabled")))
                .toList();

        for (int i = 0; i < configsOptions.size(); i++) {
            Config<?> config = configsOptions.get(i);

            int renderIndex = i % CONFIGS_PER_PAGE;

            configs.add(new ConfigTile(
                    Texture.CONFIG_BOOK_BACKGROUND.width() / 2 + 10, 21 + renderIndex * 46, 160, 45, this, config));
        }

        int roundedUpPageNeed = configs.size() / CONFIGS_PER_PAGE + (configs.size() % CONFIGS_PER_PAGE == 0 ? 0 : 1);
        configListScrollButton = new ScrollButton(
                Texture.CONFIG_BOOK_BACKGROUND.width() - 23,
                17,
                Texture.CONFIG_BOOK_BACKGROUND.height() - 25,
                Texture.CONFIG_BOOK_SCROLL_BUTTON.width(),
                Texture.CONFIG_BOOK_SCROLL_BUTTON.height() / 2,
                configs.size() <= CONFIGS_PER_PAGE ? 0 : (roundedUpPageNeed - 1) * CONFIGS_PER_PAGE,
                CONFIGS_PER_PAGE,
                this::scrollConfigList,
                CommonColors.GRAY);
    }

    private float getTranslationY() {
        return (this.height - Texture.CONFIG_BOOK_BACKGROUND.height()) / 2f;
    }

    private float getTranslationX() {
        return (this.width - Texture.CONFIG_BOOK_BACKGROUND.width()) / 2f;
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
