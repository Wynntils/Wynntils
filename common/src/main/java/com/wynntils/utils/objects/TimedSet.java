/*
 * Copyright © Wynntils 2022.
 * This file is released under AGPLv3. See LICENSE for full license details.
 */
package com.wynntils.utils.objects;

import java.util.HashSet;
import java.util.Iterator;
import java.util.Set;
import java.util.concurrent.TimeUnit;

public class TimedSet<T> implements Iterable<T> {
    private final Set<TimedEntry> entries = new HashSet<>();

    long timeJump;
    boolean autoClear;

    public TimedSet(long duration, TimeUnit unit, boolean autoClear) {
        timeJump = unit.toMillis(duration);
        this.autoClear = autoClear;
    }

    public TimedSet(long duration, TimeUnit unit) {
        this(duration, unit, false);
    }

    public void releaseEntries() {
        Iterator<TimedEntry> it = entries.iterator();
        while (it.hasNext()) {
            TimedEntry entry = it.next();
            if (!entry.shouldRelease()) continue;

            it.remove();
        }
    }

    public void put(T entry) {
        entries.add(new TimedEntry(entry, System.currentTimeMillis() + timeJump));
    }

    public void clear() {
        entries.clear();
    }

    public int size() {
        return entries.size();
    }

    public boolean isEmpty() {
        return size() == 0;
    }

    @Override
    public Iterator<T> iterator() {
        if (autoClear) releaseEntries();

        return new TimedSetIterator();
    }

    private class TimedSetIterator implements Iterator<T> {
        private final Iterator<TimedEntry> original = entries.iterator();

        @Override
        public boolean hasNext() {
            return original.hasNext();
        }

        @Override
        public T next() {
            return original.next().getEntry();
        }
    }

    private final class TimedEntry {
        T entry;
        long expiration;

        private TimedEntry(T entry, long expiration) {
            this.entry = entry;
            this.expiration = expiration;
        }

        public long getExpiration() {
            return expiration;
        }

        public T getEntry() {
            return entry;
        }

        public boolean shouldRelease() {
            return System.currentTimeMillis() >= expiration;
        }
    }
}
