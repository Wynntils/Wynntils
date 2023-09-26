/*
 * Copyright © Wynntils 2023.
 * This file is released under LGPLv3. See LICENSE for full license details.
 */
package com.wynntils.core.crowdsource;

import com.wynntils.core.components.Managers;
import com.wynntils.core.crowdsource.type.CrowdSourcedDataType;

/**
 * A class that collects a single type of crowd source data.
 * This class is meant to collect data from (an) event(s), do the necessary processing
 * and then call {@link #collect(T)} with the processed data to store it.
 *
 * Be aware that this class is not supposed to do any processing that is not directly
 * related to the data collection. If you need to do any processing that is not directly
 * related to the data collection, you should do that in the corresponding model.
 * @param <T> The class of the data that this collector collects.
 */
public abstract class CrowdSourcedDataCollector<T> {
    protected final void collect(T data) {
        Managers.CrowdSourcedData.putData(getDataType(), data);
    }

    protected abstract CrowdSourcedDataType getDataType();
}
