/*
 * Copyright © Wynntils 2022-2026.
 * This file is released under LGPLv3. See LICENSE for full license details.
 */
package com.wynntils.screens.emotewheel;

import com.mojang.blaze3d.platform.InputConstants;
import com.wynntils.core.components.Handlers;
import com.wynntils.core.components.Managers;
import com.wynntils.core.consumers.screens.WynntilsScreen;
import com.wynntils.core.text.StyledText;
import com.wynntils.core.text.StyledTextPart;
import com.wynntils.features.ui.EmoteWheelFeature;
import com.wynntils.utils.MathUtils;
import com.wynntils.utils.StringUtils;
import com.wynntils.utils.colors.CommonColors;
import com.wynntils.utils.colors.CustomColor;
import com.wynntils.utils.mc.KeyboardUtils;
import com.wynntils.utils.render.FontRenderer;
import com.wynntils.utils.render.RenderUtils;
import com.wynntils.utils.render.type.HorizontalAlignment;
import com.wynntils.utils.render.type.TextShadow;
import com.wynntils.utils.render.type.VerticalAlignment;
import com.wynntils.utils.type.Pair;
import java.util.ArrayList;
import java.util.List;
import net.minecraft.client.KeyMapping;
import net.minecraft.client.gui.GuiGraphics;
import net.minecraft.client.gui.screens.Screen;
import net.minecraft.client.input.KeyEvent;
import net.minecraft.client.input.MouseButtonEvent;
import net.minecraft.network.chat.Component;
import net.minecraft.network.chat.Style;
import org.lwjgl.glfw.GLFW;

public final class EmoteWheelScreen extends WynntilsScreen {
    private final List<Pair<Integer, Integer>> buttonPositions = new ArrayList<>();
    private final CustomColor backgroundColor;
    private final CustomColor hoverColor;
    private final int buttonRadius;
    private final int numOfEmotes;
    private final double scale;

    private static final int squareSize = 50;
    private static final int distanceFromCenter = 110;
    private int centerX = 0;
    private int centerY = 0;
    private int hoveredEmoji = -1;
    private Pair<Integer, Integer> screenDimensions;
    private List<String> emotes;

    private EmoteWheelScreen(
            CustomColor backgroundColor, CustomColor hoverColor, int buttonRadius, int numOfEmotes, double scale) {
        super(Component.literal("Emote Wheel"));
        this.backgroundColor = backgroundColor;
        this.hoverColor = hoverColor;
        this.buttonRadius = buttonRadius;
        this.numOfEmotes = MathUtils.clamp(numOfEmotes, 1, 10);
        this.scale = scale;
    }

    public static Screen create(
            CustomColor backgroundColor, CustomColor hoverColor, int buttonRadius, int numOfEmotes, double scale) {
        return new EmoteWheelScreen(backgroundColor, hoverColor, buttonRadius, numOfEmotes, scale);
    }

    @Override
    public void doInit() {
        EmoteWheelFeature emoteWheelFeature = Managers.Feature.getFeatureInstance(EmoteWheelFeature.class);
        KeyMapping keyMapping = emoteWheelFeature.openEmoteWheelKeybind.getKeyMapping();
        KeyMapping.set(keyMapping.key, KeyboardUtils.isKeyDown(keyMapping.key.getValue()));
        rememberKeyHolds();

        emotes = emoteWheelFeature.favoritedEmotes.get();

        screenDimensions = new Pair<>(width, height);
        calculateButtonPositions();
    }

