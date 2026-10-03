/*
 * Copyright © Wynntils 2022-2026.
 * This file is released under LGPLv3. See LICENSE for full license details.
 */
package com.wynntils.screens.guides;

import com.wynntils.core.components.Handlers;
import com.wynntils.core.components.Managers;
import com.wynntils.core.text.StyledText;
import com.wynntils.core.text.fonts.CommonFonts;
import com.wynntils.handlers.item.ItemAnnotation;
import com.wynntils.models.wynnitem.type.ItemObtainInfo;
import com.wynntils.utils.colors.CustomColor;
import com.wynntils.utils.mc.LoreUtils;
import com.wynntils.utils.mc.McUtils;
import com.wynntils.utils.render.FontRenderer;
import java.util.ArrayList;
import java.util.List;
import net.minecraft.ChatFormatting;
import net.minecraft.client.gui.GuiGraphicsExtractor;
import net.minecraft.core.component.DataComponents;
import net.minecraft.network.chat.Component;
import net.minecraft.network.chat.MutableComponent;
import net.minecraft.network.chat.Style;
import net.minecraft.world.item.ItemStack;

public abstract class GuideItemStack extends ItemStack {
    private static final CustomColor CURRENT_PAGE = new CustomColor(255, 234, 128);
    private static final CustomColor INACTIVE_PAGE = new CustomColor(69, 84, 73);

    protected GuideItemStack(ItemStack itemStack, ItemAnnotation annotation, String baseName) {
        super(itemStack.getItem(), 1);
        this.applyComponents(itemStack.getComponentsPatch());
        Handlers.Item.updateItem(this, annotation, StyledText.fromString(baseName));
    }

    public void queueGuideTooltip(GuiGraphicsExtractor guiGraphics, int mouseX, int mouseY) {
        List<Component> tooltipLines = new ArrayList<>(LoreUtils.getTooltipLines(this));

        guiGraphics.setTooltipForNextFrame(
                FontRenderer.getInstance().getFont(),
                tooltipLines,
                this.getTooltipImage(),
                mouseX,
                mouseY,
                this.get(DataComponents.TOOLTIP_STYLE));
    }

    protected List<Component> buildObtainInfoPage(List<ItemObtainInfo> itemObtainInfos) {
        List<Component> obtainLines = new ArrayList<>();

        obtainLines.add(Component.literal("Obtain from:")
                .withStyle(Style.EMPTY
                        .withFont(CommonFonts.LANGUAGE_WYNNCRAFT_FONT)
                        .withColor(ChatFormatting.GRAY)));
        for (ItemObtainInfo obtainInfo : itemObtainInfos) {
            MutableComponent obtainSourceType =
                    Component.literal(obtainInfo.sourceType().getDisplayName());
            if (obtainInfo.name().isPresent()) {
                obtainSourceType.append(Component.literal(": ").withStyle(ChatFormatting.GRAY));
                obtainSourceType.append(
                        Component.literal(obtainInfo.name().get()).withStyle(ChatFormatting.YELLOW));
            }
            obtainLines.add(obtainSourceType);
        }

        return obtainLines;
    }

    protected List<Component> buildPaginationLines(int currentPage, int pageCount, int widestLine) {
        List<Component> paginationLines = new ArrayList<>();
        MutableComponent paginationLine = Component.empty()
                .append(Component.literal("\uDB00\uDC30").withStyle(Style.EMPTY.withFont(CommonFonts.SPACE_FONT)))
                .append(Component.literal("\uE001")
                        .withStyle(Style.EMPTY.withFont(CommonFonts.WYNNTILS_TOOLTIP_ICONS)));

        MutableComponent paginationButtons = Component.empty();

        for (int i = 0; i < pageCount; i++) {
            paginationButtons.append(Component.literal("\uE000")
                    .withStyle(Style.EMPTY
                            .withColor((currentPage == i ? CURRENT_PAGE : INACTIVE_PAGE).asInt())
                            .withFont(CommonFonts.TOOLTIP_PAGE_FONT))
                    .withoutShadow());

            if (i != pageCount - 1) {
                paginationButtons.append(
                        Component.literal("\uDB00\uDC04").withStyle(Style.EMPTY.withFont(CommonFonts.SPACE_FONT)));
            }
        }

        MutableComponent temp = Component.empty().append(paginationLine).append(paginationButtons);

        int width = McUtils.mc().font.width(temp);
        String keySpacing = Managers.Font.calculateOffset(width, 42);

        paginationLine = paginationLine
                .append(Component.literal(keySpacing).withStyle(Style.EMPTY.withFont(CommonFonts.SPACE_FONT)))
                .append(paginationButtons);

        int target = widestLine / 2;
        int currentWidth = McUtils.mc().font.width(paginationLine);
        String spacing = Managers.Font.calculateOffset(currentWidth, target);
        Component paddedFooter = Component.literal(spacing)
                .withStyle(Style.EMPTY.withFont(CommonFonts.SPACE_FONT))
                .append(paginationLine);

        paginationLines.add(paddedFooter);
        paginationLines.add(Component.empty());

        return paginationLines;
    }
}
