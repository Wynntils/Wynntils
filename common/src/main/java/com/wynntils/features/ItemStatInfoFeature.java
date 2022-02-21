/*
 * Copyright © Wynntils 2022.
 * This file is released under AGPLv3. See LICENSE for full license details.
 */
package com.wynntils.features;

import com.google.common.collect.ImmutableList;
import com.wynntils.core.WynntilsMod;
import com.wynntils.core.features.Feature;
import com.wynntils.core.webapi.WebManager;
import com.wynntils.mc.event.ItemToolTipHoveredNameEvent;
import com.wynntils.mc.event.ItemTooltipRenderEvent;
import com.wynntils.mc.utils.ComponentUtils;
import com.wynntils.mc.utils.ItemUtils;
import com.wynntils.mc.utils.McUtils;
import com.wynntils.utils.MathUtils;
import com.wynntils.wc.objects.items.IdentificationContainer;
import com.wynntils.wc.objects.items.ItemProfile;
import com.wynntils.wc.utils.WynnUtils;
import java.awt.*;
import java.util.*;
import java.util.function.BiFunction;
import java.util.regex.Matcher;
import java.util.regex.Pattern;
import net.minecraft.ChatFormatting;
import net.minecraft.nbt.ListTag;
import net.minecraft.network.chat.*;
import net.minecraft.network.chat.Component;
import net.minecraft.network.chat.TextComponent;
import net.minecraft.world.item.ItemStack;
import net.minecraftforge.eventbus.api.SubscribeEvent;
import org.lwjgl.glfw.GLFW;

public class ItemStatInfoFeature extends Feature {
    private static final Pattern ITEM_STATUS_PATTERN =
            Pattern.compile("([+\\-])(\\d+)(%|\\stier|/3s|/4s|/5s|)\\*{0,3}\\s(.+)");

    // TODO: Replace these with configs
    // Possible variables: %percentage% %minmax% %reidchance%
    private static final String MAIN_FORMAT_STRING = "%percentage%";
    private static final String ALTERNATIVE_FORMAT_STRING =
            "%percentage% -- - -- %percentage%"; // Used when uses presses SHIFT on lore.

    private static final Map<String, BiFunction<IdentificationContainer, Integer, MutableComponent>>
            infoVariableMap =
                    new HashMap<>() {
                        {
                            put(
                                    "%percentage%",
                                    ItemStatInfoFeature::getPercentageTextComponent);
                        }
                    };

    @Override
    protected void init(ImmutableList.Builder<Condition> conditions) {
        conditions.add(new WebLoadedCondition());
    }

    @Override
    protected boolean onEnable() {
        WynntilsMod.getEventBus().register(this);
        return WebManager.tryLoadItemList();
    }

    @Override
    protected void onDisable() {}

    @SubscribeEvent
    public void onToolTipHoveredNameEvent(ItemToolTipHoveredNameEvent e) {
        /*
         * This math was originally based off Avaritia code.
         * Special thanks for Morpheus1101 and SpitefulFox
         * Avaritia Repo: https://github.com/Morpheus1101/Avaritia
         */
        if (ItemUtils.hasMarker(e.getStack(), "isPerfect")) {
            MutableComponent newName = new TextComponent("").withStyle(ChatFormatting.BOLD);

            String name =
                    "Perfect "
                            + WynnUtils.normalizeBadString(
                                    ChatFormatting.stripFormatting(
                                            ComponentUtils.getUnformatted(
                                                    e.getStack().getHoverName())));

            for (int i = 0; i < name.length(); i++) {
                long time = System.currentTimeMillis();
                float z = 2000.0F;
                // FIXME: I don't think this functions properly, colors are not getting updated real
                // time (I assume that was the intention)
                Style color =
                        Style.EMPTY.withColor(
                                Color.HSBtoRGB(((time + i * z / 7F) % (int) z) / z, 0.8F, 0.8F));

                newName.append(new TextComponent(String.valueOf(name.charAt(i))).setStyle(color));
            }

            e.setHoveredName(newName);
        } else if (ItemUtils.hasMarker(
                e.getStack(), "isDefective")) { // FIXME: Current implementation seems buggy?
            MutableComponent newName =
                    new TextComponent("").withStyle(ChatFormatting.BOLD, ChatFormatting.DARK_RED);

            float obfuscationChance =
                    0.08f; // UtilitiesConfig.Identifications.INSTANCE.defectiveObfuscationAmount /
            // 100;

            String name =
                    "Defective "
                            + WynnUtils.normalizeBadString(
                                    ChatFormatting.stripFormatting(
                                            ComponentUtils.getUnformatted(
                                                    e.getStack().getHoverName())));

            boolean obfuscated = Math.random() < obfuscationChance;
            StringBuilder current = new StringBuilder();

            for (int i = 0; i < name.length() - 1; i++) {
                current.append(name.charAt(i));

                if (Math.random() < obfuscationChance && !obfuscated) {
                    newName.append(
                            new TextComponent(current.toString())
                                    .withStyle(ChatFormatting.OBFUSCATED));
                    current = new StringBuilder();

                    obfuscated = true;
                } else if (Math.random() > obfuscationChance && obfuscated) {
                    newName.append(new TextComponent(current.toString()));
                    current = new StringBuilder();

                    obfuscated = false;
                }
            }

            current.append(name.charAt(name.length() - 1));

            if (obfuscated) {
                newName.append(
                        new TextComponent(current.toString()).withStyle(ChatFormatting.OBFUSCATED));
            } else {
                newName.append(new TextComponent(current.toString()));
            }

            e.setHoveredName(newName);
        }
    }

