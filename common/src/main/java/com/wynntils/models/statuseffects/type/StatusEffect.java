/*
 * Copyright © Wynntils 2022-2023.
 * This file is released under AGPLv3. See LICENSE for full license details.
 */
package com.wynntils.models.statuseffects.type;

import com.google.common.collect.ComparisonChain;
import com.wynntils.core.text.StyledText;
import java.util.Optional;
import net.minecraft.ChatFormatting;

public class StatusEffect implements Comparable<StatusEffect> {
    private final StyledText fullName;
    private final StyledText name; // The name of the consumable (also used to identify it)
    private final StyledText modifier; // The modifier of the consumable (+100, 23/3s etc.)
    private final StyledText modifierSuffix; // The suffix of the modifier (/3s, %)
    private final Optional<Double> modifierValue;
    private StyledText displayedTime; // The displayed time remaining. Allows for xx:xx for infinite time effects.
    private StyledText prefix; // The prefix to display before the name. Not included in identifying name.


    public StatusEffect(StyledText name, StyledText modifier, StyledText displayedTime, StyledText prefix) {
        this.name = name;
        this.displayedTime = displayedTime;
        this.prefix = prefix;
        this.modifier = modifier;
        
        if( modifier == StyledText.EMPTY ){
            this.modifierSuffix = StyledText.EMPTY;
            this.modifierValue = Optional.empty();
        } else {
            this.modifierSuffix = readModifierSuffix(this.modifier);
            this.modifierValue = readModifierValue(this.modifier, this.modifierSuffix);
        }

        this.fullName = StyledText.concat(
                prefix,
                StyledText.fromString(" "),
                modifier,
                StyledText.fromString(" "),
                name,
                StyledText.fromString(" "),
                displayedTime);
    }

    private StyledText readModifierSuffix(StyledText modifier){
        if( modifier == StyledText.EMPTY) return StyledText.EMPTY;

        String modifierString = modifier.getString();
        int dashIndex = modifierString.indexOf('/');
        int percentIndex = modifierString.indexOf('%');
        int prefixIndex = Math.max(dashIndex, percentIndex);
        
        if( prefixIndex == -1 ){
            return StyledText.EMPTY;
        }
        return StyledText.fromString(ChatFormatting.GRAY + modifierString.substring(prefixIndex));
    }

    private Optional<Double> readModifierValue(StyledText modifier, StyledText modifierSuffix){
        String modifierStr = modifier.getStringWithoutFormatting();
        if( modifierStr.isEmpty() ){
            return Optional.empty();
        }

        int end = Math.max(modifierStr.indexOf('%'), modifierStr.indexOf('/'));
        if( end == -1 ) end = modifierStr.length();

        double val = Double.parseDouble(modifierStr.substring(0, end));
        return Optional.of(val);
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

    public StyledText getModifierSuffix(){
        return this.modifierSuffix;
    }

    public boolean hasModifierValue(){
        return this.modifierValue.isPresent();
    }

    public double getModifierValue(){
        return this.modifierValue.get();
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
