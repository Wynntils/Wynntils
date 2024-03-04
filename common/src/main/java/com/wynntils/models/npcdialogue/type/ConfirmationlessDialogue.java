/*
 * Copyright © Wynntils 2024.
 * This file is released under LGPLv3. See LICENSE for full license details.
 */
package com.wynntils.models.npcdialogue.type;

import com.wynntils.core.text.StyledText;
import java.util.List;

public record ConfirmationlessDialogue(List<StyledText> text, long addTime, long removeTime) {}
