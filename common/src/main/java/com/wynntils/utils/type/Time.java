/*
 * Copyright © Wynntils 2025.
 * This file is released under LGPLv3. See LICENSE for full license details.
 */
package com.wynntils.utils.type;

import com.wynntils.utils.StringUtils;

public record Time(long timestamp) {
    // This will technically remove our possibility to express times in 1969 correctly,
    // but since that is not relevant, this is easier than e.g. Long.MAX_VALUE
    public static final Time NONE = Time.of(-1);

    public static Time of(long timestamp) {
        return new Time(timestamp);
    }

    public static Time now() {
        return Time.of(System.currentTimeMillis());
    }

    /**
     * Returns a new Time object offset by the given number of seconds. If seconds is negative,
     * the returned time will be in the past.
     * @param seconds
     * @return
     */
    public Time offset(int seconds) {
        if (this.equals(NONE)) return this;

        return Time.of(timestamp + seconds * 1000L);
    }

    /**
     * Returns the offset in seconds between this time and another time. If the other time is
     * prior to this, the result will be negative.
     * @param other
     * @return
     */
    public long getOffset(Time other) {
        if (this.equals(NONE)) return 0;
        if (other.equals(NONE)) return 0;

        return (other.timestamp - timestamp) / 1000L;
    }

    public String toAbsoluteString() {
        if (this.equals(NONE)) return "";

        return StringUtils.formatDateTime(timestamp);
    }

    @Override
    public String toString() {
        if (this.equals(NONE)) return "";

        return StringUtils.getRelativeTimeString(timestamp);
    }
}
