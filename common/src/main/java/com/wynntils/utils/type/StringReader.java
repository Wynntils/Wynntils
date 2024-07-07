/*
 * Copyright © Wynntils 2024.
 * This file is released under LGPLv3. See LICENSE for full license details.
 */
package com.wynntils.utils.type;

public class StringReader {
    private final String string;
    private int index;

    public StringReader(String string) {
        this.string = string;
        this.index = 0;
    }

    public char peek() {
        if (index >= string.length()) {
            throw new ArrayIndexOutOfBoundsException("Tried to read past the end of the string");
        }

        return string.charAt(index);
    }

    public char read() {
        if (index >= string.length()) {
            throw new ArrayIndexOutOfBoundsException("Tried to read past the end of the string");
        }

        return string.charAt(index++);
    }

    public String read(int length) {
        if (index + length > string.length()) {
            throw new ArrayIndexOutOfBoundsException("Tried to read past the end of the string");
        }

        String result = string.substring(index, index + length);
        index += length;
        return result;
    }

    public String readRemaining() {
        return read(string.length() - index);
    }

    public boolean hasRemaining() {
        return index < string.length();
    }
}
