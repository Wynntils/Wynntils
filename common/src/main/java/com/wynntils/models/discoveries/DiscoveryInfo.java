/*
 * Copyright © Wynntils 2022.
 * This file is released under AGPLv3. See LICENSE for full license details.
 */
package com.wynntils.models.discoveries;

import com.wynntils.core.components.Models;
import com.wynntils.core.text.StyledText;
import com.wynntils.models.discoveries.profile.DiscoveryProfile;
import com.wynntils.models.discoveries.type.DiscoveryType;
import com.wynntils.utils.mc.ComponentUtils;
import com.wynntils.utils.mc.LoreUtils;
import java.util.ArrayList;
import java.util.List;
import java.util.Locale;
import java.util.regex.Matcher;
import java.util.regex.Pattern;
import net.minecraft.ChatFormatting;
import net.minecraft.network.chat.Component;
import net.minecraft.world.item.ItemStack;

public class DiscoveryInfo {
    private static final Pattern COMBAT_LEVEL_PATTERN = Pattern.compile("§a✔§r§7 Combat Lv. Min: §r§f(\\d+)");
    private final String name;
    private final DiscoveryType type;
    private final String description;
    private final int minLevel;
    private final boolean discovered;
    private final List<String> requirements;
    private List<Component> displayLore = null;

    public DiscoveryInfo(DiscoveryProfile discoveryProfile) {
        this.name = discoveryProfile.getName();
        this.type = DiscoveryType.valueOf(discoveryProfile.getType().toUpperCase(Locale.ROOT));
        this.description = "";
        this.minLevel = discoveryProfile.getLevel();
        this.discovered = false;
        this.requirements = discoveryProfile.getRequirements();
    }

    private DiscoveryInfo(String name, DiscoveryType type, String description, int minLevel) {
        this.name = name;
        this.type = type;
        this.description = description;
        this.minLevel = minLevel;
        this.discovered = true;
        this.requirements = List.of();
    }

    public static DiscoveryInfo parseFromItemStack(ItemStack itemStack) {
        List<StyledText> lore = LoreUtils.getLore(itemStack);
        if (lore.isEmpty()) {
            return null;
        }

        Matcher m = lore.get(0).match(COMBAT_LEVEL_PATTERN);
        if (!m.matches()) return null;
        int minLevel = Integer.parseInt(m.group(1));

        StyledText name = ComponentUtils.getCoded(itemStack.getHoverName()).getNormalized();
        DiscoveryType type = DiscoveryType.getDiscoveryTypeFromString(name);
        if (type == null) return null;

        StringBuilder descriptionBuilder = new StringBuilder();
        for (int i = 2; i < lore.size(); i++) {
            descriptionBuilder.append(ComponentUtils.stripFormatting(lore.get(i)));
        }
        String description = descriptionBuilder.toString();

        return new DiscoveryInfo(ComponentUtils.stripFormatting(name), type, description, minLevel);
    }

    private List<Component> generateLore() {
        displayLore = new ArrayList<>();

        displayLore.add(Component.literal(name).withStyle(type.getColor()).withStyle(ChatFormatting.BOLD));

        if (Models.CombatXp.getCombatLevel().current() >= minLevel) {
            displayLore.add(Component.literal("✔")
                    .withStyle(ChatFormatting.GREEN)
                    .append(Component.literal(" Combat Lv. Min: ")
                            .withStyle(ChatFormatting.GRAY)
                            .append(Component.literal(String.valueOf(minLevel)).withStyle(ChatFormatting.WHITE))));
        } else {
            displayLore.add(Component.literal("✘")
                    .withStyle(ChatFormatting.RED)
                    .append(Component.literal(" Combat Lv. Min: ")
                            .withStyle(ChatFormatting.GRAY)
                            .append(Component.literal(String.valueOf(minLevel)).withStyle(ChatFormatting.WHITE))));
        }

        displayLore.add(Component.empty());

        if (discovered) {
            displayLore.add(Component.literal("Discovered").withStyle(ChatFormatting.GREEN));
        } else {
            displayLore.add(Component.literal("Not Discovered").withStyle(ChatFormatting.RED));
        }

        if (!description.isEmpty()) {
            displayLore.add(Component.empty());
            displayLore.addAll(ComponentUtils.wrapTooltips(
                    List.of(Component.literal(description).withStyle(ChatFormatting.GRAY)), 300));
        }

        return displayLore;
    }

    public List<Component> getLore() {
        if (displayLore == null) {
            displayLore = generateLore();
        }

        return displayLore;
    }

    public String getName() {
        return name;
    }

    public DiscoveryType getType() {
        return type;
    }

    public boolean isDiscovered() {
        return discovered;
    }

    public int getMinLevel() {
        return minLevel;
    }

    public List<String> getRequirements() {
        return requirements;
    }
}
