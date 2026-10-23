/*
 * Copyright © Wynntils 2026.
 * This file is released under LGPLv3. See LICENSE for full license details.
 */
package com.wynntils.handlers.item;

import com.wynntils.core.text.StyledText;
import com.wynntils.models.emeralds.type.EmeraldUnits;
import com.wynntils.models.items.WynnItem;
import com.wynntils.models.items.WynnItemData;
import com.wynntils.models.items.annotators.game.EmeraldPouchAnnotator;
import com.wynntils.models.items.items.game.EmeraldPouchItem;
import com.wynntils.models.items.properties.PagedItemProperty;
import java.lang.invoke.MethodHandle;
import java.lang.invoke.MethodHandles;
import java.lang.reflect.Method;
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
import net.minecraft.world.item.ItemStack;
import net.minecraft.world.item.Items;
import net.minecraft.world.item.component.ItemLore;
import org.junit.jupiter.api.Assertions;
import org.junit.jupiter.api.BeforeAll;
import org.junit.jupiter.api.Test;

public class TestItemHandlerLoreMatching {
    private static MethodHandle matcher;

    @BeforeAll
    public static void setup() throws ReflectiveOperationException {
        SharedConstants.tryDetectVersion();
        Bootstrap.bootStrap();
        Method method = ItemHandler.class.getDeclaredMethod(
                "isLoreSoftMatching", ItemStack.class, ItemStack.class, ItemAnnotation.class);
        method.setAccessible(true);
        matcher = MethodHandles.lookup().unreflect(method).bindTo(new ItemHandler());
    }

    @Test
    public void matchesIndependentEqualComponentsWithStylesAndEvents() throws Throwable {
        ItemStack first = stack(eventLine("/first"));
        ItemStack second = stack(eventLine("/first"));
        Assertions.assertNotSame(
                first.get(DataComponents.LORE).lines().getFirst(),
                second.get(DataComponents.LORE).lines().getFirst());
        Assertions.assertTrue(matches(first, second, new WynnItem()));
    }

    @Test
    public void matchesMissingAndEmptyLore() throws Throwable {
        Assertions.assertTrue(matches(new ItemStack(Items.DIAMOND_SWORD), stack(), new WynnItem()));
    }

    @Test
    public void matchesEquivalentCodedAndNativeStyles() throws Throwable {
        ItemStack coded = stack(Component.literal("§c§lLine"));
        ItemStack nativeStyle = stack(Component.literal("Line").withStyle(ChatFormatting.RED, ChatFormatting.BOLD));
        Assertions.assertNotEquals(
                coded.get(DataComponents.LORE).lines(),
                nativeStyle.get(DataComponents.LORE).lines());
        Assertions.assertTrue(matches(coded, nativeStyle, new WynnItem()));
    }

    @Test
    public void matchesEquivalentInheritedFontAndColor() throws Throwable {
        Style style = Style.EMPTY
                .withColor(ChatFormatting.BLUE)
                .withFont(new FontDescription.Resource(Identifier.withDefaultNamespace("common")));
        ItemStack inherited = stack(Component.empty().withStyle(style).append(Component.literal("Line")));
        ItemStack explicit = stack(Component.literal("Line").withStyle(style));
        Assertions.assertNotEquals(
                inherited.get(DataComponents.LORE).lines(),
                explicit.get(DataComponents.LORE).lines());
        Assertions.assertTrue(matches(inherited, explicit, new WynnItem()));
    }

    @Test
    public void rejectsChangedText() throws Throwable {
        Assertions.assertFalse(
                matches(stack(Component.literal("Old")), stack(Component.literal("New")), new WynnItem()));
    }

    @Test
    public void rejectsChangedFormattingWithEqualVisibleText() throws Throwable {
        Assertions.assertFalse(matches(
                stack(Component.literal("Line").withStyle(ChatFormatting.RED)),
                stack(Component.literal("Line").withStyle(ChatFormatting.BLUE)),
                new WynnItem()));
    }

    @Test
    public void rejectsChangedClickEvent() throws Throwable {
        Assertions.assertFalse(matches(stack(eventLine("/first")), stack(eventLine("/second")), new WynnItem()));
    }

    @Test
    public void rejectsChangedHoverEvent() throws Throwable {
        Assertions.assertFalse(matches(
                stack(eventLine("/same")),
                stack(eventLine("/same")
                        .copy()
                        .withStyle(
                                style -> style.withHoverEvent(new HoverEvent.ShowText(Component.literal("Changed"))))),
                new WynnItem()));
    }

