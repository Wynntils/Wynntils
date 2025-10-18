/*
 * Copyright © Wynntils 2024-2025.
 * This file is released under LGPLv3. See LICENSE for full license details.
 */
package com.wynntils.screens.overlays.selection.widgets;

import com.google.common.collect.Lists;
import com.google.common.collect.Streams;
import com.mojang.blaze3d.vertex.PoseStack;
import com.wynntils.core.components.Managers;
import com.wynntils.core.consumers.overlays.CustomNameProperty;
import com.wynntils.core.consumers.overlays.Overlay;
import com.wynntils.core.text.StyledText;
import com.wynntils.overlays.custombars.CustomBarOverlayBase;
import com.wynntils.overlays.infobox.InfoBoxOverlay;
import com.wynntils.screens.base.widgets.TextInputBoxWidget;
import com.wynntils.screens.base.widgets.WynntilsButton;
import com.wynntils.screens.overlays.selection.OverlaySelectionScreen;
import com.wynntils.utils.colors.CommonColors;
import com.wynntils.utils.colors.CustomColor;
import com.wynntils.utils.mc.ComponentUtils;
import com.wynntils.utils.mc.McUtils;
import com.wynntils.utils.render.FontRenderer;
import com.wynntils.utils.render.RenderUtils;
import com.wynntils.utils.render.type.HorizontalAlignment;
import com.wynntils.utils.render.type.TextShadow;
import com.wynntils.utils.render.type.VerticalAlignment;
import java.util.List;
import net.minecraft.client.gui.GuiGraphics;
import net.minecraft.network.chat.Component;
import org.lwjgl.glfw.GLFW;

public class OverlayButton extends WynntilsButton {
    private static final CustomColor ENABLED_COLOR = new CustomColor(0, 220, 0, 255);
    private static final CustomColor ENABLED_COLOR_BORDER = new CustomColor(0, 116, 0, 255);
    private static final CustomColor ENABLED_COLOR_BORDER_HOVERED = new CustomColor(0, 66, 0, 255);

    private static final CustomColor DISABLED_COLOR = new CustomColor(0, 0, 0, 255);
    private static final CustomColor DISABLED_COLOR_BORDER = new CustomColor(60, 60, 60, 255);
    private static final CustomColor DISABLED_COLOR_BORDER_HOVERED = new CustomColor(10, 10, 10, 255);

    private static final CustomColor DISABLED_FEATURE_COLOR = new CustomColor(255, 0, 0, 255);
    private static final CustomColor DISABLED_FEATURE_COLOR_BORDER = new CustomColor(120, 0, 0, 255);
    private static final CustomColor DISABLED_FEATURE_COLOR_BORDER_HOVERED = new CustomColor(70, 0, 0, 255);

    private static final List<Component> EDIT_NAME_TOOLTIP =
            List.of(Component.translatable("screens.wynntils.overlaySelection.editName"));

    private static final List<Component> SAVE_NAME_TOOLTIP =
            List.of(Component.translatable("screens.wynntils.overlaySelection.stopEdit"));

    private static final List<Component> VIEW_TOOLTIP =
            List.of(Component.translatable("screens.wynntils.overlaySelection.viewTooltip"));

    private final List<Component> descriptionTooltip;
    private final Overlay overlay;
    private final OverlaySelectionScreen selectionScreen;

    private String textToRender;
    private TextInputBoxWidget editInput;

    public OverlayButton(int x, int y, int width, int height, Overlay overlay, OverlaySelectionScreen selectionScreen) {
        super(x, y, width, height, Component.literal(overlay.getTranslatedName()));

        this.overlay = overlay;
        this.selectionScreen = selectionScreen;

        // Use custom name of overlay if present
        if (overlay instanceof CustomNameProperty customNameProperty) {
            if (!customNameProperty.getCustomName().get().isEmpty()) {
                textToRender = customNameProperty.getCustomName().get();
            } else {
                textToRender = overlay.getTranslatedName();
            }
        } else {
            textToRender = overlay.getTranslatedName();
        }

        // Display a tooltip with delete instructions for info boxes and custom bars.
        if (overlay instanceof InfoBoxOverlay) {
            descriptionTooltip = ComponentUtils.wrapTooltips(
                    Streams.concat(VIEW_TOOLTIP.stream(), EDIT_NAME_TOOLTIP.stream())
                            .toList(),
                    150);
        } else if (overlay instanceof CustomBarOverlayBase) {
            descriptionTooltip = ComponentUtils.wrapTooltips(
                    Streams.concat(VIEW_TOOLTIP.stream(), EDIT_NAME_TOOLTIP.stream())
                            .toList(),
                    150);
        } else {
            descriptionTooltip = ComponentUtils.wrapTooltips(VIEW_TOOLTIP, 150);
        }

        if (overlay instanceof CustomNameProperty customNameOverlay) {
            editInput = new TextInputBoxWidget(x, y, width, height, null, selectionScreen);

            editInput.visible = false;
            String currentName = customNameOverlay.getCustomName().get();

            if (currentName.isEmpty()) {
                editInput.setTextBoxInput(overlay.getTranslatedName());
            } else {
                editInput.setTextBoxInput(currentName);
            }
        }
    }

