/*
 * Copyright © Wynntils 2023.
 * This file is released under AGPLv3. See LICENSE for full license details.
 */
package com.wynntils.screens.changelog;

import com.mojang.blaze3d.vertex.PoseStack;
import com.wynntils.screens.base.WynntilsScreen;
import com.wynntils.utils.StringUtils;
import com.wynntils.utils.colors.CommonColors;
import com.wynntils.utils.mc.McUtils;
import com.wynntils.utils.render.FontRenderer;
import com.wynntils.utils.render.RenderUtils;
import com.wynntils.utils.render.TextRenderSetting;
import com.wynntils.utils.render.TextRenderTask;
import com.wynntils.utils.render.Texture;
import com.wynntils.utils.render.type.TextShadow;
import java.util.Arrays;
import java.util.List;
import net.minecraft.client.gui.screens.Screen;
import net.minecraft.network.chat.Component;

public class ChangelogScreen extends WynntilsScreen {
    private final String changelog;
    private List<TextRenderTask> changelogTasks;

    public ChangelogScreen(String changelog) {
        super(Component.translatable("screens.wynntils.changelog.name"));

        this.changelog = changelog;
    }

    public static Screen create(String changelog) {
        return new ChangelogScreen(changelog);
    }

    @Override
    protected void doInit() {
        TextRenderSetting setting = TextRenderSetting.DEFAULT
                .withMaxWidth(Texture.CHANGELOG_BACKGROUND.width() - 50)
                .withCustomColor(CommonColors.WHITE)
                .withTextShadow(TextShadow.OUTLINE);

        this.changelogTasks = Arrays.stream(changelog.split("\n"))
                .map(StringUtils::stripMarkdownStyling)
                .map(s -> new TextRenderTask(s, setting))
                .toList();
    }

    @Override
    public void doRender(PoseStack poseStack, int mouseX, int mouseY, float partialTick) {
        poseStack.pushPose();
        poseStack.translate(
                (McUtils.window().getGuiScaledWidth() - Texture.CHANGELOG_BACKGROUND.width()) / 2f,
                (McUtils.window().getGuiScaledHeight() - Texture.CHANGELOG_BACKGROUND.height()) / 2f,
                0);

        RenderUtils.drawTexturedRect(poseStack, Texture.CHANGELOG_BACKGROUND, 0, 0);

        FontRenderer.getInstance().renderTexts(poseStack, 45, 15, changelogTasks);

        poseStack.popPose();
    }
}
