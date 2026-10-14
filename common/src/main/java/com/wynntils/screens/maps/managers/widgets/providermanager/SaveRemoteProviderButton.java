package com.wynntils.screens.maps.managers.widgets.providermanager;

import com.mojang.blaze3d.platform.InputConstants;
import com.wynntils.core.components.Services;
import com.wynntils.core.text.StyledText;
import com.wynntils.screens.base.TooltipProvider;
import com.wynntils.screens.maps.managers.ProviderManagementScreen;
import com.wynntils.screens.maps.managers.widgets.TexturedTextInputBoxWidget;
import com.wynntils.services.mapdata.providers.json.JsonProviderInfo;
import com.wynntils.utils.MathUtils;
import com.wynntils.utils.colors.CommonColors;
import com.wynntils.utils.mc.McUtils;
import com.wynntils.utils.mc.RenderedStringUtils;
import com.wynntils.utils.render.FontRenderer;
import com.wynntils.utils.render.RenderUtils;
import com.wynntils.utils.render.Texture;
import com.wynntils.utils.render.type.HorizontalAlignment;
import com.wynntils.utils.render.type.TextShadow;
import com.wynntils.utils.render.type.VerticalAlignment;
import java.net.URI;
import java.util.ArrayList;
import java.util.List;
import net.minecraft.ChatFormatting;
import net.minecraft.client.gui.GuiGraphics;
import net.minecraft.client.gui.components.AbstractWidget;
import net.minecraft.client.gui.narration.NarrationElementOutput;
import net.minecraft.client.input.MouseButtonEvent;
import net.minecraft.network.chat.Component;

public class SaveRemoteProviderButton extends AbstractWidget implements TooltipProvider {
    private static final int TOOLTIP_WRAP_WIDTH = 210;

    private final ProviderManagementScreen parent;
    private final TexturedTextInputBoxWidget nameTextBox;
    private final TexturedTextInputBoxWidget urlTextBox;

    public SaveRemoteProviderButton(
            int x,
            int y,
            int width,
            int height,
            ProviderManagementScreen parent,
            TexturedTextInputBoxWidget nameTextBox,
            TexturedTextInputBoxWidget urlTextBox) {
        super(x, y, width, height, Component.literal("Save Remote Provider Button"));
        this.parent = parent;
        this.nameTextBox = nameTextBox;
        this.urlTextBox = urlTextBox;
        this.active = false;
    }

    @Override
    protected void renderWidget(GuiGraphics guiGraphics, int mouseX, int mouseY, float partialTick) {
        this.active = getValidationError() == null;
        handleCursor(guiGraphics);

        RenderUtils.drawNineSliceScalingTexturedRect(
                guiGraphics,
                this.active ? Texture.MANAGER_WIDGET_BACKGROUND_GREEN : Texture.MANAGER_WIDGET_BACKGROUND_DARKGREEN,
                getX(),
                getY(),
                getWidth(),
                getHeight());

        FontRenderer.getInstance()
                .renderText(
                        guiGraphics,
                        StyledText.fromComponent(Component.translatable(
                                "screens.wynntils.map.managers.providerManager.saveRemoteProviderButton.text")),
                        getX() + getWidth() / 2f,
                        getY() + getHeight() / 2f,
                        CommonColors.WHITE,
                        HorizontalAlignment.CENTER,
                        VerticalAlignment.MIDDLE,
                        TextShadow.NORMAL);
    }

    @Override
    public boolean mouseClicked(MouseButtonEvent event, boolean isDoubleClick) {
        if (!this.visible) return false;
        if (event.button() != InputConstants.MOUSE_BUTTON_LEFT) return false;
        if (!this.active) return false;

        this.playDownSound(McUtils.mc().getSoundManager());
        save();

        return true;
    }

    @Override
    public boolean isMouseOver(double mouseX, double mouseY) {
        return this.visible
                && MathUtils.isInside(
                (int) mouseX, (int) mouseY, getX(), getX() + getWidth(), getY(), getY() + getHeight());
    }

    @Override
    public List<Component> getTooltipLines() {
        List<Component> lines = new ArrayList<>();

        Component error = getValidationError();
        if (error == null) {
            return lines;
        }

        for (StyledText line : RenderedStringUtils.wrapTextBySize(StyledText.fromComponent(error), TOOLTIP_WRAP_WIDTH)) {
            lines.add(Component.empty().append(line.getComponent()).withStyle(ChatFormatting.RED));
        }

        return lines;
    }

    @Override
    protected void updateWidgetNarration(NarrationElementOutput narrationElementOutput) {}

    private void save() {
        Services.MapData.addJsonProvider(JsonProviderInfo.createRemote(getName(), getUrl()));

        nameTextBox.setTextBoxInput("");
        urlTextBox.setTextBoxInput("");

        parent.providerScrollBarWidget.populateWidgets();
    }

    private String getName() {
        return nameTextBox.getTextBoxInput().trim();
    }

    private String getUrl() {
        return urlTextBox.getTextBoxInput().trim();
    }

    private Component getValidationError() {
        String name = getName();

        if (name.isEmpty()) {
            return Component.translatable(
                    "screens.wynntils.map.managers.providerManager.saveRemoteProviderButton.error.nameEmpty");
        }

        if (Services.MapData.getJsonProviderInfos().containsKey(name)) {
            return Component.translatable(
                    "screens.wynntils.map.managers.providerManager.saveRemoteProviderButton.error.nameExists");
        }

        String url = getUrl();
        if (url.isEmpty()) {
            return Component.translatable(
                    "screens.wynntils.map.managers.providerManager.saveRemoteProviderButton.error.urlEmpty");
        }

        URI uri;
        try {
            uri = URI.create(url);
        } catch (IllegalArgumentException e) {
            return Component.translatable(
                    "screens.wynntils.map.managers.providerManager.saveRemoteProviderButton.error.urlInvalid");
        }

        String scheme = uri.getScheme();
        if (scheme == null || !(scheme.equals("http") || scheme.equals("https"))) {
            return Component.translatable(
                    "screens.wynntils.map.managers.providerManager.saveRemoteProviderButton.error.urlScheme");
        }

        if (uri.getHost() == null) {
            return Component.translatable(
                    "screens.wynntils.map.managers.providerManager.saveRemoteProviderButton.error.urlHost");
        }

        return null;
    }
}
