/*
 * Copyright © Wynntils 2026.
 * This file is released under LGPLv3. See LICENSE for full license details.
 */
package com.wynntils.screens.hadesinteraction;

import com.mojang.blaze3d.platform.InputConstants;
import com.wynntils.core.components.Models;
import com.wynntils.core.components.Services;
import com.wynntils.core.consumers.screens.WynntilsScreen;
import com.wynntils.core.text.StyledText;
import com.wynntils.core.text.fonts.CommonFonts;
import com.wynntils.core.text.fonts.WynnFont;
import com.wynntils.core.text.fonts.wynnfonts.WynncraftKeybindsFont;
import com.wynntils.features.players.HadesFeature;
import com.wynntils.features.players.PlayerPingFeature;
import com.wynntils.hades.protocol.enums.PlayerPingType;
import com.wynntils.utils.EnumUtils;
import com.wynntils.utils.colors.CommonColors;
import com.wynntils.utils.colors.CustomColor;
import com.wynntils.utils.mc.McUtils;
import com.wynntils.utils.render.FontRenderer;
import com.wynntils.utils.render.RenderUtils;
import com.wynntils.utils.render.Texture;
import com.wynntils.utils.render.type.HorizontalAlignment;
import com.wynntils.utils.render.type.TextShadow;
import com.wynntils.utils.render.type.VerticalAlignment;
import com.wynntils.utils.render.type.WheelButtonStyle;
import com.wynntils.utils.type.Pair;
import com.wynntils.utils.wynn.RaycastUtils;
import java.util.ArrayList;
import java.util.List;
import net.minecraft.client.KeyMapping;
import net.minecraft.client.gui.GuiGraphics;
import net.minecraft.client.gui.screens.Screen;
import net.minecraft.client.input.KeyEvent;
import net.minecraft.client.input.MouseButtonEvent;
import net.minecraft.client.multiplayer.PlayerInfo;
import net.minecraft.client.resources.DefaultPlayerSkin;
import net.minecraft.core.Vec3i;
import net.minecraft.network.chat.Component;
import net.minecraft.network.chat.MutableComponent;
import net.minecraft.network.chat.Style;
import net.minecraft.resources.Identifier;

public class HadesInteractionWheelScreen extends WynntilsScreen {
    private static final int BUTTON_SIZE = 50;
    private static final int DIST_FROM_CENTER = 110;
    private static final int DEAD_ZONE = 20;
    private static final int PARTY_MEMBER_WIDGET_WIDTH = 110;
    private static final int PARTY_MEMBER_WIDGET_HEIGHT = 20;
    private static final int PARTY_MEMBER_WIDGET_SPACING = 2;
    private static final int PARTY_MEMBER_WIDGET_MARGIN = 8;
    private static final int PARTY_MEMBER_SELECTOR_INDICATOR_WIDTH = 12;

    private final HadesFeature hadesFeature;
    private final PlayerPingFeature playerPingFeature;
    private final List<WheelOption> options = new ArrayList<>();
    private final List<Pair<Integer, Integer>> buttonPositions = new ArrayList<>();
    private final List<String> pingTargets = new ArrayList<>();

    private int centerX;
    private int centerY;
    private int hoveredOption = -1;
    private int selectedPingTarget;

    private String pingTarget = "";

    private HadesInteractionWheelScreen(HadesFeature hadesFeature, PlayerPingFeature playerPingFeature) {
        super(Component.literal("Hades Interaction Wheel"));
        this.hadesFeature = hadesFeature;
        this.playerPingFeature = playerPingFeature;
    }

    public static Screen create(HadesFeature hadesFeature, PlayerPingFeature playerPingFeature) {
        return new HadesInteractionWheelScreen(hadesFeature, playerPingFeature);
    }

    @Override
    public void doInit() {
        rememberKeyHolds();
        createOptions();
        getButtonPositions();
        createPingTargets();
    }

