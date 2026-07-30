/*
 * Copyright © Wynntils 2026.
 * This file is released under LGPLv3. See LICENSE for full license details.
 */
package com.wynntils.templates.backends.compiler.exceptions;

public class TemplateCompileException extends RuntimeException {
    public TemplateCompileException(String message, Throwable cause) {
        super(message, cause);
    }

    public TemplateCompileException(String message) {
        super(message);
    }
}
