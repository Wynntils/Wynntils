/*
 * Copyright © Wynntils 2023-2024.
 * This file is released under LGPLv3. See LICENSE for full license details.
 */
package com.wynntils.utils.type;

import java.util.Arrays;

public class ArrayReader<T> {
    private final T[] array;
    private int index;

    public ArrayReader(T[] array) {
        this.array = Arrays.copyOf(array, array.length);
        this.index = 0;
    }

    public T peek() {
        // Throw an exception if we try to read past the end of the array
        if (index >= array.length) {
            throw new ArrayIndexOutOfBoundsException("Tried to read past the end of the array");
        }

        return array[index];
    }

    public T read() {
        // Throw an exception if we try to read past the end of the array
        if (index >= array.length) {
            throw new ArrayIndexOutOfBoundsException("Tried to read past the end of the array");
        }

        return array[index++];
    }

    public T[] read(int length) {
        // Throw an exception if we try to read past the end of the array
        if (index + length > array.length) {
            throw new ArrayIndexOutOfBoundsException("Tried to read past the end of the array");
        }

        T[] result = Arrays.copyOfRange(array, index, index + length);
        index += length;
        return result;
    }

    public T[] readRemaining() {
        return read(array.length - index);
    }

    public boolean hasRemaining() {
        return index < array.length;
    }
}
