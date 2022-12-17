/*
 * Copyright © Wynntils 2022.
 * This file is released under AGPLv3. See LICENSE for full license details.
 */
package com.wynntils.gui.widgets;

import com.mojang.blaze3d.vertex.PoseStack;
import com.wynntils.core.managers.Models;
import com.wynntils.gui.render.FontRenderer;
import com.wynntils.gui.render.HorizontalAlignment;
import com.wynntils.gui.render.RenderUtils;
import com.wynntils.gui.render.VerticalAlignment;
import com.wynntils.gui.screens.WynntilsLootrunsScreen;
import com.wynntils.gui.screens.maps.MainMapScreen;
import com.wynntils.mc.objects.CommonColors;
import com.wynntils.mc.objects.CustomColor;
import com.wynntils.mc.utils.McUtils;
import com.wynntils.utils.KeyboardUtils;
import com.wynntils.utils.StringUtils;
import com.wynntils.wynn.model.LootrunModel;
import java.io.File;
import java.util.Objects;
import net.minecraft.Util;
import net.minecraft.client.gui.components.AbstractButton;
import net.minecraft.client.gui.narration.NarrationElementOutput;
import net.minecraft.network.chat.TextComponent;
import net.minecraft.world.phys.Vec3;
import org.lwjgl.glfw.GLFW;

public class LootrunButton extends AbstractButton {
    private static final CustomColor BUTTON_COLOR = new CustomColor(181, 174, 151);
    private static final CustomColor BUTTON_COLOR_HOVERED = new CustomColor(121, 116, 101);
    private static final CustomColor TRACKED_BUTTON_COLOR = new CustomColor(176, 197, 148);
    private static final CustomColor TRACKED_BUTTON_COLOR_HOVERED = new CustomColor(126, 211, 106);

    private final LootrunModel.LootrunInstance lootrun;
    private final WynntilsLootrunsScreen screen;

    public LootrunButton(
            int x, int y, int width, int height, LootrunModel.LootrunInstance lootrun, WynntilsLootrunsScreen screen) {
        super(x, y, width, height, new TextComponent("Lootrun Button"));
        this.lootrun = lootrun;
        this.screen = screen;
    }

    @Override
    public void renderButton(PoseStack poseStack, int mouseX, int mouseY, float partialTick) {
        CustomColor backgroundColor = isLoaded()
                ? (this.isHovered ? TRACKED_BUTTON_COLOR_HOVERED : TRACKED_BUTTON_COLOR)
                : (this.isHovered ? BUTTON_COLOR_HOVERED : BUTTON_COLOR);
        RenderUtils.drawRect(poseStack, backgroundColor, this.x, this.y, 0, this.width, this.height);

        int maxTextWidth = this.width - 21;
        FontRenderer.getInstance()
                .renderText(
                        poseStack,
                        StringUtils.getMaxFittingText(
                                lootrun.name(),
                                maxTextWidth,
                                FontRenderer.getInstance().getFont()),
                        this.x + 14,
                        this.y + 1,
                        0,
                        CommonColors.BLACK,
                        HorizontalAlignment.Left,
                        VerticalAlignment.Top,
                        FontRenderer.TextShadow.NONE);
    }

    @Override
    public boolean mouseClicked(double mouseX, double mouseY, int button) {
        if (button == GLFW.GLFW_MOUSE_BUTTON_LEFT) {
            if (isLoaded()) {
                Models.Lootrun.clearCurrentLootrun();
            } else {
                Models.Lootrun.tryLoadFile(lootrun.name());
            }
            return true;
        }

        if (button == GLFW.GLFW_MOUSE_BUTTON_MIDDLE) {
            Util.getPlatform().openFile(Models.Lootrun.LOOTRUNS);
            return true;
        }

        if (button == GLFW.GLFW_MOUSE_BUTTON_RIGHT) {
            if ((KeyboardUtils.isKeyDown(GLFW.GLFW_KEY_LEFT_SHIFT)
                            || KeyboardUtils.isKeyDown(GLFW.GLFW_KEY_RIGHT_SHIFT))
                    && !isLoaded()) {
                tryDeleteLootrun();
                return true;
            }

            LootrunModel.Path path = lootrun.path();
            Vec3 start = path.points().get(0);

            McUtils.mc().setScreen(MainMapScreen.create((float) start.x, (float) start.z));
            return true;
        }

        return true;
    }

    // Not called
    @Override
    public void onPress() {}

    @Override
    public void updateNarration(NarrationElementOutput narrationElementOutput) {}

    private void tryDeleteLootrun() {
        File file = new File(Models.Lootrun.LOOTRUNS, lootrun.name() + ".json");
        file.delete();
        screen.reloadElements();
    }

    private boolean isLoaded() {
        LootrunModel.LootrunInstance currentLootrun = Models.Lootrun.getCurrentLootrun();
        return currentLootrun != null && Objects.equals(currentLootrun.name(), lootrun.name());
    }

    public LootrunModel.LootrunInstance getLootrun() {
        return lootrun;
    }
}
