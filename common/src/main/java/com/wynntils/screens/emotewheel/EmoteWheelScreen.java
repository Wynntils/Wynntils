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

public class EmoteWheelScreen extends WynntilsScreen {
    private static final List<Pair<Integer, Integer>> BUTTON_POSITIONS = new ArrayList<>();
    public static final int BUTTON_SIZE = 50;
    public static final int DIST_FROM_CENTER = 110;

    private final int numOfEmotes;
    private final EmoteWheelFeature emoteWheelFeature;
    private final List<String> emotes;
    private final double scale;
    private final float buttonSize;
    private final EmoteWheelButton buttonStyle;
    private final int buttonRadius;
    private final boolean canInteract;

    protected int centerX = 0;
    protected int centerY = 0;
    private int hoveredEmoji = -1;

    EmoteWheelScreen(boolean canInteract) {
        super(Component.literal("Emote Wheel"));

        this.canInteract = canInteract;

        emoteWheelFeature = Managers.Feature.getFeatureInstance(EmoteWheelFeature.class);
        numOfEmotes = MathUtils.clamp(emoteWheelFeature.numberOfButtons.get(), 1, 10);
        emotes = emoteWheelFeature.configureEmotes.get().getFavoritedEmotes();
        scale = emoteWheelFeature.scale.get();
        buttonSize = (float) (BUTTON_SIZE * scale);
        buttonStyle = emoteWheelFeature.buttonStyle.get();
        buttonRadius = emoteWheelFeature.buttonRadius.get();
    }

    public static Screen create() {
        return new EmoteWheelScreen(true);
    }

    @Override
    public void doInit() {
        if (canInteract) {
            KeyMapping keyMapping = emoteWheelFeature.openEmoteWheelKeybind.getKeyMapping();
            KeyMapping.set(keyMapping.key, KeyboardUtils.isKeyDown(keyMapping.key.getValue()));
            rememberKeyHolds();
        }

        getButtonPositions();
    }

    private void getButtonPositions() {
        BUTTON_POSITIONS.clear();
        getCenterOfWheel();
        double buttonAngle = (360.0 / numOfEmotes);
        int distFromCenter = DIST_FROM_CENTER;

        if (buttonStyle == EmoteWheelButton.WHEEL) {
            distFromCenter += buttonRadius;
        }

        for (int i = 0; i < numOfEmotes; i++) {
            // Subtracting 90 degress so it starts at the top
            double angle = Math.toRadians((buttonAngle * i) - 90);
            int x = (int) (centerX + (distFromCenter * scale * Math.cos(angle)));
            int y = (int) (centerY + (distFromCenter * scale * Math.sin(angle)));
            BUTTON_POSITIONS.add(new Pair<>(x, y));
        }
    }

    protected void getCenterOfWheel() {
        centerX = width / 2;
        centerY = height / 2;
    }

    @Override
    public void doRender(GuiGraphics guiGraphics, int mouseX, int mouseY, float partialTick) {
        if (!emoteWheelFeature.openEmoteWheelKeybind.getKeyMapping().isDown() && canInteract) {
            onClose();
            return;
        }

        hoveredEmoji = (getHoveredEmoji(mouseX, mouseY));

        for (int i = 0; i < BUTTON_POSITIONS.size(); i++) {
            Pair<Integer, Integer> centerPos = BUTTON_POSITIONS.get(i);
            float buttonX = centerPos.key() - buttonSize / 2;
            float buttonY = centerPos.value() - buttonSize / 2;
            Texture buttonTexture = getButtonTexture(i, buttonStyle);
            CustomColor color = getButtonColor(i, buttonStyle);

            // Render button background
            if (buttonTexture != null) {
                RenderUtils.drawScalingTexturedRect(
                        guiGraphics, buttonTexture, color, buttonX, buttonY, buttonSize, buttonSize);
            } else {
                if (buttonStyle == EmoteWheelButton.WHEEL) {
                    renderWheelStyle(guiGraphics, color, i);
                } else {
                    RenderUtils.drawRoundedRect(guiGraphics, color, buttonX, buttonY, buttonSize, buttonSize, 0, (int)
                            (buttonRadius * scale));
                }
            }

            CustomColor fontColor = getTextColor(i);
            float buttonX2 = centerPos.key() + buttonSize / 2;
            float buttonY2 = centerPos.value() + buttonSize / 2;
            TextShadow textShadow = emoteWheelFeature.textShadow.get();

            // Render button name
            String emoteName = !doesEmoteExistInWheel(i) ? "" : emotes.get(i);
            float textMargin = 3;
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
        if (!canInteract) {
            return -1;
        }
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
                return hoveredEmoji == index
                        ? Texture.EMOTE_WHEEL_STYLE_BUTTON_HOVERED
                        : Texture.EMOTE_WHEEL_STYLE_BUTTON;
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

    private CustomColor getTextColor(int index) {
        CustomColor textColor = emoteWheelFeature.textColor.get();
        CustomColor textHoverColor = emoteWheelFeature.textColorHovered.get();
        return hoveredEmoji == index ? textHoverColor : textColor;
    }

    private void renderWheelStyle(GuiGraphics guiGraphics, CustomColor color, int buttonNum) {
        float segmentFillPercent = ((float) 1 / numOfEmotes);
        double segmentAngleDegrees = (360.0 / numOfEmotes);
        int innerRadius = (int) ((DIST_FROM_CENTER - 5 - (double) BUTTON_SIZE / 2) * scale);
        int outerRadius = (int) ((DIST_FROM_CENTER + 5 + (double) BUTTON_SIZE / 2) * scale);

        float angleOffset = (float) Math.toRadians((segmentAngleDegrees * buttonNum) - segmentAngleDegrees / 2);
        double buttonAngle = Math.toRadians((segmentAngleDegrees * buttonNum) - 90);
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
        if (!canInteract) {
            return super.keyPressed(event);
        }

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
        if (!canInteract) return;

        if (doesEmoteExistInWheel(emoteNum)) {
            Handlers.Command.sendCommandImmediately("emote " + emotes.get(emoteNum));
        }

        onClose();
    }

    private boolean doesEmoteExistInWheel(int emoteNum) {
        return emotes.size() > emoteNum && emotes.get(emoteNum) != null;
    }

    @Override
    public boolean keyReleased(KeyEvent event) {
        if (!canInteract) {
            return super.keyPressed(event);
        }

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
    protected void renderBlurredBackground(GuiGraphics guiGraphics) {
        if (!canInteract) {
            super.renderBlurredBackground(guiGraphics);
        }
    }

    @Override
    protected void renderMenuBackground(GuiGraphics partialTick) {
        if (!canInteract) {
            super.renderMenuBackground(partialTick);
        }
    }
}
