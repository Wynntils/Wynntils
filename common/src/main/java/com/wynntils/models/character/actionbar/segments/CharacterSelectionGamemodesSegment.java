package com.wynntils.models.character.actionbar.segments;

import com.wynntils.handlers.actionbar.ActionBarSegment;
import com.wynntils.models.character.type.CharacterGamemode;

import java.util.Collections;
import java.util.Set;

public class CharacterSelectionGamemodesSegment extends ActionBarSegment {
    private final Set<CharacterGamemode> gamemodes;

    public CharacterSelectionGamemodesSegment(
            String segmentText, int startIndex, int endIndex, Set<CharacterGamemode> gamemodes) {
        super(segmentText, startIndex, endIndex);

        this.gamemodes = Set.copyOf(gamemodes);
    }

    public Set<CharacterGamemode> getGamemodes() {
        return gamemodes;
    }

    @Override
    public String toString() {
        return "CharacterSelectionGamemodesSegment{" + "gamemodes="
                + gamemodes + ", segmentText='"
                + segmentText + '\'' + '}';
    }
}