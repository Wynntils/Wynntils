/*
 * Copyright © Wynntils 2026.
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
import com.wynntils.models.items.items.gui.EmoteItem;
import com.wynntils.utils.MathUtils;
import com.wynntils.utils.colors.CustomColor;
import com.wynntils.utils.mc.KeyboardUtils;
import com.wynntils.utils.render.FontRenderer;
import com.wynntils.utils.render.RenderUtils;
import com.wynntils.utils.render.Texture;
import com.wynntils.utils.render.type.EmoteWheelButton;
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
    private final int numOfEmotes;
    private final double scale;

    private static final int squareSize = 50;
    private static final int distanceFromCenter = 110;
    private int centerX = 0;
    private int centerY = 0;
    private int hoveredEmoji = -1;
    private Pair<Integer, Integer> screenDimensions;
    private List<EmoteItem> emotes;
    private EmoteWheelFeature emoteWheelFeature;

    private EmoteWheelScreen(int numOfEmotes, double scale) {
        super(Component.literal("Emote Wheel"));
        this.numOfEmotes = MathUtils.clamp(numOfEmotes, 1, 10);
        this.scale = scale;
    }

    public static Screen create(int numOfEmotes, double scale) {
        return new EmoteWheelScreen(numOfEmotes, scale);
    }

    @Override
    public void doInit() {
        emoteWheelFeature = Managers.Feature.getFeatureInstance(EmoteWheelFeature.class);
        KeyMapping keyMapping = emoteWheelFeature.openEmoteWheelKeybind.getKeyMapping();
        KeyMapping.set(keyMapping.key, KeyboardUtils.isKeyDown(keyMapping.key.getValue()));
        rememberKeyHolds();

        emotes = emoteWheelFeature.favoritedEmotes.get().stream()
                .map(EmoteItem::fromString)
                .toList();

        screenDimensions = new Pair<>(width, height);
        getButtonPositions();
    }

    private void getButtonPositions() {
        buttonPositions.clear();
        centerX = width / 2;
        centerY = height / 2;
        double segmentAngle = (360.0 / numOfEmotes);
        int distFromCenter = this.distanceFromCenter;

        if (emoteWheelFeature.buttonStyle.get() == EmoteWheelButton.WHEEL) {
            distFromCenter += emoteWheelFeature.buttonRadius.get();
        }

        for (int i = 0; i < numOfEmotes; i++) {
            // Subtracting 90 degress so it starts at the top
            double angle = Math.toRadians((segmentAngle * i) - 90);
            int x = (int) (centerX + (distFromCenter * scale * Math.cos(angle)));
            int y = (int) (centerY + (distFromCenter * scale * Math.sin(angle)));
            buttonPositions.add(new Pair<>(x, y));
        }
    }

    @Override
    public void doRender(GuiGraphics guiGraphics, int mouseX, int mouseY, float partialTick) {
        if (!emoteWheelFeature.openEmoteWheelKeybind.getKeyMapping().isDown()) {
            onClose();
            return;
        }

        if (screenDimensions.key() != width || screenDimensions.value() != height) {
            getButtonPositions();
        }

        hoveredEmoji = (getHoveredEmoji(mouseX, mouseY));
        float buttonSize = (float) (squareSize * scale);
        EmoteWheelButton buttonStyle = emoteWheelFeature.buttonStyle.get();
        CustomColor textColor = emoteWheelFeature.textColor.get();
        CustomColor textHoverColor = emoteWheelFeature.textColorHovered.get();
        int buttonRadius = emoteWheelFeature.buttonRadius.get();
        // Wheel button style calculations
        float segmentFillPercent = ((float) 1 / numOfEmotes);
        double segmentAngleDegrees = (360.0 / numOfEmotes);
        int innerRadius = (int) ((distanceFromCenter - 5 - (double) squareSize / 2) * scale);
        int outerRadius = (int) ((distanceFromCenter + 5 + (double) squareSize / 2) * scale);

        for (int i = 0; i < buttonPositions.size(); i++) {
            // General button calculations
            Pair<Integer, Integer> centerPos = buttonPositions.get(i);
            float buttonX = centerPos.key() - buttonSize / 2;
            float buttonY = centerPos.value() - buttonSize / 2;
            float buttonX2 = centerPos.key() + buttonSize / 2;
            float buttonY2 = centerPos.value() + buttonSize / 2;
            Texture buttonTexture = getButtonTexture(i, buttonStyle);
            CustomColor color = getButtonColor(i, buttonStyle);

            // Render button background
            if (buttonTexture != null) {
                RenderUtils.drawScalingTexturedRect(
                        guiGraphics, buttonTexture, color, buttonX, buttonY, buttonSize, buttonSize);
            } else {
                if (buttonStyle == EmoteWheelButton.WHEEL) {
                    float angleOffset = (float) Math.toRadians((segmentAngleDegrees * i) - segmentAngleDegrees / 2);
                    double buttonAngle = Math.toRadians((segmentAngleDegrees * i) - 90);
                    int xOffset = (int) (buttonRadius * scale * Math.cos(buttonAngle));
                    int yOffset = (int) (buttonRadius * scale * Math.sin(buttonAngle));
                    float maxArcSegments = getMaxArcSegments();

                    RenderUtils.drawArc(
                            guiGraphics,
                            color,
                            centerX - outerRadius + xOffset,
                            centerY - outerRadius + yOffset,
                            segmentFillPercent,
                            innerRadius,
                            outerRadius,
                            angleOffset,
                            maxArcSegments);
                } else {
                    RenderUtils.drawRoundedRect(guiGraphics, color, buttonX, buttonY, buttonSize, buttonSize, 0, (int)
                            (buttonRadius * scale));
                }
            }

            // Render button name
            float textMargin = 3;
            String emoteName = !doesEmoteExistInWheel(i) ? "" : emotes.get(i).getEmoteName();
            CustomColor fontColor = hoveredEmoji == i ? textHoverColor : textColor;
            TextShadow textShadow = emoteWheelFeature.textShadow.get();

            FontRenderer.getInstance()
                    .renderAlignedTextInBox(
                            guiGraphics,
                            StyledText.fromPart(new StyledTextPart(emoteName, Style.EMPTY, null, Style.EMPTY)),
                            buttonX + textMargin,
                            buttonX2 - textMargin,
                            buttonY,
                            buttonY2,
                            buttonSize - textMargin * 2,
                            fontColor,
                            HorizontalAlignment.CENTER,
                            VerticalAlignment.MIDDLE,
                            textShadow,
                            (float) (0.9F * scale));

            // Render button number
            if (emoteWheelFeature.showNumbers.get()) {
                String emoteNumber = i == 9 ? "0" : Integer.toString(i + 1);
                float numberTextScale = (float) (0.8F * scale);
                FontRenderer.getInstance()
                        .renderText(
                                guiGraphics,
                                StyledText.fromPart(new StyledTextPart(emoteNumber, Style.EMPTY, null, Style.EMPTY)),
                                buttonX2 - (font.width(emoteNumber) * numberTextScale) - 1,
                                buttonY2 - (font.lineHeight * numberTextScale) - 1,
                                fontColor,
                                HorizontalAlignment.CENTER,
                                VerticalAlignment.MIDDLE,
                                textShadow,
                                numberTextScale);
            }
        }
    }

    private int getHoveredEmoji(int mouseX, int mouseY) {
        // Subtracting 90 degress so it starts at the top
        double angle = Math.atan2(centerY - mouseY, centerX - mouseX) % (2 * Math.PI) + (Math.PI / 2);
        float position = (float) ((angle + Math.PI) / (Math.PI / ((double) numOfEmotes / 2)));
        return Math.round(position) % numOfEmotes;
    }

    private Texture getButtonTexture(int index, EmoteWheelButton buttonStyle) {
        switch (buttonStyle) {
            case TOOLTIP -> {
                return hoveredEmoji == index
                        ? Texture.EMOTE_WHEEL_STYLE_TOOLTIP_HOVERED
                        : Texture.EMOTE_WHEEL_STYLE_TOOLTIP;
            }
            case BUTTON -> {
                return hoveredEmoji == index ? Texture.EMOTE_WHEEL_STYLE_BUTTON_HOVERED : Texture.EMOTE_WHEEL_STYLE_BUTTON;
            }
            default -> {
                return null;
            }
        }
    }

    private CustomColor getButtonColor(int index, EmoteWheelButton buttonStyle) {
        if (buttonStyle == EmoteWheelButton.BUTTON) {
            return CustomColor.NONE;
        }

        CustomColor buttonColor = emoteWheelFeature.backgroundColor.get();
        CustomColor buttonHoverColor = emoteWheelFeature.backgroundColorHovered.get();
        return hoveredEmoji == index ? buttonHoverColor : buttonColor;
    }

    private float getMaxArcSegments() {
        switch (numOfEmotes) {
            case 3, 7 -> {
                return 21;
            }
            case 6, 9 -> {
                return 18;
            }
            case 8 -> {
                return 16;
            }
            default -> {
                return 20;
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

    private void executeEmote(int emoteNum) {
        if (doesEmoteExistInWheel(emoteNum)) {
            Handlers.Command.sendCommandImmediately(
                    "emote " + emotes.get(emoteNum).getEmoteCommand());

            emoteWheelFeature.setlastEmoteNum(emoteNum);
        }

        onClose();
    }

    private boolean doesEmoteExistInWheel(int emoteNum) {
        return emotes.size() > emoteNum && emotes.get(emoteNum) != null;
    }

    @Override
    public boolean keyReleased(KeyEvent event) {
        if (event.key()
                == emoteWheelFeature.openEmoteWheelKeybind.getKeyMapping().key.getValue()) {
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
}
