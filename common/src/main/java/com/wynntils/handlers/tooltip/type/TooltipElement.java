/*
 * Copyright © Wynntils 2026.
 * This file is released under LGPLv3. See LICENSE for full license details.
 */
package com.wynntils.handlers.tooltip.type;

public sealed interface TooltipElement permits TooltipLine, TooltipGrid, TooltipWrappedText {}
