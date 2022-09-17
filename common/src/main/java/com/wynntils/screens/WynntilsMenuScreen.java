/*
 * Copyright © Wynntils 2022.
 * This file is released under AGPLv3. See LICENSE for full license details.
 */
package com.wynntils.screens;

import com.mojang.blaze3d.vertex.PoseStack;
import com.wynntils.core.WynntilsMod;
import com.wynntils.core.webapi.WebManager;
import com.wynntils.mc.objects.CommonColors;
import com.wynntils.mc.objects.CustomColor;
import com.wynntils.mc.render.FontRenderer;
import com.wynntils.mc.render.HorizontalAlignment;
import com.wynntils.mc.render.RenderUtils;
import com.wynntils.mc.render.Texture;
import com.wynntils.mc.render.VerticalAlignment;
import com.wynntils.mc.utils.ComponentUtils;
import com.wynntils.mc.utils.McUtils;
import com.wynntils.screens.overlays.OverlaySelectionScreen;
import com.wynntils.screens.settings.WynntilsSettingsScreen;
import com.wynntils.screens.widgets.WynntilsMenuButton;
import com.wynntils.wynn.model.CharacterManager;
import java.util.ArrayList;
import java.util.List;
import java.util.Locale;
import net.minecraft.ChatFormatting;
import net.minecraft.client.gui.screens.Screen;
import net.minecraft.client.gui.screens.inventory.InventoryScreen;
import net.minecraft.client.resources.language.I18n;
import net.minecraft.network.chat.TextComponent;
import net.minecraft.network.chat.TranslatableComponent;
import org.lwjgl.glfw.GLFW;

public class WynntilsMenuScreen extends Screen {
    private static final int BUTTON_SIZE = 30;
    private static final CustomColor BUTTON_COLOR = new CustomColor(181, 174, 151);
    private static final CustomColor BUTTON_COLOR_HOVERED = new CustomColor(121, 116, 101);

    private final List<WynntilsMenuButton> buttons = new ArrayList<>();
    private WynntilsMenuButton hovered = null;

    public WynntilsMenuScreen() {
        super(new TranslatableComponent("screens.wynntils.wynntilsMenu.name"));

        buttons.add(new WynntilsMenuButton(
                Texture.QUEST_BOOK_ICON,
                null,
                List.of(
                        new TextComponent("[>] ")
                                .withStyle(ChatFormatting.GOLD)
                                .append(new TextComponent("Quest Book")
                                        .withStyle(ChatFormatting.BOLD)
                                        .withStyle(ChatFormatting.GOLD)),
                        new TextComponent("See and pin all your currently available quests.")
                                .withStyle(ChatFormatting.GRAY),
                        new TextComponent(""),
                        new TextComponent("Left click to select").withStyle(ChatFormatting.GREEN))));
        buttons.add(new WynntilsMenuButton(
                Texture.SETTINGS_ICON,
                new WynntilsSettingsScreen(),
                List.of(
                        new TextComponent("[>] ")
                                .withStyle(ChatFormatting.GOLD)
                                .append(new TextComponent("Configuration")
                                        .withStyle(ChatFormatting.BOLD)
                                        .withStyle(ChatFormatting.GOLD)),
                        new TextComponent("Change the settings the way you want.").withStyle(ChatFormatting.GRAY),
                        new TextComponent(""),
                        new TextComponent("Left click to select").withStyle(ChatFormatting.GREEN))));
        buttons.add(new WynntilsMenuButton(
                Texture.OVERLAYS_ICON,
                new OverlaySelectionScreen(),
                List.of(
                        new TextComponent("[>] ")
                                .withStyle(ChatFormatting.GOLD)
                                .append(new TextComponent("Overlay Configuration")
                                        .withStyle(ChatFormatting.BOLD)
                                        .withStyle(ChatFormatting.GOLD)),
                        new TextComponent(
                                        "Enable or disable and change the positions and sizes of various Wynntils overlays.")
                                .withStyle(ChatFormatting.GRAY),
                        new TextComponent(""),
                        new TextComponent("Left click to select").withStyle(ChatFormatting.GREEN))));

        assert buttons.size() <= 8;
    }

