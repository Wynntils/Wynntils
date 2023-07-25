/*
 * Copyright © Wynntils 2022.
 * This file is released under AGPLv3. See LICENSE for full license details.
 */
package com.wynntils.screens.overlays.selection;

import com.google.common.collect.ImmutableList;
import com.mojang.blaze3d.vertex.PoseStack;
import com.wynntils.core.components.Managers;
import com.wynntils.core.consumers.features.overlays.Overlay;
import com.wynntils.core.text.StyledText;
import com.wynntils.screens.overlays.placement.OverlayManagementScreen;
import com.wynntils.utils.colors.CommonColors;
import com.wynntils.utils.colors.CustomColor;
import com.wynntils.utils.mc.McUtils;
import com.wynntils.utils.render.FontRenderer;
import com.wynntils.utils.render.RenderUtils;
import com.wynntils.utils.render.type.HorizontalAlignment;
import com.wynntils.utils.render.type.TextShadow;
import com.wynntils.utils.render.type.VerticalAlignment;
import java.util.List;
import net.minecraft.client.gui.components.ContainerObjectSelectionList;
import net.minecraft.client.gui.components.events.GuiEventListener;
import net.minecraft.client.gui.narration.NarratableEntry;

public class OverlayEntry extends ContainerObjectSelectionList.Entry<OverlayEntry> {
    private static final float PADDING = 2.4f;
    private static final CustomColor ENABLED_COLOR = new CustomColor(0, 116, 0, 255);
    private static final CustomColor DISABLED_COLOR = new CustomColor(60, 60, 60, 255);
    private static final CustomColor DISABLED_FEATURE_COLOR = new CustomColor(120, 0, 0, 255);
    private static final CustomColor ENABLED_COLOR_BORDER = new CustomColor(0, 220, 0, 255);
    private static final CustomColor DISABLED_COLOR_BORDER = new CustomColor(0, 0, 0, 255);
    private static final CustomColor DISABLED_FEATURE_COLOR_BORDER = new CustomColor(255, 0, 0, 255);

    private final Overlay overlay;

    public OverlayEntry(Overlay overlay) {
        this.overlay = overlay;
    }

    @Override
    public List<? extends NarratableEntry> narratables() {
        return ImmutableList.of();
    }

    @Override
    public void render(
            PoseStack poseStack,
            int index,
            int top,
            int left,
            int width,
            int height,
            int mouseX,
            int mouseY,
            boolean isMouseOver,
            float partialTick) {
        poseStack.pushPose();
        poseStack.translate(left + PADDING, top + PADDING, 0);

        boolean enabled = Managers.Overlay.isEnabled(this.overlay);
        int y = index != 0 ? 2 : 0;

        CustomColor borderColor = getBorderColor(enabled);
        RenderUtils.drawRect(poseStack, borderColor.withAlpha(100), 0, y, 0, width - PADDING, height - y - PADDING);

        CustomColor rectColor = getRectColor(enabled);
        RenderUtils.drawRectBorders(poseStack, rectColor, 0, y, width - PADDING, height - PADDING, 1, 2);

        poseStack.translate(0, 0, 1);
        String translatedName = this.overlay.getTranslatedName();
        float renderHeightForOverlayName = FontRenderer.getInstance().calculateRenderHeight(translatedName, width);
        FontRenderer.getInstance()
                .renderText(
                        poseStack,
                        StyledText.fromString(translatedName),
                        3,
                        (OverlayList.getItemHeight() - renderHeightForOverlayName / 2f) / 2f - PADDING / 2f,
                        width - PADDING,
                        CommonColors.WHITE,
                        HorizontalAlignment.LEFT,
                        VerticalAlignment.TOP,
                        TextShadow.NORMAL);

        poseStack.popPose();
    }

    private CustomColor getBorderColor(boolean enabled) {
        if (!overlay.isParentEnabled()) return DISABLED_FEATURE_COLOR_BORDER;

        return enabled ? ENABLED_COLOR_BORDER : DISABLED_COLOR_BORDER;
    }

    private CustomColor getRectColor(boolean enabled) {
        if (!overlay.isParentEnabled()) return DISABLED_FEATURE_COLOR;

        return enabled ? ENABLED_COLOR : DISABLED_COLOR;
    }

    @Override
    public List<? extends GuiEventListener> children() {
        return ImmutableList.of();
    }

    public Overlay getOverlay() {
        return overlay;
    }

    @Override
    public boolean mouseClicked(double mouseX, double mouseY, int button) {
        if (!overlay.isParentEnabled()) return false;

        // right click
        if (button == 1) {
            Managers.Config.getConfigHolders()
                    .filter(configHolder -> configHolder.getParent() == overlay
                            && configHolder.getFieldName().equals("userEnabled"))
                    .findFirst()
                    .ifPresent(configHolder -> configHolder.setValue(!Managers.Overlay.isEnabled(overlay)));
            Managers.Config.saveConfig();
            return true;
        }

        if (!Managers.Overlay.isEnabled(overlay)) return false;

        McUtils.mc().setScreen(OverlayManagementScreen.create(this.overlay));
        return true;
    }
}