    @SubscribeEvent
    public void onItemToolTipRender(ItemTooltipRenderEvent e) {
        ItemStack itemStack = e.getItemStack();

        replaceLore(
                itemStack,
                GLFW.glfwGetKey(McUtils.mc().getWindow().getWindow(), GLFW.GLFW_KEY_LEFT_SHIFT)
                        == 1);
    }

    private void replaceLore(ItemStack itemStack, boolean alternativeForm) {
        String itemName =
                WynnUtils.normalizeBadString(
                        ChatFormatting.stripFormatting(itemStack.getHoverName().getString()));

        if (!WebManager.getItemsMap().containsKey(itemName)) return;

        if (ItemUtils.hasMarker(itemStack, "loreMainForm") && !alternativeForm) return;
        if (ItemUtils.hasMarker(itemStack, "loreAlternativeForm") && alternativeForm) return;

        ItemProfile profile = WebManager.getItemsMap().get(itemName);

        ListTag lore = ItemUtils.getLoreTagElseEmpty(itemStack);
        ListTag newLore = new ListTag();

        // Used to not give set bonuses a different lore
        boolean endOfStatuses = false;

        float percentTotal = 0;
        int idAmount = 0;

        boolean hasNew = false;

        for (int i = 0; i < lore.size(); i++) {
            MutableComponent loreLine = Component.Serializer.fromJson(lore.getString(i));

            if (loreLine == null) {
                continue;
            }

            String unformattedLoreLine = WynnUtils.normalizeBadString(loreLine.getString());

            if (unformattedLoreLine.equals("Set Bonus:")) {
                endOfStatuses = true;
            }

            if (!endOfStatuses) {
                Matcher statusMatcher = ITEM_STATUS_PATTERN.matcher(unformattedLoreLine);

                if (!statusMatcher.matches()) {
                    newLore.add(lore.get(i));
                    continue;
                }

                int statValue = Integer.parseInt(statusMatcher.group(1) + statusMatcher.group(2));
                String statusName = statusMatcher.group(4);

                // FIXME: Some other ids are recognized as new
                // FIXME: Fix "Spell Name Cost" (For example: Totem Cost is recognized as NEW)
                if (!profile.getLongNameStatusMap().containsKey(statusName)) {
                    loreLine.append(new TextComponent(" [NEW]").withStyle(ChatFormatting.GOLD));
                    newLore.add(ItemUtils.toLoreStringTag(loreLine));
                    hasNew = true;
                    continue;
                }

                IdentificationContainer idContainer =
                        profile.getLongNameStatusMap().get(statusName);

                if (idContainer.hasConstantValue()) {
                    newLore.add(lore.get(i)); // unchanged
                    continue;
                }

                if (!idContainer.isValidValue(statValue)) {
                    loreLine.append(new TextComponent(" [NEW]").withStyle(ChatFormatting.GOLD));
                    newLore.add(ItemUtils.toLoreStringTag(loreLine));
                    hasNew = true;
                    continue;
                }

                float percentage =
                        MathUtils.inverseLerp(idContainer.getMin(), idContainer.getMax(), statValue)
                                * 100;

                // Fixed space between stat and info formatter
                loreLine.append(" ");

                String loreAdditions =
                        alternativeForm ? ALTERNATIVE_FORMAT_STRING : MAIN_FORMAT_STRING;
                int loreAdditionIndex = 0;

                Set<String> infoVariables = infoVariableMap.keySet();

                while (loreAdditionIndex < loreAdditions.length()) {
                    boolean startedWithVariable = false;
                    for (String infoVariable : infoVariables) {
                        if (!loreAdditions.startsWith(infoVariable, loreAdditionIndex)) {
                            continue;
                        }
                        loreAdditionIndex += infoVariable.length();
                        startedWithVariable = true;
                        loreLine.append(
                                infoVariableMap.get(infoVariable).apply(idContainer, statValue));
                    }

                    if (startedWithVariable) continue;

                    int indexOfNextVariable = loreAdditions.indexOf('%', loreAdditionIndex);
                    if (indexOfNextVariable == -1) {
                        loreLine.append(loreAdditions.substring(loreAdditionIndex));
                        break;
                    } else {
                        loreLine.append(
                                loreAdditions.substring(
                                        loreAdditionIndex, indexOfNextVariable - 1));
                        loreAdditionIndex = indexOfNextVariable;
                    }
                }

                newLore.add(ItemUtils.toLoreStringTag(loreLine));

                percentTotal += Math.max(Math.min(percentage, 100), 0);
                idAmount++;

                continue;
            }

            newLore.add(lore.get(i));
        }

        if (hasNew) {
            TextComponent newName = new TextComponent("");

            newName.append(
                    WynnUtils.normalizeBadString(
                            ComponentUtils.getUnformatted(itemStack.getHoverName())));
            newName.append(new TextComponent(" [NEW]").withStyle(ChatFormatting.GOLD));

            itemStack.setHoverName(newName);
        } else if (idAmount > 0) {
            float averagePercentage = percentTotal / (float) idAmount;

            // check for item perfection or 0% items, else put %
            if (!ItemUtils.hasMarker(itemStack, "isPerfect")
                    && !ItemUtils.hasMarker(itemStack, "isDefective")) {
                if (averagePercentage >= 100d) {
                    ItemUtils.addMarker(itemStack, "isPerfect");
                } else if (averagePercentage == 0) {
                    ItemUtils.addMarker(itemStack, "isDefective");
                } else {
                    TextComponent newName = new TextComponent("");

                    newName.append(
                            WynnUtils.normalizeBadString(
                                    ComponentUtils.getUnformatted(itemStack.getHoverName())));
                    Style color = Style.EMPTY.withColor(getPercentageColor(averagePercentage));

                    newName.append(
                            new TextComponent(String.format(" [%.1f%%]", averagePercentage))
                                    .withStyle(color));

                    itemStack.setHoverName(newName);
                }
            }
        }

        if (alternativeForm) ItemUtils.addMarker(itemStack, "loreAlternativeForm");
        else ItemUtils.addMarker(itemStack, "loreMainForm");

        ItemUtils.replaceLore(itemStack, newLore);
    }

