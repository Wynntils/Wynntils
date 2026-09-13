package com.wynntils.screens.maps.managers.widgets.providermanager;

import com.wynntils.core.components.Services;
import com.wynntils.core.text.StyledText;
import com.wynntils.screens.maps.managers.ProviderManagementScreen;
import com.wynntils.screens.maps.managers.type.ProviderType;
import com.wynntils.services.mapdata.providers.builtin.BuiltInProvider;
import com.wynntils.services.mapdata.providers.json.JsonProviderInfo;
import com.wynntils.services.mapdata.providers.type.MapDataProvider;
import com.wynntils.utils.MathUtils;
import com.wynntils.utils.colors.CommonColors;
import com.wynntils.utils.render.FontRenderer;
import com.wynntils.utils.render.RenderUtils;
import com.wynntils.utils.render.Texture;
import com.wynntils.utils.render.type.HorizontalAlignment;
import com.wynntils.utils.render.type.TextShadow;
import com.wynntils.utils.render.type.VerticalAlignment;
import net.minecraft.client.Minecraft;
import net.minecraft.client.gui.GuiGraphics;
import net.minecraft.client.gui.components.AbstractWidget;
import net.minecraft.client.gui.narration.NarrationElementOutput;
import net.minecraft.client.input.MouseButtonEvent;
import net.minecraft.network.chat.Component;
import org.lwjgl.glfw.GLFW;

import java.util.Objects;
import java.util.Optional;

public class ProviderInfoWidget extends AbstractWidget {
    private final int PADDING = 5;

    private final ProviderManagementScreen parent;

    public ProviderInfoWidget(int x, int y, int width, int height, ProviderManagementScreen parent) {
        super(x, y, width, height, Component.literal("Provider Info Widget"));
        this.parent = parent;
    }

    @Override
    protected void renderWidget(GuiGraphics guiGraphics, int mouseX, int mouseY, float partialTick) {
        RenderUtils.drawNineSliceScalingTexturedRect(
                guiGraphics,
                Texture.MANAGER_WIDGET_BACKGROUND,
                getX(),
                getY(),
                this.width,
                this.height);

        int yOffset = PADDING * 2;
        String providerId = parent.getSelectedProviderId();
        Optional<MapDataProvider> provider = Services.MapData.getProviderFromId(providerId);

        FontRenderer.getInstance()
                .renderText(
                        guiGraphics,
                        StyledText.fromString(providerId),
                        getX() + PADDING,
                        getY() + yOffset,
                        CommonColors.WHITE,
                        HorizontalAlignment.LEFT,
                        VerticalAlignment.MIDDLE,
                        TextShadow.NORMAL);

        yOffset += 10 + PADDING;

        if (provider.isPresent()) {
            FontRenderer.getInstance()
                    .renderText(
                            guiGraphics,
                            StyledText.fromString("Categories: " + provider.get().getCategories().count()),
                            getX() + PADDING,
                            getY() + yOffset,
                            CommonColors.WHITE,
                            HorizontalAlignment.LEFT,
                            VerticalAlignment.MIDDLE,
                            TextShadow.NORMAL);

            yOffset += 10 + PADDING;

            FontRenderer.getInstance()
                    .renderText(
                            guiGraphics,
                            StyledText.fromString("Features: " + provider.get().getFeatures().count()),
                            getX() + PADDING,
                            getY() + yOffset,
                            CommonColors.WHITE,
                            HorizontalAlignment.LEFT,
                            VerticalAlignment.MIDDLE,
                            TextShadow.NORMAL);

            yOffset += 10 + PADDING;

            FontRenderer.getInstance()
                    .renderText(
                            guiGraphics,
                            StyledText.fromString("Icons: " + provider.get().getIcons().count()),
                            getX() + PADDING,
                            getY() + yOffset,
                            CommonColors.WHITE,
                            HorizontalAlignment.LEFT,
                            VerticalAlignment.MIDDLE,
                            TextShadow.NORMAL);
            }
    }

    @Override
    public boolean mouseClicked(MouseButtonEvent event, boolean isDoubleClick) {
        if (event.button() != GLFW.GLFW_MOUSE_BUTTON_LEFT) return false;

        return false;
    }

    @Override
    protected void updateWidgetNarration(NarrationElementOutput narrationElementOutput) {}

}