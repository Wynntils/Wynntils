/*
 * Copyright © Wynntils 2022.
 * This file is released under AGPLv3. See LICENSE for full license details.
 */
package com.wynntils.core.net;

import java.io.InputStream;
import java.util.function.Function;
import java.util.function.Supplier;

public class NetAction {
    Supplier<InputStream> opener;
    Supplier<Object> processor;
}