    @Override
    public void renderWidget(GuiGraphics guiGraphics, int mouseX, int mouseY, float partialTick) {
        PoseStack poseStack = guiGraphics.pose();
        boolean enabled = Managers.Overlay.isEnabled(overlay);

        RenderUtils.drawRect(poseStack, getRectColor(enabled).withAlpha(100), getX(), getY(), 0, width, height);

        RenderUtils.drawRectBorders(
                poseStack, getBorderColor(enabled), getX(), getY(), getX() + width, getY() + height, 1, 2);

        FontRenderer.getInstance()
                .renderScrollingText(
                        poseStack,
                        StyledText.fromString(textToRender),
                        getX() + 2,
                        getY() + (height / 2f),
                        width - 4,
                        CommonColors.WHITE,
                        HorizontalAlignment.LEFT,
                        VerticalAlignment.MIDDLE,
                        TextShadow.NORMAL,
                        1.0f);

        if (editInput != null) {
            editInput.render(guiGraphics, mouseX, mouseY, partialTick);
        }

        // Don't want to display tooltip when the tile is outside the mask from the screen
        if (isHovered
                && (mouseY <= selectionScreen.getConfigMaskTopY()
                        || mouseY >= selectionScreen.getConfigMaskBottomY())) {
            isHovered = false;
        }

        // Display tooltip, if the parent feature is disabled then display that.
        // Otherwise, display the description/save tooltip depending on if info box/custom bar
        // name is being edited
        if (isHovered) {
            if (!overlay.isParentEnabled()) {
                McUtils.screen()
                        .setTooltipForNextRenderPass(Lists.transform(
                                ComponentUtils.wrapTooltips(
                                        List.of(Component.translatable(
                                                "screens.wynntils.overlaySelection.parentDisabled",
                                                overlay.getParentTranslatedName())),
                                        200),
                                Component::getVisualOrderText));
            } else {
                McUtils.screen()
                        .setTooltipForNextRenderPass(Lists.transform(
                                (editInput != null && editInput.visible) ? SAVE_NAME_TOOLTIP : descriptionTooltip,
                                Component::getVisualOrderText));
            }
        }
    }

    @Override
    public boolean mouseClicked(double mouseX, double mouseY, int button) {
        // Prevent interaction when the tile is outside of the mask from the screen, same applies and released
        if ((mouseY <= selectionScreen.getConfigMaskTopY() || mouseY >= selectionScreen.getConfigMaskBottomY())) {
            return false;
        }

        if (editInput != null && editInput.visible && editInput.mouseClicked(mouseX, mouseY, button)) {
            return true;
        }

        if (button == GLFW.GLFW_MOUSE_BUTTON_LEFT) {
            if (isSelected() && editInput != null) {
                editInput.visible = true;
                selectionScreen.setFocusedTextInput(editInput);
            } else {
                selectionScreen.selectOverlay(overlay);
            }
        }

        return super.mouseClicked(mouseX, mouseY, button);
    }

    @Override
    public boolean mouseReleased(double mouseX, double mouseY, int button) {
        // Prevent interaction when the tile is outside of the mask from the screen, same applies and released
        if ((mouseY <= selectionScreen.getConfigMaskTopY() || mouseY >= selectionScreen.getConfigMaskBottomY())) {
            return false;
        }

        if (editInput != null) {
            editInput.mouseReleased(mouseX, mouseY, button);
        }

        return super.mouseReleased(mouseX, mouseY, button);
    }

    @Override
    public void onPress() {}

    @Override
    public boolean keyPressed(int keyCode, int scanCode, int modifiers) {
        if (keyCode == GLFW.GLFW_KEY_ENTER && editInput != null && editInput.visible) {
            editInput.visible = false;

            if (overlay instanceof CustomNameProperty customNameOverlay) {
                customNameOverlay.setCustomName(editInput.getTextBoxInput());

                if (editInput.getTextBoxInput().isEmpty()) {
                    textToRender = overlay.getTranslatedName();
                } else {
                    textToRender = editInput.getTextBoxInput();
                }
            }

            return true;
        }

        return false;
    }

    @Override
    public void setY(int y) {
        super.setY(y);

        if (editInput != null) {
            editInput.setY(y);
        }
    }

    public Overlay getOverlay() {
        return overlay;
    }

    public void hideEditInput() {
        if (editInput != null) {
            editInput.visible = false;
        }
    }

    private CustomColor getRectColor(boolean enabled) {
        if (isSelected()) {
            return CommonColors.GRAY;
        }

        if (!overlay.isParentEnabled()) return DISABLED_FEATURE_COLOR;

        return enabled ? ENABLED_COLOR : DISABLED_COLOR;
    }

    private CustomColor getBorderColor(boolean enabled) {
        if (isSelected()) {
            return isHovered ? CommonColors.LIGHT_GRAY : CommonColors.WHITE;
        }

        if (!overlay.isParentEnabled()) {
            return isHovered ? DISABLED_FEATURE_COLOR_BORDER_HOVERED : DISABLED_FEATURE_COLOR_BORDER;
        }

        return enabled
                ? (isHovered ? ENABLED_COLOR_BORDER_HOVERED : ENABLED_COLOR_BORDER)
                : (isHovered ? DISABLED_COLOR_BORDER_HOVERED : DISABLED_COLOR_BORDER);
    }

    private boolean isSelected() {
        return selectionScreen.getSelectedOverlay() == overlay;
    }
}
