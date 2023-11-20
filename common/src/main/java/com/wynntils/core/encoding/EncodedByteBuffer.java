/*
 * Copyright © Wynntils 2023.
 * This file is released under LGPLv3. See LICENSE for full license details.
 */
package com.wynntils.core.encoding;

// byte[] <-> UTF-16, Base64, etc.
public final class EncodedByteBuffer {
    private byte[] bytes;

    private EncodedByteBuffer(byte[] bytes) {
        this.bytes = bytes;
    }

    public static EncodedByteBuffer fromBytes(byte[] bytes) {
        return new EncodedByteBuffer(bytes);
    }

    public static EncodedByteBuffer fromUtf16String(String string) {
        // UTF-16 -> byte extraction
        return null;
    }

    public static EncodedByteBuffer fromBase64String(String string) {
        // Base64 -> byte extraction
        return null;
    }
}
