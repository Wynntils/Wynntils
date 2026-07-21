package com.wynntils.screens.buildloadouts.widgets;

import com.wynntils.core.WynntilsMod;
import com.wynntils.core.components.Models;
import com.wynntils.core.text.StyledText;
import com.wynntils.models.character.type.SavableSkillPointSet;
import com.wynntils.models.character.type.SavableTome;
import com.wynntils.models.items.FakeItemStack;
import com.wynntils.models.items.WynnItem;
import com.wynntils.models.items.items.game.CraftedGearItem;
import com.wynntils.models.items.items.game.GearItem;
import com.wynntils.models.items.items.game.TomeItem;
import com.wynntils.models.rewards.type.TomeType;
import com.wynntils.screens.buildloadouts.BuildLoadoutsScreen;
import com.wynntils.services.loadout.type.Loadout;
import com.wynntils.utils.EncodedByteBuffer;
import com.wynntils.utils.colors.CommonColors;
import com.wynntils.utils.colors.CustomColor;
import com.wynntils.utils.render.FontRenderer;
import com.wynntils.utils.render.RenderUtils;
import com.wynntils.utils.render.Texture;
import com.wynntils.utils.render.type.HorizontalAlignment;
import com.wynntils.utils.render.type.TextShadow;
import com.wynntils.utils.render.type.VerticalAlignment;
import com.wynntils.utils.type.ErrorOr;
import net.minecraft.client.gui.GuiGraphics;
import net.minecraft.client.gui.components.AbstractWidget;
import net.minecraft.client.gui.narration.NarrationElementOutput;
import net.minecraft.client.input.MouseButtonEvent;
import net.minecraft.network.chat.Component;
import net.minecraft.world.item.ItemStack;

import java.util.ArrayList;
import java.util.List;
import java.util.Optional;
import java.util.regex.Matcher;

public class LoadoutMenuItemWidget extends AbstractWidget implements ItemTooltipProvider {
    private static final CustomColor BOX_BORDER_COLOR = CustomColor.fromInt(0x654f3c);
    private static final int BOX_SIZE = 16;
    private static final int BOXES_PER_ROW = 5;

    private final int x;
    private final int y;
    private final BuildLoadoutsScreen parent;
    private Loadout cachedLoadout;
    private List<ItemStack> cachedItemStacks = List.of();

    public LoadoutMenuItemWidget(int x, int y, BuildLoadoutsScreen parent) {
        super(x, y, 98, 68, Component.literal("Loadout Menu Item Widget"));
        this.x = x;
        this.y = y;
        this.parent = parent;
    }

    @Override
    protected void renderWidget(GuiGraphics guiGraphics, int mouseX, int mouseY, float partialTick) {
        RenderUtils.drawNineSliceScalingTexturedRect(
                guiGraphics,
                Texture.BUILD_LOADOUTS_WIDGET_BACKGROUND_LIGHT,
                x,
                y,
                this.width,
                this.height);

        List<ItemStack> boxItemStacks = getBoxItemStacks();

        // Bottom row: 5 boxes (tome + armour, indices 5-9)
        for (int i = 0; i < BOXES_PER_ROW; i++) {
            int[] pos = getBoxPosition(1, i);
            int bx = pos[0];
            int by = pos[1];

            RenderUtils.drawRectBorders(
                    guiGraphics,
                    BOX_BORDER_COLOR,
                    bx, by,
                    bx + BOX_SIZE, by + BOX_SIZE,
                    1);

            renderBoxItem(guiGraphics, boxItemStacks, BOXES_PER_ROW + i, bx, by);
        }

        // Top row: 5 boxes (weapon + accessories, indices 0-4)
        for (int i = 0; i < BOXES_PER_ROW; i++) {
            int[] pos = getBoxPosition(0, i);
            int bx = pos[0];
            int by = pos[1];

            RenderUtils.drawRectBorders(
                    guiGraphics,
                    BOX_BORDER_COLOR,
                    bx, by,
                    bx + BOX_SIZE, by + BOX_SIZE,
                    1);

            renderBoxItem(guiGraphics, boxItemStacks, i, bx, by);
        }

        RenderUtils.drawNineSliceScalingTexturedRect(
                guiGraphics,
                Texture.BUILD_LOADOUTS_MENU_RIBBON,
                x + 6,
                y + 2,
                this.width - 12,
                17);

        FontRenderer.getInstance()
                .renderText(
                        guiGraphics,
                        StyledText.fromString("Items"),
                        this.x + 5 + (this.width - 10) / 2f,
                        this.y + 11,
                        CommonColors.WHITE,
                        HorizontalAlignment.CENTER,
                        VerticalAlignment.MIDDLE,
                        TextShadow.NORMAL);
    }

    @Override
    public boolean mouseClicked(MouseButtonEvent event, boolean isDoubleClick) {
        return false;
    }

