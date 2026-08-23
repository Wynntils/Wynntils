package com.wynntils.screens.maps.managers.widgets;

import com.wynntils.core.components.Services;
import com.wynntils.core.text.StyledText;
import com.wynntils.screens.base.TooltipProvider;
import com.wynntils.screens.maps.managers.CategoryManagementScreen;
import com.wynntils.utils.colors.CommonColors;
import com.wynntils.utils.mc.RenderedStringUtils;
import com.wynntils.utils.render.FontRenderer;
import com.wynntils.utils.render.RenderUtils;
import com.wynntils.utils.render.Texture;
import com.wynntils.utils.render.type.HorizontalAlignment;
import com.wynntils.utils.render.type.TextShadow;
import com.wynntils.utils.render.type.VerticalAlignment;
import net.minecraft.ChatFormatting;
import net.minecraft.client.Minecraft;
import net.minecraft.client.gui.GuiGraphics;
import net.minecraft.client.gui.components.AbstractWidget;
import net.minecraft.client.gui.narration.NarrationElementOutput;
import net.minecraft.client.input.MouseButtonEvent;
import net.minecraft.network.chat.Component;
import org.lwjgl.glfw.GLFW;

import java.util.ArrayList;
import java.util.Collections;
import java.util.List;
import java.util.Locale;

public class DeleteButtonWidget extends AbstractWidget implements TooltipProvider {
    private final int x;
    private final int y;
    private final CategoryManagementScreen parent;
    private List<Component> generatedTooltip = new ArrayList<>();

    public DeleteButtonWidget(int x, int y, int width, int height, CategoryManagementScreen parent) {
        super(x, y, width, height, Component.literal("Delete Button Widget"));
        this.x = x;
        this.y = y;
        this.parent = parent;
        generateTooltip();
    }

    @Override
    protected void renderWidget(GuiGraphics guiGraphics, int mouseX, int mouseY, float partialTick) {
        handleCursor(guiGraphics);

        RenderUtils.drawNineSliceScalingTexturedRect(
                guiGraphics,
                Texture.MANAGER_WIDGET_BACKGROUND_RED,
                x,
                y,
                this.width,
                this.height);

        FontRenderer.getInstance()
                .renderText(
                        guiGraphics,
                        StyledText.fromComponent(Component.translatable("screens.wynntils.map.managers.categoryManager.deleteButton.label")),
                        x + this.width / 2f,
                        y + this.height / 2f,
                        CommonColors.WHITE,
                        HorizontalAlignment.CENTER,
                        VerticalAlignment.MIDDLE,
                        TextShadow.NORMAL);
    }

    @Override
    public boolean mouseClicked(MouseButtonEvent event, boolean isDoubleClick) {
        if (event.button() != GLFW.GLFW_MOUSE_BUTTON_LEFT) return false;

        this.playDownSound(Minecraft.getInstance().getSoundManager());

        Services.MapData.removeOverrideProvider(
                "json-override:" +
                        parent.getSelectedOverrideType().name().toLowerCase(Locale.ROOT) +
                        ":" +
                        parent.getSelectedCategory());
        parent.setSelectedCategory(parent.getSelectedCategory());

        return true;
    }

    @Override
    protected void updateWidgetNarration(NarrationElementOutput narrationElementOutput) {}

    @Override
    public List<Component> getTooltipLines() {
        return Collections.unmodifiableList(this.generatedTooltip);
    }

    public void generateTooltip() {
        this.generatedTooltip = new ArrayList<>();

        this.generatedTooltip.add(Component.translatable("screens.wynntils.map.managers.categoryManager.deleteButton.label")
                .withStyle(ChatFormatting.GOLD));

        boolean overrideExists = Services.MapData.getOverrideProvider(
                "json-override:"
                        + parent.getSelectedOverrideType().name().toLowerCase(Locale.ROOT)
                        + ":"
                        + parent.getSelectedCategory()) != null;

        StyledText description = StyledText.fromComponent(Component.translatable(overrideExists
                ? "screens.wynntils.map.managers.categoryManager.deleteButton.description"
                : "screens.wynntils.map.managers.categoryManager.deleteButton.description.empty"));

        for (StyledText line : RenderedStringUtils.wrapTextBySize(description, 210)) {
            this.generatedTooltip.add(Component.empty()
                    .append(line.getComponent())
                    .withStyle(overrideExists ? ChatFormatting.GRAY : ChatFormatting.DARK_GRAY));
        }
    }

}
