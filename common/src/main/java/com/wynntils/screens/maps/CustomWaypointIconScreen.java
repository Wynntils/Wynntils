/*
 * Copyright © Wynntils 2024-2025.
 * This file is released under LGPLv3. See LICENSE for full license details.
 */
package com.wynntils.screens.maps;

import com.mojang.blaze3d.vertex.PoseStack;
import com.wynntils.core.WynntilsMod;
import com.wynntils.core.components.Services;
import com.wynntils.core.text.StyledText;
import com.wynntils.screens.base.AbstractSideListScreen;
import com.wynntils.screens.base.widgets.TextInputBoxWidget;
import com.wynntils.screens.maps.widgets.CustomIconWidget;
import com.wynntils.services.mapdata.impl.MapIconImpl;
import com.wynntils.utils.colors.CommonColors;
import com.wynntils.utils.colors.CustomColor;
import com.wynntils.utils.mc.McUtils;
import com.wynntils.utils.render.FontRenderer;
import com.wynntils.utils.render.RenderUtils;
import com.wynntils.utils.render.type.HorizontalAlignment;
import com.wynntils.utils.render.type.TextShadow;
import com.wynntils.utils.render.type.VerticalAlignment;
import java.io.IOException;
import java.util.ArrayList;
import java.util.Base64;
import java.util.regex.Pattern;
import net.minecraft.client.gui.GuiGraphics;
import net.minecraft.client.gui.components.Button;
import net.minecraft.client.gui.components.Renderable;
import net.minecraft.client.gui.components.Tooltip;
import net.minecraft.client.gui.screens.Screen;
import net.minecraft.client.resources.language.I18n;
import net.minecraft.network.chat.Component;

public class CustomWaypointIconScreen extends AbstractSideListScreen {
    // Accepted resource location name pattern `^[a-z0-9/._-]+`
    // This is made stricter to avoid weird names
    private static final Pattern ICON_NAME_PATTERN = Pattern.compile("^[a-z0-9-]+$");

    private final WaypointCreationScreen creationScreen;

    private Button saveIconButton;
    private CustomColor iconNameTextColor = CommonColors.RED;
    private CustomColor iconBase64TextColor = CommonColors.RED;
    private MapIconImpl newIcon;
    private TextInputBoxWidget iconNameInput;
    private TextInputBoxWidget iconBase64Input;

    private CustomWaypointIconScreen(WaypointCreationScreen creationScreen) {
        super(Component.literal("Custom Waypoint Icon Screen"));

        this.creationScreen = creationScreen;
    }

    public static Screen create(WaypointCreationScreen creationScreen) {
        return new CustomWaypointIconScreen(creationScreen);
    }

    @Override
    protected void doInit() {
        super.doInit();

        iconNameInput = new TextInputBoxWidget(
                (int) (dividedWidth * 43),
                (int) (dividedHeight * 18),
                (int) (dividedWidth * 20),
                BUTTON_SIZE,
                (s) -> tryParseIcon(),
                this,
                iconNameInput);
        iconNameInput.setTooltip(
                Tooltip.create(Component.translatable("screens.wynntils.customWaypointIcon.nameTooltip")));
        this.addRenderableWidget(iconNameInput);

        iconBase64Input = new TextInputBoxWidget(
                (int) (dividedWidth * 43),
                (int) (dividedHeight * 26),
                (int) (dividedWidth * 20),
                BUTTON_SIZE,
                (s) -> tryParseIcon(),
                this,
                iconBase64Input);
        iconBase64Input.setTooltip(
                Tooltip.create(Component.translatable("screens.wynntils.customWaypointIcon.base64Tooltip")));
        this.addRenderableWidget(iconBase64Input);

        saveIconButton = new Button.Builder(
                        Component.translatable("screens.wynntils.customWaypointIcon.saveIcon"), (button) -> saveIcon())
                .pos((int) (dividedWidth * 35), (int) (dividedHeight * 31))
                .size((int) (dividedWidth * 12), BUTTON_SIZE)
                .build();
        saveIconButton.active = newIcon != null;
        this.addRenderableWidget(saveIconButton);

        this.addRenderableWidget(new Button.Builder(
                        Component.translatable("screens.wynntils.customWaypointIcon.back"), (button) -> onClose())
                .pos((int) (dividedWidth * 50), (int) (dividedHeight * 31))
                .size((int) (dividedWidth * 12), BUTTON_SIZE)
                .build());

        populateIcons();
    }