    @Override
    public void render(PoseStack poseStack, int mouseX, int mouseY, float partialTick) {
        renderBackgroundTexture(poseStack);

        // Make 0, 0 the top left corner of the rendered quest book background
        poseStack.pushPose();
        final float translationX = (this.width - Texture.QUEST_BOOK_BACKGROUND.width()) / 2f;
        final float translationY = (this.height - Texture.QUEST_BOOK_BACKGROUND.height()) / 2f;
        poseStack.translate(translationX, translationY, 1f);

        renderTitle(poseStack);

        renderVersion(poseStack);

        renderButtons(poseStack, mouseX, mouseY);

        renderTooltip(poseStack, mouseX, mouseY, translationX, translationY);

        renderDescription(poseStack);

        renderPlayerInfo(poseStack, mouseX, mouseY, translationX, translationY);

        poseStack.popPose();
    }

    private static void renderPlayerInfo(
            PoseStack poseStack, int mouseX, int mouseY, float translationX, float translationY) {
        int posX = (int) (translationX + Texture.QUEST_BOOK_BACKGROUND.width()) - 85;
        int posY = (int) (translationY + Texture.QUEST_BOOK_BACKGROUND.height() / 2f) + 25;
        InventoryScreen.renderEntityInInventory(posX, posY, 30, posX - mouseX, posY - mouseY, McUtils.player());

        FontRenderer.getInstance()
                .renderAlignedTextInBox(
                        poseStack,
                        ComponentUtils.getUnformatted(McUtils.player().getDisplayName()),
                        Texture.QUEST_BOOK_BACKGROUND.width() / 2f,
                        Texture.QUEST_BOOK_BACKGROUND.width(),
                        60,
                        0,
                        CommonColors.BLACK,
                        HorizontalAlignment.Center,
                        FontRenderer.TextShadow.NONE);
        CharacterManager.CharacterInfo characterInfo = CharacterManager.getCharacterInfo();
        FontRenderer.getInstance()
                .renderAlignedTextInBox(
                        poseStack,
                        characterInfo.getClassType().getName().toUpperCase(Locale.ROOT) + " Level "
                                + characterInfo.getLevel(),
                        Texture.QUEST_BOOK_BACKGROUND.width() / 2f,
                        Texture.QUEST_BOOK_BACKGROUND.width(),
                        145,
                        0,
                        CommonColors.PURPLE,
                        HorizontalAlignment.Center,
                        FontRenderer.TextShadow.NONE);

        String currentSplash = WebManager.getCurrentSplash();
        currentSplash = currentSplash == null ? "" : currentSplash;
        FontRenderer.getInstance()
                .renderAlignedTextInBox(
                        poseStack,
                        currentSplash,
                        Texture.QUEST_BOOK_BACKGROUND.width() / 2f,
                        Texture.QUEST_BOOK_BACKGROUND.width(),
                        Texture.QUEST_BOOK_BACKGROUND.height() - 45,
                        0,
                        CommonColors.MAGENTA,
                        HorizontalAlignment.Center,
                        FontRenderer.TextShadow.NONE);
    }

    private static void renderDescription(PoseStack poseStack) {
        FontRenderer.getInstance()
                .renderAlignedTextInBox(
                        poseStack,
                        I18n.get("screens.wynntils.wynntilsMenu.description"),
                        20,
                        Texture.QUEST_BOOK_BACKGROUND.width() / 2f - 10,
                        140,
                        Texture.QUEST_BOOK_BACKGROUND.width() / 2f - 30,
                        CommonColors.BLACK,
                        HorizontalAlignment.Left,
                        FontRenderer.TextShadow.NONE);
    }

    @Override
    public boolean mouseClicked(double mouseX, double mouseY, int button) {
        if (this.hovered == null) return false;

        if (button == GLFW.GLFW_MOUSE_BUTTON_LEFT) {
            McUtils.mc().setScreen(this.hovered.openedScreen());
        }

        return true;
    }

    private void renderTooltip(PoseStack poseStack, int mouseX, int mouseY, float translationX, float translationY) {
        if (this.hovered != null) {
            poseStack.pushPose();
            poseStack.translate(mouseX - translationX, mouseY - translationY, 1);
            RenderUtils.drawTooltip(
                    poseStack,
                    this.hovered.tooltipList(),
                    FontRenderer.getInstance().getFont(),
                    true);
            poseStack.popPose();
        }
    }

