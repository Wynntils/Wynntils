/*
 * Copyright © Wynntils 2022.
 * This file is released under AGPLv3. See LICENSE for full license details.
 */
package com.wynntils.utils.objects;

import org.jetbrains.annotations.NotNull;

public interface TriPredicate<S, T, U> {
    boolean test(S s, T t, U u);

    @NotNull
    default TriPredicate<S, T, U> and(@NotNull TriPredicate<? super S, ? super T, ? super U> other) {
        return (S s, T t, U u) -> this.test(s, t, u) && other.test(s, t, u);
    }

    @NotNull
    default TriPredicate<S, T, U> negate() {
        return (S s, T t, U u) -> !this.test(s, t, u);
    }

    @NotNull
    default TriPredicate<S, T, U> or(@NotNull TriPredicate<? super S, ? super T, ? super U> other) {
        return (S s, T t, U u) -> this.test(s, t, u) || other.test(s, t, u);
    }
}
