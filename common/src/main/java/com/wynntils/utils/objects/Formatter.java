/*
 * Copyright © Wynntils 2022.
 * This file is released under AGPLv3. See LICENSE for full license details.
 */
package com.wynntils.utils.objects;

import java.util.Map;
import java.util.Set;
import java.util.function.Consumer;
import java.util.function.Function;

public class Formatter {

    // TODO: Check edge cases with this formatting implementations
    public static <T> void doFormat(
            String format,
            Consumer<T> consumer,
            Function<String, T> mapper,
            Map<String, T> infoVariableMap) {
        Set<String> infoVariables = infoVariableMap.keySet();

        int index = 0;
        while (index < format.length()) {
            int indexStartOfNextVariable = format.indexOf('%', index);
            if (indexStartOfNextVariable == -1) {
                break;
            }

            if (index != indexStartOfNextVariable) {
                consumer.accept(mapper.apply(format.substring(index, indexStartOfNextVariable)));
            }

            int indexEndOfNextVariable = format.indexOf('%', index);
            if (indexEndOfNextVariable == -1) {
                break;
            }

            String toMatch = format.substring(indexStartOfNextVariable, indexEndOfNextVariable);

            for (String infoVariable : infoVariableMap.keySet()) {
                if (!toMatch.equals(infoVariable)) {
                    continue;
                }

                index = indexEndOfNextVariable + 1; // skip ending %
                consumer.accept(infoVariableMap.get(infoVariable));
                break;
            }
        }

        consumer.accept(mapper.apply(format.substring(index)));
    }
}
