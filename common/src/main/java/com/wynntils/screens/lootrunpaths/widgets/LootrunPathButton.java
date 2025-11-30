/*
 * Copyright © Wynntils 2022-2025.
 * This file is released under LGPLv3. See LICENSE for full license details.
 */
package com.wynntils.screens.lootrunpaths.widgets;

import com.wynntils.core.components.Services;
import com.wynntils.core.text.StyledText;
import com.wynntils.screens.base.widgets.WynntilsButton;
import com.wynntils.screens.lootrunpaths.WynntilsLootrunPathsScreen;
import com.wynntils.screens.maps.MainMapScreen;
import com.wynntils.services.lootrunpaths.LootrunPathInstance;
import com.wynntils.services.lootrunpaths.type.LootrunPath;
import com.wynntils.utils.colors.CommonColors;
import com.wynntils.utils.colors.CustomColor;
import com.wynntils.utils.mc.KeyboardUtils;
import com.wynntils.utils.mc.McUtils;
import com.wynntils.utils.render.FontRenderer;
import com.wynntils.utils.render.RenderUtils;
import com.wynntils.utils.render.type.HorizontalAlignment;
import com.wynntils.utils.render.type.TextShadow;
import com.wynntils.utils.render.type.VerticalAlignment;
import java.io.File;
import java.util.Objects;
import net.minecraft.client.gui.GuiGraphics;
import net.minecraft.client.input.InputWithModifiers;
import net.minecraft.client.input.MouseButtonEvent;
import net.minecraft.core.Position;
import net.minecraft.network.chat.Component;
import net.minecraft.util.Util;
import org.lwjgl.glfw.GLFW;

public class LootrunPathButton extends WynntilsButton {
    private static final CustomColor BUTTON_COLOR = new CustomColor(181, 174, 151);
    private static final CustomColor BUTTON_COLOR_HOVERED = new CustomColor(121, 116, 101);
    private static final CustomColor TRACKED_BUTTON_COLOR = new CustomColor(176, 197, 148);
    private static final CustomColor TRACKED_BUTTON_COLOR_HOVERED = new CustomColor(126, 211, 106);

    private final LootrunPathInstance lootrun;
    private final WynntilsLootrunPathsScreen screen;

    public LootrunPathButton(
            int x, int y, int width, int height, LootrunPathInstance lootrun, WynntilsLootrunPathsScreen screen) {
        super(x, y, width, height, Component.literal("Lootrun Button"));
        this.lootrun = lootrun;
        this.screen = screen;
    }

    @Override
    public void renderContents(GuiGraphics guiGraphics, int mouseX, int mouseY, float partialTick) {
        CustomColor backgroundColor = getButtonBackgroundColor();
        RenderUtils.drawRect(guiGraphics, backgroundColor, this.getX(), this.getY(), this.width, this.height);

        FontRenderer.getInstance()
                .renderScrollingText(
                        guiGraphics,
                        StyledText.fromString(lootrun.name()),
                        this.getX() + 2,
                        this.getY() + 1,
                        this.width - 3,
                        CommonColors.BLACK,
                        HorizontalAlignment.LEFT,
                        VerticalAlignment.TOP,
                        TextShadow.NONE,
                        1f);
    }

    private CustomColor getButtonBackgroundColor() {
        if (isLoaded()) {
            return isHovered ? TRACKED_BUTTON_COLOR_HOVERED : TRACKED_BUTTON_COLOR;
        } else {
            return isHovered ? BUTTON_COLOR_HOVERED : BUTTON_COLOR;
        }
    }

    @Override
    public boolean mouseClicked(MouseButtonEvent event, boolean isDoubleClick) {
        if (event.button() == GLFW.GLFW_MOUSE_BUTTON_LEFT) {
            if (isLoaded()) {
                Services.LootrunPaths.clearCurrentLootrun();
            } else {
                Services.LootrunPaths.tryLoadLootrun(lootrun.name());
            }
            return true;
        }

        if (event.button() == GLFW.GLFW_MOUSE_BUTTON_MIDDLE) {
            Util.getPlatform().openFile(Services.LootrunPaths.LOOTRUNS);
            return true;
        }

        if (event.button() == GLFW.GLFW_MOUSE_BUTTON_RIGHT) {
            if ((KeyboardUtils.isKeyDown(GLFW.GLFW_KEY_LEFT_SHIFT)
                            || KeyboardUtils.isKeyDown(GLFW.GLFW_KEY_RIGHT_SHIFT))
                    && !isLoaded()) {
                tryDeleteLootrun();
                return true;
            }

            LootrunPath path = lootrun.path();
            Position start = path.points().getFirst();

            McUtils.setScreen(MainMapScreen.create((float) start.x(), (float) start.z()));
            return true;
        }

        return true;
    }

    // Not called
    @Override
    public void onPress(InputWithModifiers input) {}

    private void tryDeleteLootrun() {
        File file = new File(Services.LootrunPaths.LOOTRUNS, lootrun.name() + ".json");
        file.delete();
        screen.reloadElements();
    }

    private boolean isLoaded() {
        LootrunPathInstance currentLootrun = Services.LootrunPaths.getCurrentLootrun();
        return currentLootrun != null && Objects.equals(currentLootrun.name(), lootrun.name());
    }

    public LootrunPathInstance getLootrun() {
        return lootrun;
    }
}
