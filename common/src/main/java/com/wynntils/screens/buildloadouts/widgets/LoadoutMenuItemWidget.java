package com.wynntils.screens.buildloadouts.widgets;

import com.wynntils.core.WynntilsMod;
import com.wynntils.core.components.Models;
import com.wynntils.core.text.StyledText;
import com.wynntils.models.character.type.SavableSkillPointSet;
import com.wynntils.models.gear.type.GearInfo;
import com.wynntils.models.items.FakeItemStack;
import com.wynntils.models.items.WynnItem;
import com.wynntils.models.items.encoding.type.EncodingSettings;
import com.wynntils.models.items.items.game.GearItem;
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
import net.minecraft.client.Minecraft;
import net.minecraft.client.gui.GuiGraphics;
import net.minecraft.client.gui.components.AbstractWidget;
import net.minecraft.client.gui.narration.NarrationElementOutput;
import net.minecraft.client.input.MouseButtonEvent;
import net.minecraft.network.chat.Component;
import net.minecraft.world.item.ItemStack;
import org.lwjgl.glfw.GLFW;

import java.util.ArrayList;
import java.util.List;
import java.util.Optional;
import java.util.regex.Matcher;

public class LoadoutMenuItemWidget extends AbstractWidget {
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

        // Bottom row: 4 boxes (armour, indices 5-8)
        for (int i = 0; i < 4; i++) {
            int bx = x + 3 + 19 + 19 * i;
            int by = y + this.height - 16 - 7;

            RenderUtils.drawRectBorders(
                    guiGraphics,
                    CustomColor.fromInt(0x654f3c),
                    bx, by,
                    bx + 16, by + 16,
                    1);

            renderBoxItem(guiGraphics, boxItemStacks, 5 + i, bx, by);
        }

        // Top row: 5 boxes (weapon + accessories, indices 0-4)
        for (int i = 0; i < 5; i++) {
            int bx = x + 3 + 19 * i;
            int by = y + this.height - 16 * 2 - 10;

            RenderUtils.drawRectBorders(
                    guiGraphics,
                    CustomColor.fromInt(0x654f3c),
                    bx, by,
                    bx + 16, by + 16,
                    1);

            renderBoxItem(guiGraphics, boxItemStacks, i, bx, by);
        }

        RenderUtils.drawNineSliceScalingTexturedRect(
                guiGraphics,
                Texture.BUILD_LOADOUTS_MENU_RIBBON,
                x + 5,
                y + 2,
                this.width - 10,
                20);

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

        // Tooltip must render last so it draws on top of everything else
        renderHoveredTooltip(guiGraphics, boxItemStacks, mouseX, mouseY);
    }

    @Override
    public boolean mouseClicked(MouseButtonEvent event, boolean isDoubleClick) {
        return false;
    }

    private void renderBoxItem(GuiGraphics guiGraphics, List<ItemStack> boxItemStacks, int index, int bx, int by) {
        if (index >= boxItemStacks.size()) return;

        ItemStack itemStack = boxItemStacks.get(index);
        if (itemStack.isEmpty()) return;

        guiGraphics.renderItem(itemStack, bx, by);
        guiGraphics.renderItemDecorations(Minecraft.getInstance().font, itemStack, bx, by);
    }

    private void renderHoveredTooltip(GuiGraphics guiGraphics, List<ItemStack> boxItemStacks, int mouseX, int mouseY) {
        int hoveredIndex = getHoveredBoxIndex(mouseX, mouseY);
        if (hoveredIndex == -1 || hoveredIndex >= boxItemStacks.size()) return;

        ItemStack hoveredStack = boxItemStacks.get(hoveredIndex);
        if (hoveredStack.isEmpty()) return;

        RenderUtils.renderTooltip(guiGraphics, hoveredStack, mouseX, mouseY);
    }

    private int getHoveredBoxIndex(int mouseX, int mouseY) {
        for (int i = 0; i < 5; i++) {
            int bx = x + 3 + 19 * i;
            int by = y + this.height - 16 * 2 - 10;
            if (isInsideBox(mouseX, mouseY, bx, by)) return i;
        }

        for (int i = 0; i < 4; i++) {
            int bx = x + 3 + 19 + 19 * i;
            int by = y + this.height - 16 - 7;
            if (isInsideBox(mouseX, mouseY, bx, by)) return 5 + i;
        }

        return -1;
    }

    private boolean isInsideBox(int mouseX, int mouseY, int boxX, int boxY) {
        return mouseX >= boxX && mouseX < boxX + 16 && mouseY >= boxY && mouseY < boxY + 16;
    }

    private Optional<ItemStack> decodeGearItemStack(String stored) {
        if (stored == null || stored.isEmpty()) return Optional.empty();

        Matcher matcher = Models.ItemEncoding.getEncodedDataPattern().matcher(stored);
        if (matcher.matches()) {
            EncodedByteBuffer encodedByteBuffer = EncodedByteBuffer.fromUtf16String(matcher.group("data"));
            String itemName = matcher.group("name");

            ErrorOr<WynnItem> errorOrItem = Models.ItemEncoding.decodeItem(encodedByteBuffer, itemName);
            if (errorOrItem.hasError()) {
                WynntilsMod.warn("Failed to decode loadout gear item: " + errorOrItem.getError());
                return Optional.empty();
            }

            if (errorOrItem.getValue() instanceof GearItem gearItem) {
                return Optional.of(new FakeItemStack(gearItem, "From loadout"));
            }
            return Optional.empty();
        }

        // Legacy pre-upfix data: a raw display name instead of an encoded item
        GearInfo gearInfo = Models.Gear.getGearInfoFromDisplayName(stored.replaceFirst("§.", ""));
        if (gearInfo == null) return Optional.empty();

        // Same "default unidentified" construction as the upfixer, wrapped for rendering
        GearItem defaultGearItem = new GearItem(gearInfo, null);
        return Optional.of(new FakeItemStack(defaultGearItem, "From loadout"));
    }
    private List<ItemStack> computeBoxItemStacks(Loadout selectedLoadout) {
        if (selectedLoadout == null || !selectedLoadout.hasSkillPoints()) return List.of();

        SavableSkillPointSet skillPoints = selectedLoadout.skillPoints();

        List<String> orderedSlots = new ArrayList<>();
        orderedSlots.add(skillPoints.weapon());
        orderedSlots.addAll(skillPoints.accessoryNames());
        orderedSlots.addAll(skillPoints.armourNames());

        return orderedSlots.stream()
                .map(this::decodeGearItemStack)
                .map(opt -> opt.orElse(ItemStack.EMPTY))
                .toList();
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
