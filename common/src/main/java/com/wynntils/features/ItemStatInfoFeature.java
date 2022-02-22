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
import com.wynntils.utils.objects.Formatter;
import com.wynntils.wc.objects.ClassType;
import com.wynntils.wc.objects.SpellType;
import com.wynntils.wc.objects.items.IdentificationContainer;
import com.wynntils.wc.objects.items.IdentificationModifier;
import com.wynntils.wc.objects.items.ItemProfile;
import com.wynntils.wc.utils.IdentificationOrderer;
import com.wynntils.wc.utils.WynnUtils;
import java.awt.*;
import java.util.*;
import java.util.regex.Matcher;
import java.util.regex.Pattern;
import net.minecraft.ChatFormatting;
import net.minecraft.nbt.*;
import net.minecraft.network.chat.*;
import net.minecraft.network.chat.Component;
import net.minecraft.network.chat.TextComponent;
import net.minecraft.world.item.ItemStack;
import net.minecraftforge.eventbus.api.SubscribeEvent;
import org.lwjgl.glfw.GLFW;

public class ItemStatInfoFeature extends Feature {
    private static final Pattern ITEM_STATUS_PATTERN =
            Pattern.compile(
                    "(^\\+?(?<Value>-?\\d+)(?: to \\+?(?<UpperValue>-?\\d+))?(?<Suffix>%|/\\ds|"
                            + " tier)?(?<Stars>\\*{0,3}) (?<ID>[a-zA-Z 0-9]+))");

    // TODO: Replace these with configs
    // Possible variables: %percentage% %minmax% %reidchance%
    private static final String MAIN_FORMAT_STRING = "%percentage%";
    private static final String ALTERNATIVE_FORMAT_STRING =
            "%percentage% -- - -- %percentage%"; // Used when uses presses SHIFT on lore.

