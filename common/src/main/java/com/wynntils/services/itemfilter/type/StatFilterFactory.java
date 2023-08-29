/*
 * Copyright © Wynntils 2023.
 * This file is released under LGPLv3. See LICENSE for full license details.
 */
package com.wynntils.services.itemfilter.type;

import java.util.Optional;

/**
 * A factory for creating {@link StatFilter} instances.
 * @param <T>
 */
public interface StatFilterFactory<T> {
    Optional<T> create(String inputString);
}