    @Override
    public void doRender(GuiGraphics guiGraphics, int mouseX, int mouseY, float partialTick) {
        MutableComponent selectMessage = Component.empty()
                .append(WynnFont.asFont("left_click", WynncraftKeybindsFont.class)
                        .withStyle(Style::withoutShadow))
                .append(Component.translatable("screens.wynntils.hadesInteractionWheel.toSelectAnOption")
                        .withStyle(Style.EMPTY.withFont(CommonFonts.WYNNTILS_LANGUAGE_WYNNCRAFT_FONT)));

        FontRenderer.getInstance()
                .renderText(
                        guiGraphics,
                        StyledText.fromComponent(selectMessage),
                        this.width / 2f,
                        this.height / 2f - 8,
                        CommonColors.WHITE,
                        HorizontalAlignment.CENTER,
                        VerticalAlignment.MIDDLE,
                        TextShadow.NORMAL,
                        1.5f);

        MutableComponent closeMessage = Component.empty()
                .append(WynnFont.asFont("right_click", WynncraftKeybindsFont.class)
                        .withStyle(Style::withoutShadow))
                .append(Component.translatable("screens.wynntils.hadesInteractionWheel.toClose")
                        .withStyle(Style.EMPTY.withFont(CommonFonts.WYNNTILS_LANGUAGE_WYNNCRAFT_FONT)));

        FontRenderer.getInstance()
                .renderText(
                        guiGraphics,
                        StyledText.fromComponent(closeMessage),
                        this.width / 2f,
                        this.height / 2f + 8,
                        CommonColors.WHITE,
                        HorizontalAlignment.CENTER,
                        VerticalAlignment.MIDDLE,
                        TextShadow.NORMAL,
                        1.5f);

        hoveredOption = getHoveredOption(mouseX, mouseY);
        for (int i = 0; i < options.size(); i++) {
            WheelOption option = options.get(i);
            Pair<Integer, Integer> centerPos = buttonPositions.get(i);
            float buttonX = centerPos.key() - BUTTON_SIZE / 2f;
            float buttonY = centerPos.value() - BUTTON_SIZE / 2f;
            WheelButtonStyle buttonStyle = playerPingFeature.buttonStyle.get();
            Texture buttonTexture = getButtonTexture(i, buttonStyle);
            CustomColor buttonColor = getButtonColor(i, buttonStyle);

            if (buttonTexture != null) {
                RenderUtils.drawScalingTexturedRect(
                        guiGraphics, buttonTexture, buttonColor, buttonX, buttonY, BUTTON_SIZE, BUTTON_SIZE);
            } else if (buttonStyle == WheelButtonStyle.WHEEL) {
                renderWheelStyle(guiGraphics, buttonColor, i);
            } else {
                RenderUtils.drawRoundedRect(guiGraphics, buttonColor, buttonX, buttonY, BUTTON_SIZE, BUTTON_SIZE, 0, 0);
            }
            FontRenderer.getInstance()
                    .renderAlignedTextInBox(
                            guiGraphics,
                            StyledText.fromComponent(Component.literal(option.label())
                                    .withStyle(Style.EMPTY.withFont(CommonFonts.WYNNTILS_LANGUAGE_WYNNCRAFT_FONT))),
                            buttonX + 4,
                            buttonX + BUTTON_SIZE - 4,
                            buttonY + 4,
                            buttonY + BUTTON_SIZE - 4,
                            BUTTON_SIZE - 8,
                            getTextColor(i),
                            HorizontalAlignment.CENTER,
                            VerticalAlignment.MIDDLE,
                            playerPingFeature.textShadow.get(),
                            0.9f);
        }

        renderPingTargetSelector(guiGraphics);
    }

    @Override
    protected void renderBlurredBackground(GuiGraphics guiGraphics) {}

    @Override
    protected void renderMenuBackground(GuiGraphics partialTick) {}

    @Override
    public boolean doMouseClicked(MouseButtonEvent event, boolean isDoubleClick) {
        if (event.button() == InputConstants.MOUSE_BUTTON_RIGHT) {
            onClose();
            return true;
        }

        executeOption(hoveredOption);
        return super.doMouseClicked(event, isDoubleClick);
    }

    @Override
    public boolean mouseScrolled(double mouseX, double mouseY, double scrollX, double scrollY) {
        if (scrollY == 0 || pingTargets.isEmpty()) return true;

        selectedPingTarget = Math.floorMod(selectedPingTarget - (int) Math.signum(scrollY), pingTargets.size());
        pingTarget = selectedPingTarget == 0 ? "" : pingTargets.get(selectedPingTarget);
        return true;
    }

    @Override
    public boolean keyPressed(KeyEvent event) {
        InputConstants.Key key = InputConstants.getKey(event);
        KeyMapping.set(key, true);

        if (event.key() == InputConstants.KEY_ESCAPE) {
            onClose();
            return true;
        }

        return false;
    }