    @Test
    public void rejectsChangedFont() throws Throwable {
        Assertions.assertFalse(matches(
                stack(eventLine("/same")),
                stack(eventLine("/same")
                        .copy()
                        .withStyle(style -> style.withFont(
                                new FontDescription.Resource(Identifier.withDefaultNamespace("default"))))),
                new WynnItem()));
    }

    @Test
    public void allowsUpToThreeExtraTrailingLinesInEitherDirection() throws Throwable {
        List<Component> lines =
                List.of(Component.literal("First"), Component.literal("Second"), Component.literal("Third"));
        ItemStack first = stack(lines);
        for (int extra = 1; extra <= 4; extra++) {
            List<Component> extended = new ArrayList<>(lines);
            for (int i = 0; i < extra; i++) {
                extended.add(Component.literal("Extra " + i));
            }
            ItemStack second = stack(extended);
            Assertions.assertEquals(extra <= 3, matches(first, second, new WynnItem()));
            Assertions.assertEquals(extra <= 3, matches(second, first, new WynnItem()));
        }
    }

    @Test
    public void rejectsExtraLinesOnShortLore() throws Throwable {
        Assertions.assertFalse(matches(
                stack(Component.literal("First")),
                stack(Component.literal("First"), Component.literal("Extra")),
                new WynnItem()));
    }

    @Test
    public void synchronizesPageEvenWhenComponentLoreIsIdentical() throws Throwable {
        PagedAnnotation annotation = new PagedAnnotation(0);
        Assertions.assertTrue(matches(pageStack("Same", 1), pageStack("Same", 1), annotation));
        Assertions.assertEquals(1, annotation.currentPage());
        Assertions.assertEquals(1, annotation.updates());
    }

    @Test
    public void pageChangeStillMatchesDifferentLore() throws Throwable {
        PagedAnnotation annotation = new PagedAnnotation(0);
        Assertions.assertTrue(matches(pageStack("Old", 0), pageStack("New", 2), annotation));
        Assertions.assertEquals(2, annotation.currentPage());
        Assertions.assertEquals(1, annotation.updates());
    }

    @Test
    public void rejectsChangedLoreOnTheSamePageWithoutUpdatingPage() throws Throwable {
        PagedAnnotation annotation = new PagedAnnotation(1);
        Assertions.assertFalse(matches(pageStack("Old", 1), pageStack("New", 1), annotation));
        Assertions.assertEquals(1, annotation.currentPage());
        Assertions.assertEquals(0, annotation.updates());
    }

    @Test
    public void refreshesReusedPouchAnnotationOnIdenticalLore() throws ReflectiveOperationException {
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
    public void reparsesPouchAmountWhenLoreChangesWithoutNameOrStackChanges() throws ReflectiveOperationException {
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

    private static boolean matches(ItemStack first, ItemStack second, ItemAnnotation annotation) throws Throwable {
        return (boolean) matcher.invokeExact(first, second, annotation);
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

    private static void updateStack(ItemHandler handler, ItemStack first, ItemStack second)
            throws ReflectiveOperationException {
        Method updater = ItemHandler.class.getDeclaredMethod("onItemStackUpdate", ItemStack.class, ItemStack.class);
        updater.setAccessible(true);
        updater.invoke(handler, first, second);
    }

    private static Component eventLine(String command) {
        return Component.literal("Line")
                .withStyle(Style.EMPTY
                        .withColor(ChatFormatting.GREEN)
                        .withFont(new FontDescription.Resource(Identifier.withDefaultNamespace("common")))
                        .withClickEvent(new ClickEvent.RunCommand(command))
                        .withHoverEvent(new HoverEvent.ShowText(Component.literal("Details"))));
    }

    private static ItemStack pageStack(String title, int selectedPage) {
        String pageLine = "§f\uE000".repeat(selectedPage) + "§#ffea80ff\uE000" + "§f\uE000".repeat(2 - selectedPage);
        return stack(Component.literal(title), Component.literal(pageLine), Component.literal("Footer"));
    }

    private static final class PagedAnnotation implements ItemAnnotation, PagedItemProperty {
        private int page;
        private int updates;

        private PagedAnnotation(int page) {
            this.page = page;
        }

        @Override
        public int currentPage() {
            return page;
        }

        @Override
        public void updatePage(int newPage) {
            page = newPage;
            updates++;
        }

        private int updates() {
            return updates;
        }

        @Override
        public void onUpdate(ItemStack itemStack) {}
    }
}
