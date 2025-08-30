/*
 * Copyright © Wynntils 2022-2025.
 * This file is released under LGPLv3. See LICENSE for full license details.
 */
package com.wynntils.models.statuseffects.type;

import com.google.common.collect.ComparisonChain;
import com.wynntils.core.text.StyledText;

public class StatusEffect implements Comparable<StatusEffect> {
    private final StyledText fullName;
    private final StyledText name; // The name of the consumable (also used to identify it)
    private final StyledText modifier; // The modifier of the consumable (+100, 23 etc.)
    private final StyledText modifierSuffix; // The suffix of the modifier (/3s, %)
    private final Double modifierValue;
    private final int duration;
    private StyledText displayedTime; // The displayed time remaining. Allows for xx:xx for infinite time effects.
    private StyledText prefix; // The prefix to display before the name. Not included in identifying name.

    public StatusEffect(
            StyledText name,
            StyledText modifier,
            StyledText modifierSuffix,
            StyledText displayedTime,
            StyledText prefix,
            int duration) {
        this.name = name;
        this.displayedTime = displayedTime;
        this.prefix = prefix;
        this.modifier = modifier;
        this.modifierSuffix = modifierSuffix;

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
                modifier == StyledText.EMPTY ? null : Double.parseDouble(modifier.getStringWithoutFormatting());
        this.duration = duration;
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