    private void renderButtons(PoseStack poseStack, int mouseX, int mouseY) {
        int buttonCount = buttons.size();

        poseStack.pushPose();
        poseStack.translate(20, 65, 0);

        final float translationX = (this.width - Texture.QUEST_BOOK_BACKGROUND.width()) / 2f + 20;
        final float translationY = (this.height - Texture.QUEST_BOOK_BACKGROUND.height()) / 2f + 65;

        mouseX -= translationX;
        mouseY -= translationY;

        this.hovered = null;

        for (int i = 0; i < buttonCount; i++) {
            final int x = i % 4 * (BUTTON_SIZE + 5);
            final int y = (i / 4) * (BUTTON_SIZE + 5);

            boolean hovered = x <= mouseX && x + BUTTON_SIZE >= mouseX && y <= mouseY && y + BUTTON_SIZE >= mouseY;

            RenderUtils.drawRect(
                    poseStack, hovered ? BUTTON_COLOR_HOVERED : BUTTON_COLOR, x, y, 0, BUTTON_SIZE, BUTTON_SIZE);
            WynntilsMenuButton button = buttons.get(i);
            Texture texture = button.buttonTexture();

            if (hovered) {
                this.hovered = button;
                RenderUtils.drawTexturedRect(
                        poseStack,
                        texture.resource(),
                        x + (BUTTON_SIZE - texture.width()) / 2f,
                        y + (BUTTON_SIZE - texture.height() / 2f) / 2f,
                        1,
                        texture.width(),
                        texture.height() / 2f,
                        0,
                        texture.height() / 2f,
                        texture.width(),
                        texture.height() / 2f,
                        texture.width(),
                        texture.height());
            } else {
                RenderUtils.drawTexturedRect(
                        poseStack,
                        texture.resource(),
                        x + (BUTTON_SIZE - texture.width()) / 2f,
                        y + (BUTTON_SIZE - texture.height() / 2f) / 2f,
                        1,
                        texture.width(),
                        texture.height() / 2f,
                        0,
                        0,
                        texture.width(),
                        texture.height() / 2f,
                        texture.width(),
                        texture.height());
            }
        }

        poseStack.popPose();
    }

    private void renderVersion(PoseStack poseStack) {
        // FIXME: Replace with better scaling support

        poseStack.pushPose();
        String version = "Build " + WynntilsMod.getBuildNumber();
        poseStack.scale(0.7f, 0.7f, 0);
        FontRenderer.getInstance()
                .renderAlignedTextInBox(
                        poseStack,
                        version,
                        0,
                        Texture.QUEST_BOOK_BACKGROUND.width() * 1.3f / 2f
                                + FontRenderer.getInstance().getFont().width(version),
                        Texture.QUEST_BOOK_BACKGROUND.height() * 1.3f - 6f,
                        0,
                        CommonColors.YELLOW,
                        HorizontalAlignment.Center,
                        FontRenderer.TextShadow.NORMAL);
        poseStack.popPose();
    }

    private void renderTitle(PoseStack poseStack) {
        int txWidth = Texture.QUEST_BOOK_TITLE.width();
        int txHeight = Texture.QUEST_BOOK_TITLE.height();
        RenderUtils.drawScalingTexturedRect(
                poseStack, Texture.QUEST_BOOK_TITLE.resource(), 0, 30, 0, txWidth, txHeight, txWidth, txHeight);

        poseStack.pushPose();
        poseStack.scale(2f, 2f, 0f);
        FontRenderer.getInstance()
                .renderText(
                        poseStack,
                        I18n.get("screens.wynntils.wynntilsMenu.userProfile"),
                        5,
                        18,
                        CommonColors.YELLOW,
                        HorizontalAlignment.Left,
                        VerticalAlignment.Top,
                        FontRenderer.TextShadow.NORMAL);
        poseStack.popPose();
    }

    private void renderBackgroundTexture(PoseStack poseStack) {
        int txWidth = Texture.QUEST_BOOK_BACKGROUND.width();
        int txHeight = Texture.QUEST_BOOK_BACKGROUND.height();

        RenderUtils.drawScalingTexturedRect(
                poseStack,
                Texture.QUEST_BOOK_BACKGROUND.resource(),
                (this.width - txWidth) / 2f,
                (this.height - txHeight) / 2f,
                0,
                txWidth,
                txHeight,
                txWidth,
                txHeight);
    }
}