    @Override
    public void doRender(GuiGraphics guiGraphics, int mouseX, int mouseY, float partialTick) {
        EmoteWheelFeature emoteWheelFeature = Managers.Feature.getFeatureInstance(EmoteWheelFeature.class);
        if (!emoteWheelFeature.openEmoteWheelKeybind.getKeyMapping().isDown()) {
            onClose();
            return;
        }

        if (screenDimensions.key() != width || screenDimensions.value() != height) {
            calculateButtonPositions();
        }

        hoveredEmoji = (getHoveredEmoji(mouseX, mouseY));
        double buttonSize = squareSize * scale;

        for (int i = 0; i < buttonPositions.size(); i++) {
            Pair<Integer, Integer> centerPos = buttonPositions.get(i);
            float buttonX = (float) (centerPos.key() - buttonSize / 2);
            float buttonY = (float) (centerPos.value() - buttonSize / 2);
            String emoteName = !doesEmoteExistInWheel(i) ? "" : StringUtils.capitalizeFirst(emotes.get(i));
            String emoteNumber = i == 9 ? "0" : Integer.toString(i + 1);

            RenderUtils.drawRoundedRect(
                    guiGraphics,
                    hoveredEmoji == i ? hoverColor : backgroundColor,
                    buttonX,
                    buttonY,
                    (float) buttonSize,
                    (float) buttonSize,
                    0,
                    (int) (buttonRadius * scale));

            FontRenderer.getInstance()
                    .renderText(
                            guiGraphics,
                            StyledText.fromPart(new StyledTextPart(emoteName, Style.EMPTY, null, Style.EMPTY)),
                            centerPos.key(),
                            centerPos.value(),
                            CommonColors.WHITE,
                            HorizontalAlignment.CENTER,
                            VerticalAlignment.MIDDLE,
                            TextShadow.NORMAL,
                            (float) (0.9F * scale));

            if (emoteWheelFeature.showNumbers.get()) {
                float buttonX2 = (float) (centerPos.key() + buttonSize / 2);
                float buttonY2 = (float) (centerPos.value() + buttonSize / 2);
                float numberTextScale = (float) (0.8F * scale);
                FontRenderer.getInstance()
                        .renderText(
                                guiGraphics,
                                StyledText.fromPart(new StyledTextPart(emoteNumber, Style.EMPTY, null, Style.EMPTY)),
                                buttonX2 - (font.width(emoteNumber) * numberTextScale),
                                buttonY2 - (font.lineHeight * numberTextScale),
                                CommonColors.WHITE,
                                HorizontalAlignment.CENTER,
                                VerticalAlignment.MIDDLE,
                                TextShadow.NORMAL,
                                numberTextScale);
            }
        }
    }

    @Override
    public boolean doMouseClicked(MouseButtonEvent event, boolean isDoubleClick) {
        executeEmote(hoveredEmoji);

        return super.doMouseClicked(event, isDoubleClick);
    }

    @Override
    public boolean keyPressed(KeyEvent event) {
        int emoteNum = -1;

        if (event.key() >= GLFW.GLFW_KEY_1 && event.key() <= GLFW.GLFW_KEY_9) {
            emoteNum = event.key() - GLFW.GLFW_KEY_1;
        } else if (event.key() == GLFW.GLFW_KEY_0) {
            emoteNum = 9;
        }

        if (emoteNum != -1 && emoteNum < numOfEmotes) {
            executeEmote(emoteNum);
        }

        // Pass along key press to move
        InputConstants.Key key = InputConstants.getKey(event);
        KeyMapping.set(key, true);

        return false;
    }

    @Override
    public boolean keyReleased(KeyEvent event) {
        if (event.key()
                == Managers.Feature.getFeatureInstance(EmoteWheelFeature.class)
                        .openEmoteWheelKeybind
                        .getKeyMapping()
                        .key
                        .getValue()) {
            onClose();
        }

        // Pass along key press to move
        InputConstants.Key key = InputConstants.getKey(event);
        KeyMapping.set(key, false);

        return false;
    }

    @Override
    protected void renderBlurredBackground(GuiGraphics guiGraphics) {}

    @Override
    protected void renderMenuBackground(GuiGraphics guiGraphics) {}

    private int getHoveredEmoji(int mouseX, int mouseY) {
        // Subtracting 90 degress so it starts at the top
        double angle = Math.atan2(centerY - mouseY, centerX - mouseX) % (2 * Math.PI) + (Math.PI / 2);
        float position = (float) ((angle + Math.PI) / (Math.PI / ((double) numOfEmotes / 2)));
        return Math.round(position) % numOfEmotes;
    }

    private void executeEmote(int emoteNum) {
        if (doesEmoteExistInWheel(emoteNum)) {
            Handlers.Command.sendCommandImmediately("emote " + emotes.get(emoteNum));
        }

        onClose();
    }

    private boolean doesEmoteExistInWheel(int emoteNum) {
        return emotes.size() > emoteNum && emotes.get(emoteNum) != null;
    }

    private void calculateButtonPositions() {
        buttonPositions.clear();
        centerX = width / 2;
        centerY = height / 2;
        double segmentAngle = (360.0 / numOfEmotes);

        for (int i = 0; i < numOfEmotes; i++) {
            // Subtracting 90 degress so it starts at the top
            double angle = Math.toRadians((segmentAngle * i) - 90);
            int x = (int) (centerX + (distanceFromCenter * scale * Math.cos(angle)));
            int y = (int) (centerY + (distanceFromCenter * scale * Math.sin(angle)));
            buttonPositions.add(new Pair<>(x, y));
        }
    }
}
