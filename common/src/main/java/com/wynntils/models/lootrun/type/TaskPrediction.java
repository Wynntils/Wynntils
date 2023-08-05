/*
 * Copyright © Wynntils 2023.
 * This file is released under AGPLv3. See LICENSE for full license details.
 */
package com.wynntils.models.lootrun.type;

import com.wynntils.models.beacons.type.Beacon;

public record TaskPrediction(Beacon beacon, TaskLocation taskLocation, double predictionScore) {}
