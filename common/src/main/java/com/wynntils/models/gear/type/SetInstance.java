/*
 * Copyright © Wynntils 2024.
 * This file is released under LGPLv3. See LICENSE for full license details.
 */
package com.wynntils.models.gear.type;

import com.wynntils.models.stats.type.StatType;
import java.util.Map;

/**
 * Represents an "instance" of a unique set worn.
 * <p>
 * wynnCount may be inaccurate when the user has two of the same ring equipped (wynncraft bug).
 * Use SetModel to determine the true count if necessary.
 */
public record SetInstance(
        SetInfo setInfo, Map<String, Boolean> activeItems, int wynnCount, Map<StatType, Integer> wynnBonuses) {}
