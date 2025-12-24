/*
 * Copyright © Wynntils 2025.
 * This file is released under LGPLv3. See LICENSE for full license details.
 */
package com.wynntils.screens.settings;

import com.wynntils.core.components.Managers;
import com.wynntils.core.consumers.screens.WynntilsScreen;
import com.wynntils.core.persisted.config.ConfigProfile;
import com.wynntils.core.text.StyledText;
import com.wynntils.screens.settings.widgets.ConfigProfileReturnButton;
import com.wynntils.screens.settings.widgets.ConfigProfileWidget;
import com.wynntils.utils.colors.CommonColors;
import com.wynntils.utils.mc.McUtils;
import com.wynntils.utils.render.FontRenderer;
import com.wynntils.utils.render.RenderUtils;
import com.wynntils.utils.render.Texture;
import com.wynntils.utils.render.type.AnimationPercentage;
import com.wynntils.utils.render.type.HorizontalAlignment;
import com.wynntils.utils.render.type.TextShadow;
import com.wynntils.utils.render.type.VerticalAlignment;
import java.time.Duration;
import java.time.temporal.ChronoUnit;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import net.minecraft.ChatFormatting;
import net.minecraft.client.gui.GuiGraphics;
import net.minecraft.client.gui.components.events.GuiEventListener;
import net.minecraft.client.gui.screens.Screen;
import net.minecraft.network.chat.Component;
import net.minecraft.network.chat.Style;
import net.minecraft.resources.ResourceLocation;
import org.joml.Vector2i;
import org.lwjgl.glfw.GLFW;

public class ConfigProfileScreen extends WynntilsScreen {
    private static final ResourceLocation RIBBON_FONT = ResourceLocation.withDefaultNamespace("banner/ribbon");
    private static final Component CONFIG_PROFILE_BANNER_BACKGROUND = Component.literal(
                    "\uDAFF\uDFF9\uE060\uDAFF\uDFFF\uE042\uDAFF\uDFFF\uE034\uDAFF\uDFFF\uE03B\uDAFF\uDFFF\uE034\uDAFF\uDFFF\uE032\uDAFF\uDFFF\uE043\uDAFF\uDFFF\uE061\uDAFF\uDFFF\uE030\uDAFF\uDFFF\uE061\uDAFF\uDFFF\uE03F\uDAFF\uDFFF\uE041\uDAFF\uDFFF\uE03E\uDAFF\uDFFF\uE035\uDAFF\uDFFF\uE038\uDAFF\uDFFF\uE03B\uDAFF\uDFFF\uE034\uDAFF\uDFFF\uE062\uDAFF\uDF9E")
            .withStyle(Style.EMPTY.withFont(RIBBON_FONT).withColor(ChatFormatting.AQUA));
    private static final Component CONFIG_PROFILE_BANNER_FOREGROUND = Component.literal(
                    "\uE012\uE004\uE00B\uE004\uE002\uE013 \uE000 \uE00F\uE011\uE00E\uE005\uE008\uE00B\uE004")
            .withStyle(Style.EMPTY.withFont(RIBBON_FONT).withColor(ChatFormatting.BLACK));

    private static final StyledText WYNNCRAFT_LOGO =
            StyledText.fromComponent(Component.literal("\uE005\uDAFF\uDFFF\uE006")
                    .withStyle(Style.EMPTY.withFont(ResourceLocation.withDefaultNamespace("screen/static"))));
    private static final StyledText AMPERSAND_WYNNCRAFT_FONT = StyledText.fromComponent(Component.literal("&")
            .withStyle(Style.EMPTY.withFont(ResourceLocation.withDefaultNamespace("language/wynncraft"))));
    private static final ResourceLocation WYNNTILS_LOGO_LOCATION =
            ResourceLocation.fromNamespaceAndPath("wynntils", "logo.png");

    private static final int WIDGET_SPACING = 180;
    private static final int BANNER_START_Y = -10;
    private static final int BANNER_TARGET_Y = 10;
    private static final int WELCOME_DELAY_MS = 500;
    private static final int WYNNTILS_FADE_TIME_MS = 2000;

    private final AnimationPercentage bannerAnimationPercentage =
            new AnimationPercentage(() -> true, Duration.of(100, ChronoUnit.MILLIS));
    private final AnimationPercentage cardAnimationPercentage =
            new AnimationPercentage(() -> true, Duration.of(150, ChronoUnit.MILLIS));
    private final AnimationPercentage welcomePromptAnimationPercentage =
            new AnimationPercentage(() -> true, Duration.of(WELCOME_DELAY_MS, ChronoUnit.MILLIS));
    private final AnimationPercentage wynntilsFadeAnimationPercentage =
            new AnimationPercentage(() -> true, Duration.of(WYNNTILS_FADE_TIME_MS, ChronoUnit.MILLIS));

    private final Screen previousScreen;

    private boolean firstInit = true;
    private boolean showWelcomePrompt;
    private ConfigProfile focusedProfile;
    private long welcomeAnimationStartTime;