    private int[] getBoxPosition(int row, int column) {
        int bx = x + 3 + 19 * column;
        int by = row == 0
                ? y + this.height - BOX_SIZE * 2 - 10
                : y + this.height - BOX_SIZE - 7;
        return new int[] {bx, by};
    }

    private void renderBoxItem(GuiGraphics guiGraphics, List<ItemStack> boxItemStacks, int index, int bx, int by) {
        ItemStack itemStack = index < boxItemStacks.size() ? boxItemStacks.get(index) : ItemStack.EMPTY;

        if (itemStack.isEmpty()) {
            RenderUtils.drawLine(guiGraphics, BOX_BORDER_COLOR, bx, by, bx + BOX_SIZE, by + BOX_SIZE, 1);
            RenderUtils.drawLine(guiGraphics, BOX_BORDER_COLOR, bx + BOX_SIZE, by, bx, by + BOX_SIZE, 1);
            return;
        }

        RenderUtils.renderItem(guiGraphics, itemStack, bx, by);
    }

    @Override
    public void renderHoveredItemTooltip(GuiGraphics guiGraphics, int mouseX, int mouseY) {
        List<ItemStack> boxItemStacks = getBoxItemStacks();

        int hoveredIndex = getHoveredBoxIndex(mouseX, mouseY);
        if (hoveredIndex == -1 || hoveredIndex >= boxItemStacks.size()) return;

        ItemStack hoveredStack = boxItemStacks.get(hoveredIndex);
        if (hoveredStack.isEmpty()) return;

        RenderUtils.renderTooltip(guiGraphics, hoveredStack, mouseX, mouseY);
    }

    private int getHoveredBoxIndex(int mouseX, int mouseY) {
        for (int i = 0; i < BOXES_PER_ROW; i++) {
            int[] pos = getBoxPosition(0, i);
            if (isInsideBox(mouseX, mouseY, pos[0], pos[1])) return i;
        }

        for (int i = 0; i < BOXES_PER_ROW; i++) {
            int[] pos = getBoxPosition(1, i);
            if (isInsideBox(mouseX, mouseY, pos[0], pos[1])) return BOXES_PER_ROW + i;
        }

        return -1;
    }

    private boolean isInsideBox(int mouseX, int mouseY, int boxX, int boxY) {
        return mouseX >= boxX && mouseX < boxX + BOX_SIZE && mouseY >= boxY && mouseY < boxY + BOX_SIZE;
    }

    private Optional<ItemStack> decodeItemStack(String stored) {
        if (stored == null || stored.isEmpty()) return Optional.empty();

        Matcher matcher = Models.ItemEncoding.getEncodedDataPattern().matcher(stored);
        if (!matcher.matches()) return Optional.empty();

        EncodedByteBuffer encodedByteBuffer = EncodedByteBuffer.fromUtf16String(matcher.group("data"));
        String itemName = matcher.group("name");

        ErrorOr<WynnItem> errorOrItem = Models.ItemEncoding.decodeItem(encodedByteBuffer, itemName);
        if (errorOrItem.hasError()) return Optional.empty();

        WynnItem item = errorOrItem.getValue();
        if (item instanceof GearItem gearItem) {
            return Optional.of(new FakeItemStack(gearItem, "From loadout"));
        }
        if (item instanceof CraftedGearItem craftedGearItem) {
            return Optional.of(new FakeItemStack(craftedGearItem, "From loadout"));
        }
        if (item instanceof TomeItem tomeItem) {
            return Optional.of(new FakeItemStack(tomeItem, "From loadout"));
        }

        return Optional.empty();
    }

    private List<ItemStack> computeBoxItemStacks(Loadout selectedLoadout) {
        if (selectedLoadout == null || !selectedLoadout.hasSkillPoints()) return List.of();

        SavableSkillPointSet skillPoints = selectedLoadout.skillPoints();

        List<String> orderedSlots = new ArrayList<>();
        orderedSlots.add(skillPoints.weapon());
        orderedSlots.addAll(skillPoints.armourNames());
        orderedSlots.add(getSkillPointTomeEncoded(selectedLoadout));
        orderedSlots.addAll(skillPoints.accessoryNames());

        return orderedSlots.stream()
                .map(this::decodeItemStack)
                .map(opt -> opt.orElse(ItemStack.EMPTY))
                .toList();
    }

    private String getSkillPointTomeEncoded(Loadout selectedLoadout) {
        if (selectedLoadout.tomes() == null) return null;

        return selectedLoadout.tomes().getTomes(TomeType.GUILD_TOME).stream()
                .findFirst()
                .map(SavableTome::encoded)
                .orElse(null);
    }

    private List<ItemStack> getBoxItemStacks() {
        Loadout selectedLoadout = parent.getSelectedLoadout();
        if (selectedLoadout != cachedLoadout) {
            cachedLoadout = selectedLoadout;
            cachedItemStacks = computeBoxItemStacks(selectedLoadout);
        }
        return cachedItemStacks;
    }

    @Override
    protected void updateWidgetNarration(NarrationElementOutput narrationElementOutput) {}
}