    @Override
    public void doRender(GuiGraphics guiGraphics, int mouseX, int mouseY, float partialTick) {
        super.doRender(guiGraphics, mouseX, mouseY, partialTick);

        PoseStack poseStack = guiGraphics.pose();

        RenderUtils.drawRect(
                poseStack,
                CommonColors.BLACK.withAlpha(100),
                dividedWidth * 33,
                dividedHeight * 16,
                0,
                dividedWidth * 31,
                dividedHeight * 20);

        for (Renderable renderable : this.renderables) {
            renderable.render(guiGraphics, mouseX, mouseY, partialTick);
        }

        FontRenderer.getInstance()
                .renderAlignedTextInBox(
                        poseStack,
                        StyledText.fromComponent(Component.translatable("screens.wynntils.customWaypointIcon.help")),
                        dividedWidth * 34,
                        dividedWidth * 62,
                        dividedHeight * 10,
                        dividedHeight * 14,
                        dividedWidth * 28,
                        CommonColors.WHITE,
                        HorizontalAlignment.LEFT,
                        VerticalAlignment.MIDDLE,
                        TextShadow.NORMAL);

        FontRenderer.getInstance()
                .renderText(
                        poseStack,
                        StyledText.fromString(I18n.get("screens.wynntils.customWaypointIcon.iconName") + ":"),
                        dividedWidth * 34,
                        dividedHeight * 20,
                        iconNameTextColor,
                        HorizontalAlignment.LEFT,
                        VerticalAlignment.MIDDLE,
                        TextShadow.NORMAL);

        FontRenderer.getInstance()
                .renderText(
                        poseStack,
                        StyledText.fromString("base64:"),
                        dividedWidth * 34,
                        dividedHeight * 28,
                        iconBase64TextColor,
                        HorizontalAlignment.LEFT,
                        VerticalAlignment.MIDDLE,
                        TextShadow.NORMAL);

        renderPreview(poseStack);
    }

    @Override
    public void onClose() {
        McUtils.mc().setScreen(creationScreen);
    }

    public void selectIcon(MapIconImpl icon) {
        creationScreen.setSelectedIcon(icon);
        McUtils.mc().setScreen(creationScreen);
    }

    public void removeIcon(MapIconImpl iconToRemove) {
        Services.Waypoints.removeCustomIcon(iconToRemove);

        scrollOffset = 0;
        populateIcons();
    }

    protected StyledText getEmptyListText() {
        return StyledText.fromComponent(Component.translatable("screens.wynntils.customWaypointIcon.noIcons"));
    }

    private void tryParseIcon() {
        if (iconNameInput.getTextBoxInput().isEmpty()
                || iconBase64Input.getTextBoxInput().isEmpty()) {
            newIcon = null;
            saveIconButton.active = false;
            iconNameTextColor = iconNameInput.getTextBoxInput().isEmpty() ? CommonColors.RED : CommonColors.WHITE;
            iconBase64TextColor = iconBase64Input.getTextBoxInput().isEmpty() ? CommonColors.RED : CommonColors.WHITE;
            return;
        } else if (!ICON_NAME_PATTERN.matcher(iconNameInput.getTextBoxInput()).matches()) {
            newIcon = null;
            saveIconButton.active = false;
            iconNameTextColor = CommonColors.RED;
            return;
        }

        iconNameTextColor = CommonColors.WHITE;

        try {
            byte[] texture = Base64.getDecoder().decode(iconBase64Input.getTextBoxInput());
            newIcon = new MapIconImpl("wynntils:icon:personal:" + iconNameInput.getTextBoxInput(), texture);
            saveIconButton.active = true;
            iconBase64TextColor = CommonColors.WHITE;
        } catch (IOException | IllegalArgumentException e) {
            WynntilsMod.warn("Bad icon texture for " + iconNameInput.getTextBoxInput(), e);
            newIcon = null;
            saveIconButton.active = false;
            iconBase64TextColor = CommonColors.RED;
        }
    }

    private void saveIcon() {
        Services.Waypoints.addCustomIcon(newIcon);
        newIcon = null;
        saveIconButton.active = false;
        iconNameInput.resetTextBoxInput();
        iconBase64Input.resetTextBoxInput();

        populateIcons();
    }

    private void renderPreview(PoseStack poseStack) {
        if (newIcon == null) return;

        FontRenderer.getInstance()
                .renderText(
                        poseStack,
                        StyledText.fromComponent(Component.translatable("screens.wynntils.customWaypointIcon.preview")),
                        dividedWidth * 34,
                        dividedHeight * 38,
                        CommonColors.WHITE,
                        HorizontalAlignment.LEFT,
                        VerticalAlignment.MIDDLE,
                        TextShadow.NORMAL);

        RenderUtils.drawScalingTexturedRect(
                poseStack,
                newIcon.getResourceLocation(),
                dividedWidth * 34,
                dividedHeight * 40,
                1,
                newIcon.getWidth(),
                newIcon.getHeight(),
                newIcon.getWidth(),
                newIcon.getHeight());
    }

    private void populateIcons() {
        sideListWidgets = new ArrayList<>();

        int renderY = 0;

        for (MapIconImpl icon : Services.Waypoints.getCustomIcons()) {
            CustomIconWidget iconWidget =
                    new CustomIconWidget(renderY, (int) (dividedWidth * 32), (int) (dividedHeight * 4), icon, this);
            sideListWidgets.add(iconWidget);

            renderY += (int) (dividedHeight * 4);
        }

        scroll(scrollOffset);
    }
}