    private List<ConfigProfileWidget> configProfileWidgets = new ArrayList<>();
    private final Map<ConfigProfile, Integer> startXPositions = new HashMap<>();
    private final Map<ConfigProfile, Vector2i> targetPositions = new HashMap<>();

    private ConfigProfileScreen(Screen previousScreen, ConfigProfile focusedProfile) {
        super(Component.translatable("screens.wynntils.configProfilesScreen.name"));

        this.previousScreen = previousScreen;
        this.focusedProfile = focusedProfile;

        this.showWelcomePrompt = Managers.Config.showWelcomeScreen.get();
    }

    public static Screen create(Screen previousScreen, ConfigProfile focusedProfile) {
        return new ConfigProfileScreen(previousScreen, focusedProfile);
    }

    @Override
    protected void doInit() {
        super.doInit();

        if (!showWelcomePrompt) {
            this.addRenderableWidget(
                    new ConfigProfileReturnButton(this.width / 2 - 40, this.height - 22, 80, 20, this));
        }

        if (firstInit) {
            populateProfiles();
            firstInit = false;
            welcomeAnimationStartTime = System.currentTimeMillis() + WELCOME_DELAY_MS;
        } else {
            updateTargetPositions();
            snapToTargets();
        }
    }

    @Override
    public void doRender(GuiGraphics guiGraphics, int mouseX, int mouseY, float partialTick) {
        super.doRender(guiGraphics, mouseX, mouseY, partialTick);

        if (showWelcomePrompt) {
            int logoX = (int) (this.width / 2f);

            if (System.currentTimeMillis() >= welcomeAnimationStartTime) {
                logoX = (int) (this.width / 2f
                        + ((this.width / 2f - 40) - this.width / 2f) * welcomePromptAnimationPercentage.getAnimation());
            }

            FontRenderer.getInstance()
                    .renderText(
                            guiGraphics.pose(),
                            WYNNCRAFT_LOGO,
                            logoX,
                            90,
                            CommonColors.WHITE,
                            HorizontalAlignment.CENTER,
                            VerticalAlignment.TOP,
                            TextShadow.NONE,
                            2f);

            if (System.currentTimeMillis() < welcomeAnimationStartTime + WELCOME_DELAY_MS) return;

            // Need a minimum value as otherwise the text renders with max alpha
            float wynntilsFadeAnimation = (float) Math.max(0.1f, wynntilsFadeAnimationPercentage.getAnimation());

            FontRenderer.getInstance()
                    .renderText(
                            guiGraphics.pose(),
                            AMPERSAND_WYNNCRAFT_FONT,
                            this.width / 2f + 92,
                            45,
                            CommonColors.GREEN.withAlpha(wynntilsFadeAnimation),
                            HorizontalAlignment.CENTER,
                            VerticalAlignment.TOP,
                            TextShadow.OUTLINE,
                            3f);

            RenderUtils.drawTexturedRectWithColor(
                    guiGraphics.pose(),
                    WYNNTILS_LOGO_LOCATION,
                    CommonColors.WHITE.withAlpha(wynntilsFadeAnimation),
                    this.width / 2f + 90,
                    20,
                    0,
                    90,
                    90,
                    0,
                    0,
                    1183,
                    1183,
                    1183,
                    1183);

            FontRenderer.getInstance()
                    .renderAlignedTextInBox(
                            guiGraphics.pose(),
                            List.of(
                                            StyledText.fromComponent(Component.translatable(
                                                            "screens.wynntils.configProfilesScreen.welcome1")
                                                    .withStyle(ChatFormatting.BOLD)),
                                            StyledText.fromString(""),
                                            StyledText.fromComponent(Component.translatable(
                                                    "screens.wynntils.configProfilesScreen.welcome2")),
                                            StyledText.fromString(""),
                                            StyledText.fromComponent(Component.translatable(
                                                    "screens.wynntils.configProfilesScreen.welcome3")),
                                            StyledText.fromString(""),
                                            StyledText.fromComponent(Component.translatable(
                                                            "screens.wynntils.configProfilesScreen.welcome4")
                                                    .withStyle(ChatFormatting.UNDERLINE)))
                                    .toArray(StyledText[]::new),
                            20,
                            this.width - 20,
                            120,
                            this.height - 20,
                            this.width - 40,
                            CommonColors.AQUA.withAlpha(wynntilsFadeAnimation),
                            HorizontalAlignment.CENTER,
                            VerticalAlignment.MIDDLE,
                            TextShadow.OUTLINE,
                            1.5f);

            return;
        }

        int bannerY =
                (int) (BANNER_START_Y + (BANNER_TARGET_Y - BANNER_START_Y) * bannerAnimationPercentage.getAnimation());

        FontRenderer.getInstance()
                .renderText(
                        guiGraphics.pose(),
                        StyledText.fromComponent(CONFIG_PROFILE_BANNER_BACKGROUND)
                                .append(StyledText.fromComponent(CONFIG_PROFILE_BANNER_FOREGROUND)),
                        this.width / 2f,
                        bannerY,
                        CommonColors.WHITE,
                        HorizontalAlignment.CENTER,
                        VerticalAlignment.TOP,
                        TextShadow.OUTLINE,
                        2f);

        for (ConfigProfileWidget widget : configProfileWidgets) {
            int startX = startXPositions.getOrDefault(
                    widget.getProfile(),
                    targetPositions.get(widget.getProfile()).x());

            int targetX = targetPositions.get(widget.getProfile()).x();

            int newX = (int) (startX + (targetX - startX) * cardAnimationPercentage.getAnimation());
            widget.setX(newX);

            widget.render(guiGraphics, mouseX, mouseY, partialTick);
        }
    }

