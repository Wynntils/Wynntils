/*
 * Copyright © Wynntils 2024.
 * This file is released under LGPLv3. See LICENSE for full license details.
 */
package com.wynntils.screens.territorymanagement.type;

import com.wynntils.utils.colors.CustomColor;
import java.util.List;

public record TerritoryColor(CustomColor borderColor, List<CustomColor> backgroundColors) {}
