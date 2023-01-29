/*
 * Copyright © Wynntils 2022.
 * This file is released under AGPLv3. See LICENSE for full license details.
 */
package com.wynntils.models.ingredients;

import java.util.ArrayList;
import java.util.List;
import java.util.Map;

public class IdentificationInvertedList {
    private final List<String> inverted = new ArrayList<>();

    public IdentificationInvertedList(
            Map<String, Integer> idOrders, ArrayList<String> groupRanges, ArrayList<String> inverted) {}

    /**
     * @param id the identification "short" name. Ex: rawMainAttackNeutralDamage
     * @return if the provided identification status is inverted (negative values are positive)
     */
    public boolean isInverted(String id) {
        return inverted.contains(id);
    }


}
