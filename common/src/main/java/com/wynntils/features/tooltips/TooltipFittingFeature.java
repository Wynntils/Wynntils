/*
 * Copyright © Wynntils 2022-2023.
 * This file is released under LGPLv3. See LICENSE for full license details.
 */
package com.wynntils.features.tooltips;

import com.mojang.blaze3d.platform.Window;
import com.mojang.blaze3d.vertex.PoseStack;
import com.wynntils.core.consumers.features.Feature;
import com.wynntils.core.persisted.Persisted;
import com.wynntils.core.persisted.config.Category;
import com.wynntils.core.persisted.config.Config;
import com.wynntils.core.persisted.config.ConfigCategory;
import com.wynntils.mc.event.ItemTooltipRenderEvent;
import com.wynntils.mc.event.TooltipRenderEvent;
import com.wynntils.utils.mc.ComponentUtils;
import com.wynntils.utils.mc.McUtils;
import java.util.List;
import net.minecraft.client.gui.screens.inventory.tooltip.ClientTooltipComponent;
import net.minecraft.client.gui.screens.inventory.tooltip.ClientTooltipPositioner;
import net.minecraft.client.gui.screens.inventory.tooltip.TooltipRenderUtil;
import net.minecraft.network.chat.Component;
import net.minecraftforge.eventbus.api.EventPriority;
import net.minecraftforge.eventbus.api.SubscribeEvent;
import org.joml.Vector2i;
import org.joml.Vector2ic;

@ConfigCategory(Category.TOOLTIPS)
public class TooltipFittingFeature extends Feature {
    @Persisted
    public final Config<Float> universalScale = new Config<>(1f);

    @Persisted
    public final Config<Boolean> fitToScreen = new Config<>(true);

    @Persisted
    public final Config<Boolean> wrapText = new Config<>(true);

    private boolean scaledLast = false;
    private float lastScaleFactor = 1f;

    // scaling should only happen after every other feature has updated tooltip
    @SubscribeEvent(priority = EventPriority.LOWEST)
    public void onTooltipPre(ItemTooltipRenderEvent.Pre e) {
        Window window = McUtils.mc().getWindow();

        if (wrapText.get()) {
            // calculate optimal wrapping for scaled up tooltips
            int tooltipWidth = ComponentUtils.getOptimalTooltipWidth(
                    e.getTooltips(), (int) (window.getGuiScaledWidth() / universalScale.get()), (int)
                            (e.getMouseX() / universalScale.get()));

            List<Component> wrappedTooltips = ComponentUtils.wrapTooltips(e.getTooltips(), tooltipWidth);

            e.setTooltips(wrappedTooltips);
        }

        // calculate scale factor
        float scaleFactor = universalScale.get();

        if (fitToScreen.get()) {
            List<Component> tooltips = e.getTooltips();

            List<ClientTooltipComponent> clientTooltipComponents = tooltips.stream()
                    .map(Component::getVisualOrderText)
                    .map(ClientTooltipComponent::create)
                    .toList();

            int tooltipHeight = clientTooltipComponents.size() == 1 ? -2 : 0;
            tooltipHeight += clientTooltipComponents.stream()
                    .mapToInt(ClientTooltipComponent::getHeight)
                    .sum();

            // Compensate for the top and bottom padding
            tooltipHeight += TooltipRenderUtil.PADDING_TOP + TooltipRenderUtil.PADDING_BOTTOM;

            tooltipHeight *= universalScale.get();

            if (tooltipHeight > window.getGuiScaledHeight()) {
                scaleFactor *= (window.getGuiScaledHeight() / (float) tooltipHeight);
            }
        }

        // push pose before scaling, so we can pop it afterwards
        PoseStack poseStack = e.getPoseStack();
        poseStack.pushPose();
        poseStack.scale(scaleFactor, scaleFactor, 1);

        scaledLast = true;
        lastScaleFactor = scaleFactor;
    }

    // highest priority to reset pose before other features start rendering
    @SubscribeEvent(priority = EventPriority.HIGHEST)
    public void onTooltipPost(ItemTooltipRenderEvent.Post e) {
        if (!scaledLast) return;

        e.getPoseStack().popPose();
        scaledLast = false;
    }

    @SubscribeEvent(priority = EventPriority.HIGHEST)
    public void onTooltipRendering(TooltipRenderEvent event) {
        if (!scaledLast) return;

        event.setPositioner(new ScaledTooltipPositioner(lastScaleFactor));
    }

    /**
     * A {@link ClientTooltipPositioner} that adjusts the position of the tooltip to fit the screen.
     * This is the same as {@link net.minecraft.client.gui.screens.inventory.tooltip.DefaultTooltipPositioner}, but scaled.
     */
    private static final class ScaledTooltipPositioner implements ClientTooltipPositioner {
        private final float scaleFactor;

        private ScaledTooltipPositioner(float scaleFactor) {
            this.scaleFactor = scaleFactor;
        }

        @Override
        public Vector2ic positionTooltip(
                int screenWidth, int screenHeight, int mouseX, int mouseY, int tooltipWidth, int tooltipHeight) {
            Vector2i tooltipPos = new Vector2i(mouseX, mouseY);

            tooltipWidth *= scaleFactor;
            tooltipHeight *= scaleFactor;

            int widthPaddings =
                    (int) ((TooltipRenderUtil.PADDING_LEFT + TooltipRenderUtil.PADDING_RIGHT) * scaleFactor);
            int heightPaddings =
                    (int) ((TooltipRenderUtil.PADDING_TOP + TooltipRenderUtil.PADDING_BOTTOM) * scaleFactor);

            if (tooltipPos.x + tooltipWidth + widthPaddings > screenWidth) {
                // FIXME: There are still edge cases where the tooltip is not fully visible
                tooltipPos.x = Math.max(tooltipPos.x - tooltipWidth - widthPaddings, TooltipRenderUtil.PADDING_LEFT);
            } else {
                tooltipPos.x = (int) ((tooltipPos.x + TooltipRenderUtil.PADDING_RIGHT + 1) / scaleFactor)
                        + TooltipRenderUtil.MOUSE_OFFSET;
            }

            if (tooltipPos.y + tooltipHeight + heightPaddings > screenHeight) {
                // FIXME: There are still edge cases where the tooltip is not fully visible
                tooltipPos.y = Math.max(tooltipPos.y - tooltipHeight - heightPaddings, TooltipRenderUtil.PADDING_TOP);
            } else {
                tooltipPos.y = (int) ((tooltipPos.y + TooltipRenderUtil.PADDING_BOTTOM + 1) / scaleFactor)
                        - TooltipRenderUtil.MOUSE_OFFSET;
            }

            return tooltipPos;
        }
    }
}
