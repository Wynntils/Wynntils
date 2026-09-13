package com.wynntils.screens.maps.managers.widgets.providermanager;

import com.wynntils.core.text.StyledText;
import com.wynntils.screens.maps.managers.ProviderManagementScreen;
import com.wynntils.screens.maps.managers.type.OptionCategory;
import com.wynntils.screens.maps.managers.type.ProviderType;
import com.wynntils.services.mapdata.providers.builtin.BuiltInProvider;
import com.wynntils.services.mapdata.providers.json.JsonProviderInfo;
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

public class ProviderEntryWidget extends AbstractProviderWidget {
    private static final int PADDING = 5;

    private final ProviderManagementScreen parent;

    public boolean enabled = true;

    private final BuiltInProvider builtInProvider;
    private final JsonProviderInfo jsonProviderInfo;
    private final String providerId;

    private ProviderEntryWidget(
            BuiltInProvider builtInProvider,
            JsonProviderInfo jsonProviderInfo,
            ProviderType providerType,
            ProviderManagementScreen parent) {
        super(Component.literal("Provider Entry Widget"), 28, providerType);
        this.builtInProvider = builtInProvider;
        this.jsonProviderInfo = jsonProviderInfo;
        this.providerId = builtInProvider != null ? builtInProvider.getProviderId() : jsonProviderInfo.providerId();
        message = Component.literal(this.providerId);
        this.parent = parent;
    }

    public static ProviderEntryWidget createBuiltInProviderEntry(
            BuiltInProvider provider,
            ProviderManagementScreen parent) {
        return new ProviderEntryWidget(provider, null, ProviderType.BUILT_IN, parent);
    }

    public static ProviderEntryWidget createJsonProviderEntry(
            JsonProviderInfo provider,
            ProviderType providerType,
            ProviderManagementScreen parent) {
        return new ProviderEntryWidget(null, provider, providerType, parent);
    }

    @Override
    protected void renderWidget(GuiGraphics guiGraphics, int mouseX, int mouseY, float partialTick) {
        handleCursor(guiGraphics);

        RenderUtils.drawNineSliceScalingTexturedRect(
                guiGraphics,
                !Objects.equals(parent.getSelectedProviderId(), providerId)
                        ? Texture.MANAGER_WIDGET_BACKGROUND
                        : Texture.MANAGER_WIDGET_BACKGROUND_RED,
                getX(),
                getY(),
                this.width,
                this.height);

        FontRenderer.getInstance()
                .renderText(
                        guiGraphics,
                        StyledText.fromString(providerId),
                        getX() + PADDING,
                        getY() + this.height / 2f,
                        CommonColors.WHITE,
                        HorizontalAlignment.LEFT,
                        VerticalAlignment.MIDDLE,
                        TextShadow.NORMAL);

        RenderUtils.drawTexturedRect(
                guiGraphics,
                getButtonTexture(),
                getButtonX(),
                getY() + (this.height - getButtonTexture().height()) / 2f);
    }

    @Override
    public boolean mouseClicked(MouseButtonEvent event, boolean isDoubleClick) {
        if (event.button() != GLFW.GLFW_MOUSE_BUTTON_LEFT) return false;

        if (isMouseInsideButton(event.x(), event.y())) {
            this.playDownSound(Minecraft.getInstance().getSoundManager());

            enabled = !enabled;

            return true;
        } else {
            this.playDownSound(Minecraft.getInstance().getSoundManager());

            parent.setSelectedProvider(providerId, providerType);
        }

        return false;
    }

    @Override
    protected void updateWidgetNarration(NarrationElementOutput narrationElementOutput) {}

    private Texture getButtonTexture() {
        return enabled ? Texture.MANAGER_TOGGLE_BUTTON_ON : Texture.MANAGER_TOGGLE_BUTTON_OFF;
    }

    private int getButtonX() {
        return getX() + getWidth() - getButtonTexture().width() - PADDING;
    }

    private boolean isMouseInsideButton(double mouseX, double mouseY) {
        int buttonX = getButtonX();
        int buttonY = getY() + (this.height - getButtonTexture().height()) / 2;

        return MathUtils.isInside(
                (int) mouseX,
                (int) mouseY,
                buttonX,
                buttonX + getButtonTexture().width(),
                buttonY,
                buttonY + getButtonTexture().height());
    }
}