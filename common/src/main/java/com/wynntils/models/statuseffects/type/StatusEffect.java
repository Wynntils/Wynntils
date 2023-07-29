/*
 * Copyright © Wynntils 2022-2023.
 * This file is released under AGPLv3. See LICENSE for full license details.
 */
package com.wynntils.models.statuseffects.type;

import com.google.common.collect.ComparisonChain;
import com.wynntils.core.text.StyledText;

public class StatusEffect implements Comparable<StatusEffect> {
    private final StyledText fullName;
    private final StyledText name; // The name of the consumable (also used to identify it)
    private final StyledText modifier; // The modifier of the consumable (+100, 23/3s etc.)
    private StyledText displayedTime; // The displayed time remaining. Allows for xx:xx for infinite time effects.
    private StyledText prefix; // The prefix to display before the name. Not included in identifying name.

    public StatusEffect(StyledText name, StyledText modifier, StyledText displayedTime, StyledText prefix) {
        this.name = name;
        this.displayedTime = displayedTime;
        this.prefix = prefix;
        this.modifier = modifier;

        this.fullName = StyledText.concat(
                prefix,
                StyledText.fromString(" "),
                modifier,
                StyledText.fromString(" "),
                name,
                StyledText.fromString(" "),
                displayedTime);
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

    @Override
    public int compareTo(StatusEffect effect) {
        return ComparisonChain.start()
                .compare(this.getPrefix().getString(), effect.getPrefix().getString())
                .compare(this.getName().getString(), effect.getName().getString())
                .compare(this.getModifier().getString(), effect.getModifier().getString())
                .result();
    }
}
