/*
 * Copyright © Wynntils 2026.
 * This file is released under LGPLv3. See LICENSE for full license details.
 */
package com.wynntils.templates.backends;

import com.wynntils.templates.language.Template;

public interface TemplateBackend {
    String evaluate(Template template);
}