    @Override
    public boolean keyReleased(KeyEvent event) {
        if (event.key()
                == hadesFeature.openInteractionWheelKeybind.getKeyMapping().key.getValue()) {
            executeOption(hoveredOption);
        }

        InputConstants.Key key = InputConstants.getKey(event);
        KeyMapping.set(key, false);
        return false;
    }

    private void createOptions() {
        options.clear();
        if (RaycastUtils.getHoveredPlayer().isPresent()) {
            options.add(new WheelOption("View Player", null));
        }

        for (PlayerPingType pingType : PlayerPingType.values()) {
            options.add(new WheelOption(EnumUtils.toNiceString(pingType), pingType));
        }
    }

    private void createPingTargets() {
        pingTargets.clear();
        pingTargets.add("All");
        for (String partyMember : Models.Party.getPartyMembers()) {
            if (partyMember.equals(McUtils.playerName())) continue;

            pingTargets.add(partyMember);
        }

        selectedPingTarget = 0;
        pingTarget = "";
    }

    private void renderPingTargetSelector(GuiGraphics guiGraphics) {
        int widgetX = Math.min(
                centerX + DIST_FROM_CENTER + BUTTON_SIZE / 2 + PARTY_MEMBER_WIDGET_MARGIN,
                width - PARTY_MEMBER_WIDGET_WIDTH - PARTY_MEMBER_SELECTOR_INDICATOR_WIDTH - PARTY_MEMBER_WIDGET_MARGIN);
        int totalHeight = pingTargets.size() * PARTY_MEMBER_WIDGET_HEIGHT
                + (pingTargets.size() - 1) * PARTY_MEMBER_WIDGET_SPACING;
        int widgetY = Math.max(PARTY_MEMBER_WIDGET_MARGIN + 14, (height - totalHeight) / 2);

        FontRenderer.getInstance()
                .renderText(
                        guiGraphics,
                        StyledText.fromComponent(
                                Component.translatable("screens.wynntils.hadesInteractionWheel.scrollToSelect")
                                        .withStyle(Style.EMPTY.withFont(CommonFonts.WYNNTILS_LANGUAGE_WYNNCRAFT_FONT))),
                        widgetX + PARTY_MEMBER_WIDGET_WIDTH / 2f,
                        widgetY - 5,
                        CommonColors.WHITE,
                        HorizontalAlignment.CENTER,
                        VerticalAlignment.BOTTOM,
                        TextShadow.NORMAL,
                        1f);

        for (int i = 0; i < pingTargets.size(); i++) {
            int y = widgetY + i * (PARTY_MEMBER_WIDGET_HEIGHT + PARTY_MEMBER_WIDGET_SPACING);
            boolean selected = i == selectedPingTarget;
            RenderUtils.drawNineSliceScalingTexturedRect(
                    guiGraphics,
                    selected ? Texture.GUIDE_WIDGET_BACKGROUND_HOVERED : Texture.GUIDE_WIDGET_BACKGROUND,
                    widgetX,
                    y,
                    PARTY_MEMBER_WIDGET_WIDTH,
                    PARTY_MEMBER_WIDGET_HEIGHT);

            String target = pingTargets.get(i);
            int textX = widgetX + 5;
            if (i != 0) {
                renderPlayerHead(guiGraphics, target, widgetX + 3, y + 2);
                textX += 18;
            }
            FontRenderer.getInstance()
                    .renderAlignedTextInBox(
                            guiGraphics,
                            StyledText.fromComponent(Component.literal(target)
                                    .withStyle(Style.EMPTY.withFont(CommonFonts.WYNNTILS_LANGUAGE_WYNNCRAFT_FONT))),
                            textX,
                            widgetX + PARTY_MEMBER_WIDGET_WIDTH - 4,
                            y,
                            y + PARTY_MEMBER_WIDGET_HEIGHT,
                            PARTY_MEMBER_WIDGET_WIDTH - (textX - widgetX) - 4,
                            CommonColors.WHITE,
                            HorizontalAlignment.LEFT,
                            VerticalAlignment.MIDDLE,
                            TextShadow.NORMAL,
                            0.8f);

            if (selected) {
                FontRenderer.getInstance()
                        .renderText(
                                guiGraphics,
                                StyledText.fromComponent(WynnFont.asFont("middle_click", WynncraftKeybindsFont.class)
                                        .withStyle(Style::withoutShadow)),
                                widgetX + PARTY_MEMBER_WIDGET_WIDTH + 4,
                                y + PARTY_MEMBER_WIDGET_HEIGHT / 2f,
                                CommonColors.WHITE,
                                HorizontalAlignment.LEFT,
                                VerticalAlignment.MIDDLE,
                                TextShadow.NONE,
                                1f);
            }
        }
    }