    @Override
    protected void onInit(ImmutableList.Builder<Condition> conditions) {
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
        CompoundTag tag = itemStack.getOrCreateTag();

        if (tag.contains("loreForm")) {
            String loreForm = tag.getString("loreForm");

            if (loreForm.equals("fail")) return;
            if (loreForm.equals("main") && !alternativeForm) return;
            if (loreForm.equals("alternative") && alternativeForm) return;
        }

        String itemName =
                WynnUtils.normalizeBadString(
                        ChatFormatting.stripFormatting(itemStack.getHoverName().getString()));

        if (tag.contains("wynntilsItemName"))
            itemName = tag.getString("wynntilsItemName"); // Reset item name to avoid problems

        if (tag.contains("wynntilsItemNameFormatted"))
            itemStack.setHoverName(new TextComponent(tag.getString("wynntilsItemNameFormatted")));

        if (!WebManager.getItemsMap().containsKey(itemName)) return;

        ItemProfile profile = WebManager.getItemsMap().get(itemName);
        ListTag lore = ItemUtils.getLoreTagElseEmpty(itemStack);

        ListTag newLore = new ListTag();

        // Used to not give set bonuses a different lore
        boolean endOfStatuses = false;

        float percentTotal = 0;
        int idAmount =
                0; // this only counts those ids that are correct and do not have fixed values

        int actualIdAmount = 0;

        boolean hasNew = false;

        CompoundTag ids = new CompoundTag();
        CompoundTag stars = new CompoundTag();

        int idStart = -1; // make sure the condition does not happen

        if (!tag.contains("wynntilsIds")) { // generate ids if not there
            tag.putString("wynntilsItemName", itemName);
            tag.putString("wynntilsItemNameFormatted", itemStack.getHoverName().getString());
            for (int i = 0; i < lore.size(); i++) {
                MutableComponent loreLine = Component.Serializer.fromJson(lore.getString(i));

                if (loreLine == null) {
                    continue;
                }

                String unformattedLoreLine = WynnUtils.normalizeBadString(loreLine.getString());

                if (unformattedLoreLine.equals("Set Bonus:")) {
                    endOfStatuses = true;
                }

                if (endOfStatuses) {
                    continue;
                }

                Matcher statusMatcher = ITEM_STATUS_PATTERN.matcher(unformattedLoreLine);
                if (!statusMatcher.find()) {
                    newLore.add(lore.get(i));
                    continue;
                }

                if (idStart == -1) {
                    idStart = i;
                }

                String idName = statusMatcher.group("ID");
                boolean isRaw = statusMatcher.group("Suffix") == null;
                int starCount = statusMatcher.group("Stars").length();

                SpellType spell = SpellType.fromName(idName);
                if (spell != null) {
                    idName = spell.getGenericName() + " Cost";
                }

                String shortIdName = IdentificationContainer.getAsShortName(idName, isRaw);
                if (starCount != 0) {
                    stars.putInt(shortIdName, starCount);
                }

                ids.putInt(shortIdName, Integer.parseInt(statusMatcher.group("Value")));
            }

            if (idStart == -1 || ids.isEmpty()) {
                tag.putString("loreForm", "fail");
            }

            tag.put("wynntilsIds", ids);
            tag.put("wynntilsStars", stars);
        } else {
            ids = tag.getCompound("wynntilsIds");
            stars = tag.getCompound("wynntilsStars");

            // Added later
            idStart = tag.getInt("wynntilsIdStart");
            int idEnd = tag.getInt("wynntilsIdEnd");

            // filter out old replacement
            for (int i = 0; i < lore.size(); i++) {
                if (idStart <= i && i <= idEnd) {
                    continue;
                }

                newLore.add(lore.get(i));
            }
        }

        // Insert generated lore
        for (String idName : ids.getAllKeys()) {
            int statValue = ids.getInt(idName);

            IdentificationContainer idContainer = profile.getStatuses().get(idName);

            IdentificationModifier type =
                    idContainer != null
                            ? idContainer.getType()
                            : IdentificationContainer.getTypeFromName(idName);

            if (type == null) continue; // not a valid id

            boolean isInverted = IdentificationOrderer.INSTANCE.isInverted(idName);

            // FIXME: These added lores are always italic for some reason
            MutableComponent loreLine =
                    new TextComponent(
                                    (statValue > 0 ? "+" : "") + statValue + type.getInGame(idName))
                            .withStyle(Style.EMPTY);

            String longName = IdentificationContainer.getAsLongName(idName);

            // FIXME: Use actual class spell names (needs current class detection implementation)
            SpellType spell = SpellType.fromName(longName);
            if (spell != null) {
                longName = spell.forOtherClass(ClassType.None).getName() + " Spell Cost";
            }

            if (isInverted ^ (statValue > 0)) {
                loreLine.setStyle(Style.EMPTY.withColor(ChatFormatting.GREEN));
            } else {
                loreLine.setStyle(Style.EMPTY.withColor(ChatFormatting.RED));
            }

            int starsCount = stars.getInt(idName);

            // Add stars
            if (stars.contains(idName)) // TODO: Add a config option whether to show id stars or not
            loreLine.append(
                        new TextComponent("***".substring(3 - starsCount))
                                .withStyle(ChatFormatting.DARK_GREEN));

            loreLine.append(new TextComponent(" " + longName).withStyle(ChatFormatting.GREEN));

            actualIdAmount++;

            if (idContainer == null || idContainer.isInvalidValue(statValue)) { // id not in api
                loreLine.append(new TextComponent(" [NEW]").withStyle(ChatFormatting.GOLD));
                newLore.add(idStart, ItemUtils.toLoreStringTag(loreLine));
                hasNew = true;
                continue;
            }

            if (idContainer.hasConstantValue()) {
                newLore.add(idStart, ItemUtils.toLoreStringTag(loreLine));
                continue;
            }

            loreLine.append(" ");

            int min = idContainer.getMin();
            int max = idContainer.getMax();
            float percentage = MathUtils.inverseLerp(min, max, statValue) * 100;

            String loreFormat = alternativeForm ? ALTERNATIVE_FORMAT_STRING : MAIN_FORMAT_STRING;

            Map<String, Component> infoVariables = new HashMap<>();

            IdentificationContainer.ReidentificationChances chances =
                    idContainer.getChances(statValue, isInverted, starsCount);

            infoVariables.put("percentage", getPercentageTextComponent(percentage));

            // TODO add escaped chars like old impl for special chars
            infoVariables.put(
                    "chance_perfect",
                    new TextComponent(
                            String.format("\u2605%.1f%%", idContainer.getPerfectChance())));
            infoVariables.put(
                    "chance_increase",
                    new TextComponent(String.format("\u21E7%.0f%%", chances.increase())));
            infoVariables.put(
                    "chance_decrease",
                    new TextComponent(String.format("\u21E9%.0f%%", chances.decrease())));

            infoVariables.put("min", new TextComponent(String.valueOf(min)));
            infoVariables.put("max", new TextComponent(String.valueOf(max)));

            // TODO: Check edge cases with this formatting implementations
            Formatter.doFormat(loreFormat, loreLine::append, TextComponent::new, infoVariables);

            percentTotal += percentage;
            idAmount++;
            actualIdAmount++;

            newLore.add(idStart, ItemUtils.toLoreStringTag(loreLine));
        }

        // Generate new name
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

        tag.putInt("wynntilsIdStart", idStart);
        tag.putInt("wynntilsIdEnd", idStart + actualIdAmount);

        if (alternativeForm) {
            tag.putString("loreForm", "alternative");
        } else {
            tag.putString("loreForm", "main");
        }

        ItemUtils.replaceLore(itemStack, newLore);
    }

    private static MutableComponent getPercentageTextComponent(float percentage) {

        Style color = Style.EMPTY.withColor(getPercentageColor(percentage));
        return new TextComponent(String.format("[%.1f%%]", percentage)).withStyle(color);
    }

    private static final TreeMap<Float, TextColor> colorMap =
            new TreeMap<>() {
                {
                    put(15f, TextColor.fromLegacyFormat(ChatFormatting.RED));
                    put(55f, TextColor.fromLegacyFormat(ChatFormatting.YELLOW));
                    put(88f, TextColor.fromLegacyFormat(ChatFormatting.GREEN));
                    put(98f, TextColor.fromLegacyFormat(ChatFormatting.AQUA));
                }
            };

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
            return TextColor.fromLegacyFormat(ChatFormatting.GREEN);
        } else {
            return TextColor.fromLegacyFormat(ChatFormatting.AQUA);
        }
    }
}
