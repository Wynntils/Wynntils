/*
 * Copyright © Wynntils 2026.
 * This file is released under LGPLv3. See LICENSE for full license details.
 */
package com.wynntils.models.characterstats.actionbar.matchers;

import com.wynntils.core.WynntilsMod;
import com.wynntils.handlers.actionbar.ActionBarSegment;
import com.wynntils.models.characterstats.actionbar.segments.MeterBarSegment;
import com.wynntils.models.characterstats.type.MeterBarInfo;
import com.wynntils.utils.type.CappedValue;
import java.util.List;
import java.util.regex.Pattern;

public class UltimateMeterBarSegmentMatcher extends AbstractMeterSegmentMatcher {
    // Number of steps in the meter bar
    private static final int ACTION_STEPS = 32;
    private static final CappedValue FULL_METER = new CappedValue(ACTION_STEPS, ACTION_STEPS);

    // All possible meter character ranges, without the animation characters, extracted from the resource pack/font
    // Sprint meter characters
    private static final String SPRINT_METER_CHARACTERS = "\uE4F0-\uE50F";

    // Breath meter characters
    private static final String BREATH_METER_CHARACTERS = "\uE510-\uE52F";

    private static final List<String> METER_CHARACTERS = List.of(SPRINT_METER_CHARACTERS, BREATH_METER_CHARACTERS);
    private static final Pattern BREATH_METER_PATTERN = Pattern.compile("[" + BREATH_METER_CHARACTERS + "]");
    private static final Pattern SPRINT_METER_PATTERN = Pattern.compile("[" + SPRINT_METER_CHARACTERS + "]");

    @Override
    protected boolean isUltimateMeter() {
        return true;
    }

    @Override
    protected List<String> getCharacterRange() {
        return METER_CHARACTERS;
    }

    @Override
    protected ActionBarSegment createSegment(String segmentText, int startIndex, int endIndex, String segmentValue) {
        MeterBarInfo meterBarInfo = fromSegmentText(segmentValue);
        return new MeterBarSegment(segmentText, startIndex, endIndex, meterBarInfo);
    }

    private MeterBarInfo fromSegmentText(String segmentValue) {
        // Find the action type from the character range
        MeterBarInfo.MeterActionType actionType;
        char firstActionCharacter;

        char meterChar = segmentValue.charAt(0);

        if (SPRINT_METER_PATTERN.matcher(segmentValue).matches()) {
            actionType = MeterBarInfo.MeterActionType.SPRINT;
            firstActionCharacter = SPRINT_METER_CHARACTERS.charAt(0);
        } else if (BREATH_METER_PATTERN.matcher(segmentValue).matches()) {
            actionType = MeterBarInfo.MeterActionType.BREATH;
            firstActionCharacter = BREATH_METER_CHARACTERS.charAt(0);
        } else {
            WynntilsMod.warn("Unknown meter bar action type: " + segmentValue);
            return MeterBarInfo.EMPTY;
        }

        // Calculate the current action step
        int actionStep = ACTION_STEPS - (meterChar - firstActionCharacter);

        // Sanity check the action step
        if (actionStep < 0 || actionStep > ACTION_STEPS) {
            WynntilsMod.warn("Invalid meter bar action step: " + actionStep);
            return MeterBarInfo.EMPTY;
        }

        // Return the action type and step
        return new MeterBarInfo(actionType, new CappedValue(actionStep, ACTION_STEPS));
    }
}
