/*
 * Copyright © Wynntils 2025.
 * This file is released under LGPLv3. See LICENSE for full license details.
 */
package com.wynntils.screens.compatibility;

import com.mojang.blaze3d.vertex.PoseStack;
import com.wynntils.core.WynntilsMod;
import com.wynntils.core.components.Services;
import com.wynntils.core.consumers.screens.WynntilsScreen;
import com.wynntils.core.text.StyledText;
import com.wynntils.screens.base.widgets.WynntilsCheckbox;
import com.wynntils.screens.update.UpdateScreen;
import com.wynntils.services.athena.type.CompatibilityTier;
import com.wynntils.services.athena.type.ModUpdateInfo;
import com.wynntils.utils.colors.CommonColors;
import com.wynntils.utils.mc.McUtils;
import com.wynntils.utils.render.FontRenderer;
import com.wynntils.utils.render.RenderUtils;
import com.wynntils.utils.render.Texture;
import com.wynntils.utils.render.type.HorizontalAlignment;
import com.wynntils.utils.render.type.TextShadow;
import com.wynntils.utils.render.type.VerticalAlignment;
import java.util.List;
import net.minecraft.client.gui.GuiGraphics;
import net.minecraft.client.gui.components.Button;
import net.minecraft.client.gui.components.Renderable;
import net.minecraft.client.gui.components.Tooltip;
import net.minecraft.network.chat.Component;
import net.minecraft.network.chat.Style;
import net.minecraft.resources.ResourceLocation;

public final class CompatibilityWarningScreen extends WynntilsScreen {
    private final CompatibilityTier compatibilityTier;

    private boolean useIncompatible = false;
    private int offsetX;
    private int offsetY;

    private CompatibilityWarningScreen(CompatibilityTier compatibilityTier) {
        super(Component.translatable("screens.wynntils.compatibility.name"));

        this.compatibilityTier = compatibilityTier;
    }

    public static CompatibilityWarningScreen create(CompatibilityTier compatibilityTier) {
        return new CompatibilityWarningScreen(compatibilityTier);
    }

    @Override
    protected void doInit() {
        super.doInit();

        offsetX = (int) ((this.width - Texture.SCROLL_BACKGROUND.width()) / 2f);
        offsetY = (int) ((this.height - Texture.SCROLL_BACKGROUND.height()) / 2f);

        if (compatibilityTier == CompatibilityTier.MAJOR_ERRORS) {
            this.addRenderableWidget(new WynntilsCheckbox(
                    offsetX + 110,
                    offsetY + 120,
                    20,
                    Component.translatable("screens.wynntils.compatibility.useWynntils"),
                    false,
                    100,
                    (checkbox, bl) -> useIncompatible = bl,
                    List.of(Component.translatable("screens.wynntils.compatibility.continueOutdated"))));
        }

        Button continueButton = new Button.Builder(
                        Component.translatable("screens.wynntils.compatibility.continue"),
                        (b) -> continueWithoutWynntils())
                .pos(offsetX + 60, offsetY + 150)
                .size(80, 20)
                .build();
        this.addRenderableWidget(continueButton);

        ModUpdateInfo updateInfo = Services.Update.getModUpdateInfo();
        boolean updateButtonActive = true;
        Component updateTooltip = Component.literal("Go to update screen");

        if (updateInfo == null) {
            updateButtonActive = false;
            updateTooltip = Component.translatable("screens.wynntils.compatibility.updateUnavailable");
        }

        Button updateButton = new Button.Builder(
                        Component.translatable("screens.wynntils.compatibility.update"),
                        (b) -> McUtils.mc().setScreen(UpdateScreen.create(this)))
                .pos(offsetX + 160, offsetY + 150)
                .size(80, 20)
                .tooltip(Tooltip.create(updateTooltip))
                .build();
        updateButton.active = updateButtonActive;
        this.addRenderableWidget(updateButton);
    }

    @Override
    public void onClose() {}

    @Override
    public void doRender(GuiGraphics guiGraphics, int mouseX, int mouseY, float partialTick) {
        renderBackground(guiGraphics, mouseX, mouseY, partialTick);

        PoseStack poseStack = guiGraphics.pose();

        RenderUtils.drawTexturedRect(poseStack, Texture.SCROLL_BACKGROUND, offsetX, offsetY);

        FontRenderer.getInstance()
                .renderText(
                        poseStack,
                        StyledText.fromComponent(Component.literal("Outdated Wynntils")
                                .withStyle(Style.EMPTY.withFont(
                                        ResourceLocation.withDefaultNamespace("language/wynncraft")))),
                        this.width / 2f,
                        offsetY + 20,
                        CommonColors.RED,
                        HorizontalAlignment.CENTER,
                        VerticalAlignment.TOP,
                        TextShadow.OUTLINE,
                        2.5f);

        FontRenderer.getInstance()
                .renderAlignedTextInBox(
                        poseStack,
                        StyledText.fromComponent(Component.translatable(
                                compatibilityTier.getScreenPromptKey(),
                                WynntilsMod.getVersion(),
                                Services.Compatibility.getWynncraftVersion().toString())),
                        this.width / 2f - 80,
                        this.width / 2f + 80,
                        this.height / 2f - 40,
                        this.height / 2f + 40,
                        Texture.SCROLL_BACKGROUND.width() - 80,
                        CommonColors.WHITE,
                        HorizontalAlignment.CENTER,
                        VerticalAlignment.TOP,
                        TextShadow.NORMAL);

        for (Renderable renderable : this.renderables) {
            renderable.render(guiGraphics, mouseX, mouseY, partialTick);
        }
    }

    private void continueWithoutWynntils() {
        super.onClose();

        if (useIncompatible) {
            Services.Compatibility.setOverrideIncompatibility();
        }
    }
}
