/*
 * Copyright © Wynntils 2022.
 * This file is released under AGPLv3. See LICENSE for full license details.
 */
package com.wynntils.gui.widgets;

import com.mojang.blaze3d.vertex.PoseStack;
import com.wynntils.core.managers.Managers;
import com.wynntils.gui.render.FontRenderer;
import com.wynntils.gui.render.HorizontalAlignment;
import com.wynntils.gui.render.RenderUtils;
import com.wynntils.gui.render.Texture;
import com.wynntils.gui.render.VerticalAlignment;
import com.wynntils.mc.objects.CommonColors;
import com.wynntils.mc.objects.CustomColor;
import com.wynntils.utils.StringUtils;
import com.wynntils.wynn.model.discoveries.objects.DiscoveryInfo;
import com.wynntils.wynn.model.discoveries.objects.DiscoveryType;
import java.util.ArrayList;
import java.util.List;
import net.minecraft.ChatFormatting;
import net.minecraft.client.gui.components.AbstractButton;
import net.minecraft.client.gui.narration.NarrationElementOutput;
import net.minecraft.network.chat.Component;
import net.minecraft.network.chat.TextComponent;
import net.minecraft.network.chat.TranslatableComponent;
import org.lwjgl.glfw.GLFW;

public class DiscoveryButton extends AbstractButton {
    private static final CustomColor BUTTON_COLOR = new CustomColor(181, 174, 151);
    private static final CustomColor BUTTON_COLOR_HOVERED = new CustomColor(121, 116, 101);

    private final DiscoveryInfo discoveryInfo;

    public DiscoveryButton(int x, int y, int width, int height, DiscoveryInfo discoveryInfo) {
        super(x, y, width, height, new TextComponent("Discovery Button"));
        this.discoveryInfo = discoveryInfo;
    }

    @Override
    public void renderButton(PoseStack poseStack, int mouseX, int mouseY, float partialTick) {
        CustomColor backgroundColor = this.isHovered ? BUTTON_COLOR_HOVERED : BUTTON_COLOR;

        RenderUtils.drawRect(poseStack, backgroundColor, this.x, this.y, 0, this.width, this.height);

        int maxTextWidth = this.width - 10 - 11;
        FontRenderer.getInstance()
                .renderText(
                        poseStack,
                        StringUtils.getMaxFittingText(
                                discoveryInfo.getName(),
                                maxTextWidth,
                                FontRenderer.getInstance().getFont()),
                        this.x + 14,
                        this.y + 1,
                        0,
                        CommonColors.BLACK,
                        HorizontalAlignment.Left,
                        VerticalAlignment.Top,
                        FontRenderer.TextShadow.NONE);

        Texture stateTexture = discoveryInfo.isDiscovered()
                ? switch (discoveryInfo.getType()) {
                    case TERRITORY -> Texture.DISCOVERED_TERRITORY_ICON;
                    case WORLD -> Texture.DISCOVERED_WORLD_ICON;
                    case SECRET -> Texture.DISCOVERED_SECRET_ICON;
                }
                : switch (discoveryInfo.getType()) {
                    case TERRITORY -> Texture.UNDISCOVERED_TERRITORY_ICON;
                    case WORLD -> Texture.UNDISCOVERED_WORLD_ICON;
                    case SECRET -> Texture.UNDISCOVERED_SECRET_ICON;
                };

        RenderUtils.drawTexturedRect(
                poseStack,
                stateTexture.resource(),
                this.x + 1,
                this.y + 1,
                stateTexture.width(),
                stateTexture.height(),
                stateTexture.width(),
                stateTexture.height());
    }

    @Override
    public boolean mouseClicked(double mouseX, double mouseY, int button) {
        if (button == GLFW.GLFW_MOUSE_BUTTON_LEFT) {
            Managers.Discovery.setDiscoveryCompass(discoveryInfo);
        } else if (button == GLFW.GLFW_MOUSE_BUTTON_MIDDLE) {
            Managers.Discovery.openDiscoveryOnMap(discoveryInfo);
        } else if (button == GLFW.GLFW_MOUSE_BUTTON_RIGHT && discoveryInfo.getType() == DiscoveryType.SECRET) {
            Managers.Discovery.openSecretDiscoveryWiki(discoveryInfo);
        }

        return true;
    }

    // not called
    @Override
    public void onPress() {}

    @Override
    public void updateNarration(NarrationElementOutput narrationElementOutput) {}

    public List<Component> getTooltipLines() {
        List<Component> lines = new ArrayList<>(discoveryInfo.getLore());

        // We need to inject requirements into lore here, as we only have updated discovery info here.
        if (!discoveryInfo.getRequirements().isEmpty()) {
            List<String> unmet = discoveryInfo.getRequirements().stream()
                    .filter(requirement -> Managers.Discovery.getAllDiscoveries()
                            .noneMatch(discovery -> discovery.getName().equals(requirement)))
                    .toList();

            if (!unmet.isEmpty()) {
                lines.add(TextComponent.EMPTY);
                lines.add(new TranslatableComponent("screens.wynntils.wynntilsDiscoveries.requirements")
                        .withStyle(ChatFormatting.DARK_AQUA));
                unmet.forEach(requirement -> lines.add(new TextComponent(" - ")
                        .withStyle(ChatFormatting.RED)
                        .append(new TextComponent(requirement).withStyle(ChatFormatting.GRAY))));
            }
        }

        if (discoveryInfo.getType() == DiscoveryType.SECRET
                || Managers.Territory.getTerritoryProfile(discoveryInfo.getName()) != null) {
            lines.add(TextComponent.EMPTY);
            lines.add(new TranslatableComponent("screens.wynntils.wynntilsDiscoveries.leftClickToSetCompass")
                    .withStyle(ChatFormatting.BOLD)
                    .withStyle(ChatFormatting.GREEN));
            lines.add(new TranslatableComponent("screens.wynntils.wynntilsDiscoveries.middleClickToOpenOnMap")
                    .withStyle(ChatFormatting.BOLD)
                    .withStyle(ChatFormatting.YELLOW));
        }

        if (discoveryInfo.getType() == DiscoveryType.SECRET) {
            lines.add(new TranslatableComponent("screens.wynntils.wynntilsDiscoveries.rightClickToOpenWiki")
                    .withStyle(ChatFormatting.BOLD)
                    .withStyle(ChatFormatting.GOLD));
        }

        return lines;
    }
}