    @Override
    public void onClose() {
        McUtils.setScreen(previousScreen);
    }

    @Override
    public boolean doMouseClicked(double mouseX, double mouseY, int button) {
        if (showWelcomePrompt) {
            if (System.currentTimeMillis() < welcomeAnimationStartTime + WELCOME_DELAY_MS + WYNNTILS_FADE_TIME_MS) {
                return false;
            } else {
                showWelcomePrompt = false;
                Managers.Config.showWelcomeScreen.store(false);
                this.addRenderableWidget(
                        new ConfigProfileReturnButton(this.width / 2 - 40, this.height - 22, 80, 20, this));
                return true;
            }
        }

        for (ConfigProfileWidget widget : configProfileWidgets) {
            if (widget.isMouseOver(mouseX, mouseY)) {
                return widget.mouseClicked(mouseX, mouseY, button);
            }
        }

        for (GuiEventListener listener : this.children()) {
            if (listener.isMouseOver(mouseX, mouseY)) {
                return listener.mouseClicked(mouseX, mouseY, button);
            }
        }

        return false;
    }

    @Override
    public boolean mouseScrolled(double mouseX, double mouseY, double deltaX, double deltaY) {
        updateFocusedProfile((int) -Math.signum(deltaY));

        return super.mouseScrolled(mouseX, mouseY, deltaX, deltaY);
    }

    @Override
    public boolean keyPressed(int keyCode, int scanCode, int modifiers) {
        if (McUtils.options().keyLeft.matches(keyCode, scanCode) || keyCode == GLFW.GLFW_KEY_LEFT) {
            updateFocusedProfile(-1);
        } else if (McUtils.options().keyRight.matches(keyCode, scanCode) || keyCode == GLFW.GLFW_KEY_RIGHT) {
            updateFocusedProfile(1);
        } else if (keyCode == GLFW.GLFW_KEY_SPACE || keyCode == GLFW.GLFW_KEY_ENTER) {
            Managers.Config.setSelectedProfile(focusedProfile);
        }

        return super.keyPressed(keyCode, scanCode, modifiers);
    }

    private void updateFocusedProfile(int direction) {
        int index = focusedProfile.ordinal();

        if (direction < 0 && index == 0) return;
        if (direction > 0 && index == ConfigProfile.values().length - 1) return;

        focusedProfile = ConfigProfile.values()[index + direction];
        updateTargetPositions();
        cardAnimationPercentage.restart();
    }

    private void updateTargetPositions() {
        startXPositions.clear();

        for (ConfigProfileWidget widget : configProfileWidgets) {
            startXPositions.put(widget.getProfile(), widget.getX());
        }

        targetPositions.clear();

        ConfigProfile[] profiles = ConfigProfile.values();
        int focusedIndex = focusedProfile.ordinal();

        int centerX = this.width / 2;
        int focusedX = (int) (centerX - Texture.CONFIG_PROFILE_BACKGROUND.width() / 2f);
        int centerY = this.height / 2 - 105 + 10;

        targetPositions.put(focusedProfile, new Vector2i(focusedX, centerY));

        int leftX = focusedX - WIDGET_SPACING;
        for (int i = focusedIndex - 1; i >= 0; i--) {
            targetPositions.put(profiles[i], new Vector2i(leftX, centerY));
            leftX -= WIDGET_SPACING;
        }

        int rightX = focusedX + WIDGET_SPACING;
        for (int i = focusedIndex + 1; i < profiles.length; i++) {
            targetPositions.put(profiles[i], new Vector2i(rightX, centerY));
            rightX += WIDGET_SPACING;
        }
    }

    private void snapToTargets() {
        for (ConfigProfileWidget widget : configProfileWidgets) {
            widget.setX(targetPositions.get(widget.getProfile()).x());
            widget.setY(targetPositions.get(widget.getProfile()).y());
        }
    }

    private void populateProfiles() {
        configProfileWidgets.clear();

        for (ConfigProfile profile : ConfigProfile.values()) {
            configProfileWidgets.add(new ConfigProfileWidget(0, this.height / 2 - 105 + 10, profile));
        }

        updateTargetPositions();
    }
}
