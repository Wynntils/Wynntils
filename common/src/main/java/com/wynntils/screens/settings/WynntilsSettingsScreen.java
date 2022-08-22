/*
 * Copyright © Wynntils 2022.
 * This file is released under AGPLv3. See LICENSE for full license details.
 */
package com.wynntils.screens.settings;

import com.mojang.blaze3d.vertex.PoseStack;
import com.wynntils.mc.objects.CommonColors;
import com.wynntils.mc.objects.CustomColor;
import com.wynntils.mc.render.FontRenderer;
import com.wynntils.mc.render.RenderUtils;
import com.wynntils.mc.utils.McUtils;
import com.wynntils.screens.settings.lists.FeatureList;
import com.wynntils.screens.widgets.SearchWidget;
import net.minecraft.client.gui.components.Button;
import net.minecraft.client.gui.screens.Screen;
import net.minecraft.client.resources.language.I18n;
import net.minecraft.network.chat.TranslatableComponent;

public class WynntilsSettingsScreen extends Screen {
    private static final int BUTTON_WIDTH = 160;
    private static final int BUTTON_HEIGHT = 20;
    private static final int SEARCH_BAR_HEIGHT = 20;
    private final float BAR_HEIGHT = 30f;

    private static final CustomColor BACKGROUND_COLOR = new CustomColor(56, 42, 27, 255);
    private static final CustomColor FOREGROUND_COLOR = new CustomColor(126, 111, 83, 255);

    private final Screen lastScreen;

    private FeatureList featureList;

    public WynntilsSettingsScreen() {
        super(new TranslatableComponent("screens.wynntils.settingsScreen.name"));
        lastScreen = McUtils.mc().screen;
        featureList = new FeatureList(this);
    }

    @Override
    protected void init() {
        this.addRenderableWidget(new Button(
                this.width / 2 - BUTTON_WIDTH / 2,
                this.height - BUTTON_HEIGHT - 5,
                BUTTON_WIDTH,
                BUTTON_HEIGHT,
                new TranslatableComponent("screens.wynntils.settingsScreen.close"),
                button -> McUtils.mc().setScreen(lastScreen)));

        this.addRenderableWidget(
                new SearchWidget(this.width / 90, (int) (BAR_HEIGHT + 25), (int) (this.width / 6f), SEARCH_BAR_HEIGHT));
    }

    @Override
    public void render(PoseStack poseStack, int mouseX, int mouseY, float partialTick) {
        this.renderBackground(poseStack);

        FontRenderer.getInstance()
                .renderText(
                        poseStack,
                        I18n.get("screens.wynntils.settingsScreen.title"),
                        this.width / 2f,
                        10,
                        CommonColors.WHITE,
                        FontRenderer.TextAlignment.CENTER_ALIGNED,
                        FontRenderer.TextShadow.OUTLINE);

        featureList.render(poseStack, mouseX, mouseY, partialTick);

        FontRenderer.getInstance()
                .renderAlignedTextInBox(
                        poseStack,
                        I18n.get("screens.wynntils.settingsScreen.featureList"),
                        0,
                        this.width / 5f,
                        BAR_HEIGHT + 7.5f,
                        0,
                        CommonColors.WHITE,
                        FontRenderer.TextAlignment.CENTER_ALIGNED,
                        FontRenderer.TextShadow.OUTLINE);

        super.render(poseStack, mouseX, mouseY, partialTick);
    }

    @Override
    public void renderBackground(PoseStack poseStack) {
        RenderUtils.drawRect(poseStack, BACKGROUND_COLOR, 0, 0, 0, this.width, this.height);

        RenderUtils.drawRect(poseStack, FOREGROUND_COLOR, 0, 0, 0, this.width, BAR_HEIGHT);
        RenderUtils.drawRect(poseStack, FOREGROUND_COLOR, 0, this.height - BAR_HEIGHT, 0, this.width, BAR_HEIGHT);
    }

    public float getBarHeight() {
        return BAR_HEIGHT;
    }
}
