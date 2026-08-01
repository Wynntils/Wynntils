package com.wynntils.features.wynntils;

import com.google.common.collect.Lists;
import com.wynntils.core.WynntilsMod;
import com.wynntils.core.components.Models;
import com.wynntils.core.consumers.features.Feature;
import com.wynntils.core.consumers.features.ProfileDefault;
import com.wynntils.core.persisted.config.Category;
import com.wynntils.core.persisted.config.ConfigCategory;
import com.wynntils.core.text.StyledText;
import com.wynntils.core.text.fonts.CommonFonts;
import com.wynntils.core.text.fonts.WynnFont;
import com.wynntils.core.text.fonts.wynnfonts.WynncraftKeybindsFont;
import com.wynntils.mc.event.ScreenOpenedEvent;
import com.wynntils.models.containers.containers.CharacterInfoContainer;
import com.wynntils.screens.base.TooltipProvider;
import com.wynntils.screens.buildloadouts.type.MenuCategory;
import com.wynntils.screens.buildloadouts.widgets.LoadoutMenuLoadButton;
import com.wynntils.utils.colors.CommonColors;
import com.wynntils.utils.mc.McUtils;
import com.wynntils.utils.render.FontRenderer;
import com.wynntils.utils.render.type.HorizontalAlignment;
import com.wynntils.utils.render.type.TextShadow;
import com.wynntils.utils.render.type.VerticalAlignment;
import net.minecraft.ChatFormatting;
import net.minecraft.client.gui.GuiGraphics;
import net.minecraft.client.gui.components.AbstractButton;
import net.minecraft.client.gui.narration.NarrationElementOutput;
import net.minecraft.client.gui.screens.inventory.ContainerScreen;
import net.minecraft.client.input.InputWithModifiers;
import net.minecraft.client.input.MouseButtonEvent;
import net.minecraft.network.chat.Component;
import net.minecraft.network.chat.Style;
import net.neoforged.bus.api.SubscribeEvent;
import org.lwjgl.glfw.GLFW;

import java.util.ArrayList;
import java.util.Collections;
import java.util.List;

@ConfigCategory(Category.WYNNTILS)
public class CharacterInfoIndicatorFeature extends Feature {

    public CharacterInfoIndicatorFeature() {
        super(ProfileDefault.ENABLED);
    }

    @SubscribeEvent
    public void onCharacterInfoScreenOpened(ScreenOpenedEvent.Post e) {
        if (!(e.getScreen() instanceof ContainerScreen screen)) return;
        if (!(Models.Container.getCurrentContainer() instanceof CharacterInfoContainer)) return;

        screen.addRenderableWidget(
                new InfoIndicatorButton(
                         screen.leftPos + screen.imageWidth + 26, screen.topPos));
    }


    private static final class InfoIndicatorButton extends AbstractButton implements TooltipProvider {
        private static final int BUTTON_WIDTH = 20;
        private static final int BUTTON_HEIGHT = 20;

        private List<Component> generatedTooltip = new ArrayList<>();

        public InfoIndicatorButton(int x, int y) {
            super(x, y, BUTTON_WIDTH, BUTTON_HEIGHT, Component.literal("Info Indicator Button"));
            buildTooltip();
        }

        @Override
        public void onPress(InputWithModifiers input) {}

        @Override
        protected void renderContents(GuiGraphics guiGraphics, int mouseX, int mouseY, float partialTick) {
            renderDefaultSprite(guiGraphics);

            FontRenderer.getInstance()
                    .renderText(
                            guiGraphics,
                            StyledText.fromComponent(Component.literal("\uE027").withStyle(Style.EMPTY.withFont(CommonFonts.COMMON_FONT))),
                            (this.getX() + this.width / 2f),
                            (this.getY() + this.height / 2f),
                            CommonColors.WHITE,
                            HorizontalAlignment.CENTER,
                            VerticalAlignment.MIDDLE,
                            TextShadow.NORMAL);

            if (isMouseOver(mouseX, mouseY)) {
                guiGraphics.setTooltipForNextFrame(
                        Lists.transform(getTooltipLines(), Component::getVisualOrderText),
                        mouseX,
                        mouseY);
            }
        }

        @Override
        public boolean mouseClicked(MouseButtonEvent event, boolean isDoubleClick) {
            if (event.button() != GLFW.GLFW_MOUSE_BUTTON_LEFT) return false;

            this.playDownSound(McUtils.mc().getSoundManager());

            WynntilsMod.info("clicked");

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

            this.generatedTooltip.add(
                    Component.translatable("feature.wynntils.characterInfoIndicator.tooltip.title")
                            .withStyle(ChatFormatting.GOLD));

            this.generatedTooltip.add(Component.empty());

            this.generatedTooltip.add(
                    Component.literal("- ").withStyle(ChatFormatting.GOLD)
                            .append(Component.translatable("feature.wynntils.characterInfoIndicator.tooltip.update")
                                    .withStyle(ChatFormatting.WHITE)));

            this.generatedTooltip.add(Component.empty());

            this.generatedTooltip.add(Component.empty()
                    .append(WynnFont.asFont("left_click", WynncraftKeybindsFont.class))
                    .append(" ")
                    .append(Component.translatable("feature.wynntils.characterInfoIndicator.tooltip.leftClick")
                            .withStyle(ChatFormatting.GREEN)));
        }
    }
}