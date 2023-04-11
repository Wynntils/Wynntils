/*
 * Copyright © Wynntils 2023.
 * This file is released under AGPLv3. See LICENSE for full license details.
 */
package com.wynntils.models.abilities.type;

import com.google.gson.JsonElement;
import com.wynntils.core.WynntilsMod;
import com.wynntils.core.components.Managers;
import com.wynntils.core.components.Models;
import com.wynntils.core.text.PartStyle;
import com.wynntils.core.text.StyledText;
import com.wynntils.utils.mc.LoreUtils;
import com.wynntils.utils.mc.McUtils;
import com.wynntils.utils.type.IterationDecision;
import java.io.File;
import java.util.ArrayList;
import java.util.HashSet;
import java.util.List;
import java.util.Locale;
import java.util.Set;
import java.util.regex.Matcher;
import java.util.regex.Pattern;
import net.minecraft.network.chat.Component;
import net.minecraft.world.item.Item;
import net.minecraft.world.item.ItemStack;
import net.minecraft.world.item.Items;

public class ParsedAbilityTree {
    private static final File SAVE_FOLDER = WynntilsMod.getModStorageDir("debug");

    private static final StyledText CONNECTION_NAME = StyledText.fromString(" ");

    private static final Pattern NODE_NAME_PATTERN = Pattern.compile("§.(Unlock )?§l(.+)(§r§. ability)?");
    private static final Pattern NODE_POINT_COST_PATTERN = Pattern.compile("§.. §7Ability Points: §f(\\d+)");
    private static final Pattern NODE_BLOCKS_ABILITY_PATTERN = Pattern.compile("§c- §7(.+)");
    private static final Pattern NODE_REQUIRED_ABILITY_PATTERN = Pattern.compile("§.. §7Required Ability: §f(.+)");
    private static final Pattern NODE_REQUIRED_ARCHETYPE_PATTERN =
            Pattern.compile("§.. §7Min (.+) Archetype: §c(\\d+)§7/(\\d+)");
    private static final Pattern NODE_ARCHETYPE_PATTERN = Pattern.compile("§.§l(.+) Archetype");

    private final List<AbilityTreeSkillNode> nodes = new ArrayList<>();

    // Do not serialize this field
    private final transient List<AbilityTreeLocation> unprocessedConnections = new ArrayList<>();

    public void addNodeFromItem(ItemStack itemStack, int page, int slot) {
        nodes.add(parseNodeFromItem(itemStack, page, slot));
    }

    public void addConnectionFromItem(int page, int slot) {
        unprocessedConnections.add(AbilityTreeLocation.fromSlot(slot, page));
    }

    public AbilityTreeSkillNode parseNodeFromItem(ItemStack itemStack, int page, int slot) {
        StyledText nameStyledText = StyledText.fromComponent(itemStack.getHoverName());

        boolean unlockable;
        StyledText actualName;
        if (nameStyledText.getPartCount() == 1) {
            actualName = nameStyledText;
            unlockable = false;
        } else {
            actualName = nameStyledText.iterate((part, changes) -> {
                // The part which is bolded is the actual name of the ability
                if (!part.getPartStyle().isBold()) {
                    changes.clear();
                }

                return IterationDecision.CONTINUE;
            });
            unlockable = true;
        }

        List<StyledText> loreStyledText = LoreUtils.getLoreStyledText(itemStack);

        if (unlockable) {
            // Empty line + "click here to unlock"
            loreStyledText = loreStyledText.subList(0, loreStyledText.size() - 2);
        }

        int cost = 0;
        List<String> blocks = new ArrayList<>();
        String requiredAbility = null;
        AbilityTreeSkillNode.ArchetypeRequirement requiredArchetype = null;
        String archetype = null;

        for (StyledText text : loreStyledText) {
            Matcher matcher = text.getMatcher(NODE_POINT_COST_PATTERN);
            if (matcher.matches()) {
                cost = Integer.parseInt(matcher.group(1));
                continue;
            }

            matcher = text.getMatcher(NODE_BLOCKS_ABILITY_PATTERN);
            if (matcher.matches()) {
                blocks.add(matcher.group(1));
                continue;
            }

            matcher = text.getMatcher(NODE_REQUIRED_ABILITY_PATTERN);
            if (matcher.matches()) {
                requiredAbility = matcher.group(1);
                continue;
            }

            matcher = text.getMatcher(NODE_REQUIRED_ARCHETYPE_PATTERN);
            if (matcher.matches()) {
                requiredArchetype = new AbilityTreeSkillNode.ArchetypeRequirement(
                        matcher.group(1), Integer.parseInt(matcher.group(3)));
                continue;
            }

            matcher = text.getMatcher(NODE_ARCHETYPE_PATTERN);
            if (matcher.matches()) {
                archetype = matcher.group(1);
                continue;
            }
        }

        AbilityTreeSkillNode.ItemInformation itemInformation = new AbilityTreeSkillNode.ItemInformation(
                Item.getId(itemStack.getItem()),
                unlockable ? itemStack.getDamageValue() - 1 : itemStack.getDamageValue());

        AbilityTreeSkillNode node = new AbilityTreeSkillNode(
                actualName.getString(PartStyle.StyleType.NONE),
                actualName.getString(PartStyle.StyleType.DEFAULT),
                loreStyledText.stream()
                        .map(styledText -> styledText.getString(PartStyle.StyleType.DEFAULT))
                        .toList(),
                itemInformation,
                cost,
                blocks,
                requiredAbility,
                requiredArchetype,
                archetype,
                AbilityTreeLocation.fromSlot(slot, page),
                new ArrayList<>());
        return node;
    }

