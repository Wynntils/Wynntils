/*
 * Copyright © Wynntils 2026.
 * This file is released under LGPLv3. See LICENSE for full license details.
 */
package com.wynntils.models.character.type;

import com.wynntils.models.rewards.type.TomeType;
import java.util.List;

public record SavableTomeSet(List<SavableTome> tomes) {
    public SavableTomeSet(List<SavableTome> tomes) {
        this.tomes = tomes == null ? List.of() : List.copyOf(tomes);
    }

    public SavableTomeSet() {
        this(List.of());
    }

    public List<SavableTome> getTomes(TomeType type) {
        return tomes.stream().filter(tome -> tome.type() == type).toList();
    }

    public List<SavableTome> getAllTomes() {
        return tomes;
    }

    public boolean isEmpty() {
        return tomes.isEmpty();
    }
}
