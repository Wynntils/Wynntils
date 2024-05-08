/*
 * Copyright © Wynntils 2022-2024.
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
import com.wynntils.screens.base.widgets.SearchWidget;
import com.wynntils.screens.base.widgets.TextInputBoxWidget;
import com.wynntils.screens.base.widgets.WynntilsButton;
import com.wynntils.screens.settings.widgets.ApplyButton;
import com.wynntils.screens.settings.widgets.CategoryButton;
import com.wynntils.screens.settings.widgets.CloseButton;
import com.wynntils.screens.settings.widgets.ConfigTile;
import com.wynntils.screens.settings.widgets.ConfigurableButton;
import com.wynntils.utils.MathUtils;
import com.wynntils.utils.StringUtils;
import com.wynntils.utils.colors.CommonColors;
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

public final class WynntilsBookSettingsScreen extends WynntilsScreen {
    private static final float SCROLL_FACTOR = 10f;
    private static final int CONFIGURABLES_PER_PAGE = 13;
    private static final int CONFIGS_PER_PAGE = 4;
    private static final int CONFIGURABLE_SCROLL_X = 23;
    private static final int CONFIG_SCROLL_X = Texture.CONFIG_BOOK_BACKGROUND.width() - 23;
    private static final int SCROLL_START_Y = 17;

    private final List<WynntilsButton> configurables = new ArrayList<>();
    private final List<WynntilsButton> configs = new ArrayList<>();
    private List<Configurable> configurableList;

    private TextInputBoxWidget focusedTextInput;
    private final SearchWidget searchWidget;

    private Configurable selected = null;

    private boolean draggingConfigurableScroll = false;
    private boolean draggingConfigScroll = false;
    private int configurableScrollOffset = 0;
    private int configScrollOffset = 0;
    private float configurableScrollRenderY;
    private float configScrollRenderY;

    private WynntilsBookSettingsScreen() {
        super(Component.translatable("screens.wynntils.settingsScreen.name"));

        searchWidget = new SearchWidget(
                95, Texture.CONFIG_BOOK_BACKGROUND.height() - 32, 100, 20, s -> reloadConfigurableButtons(), this);
        setFocusedTextInput(searchWidget);
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

    @Override
    public void doRender(GuiGraphics guiGraphics, int mouseX, int mouseY, float partialTick) {
        float backgroundRenderX = getTranslationX();
        float backgroundRenderY = getTranslationY();

        PoseStack poseStack = guiGraphics.pose();

        poseStack.pushPose();
        poseStack.translate(backgroundRenderX, backgroundRenderY, 0);

        renderBg(poseStack);

        renderScrollArea(poseStack);

        renderConfigurableScroll(poseStack);

        renderWidgets(guiGraphics, mouseX, mouseY, partialTick);

        if (selected != null) {
            renderConfigTitle(poseStack);

            renderConfigScroll(poseStack);
        }

        poseStack.popPose();
    }

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

    @Override
    public boolean doMouseClicked(double mouseX, double mouseY, int button) {
        double adjustedMouseX = mouseX - getTranslationX();
        double adjustedMouseY = mouseY - getTranslationY();

        for (GuiEventListener listener : getWidgetsForIteration().toList()) {
            if (listener.isMouseOver(adjustedMouseX, adjustedMouseY)) {
                return listener.mouseClicked(adjustedMouseX, adjustedMouseY, button);
            }
        }

        if (!draggingConfigurableScroll
                && MathUtils.isInside(
                        (int) adjustedMouseX,
                        (int) adjustedMouseY,
                        CONFIGURABLE_SCROLL_X,
                        CONFIGURABLE_SCROLL_X + Texture.CONFIG_BOOK_SCROLL_BUTTON.width(),
                        (int) configurableScrollRenderY,
                        (int) (configurableScrollRenderY + Texture.CONFIG_BOOK_SCROLL_BUTTON.height() / 2))) {
            draggingConfigurableScroll = true;
            return true;
        }

        if (!draggingConfigScroll
                && (configs.size() > CONFIGS_PER_PAGE)
                && MathUtils.isInside(
                        (int) adjustedMouseX,
                        (int) adjustedMouseY,
                        CONFIG_SCROLL_X,
                        CONFIG_SCROLL_X + Texture.CONFIG_BOOK_SCROLL_BUTTON.width(),
                        (int) configScrollRenderY,
                        (int) (configScrollRenderY + Texture.CONFIG_BOOK_SCROLL_BUTTON.height() / 2))) {
            draggingConfigScroll = true;
            return true;
        }

        return true;
    }

    @Override
    public boolean mouseDragged(double mouseX, double mouseY, int button, double dragX, double dragY) {
        double adjustedMouseX = mouseX - getTranslationX();
        double adjustedMouseY = mouseY - getTranslationY();

        if (draggingConfigurableScroll) {
            int scrollAreaStartY = SCROLL_START_Y + 7;
            int scrollAreaHeight =
                    (int) (CONFIGURABLES_PER_PAGE * 12 - Texture.CONFIG_BOOK_SCROLL_BUTTON.height() / 2f);

            int newOffset = Math.round(MathUtils.map(
                    (float) adjustedMouseY,
                    scrollAreaStartY,
                    scrollAreaStartY + scrollAreaHeight,
                    0,
                    getMaxConfigurableScrollOffset()));

            newOffset = Math.max(0, Math.min(newOffset, getMaxConfigurableScrollOffset()));

            scrollConfigurables(newOffset);

            return true;
        }

        if (draggingConfigScroll) {
            int scrollAreaStartY = SCROLL_START_Y + 7;
            int scrollAreaHeight = (int) (CONFIGS_PER_PAGE * 46 - Texture.CONFIG_BOOK_SCROLL_BUTTON.height() / 2f);

            int newOffset = Math.round(MathUtils.map(
                    (float) adjustedMouseY,
                    scrollAreaStartY,
                    scrollAreaStartY + scrollAreaHeight,
                    0,
                    getMaxConfigScrollOffset()));

            newOffset = Math.max(0, Math.min(newOffset, getMaxConfigScrollOffset()));

            scrollConfigs(newOffset);

            return true;
        }

        for (GuiEventListener listener : getWidgetsForIteration().toList()) {
            if (listener.isMouseOver(adjustedMouseX, adjustedMouseY)) {
                return listener.mouseDragged(adjustedMouseX, adjustedMouseY, button, dragX, dragY);
            }
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

        draggingConfigurableScroll = false;
        draggingConfigScroll = false;

        return true;
    }

    @Override
    public boolean mouseScrolled(double mouseX, double mouseY, double deltaX, double deltaY) {
        double adjustedMouseX = mouseX - getTranslationX();
        int scrollAmount = (int) (-deltaY * SCROLL_FACTOR);

        if (adjustedMouseX <= Texture.CONFIG_BOOK_BACKGROUND.width() / 2f) {
            int newOffset =
                    Math.max(0, Math.min(configurableScrollOffset + scrollAmount, getMaxConfigurableScrollOffset()));
            scrollConfigurables(newOffset);
        } else if (configs.size() > CONFIGS_PER_PAGE) {
            int newOffset = Math.max(0, Math.min(configScrollOffset + scrollAmount, getMaxConfigScrollOffset()));
            scrollConfigs(newOffset);
        }

        return true;
    }

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

    @Override
    public TextInputBoxWidget getFocusedTextInput() {
        return focusedTextInput;
    }

    @Override
    public void setFocusedTextInput(TextInputBoxWidget focusedTextInput) {
        this.focusedTextInput = focusedTextInput;
    }

    public boolean configOptionContains(Config<?> config) {
        return !searchWidget.getTextBoxInput().isEmpty()
                && StringUtils.containsIgnoreCase(config.getDisplayName(), searchWidget.getTextBoxInput());
    }

    public Configurable getSelected() {
        return selected;
    }

    public void setSelected(Configurable selected) {
        this.selected = selected;
        reloadConfigButtons();
    }

    public float getTranslationY() {
        return (this.height - Texture.CONFIG_BOOK_BACKGROUND.height()) / 2f;
    }

    public float getTranslationX() {
        return (this.width - Texture.CONFIG_BOOK_BACKGROUND.width()) / 2f;
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

        FontRenderer.getInstance()
                .renderScrollingText(
                        poseStack,
                        StyledText.fromString(name + ": "
                                + (enabled
                                        ? ChatFormatting.DARK_GREEN + "Enabled"
                                        : ChatFormatting.DARK_RED + "Disabled")),
                        Texture.CONFIG_BOOK_BACKGROUND.width() / 2f + 10,
                        10,
                        Texture.CONFIG_BOOK_BACKGROUND.width() / 2f - 20,
                        getTranslationX(),
                        getTranslationY(),
                        CommonColors.BLACK,
                        HorizontalAlignment.LEFT,
                        VerticalAlignment.TOP,
                        TextShadow.NONE,
                        0.8f);
    }

    private void renderWidgets(GuiGraphics guiGraphics, int mouseX, int mouseY, float partialTick) {
        int adjustedMouseX = mouseX - (int) getTranslationX();
        int adjustedMouseY = mouseY - (int) getTranslationY();

        for (Renderable renderable : renderables) {
            renderable.render(guiGraphics, adjustedMouseX, adjustedMouseY, partialTick);
        }

        RenderUtils.createRectMask(guiGraphics.pose(), 37, 16, 140, 163);

        for (WynntilsButton configurable : configurables) {
            configurable.render(guiGraphics, adjustedMouseX, adjustedMouseY, partialTick);
        }

        RenderUtils.clearMask();

        RenderUtils.createRectMask(
                guiGraphics.pose(), Texture.CONFIG_BOOK_BACKGROUND.width() / 2f + 10, 21, 160, CONFIGS_PER_PAGE * 46);

        for (WynntilsButton config : configs) {
            config.render(guiGraphics, adjustedMouseX, adjustedMouseY, partialTick);
        }

        RenderUtils.clearMask();
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

    private void renderConfigurableScroll(PoseStack poseStack) {
        configurableScrollRenderY = SCROLL_START_Y
                + MathUtils.map(
                        configurableScrollOffset,
                        0,
                        getMaxConfigurableScrollOffset(),
                        0,
                        161 - Texture.CONFIG_BOOK_SCROLL_BUTTON.height() / 2f);

        RenderUtils.drawHoverableTexturedRect(
                poseStack,
                Texture.CONFIG_BOOK_SCROLL_BUTTON,
                CONFIGURABLE_SCROLL_X,
                configurableScrollRenderY,
                draggingConfigurableScroll);
    }

    private void renderConfigScroll(PoseStack poseStack) {
        if (configs.size() <= CONFIGS_PER_PAGE) return;

        RenderUtils.drawRect(
                poseStack,
                CommonColors.GRAY,
                CONFIG_SCROLL_X,
                SCROLL_START_Y,
                0,
                Texture.CONFIG_BOOK_SCROLL_BUTTON.width(),
                CONFIGS_PER_PAGE * 46);

        configScrollRenderY = SCROLL_START_Y
                + MathUtils.map(
                        configScrollOffset,
                        0,
                        getMaxConfigScrollOffset(),
                        0,
                        CONFIGS_PER_PAGE * 46 - Texture.CONFIG_BOOK_SCROLL_BUTTON.height() / 2f);

        RenderUtils.drawHoverableTexturedRect(
                poseStack,
                Texture.CONFIG_BOOK_SCROLL_BUTTON,
                CONFIG_SCROLL_X,
                configScrollRenderY,
                draggingConfigScroll);
    }

    private static void renderBg(PoseStack poseStack) {
        RenderUtils.drawTexturedRect(poseStack, Texture.CONFIG_BOOK_BACKGROUND, 0, 0);
    }

    private Stream<GuiEventListener> getWidgetsForIteration() {
        return Stream.concat(children().stream(), Stream.concat(configurables.stream(), configs.stream()));
    }

    private void scrollConfigurables(int newOffset) {
        configurableScrollOffset = newOffset;

        for (WynntilsButton configurable : configurables) {
            int newY = 21 + (configurables.indexOf(configurable) * 12) - configurableScrollOffset;

            configurable.setY(newY);
            configurable.visible = newY >= (21 - 12) && newY <= (21 + (CONFIGURABLES_PER_PAGE + 1) * 11);
        }
    }

    private int getMaxConfigurableScrollOffset() {
        return (configurables.size() - CONFIGURABLES_PER_PAGE) * 12;
    }

    private void scrollConfigs(int newOffset) {
        configScrollOffset = newOffset;

        for (WynntilsButton config : configs) {
            int newY = 21 + (configs.indexOf(config) * 46) - configScrollOffset;

            config.setY(newY);
            config.visible = newY >= (21 - 46) && newY <= (21 + CONFIGS_PER_PAGE * 45);
        }
    }

    private int getMaxConfigScrollOffset() {
        return (configs.size() - CONFIGS_PER_PAGE) * 46;
    }

    private void reloadConfigurableButtons() {
        configurables.clear();
        configurableScrollOffset = 0;

        Category oldCategory = null;

        configurableList = Managers.Feature.getFeatures().stream()
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

        int renderY = 21;

        for (int i = 0; i < configurableList.size(); i++) {
            Configurable configurable = configurableList.get(i);

            Category category;

            if (configurable instanceof Feature feature) {
                category = feature.getCategory();
            } else if (configurable instanceof Overlay overlay) {
                category = Managers.Overlay.getOverlayParent(overlay).getCategory();
            } else {
                throw new IllegalStateException("Unknown configurable type: " + configurable.getClass());
            }

            if (category != oldCategory) {
                CategoryButton categoryButton = new CategoryButton(37, renderY, 140, 10, category);
                categoryButton.visible = renderY >= (21 - 12) && renderY <= (21 + (CONFIGURABLES_PER_PAGE + 1) * 11);
                configurables.add(categoryButton);
                oldCategory = category;
                renderY += 12;
            }

            ConfigurableButton configurableButton = new ConfigurableButton(37, renderY, 140, 10, configurable, this);
            configurableButton.visible = renderY >= (21 - 12) && renderY <= (21 + (CONFIGURABLES_PER_PAGE + 1) * 11);
            configurables.add(configurableButton);

            renderY += 12;

            if (configurable instanceof Feature feature) {
                for (Overlay overlay : Managers.Overlay.getFeatureOverlays(feature).stream()
                        .sorted()
                        .toList()) {
                    ConfigurableButton overlayButton = new ConfigurableButton(37, renderY, 140, 10, overlay, this);
                    overlayButton.visible = renderY >= (21 - 12) && renderY <= (21 + (CONFIGURABLES_PER_PAGE + 1) * 11);
                    configurables.add(overlayButton);
                    renderY += 12;
                }
            }
        }

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

    private boolean searchMatches(Translatable translatable) {
        return StringUtils.partialMatch(translatable.getTranslatedName(), searchWidget.getTextBoxInput());
    }

    private void reloadConfigButtons() {
        configs.clear();
        configScrollOffset = 0;

        if (selected == null) return;

        List<Config<?>> configsOptions = selected.getVisibleConfigOptions().stream()
                .sorted(Comparator.comparing(config -> !Objects.equals(config.getFieldName(), "userEnabled")))
                .toList();

        int renderY = 21;

        for (int i = 0; i < configsOptions.size(); i++) {
            Config<?> config = configsOptions.get(i);

            configs.add(
                    new ConfigTile(Texture.CONFIG_BOOK_BACKGROUND.width() / 2 + 10, renderY, 160, 45, this, config));

            renderY += 46;
        }
    }
}
