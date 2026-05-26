/*
 * Copyright © Wynntils 2026.
 * This file is released under LGPLv3. See LICENSE for full license details.
 */
package com.wynntils.overlays.remoteoverlays;

import com.mojang.blaze3d.platform.Window;
import com.wynntils.core.components.Managers;
import com.wynntils.core.consumers.overlays.RemoteOverlayBase;
import com.wynntils.core.persisted.Persisted;
import com.wynntils.core.persisted.config.Config;
import com.wynntils.core.text.StyledText;
import com.wynntils.utils.colors.CommonColors;
import com.wynntils.utils.render.FontRenderer;
import com.wynntils.utils.render.type.TextShadow;
import net.minecraft.client.DeltaTracker;
import net.minecraft.client.gui.GuiGraphics;

public class RemoteInfoBox extends RemoteOverlayBase {
    @Persisted(i18nKey = "overlay.wynntils.textOverlay.textShadow")
    private final Config<TextShadow> textShadow = new Config<>(TextShadow.OUTLINE);

    @Persisted(i18nKey = "overlay.wynntils.textOverlay.fontScale")
    private final Config<Float> fontScale = new Config<>(1.0f);

    @Persisted(i18nKey = "overlay.wynntils.textOverlay.fontScale.fitText")
    private final Config<Boolean> fitText = new Config<>(false);

    private final String RemoteFunction;
    private final String providedName;

    private StyledText[] cachedLines = new StyledText[0];

    public RemoteInfoBox(String remoteFunction, String providedName, String nameId) {
        super(nameId);
        this.providedName = providedName;
        this.RemoteFunction = remoteFunction;
    }

    @Override
    public void render(GuiGraphics guiGraphics, DeltaTracker deltaTracker, Window window) {
        float renderX = this.getRenderX();
        float renderY = this.getRenderY();
        FontRenderer.getInstance()
                .renderAlignedTextInBox(
                        guiGraphics,
                        cachedLines,
                        renderX,
                        renderX + this.getWidth(),
                        renderY,
                        renderY + this.getHeight(),
                        fitText.get() ? this.getWidth() : 0,
                        CommonColors.WHITE,
                        this.getRenderHorizontalAlignment(),
                        this.getRenderVerticalAlignment(),
                        textShadow.get(),
                        fontScale.get());
    }

    @Override
    public void tick() {
        if (!isRendered()) return;
        cachedLines = calculateTemplateValue();
    }

    private StyledText[] calculateTemplateValue() {
        return Managers.Function.doFormatLines(RemoteFunction);
    }

    public String getProvidedName() {
        return providedName;
    }
}
