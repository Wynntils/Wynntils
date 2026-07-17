package com.wynntils.screens.buildloadouts.widgets;

import com.wynntils.core.text.StyledText;
import com.wynntils.core.text.fonts.WynnFont;
import com.wynntils.core.text.fonts.wynnfonts.WynncraftKeybindsFont;
import com.wynntils.screens.base.TooltipProvider;
import com.wynntils.screens.buildloadouts.BuildLoadoutsScreen;
import com.wynntils.services.loadout.type.LoadoutType;
import com.wynntils.utils.colors.CommonColors;
import com.wynntils.utils.render.FontRenderer;
import com.wynntils.utils.render.RenderUtils;
import com.wynntils.utils.render.Texture;
import com.wynntils.utils.render.type.HorizontalAlignment;
import com.wynntils.utils.render.type.TextShadow;
import com.wynntils.utils.render.type.VerticalAlignment;
import net.minecraft.ChatFormatting;
import net.minecraft.client.Minecraft;
import net.minecraft.client.gui.GuiGraphics;
import net.minecraft.client.gui.components.AbstractButton;
import net.minecraft.client.gui.narration.NarrationElementOutput;
import net.minecraft.client.input.InputWithModifiers;
import net.minecraft.client.input.MouseButtonEvent;
import net.minecraft.network.chat.Component;
import org.lwjgl.glfw.GLFW;

import java.util.ArrayList;
import java.util.Collections;
import java.util.List;

public class LoadoutMenuLoadButton extends AbstractButton implements TooltipProvider {
    private final int x;
    private final int y;
    private final BuildLoadoutsScreen parent;
    private List<Component> generatedTooltip = new ArrayList<>();
    private LoadType loadType = LoadType.BUILD;

    public LoadoutMenuLoadButton(int x, int y, BuildLoadoutsScreen parent) {
        super(x, y, 79, 20, Component.literal("Loadout Menu Load Button"));
        this.x = x;
        this.y = y;
        this.parent = parent;
        buildTooltip();
    }

    @Override
    protected void renderContents(GuiGraphics guiGraphics, int mouseX, int mouseY, float partialTick) {
        handleCursor(guiGraphics);

        RenderUtils.drawNineSliceScalingTexturedRect(
                guiGraphics,
                Texture.BUILD_LOADOUTS_WIDGET_BACKGROUND_GREEN,
                x,
                y,
                this.width,
                this.height);

            FontRenderer.getInstance()
                    .renderText(
                            guiGraphics,
                            StyledText.fromString("Load"),
                            (this.x + this.width / 2f),
                            (this.y + this.height / 2f),
                            CommonColors.WHITE,
                            HorizontalAlignment.CENTER,
                            VerticalAlignment.MIDDLE,
                            TextShadow.NORMAL);
    }

    @Override
    public void onPress(InputWithModifiers input) {}

    @Override
    public boolean mouseClicked(MouseButtonEvent event, boolean isDoubleClick) {
        if (event.button() == GLFW.GLFW_MOUSE_BUTTON_MIDDLE) return false;

        this.playDownSound(Minecraft.getInstance().getSoundManager());

        if (event.button() == GLFW.GLFW_MOUSE_BUTTON_RIGHT) {
            loadType = loadType.next();
            buildTooltip();
            return true;
        }

        return true;
    }

    @Override
    protected void updateWidgetNarration(NarrationElementOutput narrationElementOutput) {}

    @Override
    public List<Component> getTooltipLines() {
        return Collections.unmodifiableList(this.generatedTooltip);
    }

    private void buildTooltip() {
        this.generatedTooltip = new ArrayList<>();

        this.generatedTooltip.add(Component.literal("Load Category")
                .withStyle(ChatFormatting.GOLD));

        this.generatedTooltip.add(Component.literal("Choose what to load")
                .withStyle(ChatFormatting.DARK_GRAY));

        this.generatedTooltip.add(Component.empty());

        for (LoadType type : LoadType.values()) {
            boolean selected = type == loadType;
            ChatFormatting color = selected ? ChatFormatting.WHITE : ChatFormatting.GRAY;

            Component label = Component.literal(type.getDisplayName()).withStyle(color);

            this.generatedTooltip.add(Component.literal("- ")
                    .withStyle(ChatFormatting.GOLD)
                    .append(label));
        }

        this.generatedTooltip.add(Component.empty());

        this.generatedTooltip.add(Component.empty()
                .append(WynnFont.asFont("left_click", WynncraftKeybindsFont.class))
                .append(" ")
                .append(Component.literal("Left-Click to load").withStyle(ChatFormatting.GREEN)));

        this.generatedTooltip.add(Component.empty()
                .append(WynnFont.asFont("right_click", WynncraftKeybindsFont.class))
                .append(" ")
                .append(Component.literal("Right-Click to change category").withStyle(ChatFormatting.GREEN)));
    }

    public void updateLoadType() {
        if (parent.getCurrentCategory().getLoadoutType() == null) return;

        switch (parent.getCurrentCategory().getLoadoutType()) {
            case LoadoutType.BUILD -> loadType = LoadType.BUILD;
            case LoadoutType.ABILITY_TREE -> loadType = LoadType.ABILITY_TREE;
            case LoadoutType.ASPECT -> loadType = LoadType.ASPECTS;
            case LoadoutType.SKILL_POINT -> loadType = LoadType.SKILL_POINTS;
        }
        buildTooltip();
    }

    private enum LoadType {
        BUILD("Build"),
        ABILITY_TREE("Ability Tree"),
        ASPECTS("Aspects"),
        SKILL_POINTS("Skill Points");

        private static final LoadType[] VALUES = values();

        private final String displayName;

        LoadType(String displayName) {
            this.displayName = displayName;
        }

        public LoadType next() {
            return VALUES[(ordinal() + 1) % VALUES.length];
        }

        public String getDisplayName() {
            return displayName;
        }
    }
}
