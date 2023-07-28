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
import com.wynntils.core.consumers.features.Configurable;
import com.wynntils.core.consumers.features.Feature;
import com.wynntils.core.consumers.features.Translatable;
import com.wynntils.core.consumers.overlays.Overlay;
import com.wynntils.core.consumers.screens.WynntilsScreen;
import com.wynntils.core.text.StyledText;
import com.wynntils.screens.base.widgets.SearchWidget;
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
import net.minecraft.client.gui.components.events.GuiEventListener;
import net.minecraft.client.gui.screens.Screen;
import net.minecraft.network.chat.Component;

public final class WynntilsBookSettingsScreen extends WynntilsScreen {
    private static final int CONFIGURABLES_PER_PAGE = 13;
    private static final int CONFIGS_PER_PAGE = 4;
    private final List<WynntilsButton> configurables = new ArrayList<>();
    private final List<ConfigTile> configs = new ArrayList<>();

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
        setFocused(searchWidget);
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

        this.addRenderableWidget(configurableListScrollButton);
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
                        Texture.SETTING_BACKGROUND.width() / 2f / 0.8f + 10,
                        12,
                        CommonColors.BLACK,
                        HorizontalAlignment.LEFT,
                        VerticalAlignment.TOP,
                        TextShadow.NONE);
        poseStack.popPose();
    }

    private void renderWidgets(PoseStack poseStack, int mouseX, int mouseY, float partialTick) {
        int adjustedMouseX = mouseX - (int) getTranslationX();
        int adjustedMouseY = mouseY - (int) getTranslationY();

        super.doRender(poseStack, adjustedMouseX, adjustedMouseY, partialTick);

        // Reverse iteration for so tooltip Z levels are correct when rendering
        for (int i = Math.min(configs.size(), configScrollOffset + CONFIGS_PER_PAGE) - 1;
                i >= configScrollOffset;
                i--) {
            ConfigTile configTile = configs.get(i);
            configTile.renderWidgets(poseStack, adjustedMouseX, adjustedMouseY, partialTick);
        }

        // Render configurable's after configs so tooltip Z levels are correct
        // Reverse iteration for so tooltip Z levels are correct when rendering
        for (int i = Math.min(configurables.size(), (configurableScrollOffset + 1) * CONFIGURABLES_PER_PAGE) - 1;
                i >= configurableScrollOffset * CONFIGURABLES_PER_PAGE;
                i--) {
            WynntilsButton featureButton = configurables.get(i);
            featureButton.render(poseStack, adjustedMouseX, adjustedMouseY, partialTick);
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
    public boolean doMouseClicked(double mouseX, double mouseY, int button) {
        return super.doMouseClicked(mouseX - getTranslationX(), mouseY - getTranslationY(), button);
    }

    @Override
    public boolean mouseDragged(double mouseX, double mouseY, int button, double dragX, double dragY) {
        return super.mouseDragged(mouseX - getTranslationX(), mouseY - getTranslationY(), button, dragX, dragY);
    }

    @Override
    public boolean mouseReleased(double mouseX, double mouseY, int button) {
        return super.mouseReleased(mouseX - getTranslationX(), mouseY - getTranslationY(), button);
    }

    @Override
    public boolean mouseScrolled(double mouseX, double mouseY, double delta) {
        double adjustedMouseX = mouseX - getTranslationX();
        double adjustedMouseY = mouseY - getTranslationY();

        if (adjustedMouseX <= Texture.SETTING_BACKGROUND.width() / 2f) {
            configurableListScrollButton.mouseScrolled(adjustedMouseX, adjustedMouseY, delta);
        } else if (configListScrollButton != null) {
            configListScrollButton.mouseScrolled(adjustedMouseX, adjustedMouseY, delta);
        } else return false;

        return true;
    }

    @Override
    public List<? extends GuiEventListener> children() {
        List<GuiEventListener> listeners = new ArrayList<>(configurables.subList(
                configurableScrollOffset * CONFIGURABLES_PER_PAGE,
                Math.min(configurables.size(), (configurableScrollOffset + 1) * CONFIGURABLES_PER_PAGE)));
        listeners.addAll(
                configs.subList(configScrollOffset, Math.min(configs.size(), configScrollOffset + CONFIGS_PER_PAGE)));
        listeners.addAll(super.children());
        return listeners;
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

            configs.add(new ConfigTile(
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
        addRenderableWidget(configListScrollButton);
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
}
