/*
 * Copyright © Wynntils 2025.
 * This file is released under LGPLv3. See LICENSE for full license details.
 */
package com.wynntils.services.itemweight.type;

import java.util.Map;

public record ItemWeighting(String weightName, Map<String, Double> identifications) {}