    private void renderPlayerHead(GuiGraphics guiGraphics, String playerName, int x, int y) {
        PlayerInfo playerInfo = McUtils.mc().getConnection() == null
                ? null
                : McUtils.mc().getConnection().getPlayerInfo(playerName);
        Identifier skin = playerInfo == null
                ? DefaultPlayerSkin.getDefaultTexture()
                : playerInfo.getSkin().body().texturePath();

        RenderUtils.drawTexturedRect(guiGraphics, skin, x, y, 16, 16, 8, 8, 8, 8, 64, 64);
        RenderUtils.drawTexturedRect(guiGraphics, skin, x, y, 16, 16, 40, 8, 8, 8, 64, 64);
    }

    private Texture getButtonTexture(int index, WheelButtonStyle buttonStyle) {
        switch (buttonStyle) {
            case TOOLTIP -> {
                return hoveredOption == index
                        ? Texture.EMOTE_WHEEL_STYLE_TOOLTIP_HOVERED
                        : Texture.EMOTE_WHEEL_STYLE_TOOLTIP;
            }
            case BUTTON -> {
                return hoveredOption == index
                        ? Texture.EMOTE_WHEEL_STYLE_BUTTON_HOVERED
                        : Texture.EMOTE_WHEEL_STYLE_BUTTON;
            }
            default -> {
                return null;
            }
        }
    }

    private CustomColor getButtonColor(int index, WheelButtonStyle buttonStyle) {
        if (buttonStyle == WheelButtonStyle.BUTTON) {
            return CustomColor.NONE;
        }

        return hoveredOption == index
                ? playerPingFeature.backgroundColorHovered.get()
                : playerPingFeature.backgroundColor.get();
    }

    private CustomColor getTextColor(int index) {
        return hoveredOption == index ? playerPingFeature.textColorHovered.get() : playerPingFeature.textColor.get();
    }

    private void renderWheelStyle(GuiGraphics guiGraphics, CustomColor color, int buttonNum) {
        float segmentFillPercent = (float) 1 / options.size();
        double segmentAngleDegrees = 360.0 / options.size();
        int innerRadius = DIST_FROM_CENTER - 5 - BUTTON_SIZE / 2;
        int outerRadius = DIST_FROM_CENTER + 5 + BUTTON_SIZE / 2;
        float angleOffset = (float) Math.toRadians((segmentAngleDegrees * buttonNum) - segmentAngleDegrees / 2);

        RenderUtils.drawArc(
                guiGraphics,
                color,
                centerX - outerRadius,
                centerY - outerRadius,
                segmentFillPercent,
                innerRadius,
                outerRadius,
                angleOffset,
                getMaxArcSegments());
    }

    private float getMaxArcSegments() {
        return switch (options.size()) {
            case 7 -> 21;
            case 6 -> 18;
            default -> 20;
        };
    }

    private void getButtonPositions() {
        buttonPositions.clear();
        centerX = width / 2;
        centerY = height / 2;
        double buttonAngle = 360.0 / options.size();

        for (int i = 0; i < options.size(); i++) {
            double angle = Math.toRadians((buttonAngle * i) - 90);
            int x = (int) (centerX + DIST_FROM_CENTER * Math.cos(angle));
            int y = (int) (centerY + DIST_FROM_CENTER * Math.sin(angle));
            buttonPositions.add(new Pair<>(x, y));
        }
    }

    private int getHoveredOption(int mouseX, int mouseY) {
        if (new Vec3i(mouseX, 0, mouseY).distManhattan(new Vec3i(centerX, 0, centerY)) < DEAD_ZONE) {
            return -1;
        }

        double angle = Math.atan2(centerY - mouseY, centerX - mouseX) % (2 * Math.PI) + (Math.PI / 2);
        float position = (float) ((angle + Math.PI) / (Math.PI / ((double) options.size() / 2)));
        return Math.round(position) % options.size();
    }

    private void executeOption(int optionIndex) {
        if (optionIndex < 0 || optionIndex >= options.size()) {
            onClose();
            return;
        }

        WheelOption option = options.get(optionIndex);
        onClose();
        if (option.pingType() == null) {
            hadesFeature.tryOpenPlayerViewer();
        } else {
            Services.Hades.sendPlayerPing(option.pingType(), pingTarget);
        }
    }

    private record WheelOption(String label, PlayerPingType pingType) {}
}