    public void processItem(ItemStack itemStack, int page, int slot) {
        if (isNodeItem(itemStack, slot)) {
            addNodeFromItem(itemStack, page, slot);
        } else if (isConnectionItem(itemStack)) {
            addConnectionFromItem(page, slot);
        }
    }

    public void processConnections(int currentPage, boolean lastPage) {
        List<AbilityTreeLocation> processedLocation = new ArrayList<>();

        // We must traverse the connections in a specific order, so we sort them
        List<AbilityTreeLocation> sortedConnections =
                unprocessedConnections.stream().sorted().toList();

        for (AbilityTreeLocation location : sortedConnections) {
            if (processedLocation.contains(location)) continue;

            List<AbilityTreeLocation> connectedLocations = new ArrayList<>();
            connectedLocations.add(location);

            for (AbilityTreeLocation other : sortedConnections) {
                if (other.equals(location)) {
                    continue;
                }

                if (connectedLocations.stream().anyMatch(connected -> connected.isNeighbor(other))) {
                    connectedLocations.add(other);
                }
            }

            Set<AbilityTreeSkillNode> connectedNodes = new HashSet<>();
            for (AbilityTreeLocation connection : connectedLocations) {
                for (AbilityTreeSkillNode node : nodes) {
                    if (node.location().isNeighbor(connection)) {
                        connectedNodes.add(node);
                    }
                }
            }

            // If the connection is not valid, or continues to the next page, we keep it for next page (if we are not on
            // the last page)
            if (connectedNodes.size() <= 1
                    || (!lastPage
                            && connectedLocations.stream()
                                    .anyMatch(loc -> loc.page() == currentPage
                                            && loc.row() + 1 == AbilityTreeLocation.MAX_ROWS))) {
                continue;
            }

            for (AbilityTreeSkillNode current : connectedNodes) {
                current.connections()
                        .addAll(connectedNodes.stream()
                                .filter(node -> !node.equals(current))
                                .map(AbilityTreeSkillNode::name)
                                .toList());
            }

            processedLocation.addAll(connectedLocations);
        }

        // Remove the processed connections
        unprocessedConnections.removeAll(processedLocation);
    }

    public void saveToDisk() {
        if (!unprocessedConnections.isEmpty()) {
            McUtils.sendMessageToClient(Component.literal(
                    "WARN: There are unprocessed connections left in the dump! Check processing algorithm!"));
        }

        // Save the dump to a file
        JsonElement element = Managers.Json.GSON.toJsonTree(this);

        String fileName = Models.Character.getClassType().getName().toLowerCase(Locale.ROOT) + "_ablities.json";
        File jsonFile = new File(SAVE_FOLDER, fileName);
        Managers.Json.savePreciousJson(jsonFile, element.getAsJsonObject());

        McUtils.sendMessageToClient(Component.literal("Saved ability tree dump to " + jsonFile.getAbsolutePath()));
    }

    private boolean isNodeItem(ItemStack itemStack, int slot) {
        StyledText nameStyledText = StyledText.fromComponent(itemStack.getHoverName());
        return itemStack.getItem() == Items.STONE_AXE
                && slot < 54
                && nameStyledText.getMatcher(NODE_NAME_PATTERN).matches();
    }

    private boolean isConnectionItem(ItemStack itemStack) {
        return itemStack.getItem() == Items.STONE_AXE
                && StyledText.fromComponent(itemStack.getHoverName()).equals(CONNECTION_NAME);
    }
}
