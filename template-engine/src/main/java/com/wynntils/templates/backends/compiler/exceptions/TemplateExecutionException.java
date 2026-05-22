/*
 * Copyright © Wynntils 2026.
 * This file is released under LGPLv3. See LICENSE for full license details.
 */
package com.wynntils.templates.backends.compiler.exceptions;

public class TemplateExecutionException extends RuntimeException {
    public TemplateExecutionException(String message, Throwable cause) {
        super(message, cause);
    }

    public TemplateExecutionException(String message) {
        super(message);
    }
}
