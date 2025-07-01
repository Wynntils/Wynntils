/*
 * Copyright © Wynntils 2022-2025.
 * This file is released under LGPLv3. See LICENSE for full license details.
 */
package com.wynntils.models.statuseffects.type;

import com.google.common.collect.ComparisonChain;
import com.wynntils.core.text.PartStyle;
import com.wynntils.core.text.StyledText;
import java.util.regex.Matcher;
import java.util.regex.Pattern;

public class StatusEffect implements Comparable<StatusEffect> {
    private static final Pattern TIME_PATTERN = Pattern.compile("\\((\\d+):(\\d+)\\)");
    private final StyledText fullName;
    private final StyledText name; // The name of the consumable (also used to identify it)
    private final StyledText modifier; // The modifier of the consumable (+100, 23 etc.)
    private final StyledText modifierSuffix; // The suffix of the modifier (/3s, %)
    private final Double modifierValue;
    private final int duration;
    private StyledText displayedTime; // The displayed time remaining. Allows for xx:xx for infinite time effects.
    private StyledText prefix; // The prefix to display before the name. Not included in identifying name.

    private int setDuration() {
        Matcher timeMatcher = getDisplayedTime().getMatcher(TIME_PATTERN, PartStyle.StyleType.NONE);
        if (timeMatcher.matches()) {
            return Integer.parseInt(timeMatcher.group(1)) * 60 + Integer.parseInt(timeMatcher.group(2));
        }
        return -1;
    }

    public StatusEffect(
            StyledText name,
            StyledText modifier,
            StyledText modifierSuffix,
            StyledText displayedTime,
            StyledText prefix) {
        this.name = name;
        this.displayedTime = displayedTime;
        this.prefix = prefix;
        this.modifier = modifier;
        this.modifierSuffix = modifierSuffix;
        this.duration = setDuration();

        this.fullName = StyledText.concat(
                prefix,
                StyledText.fromString(" "),
                modifier,
                modifierSuffix,
                StyledText.fromString(" "),
                name,
                StyledText.fromString(" "),
                displayedTime);
        this.modifierValue =
                modifier != StyledText.EMPTY ? Double.parseDouble(modifier.getStringWithoutFormatting()) : null;
    }

    /**
     * @return The name of the consumable
     */
    public StyledText getName() {
        return name;
    }

    /**
     * @return The modifier of the consumable
     */
    public StyledText getModifier() {
        return modifier;
    }

    /**
     * @return The time remaining for the consumable
     */
    public StyledText getDisplayedTime() {
        return displayedTime;
    }

    /**
     * @param displayedTime The new time remaining for the consumable
     */
    public void setDisplayedTime(StyledText displayedTime) {
        this.displayedTime = displayedTime;
    }

    /**
     * @return The prefix to display before the name
     */
    public StyledText getPrefix() {
        return prefix;
    }

    /**
     * @param prefix The new prefix to display before the name
     */
    public void setPrefix(StyledText prefix) {
        this.prefix = prefix;
    }

    public StyledText asString() {
        return fullName;
    }

    public StyledText getModifierSuffix() {
        return this.modifierSuffix;
    }

    public boolean hasModifierValue() {
        return this.modifierValue != null;
    }

    public double getModifierValue() {
        return this.modifierValue;
    }

    /**
     * @return Time in seconds, -1 for infinite
     */
    public int getDuration() {
        return duration;
    }

    @Override
    public int compareTo(StatusEffect effect) {
        return ComparisonChain.start()
                .compare(this.getPrefix().getString(), effect.getPrefix().getString())
                .compare(this.getName().getString(), effect.getName().getString())
                .compare(this.getModifier().getString(), effect.getModifier().getString())
                .result();
    }
}
