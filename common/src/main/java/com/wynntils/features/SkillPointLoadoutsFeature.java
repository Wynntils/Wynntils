/*
 * Copyright © Wynntils 2022.
 * This file is released under AGPLv3. See LICENSE for full license details.
 */
package com.wynntils.features;

import com.google.common.collect.ImmutableList;
import com.wynntils.core.features.UserFeature;
import com.wynntils.core.managers.Model;
import com.wynntils.mc.event.ContainerClickEvent;
import com.wynntils.mc.event.ContainerSetContentEvent;
import com.wynntils.mc.event.ContainerSetSlotEvent;
import com.wynntils.mc.utils.ComponentUtils;
import com.wynntils.mc.utils.ItemUtils;
import com.wynntils.mc.utils.McUtils;
import com.wynntils.wynn.item.GearItemStack;
import com.wynntils.wynn.item.ItemStackTransformModel;
import com.wynntils.wynn.objects.Skills;
import com.wynntils.wynn.utils.WynnItemUtils;
import java.util.List;
import java.util.Map;
import net.minecraft.ChatFormatting;
import net.minecraft.client.gui.screens.Screen;
import net.minecraft.client.gui.screens.inventory.ContainerScreen;
import net.minecraft.nbt.ListTag;
import net.minecraft.network.chat.Component;
import net.minecraft.network.chat.Style;
import net.minecraft.network.chat.TextComponent;
import net.minecraft.network.chat.TranslatableComponent;
import net.minecraft.world.item.ItemStack;
import net.minecraft.world.item.Items;
import net.minecraftforge.eventbus.api.SubscribeEvent;

public class SkillPointLoadoutsFeature extends UserFeature {
    private static final String CHARACTER_MENU_TITLE = "Character Info";

    private static final int SAVE_SLOT = 3;
    private static final int LOAD_SLOT = 5;

    private static final List<Component> SAVE_TOOLTIP = List.of(
            new TranslatableComponent("feature.wynntils.skillPointLoadouts.saveTooltip").withStyle(ChatFormatting.GRAY),
            TextComponent.EMPTY,
            new TranslatableComponent("feature.wynntils.skillPointLoadouts.warnTooltip").withStyle(ChatFormatting.RED));
    private static final List<Component> LOAD_TOOLTIP = List.of(
            new TranslatableComponent("feature.wynntils.skillPointLoadouts.loadTooltip").withStyle(ChatFormatting.GRAY),
            TextComponent.EMPTY,
            new TranslatableComponent("feature.wynntils.skillPointLoadouts.warnTooltip").withStyle(ChatFormatting.RED));

    private ItemStack saveItem;
    private ItemStack loadItem;

    @Override
    public List<Class<? extends Model>> getModelDependencies() {
        return List.of(ItemStackTransformModel.class);
    }

    @Override
    protected void onInit(ImmutableList.Builder<Condition> conditions) {
        saveItem = new ItemStack(Items.WRITABLE_BOOK);
        loadItem = new ItemStack(Items.KNOWLEDGE_BOOK);

        ListTag saveListTag = new ListTag();
        saveListTag.addAll(ComponentUtils.wrapTooltips(SAVE_TOOLTIP, 200).stream()
                .map(ItemUtils::toLoreStringTag)
                .toList());
        ItemUtils.replaceLore(saveItem, saveListTag);
        saveItem.setHoverName(new TranslatableComponent("feature.wynntils.skillPointLoadouts.save")
                .withStyle(Style.EMPTY.withItalic(false))
                .withStyle(ChatFormatting.GOLD));

        ListTag loadListTag = new ListTag();
        loadListTag.addAll(ComponentUtils.wrapTooltips(LOAD_TOOLTIP, 200).stream()
                .map(ItemUtils::toLoreStringTag)
                .toList());
        ItemUtils.replaceLore(loadItem, loadListTag);
        loadItem.setHoverName(new TranslatableComponent("feature.wynntils.skillPointLoadouts.load")
                .withStyle(Style.EMPTY.withItalic(false))
                .withStyle(ChatFormatting.GOLD));
    }

    @SubscribeEvent
    public void onContainerClick(ContainerClickEvent event) {
        String itemName = event.getItemStack().getHoverName().getString();

        if (!isSaveItem(itemName) && !isLoadItem(itemName)) return;

        event.setCanceled(true);

        if (isSaveItem(itemName)) {
            List<GearItemStack> gearList = WynnItemUtils.getPlayersGearAndWeapon();

            Map<Skills, Integer> gearAddedSkillPoints = WynnItemUtils.getSkillSumFromGear(gearList);

            for (Map.Entry<Skills, Integer> entry : gearAddedSkillPoints.entrySet()) {
                System.out.println("entry.getKey() = " + entry.getKey());
                System.out.println("entry.getValue() = " + entry.getValue());
            }
        }
    }

    @SubscribeEvent
    public void onSetSlot(ContainerSetSlotEvent.Post event) {
        injectItems(event.getContainerId());
    }

    @SubscribeEvent
    public void onSetContents(ContainerSetContentEvent.Post event) {
        injectItems(event.getContainerId());
    }

    private void injectItems(int containerId) {
        Screen screen = McUtils.mc().screen;

        ContainerScreen containerScreen = getIsCharacterScreen(screen);
        if (containerScreen == null) return;

        if (containerScreen.getMenu().containerId != containerId) return;

        containerScreen.getMenu().getSlot(SAVE_SLOT).set(saveItem);
        containerScreen.getMenu().getSlot(LOAD_SLOT).set(loadItem);
    }

    private static ContainerScreen getIsCharacterScreen(Screen screen) {
        if (screen == null) return null;
        if (!(screen instanceof ContainerScreen containerScreen)) return null;

        String title = ComponentUtils.getUnformatted(screen.getTitle());

        if (!CHARACTER_MENU_TITLE.equals(title)) return null;
        return containerScreen;
    }

    private boolean isLoadItem(String itemName) {
        return itemName.equals(loadItem.getHoverName().getString());
    }

    private boolean isSaveItem(String itemName) {
        return itemName.equals(saveItem.getHoverName().getString());
    }

    public static class SkillPointLoadout {
        private String name;
        private Map<Skills, Integer> skills;
        private int combatLevelMin;

        public SkillPointLoadout(String name, Map<Skills, Integer> skills, int combatLevelMin) {
            this.name = name;
            this.skills = skills;
            this.combatLevelMin = combatLevelMin;
        }

        public String getName() {
            return name;
        }

        public Map<Skills, Integer> getSkills() {
            return skills;
        }

        public int getCombatLevelMin() {
            return combatLevelMin;
        }
    }
}
