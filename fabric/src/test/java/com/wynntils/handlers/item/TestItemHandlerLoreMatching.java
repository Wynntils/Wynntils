/*
 * Copyright © Wynntils 2026.
 * This file is released under LGPLv3. See LICENSE for full license details.
 */
package com.wynntils.handlers.item;

import com.wynntils.core.text.StyledText;
import com.wynntils.core.text.fonts.CommonFonts;
import com.wynntils.mc.event.SetSlotEvent;
import com.wynntils.models.emeralds.type.EmeraldUnits;
import com.wynntils.models.gear.type.GearTier;
import com.wynntils.models.gear.type.GearType;
import com.wynntils.models.items.WynnItem;
import com.wynntils.models.items.WynnItemData;
import com.wynntils.models.items.annotators.game.EmeraldPouchAnnotator;
import com.wynntils.models.items.items.game.EmeraldPouchItem;
import com.wynntils.models.items.items.game.UnknownGearItem;
import java.util.ArrayList;
import java.util.List;
import net.minecraft.ChatFormatting;
import net.minecraft.SharedConstants;
import net.minecraft.core.component.DataComponents;
import net.minecraft.network.chat.ClickEvent;
import net.minecraft.network.chat.Component;
import net.minecraft.network.chat.FontDescription;
import net.minecraft.network.chat.HoverEvent;
import net.minecraft.network.chat.Style;
import net.minecraft.resources.Identifier;
import net.minecraft.server.Bootstrap;
import net.minecraft.world.SimpleContainer;
import net.minecraft.world.item.ItemStack;
import net.minecraft.world.item.Items;
import net.minecraft.world.item.component.ItemLore;
import org.junit.jupiter.api.Assertions;
import org.junit.jupiter.api.BeforeAll;
import org.junit.jupiter.api.Test;

public class TestItemHandlerLoreMatching {
    @BeforeAll
    public static void setup() {
        SharedConstants.tryDetectVersion();
        Bootstrap.bootStrap();
    }

    @Test
    public void matchesIndependentEqualComponentsWithStylesAndEvents() {
        ItemStack first = stack(eventLine("/first"));
        ItemStack second = stack(eventLine("/first"));
        Assertions.assertNotSame(
                first.get(DataComponents.LORE).lines().getFirst(),
                second.get(DataComponents.LORE).lines().getFirst());
        Assertions.assertTrue(matches(first, second));
    }

    @Test
    public void matchesMissingAndEmptyLore() {
        Assertions.assertTrue(matches(new ItemStack(Items.DIAMOND_SWORD), stack()));
    }

    @Test
    public void matchesEquivalentCodedAndNativeStyles() {
        ItemStack coded = stack(Component.literal("§c§lLine"));
        ItemStack nativeStyle = stack(Component.literal("Line").withStyle(ChatFormatting.RED, ChatFormatting.BOLD));
        Assertions.assertNotEquals(
                coded.get(DataComponents.LORE).lines(),
                nativeStyle.get(DataComponents.LORE).lines());
        Assertions.assertTrue(matches(coded, nativeStyle));
    }

    @Test
    public void matchesEquivalentInheritedFontAndColor() {
        Style style = Style.EMPTY
                .withColor(ChatFormatting.BLUE)
                .withFont(new FontDescription.Resource(Identifier.withDefaultNamespace("common")));
        ItemStack inherited = stack(Component.empty().withStyle(style).append(Component.literal("Line")));
        ItemStack explicit = stack(Component.literal("Line").withStyle(style));
        Assertions.assertNotEquals(
                inherited.get(DataComponents.LORE).lines(),
                explicit.get(DataComponents.LORE).lines());
        Assertions.assertTrue(matches(inherited, explicit));
    }

    @Test
    public void rejectsChangedText() {
        Assertions.assertFalse(matches(stack(Component.literal("Old")), stack(Component.literal("New"))));
    }

    @Test
    public void rejectsChangedFormattingWithEqualVisibleText() {
        Assertions.assertFalse(matches(
                stack(Component.literal("Line").withStyle(ChatFormatting.RED)),
                stack(Component.literal("Line").withStyle(ChatFormatting.BLUE))));
    }

    @Test
    public void rejectsChangedClickEvent() {
        Assertions.assertFalse(matches(stack(eventLine("/first")), stack(eventLine("/second"))));
    }

    @Test
    public void rejectsChangedHoverEvent() {
        Assertions.assertFalse(matches(
                stack(eventLine("/same")),
                stack(eventLine("/same")
                        .copy()
                        .withStyle(style ->
                                style.withHoverEvent(new HoverEvent.ShowText(Component.literal("Changed")))))));
    }

    @Test
    public void rejectsChangedFont() {
        Assertions.assertFalse(matches(
                stack(eventLine("/same")),
                stack(eventLine("/same")
                        .copy()
                        .withStyle(style -> style.withFont(
                                new FontDescription.Resource(Identifier.withDefaultNamespace("default")))))));
    }

    @Test
    public void allowsUpToThreeExtraTrailingLinesInEitherDirection() {
        List<Component> lines =
                List.of(Component.literal("First"), Component.literal("Second"), Component.literal("Third"));
        ItemStack first = stack(lines);
        for (int extra = 1; extra <= 4; extra++) {
            List<Component> extended = new ArrayList<>(lines);
            for (int i = 0; i < extra; i++) {
                extended.add(Component.literal("Extra " + i));
            }
            ItemStack second = stack(extended);
            Assertions.assertEquals(extra <= 3, matches(first, second));
            Assertions.assertEquals(extra <= 3, matches(second, first));
        }
    }

