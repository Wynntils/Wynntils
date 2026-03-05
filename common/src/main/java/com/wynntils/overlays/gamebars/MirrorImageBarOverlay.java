/*
 * Copyright © Wynntils 2026.
 * This file is released under LGPLv3. See LICENSE for full license details.
 */
package com.wynntils.overlays.gamebars;

import com.wynntils.core.components.Models;
import com.wynntils.core.consumers.overlays.OverlayPosition;
import com.wynntils.core.consumers.overlays.OverlaySize;
import com.wynntils.core.text.StyledText;
import com.wynntils.handlers.bossbar.TrackedBar;
import com.wynntils.handlers.bossbar.type.BossBarProgress;
import com.wynntils.models.abilities.bossbars.MirrorImageBar;
import com.wynntils.models.abilities.type.MirrorImageClone;
import com.wynntils.utils.colors.CommonColors;
import com.wynntils.utils.render.FontRenderer;
import com.wynntils.utils.render.type.HorizontalAlignment;
import com.wynntils.utils.render.type.VerticalAlignment;
import java.util.stream.Collectors;
import net.minecraft.client.gui.GuiGraphics;
import net.minecraft.network.chat.Component;
import net.minecraft.network.chat.FontDescription;
import net.minecraft.network.chat.Style;
import net.minecraft.resources.Identifier;

public class MirrorImageBarOverlay extends BaseBarOverlay {
    public MirrorImageBarOverlay() {
        super(
                new OverlayPosition(
                        -70,
                        -150,
                        VerticalAlignment.BOTTOM,
                        HorizontalAlignment.CENTER,
                        OverlayPosition.AnchorSection.BOTTOM_MIDDLE),
                new OverlaySize(81, 21),
                CommonColors.PURPLE);
    }

    @Override
    protected BossBarProgress progress() {
        return Models.Ability.mirrorImageBar.getBarProgress();
    }

    @Override
    protected Class<? extends TrackedBar> getTrackedBarClass() {
        return MirrorImageBar.class;
    }

    @Override
    protected boolean isVisible() {
        return Models.Ability.mirrorImageBar.isActive();
    }

    @Override
    protected void renderText(GuiGraphics guiGraphics, float renderY, String text) {
        FontRenderer.getInstance()
                .renderAlignedTextInBox(
                        guiGraphics,
                        StyledText.fromString(Models.Ability.mirrorImageBar.getDuration() + "s ")
                                .append(StyledText.fromComponent(
                                        Component.literal(Models.Ability.mirrorImageBar.getClones().stream()
                                                        .map(MirrorImageClone::getString)
                                                        .collect(Collectors.joining()))
                                                .withStyle(Style.EMPTY.withFont(new FontDescription.Resource(
                                                        Identifier.withDefaultNamespace("common")))))),
                        this.getRenderX(),
                        this.getRenderX() + this.getWidth(),
                        renderY,
                        0,
                        this.textColor.get(),
                        this.getRenderHorizontalAlignment(),
                        this.textShadow.get());
    }
}
