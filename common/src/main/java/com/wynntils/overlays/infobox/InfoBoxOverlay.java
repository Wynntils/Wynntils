/*
 * Copyright © Wynntils 2022-2025.
 * This file is released under LGPLv3. See LICENSE for full license details.
 */
package com.wynntils.overlays.infobox;

import com.mojang.blaze3d.platform.Window;
import com.wynntils.core.components.Managers;
import com.wynntils.core.consumers.overlays.CustomNameProperty;
import com.wynntils.core.consumers.overlays.TextOverlay;
import com.wynntils.core.persisted.Persisted;
import com.wynntils.core.persisted.config.Config;
import com.wynntils.core.persisted.config.HiddenConfig;
import com.wynntils.core.text.StyledText;
import com.wynntils.utils.colors.CommonColors;
import com.wynntils.utils.colors.CustomColor;
import com.wynntils.utils.render.buffered.BufferedFontRenderer;
import com.wynntils.utils.render.type.HorizontalAlignment;
import com.wynntils.utils.render.type.TextShadow;
import com.wynntils.utils.render.type.VerticalAlignment;
import com.wynntils.utils.type.ErrorOr;
import net.minecraft.client.DeltaTracker;
import net.minecraft.client.gui.GuiGraphics;
import net.minecraft.client.renderer.MultiBufferSource;
import net.minecraft.client.resources.language.I18n;

public class InfoBoxOverlay extends TextOverlay implements CustomNameProperty {
    @Persisted
    private final HiddenConfig<String> customName = new HiddenConfig<>("");

    @Persisted
    private final Config<String> content = new Config<>("");

    @Persisted
    private final Config<String> colorTemplate = new Config<>("");

    private ErrorOr<CustomColor> colorCache = ErrorOr.of(CommonColors.WHITE);

    public InfoBoxOverlay(int id) {
        super(id);
    }

    @Override
    public String getTemplate() {
        return content.get();
    }

    @Override
    public String getPreviewTemplate() {
        if (!content.get().isEmpty()) {
            return content.get();
        }

        return "&cX: {x(my_loc):0}, &9Y: {y(my_loc):0}, &aZ: {z(my_loc):0}";
    }

    @Override
    protected void renderOrErrorMessage(
            GuiGraphics guiGraphics, MultiBufferSource bufferSource, DeltaTracker deltaTracker, Window window) {
        if (colorCache.hasError()) {
            StyledText[] errorMessage = {
                StyledText.fromString("§c§l"
                        + I18n.get(
                                "feature.wynntils.customBarsOverlay.overlay.universalTexturedCustomBar.colorTemplate.error")
                        + " " + getTranslatedName()),
                StyledText.fromUnformattedString(colorCache.getError())
            };
            BufferedFontRenderer.getInstance()
                    .renderAlignedTextInBox(
                            guiGraphics.pose(),
                            bufferSource,
                            errorMessage,
                            getRenderX(),
                            getRenderX() + getWidth(),
                            getRenderY(),
                            getRenderY() + getHeight(),
                            0,
                            CommonColors.WHITE,
                            HorizontalAlignment.CENTER,
                            VerticalAlignment.MIDDLE,
                            TextShadow.NORMAL,
                            1);

        } else {
            super.renderOrErrorMessage(guiGraphics, bufferSource, deltaTracker, window);
        }
    }

    @Override
    protected CustomColor getRenderColor() {
        return colorCache.hasError() ? CommonColors.WHITE : colorCache.getValue();
    }

    @Override
    public void tick() {
        super.tick();
        if (!isRendered()) return;

        String template = colorTemplate.get();
        if (template.isBlank()) {
            colorCache = ErrorOr.of(CommonColors.WHITE);
            return;
        }

        String formattedText =
                StyledText.join("", Managers.Function.doFormatLines(template)).getString();
        colorCache = Managers.Function.tryGetRawValueOfType(formattedText, CustomColor.class);
    }

    @Override
    public Config<String> getCustomName() {
        return customName;
    }

    @Override
    public void setCustomName(String newName) {
        customName.setValue(newName);
    }
}