    @Test
    public void rejectsExtraLinesOnShortLore() {
        Assertions.assertFalse(matches(
                stack(Component.literal("First")), stack(Component.literal("First"), Component.literal("Extra"))));
    }

    @Test
    public void synchronizesCachedPageEvenWhenComponentLoreIsIdentical() {
        ItemHandler handler = new ItemHandler();
        Component pageLine = Component.literal("§f\uE000§#ffea80ff\uE000")
                .withStyle(Style.EMPTY.withFont(CommonFonts.TOOLTIP_PAGE_FONT));
        ItemStack first = stack(Component.literal("Gear"), pageLine);
        ItemStack second = stack(Component.literal("Gear"), pageLine.copy());
        UnknownGearItem annotation = new UnknownGearItem("Gear", GearTier.NORMAL, GearType.SPEAR, false);
        annotation.setCurrentPage(0);
        annotation.markParsedPage(0);
        annotation.markParsedPage(1);
        handler.updateItem(
                first,
                annotation,
                StyledText.fromComponent(first.getHoverName()).getNormalized());

        updateStack(handler, first, second);

        Assertions.assertSame(
                annotation, ItemHandler.getItemStackAnnotation(second).orElseThrow());
        Assertions.assertEquals(1, annotation.currentPage());
        Assertions.assertTrue(annotation.hasParsedPage(0));
        Assertions.assertSame(second, annotation.getData().get(WynnItemData.ITEMSTACK_KEY));
    }

    @Test
    public void refreshesReusedPouchAnnotationOnIdenticalLore() {
        ItemHandler handler = new ItemHandler();
        handler.registerAnnotator(new EmeraldPouchAnnotator());
        ItemStack first = emeraldPouch(100);
        ItemStack second = emeraldPouch(100);
        EmeraldPouchItem annotation = new EmeraldPouchItem(1, 100);
        handler.updateItem(
                first,
                annotation,
                StyledText.fromComponent(first.getHoverName()).getNormalized());
        annotation.getData().store(WynnItemData.TOOLTIP_KEY, "Cached tooltip");

        updateStack(handler, first, second);

        Assertions.assertSame(
                annotation, ItemHandler.getItemStackAnnotation(second).orElseThrow());
        Assertions.assertSame(second, annotation.getData().get(WynnItemData.ITEMSTACK_KEY));
        Assertions.assertNull(annotation.getData().get(WynnItemData.TOOLTIP_KEY));
        Assertions.assertTrue(handler.getProfilingCounts().isEmpty());
    }

    @Test
    public void reparsesPouchAmountWhenLoreChangesWithoutNameOrStackChanges() {
        ItemHandler handler = new ItemHandler();
        handler.registerAnnotator(new EmeraldPouchAnnotator());
        ItemStack first = emeraldPouch(100);
        ItemStack second = emeraldPouch(200);
        EmeraldPouchItem annotation = new EmeraldPouchItem(1, 100);
        handler.updateItem(
                first,
                annotation,
                StyledText.fromComponent(first.getHoverName()).getNormalized());

        updateStack(handler, first, second);

        EmeraldPouchItem updated = Assertions.assertInstanceOf(
                EmeraldPouchItem.class,
                ItemHandler.getItemStackAnnotation(second).orElseThrow());
        Assertions.assertNotSame(annotation, updated);
        Assertions.assertEquals(200, updated.getValue());
        Assertions.assertSame(second, updated.getData().get(WynnItemData.ITEMSTACK_KEY));
        Assertions.assertEquals(1, handler.getProfilingCounts().get(EmeraldPouchItem.class));
    }

    private static boolean matches(ItemStack first, ItemStack second) {
        ItemHandler handler = new ItemHandler();
        ItemStack existing = first.copy();
        ItemStack incoming = second.copy();
        WynnItem annotation = new WynnItem();
        handler.updateItem(
                existing,
                annotation,
                StyledText.fromComponent(existing.getHoverName()).getNormalized());
        updateStack(handler, existing, incoming);
        return ItemHandler.getItemStackAnnotation(incoming).orElse(null) == annotation;
    }

    private static ItemStack stack(Component... lines) {
        return stack(List.of(lines));
    }

    private static ItemStack stack(List<Component> lines) {
        ItemStack stack = new ItemStack(Items.DIAMOND_SWORD);
        stack.set(DataComponents.LORE, new ItemLore(lines));
        return stack;
    }

    private static ItemStack emeraldPouch(int amount) {
        ItemStack stack = new ItemStack(Items.DIAMOND_AXE);
        stack.set(DataComponents.CUSTOM_NAME, Component.literal("§aEmerald Pouch §2[Tier 1]"));
        stack.set(
                DataComponents.LORE,
                new ItemLore(List.of(
                        Component.literal("§6§l" + amount + EmeraldUnits.EMERALD.getSymbol()),
                        Component.empty(),
                        Component.literal("Footer"))));
        return stack;
    }

    private static void updateStack(ItemHandler handler, ItemStack first, ItemStack second) {
        handler.onSetSlot(new SetSlotEvent.Pre(new SimpleContainer(first), 0, second));
    }

    private static Component eventLine(String command) {
        return Component.literal("Line")
                .withStyle(Style.EMPTY
                        .withColor(ChatFormatting.GREEN)
                        .withFont(new FontDescription.Resource(Identifier.withDefaultNamespace("common")))
                        .withClickEvent(new ClickEvent.RunCommand(command))
                        .withHoverEvent(new HoverEvent.ShowText(Component.literal("Details"))));
    }
}