    private static MutableComponent getPercentageTextComponent(
            IdentificationContainer identificationContainer, int statValue) {
        float percentage =
                MathUtils.inverseLerp(
                                identificationContainer.getMin(),
                                identificationContainer.getMax(),
                                statValue)
                        * 100;
        Style color = Style.EMPTY.withColor(getPercentageColor(percentage));
        return new TextComponent(String.format("[%.1f%%]", percentage)).withStyle(color);
    }

    private static final TreeMap<Float, TextColor> colorMap =
            new TreeMap<>() {
                {
                    put(15f, TextColor.fromLegacyFormat(ChatFormatting.RED));
                    put(55f, TextColor.fromLegacyFormat(ChatFormatting.YELLOW));
                    put(88f, TextColor.fromLegacyFormat(ChatFormatting.GRAY));
                    put(98f, TextColor.fromLegacyFormat(ChatFormatting.AQUA));
                }
            };

    // FIXME: All values appear as gray rgb(170, 170, 170) instead of green when rolls are around
    // 90%
    private static TextColor getPercentageColor(float percentage) {
        Map.Entry<Float, TextColor> lowerEntry = colorMap.floorEntry(percentage);
        Map.Entry<Float, TextColor> higherEntry = colorMap.ceilingEntry(percentage);

        // Boundary conditions
        if (lowerEntry == null) {
            return higherEntry.getValue();
        } else if (higherEntry == null) {
            return lowerEntry.getValue();
        }

        if (Objects.equals(lowerEntry.getValue(), higherEntry.getValue())) {
            return lowerEntry.getValue();
        }

        float t = MathUtils.inverseLerp(lowerEntry.getKey(), higherEntry.getKey(), percentage);

        int lowerColor = lowerEntry.getValue().getValue();
        int higherColor = lowerEntry.getValue().getValue();

        int r = (int) MathUtils.lerp((lowerColor >> 16) & 0xff, (higherColor >> 16) & 0xff, t);
        int g = (int) MathUtils.lerp((lowerColor >> 8) & 0xff, (higherColor >> 8) & 0xff, t);
        int b = (int) MathUtils.lerp(lowerColor & 0xff, higherColor & 0xff, t);

        return TextColor.fromRgb((r << 16) | (g << 8) | b);
    }

    private TextColor getFlatPercentageColor(float percentage) {
        if (percentage < 30f) {
            return TextColor.fromLegacyFormat(ChatFormatting.RED);
        } else if (percentage < 80f) {
            return TextColor.fromLegacyFormat(ChatFormatting.YELLOW);
        } else if (percentage < 96f) {
            return TextColor.fromLegacyFormat(ChatFormatting.GRAY);
        } else {
            return TextColor.fromLegacyFormat(ChatFormatting.AQUA);
        }
    }
}
